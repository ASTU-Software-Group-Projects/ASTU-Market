package com.market.astu.ui.screens.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.Product
import com.market.astu.data.model.SellerProductInput
import com.market.astu.data.model.User
import com.market.astu.data.repository.ProductRepository
import com.market.astu.data.repository.UserRepository
import com.market.astu.util.SellerProductValidator
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

data class SellerInventoryUiState(
    val seller: User? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SellerInventoryEvent {
    data class Message(val value: String) : SellerInventoryEvent
}

@HiltViewModel
class SellerInventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SellerInventoryUiState())
    val uiState: StateFlow<SellerInventoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SellerInventoryEvent>()
    val events: SharedFlow<SellerInventoryEvent> = _events.asSharedFlow()

    init {
        loadInventory()
    }

    fun loadInventory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val userResult = userRepository.getCurrentUserProfile()
            val user = userResult.getOrNull()
            if (user == null) {
                _uiState.value = SellerInventoryUiState(
                    isLoading = false,
                    errorMessage = userResult.exceptionOrNull()
                        .toReadableMessage("We couldn't load your seller profile.")
                )
                return@launch
            }

            val inventoryResult = productRepository.getSellerProducts(user.uid)
            _uiState.value = SellerInventoryUiState(
                seller = user,
                products = inventoryResult.getOrElse { emptyList() },
                isLoading = false,
                errorMessage = inventoryResult.exceptionOrNull()
                    ?.toReadableMessage("We couldn't load seller inventory.")
                    ?.takeIf { inventoryResult.isFailure }
            )
        }
    }

    fun saveProduct(
        productId: String?,
        input: SellerProductInput
    ) {
        val validationMessage = SellerProductValidator.validate(input)
        if (validationMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationMessage)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val seller = _uiState.value.seller
            if (seller == null) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Your seller profile is not ready yet."
                )
                return@launch
            }
            val result = if (productId.isNullOrBlank()) {
                productRepository.createSellerProduct(input, seller)
            } else {
                productRepository.updateSellerProduct(productId, input, seller)
            }
            if (result.isSuccess) {
                _events.emit(
                    SellerInventoryEvent.Message(
                        if (productId.isNullOrBlank()) {
                            "Listing created and published."
                        } else {
                            "Listing updated successfully."
                        }
                    )
                )
                loadInventory()
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't save this listing.")
                )
            }
        }
    }
}
