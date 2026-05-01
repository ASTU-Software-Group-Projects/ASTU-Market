package com.market.astu.ui.screens.delivery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.SafetyCheck
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.DeliveryGig
import com.market.astu.data.model.RunnerStatus
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceMetricPill
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.formatCampus
import com.market.astu.util.runnerTrustBand

@Composable
fun GigBoardScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    balanceLabel: String?,
    viewModel: GigBoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is GigBoardEvent.Message -> snackbarHostState.showSnackbar(event.value)
            }
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommerceTopBar(
                title = "Delivery board",
                subtitle = "Claim work confidently and move orders through a cleaner runner flow.",
                themeMode = themeMode,
                notificationCount = 0,
                searchQuery = "",
                onSearchQueryChange = {},
                onToggleTheme = onToggleTheme,
                showSearch = false,
                avatarText = "R",
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Loading the delivery board...")
                }

                uiState.errorMessage != null && uiState.runner == null -> {
                    MessageStatePane(
                        title = "We couldn't open the delivery board",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadBoard
                    )
                }

                else -> {
                    val runner = uiState.runner
                    val band = runnerTrustBand(runner?.trustScore ?: 0)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GigHeroCard(
                                trustScore = runner?.trustScore ?: 0,
                                bandLabel = band.label,
                                maxValue = band.maxOrderValue
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                                    label = "Trust tier",
                                    value = band.label
                                )
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.SafetyCheck,
                                    label = "Max gig",
                                    value = formatCampus(band.maxOrderValue)
                                )
                            }
                        }
                        if (runner == null || RunnerStatus.fromValue(runner.runnerStatus) != RunnerStatus.ACTIVE) {
                            item {
                                RunnerActivationCard(
                                    isActivating = uiState.isActivating,
                                    onActivate = viewModel::activateRunner
                                )
                            }
                        } else {
                            item {
                                CommerceSectionHeader(
                                    eyebrow = "Board",
                                    title = "Available deliveries",
                                    description = "Only deliveries inside your trust band are shown here."
                                )
                            }
                            if (uiState.gigs.isEmpty()) {
                                item {
                                    MessageStatePane(
                                        title = "No deliveries fit your trust lane right now",
                                        message = "Keep checking back as new escrow orders enter the runner queue."
                                    )
                                }
                            } else {
                                items(uiState.gigs, key = { it.order.id }) { gig ->
                                    GigCard(
                                        gig = gig,
                                        isAccepting = uiState.isAcceptingGigId == gig.order.id,
                                        onAccept = { viewModel.acceptGig(gig.order.id) }
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
private fun GigHeroCard(
    trustScore: Int,
    bandLabel: String,
    maxValue: Double
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
                    text = "Runner protocol",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "Trust Score $trustScore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$bandLabel tier unlocks deliveries up to ${formatCampus(maxValue)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RunnerActivationCard(
    isActivating: Boolean,
    onActivate: () -> Unit
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
                eyebrow = "Onboarding",
                title = "Unlock runner mode",
                description = "This first-pass flow activates the gig protocol and grants the base trust score of 10."
            )
            Button(
                onClick = onActivate,
                enabled = !isActivating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isActivating) "Activating..." else "Pass quiz and activate")
            }
        }
    }
}

@Composable
private fun GigCard(
    gig: DeliveryGig,
    isAccepting: Boolean,
    onAccept: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = gig.order.productName.ifBlank { "Campus delivery" },
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Pickup: ${gig.order.pickupLocation.ifBlank { "Seller pickup point" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Reward ${formatCampus(gig.order.runnerReward)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Escrow ${formatCampus(gig.order.escrowedAmount)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = gig.trustTier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "tier",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onAccept, enabled = !isAccepting) {
                    Text(if (isAccepting) "Claiming..." else "Accept")
                }
            }
        }
    }
}
