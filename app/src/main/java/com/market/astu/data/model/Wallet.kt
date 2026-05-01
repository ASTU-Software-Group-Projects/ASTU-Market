package com.market.astu.data.model

import com.google.firebase.Timestamp

data class Wallet(
    val uid: String = "",
    val balance: Double = 0.0,
    val monthlyYieldEarned: Double = 0.0,
    val lastYieldDate: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
