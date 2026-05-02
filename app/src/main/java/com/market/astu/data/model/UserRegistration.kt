package com.market.astu.data.model

data class UserRegistration(
    val displayName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val address: String,
    val role: String,
    val roles: List<String> = listOf(role),
    val storeName: String = "",
    val bio: String = ""
) {
    fun normalizedRoles(): List<String> {
        return (roles + role)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
    }

    fun hasRole(userRole: UserRole): Boolean = normalizedRoles().contains(userRole.name)

    fun primaryRole(): UserRole {
        return when {
            hasRole(UserRole.SELLER) -> UserRole.SELLER
            else -> UserRole.BUYER
        }
    }
}
