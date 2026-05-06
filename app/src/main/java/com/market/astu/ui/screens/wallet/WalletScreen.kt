package com.market.astu.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.ThemeMode
import com.market.astu.data.model.WalletTransaction
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceMetricPill
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.formatCampus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalletScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    balanceLabel: String?,
    onBalanceUpdated: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSendDialog by remember { mutableStateOf(false) }
    var showReceiveSheet by remember { mutableStateOf(false) }
    val receiveQrCode by remember { derivedStateOf { viewModel.generateReceiveQrCode() } }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is WalletEvent.Message -> {
                    snackbarHostState.showSnackbar(event.value)
                    onBalanceUpdated()
                }
            }
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommerceTopBar(
                title = "Wallet",
                subtitle = "Send, receive, buy airtime, and track your CAMPUS balance.",
                themeMode = themeMode,
                notificationCount = 0,
                searchQuery = "",
                onSearchQueryChange = {},
                onToggleTheme = onToggleTheme,
                showSearch = false,
                avatarText = "W",
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Loading your CAMPUS wallet...")
                }

                uiState.errorMessage != null -> {
                    MessageStatePane(
                        title = "We couldn't open your wallet",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadWallet
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            WalletHeroCard(
                                balance = uiState.wallet.balance,
                                monthlyYield = uiState.wallet.monthlyYieldEarned
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Wallet,
                                    label = "Available",
                                    value = formatCampus(uiState.wallet.balance)
                                )
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Savings,
                                    label = "Yield",
                                    value = formatCampus(uiState.wallet.monthlyYieldEarned)
                                )
                            }
                        }

                        // --- NEW: Airtime Recharge Card ---
                        item {
                            AirtimeRechargeCard(
                                myPhoneNumber = userPhone,
                                isProcessing = uiState.isProcessingTopUp,
                                onBuyAirtime = viewModel::buyAirtime
                            )
                        }

                        // --- Send / Receive buttons ---
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { showSendDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Send")
                                }
                                OutlinedButton(
                                    onClick = { showReceiveSheet = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Receive")
                                }
                            }
                        }

                        item {
                            CommerceSectionHeader(
                                eyebrow = "Ledger",
                                title = "Transaction history",
                                description = "Every credit and debit in the protocol stays visible here."
                            )
                        }
                        if (uiState.transactions.isEmpty()) {
                            item {
                                MessageStatePane(
                                    title = "No wallet activity yet",
                                    message = "Top up your CAMPUS balance to start buying, selling, or handling deliveries."
                                )
                            }
                        } else {
                            items(uiState.transactions, key = { it.id }) { transaction ->
                                TransactionCard(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs / Sheets
    if (showSendDialog) {
        SendMoneyDialog(
            onDismiss = { showSendDialog = false },
            onSendByPhone = { phone, amount ->
                viewModel.sendMoneyByPhone(phone, amount)
                showSendDialog = false
            },
            onSendByUid = { uid, amount ->
                viewModel.sendMoneyByUid(uid, amount)
                showSendDialog = false
            }
        )
    }

    if (showReceiveSheet) {
        ReceiveMoneySheet(
            onDismiss = { showReceiveSheet = false },
            receiveCode = receiveQrCode
        )
    }
}

// ---------- Airtime Recharge Card ----------
@Composable
private fun AirtimeRechargeCard(
    myPhoneNumber: String?,
    isProcessing: Boolean,
    onBuyAirtime: (phoneNumber: String, amount: Double) -> Unit
) {
    var useMyNumber by remember { mutableStateOf(true) }
    var otherPhone by remember { mutableStateOf("") }
    var rechargeAmount by remember { mutableStateOf("") }

    val phoneNumber = if (useMyNumber) myPhoneNumber.orEmpty() else otherPhone.trim()
    val amountValid = (rechargeAmount.toDoubleOrNull() ?: 0.0) > 0

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommerceSectionHeader(
                eyebrow = "Airtime",
                title = "Buy mobile recharge",
                description = "Top up any phone directly from your CAMPUS balance."
            )

            // Choose self / other
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = useMyNumber,
                    onClick = { useMyNumber = true },
                    label = { Text("My number") }
                )
                FilterChip(
                    selected = !useMyNumber,
                    onClick = { useMyNumber = false },
                    label = { Text("Other number") }
                )
            }

            // Show phone field if "Other"
            if (!useMyNumber) {
                OutlinedTextField(
                    value = otherPhone,
                    onValueChange = { otherPhone = it },
                    label = { Text("Phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (myPhoneNumber.isNullOrBlank()) {
                Text(
                    "No phone on file. Please use 'Other number' or update your profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Amount
            OutlinedTextField(
                value = rechargeAmount,
                onValueChange = { rechargeAmount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Buy button
            Button(
                onClick = {
                    val amount = rechargeAmount.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && phoneNumber.isNotBlank()) {
                        onBuyAirtime(phoneNumber, amount)
                    }
                },
                enabled = !isProcessing && amountValid && phoneNumber.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (isProcessing) "Recharging…" else "Buy airtime")
            }
        }
    }
}

// ---------- Rest of the file is EXACTLY the same as before ----------
// WalletHeroCard, TransactionCard, etc. (unchanged)
@Composable
private fun WalletHeroCard(
    balance: Double,
    monthlyYield: Double
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "CAMPUS treasury",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = formatCampus(balance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Monthly yield earned: ${formatCampus(monthlyYield)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun TransactionCard(
    transaction: WalletTransaction
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.type.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.description.ifBlank { "Protocol activity" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = transaction.timestamp?.toDate()?.let { dateFormatter.format(it) } ?: "Just now",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCampus(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.amount >= 0.0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())