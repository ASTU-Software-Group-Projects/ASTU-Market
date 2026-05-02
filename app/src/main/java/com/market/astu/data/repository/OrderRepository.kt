package com.market.astu.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.market.astu.data.model.CartItem
import com.market.astu.data.model.MarketOrder
import com.market.astu.data.model.OrderStatus
import com.market.astu.data.model.PaymentStatus
import com.market.astu.data.model.PayoutStatus
import com.market.astu.data.model.Product
import com.market.astu.data.model.User
import com.market.astu.data.model.Wallet
import com.market.astu.data.model.WalletTransaction
import com.market.astu.data.model.WalletTransactionType
import com.market.astu.util.calculateOrderPricing
import com.market.astu.util.canBeCancelledBy
import com.market.astu.util.canBeDeliveredBy
import com.market.astu.util.canBeDisputedBy
import com.market.astu.util.canBeMarkedInTransitBy
import com.market.astu.util.canBeMarkedPickedUpBy
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Please sign in to manage orders.")
    }

    private fun walletRef(uid: String) = firestore.collection("wallets").document(uid)

    private fun userRef(uid: String) = firestore.collection("users").document(uid)

    private fun productRef(productId: String) = firestore.collection("products").document(productId)

    private fun orderRef(orderId: String) = firestore.collection("orders").document(orderId)

    private fun walletTransactionRef(uid: String) = walletRef(uid).collection("transactions").document()

    private fun createDeliveryCode(): String = (1000..9999).random().toString()

    suspend fun getOrdersForCurrentUser(): Result<List<MarketOrder>> {
        return try {
            val uid = requireUserId()
            val buyerOrders = firestore.collection("orders")
                .whereEqualTo("buyerId", uid)
                .get()
                .await()
                .documents
            val sellerOrders = firestore.collection("orders")
                .whereEqualTo("sellerId", uid)
                .get()
                .await()
                .documents
            val runnerOrders = firestore.collection("orders")
                .whereEqualTo("runnerId", uid)
                .get()
                .await()
                .documents

            val orders = (buyerOrders + sellerOrders + runnerOrders)
                .distinctBy { it.id }
                .mapNotNull { document ->
                    document.toObject(MarketOrder::class.java)?.copy(id = document.id)
                }
                .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkoutCart(): Result<Int> {
        return try {
            val uid = requireUserId()
            val buyerProfile = firestore.collection("users").document(uid).get().await()
                .toObject(User::class.java)
                ?: throw IllegalStateException("Profile not found.")
            val cartSnapshot = firestore.collection("users").document(uid).collection("cart").get().await()
            val cartItems = cartSnapshot.documents.mapNotNull { document ->
                document.toObject(CartItem::class.java)
            }
            if (cartItems.isEmpty()) {
                throw IllegalStateException("Your cart is empty.")
            }

            val enrichedItems = cartItems.map { cartItem ->
                val productDoc = firestore.collection("products").document(cartItem.productId).get().await()
                val product = productDoc.toObject(Product::class.java)?.copy(id = productDoc.id)
                    ?: throw IllegalStateException("${cartItem.name.ifBlank { "A product" }} is no longer available.")
                if (!product.isVisibleToBuyers()) {
                    throw IllegalStateException("${product.name.ifBlank { "A product" }} is currently unavailable.")
                }
                if (cartItem.quantity > product.stockQuantity) {
                    throw IllegalStateException("Only ${product.stockQuantity} ${product.name} item(s) remain.")
                }
                product to cartItem
            }

            val total = enrichedItems.sumOf { (_, item) -> item.price * item.quantity }

            val buyerWalletRef = walletRef(uid)
            val purchaseTxRef = walletTransactionRef(uid)
            val orderRefs = enrichedItems.map { firestore.collection("orders").document() }
            val cartRefs = cartSnapshot.documents.associateBy({ it.id }, { it.reference })
            val deliveryCodes = enrichedItems.associate { (_, item) -> item.productId to createDeliveryCode() }

            firestore.runTransaction { transaction ->
                val wallet = transaction.get(buyerWalletRef).toObject(Wallet::class.java) ?: Wallet(uid = uid)
                if (wallet.balance < total) {
                    throw IllegalStateException("Insufficient CAMPUS balance. Top up your wallet first.")
                }
                val now = Timestamp.now()

                transaction.set(
                    buyerWalletRef,
                    wallet.copy(
                        uid = uid,
                        balance = wallet.balance - total,
                        updatedAt = now
                    )
                )
                transaction.set(
                    purchaseTxRef,
                    WalletTransaction(
                        amount = -total,
                        type = WalletTransactionType.PURCHASE.name,
                        description = "Escrow created for ${enrichedItems.size} order(s)",
                        timestamp = now
                    )
                )

                enrichedItems.forEachIndexed { index, (product, item) ->
                    val currentProduct = transaction.get(productRef(product.id))
                        .toObject(Product::class.java)
                        ?.copy(id = product.id)
                        ?: throw IllegalStateException("${product.name} is no longer available.")
                    if (!currentProduct.isVisibleToBuyers()) {
                        throw IllegalStateException("${currentProduct.name} is currently unavailable.")
                    }
                    if (item.quantity > currentProduct.stockQuantity) {
                        throw IllegalStateException("Only ${currentProduct.stockQuantity} ${currentProduct.name} item(s) remain.")
                    }
                    val lineTotal = item.price * item.quantity
                    val pricing = calculateOrderPricing(lineTotal)
                    val remainingStock = currentProduct.stockQuantity - item.quantity
                    val order = MarketOrder(
                        id = orderRefs[index].id,
                        buyerId = uid,
                        buyerName = buyerProfile.displayName,
                        sellerId = currentProduct.sellerId,
                        sellerName = currentProduct.sellerName.ifBlank { "Campus seller" },
                        productId = currentProduct.id,
                        productName = currentProduct.name,
                        productImageUrl = currentProduct.imageUrl,
                        pickupLocation = currentProduct.pickupLocation.ifBlank { "Seller pickup point" },
                        meetupLocation = buyerProfile.address,
                        quantity = item.quantity,
                        escrowedAmount = pricing.buyerCharge,
                        runnerReward = pricing.runnerReward,
                        protocolFee = pricing.protocolFee,
                        sellerPayoutAmount = pricing.sellerPayout,
                        insuranceCovered = false,
                        insuranceAmount = pricing.buyerCharge,
                        deliveryToken = deliveryCodes[item.productId],
                        paymentStatus = PaymentStatus.HELD_IN_ESCROW.name,
                        sellerPayoutStatus = PayoutStatus.PENDING.name,
                        runnerPayoutStatus = PayoutStatus.PENDING.name,
                        paymentReference = purchaseTxRef.id,
                        status = OrderStatus.AWAITING_RUNNER.name,
                        createdAt = now,
                        updatedAt = now,
                        timeoutAt = Timestamp(now.seconds + 45 * 60, 0)
                    )
                    transaction.set(orderRefs[index], order)
                    transaction.set(
                        productRef(currentProduct.id),
                        currentProduct.copy(
                            stockQuantity = remainingStock,
                            isAvailable = currentProduct.isAvailable && remainingStock > 0,
                            updatedAt = now
                        )
                    )
                    cartRefs[item.productId]?.let { transaction.delete(it) }
                }
            }.await()

            Result.success(enrichedItems.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markPickedUp(orderId: String): Result<Unit> {
        return runRunnerTransition(orderId, "picked up") { order ->
            if (!order.canBeMarkedPickedUpBy(requireUserId())) {
                throw IllegalStateException("Only the assigned runner can confirm pickup.")
            }
            order.copy(
                status = OrderStatus.PICKED_UP.name,
                pickedUpAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        }
    }

    suspend fun markInTransit(orderId: String): Result<Unit> {
        return runRunnerTransition(orderId, "in transit") { order ->
            if (!order.canBeMarkedInTransitBy(requireUserId())) {
                throw IllegalStateException("Pickup must be confirmed before transit starts.")
            }
            order.copy(
                status = OrderStatus.IN_TRANSIT.name,
                inTransitAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        }
    }

    suspend fun markDelivered(
        orderId: String,
        confirmationCode: String,
        proofNote: String
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val orderRef = orderRef(orderId)
            val runnerRef = userRef(uid)
            firestore.runTransaction { transaction ->
                val order = transaction.get(orderRef).toObject(MarketOrder::class.java)?.copy(id = orderId)
                    ?: throw IllegalStateException("Order not found.")
                if (!order.canBeDeliveredBy(uid)) {
                    throw IllegalStateException("Only the assigned runner can complete delivery.")
                }
                if (order.deliveryToken?.trim().orEmpty() != confirmationCode.trim()) {
                    throw IllegalStateException("Delivery confirmation code is incorrect.")
                }
                val sellerWalletRef = walletRef(order.sellerId)
                val runnerWalletRef = walletRef(uid)
                val sellerWallet = transaction.get(sellerWalletRef).toObject(Wallet::class.java) ?: Wallet(uid = order.sellerId)
                val runnerWallet = transaction.get(runnerWalletRef).toObject(Wallet::class.java) ?: Wallet(uid = uid)
                val runner = transaction.get(runnerRef).toObject(User::class.java)
                    ?: throw IllegalStateException("Runner profile not found.")
                val now = Timestamp.now()

                transaction.set(
                    sellerWalletRef,
                    sellerWallet.copy(
                        uid = order.sellerId,
                        balance = sellerWallet.balance + order.sellerPayoutAmount,
                        updatedAt = now
                    )
                )
                transaction.set(
                    runnerWalletRef,
                    runnerWallet.copy(
                        uid = uid,
                        balance = runnerWallet.balance + order.runnerReward,
                        updatedAt = now
                    )
                )
                transaction.set(
                    walletTransactionRef(order.sellerId),
                    WalletTransaction(
                        amount = order.sellerPayoutAmount,
                        type = WalletTransactionType.SELLER_EARNING.name,
                        orderId = order.id,
                        description = "Released seller payout for ${order.productName}",
                        timestamp = now
                    )
                )
                transaction.set(
                    walletTransactionRef(uid),
                    WalletTransaction(
                        amount = order.runnerReward,
                        type = WalletTransactionType.RUNNER_REWARD.name,
                        orderId = order.id,
                        description = "Released runner reward for ${order.productName}",
                        timestamp = now
                    )
                )
                transaction.set(
                    orderRef,
                    order.copy(
                        status = OrderStatus.DELIVERED.name,
                        paymentStatus = PaymentStatus.RELEASED.name,
                        sellerPayoutStatus = PayoutStatus.RELEASED.name,
                        runnerPayoutStatus = PayoutStatus.RELEASED.name,
                        deliveryProofNote = proofNote.trim(),
                        deliveredAt = now,
                        updatedAt = now
                    )
                )
                transaction.set(
                    runnerRef,
                    runner.copy(
                        trustScore = (runner.trustScore + 2).coerceAtMost(100),
                        completedDeliveries = runner.completedDeliveries + 1,
                        activeDeliveryCount = (runner.activeDeliveryCount - 1).coerceAtLeast(0)
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrder(orderId: String, reason: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val orderRef = orderRef(orderId)
            firestore.runTransaction { transaction ->
                val order = transaction.get(orderRef).toObject(MarketOrder::class.java)?.copy(id = orderId)
                    ?: throw IllegalStateException("Order not found.")
                if (!order.canBeCancelledBy(uid)) {
                    throw IllegalStateException("This order can no longer be cancelled.")
                }
                val now = Timestamp.now()
                val buyerWalletRef = walletRef(order.buyerId)
                val buyerWallet = transaction.get(buyerWalletRef).toObject(Wallet::class.java) ?: Wallet(uid = order.buyerId)
                val productSnapshot = transaction.get(productRef(order.productId))
                val currentProduct = productSnapshot.toObject(Product::class.java)?.copy(id = order.productId)

                transaction.set(
                    buyerWalletRef,
                    buyerWallet.copy(
                        uid = order.buyerId,
                        balance = buyerWallet.balance + order.escrowedAmount,
                        updatedAt = now
                    )
                )
                transaction.set(
                    walletTransactionRef(order.buyerId),
                    WalletTransaction(
                        amount = order.escrowedAmount,
                        type = WalletTransactionType.REFUND.name,
                        orderId = order.id,
                        description = "Refund for cancelled order ${order.productName}",
                        timestamp = now
                    )
                )
                currentProduct?.let { product ->
                    transaction.set(
                        productRef(order.productId),
                        product.copy(
                            stockQuantity = product.stockQuantity + order.quantity,
                            isAvailable = true,
                            updatedAt = now
                        )
                    )
                }
                val assignedRunnerId = order.runnerId
                if (!assignedRunnerId.isNullOrBlank()) {
                    val runnerRef = userRef(assignedRunnerId)
                    val runner = transaction.get(runnerRef).toObject(User::class.java)
                    if (runner != null) {
                        transaction.set(
                            runnerRef,
                            runner.copy(activeDeliveryCount = (runner.activeDeliveryCount - 1).coerceAtLeast(0))
                        )
                    }
                }
                transaction.set(
                    orderRef,
                    order.copy(
                        status = OrderStatus.CANCELLED.name,
                        paymentStatus = PaymentStatus.REFUNDED.name,
                        sellerPayoutStatus = PayoutStatus.BLOCKED.name,
                        runnerPayoutStatus = PayoutStatus.BLOCKED.name,
                        cancellationReason = reason.trim(),
                        cancelledAt = now,
                        updatedAt = now
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun openDispute(orderId: String, reason: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val orderRef = orderRef(orderId)
            firestore.runTransaction { transaction ->
                val order = transaction.get(orderRef).toObject(MarketOrder::class.java)?.copy(id = orderId)
                    ?: throw IllegalStateException("Order not found.")
                if (!order.canBeDisputedBy(uid)) {
                    throw IllegalStateException("This order cannot be disputed in its current state.")
                }
                val now = Timestamp.now()
                transaction.set(
                    orderRef,
                    order.copy(
                        status = OrderStatus.DISPUTED.name,
                        paymentStatus = PaymentStatus.DISPUTED.name,
                        disputeReason = reason.trim(),
                        disputedAt = now,
                        updatedAt = now
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun runRunnerTransition(
        orderId: String,
        transitionName: String,
        transform: (MarketOrder) -> MarketOrder
    ): Result<Unit> {
        return try {
            val orderRef = orderRef(orderId)
            firestore.runTransaction { transaction ->
                val order = transaction.get(orderRef).toObject(MarketOrder::class.java)?.copy(id = orderId)
                    ?: throw IllegalStateException("Order not found.")
                transaction.set(orderRef, transform(order))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("We couldn't mark this order as $transitionName.", e))
        }
    }
}
