package com.market.astu.data.model

enum class OrderStatus {
    AWAITING_RUNNER,
    RUNNER_ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    DISPUTED,
    CANCELLED;

    companion object {
        fun fromValue(value: String?): OrderStatus {
            return entries.firstOrNull { it.name == value } ?: AWAITING_RUNNER
        }
    }
}
