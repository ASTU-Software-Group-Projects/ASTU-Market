package com.market.astu.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.market.astu.data.model.CartItem
import com.market.astu.data.model.Product
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Please sign in to manage your cart.")

    suspend fun getCartItemsList(): Result<List<CartItem>> {
        return try {
            val uid = requireUserId()
            val snap = firestore.collection("users").document(uid).collection("cart").get().await()
            Result.success(
                snap.documents.mapNotNull { document ->
                    document.toObject(CartItem::class.java)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProductToCart(product: Product): Result<Unit> {
        return try {
            val uid = requireUserId()
            if (!product.isVisibleToBuyers()) {
                throw IllegalStateException("This product is currently unavailable.")
            }
            val cartDocument =
                firestore.collection("users").document(uid).collection("cart").document(product.id)

            firestore.runTransaction { transaction ->
                val existing = transaction.get(cartDocument).toObject(CartItem::class.java)
                val nextQuantity = (existing?.quantity ?: 0) + 1
                if (nextQuantity > product.stockQuantity) {
                    throw IllegalStateException("Only ${product.stockQuantity} item(s) are available right now.")
                }
                val updatedItem = existing?.copy(
                    quantity = nextQuantity,
                    name = product.name.ifBlank { existing.name },
                    price = if (product.price > 0) product.price else existing.price,
                    imageUrl = product.imageUrl.ifBlank { existing.imageUrl }
                ) ?: CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    quantity = 1
                )

                transaction.set(cartDocument, updatedItem)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(productId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            firestore.collection("users").document(uid).collection("cart").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
