package com.market.astu.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.Product
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MarketAsyncImage
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.formatPrice

@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProductDetailEvent.AddedToCart -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Added to your cart.",
                        actionLabel = "View cart"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onOpenCart()
                    }
                }

                is ProductDetailEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val product = uiState.product
            if (product != null && !uiState.isLoading && uiState.errorMessage == null) {
                DetailBottomBar(
                    product = product,
                    isAddingToCart = uiState.isAddingToCart,
                    onAddToCart = viewModel::addToCart
                )
            }
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Loading product details...")
                }

                uiState.errorMessage != null -> {
                    MessageStatePane(
                        title = "Couldn't load product",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = { viewModel.loadProduct(productId) }
                    )
                }

                uiState.product == null -> {
                    MessageStatePane(
                        title = "Product not found",
                        message = "It may have been removed or is temporarily unavailable.",
                        actionLabel = "Go back",
                        onAction = onBack
                    )
                }

                else -> {
                    val product = requireNotNull(uiState.product)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ProductHeaderImage(
                            product = product,
                            onBack = onBack
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProductOverviewCard(product = product)
                            ProductStoryCard(product = product)
                            ProductSignalsCard(product = product)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductHeaderImage(
    product: Product,
    onBack: () -> Unit
) {
    Box {
        MarketAsyncImage(
            imageUrl = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.16f),
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.58f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save product"
                    )
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Text(
                text = formatPrice(product.price),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProductOverviewCard(
    product: Product
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = product.category.ifBlank { "Campus listing" },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = product.description.ifBlank { "No description provided for this item yet." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Seller: ${product.sellerName.ifBlank { "Campus seller" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Pickup: ${product.pickupLocation.ifBlank { "Campus handoff point" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (product.isVisibleToBuyers()) {
                    "In stock: ${product.stockQuantity}"
                } else {
                    "Currently unavailable"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (product.isVisibleToBuyers()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun ProductStoryCard(
    product: Product
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommerceSectionHeader(
                title = "Why it stands out",
                eyebrow = "Highlights",
                description = "This refreshed detail page keeps product context, delivery readiness, and purchase action tighter together."
            )
            SignalRow(
                icon = Icons.Default.Bolt,
                title = "Fast decision making",
                description = "Cleaner hierarchy keeps the key information above the fold."
            )
            SignalRow(
                icon = Icons.Default.LocalShipping,
                title = "Campus-ready handoff",
                description = "The layout leaves room for future runner and escrow details without feeling crowded."
            )
            SignalRow(
                icon = Icons.Default.ShoppingCart,
                title = "Purchase-first CTA",
                description = "Pricing stays visible while the main add-to-cart action remains anchored below."
            )
        }
    }
}

@Composable
private fun ProductSignalsCard(
    product: Product
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CommerceSectionHeader(
                title = "Campus protocol preview",
                eyebrow = "Flow",
                description = "This listing already carries the buyer-facing details needed for checkout, pickup, and runner handoff."
            )
            SignalRow(
                icon = Icons.Default.LocalShipping,
                title = "Pickup point ready",
                description = product.pickupLocation.ifBlank { "Seller has not set a pickup location yet." }
            )
            SignalRow(
                icon = Icons.Default.ShoppingCart,
                title = "Inventory state",
                description = if (product.isVisibleToBuyers()) {
                    "${product.stockQuantity} item(s) available for purchase."
                } else {
                    "This listing is currently hidden or out of stock."
                }
            )
            SignalRow(
                icon = Icons.Default.Bolt,
                title = "Dynamic price lane",
                description = if (product.basePrice > product.price) {
                    "A time-based discount is active on this listing right now."
                } else {
                    "The live price currently matches the seller's base price."
                }
            )
        }
    }
}

@Composable
private fun SignalRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailBottomBar(
    product: Product,
    isAddingToCart: Boolean,
    onAddToCart: () -> Unit
) {
    Surface(
        tonalElevation = 10.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Campus price",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatPrice(product.price),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onAddToCart,
                enabled = product.isVisibleToBuyers() && !isAddingToCart,
                modifier = Modifier.height(54.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) {
                if (isAddingToCart) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null
                        )
                        Text(if (product.isVisibleToBuyers()) "Add to Cart" else "Sold out")
                    }
                }
            }
        }
    }
}
