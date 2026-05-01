package com.market.astu.util

import com.google.firebase.Timestamp
import kotlin.math.max

private const val DISCOUNT_WINDOW_HOURS = 72.0
private const val MINIMUM_PRICE_FACTOR = 0.5

fun computeDynamicPrice(
    basePrice: Double,
    currentPrice: Double,
    expirationDate: Timestamp?,
    nowMillis: Long = System.currentTimeMillis()
): Double {
    val baseline = when {
        basePrice > 0.0 -> basePrice
        currentPrice > 0.0 -> currentPrice
        else -> 0.0
    }
    val expiryMillis = expirationDate?.toDate()?.time ?: return baseline
    val remainingMillis = expiryMillis - nowMillis
    val windowMillis = (DISCOUNT_WINDOW_HOURS * 60 * 60 * 1000).toLong()

    if (remainingMillis >= windowMillis) return baseline

    val ratio = max(MINIMUM_PRICE_FACTOR, remainingMillis.toDouble() / windowMillis)
    return (baseline * ratio).coerceAtLeast(baseline * MINIMUM_PRICE_FACTOR)
}

fun flashDealDiscountPercent(
    basePrice: Double,
    currentPrice: Double
): Int {
    if (basePrice <= 0.0 || currentPrice >= basePrice) return 0
    return (((basePrice - currentPrice) / basePrice) * 100).toInt().coerceIn(0, 50)
}
