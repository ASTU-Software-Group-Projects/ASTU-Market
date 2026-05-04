package com.market.astu.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.market.astu.data.model.Product
import com.market.astu.data.model.ThemeMode
import com.market.astu.data.model.UserRole
import com.market.astu.ui.common.CommerceBackdrop
import com.market.astu.ui.common.CommerceMetricPill
import com.market.astu.ui.common.CommerceSectionHeader
import com.market.astu.ui.common.CommerceTopBar
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.common.MarketAsyncImage
import com.market.astu.ui.common.MessageStatePane
import com.market.astu.util.flashDealDiscountPercent
import com.market.astu.util.formatCampus
import java.util.Calendar

@Composable
fun HomeScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onProductClick: (Product) -> Unit,
    balanceLabel: String? = null,
    onOpenWallet: () -> Unit = {},
    onOpenOrders: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }

    val filteredProducts = remember(uiState.products, searchQuery, selectedCategory) {
        uiState.products.filter { product ->
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
    }
    val featuredProducts = remember(filteredProducts) {
        filteredProducts
            .sortedWith(
                compareByDescending<Product> { flashDealDiscountPercent(it.basePrice, it.price) }
                    .thenByDescending { it.updatedAt?.toDate()?.time ?: 0L }
                    .thenBy { it.name.lowercase() }
            )
            .take(5)
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            CommerceTopBar(
                title = "ASTU Market",
                subtitle = "Calm, premium campus commerce with faster decisions and clearer handoff signals.",
                themeMode = themeMode,
                notificationCount = uiState.notificationCount,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onToggleTheme = onToggleTheme,
                searchPlaceholder = "Search products, sellers, pickup points",
                avatarText = uiState.shopper?.displayName?.firstOrNull()?.uppercase() ?: "A",
                balanceLabel = balanceLabel
            )
        }
    ) { padding ->
        CommerceBackdrop(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    LoadingStatePane(message = "Building your storefront...")
                }

                uiState.errorMessage != null -> {
                    MessageStatePane(
                        title = "We couldn't load the marketplace",
                        message = uiState.errorMessage,
                        actionLabel = "Try again",
                        onAction = viewModel::loadProducts
                    )
                }

                else -> {
                    val shopper = uiState.shopper
                    val role = shopper?.primaryRole() ?: UserRole.BUYER
                    val categories = listOf("All") + uiState.categories

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 164.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeHeroCard(
                                displayName = shopper?.displayName.orEmpty(),
                                role = role,
                                productCount = filteredProducts.size,
                                notificationCount = uiState.notificationCount,
                                searchActive = searchQuery.isNotBlank(),
                                onOpenWallet = onOpenWallet,
                                onOpenOrders = onOpenOrders
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Storefront,
                                    label = if (role == UserRole.SELLER) "Live listings" else "Open storefronts",
                                    value = filteredProducts.size.toString()
                                )
                                CommerceMetricPill(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.NotificationsNone,
                                    label = "Alerts",
                                    value = uiState.notificationCount.toString()
                                )
                            }
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategorySelector(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it }
                            )
                        }
                        if (featuredProducts.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    CommerceSectionHeader(
                                        eyebrow = "Flash Lane",
                                        title = "High-visibility picks",
                                        description = "Hero cards surface the items most ready to move right now."
                                    )
                                    FeaturedProductRail(
                                        products = featuredProducts,
                                        onProductClick = onProductClick
                                    )
                                }
                            }
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CommerceSectionHeader(
                                eyebrow = "Catalog",
                                title = if (role == UserRole.SELLER) {
                                    "Storefront grid"
                                } else {
                                    "Browse all products"
                                },
                                description = "${filteredProducts.size} items are currently visible in this lane.",
                                trailing = {
                                    FilledTonalButton(onClick = viewModel::loadProducts) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text("Refresh")
                                    }
                                }
                            )
                        }
                        if (filteredProducts.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                MessageStatePane(
                                    title = "No matches found",
                                    message = "Try another keyword or switch categories to widen the grid."
                                )
                            }
                        } else {
                            items(
                                items = filteredProducts,
                                key = { it.id }
                            ) { product ->
                                ProductCard(
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
private fun HomeHeroCard(
    displayName: String,
    role: UserRole,
    productCount: Int,
    notificationCount: Int,
    searchActive: Boolean,
    onOpenWallet: () -> Unit,
    onOpenOrders: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = if (searchActive) "Search mode" else "Campus pulse",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${timeAwareGreeting()}, ${displayName.ifBlank { "student" }}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (role == UserRole.SELLER) {
                            "Your storefront now feels faster, cleaner, and easier to scan at a glance."
                        } else {
                            "Discover, compare, and move into checkout with fewer taps and less clutter."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeroPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bolt,
                        label = "Live now",
                        value = productCount.toString()
                    )
                    HeroPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "Momentum",
                        value = if (notificationCount > 0) "Active" else "Stable"
                    )
                    HeroPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        label = "Flow",
                        value = if (role == UserRole.SELLER) "Seller" else "Buyer"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenWallet
                    ) {
                        Text("Wallet")
                    }
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenOrders
                    ) {
                        Text("Orders")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CommerceSectionHeader(
            eyebrow = "Explore",
            title = "Fast category lanes",
            description = "Jump between product groups without giving up screen space."
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
        }
    }
}

@Composable
private fun FeaturedProductRail(
    products: List<Product>,
    onProductClick: (Product) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(products, key = { it.id }) { product ->
            val discount = flashDealDiscountPercent(product.basePrice, product.price)
            Card(
                onClick = { onProductClick(product) },
                modifier = Modifier.fillParentMaxWidth(0.86f),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Box {
                    MarketAsyncImage(
                        imageUrl = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(224.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.66f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                            ) {
                                Text(
                                    text = if (discount > 0) "Flash Deal $discount% off" else product.category.ifBlank { "Featured" },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = androidx.compose.ui.graphics.Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatCampus(product.price),
                                style = MaterialTheme.typography.headlineSmall,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    val discount = flashDealDiscountPercent(product.basePrice, product.price)
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column {
            Box {
                MarketAsyncImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = if (discount > 0) "Flash Deal" else product.category.ifBlank { "General" },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save product",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)
                ) {
                    Text(
                        text = formatCampus(product.price),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description.ifBlank { "Freshly added to the marketplace." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.sellerName.ifBlank { "Campus seller" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = product.pickupLocation.ifBlank { "Pickup point shared after checkout" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (discount > 0) {
                        "Auto-discounted as expiry approaches"
                    } else {
                        "${product.stockQuantity} in stock"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (discount > 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}

private fun timeAwareGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Welcome back"
    }
}..
