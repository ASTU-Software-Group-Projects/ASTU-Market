package com.market.astu.util

import java.text.NumberFormat
import java.util.Locale

fun formatPrice(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}
