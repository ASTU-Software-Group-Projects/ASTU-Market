package com.market.astu.ui.screens.wallet

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane

@Composable
fun VoucherScanScreen(
    onBack: () -> Unit,
    onVoucherRedeemed: () -> Unit,
    viewModel: VoucherScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.redeemVoucher(result.contents)
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        scannerLauncher.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Scan recharge voucher")
                .setBeepEnabled(false)
                .setOrientationLocked(true)
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is VoucherScanEvent.Success -> {
                    snackbarHostState.showSnackbar("Voucher redeemed! Amount added to wallet.")
                    onVoucherRedeemed()
                }
                is VoucherScanEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommerceTopBar(
                title = "Scan voucher",
                subtitle = "Align voucher QR code within the frame",
                themeMode = ThemeMode.SYSTEM,
                notificationCount = 0,
                searchQuery = "",
                onSearchQueryChange = {},
                onToggleTheme = {},
                showSearch = false,
                avatarText = "V",
                balanceLabel = null
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                LoadingStatePane(message = "Redeeming voucher...")
            } else {
                Text(
                    "Scanning…",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}