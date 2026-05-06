package com.market.astu.ui.screens.auth


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.UserRegistration
import com.market.astu.data.model.UserRole

@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val isSubmitting = authState is AuthState.Submitting
    val errorMessage = (authState as? AuthState.Error)?.message
    var isBuyerSelected by rememberSaveable { mutableStateOf(true) }
    var isSellerSelected by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var storeName by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Build your ASTU Market identity", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Choose buyer, seller, or both now. Delivery tools can be activated later when you need them.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = isBuyerSelected,
                        onClick = {
                            isBuyerSelected = if (isBuyerSelected && !isSellerSelected) {
                                true
                            } else {
                                !isBuyerSelected
                            }
                            viewModel.clearError()
                        },
                        label = { Text("Buyer") }
                    )
                    FilterChip(
                        selected = isSellerSelected,
                        onClick = {
                            isSellerSelected = if (isSellerSelected && !isBuyerSelected) {
                                true
                            } else {
                                !isSellerSelected
                            }
                            viewModel.clearError()
                        },
                        label = { Text("Seller") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 3.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.clearError()
                    },
                    label = { Text("Full name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearError()
                    },
                    label = { Text("Email address") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = errorMessage != null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        viewModel.clearError()
                    },
                    label = { Text("Phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = errorMessage != null
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        viewModel.clearError()
                    },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                if (isSellerSelected) {
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = {
                            storeName = it
                            viewModel.clearError()
                        },
                        label = { Text("Store name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = {
                            bio = it
                            viewModel.clearError()
                        },
                        label = { Text("Brand story") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearError()
                    },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = errorMessage != null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        viewModel.clearError()
                    },
                    label = { Text("Confirm password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = errorMessage != null,
                    singleLine = true
                )
                Button(
                    onClick = {
                        val selectedRoles = buildList {
                            if (isBuyerSelected) add(UserRole.BUYER.name)
                            if (isSellerSelected) add(UserRole.SELLER.name)
                        }
                        val primaryRole = if (isSellerSelected) {
                            UserRole.SELLER.name
                        } else {
                            UserRole.BUYER.name
                        }
                        viewModel.signUp(
                            registration = UserRegistration(
                                displayName = name,
                                email = email,
                                password = password,
                                phoneNumber = phoneNumber,
                                address = address,
                                role = primaryRole,
                                roles = selectedRoles,
                                storeName = storeName,
                                bio = bio
                            ),
                            confirmPassword = confirmPassword
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Professional Account")
                    }
                }
                TextButton(onClick = onNavigateToSignIn) {
                    Text("Already have an account? Sign in")
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
