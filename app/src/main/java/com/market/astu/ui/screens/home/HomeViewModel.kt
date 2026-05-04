package com.market.astu.ui.screens.home
package com.market.astu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.Product
import com.market.astu.data.model.User
import com.market.astu.data.model.UserRole
import com.market.astu.data.repository.ProductRepository
import com.market.astu.data.repository.UserRepository
import com.market.astu.util.flashDealDiscountPercent
import com.market.astu.util.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val shopper: User? = null,
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val notificationCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userResult = userRepository.getCurrentUserProfile()
            val shopper = userResult.getOrNull()

            repository.getProducts()
                .onSuccess { products ->
                    _uiState.value = HomeUiState(
                        shopper = shopper,
                        products = products,
                        categories = products.map { it.category.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .sorted(),
                        notificationCount = notificationCountFor(shopper, products),
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState(
                        shopper = shopper,
                        products = emptyList(),
                        isLoading = false,
                        errorMessage = error.toReadableMessage("We couldn't load marketplace products.")
                    )
                }
        }
    }

    private fun notificationCountFor(user: User?, products: List<Product>): Int {
        if (user == null) return 0

        return when (user.primaryRole()) {
            UserRole.SELLER -> products.count { product ->
                product.sellerId == user.uid && (
                    product.stockQuantity <= 2 ||
                        flashDealDiscountPercent(product.basePrice, product.price) > 0
                    )
            }.coerceAtMost(9)

            UserRole.BUYER -> products.count { product ->
                flashDealDiscountPercent(product.basePrice, product.price) > 0
            }.coerceAtMost(9)
        }
    }
}
