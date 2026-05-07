package com.market.astu.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.MarketOrder
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

data class OrdersUiState(
    val currentUserId: String? = null,
    val orders: List<MarketOrder> = emptyList(),
    val isLoading: Boolean = true,
    val activeOrderId: String? = null,
    val errorMessage: String? = null
)

sealed interface OrdersEvent {
    data class Message(val value: String) : OrdersEvent
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OrdersEvent>()
    val events: SharedFlow<OrdersEvent> = _events.asSharedFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val userResult = userRepository.getCurrentUserProfile()
            val result = orderRepository.getOrdersForCurrentUser()
            _uiState.value = if (result.isSuccess) {
                OrdersUiState(
                    currentUserId = userResult.getOrNull()?.uid,
                    orders = result.getOrElse { emptyList() },
                    isLoading = false
                )
            } else {
                OrdersUiState(
                    currentUserId = userResult.getOrNull()?.uid,
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't load your orders.")
                )
            }
        }
    }

    fun markPickedUp(orderId: String) {
        runAction(
            orderId = orderId,
            successMessage = "Pickup confirmed."
        ) {
            orderRepository.markPickedUp(orderId)
        }
    }

    fun markInTransit(orderId: String) {
        runAction(
            orderId = orderId,
            successMessage = "Delivery is now in transit."
        ) {
            orderRepository.markInTransit(orderId)
        }
    }

    fun markDelivered(orderId: String, confirmationCode: String, proofNote: String) {
        runAction(
            orderId = orderId,
            successMessage = "Delivery completed and payouts released."
        ) {
            orderRepository.markDelivered(orderId, confirmationCode, proofNote)
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        runAction(
            orderId = orderId,
            successMessage = "Order cancelled and refunded."
        ) {
            orderRepository.cancelOrder(orderId, reason)
        }
    }

    fun openDispute(orderId: String, reason: String) {
        runAction(
            orderId = orderId,
            successMessage = "Dispute opened for this order."
        ) {
            orderRepository.openDispute(orderId, reason)
        }
    }

    private fun runAction(
        orderId: String,
        successMessage: String,
        action: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeOrderId = orderId, errorMessage = null)
            val result = action()
            if (result.isSuccess) {
                _events.emit(OrdersEvent.Message(successMessage))
                loadOrders()
            } else {
                _uiState.value = _uiState.value.copy(
                    activeOrderId = null,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't update this order.")
                )
            }
        }
    }
}
