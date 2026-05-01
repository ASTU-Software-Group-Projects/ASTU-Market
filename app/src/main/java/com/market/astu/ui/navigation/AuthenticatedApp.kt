package com.market.astu.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.compose.*
import com.market.astu.data.model.ThemeMode
import com.market.astu.data.model.UserRole
import com.market.astu.ui.WalletBalanceViewModel
import com.market.astu.ui.common.BottomNavItem
import com.market.astu.ui.common.CommerceBottomDock
import com.market.astu.ui.screens.cart.CartScreen
import com.market.astu.ui.screens.delivery.DeliveryVerificationScreen
import com.market.astu.ui.screens.delivery.GigBoardScreen
import com.market.astu.ui.screens.discover.DiscoverScreen
import com.market.astu.ui.screens.detail.ProductDetailScreen
import com.market.astu.ui.screens.home.HomeScreen
import com.market.astu.ui.screens.orders.OrdersScreen
import com.market.astu.ui.screens.profile.ProfileScreen
import com.market.astu.ui.screens.seller.SellerInventoryScreen
import com.market.astu.ui.screens.wallet.VoucherScanScreen
import com.market.astu.ui.screens.wallet.WalletScreen
import com.market.astu.util.formatCampus

private object ProductDetailDestination {
    const val route = "product/{productId}"
    fun createRoute(productId: String) = "product/$productId"
}

private object OrdersDestination {
    const val route = "orders"
}

private object CartDestination {
    const val route = "cart"
}

private object SellerInventoryDestination {
    private const val baseRoute = "seller_inventory"
    const val route = "$baseRoute?create={create}"
    fun createRoute(create: Boolean = false) = "$baseRoute?create=$create"
}

private object DeliveryBoardDestination {
    const val route = "delivery_board"
}

private object DeliveryVerificationDestination {
    const val route = "delivery_verification"
}

private object VoucherScanDestination {
    const val route = "voucher_scan"
}

@Composable
fun AuthenticatedApp(
    onSignOut: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    navController: NavHostController = rememberNavController(),
    walletBalanceViewModel: WalletBalanceViewModel = hiltViewModel(),
    quickActionsViewModel: QuickActionsViewModel = hiltViewModel()
) {
    val walletUiState by walletBalanceViewModel.uiState.collectAsStateWithLifecycle()
    val quickActionsUiState by quickActionsViewModel.uiState.collectAsStateWithLifecycle()
    val balanceLabel = formatCampus(walletUiState.balance)
    var showQuickActions by rememberSaveable { mutableStateOf(false) }

    val topLevelDestinations = listOf(
        BottomNavItem.Home,
        BottomNavItem.Shop,
        BottomNavItem.Create,
        BottomNavItem.Wallet,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    if (showQuickActions) {
        QuickActionDialog(
            canPostProducts = quickActionsUiState.user?.hasRole(UserRole.SELLER) == true,
            onDismiss = { showQuickActions = false },
            onPostProduct = {
                showQuickActions = false
                navController.navigate(SellerInventoryDestination.createRoute(create = true))
            },
            onScanVoucher = {
                showQuickActions = false
                navController.navigate(VoucherScanDestination.route) {
                    launchSingleTop = true
                }
            },
            onDeliveryVerification = {
                showQuickActions = false
                navController.navigate(DeliveryVerificationDestination.route) {
                    launchSingleTop = true
                }
            }
        )
    }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarHeight)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel,
                    onProductClick = { product ->
                        navController.navigate(ProductDetailDestination.createRoute(product.id))
                    },
                    onOpenWallet = {
                        navController.navigate(BottomNavItem.Wallet.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenOrders = {
                        navController.navigate(OrdersDestination.route)
                    }
                )
            }
            composable(BottomNavItem.Shop.route) {
                DiscoverScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel,
                    onProductClick = { product ->
                        navController.navigate(ProductDetailDestination.createRoute(product.id))
                    }
                )
            }
            composable(BottomNavItem.Wallet.route) {
                WalletScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel,
                    onBalanceUpdated = walletBalanceViewModel::refresh
                )
            }
            composable(DeliveryBoardDestination.route) {
                GigBoardScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel
                )
            }
            composable(OrdersDestination.route) {
                OrdersScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel
                )
            }
            composable(CartDestination.route) {
                CartScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel,
                    onBalanceUpdated = walletBalanceViewModel::refresh,
                    onOpenOrders = {
                        navController.navigate(OrdersDestination.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    themeMode = themeMode,
                    onThemeModeSelected = onThemeModeSelected,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel,
                    onOpenSellerInventory = {
                        navController.navigate(SellerInventoryDestination.createRoute())
                    },
                    onOpenOrders = {
                        navController.navigate(OrdersDestination.route)
                    },
                    onOpenDeliveryBoard = {
                        navController.navigate(DeliveryBoardDestination.route)
                    }
                )
            }
            composable(
                route = SellerInventoryDestination.route,
                arguments = listOf(
                    navArgument("create") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val launchCreateComposer = backStackEntry.arguments?.getBoolean("create") ?: false
                SellerInventoryScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel,
                    launchCreateComposer = launchCreateComposer
                )
            }
            composable(DeliveryVerificationDestination.route) {
                DeliveryVerificationScreen(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    balanceLabel = balanceLabel,
                    onBack = { navController.popBackStack() },
                    onOpenOrders = {
                        navController.navigate(OrdersDestination.route) {
                            launchSingleTop = true
                        }
                    },
                    onBalanceUpdated = walletBalanceViewModel::refresh
                )
            }
            // New voucher scan route
            composable(VoucherScanDestination.route) {
                VoucherScanScreen(
                    onBack = { navController.popBackStack() },
                    onVoucherRedeemed = {
                        walletBalanceViewModel.refresh()
                        navController.popBackStack()
                    }
                )
            }
            composable(
                ProductDetailDestination.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onOpenCart = {
                        navController.navigate(CartDestination.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

// Inside the Box that is the root of AuthenticatedApp, at the bottom
        if (showBottomBar) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                CommerceBottomDock(
                    items = topLevelDestinations,
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        if (item == BottomNavItem.Create) {
                            showQuickActions = true
                        } else if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(BottomNavItem.Home.route)
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionDialog(
    canPostProducts: Boolean,
    onDismiss: () -> Unit,
    onPostProduct: () -> Unit,
    onScanVoucher: () -> Unit,
    onDeliveryVerification: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick actions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (canPostProducts) {
                    Button(
                        onClick = onPostProduct,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Post product")
                    }
                }
                Button(
                    onClick = onScanVoucher,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scan voucher")
                }
                Button(
                    onClick = onDeliveryVerification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delivery verification")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}