package com.market.astu.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val role: String = UserRole.BUYER.name,
    val roles: List<String> = emptyList(),
    val storeName: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val trustScore: Int = 0,
    val completedDeliveries: Int = 0,
    val failedDeliveries: Int = 0,
    val activeDeliveryCount: Int = 0,
    val runnerStatus: String = RunnerStatus.INACTIVE.name
) {
    fun normalizedRoles(): List<String> {
        val baseRoles = roles.ifEmpty { listOf(role) }
        return (baseRoles + role)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
    }

    fun hasRole(userRole: UserRole): Boolean = normalizedRoles().contains(userRole.name)

    fun primaryRole(): UserRole {
        return when {
            hasRole(UserRole.SELLER) -> UserRole.SELLER
            else -> UserRole.fromValue(role)
        }
    }
}
