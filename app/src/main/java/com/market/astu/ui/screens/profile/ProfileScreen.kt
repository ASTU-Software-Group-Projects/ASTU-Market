package com.market.astu.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.RunnerStatus
import com.market.astu.data.model.ThemeMode
import com.market.astu.data.model.User
import com.market.astu.data.model.UserRole
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceMetricPill
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MessageStatePane

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onToggleTheme: () -> Unit,
    balanceLabel: String? = null,
    onOpenSellerInventory: () -> Unit = {},
    onOpenOrders: () -> Unit = {},
    onOpenDeliveryBoard: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val avatar = uiState.user?.displayName?.firstOrNull()?.uppercase() ?: "A"

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            CommerceTopBar(
                title = "Profile",
                subtitle = "Identity, storefront, runner status, and preferences in one polished space.",
                themeMode = themeMode,
                notificationCount = 0,
                searchQuery = "",
                onSearchQueryChange = {},
                onToggleTheme = onToggleTheme,
                showSearch = false,
                avatarText = avatar,
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Loading your account...")
                }

                uiState.errorMessage != null -> {
                    MessageStatePane(
                        title = "Couldn't load your profile",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadProfile
                    )
                }

                uiState.user == null -> {
                    MessageStatePane(
                        title = "No profile found",
                        message = "Sign in again to restore your buyer or seller account.",
                        actionLabel = "Reload",
                        onAction = viewModel::loadProfile
                    )
                }

                else -> {
                    val user = requireNotNull(uiState.user)
                    val role = user.primaryRole()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ProfileHeroCard(user = user, role = role)
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Wallet,
                                    label = "Runner score",
                                    value = user.trustScore.toString()
                                )
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Storefront,
                                    label = "Storefront",
                                    value = if (user.hasRole(UserRole.SELLER) && user.storeName.isNotBlank()) {
                                        "Ready"
                                    } else if (user.hasRole(UserRole.SELLER)) {
                                        "Draft"
                                    } else {
                                        "Shopper"
                                    }
                                )
                            }
                        }
                        item {
                            QuickActionsCard(
                                canOpenSellerInventory = user.hasRole(UserRole.SELLER),
                                runnerStatus = RunnerStatus.fromValue(user.runnerStatus),
                                onOpenSellerInventory = onOpenSellerInventory,
                                onOpenOrders = onOpenOrders,
                                onOpenDeliveryBoard = onOpenDeliveryBoard
                            )
                        }
                        item {
                            DetailsCard(
                                title = "Account",
                                entries = listOf(
                                    DetailEntry("Email", user.email, Icons.Default.Email),
                                    DetailEntry("Phone", user.phoneNumber.ifBlank { "Not added yet" }, Icons.Default.Phone),
                                    DetailEntry("Address", user.address.ifBlank { "Add your delivery or business address" }, Icons.Default.LocationOn)
                                )
                            )
                        }
                        if (user.hasRole(UserRole.SELLER)) {
                            item {
                                DetailsCard(
                                    title = "Seller cockpit",
                                    entries = listOf(
                                        DetailEntry("Store name", user.storeName.ifBlank { "Not set" }, Icons.Default.Storefront),
                                        DetailEntry(
                                            "Brand story",
                                            user.bio.ifBlank { "Add a short story to help buyers trust your store." },
                                            Icons.Default.Wallet
                                        )
                                    )
                                )
                            }
                        }
                        item {
                            AppearanceCard(
                                selectedThemeMode = themeMode,
                                onThemeModeSelected = onThemeModeSelected
                            )
                        }
                        item {
                            Button(
                                onClick = onSignOut,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign Out")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    user: User,
    role: UserRole
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "A",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = if (role == UserRole.SELLER) "Seller identity" else "Buyer identity",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = user.displayName.ifBlank { "ASTU Market member" },
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Runner status: ${RunnerStatus.fromValue(user.runnerStatus).name.lowercase().replaceFirstChar(Char::uppercase)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (role == UserRole.SELLER) {
                    "Your seller space now reads more like a premium cockpit than a settings page."
                } else {
                    "Your buyer identity is easier to review, update, and trust at a glance."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionsCard(
    canOpenSellerInventory: Boolean,
    runnerStatus: RunnerStatus,
    onOpenSellerInventory: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenDeliveryBoard: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommerceSectionHeader(
                title = "Protocol shortcuts",
                eyebrow = "Actions",
                description = "Jump into the surfaces that matter most for your current role mix."
            )
            Button(
                onClick = onOpenOrders,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open order timeline")
            }
            Button(
                onClick = onOpenDeliveryBoard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open delivery board")
            }
            if (canOpenSellerInventory) {
                Button(
                    onClick = onOpenSellerInventory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open seller inventory")
                }
            }
            Text(
                text = if (runnerStatus == RunnerStatus.ACTIVE) {
                    "Runner mode is active and ready from the delivery board."
                } else {
                    "Runner mode unlocks when you activate delivery tools from the board."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class DetailEntry(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun DetailsCard(
    title: String,
    entries: List<DetailEntry>
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommerceSectionHeader(title = title, eyebrow = "Details")
            entries.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = entry.value, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceCard(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommerceSectionHeader(
                title = "Appearance",
                eyebrow = "Theme",
                description = "Choose the visual mode that makes the commerce workspace feel right for you."
            )
            ThemeMode.entries.forEach { mode ->
                val selected = selectedThemeMode == mode
                Surface(
                    onClick = { onThemeModeSelected(mode) },
                    shape = RoundedCornerShape(22.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (mode) {
                                        ThemeMode.SYSTEM -> Icons.Default.Storefront
                                        ThemeMode.DARK -> Icons.Default.DarkMode
                                        ThemeMode.LIGHT -> Icons.Default.LightMode
                                    },
                                    contentDescription = null,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.name.lowercase().replaceFirstChar(Char::uppercase),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "Follow the device setting automatically."
                                    ThemeMode.DARK -> "Reduce glare and deepen the finance aesthetic."
                                    ThemeMode.LIGHT -> "Keep the interface bright and airy."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (selected) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
