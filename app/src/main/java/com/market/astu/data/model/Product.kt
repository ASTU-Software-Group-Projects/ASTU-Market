package com.market.astu.data.model

import com.google.firebase.Timestamp

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val basePrice: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val description: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val pickupLocation: String = "",
    val stockQuantity: Int = 1,
    val isAvailable: Boolean = true,
    val expirationDate: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun isVisibleToBuyers(): Boolean = isAvailable && stockQuantity > 0
}
