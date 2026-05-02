package com.market.astu.data.model

data class SellerProductInput(
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val pickupLocation: String = "",
    val basePrice: Double = 0.0,
    val stockQuantity: Int = 1,
    val isAvailable: Boolean = true,
    val expirationDateMillis: Long? = null
)
