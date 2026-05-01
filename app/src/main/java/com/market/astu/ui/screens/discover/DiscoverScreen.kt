package com.market.astu.ui.screens.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.Product
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MarketAsyncImage
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.ui.screens.home.HomeViewModel
import com.market.astu.util.flashDealDiscountPercent
import com.market.astu.util.formatCampus

private enum class DiscoverSort(val label: String) {
    BestMatch("Best match"),
    LowestPrice("Lowest price"),
    HighestPrice("Highest price")
}

@Composable
fun DiscoverScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onProductClick: (Product) -> Unit,
    balanceLabel: String? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var selectedSort by rememberSaveable { mutableStateOf(DiscoverSort.BestMatch) }

    val categories = listOf("All") + uiState.categories
    val products = remember(uiState.products, searchQuery, selectedCategory, selectedSort) {
        uiState.products
            .filter { product ->
                val matchesCategory = selectedCategory == "All" || product.category == selectedCategory
                val query = searchQuery.trim()
                val matchesQuery = query.isEmpty() ||
                        product.name.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true) ||
                        product.sellerName.contains(query, ignoreCase = true) ||
                        product.pickupLocation.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery
            }
            .let { filtered ->
                when (selectedSort) {
                    DiscoverSort.BestMatch -> filtered.sortedWith(
                        compareByDescending<Product> { flashDealDiscountPercent(it.basePrice, it.price) }
                            .thenBy { it.name.lowercase() }
                    )
                    DiscoverSort.LowestPrice -> filtered.sortedBy { it.price }
                    DiscoverSort.HighestPrice -> filtered.sortedByDescending { it.price }
                }
            }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            CommerceTopBar(
                title = "Discover",
                subtitle = "",  // removed for cleaner look
                themeMode = themeMode,
                notificationCount = uiState.notificationCount,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onToggleTheme = onToggleTheme,
                searchPlaceholder = "Search listings, pickup points, sellers",
                avatarText = uiState.shopper?.displayName?.firstOrNull()?.uppercase() ?: "A",
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Curating collections...")
                }

                uiState.errorMessage != null -> {
                    MessageStatePane(
                        title = "We couldn't open discovery",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadProducts
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),  // tighter padding
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category chips only, no header text
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(categories) { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category },
                                        label = { Text(category) }
                                    )
                                }
                            }
                        }

                        // Sort chips only, no header text
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(DiscoverSort.entries) { sort ->
                                    FilterChip(
                                        selected = selectedSort == sort,
                                        onClick = { selectedSort = sort },
                                        label = { Text(sort.label) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Product list
                        if (products.isEmpty()) {
                            item {
                                MessageStatePane(
                                    title = "No products match this view",
                                    message = "Switch categories, clear search, or try another ranking mode."
                                )
                            }
                        } else {
                            items(products, key = { it.id }) { product ->
                                DiscoverListItem(
                                    product = product,
                                    onClick = { onProductClick(product) }
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
private fun DiscoverListItem(
    product: Product,
    onClick: () -> Unit
) {
    val discount = flashDealDiscountPercent(product.basePrice, product.price)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                MarketAsyncImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.size(96.dp),
                    shape = RoundedCornerShape(20.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description.ifBlank { "Freshly listed and ready for discovery." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.sellerName.ifBlank { "Campus seller" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = product.pickupLocation.ifBlank { "Pickup shared after checkout" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (discount > 0) "Flash Deal $discount% off" else product.category.ifBlank { "General" },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sell,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatCampus(product.price),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${product.stockQuantity} left",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}