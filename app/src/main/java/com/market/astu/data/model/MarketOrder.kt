package com.market.astu.data.model

import com.google.firebase.Timestamp

data class MarketOrder(
    val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val runnerId: String? = null,
    val runnerName: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String = "",
    val pickupLocation: String = "",
    val meetupLocation: String = "",
    val quantity: Int = 1,
    val escrowedAmount: Double = 0.0,
    val runnerReward: Double = 0.0,
    val protocolFee: Double = 0.0,
    val sellerPayoutAmount: Double = 0.0,
    val insuranceCovered: Boolean = false,
    val insuranceAmount: Double = 0.0,
    val deliveryToken: String? = null,
    val paymentStatus: String = PaymentStatus.HELD_IN_ESCROW.name,
    val sellerPayoutStatus: String = PayoutStatus.PENDING.name,
    val runnerPayoutStatus: String = PayoutStatus.PENDING.name,
    val paymentReference: String = "",
    val deliveryProofNote: String = "",
    val cancellationReason: String = "",
    val disputeReason: String = "",
    val status: String = OrderStatus.AWAITING_RUNNER.name,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val runnerAssignedAt: Timestamp? = null,
    val pickedUpAt: Timestamp? = null,
    val inTransitAt: Timestamp? = null,
    val deliveredAt: Timestamp? = null,
    val cancelledAt: Timestamp? = null,
    val disputedAt: Timestamp? = null,
    val timeoutAt: Timestamp? = null
)

enum class PaymentStatus {
    HELD_IN_ESCROW,
    RELEASED,
    REFUNDED,
    DISPUTED
}

enum class PayoutStatus {
    PENDING,
    RELEASED,
    BLOCKED
}package com.market.astu.data.model

