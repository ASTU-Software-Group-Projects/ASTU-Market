package com.market.astu.ui.screens.delivery

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.market.astu.data.model.MarketOrder
import com.market.astu.data.model.OrderStatus
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.canBeDeliveredBy
import com.market.astu.util.formatCampus

@Composable
fun DeliveryVerificationScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    balanceLabel: String?,
    onBack: () -> Unit,
    onOpenOrders: () -> Unit,
    onBalanceUpdated: () -> Unit,
    viewModel: DeliveryVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingOrderId by remember { mutableStateOf<String?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val orderId = pendingOrderId
        pendingOrderId = null
        if (!result.contents.isNullOrBlank() && orderId != null) {
            viewModel.markDelivered(orderId, result.contents)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DeliveryVerificationEvent.Message -> {
                    onBalanceUpdated()
                    snackbarHostState.showSnackbar(event.value)
                }
            }
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            VerificationTopBar(
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                balanceLabel = balanceLabel,
                onBack = onBack
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Preparing delivery verification...")
                }

                uiState.errorMessage != null && uiState.orders.isEmpty() -> {
                    MessageStatePane(
                        title = "We couldn't open delivery verification",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadVerificationState
                    )
                }

                else -> {
                    val currentUserId = uiState.currentUser?.uid
                    val buyerOrders = rememberVerificationBuyerOrders(uiState.orders, currentUserId)
                    val runnerOrders = rememberVerificationRunnerOrders(uiState.orders, currentUserId)

                    if (buyerOrders.isEmpty() && runnerOrders.isEmpty()) {
                        MessageStatePane(
                            title = "No active delivery verification",
                            message = "QR passes appear for buyers with active deliveries, and scan actions appear for runners with in-transit orders.",
                            actionLabel = "Open orders",
                            onAction = onOpenOrders
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (buyerOrders.isNotEmpty()) {
                                item {
                                    CommerceSectionHeader(
                                        eyebrow = "Buyer pass",
                                        title = "Show this QR to your runner",
                                        description = "Each active delivery gets its own QR pass with the same release code as your order."
                                    )
                                }
                                items(buyerOrders, key = { it.id }) { order ->
                                    BuyerVerificationCard(order = order)
                                }
                            }

                            if (runnerOrders.isNotEmpty()) {
                                item {
                                    CommerceSectionHeader(
                                        eyebrow = "Runner scan",
                                        title = "Scan buyer QR to complete delivery",
                                        description = "Scanning verifies the handoff and releases seller and runner payouts immediately."
                                    )
                                }
                                items(runnerOrders, key = { it.id }) { order ->
                                    RunnerVerificationCard(
                                        order = order,
                                        isBusy = uiState.activeOrderId == order.id,
                                        onScan = {
                                            pendingOrderId = order.id
                                            scannerLauncher.launch(
                                                ScanOptions()
                                                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                                    .setPrompt("Scan buyer delivery QR")
                                                    .setBeepEnabled(false)
                                                    .setOrientationLocked(true)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationTopBar(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    balanceLabel: String?,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CommerceTopBar(
            title = "Delivery verification",
            subtitle = "Scan or present a QR pass to complete the handoff.",
            themeMode = themeMode,
            notificationCount = 0,
            searchQuery = "",
            onSearchQueryChange = {},
            onToggleTheme = onToggleTheme,
            showSearch = false,
            avatarText = "V",
            balanceLabel = balanceLabel
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            }
        }
    }
}

@Composable
private fun BuyerVerificationCard(
    order: MarketOrder
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = order.productName.ifBlank { "Delivery order" },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Runner: ${order.runnerName.ifBlank { "Assigned soon" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = formatStatusLabel(order.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            VerificationQrCode(
                value = order.deliveryToken.orEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Code: ${order.deliveryToken.orEmpty()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = "Meet-up: ${order.meetupLocation.ifBlank { "Shared in your order details" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RunnerVerificationCard(
    order: MarketOrder,
    isBusy: Boolean,
    onScan: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = order.productName.ifBlank { "Delivery order" },
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Buyer: ${order.buyerName.ifBlank { "Campus buyer" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Meet-up: ${order.meetupLocation.ifBlank { "Shared in order details" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Release amount: ${formatCampus(order.runnerReward)} runner reward",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = onScan,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (isBusy) "Verifying..." else "Scan buyer QR")
            }
        }
    }
}

@Composable
private fun VerificationQrCode(
    value: String,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(value) {
        value.takeIf { it.isNotBlank() }?.let { token ->
            runCatching {
                BarcodeEncoder().encodeBitmap(token, BarcodeFormat.QR_CODE, 640, 640)
            }.getOrNull()
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Delivery QR code",
                    modifier = Modifier.size(220.dp)
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = value.ifBlank { "QR unavailable" },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

private fun rememberVerificationBuyerOrders(
    orders: List<MarketOrder>,
    currentUserId: String?
): List<MarketOrder> {
    return orders.filter { order ->
        order.buyerId == currentUserId &&
            !order.deliveryToken.isNullOrBlank() &&
            OrderStatus.fromValue(order.status) in setOf(
                OrderStatus.RUNNER_ASSIGNED,
                OrderStatus.PICKED_UP,
                OrderStatus.IN_TRANSIT
            )
    }
}

private fun rememberVerificationRunnerOrders(
    orders: List<MarketOrder>,
    currentUserId: String?
): List<MarketOrder> {
    return orders.filter { order -> order.canBeDeliveredBy(currentUserId) }
}

private fun formatStatusLabel(status: String): String {
    return OrderStatus.fromValue(status).name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
}

