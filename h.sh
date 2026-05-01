#!/bin/bash

# ASTU Market – create package skeleton
# Run this script from the root of the group project:
#   chmod +x create_skeleton.sh
#   ./create_skeleton.sh

BASE="app/src/main/java/com/market/astu"

# ---------- helper ----------
create_file() {
    local rel_path="$1"
    local pkg="$2"
    local file="$BASE/$rel_path"

    if [ -f "$file" ]; then
        echo "SKIP: $file already exists"
    else
        mkdir -p "$(dirname "$file")"
        echo "package $pkg" > "$file"
        echo "CREATED: $file"
    fi
}

# ---------- root ----------
create_file "ASTUMarketApp.kt"              "com.market.astu"
# MainActivity.kt already exists (if you want to overwrite only the package line, remove the later comment)
# create_file "MainActivity.kt"              "com.market.astu"

# ---------- data/model ----------
create_file "data/model/CartItem.kt"               "com.market.astu.data.model"
create_file "data/model/DeliveryGig.kt"            "com.market.astu.data.model"
create_file "data/model/MarketOrder.kt"             "com.market.astu.data.model"
create_file "data/model/OrderStatus.kt"            "com.market.astu.data.model"
create_file "data/model/Product.kt"                "com.market.astu.data.model"
create_file "data/model/RunnerStatus.kt"           "com.market.astu.data.model"
create_file "data/model/SellerProductInput.kt"     "com.market.astu.data.model"
create_file "data/model/ThemeMode.kt"              "com.market.astu.data.model"
create_file "data/model/User.kt"                   "com.market.astu.data.model"
create_file "data/model/UserRegistration.kt"       "com.market.astu.data.model"
create_file "data/model/UserRole.kt"               "com.market.astu.data.model"
create_file "data/model/Wallet.kt"                 "com.market.astu.data.model"
create_file "data/model/WalletTransaction.kt"      "com.market.astu.data.model"

# ---------- data/repository ----------
create_file "data/repository/AppSettingsRepository.kt"  "com.market.astu.data.repository"
create_file "data/repository/AuthRepository.kt"         "com.market.astu.data.repository"
create_file "data/repository/CartRepository.kt"         "com.market.astu.data.repository"
create_file "data/repository/DeliveryRepository.kt"     "com.market.astu.data.repository"
create_file "data/repository/OrderRepository.kt"        "com.market.astu.data.repository"
create_file "data/repository/ProductRepository.kt"      "com.market.astu.data.repository"
create_file "data/repository/UserRepository.kt"         "com.market.astu.data.repository"
create_file "data/repository/WalletRepository.kt"       "com.market.astu.data.repository"

# ---------- di ----------
create_file "di/AppModule.kt"   "com.market.astu.di"

# ---------- ui (top-level) ----------
create_file "ui/AppViewModel.kt"            "com.market.astu.ui"
create_file "ui/WalletBalanceViewModel.kt"  "com.market.astu.ui"

# ---------- ui/common ----------
create_file "ui/common/AsyncStateViews.kt"       "com.market.astu.ui.common"
create_file "ui/common/BottomNavItem.kt"         "com.market.astu.ui.common"
create_file "ui/common/CommerceBottomDock.kt"    "com.market.astu.ui.common"
create_file "ui/common/CommerceChrome.kt"        "com.market.astu.ui.common"
create_file "ui/common/CommerceTopBar.kt"        "com.market.astu.ui.common"
create_file "ui/common/MarketAsyncImage.kt"      "com.market.astu.ui.common"

# ---------- ui/navigation ----------
create_file "ui/navigation/AuthenticatedApp.kt"       "com.market.astu.ui.navigation"
create_file "ui/navigation/NavGraph.kt"               "com.market.astu.ui.navigation"
create_file "ui/navigation/QuickActionsViewModel.kt"  "com.market.astu.ui.navigation"

# ---------- ui/screens/auth ----------
create_file "ui/screens/auth/AuthViewModel.kt"  "com.market.astu.ui.screens.auth"
create_file "ui/screens/auth/SignInScreen.kt"   "com.market.astu.ui.screens.auth"
create_file "ui/screens/auth/SignUpScreen.kt"   "com.market.astu.ui.screens.auth"

