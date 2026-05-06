package com.market.astu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletBalanceUiState(
    val balance: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class WalletBalanceViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletBalanceUiState())
    val uiState: StateFlow<WalletBalanceUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val wallet = walletRepository.getWallet().getOrNull()
            _uiState.value = WalletBalanceUiState(
                balance = wallet?.balance ?: 0.0,
                isLoading = false
            )
        }
    }
}
