package com.market.astu.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.repository.WalletRepository
import com.market.astu.util.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoucherScanUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface VoucherScanEvent {
    object Success : VoucherScanEvent
    data class Error(val message: String) : VoucherScanEvent
}

@HiltViewModel
class VoucherScanViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(VoucherScanUiState())
    val uiState: StateFlow<VoucherScanUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VoucherScanEvent>()
    val events: SharedFlow<VoucherScanEvent> = _events.asSharedFlow()

    fun redeemVoucher(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            walletRepository.topUpFromVoucher(code)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(VoucherScanEvent.Success)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
                    _events.emit(VoucherScanEvent.Error(e.toReadableMessage("Invalid voucher")))
                }
        }
    }
}