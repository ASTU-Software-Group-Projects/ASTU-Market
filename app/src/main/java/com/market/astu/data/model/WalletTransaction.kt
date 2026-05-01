package com.market.astu.data.model

import com.google.firebase.Timestamp

data class WalletTransaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: String = WalletTransactionType.TOP_UP.name,
    val orderId: String = "",
    val description: String = "",
    val timestamp: Timestamp? = null
)

enum class WalletTransactionType {
    TOP_UP,
    PURCHASE,
    REFUND,
    SELLER_EARNING,
    RUNNER_REWARD,
    PROTOCOL_FEE,
    YIELD,
    SEND,
    RECEIVE,
    VOUCHER_TOPUP,
    AIRTIME
}