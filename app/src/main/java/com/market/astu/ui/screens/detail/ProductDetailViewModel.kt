package com.market.astu.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.Product
import com.market.astu.data.repository.CartRepository
import com.market.astu.data.repository.ProductRepository
import com.market.astu.util.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = true,
    val isAddingToCart: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ProductDetailEvent {
    data object AddedToCart : ProductDetailEvent
    data class Error(val message: String) : ProductDetailEvent
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductDetailEvent>()
    val events = _events.asSharedFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            productRepository.getProduct(productId)
                .onSuccess { product ->
                    if (product == null) {
                        _uiState.value = ProductDetailUiState(
                            product = null,
                            isLoading = false,
                            errorMessage = "This product is no longer available."
                        )
                    } else {
                        _uiState.value = ProductDetailUiState(
                            product = product,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = ProductDetailUiState(
                        product = null,
                        isLoading = false,
                        errorMessage = error.toReadableMessage("We couldn't load this product.")
                    )
                }
        }
    }

    fun addToCart() {
        val product = _uiState.value.product ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCart = true) }

            cartRepository.addProductToCart(product)
                .onSuccess {
                    _events.emit(ProductDetailEvent.AddedToCart)
                }
                .onFailure { error ->
                    _events.emit(
                        ProductDetailEvent.Error(
                            error.toReadableMessage("We couldn't add this item to your cart.")
                        )
                    )
                }

            _uiState.update { it.copy(isAddingToCart = false) }
        }
    }
}