# ---------- ui/screens/cart ----------
create_file "ui/screens/cart/CartScreen.kt"     "com.market.astu.ui.screens.cart"
create_file "ui/screens/cart/CartViewModel.kt"  "com.market.astu.ui.screens.cart"

# ---------- ui/screens/delivery ----------
create_file "ui/screens/delivery/DeliveryVerificationScreen.kt"      "com.market.astu.ui.screens.delivery"
create_file "ui/screens/delivery/DeliveryVerificationViewModel.kt"   "com.market.astu.ui.screens.delivery"
create_file "ui/screens/delivery/GigBoardScreen.kt"                 "com.market.astu.ui.screens.delivery"
create_file "ui/screens/delivery/GigBoardViewModel.kt"              "com.market.astu.ui.screens.delivery"

# ---------- ui/screens/detail ----------
create_file "ui/screens/detail/ProductDetailScreen.kt"     "com.market.astu.ui.screens.detail"
create_file "ui/screens/detail/ProductDetailViewModel.kt"  "com.market.astu.ui.screens.detail"

# ---------- ui/screens/discover ----------
create_file "ui/screens/discover/DiscoverScreen.kt"  "com.market.astu.ui.screens.discover"

# ---------- ui/screens/home ----------
create_file "ui/screens/home/HomeScreen.kt"     "com.market.astu.ui.screens.home"
create_file "ui/screens/home/HomeViewModel.kt"  "com.market.astu.ui.screens.home"

# ---------- ui/screens/orders ----------
create_file "ui/screens/orders/OrdersScreen.kt"     "com.market.astu.ui.screens.orders"
create_file "ui/screens/orders/OrdersViewModel.kt"  "com.market.astu.ui.screens.orders"

# ---------- ui/screens/profile ----------
create_file "ui/screens/profile/ProfileScreen.kt"     "com.market.astu.ui.screens.profile"
create_file "ui/screens/profile/ProfileViewModel.kt"  "com.market.astu.ui.screens.profile"

# ---------- ui/screens/seller ----------
create_file "ui/screens/seller/SellerInventoryScreen.kt"     "com.market.astu.ui.screens.seller"
create_file "ui/screens/seller/SellerInventoryViewModel.kt"  "com.market.astu.ui.screens.seller"

# ---------- ui/screens/wallet ----------
create_file "ui/screens/wallet/ReceiveMoneySheet.kt"    "com.market.astu.ui.screens.wallet"
create_file "ui/screens/wallet/SendMoneyDialog.kt"      "com.market.astu.ui.screens.wallet"
create_file "ui/screens/wallet/VoucherScanScreen.kt"    "com.market.astu.ui.screens.wallet"
create_file "ui/screens/wallet/VoucherScanViewModel.kt" "com.market.astu.ui.screens.wallet"
create_file "ui/screens/wallet/WalletScreen.kt"         "com.market.astu.ui.screens.wallet"
create_file "ui/screens/wallet/WalletViewModel.kt"      "com.market.astu.ui.screens.wallet"

# ---------- ui/theme ----------
create_file "ui/theme/Color.kt"  "com.market.astu.ui.theme"
create_file "ui/theme/Theme.kt"  "com.market.astu.ui.theme"
create_file "ui/theme/Type.kt"   "com.market.astu.ui.theme"

# ---------- util ----------
create_file "util/AuthFormValidator.kt"       "com.market.astu.util"
create_file "util/CampusFormatting.kt"        "com.market.astu.util"
create_file "util/DynamicPricing.kt"          "com.market.astu.util"
create_file "util/ErrorMessage.kt"            "com.market.astu.util"
create_file "util/OrderLifecycle.kt"          "com.market.astu.util"
create_file "util/OrderPricing.kt"            "com.market.astu.util"
create_file "util/PriceFormatter.kt"          "com.market.astu.util"
create_file "util/RunnerTrust.kt"             "com.market.astu.util"
create_file "util/SellerProductValidator.kt"  "com.market.astu.util"

echo ""
echo "All missing Kotlin files have been created with the correct package declarations."
echo "You can now paste the class code into each file."
