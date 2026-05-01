package com.market.astu.ui.screens.seller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.Product
import com.market.astu.data.model.SellerProductInput
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.flashDealDiscountPercent
import com.market.astu.util.formatCampus
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun SellerInventoryScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    balanceLabel: String?,
    launchCreateComposer: Boolean = false,
    viewModel: SellerInventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SellerInventoryEvent.Message -> snackbarHostState.showSnackbar(event.value)
            }
        }
    }

    LaunchedEffect(launchCreateComposer) {
        if (launchCreateComposer) {
            isCreating = true
        }
    }

    if (editingProduct != null || isCreating) {
        ProductEditorDialog(
            product = editingProduct,
            isSaving = uiState.isSaving,
            onDismiss = {
                editingProduct = null
                isCreating = false
            },
            onSave = { input ->
                viewModel.saveProduct(editingProduct?.id, input)
                editingProduct = null
                isCreating = false
            }
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommerceTopBar(
                title = "Seller Inventory",
                subtitle = "Run your storefront with clearer control over pricing, stock, and visibility.",
                themeMode = themeMode,
                notificationCount = 0,
                searchQuery = "",
                onSearchQueryChange = {},
                onToggleTheme = onToggleTheme,
                showSearch = false,
                avatarText = "S",
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Loading your seller inventory...")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CommerceSectionHeader(
                                    eyebrow = "Cockpit",
                                    title = "Seller inventory",
                                    description = "Create listings, adjust stock, and keep each storefront item live from one place."
                                )
                                Button(onClick = { isCreating = true }) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Add listing")
                                }
                            }
                        }
                        if (uiState.errorMessage != null) {
                            item {
                                Text(
                                    text = uiState.errorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        if (uiState.products.isEmpty()) {
                            item {
                                MessageStatePane(
                                    title = "No seller listings yet",
                                    message = "Create your first product to start selling through the marketplace."
                                )
                            }
                        } else {
                            items(uiState.products, key = { it.id }) { product ->
                                SellerInventoryCard(
                                    product = product,
                                    onEdit = { editingProduct = product }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerInventoryCard(
    product: Product,
    onEdit: () -> Unit
) {
    val discount = flashDealDiscountPercent(product.basePrice, product.price)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = product.description.ifBlank { "Seller listing ready for dynamic pricing." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Pickup: ${product.pickupLocation.ifBlank { "Campus handoff point" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PriceBadge(label = "Base", value = formatCampus(product.basePrice))
                PriceBadge(label = "Live", value = formatCampus(product.price))
                PriceBadge(label = "Stock", value = product.stockQuantity.toString())
                if (discount > 0) {
                    PriceBadge(label = "Deal", value = "$discount% off")
                }
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (product.isVisibleToBuyers()) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Text(
                    text = if (product.isVisibleToBuyers()) "Visible to buyers" else "Hidden or sold out",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = if (product.isVisibleToBuyers()) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit listing")
            }
        }
    }
}

@Composable
private fun PriceBadge(
    label: String,
    value: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProductEditorDialog(
    product: Product?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (input: SellerProductInput) -> Unit
) {
    var name by rememberSaveable(product?.id) { mutableStateOf(product?.name.orEmpty()) }
    var category by rememberSaveable(product?.id) { mutableStateOf(product?.category.orEmpty()) }
    var description by rememberSaveable(product?.id) { mutableStateOf(product?.description.orEmpty()) }
    var imageUrl by rememberSaveable(product?.id) { mutableStateOf(product?.imageUrl.orEmpty()) }
    var pickupLocation by rememberSaveable(product?.id) { mutableStateOf(product?.pickupLocation.orEmpty()) }
    var basePrice by rememberSaveable(product?.id) {
        mutableStateOf((product?.let { if (it.basePrice > 0.0) it.basePrice else it.price } ?: 0.0).takeIf { it > 0.0 }?.toString().orEmpty())
    }
    var stockQuantity by rememberSaveable(product?.id) { mutableStateOf(product?.stockQuantity?.toString().orEmpty()) }
    var expiryDays by rememberSaveable(product?.id) { mutableStateOf("") }
    var isAvailable by rememberSaveable(product?.id) { mutableStateOf(product?.isAvailable ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Create seller listing" else "Update seller listing") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = { pickupLocation = it },
                    label = { Text("Pickup location") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = basePrice,
                    onValueChange = { basePrice = it },
                    label = { Text("Base price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stockQuantity,
                    onValueChange = { stockQuantity = it },
                    label = { Text("Stock quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expiryDays,
                    onValueChange = { expiryDays = it },
                    label = { Text("Expiry in days (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = isAvailable,
                        onClick = { isAvailable = true },
                        label = { Text("Visible") }
                    )
                    FilterChip(
                        selected = !isAvailable,
                        onClick = { isAvailable = false },
                        label = { Text("Hidden") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        SellerProductInput(
                            name = name,
                            category = category,
                            description = description,
                            imageUrl = imageUrl,
                            pickupLocation = pickupLocation,
                            basePrice = basePrice.toDoubleOrNull() ?: 0.0,
                            stockQuantity = stockQuantity.toIntOrNull() ?: -1,
                            isAvailable = isAvailable,
                            expirationDateMillis = expiryDays.toIntOrNull()?.let {
                                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(it.toLong())
                            }
                        )
                    )
                },
                enabled = !isSaving
            ) {
                Text(if (product == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
