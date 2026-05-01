package com.market.astu.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.market.astu.data.model.DeliveryGig
import com.market.astu.data.model.MarketOrder
import com.market.astu.data.model.OrderStatus
import com.market.astu.data.model.PaymentStatus
import com.market.astu.data.model.RunnerStatus
import com.market.astu.data.model.User
import com.market.astu.util.runnerTrustBand
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Please sign in to manage gigs.")
    }

    suspend fun getRunnerProfile(): Result<User> {
        return try {
            val uid = requireUserId()
            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
                ?: throw IllegalStateException("Profile not found.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableGigs(): Result<List<DeliveryGig>> {
        return try {
            val runner = getRunnerProfile().getOrThrow()
            val band = runnerTrustBand(runner.trustScore)
            val snapshot = firestore.collection("orders")
                .whereEqualTo("status", OrderStatus.AWAITING_RUNNER.name)
                .get()
                .await()

            val gigs = snapshot.documents.mapNotNull { document ->
                document.toObject(MarketOrder::class.java)?.copy(id = document.id)
            }.filter { order ->
                order.paymentStatus == PaymentStatus.HELD_IN_ESCROW.name &&
                    order.escrowedAmount <= band.maxOrderValue &&
                    order.buyerId != runner.uid
            }.sortedByDescending { it.escrowedAmount }
                .map { order ->
                    DeliveryGig(
                        order = order,
                        trustTier = band.label,
                        maxOrderValue = band.maxOrderValue
                    )
                }

            Result.success(gigs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateRunner(): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userRef = firestore.collection("users").document(uid)
            firestore.runTransaction { transaction ->
                val current = transaction.get(userRef).toObject(User::class.java)
                    ?: throw IllegalStateException("Profile not found.")
                transaction.set(
                    userRef,
                    current.copy(
                        roles = (current.roles + current.role).distinct(),
                        trustScore = current.trustScore.coerceAtLeast(10),
                        runnerStatus = RunnerStatus.ACTIVE.name
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptGig(orderId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val orderRef = firestore.collection("orders").document(orderId)
            val userRef = firestore.collection("users").document(uid)
            firestore.runTransaction { transaction ->
                val runner = transaction.get(userRef).toObject(User::class.java)
                    ?: throw IllegalStateException("Profile not found.")
                val order = transaction.get(orderRef).toObject(MarketOrder::class.java)
                    ?: throw IllegalStateException("Gig no longer exists.")
                if (RunnerStatus.fromValue(runner.runnerStatus) != RunnerStatus.ACTIVE) {
                    throw IllegalStateException("Complete runner onboarding before accepting gigs.")
                }
                if (OrderStatus.fromValue(order.status) != OrderStatus.AWAITING_RUNNER) {
                    throw IllegalStateException("This gig has already been claimed.")
                }
                if (order.escrowedAmount > runnerTrustBand(runner.trustScore).maxOrderValue) {
                    throw IllegalStateException("This gig is above your current trust limit.")
                }

                transaction.set(
                    orderRef,
                    order.copy(
                        runnerId = uid,
                        runnerName = runner.displayName,
                        status = OrderStatus.RUNNER_ASSIGNED.name,
                        insuranceCovered = true,
                        insuranceAmount = order.escrowedAmount,
                        runnerAssignedAt = Timestamp.now(),
                        updatedAt = Timestamp.now(),
                        timeoutAt = Timestamp(Timestamp.now().seconds + 45 * 60, 0)
                    )
                )
                transaction.set(
                    userRef,
                    runner.copy(activeDeliveryCount = runner.activeDeliveryCount + 1)
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
