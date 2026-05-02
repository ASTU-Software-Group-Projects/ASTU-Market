package com.market.astu.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.market.astu.data.model.Product
import com.market.astu.data.model.SellerProductInput
import com.market.astu.data.model.User
import com.market.astu.util.computeDynamicPrice
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Please sign in to manage products.")
    }

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)?.normalized()
            }
                .filter { it.isVisibleToBuyers() }
                .sortedByDescending { it.updatedAt?.toDate()?.time ?: 0L }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProduct(productId: String): Result<Product?> {
        return try {
            val document = firestore.collection("products").document(productId).get().await()
            val product = document.toObject(Product::class.java)?.copy(id = document.id)?.normalized()
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSellerProducts(sellerId: String): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)?.normalized()
            }.sortedByDescending { it.updatedAt?.toDate()?.time ?: 0L }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSellerProduct(
        input: SellerProductInput,
        seller: User
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            require(uid == seller.uid) { "You can only create listings for your own seller profile." }
            val now = Timestamp.now()
            val docRef = firestore.collection("products").document()
            val product = Product(
                id = docRef.id,
                name = input.name.trim(),
                price = input.basePrice,
                basePrice = input.basePrice,
                imageUrl = input.imageUrl.trim(),
                category = input.category.trim(),
                description = input.description.trim(),
                sellerId = seller.uid,
                sellerName = seller.storeName.ifBlank { seller.displayName },
                pickupLocation = input.pickupLocation.trim(),
                stockQuantity = input.stockQuantity.coerceAtLeast(0),
                isAvailable = input.isAvailable && input.stockQuantity > 0,
                expirationDate = input.expirationDateMillis?.let { Timestamp(Date(it)) },
                createdAt = now,
                updatedAt = now
            )
            docRef.set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSellerProduct(
        productId: String,
        input: SellerProductInput,
        seller: User
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            require(uid == seller.uid) { "You can only edit your own listings." }
            val productRef = firestore.collection("products").document(productId)
            firestore.runTransaction { transaction ->
                val current = transaction.get(productRef).toObject(Product::class.java)?.copy(id = productId)
                    ?: throw IllegalStateException("This listing no longer exists.")
                if (current.sellerId != uid) {
                    throw IllegalStateException("You can only edit your own listings.")
                }
                transaction.set(
                    productRef,
                    current.copy(
                        name = input.name.trim(),
                        price = input.basePrice,
                        basePrice = input.basePrice,
                        imageUrl = input.imageUrl.trim(),
                        category = input.category.trim(),
                        description = input.description.trim(),
                        sellerName = seller.storeName.ifBlank { seller.displayName },
                        pickupLocation = input.pickupLocation.trim(),
                        stockQuantity = input.stockQuantity.coerceAtLeast(0),
                        isAvailable = input.isAvailable && input.stockQuantity > 0,
                        expirationDate = input.expirationDateMillis?.let { Timestamp(Date(it)) },
                        updatedAt = Timestamp.now()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Product.normalized(): Product {
        val effectiveBasePrice = if (basePrice > 0.0) basePrice else price
        return copy(
            basePrice = effectiveBasePrice,
            price = computeDynamicPrice(
                basePrice = effectiveBasePrice,
                currentPrice = price,
                expirationDate = expirationDate
            ),
            isAvailable = isAvailable && stockQuantity > 0
        )
    }
}
