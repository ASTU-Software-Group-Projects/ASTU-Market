package com.market.astu.ui.screens.cart

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.CartItem
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceMetricPill
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MarketAsyncImage
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.calculateOrderPricing
import com.market.astu.util.formatCampus
import com.market.astu.util.formatPrice

@Composable
fun CartScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    balanceLabel: String? = null,
    onBalanceUpdated: () -> Unit = {},
    onOpenOrders: () -> Unit = {},
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val total = viewModel.total
    val pricing = calculateOrderPricing(total)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.CheckoutComplete -> {
                    onBalanceUpdated()
                    val result = snackbarHostState.showSnackbar(
                        message = "${event.count} escrow order(s) created.",
                        actionLabel = "View orders"
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        onOpenOrders()
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommerceTopBar(
                title = "Cart",
                subtitle = "Checkout with cleaner math, calmer review, and fewer surprises.",
                themeMode = themeMode,
                notificationCount = 0,
                searchQuery = "",
                onSearchQueryChange = {},
                onToggleTheme = onToggleTheme,
                showSearch = false,
                avatarText = "C",
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Loading your cart...")
                }

                uiState.errorMessage != null && uiState.cartItems.isEmpty() -> {
                    MessageStatePane(
                        title = "Couldn't load your cart",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadCart
                    )
                }

                uiState.cartItems.isEmpty() -> {
                    MessageStatePane(
                        title = "Your cart is empty",
                        message = "Items you add from product pages will appear here in a cleaner checkout-ready view."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            CartSummaryCard(
                                itemCount = uiState.cartItems.size,
                                total = total
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Wallet,
                                    label = "Subtotal",
                                    value = formatCampus(total)
                                )
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Toll,
                                    label = "Fee inside",
                                    value = formatCampus(pricing.protocolFee)
                                )
                            }
                        }
                        item {
                            CommerceSectionHeader(
                                eyebrow = "Items",
                                title = "Checkout queue",
                                description = "Each row now makes quantity, pricing, and removal much easier to scan."
                            )
                        }
                        items(
                            items = uiState.cartItems,
                            key = { it.productId }
                        ) { item ->
                            CartItemRow(item) { viewModel.removeFromCart(item.productId) }
                        }
                        item {
                            CheckoutPreviewCard(
                                pricing = pricing,
                                isCheckingOut = uiState.isCheckingOut,
                                onCheckout = viewModel::checkout
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartSummaryCard(
    itemCount: Int,
    total: Double
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Checkout lane",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text("Cart summary", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "$itemCount items are staged for checkout with the final buyer charge kept upfront.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        Text(
            text = formatCampus(total),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarketAsyncImage(
                imageUrl = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(94.dp)
                    .clip(RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Qty ${item.quantity}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = "${formatCampus(item.price)} each",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Line total ${formatCampus(item.price * item.quantity)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CheckoutPreviewCard(
    pricing: com.market.astu.util.OrderPricingBreakdown,
    isCheckingOut: Boolean,
    onCheckout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommerceSectionHeader(
                title = "Checkout preview",
                eyebrow = "Summary",
                description = "Your cart total is the final buyer charge. Runner reward and protocol fee are split from the escrowed amount."
            )
            PriceLine(label = "Buyer charge", value = formatCampus(pricing.buyerCharge), emphasize = true)
            PriceLine(label = "Protocol fee inside", value = formatCampus(pricing.protocolFee))
            PriceLine(label = "Runner reward inside", value = formatCampus(pricing.runnerReward))
            PriceLine(label = "Seller payout after split", value = formatCampus(pricing.sellerPayout))
            Button(
                onClick = onCheckout,
                enabled = !isCheckingOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isCheckingOut) "Creating escrow..." else "Create Escrow Orders")
            }
        }
    }
}

@Composable
private fun PriceLine(
    label: String,
    value: String,
    emphasize: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium
        )
    }
}
