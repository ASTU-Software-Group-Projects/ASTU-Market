package com.market.astu.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val showLabel: Boolean = true,
    val isPrimaryAction: Boolean = false
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Shop : BottomNavItem("discover", "Discover", Icons.Default.TravelExplore)
    object Create : BottomNavItem(
        route = "quick_action",
        title = "Create",
        icon = Icons.Default.Add,
        showLabel = false,
        isPrimaryAction = true
    )
    object Wallet : BottomNavItem("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}
