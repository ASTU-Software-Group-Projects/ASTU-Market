package com.market.astu.data.model

enum class RunnerStatus {
    INACTIVE,
    ACTIVE,
    SUSPENDED;

    companion object {
        fun fromValue(value: String?): RunnerStatus {
            return entries.firstOrNull { it.name == value } ?: INACTIVE
        }
    }
}
