package com.market.astu.ui.screens.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.UserRegistration
import com.market.astu.data.repository.AuthRepository
import com.market.astu.util.AuthFormValidator
import com.market.astu.util.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object CheckingSession : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Submitting : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.CheckingSession)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        _authState.value = if (repository.isUserAuthenticated()) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signUp(registration: UserRegistration, confirmPassword: String) {
        val validationMessage = AuthFormValidator.validateSignUp(registration, confirmPassword)
        if (validationMessage != null) {
            _authState.value = AuthState.Error(validationMessage)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Submitting
            val result = repository.signUpWithEmail(registration)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull().toReadableMessage("We couldn't create your account.")
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        val validationMessage = AuthFormValidator.validateSignIn(email, password)
        if (validationMessage != null) {
            _authState.value = AuthState.Error(validationMessage)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Submitting
            val result = repository.signInWithEmail(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull().toReadableMessage("We couldn't sign you in.")
                )
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}
