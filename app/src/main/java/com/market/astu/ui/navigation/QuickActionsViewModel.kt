package com.market.astu.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.User
import com.market.astu.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickActionsUiState(
    val user: User? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class QuickActionsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuickActionsUiState())
    val uiState: StateFlow<QuickActionsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUserProfile().getOrNull()
            _uiState.value = QuickActionsUiState(
                user = user,
                isLoading = false
            )
        }
    }
}
