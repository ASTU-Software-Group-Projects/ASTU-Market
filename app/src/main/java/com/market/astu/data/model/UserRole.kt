package com.market.astu.data.model

enum class UserRole {
    BUYER,
    SELLER;

    companion object {
        fun fromValue(value: String?): UserRole {
            return entries.firstOrNull { it.name == value } ?: BUYER
        }
    }
}
