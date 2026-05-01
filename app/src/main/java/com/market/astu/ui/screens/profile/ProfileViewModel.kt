package com.market.astu.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.User
import com.market.astu.data.repository.UserRepository
import com.market.astu.util.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            userRepository.getCurrentUserProfile()
                .onSuccess { user ->
                    _uiState.value = ProfileUiState(
                        user = user,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState(
                        user = null,
                        isLoading = false,
                        errorMessage = error.toReadableMessage("We couldn't load your profile.")
                    )
                }
        }
    }
}
