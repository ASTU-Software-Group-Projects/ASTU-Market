package com.market.astu.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.MarketOrder
import com.market.astu.data.model.OrderStatus
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceMetricPill
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.actorFor
import com.market.astu.util.canBeCancelledBy
import com.market.astu.util.canBeDeliveredBy
import com.market.astu.util.canBeDisputedBy
import com.market.astu.util.canBeMarkedInTransitBy
import com.market.astu.util.canBeMarkedPickedUpBy
import com.market.astu.util.formatCampus
import com.market.astu.util.isLive
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    balanceLabel: String?,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deliveryOrder by remember { mutableStateOf<MarketOrder?>(null) }
    var cancelOrder by remember { mutableStateOf<MarketOrder?>(null) }
    var disputeOrder by remember { mutableStateOf<MarketOrder?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OrdersEvent.Message -> snackbarHostState.showSnackbar(event.value)
            }
        }
    }

    deliveryOrder?.let { order ->
        DeliveryProofDialog(
            order = order,
            isSaving = uiState.activeOrderId == order.id,
            onDismiss = { deliveryOrder = null },
            onConfirm = { code, note ->
                viewModel.markDelivered(order.id, code, note)
                deliveryOrder = null
            }
        )
    }

    cancelOrder?.let { order ->
        ReasonDialog(
            title = "Cancel order",
            description = "Add a short reason before this order is refunded and removed from the runner flow.",
            confirmLabel = "Cancel order",
            isSaving = uiState.activeOrderId == order.id,
            onDismiss = { cancelOrder = null },
            onConfirm = { reason ->
                viewModel.cancelOrder(order.id, reason)
                cancelOrder = null
            }
        )
    }

    disputeOrder?.let { order ->
        ReasonDialog(
            title = "Open dispute",
            description = "Describe what went wrong so the issue stays attached to this order for follow-up.",
            confirmLabel = "Open dispute",
            isSaving = uiState.activeOrderId == order.id,
            onDismiss = { disputeOrder = null },
            onConfirm = { reason ->
                viewModel.openDispute(order.id, reason)
                disputeOrder = null
            }
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommerceTopBar(
                title = "Orders",
                subtitle = "Track pickup, delivery, refunds, and proof from one operational timeline.",
                themeMode = themeMode,
                notificationCount = 0,
                searchQuery = "",
                onSearchQueryChange = {},
                onToggleTheme = onToggleTheme,
                showSearch = false,
                avatarText = "O",
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Loading your order timeline...")
                }

                uiState.errorMessage != null && uiState.orders.isEmpty() -> {
                    MessageStatePane(
                        title = "We couldn't open orders",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadOrders
                    )
                }

                uiState.orders.isEmpty() -> {
                    MessageStatePane(
                        title = "No protocol orders yet",
                        message = "Orders created from checkout will appear here with runner, payout, and delivery proof status."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            OrdersHeroCard(
                                orderCount = uiState.orders.size,
                                liveCount = uiState.orders.count {
                                    OrderStatus.fromValue(it.status).isLive()
                                }
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Inventory2,
                                    label = "Orders",
                                    value = uiState.orders.size.toString()
                                )
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.LocalShipping,
                                    label = "Live",
                                    value = uiState.orders.count {
                                        OrderStatus.fromValue(it.status).isLive()
                                    }.toString()
                                )
                            }
                        }
                        item {
                            CommerceSectionHeader(
                                eyebrow = "Timeline",
                                title = "Escrow orders",
                                description = "Track delivery progress, refunds, disputes, and payout release from the same order thread."
                            )
                        }
                        if (uiState.errorMessage != null) {
                            item {
                                Text(
                                    text = uiState.errorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        items(uiState.orders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                currentUserId = uiState.currentUserId,
                                isBusy = uiState.activeOrderId == order.id,
                                onMarkPickedUp = { viewModel.markPickedUp(order.id) },
                                onMarkInTransit = { viewModel.markInTransit(order.id) },
                                onOpenDeliver = { deliveryOrder = order },
                                onOpenCancel = { cancelOrder = order },
                                onOpenDispute = { disputeOrder = order }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdersHeroCard(
    orderCount: Int,
    liveCount: Int
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Protocol timeline",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "$orderCount order${if (orderCount == 1) "" else "s"} tracked",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$liveCount order${if (liveCount == 1) "" else "s"} currently moving through pickup, transit, refund, or resolution.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: MarketOrder,
    currentUserId: String?,
    isBusy: Boolean,
    onMarkPickedUp: () -> Unit,
    onMarkInTransit: () -> Unit,
    onOpenDeliver: () -> Unit,
    onOpenCancel: () -> Unit,
    onOpenDispute: () -> Unit
) {
    val status = OrderStatus.fromValue(order.status)
    val actor = order.actorFor(currentUserId)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = order.productName.ifBlank { "Campus order" },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Created ${order.createdAt?.toDate()?.let { orderDateFormatter.format(it) } ?: "recently"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = actor.name.lowercase().replaceFirstChar(Char::uppercase),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusChip(status = status)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InlineMetric(icon = Icons.Default.Payments, text = formatCampus(order.escrowedAmount))
                InlineMetric(icon = Icons.Default.Schedule, text = order.pickupLocation.ifBlank { "Pickup pending" })
            }

            if (order.meetupLocation.isNotBlank()) {
                Text(
                    text = "Drop-off: ${order.meetupLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (order.runnerName.isNotBlank()) {
                Text(
                    text = "Runner: ${order.runnerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PaymentChip(label = "Payment", value = order.paymentStatus)
                PaymentChip(label = "Seller payout", value = order.sellerPayoutStatus)
                PaymentChip(label = "Runner payout", value = order.runnerPayoutStatus)
            }

            if (currentUserId == order.buyerId && !order.deliveryToken.isNullOrBlank() &&
                status in setOf(OrderStatus.RUNNER_ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Delivery code: ${order.deliveryToken}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (order.deliveryProofNote.isNotBlank()) {
                OrderNote(label = "Delivery proof", value = order.deliveryProofNote)
            }
            if (order.cancellationReason.isNotBlank()) {
                OrderNote(label = "Cancellation reason", value = order.cancellationReason)
            }
            if (order.disputeReason.isNotBlank()) {
                OrderNote(label = "Dispute reason", value = order.disputeReason)
            }

            OrderActions(
                order = order,
                currentUserId = currentUserId,
                isBusy = isBusy,
                onMarkPickedUp = onMarkPickedUp,
                onMarkInTransit = onMarkInTransit,
                onOpenDeliver = onOpenDeliver,
                onOpenCancel = onOpenCancel,
                onOpenDispute = onOpenDispute
            )
        }
    }
}

@Composable
private fun OrderActions(
    order: MarketOrder,
    currentUserId: String?,
    isBusy: Boolean,
    onMarkPickedUp: () -> Unit,
    onMarkInTransit: () -> Unit,
    onOpenDeliver: () -> Unit,
    onOpenCancel: () -> Unit,
    onOpenDispute: () -> Unit
) {
    val canPickUp = order.canBeMarkedPickedUpBy(currentUserId)
    val canTransit = order.canBeMarkedInTransitBy(currentUserId)
    val canDeliver = order.canBeDeliveredBy(currentUserId)
    val canCancel = order.canBeCancelledBy(currentUserId)
    val canDispute = order.canBeDisputedBy(currentUserId)

    if (!canPickUp && !canTransit && !canDeliver && !canCancel && !canDispute) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (canPickUp) {
            Button(
                onClick = onMarkPickedUp,
                enabled = !isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isBusy) "Saving..." else "Picked up")
            }
        }
        if (canTransit) {
            Button(
                onClick = onMarkInTransit,
                enabled = !isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isBusy) "Saving..." else "In transit")
            }
        }
        if (canDeliver) {
            Button(
                onClick = onOpenDeliver,
                enabled = !isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Text("Deliver")
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (canCancel) {
            OutlinedButton(
                onClick = onOpenCancel,
                enabled = !isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }
        if (canDispute) {
            OutlinedButton(
                onClick = onOpenDispute,
                enabled = !isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Text("Dispute")
            }
        }
    }
}

@Composable
private fun OrderNote(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PaymentChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = "$label: ${value.lowercase().replace('_', ' ')}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun InlineMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun StatusChip(
    status: OrderStatus
) {
    val (label, color) = when (status) {
        OrderStatus.AWAITING_RUNNER -> "Awaiting runner" to MaterialTheme.colorScheme.tertiaryContainer
        OrderStatus.RUNNER_ASSIGNED -> "Runner assigned" to MaterialTheme.colorScheme.secondaryContainer
        OrderStatus.PICKED_UP -> "Picked up" to MaterialTheme.colorScheme.secondaryContainer
        OrderStatus.IN_TRANSIT -> "In transit" to MaterialTheme.colorScheme.primaryContainer
        OrderStatus.DELIVERED -> "Delivered" to MaterialTheme.colorScheme.primaryContainer
        OrderStatus.DISPUTED -> "Disputed" to MaterialTheme.colorScheme.errorContainer
        OrderStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ReasonDialog(
    title: String,
    description: String,
    confirmLabel: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason.trim()) },
                enabled = !isSaving
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DeliveryProofDialog(
    order: MarketOrder,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var code by rememberSaveable(order.id) { mutableStateOf("") }
    var note by rememberSaveable(order.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete delivery") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter the buyer's delivery code and a short handoff note to release seller and runner payouts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Delivery code") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Delivery proof note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(code.trim(), note.trim()) },
                enabled = !isSaving
            ) {
                Text("Release payouts")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private val orderDateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
