package com.market.astu.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.CartItem
import com.market.astu.data.repository.CartRepository
import com.market.astu.data.repository.OrderRepository
import com.market.astu.util.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = true,
    val isCheckingOut: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CartEvent {
    data class CheckoutComplete(val count: Int) : CartEvent
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CartEvent>()
    val events: SharedFlow<CartEvent> = _events.asSharedFlow()

    init { loadCart() }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isCheckingOut = false) }
            cartRepository.getCartItemsList()
                .onSuccess { items ->
                    _uiState.value = CartUiState(
                        cartItems = items,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = CartUiState(
                        cartItems = emptyList(),
                        isLoading = false,
                        errorMessage = error.toReadableMessage("We couldn't load your cart.")
                    )
                }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
                .onSuccess {
                    loadCart()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.toReadableMessage("We couldn't remove this item."))
                    }
                }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true, errorMessage = null) }
            orderRepository.checkoutCart()
                .onSuccess { count ->
                    _events.emit(CartEvent.CheckoutComplete(count))
                    loadCart()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCheckingOut = false,
                            errorMessage = error.toReadableMessage("We couldn't create your escrow order.")
                        )
                    }
                }
        }
    }

    val total: Double get() = uiState.value.cartItems.sumOf { it.price * it.quantity }
}

