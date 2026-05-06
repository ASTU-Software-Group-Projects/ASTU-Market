package com.market.astu.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.market.astu.data.model.Wallet
import com.market.astu.data.model.WalletTransaction
import com.market.astu.data.repository.UserRepository
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

data class WalletUiState(
    val wallet: Wallet = Wallet(),
    val transactions: List<WalletTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val isProcessingTopUp: Boolean = false,
    val isSendingMoney: Boolean = false,
    val errorMessage: String? = null
)

sealed interface WalletEvent {
    data class Message(val value: String) : WalletEvent
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WalletEvent>()
    val events: SharedFlow<WalletEvent> = _events.asSharedFlow()

    private val _userPhone = MutableStateFlow<String?>(null)
    val userPhone: StateFlow<String?> = _userPhone.asStateFlow()

    init {
        loadWallet()
        loadUserPhone()
    }

    private fun loadUserPhone() {
        viewModelScope.launch {
            userRepository.getCurrentUserProfile()
                .onSuccess { user -> _userPhone.value = user.phoneNumber }
        }
    }

    fun loadWallet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val walletResult = walletRepository.getWallet()
            val transactionsResult = walletRepository.getTransactions()

            if (walletResult.isSuccess) {
                _uiState.value = WalletUiState(
                    wallet = walletResult.getOrThrow(),
                    transactions = transactionsResult.getOrElse { emptyList() },
                    isLoading = false,
                    errorMessage = transactionsResult.exceptionOrNull()
                        ?.toReadableMessage("We couldn't load your wallet activity.")
                        ?.takeIf { transactionsResult.isFailure }
                )
            } else {
                _uiState.value = WalletUiState(
                    isLoading = false,
                    errorMessage = walletResult.exceptionOrNull()
                        .toReadableMessage("We couldn't load your wallet.")
                )
            }
        }
    }

    fun topUp(amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingTopUp = true, errorMessage = null)
            val result = walletRepository.topUp(amount)
            if (result.isSuccess) {
                loadWallet()
                _events.emit(WalletEvent.Message("Balance added to your ETB wallet."))
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessingTopUp = false,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't top up your wallet.")
                )
            }
        }
    }

    // --- Send money by phone (resolve via repository) ---
    fun sendMoneyByPhone(phoneNumber: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingMoney = true, errorMessage = null)
            val userResult = walletRepository.findUserByPhone(phoneNumber)
            if (userResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isSendingMoney = false,
                    errorMessage = userResult.exceptionOrNull()?.localizedMessage ?: "User not found."
                )
                return@launch
            }
            val toUid = userResult.getOrThrow().uid
            val transferResult = walletRepository.sendMoney(toUid, amount)
            if (transferResult.isSuccess) {
                _events.emit(WalletEvent.Message("Money sent successfully."))
                loadWallet()
            } else {
                _uiState.value = _uiState.value.copy(
                    isSendingMoney = false,
                    errorMessage = transferResult.exceptionOrNull()?.localizedMessage ?: "Transfer failed."
                )
            }
        }
    }

    // --- Send money by direct UID ---
    fun sendMoneyByUid(uid: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingMoney = true, errorMessage = null)
            val result = walletRepository.sendMoney(uid, amount)
            if (result.isSuccess) {
                _events.emit(WalletEvent.Message("Money sent successfully."))
                loadWallet()
            } else {
                _uiState.value = _uiState.value.copy(
                    isSendingMoney = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Transfer failed."
                )
            }
        }
    }

    // --- Receive QR code ---
    fun generateReceiveQrCode(): String {
        val uid = auth.currentUser?.uid ?: return ""
        return "astu_wallet:receive:$uid"
    }

    // --- Airtime purchase ---
    fun buyAirtime(phoneNumber: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingTopUp = true, errorMessage = null)
            val result = walletRepository.buyAirtime(phoneNumber, amount)
            if (result.isSuccess) {
                _events.emit(WalletEvent.Message("Airtime recharge sent to $phoneNumber."))
                loadWallet()
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessingTopUp = false,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't complete the airtime recharge.")
                )
            }
        }
    }
}