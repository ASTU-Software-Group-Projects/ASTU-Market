package com.market.astu.data.model

data class DeliveryGig(
    val order: MarketOrder = MarketOrder(),
    val trustTier: String = "Bronze",
    val maxOrderValue: Double = 20.0
)
