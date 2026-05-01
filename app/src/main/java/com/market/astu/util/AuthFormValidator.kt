package com.market.astu.util

import com.market.astu.data.model.UserRegistration
import com.market.astu.data.model.UserRole

private val EmailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

object AuthFormValidator {
    fun validateSignIn(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) {
            return "Enter both your email and password."
        }
        if (!email.trim().matches(EmailRegex)) {
            return "Enter a valid email address."
        }
        return null
    }

    fun validateSignUp(registration: UserRegistration, confirmPassword: String): String? {
        if (registration.displayName.trim().length < 3) {
            return "Enter your full name."
        }
        validateSignIn(registration.email, registration.password)?.let { return it }
        if (registration.normalizedRoles().isEmpty()) {
            return "Choose at least one account type."
        }

        if (registration.phoneNumber.filter(Char::isDigit).length < 9) {
            return "Enter a valid phone number."
        }
        if (registration.address.trim().length < 4) {
            return "Add your delivery or business address."
        }
        if (registration.password.length < 8) {
            return "Password must be at least 8 characters long."
        }
        if (!registration.password.any(Char::isDigit)) {
            return "Password should include at least one number."
        }
        if (registration.password != confirmPassword) {
            return "Passwords do not match."
        }
        if (
            registration.hasRole(UserRole.SELLER) &&
            registration.storeName.trim().length < 2
        ) {
            return "Seller accounts need a store name."
        }
        return null
    }
}
