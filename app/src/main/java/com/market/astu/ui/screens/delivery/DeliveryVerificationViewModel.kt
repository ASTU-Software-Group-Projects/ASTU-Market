package com.market.astu.ui.screens.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.MarketOrder
import com.market.astu.data.model.User
import com.market.astu.data.repository.OrderRepository
import com.market.astu.data.repository.UserRepository
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

data class DeliveryVerificationUiState(
    val currentUser: User? = null,
    val orders: List<MarketOrder> = emptyList(),
    val isLoading: Boolean = true,
    val activeOrderId: String? = null,
    val errorMessage: String? = null
)

sealed interface DeliveryVerificationEvent {
    data class Message(val value: String) : DeliveryVerificationEvent
}

@HiltViewModel
class DeliveryVerificationViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeliveryVerificationUiState())
    val uiState: StateFlow<DeliveryVerificationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DeliveryVerificationEvent>()
    val events: SharedFlow<DeliveryVerificationEvent> = _events.asSharedFlow()

    init {
        loadVerificationState()
    }

    fun loadVerificationState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val userResult = userRepository.getCurrentUserProfile()
            val ordersResult = orderRepository.getOrdersForCurrentUser()
            _uiState.value = if (ordersResult.isSuccess) {
                DeliveryVerificationUiState(
                    currentUser = userResult.getOrNull(),
                    orders = ordersResult.getOrElse { emptyList() },
                    isLoading = false
                )
            } else {
                DeliveryVerificationUiState(
                    currentUser = userResult.getOrNull(),
                    orders = emptyList(),
                    isLoading = false,
                    errorMessage = ordersResult.exceptionOrNull()
                        .toReadableMessage("We couldn't load delivery verification.")
                )
            }
        }
    }

    fun markDelivered(orderId: String, confirmationCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeOrderId = orderId, errorMessage = null)
            val result = orderRepository.markDelivered(
                orderId = orderId,
                confirmationCode = confirmationCode,
                proofNote = "Verified via QR code."
            )
            if (result.isSuccess) {
                _events.emit(DeliveryVerificationEvent.Message("Delivery verified and payouts released."))
                loadVerificationState()
            } else {
                _uiState.value = _uiState.value.copy(
                    activeOrderId = null,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't verify this delivery.")
                )
            }
        }
    }
}
