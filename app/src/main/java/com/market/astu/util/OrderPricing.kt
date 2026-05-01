package com.market.astu.util

import kotlin.math.roundToInt

private const val RUNNER_REWARD_RATE = 0.15
private const val PROTOCOL_FEE_RATE = 0.01

data class OrderPricingBreakdown(
    val buyerCharge: Double,
    val protocolFee: Double,
    val runnerReward: Double,
    val sellerPayout: Double
)

fun calculateOrderPricing(subtotal: Double): OrderPricingBreakdown {
    val safeSubtotal = subtotal.coerceAtLeast(0.0).roundCurrency()
    val protocolFee = (safeSubtotal * PROTOCOL_FEE_RATE).roundCurrency()
    val runnerReward = (safeSubtotal * RUNNER_REWARD_RATE).roundCurrency()
    val sellerPayout = (safeSubtotal - protocolFee - runnerReward).coerceAtLeast(0.0).roundCurrency()

    return OrderPricingBreakdown(
        buyerCharge = safeSubtotal,
        protocolFee = protocolFee,
        runnerReward = runnerReward,
        sellerPayout = sellerPayout
    )
}

private fun Double.roundCurrency(): Double = (this * 100).roundToInt() / 100.0
