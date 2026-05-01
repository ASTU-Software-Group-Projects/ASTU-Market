package com.market.astu.util

import com.market.astu.data.model.SellerProductInput

object SellerProductValidator {
    fun validate(input: SellerProductInput): String? {
        if (input.name.trim().length < 3) return "Listing name must be at least 3 characters."
        if (input.category.trim().length < 2) return "Add a category for this listing."
        if (input.description.trim().length < 8) return "Add a short product description."
        if (input.pickupLocation.trim().length < 3) return "Add a pickup location."
        if (input.basePrice <= 0.0) return "Price must be greater than zero."
        if (input.stockQuantity < 0) return "Stock cannot be negative."
        return null
    }
}
