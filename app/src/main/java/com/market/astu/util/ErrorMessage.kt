package com.market.astu.util

fun Throwable?.toReadableMessage(fallback: String): String {
    return this?.message?.takeIf { it.isNotBlank() } ?: fallback
}
