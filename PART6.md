# Part 6 – Seller Inventory, Profile, Common Components & Theme

## Scope

Provide **all shared UI components** that every other screen uses, plus the **seller inventory** management and **user profile** screen. Also own the app’s theme (light/dark) and bottom navigation bar.

## Files you maintain

- `ui/screens/seller/SellerInventoryScreen.kt`
- `ui/screens/seller/SellerInventoryViewModel.kt`
- `ui/screens/profile/ProfileScreen.kt`
- `ui/screens/profile/ProfileViewModel.kt`
- `ui/common/AsyncStateViews.kt` – `LoadingStatePane`, `MessageStatePane`
- `ui/common/CommerceChrome.kt` – `CommerceBackdrop`, `CommerceSectionHeader`, `CommerceMetricPill`
- `ui/common/CommerceTopBar.kt` – the main top bar
- `ui/common/CommerceBottomDock.kt` – bottom navigation bar
- `ui/common/MarketAsyncImage.kt` – async image loader with fallback
- `ui/common/BottomNavItem.kt` – bottom tab definitions
- `ui/theme/Color.kt`, `Type.kt`, `Theme.kt`
- `data/repository/AppSettingsRepository.kt` – theme persistence (DataStore)
- `data/model/ThemeMode.kt` – enum (often placed here, but can live in Part 1)

## Public API – shared composables (used by ALL other parts)

### LoadingStatePane
    @Composable
    fun LoadingStatePane(modifier: Modifier = Modifier, message: String? = null)
Centered circular progress indicator with an optional message.

### MessageStatePane
    @Composable
    fun MessageStatePane(
        title: String,
        modifier: Modifier = Modifier,
        message: String? = null,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    )
Card with a title, optional description, and an optional retry button.

### CommerceBackdrop
    @Composable
    fun CommerceBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit)
Full-screen gradient background with decorative circles. Content is placed on top.

### CommerceSectionHeader
    @Composable
    fun CommerceSectionHeader(
        title: String,
        modifier: Modifier = Modifier,
        eyebrow: String? = null,
        description: String? = null,
        trailing: (@Composable () -> Unit)? = null
    )
Section header with optional eyebrow chip, title, and description.

### CommerceMetricPill
    @Composable
    fun CommerceMetricPill(
        icon: ImageVector,
        label: String,
        value: String,
        modifier: Modifier = Modifier
    )
Small card showing an icon, a large value, and a small label.

### CommerceTopBar
    @Composable
    fun CommerceTopBar(
        title: String = "",
        themeMode: ThemeMode,
        notificationCount: Int,
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        onToggleTheme: () -> Unit,
        modifier: Modifier = Modifier,
        showSearch: Boolean = true,
        searchPlaceholder: String = "Search products, sellers, pickup points",
        avatarText: String = "A",
        balanceLabel: String? = null,        // kept for compatibility, no longer shown
        subtitle: String = "",
        onOpenHome: (() -> Unit)? = null,    // click on the home icon
        onOpenProfile: (() -> Unit)? = null  // click on the profile avatar
    )
Design:
- Left side: home icon (clickable if `onOpenHome` is provided) + optional title.
- Right side: search toggle (icon), notification badge, theme toggle icon, profile avatar (clickable if `onOpenProfile` is provided). The avatar shows the first letter of `avatarText`.
- Search field expands below with animation when activated.
- `balanceLabel` is **ignored** – no wallet badge is displayed.

### CommerceBottomDock
    @Composable
    fun CommerceBottomDock(
        items: List<BottomNavItem>,
        currentRoute: String?,
        onItemSelected: (BottomNavItem) -> Unit
    )
Floating bottom bar with navigation icons and a central circular primary action button (the “+” button). Each item is weighted equally.

### MarketAsyncImage
    @Composable
    fun MarketAsyncImage(
        imageUrl: String?,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        shape: Shape = RoundedCornerShape(0.dp),
        contentScale: ContentScale = ContentScale.Crop
    )
Loads an image from URL using Coil, shows a gradient placeholder and loading/error fallbacks.

### BottomNavItem
    sealed class BottomNavItem(
        val route: String,
        val title: String,
        val icon: ImageVector,
        val showLabel: Boolean = true,
        val isPrimaryAction: Boolean = false
    )
Predefined instances: Home, Shop, Create, Wallet, Profile.

### Theme
- `ASTUMarketTheme` composable that applies `Material3` theme with dynamic colours based on `ThemeMode` (LIGHT, DARK, SYSTEM).
- `Color.kt` – defines light and dark colour schemes.
- `Type.kt` – typography definitions.

## Seller inventory behaviour

### SellerInventoryViewModel
- `uiState: StateFlow<SellerInventoryUiState>` containing:
  - `seller: User?`
  - `products: List<Product>`
  - `isLoading: Boolean`, `isSaving: Boolean`, `errorMessage: String?`
- `loadInventory()` – fetches user profile and seller products using `ProductRepository.getSellerProducts(sellerId)`.
- `saveProduct(productId: String?, input: SellerProductInput)` – validates input using `SellerProductValidator` (from Part 3, but can be shared). If valid, calls `createSellerProduct` or `updateSellerProduct` on `ProductRepository`, reloads inventory on success.

### SellerInventoryScreen
- Top bar.
- “Add listing” button.
- List of existing products:
  - Name, description, pickup location, base price, live price, stock, discount badge, visibility chip.
  - “Edit listing” button.
- `ProductEditorDialog` (AlertDialog) for creating/editing:
  - Fields: name, category, description, image URL, pickup location, base price (decimal), stock quantity (number), expiry in days (optional), visibility toggle.
  - On confirm, builds `SellerProductInput` and calls `viewModel.saveProduct`.

## Profile behaviour

### ProfileViewModel
- `uiState: StateFlow<ProfileUiState>` containing user info (from `UserRepository.getCurrentUserProfile()`), theme mode, and loading/error states.
- `signOut()` – calls `AuthRepository.signOut()` (inject both).
- `setThemeMode(mode: ThemeMode)` – calls `AppSettingsRepository.setThemeMode(mode)` and updates local state.

### ProfileScreen
- Top bar.
- User info: avatar initial, display name, email, roles, runner status, trust score.
- Theme selector: three options (Light, Dark, System).
- Links/buttons:
  - “Seller Inventory” (navigates to `SellerInventoryScreen`, visible if user has SELLER role).
  - “My Orders” (navigates to orders).
  - “Delivery Board” (navigates to delivery board, visible if user has RUNNER role or wants to activate).
  - “Sign Out” – calls `viewModel.signOut()`.

## Integration with other parts
- All UI parts use the common composables listed above.
- `AuthenticatedApp` (owned by Part 2) uses `CommerceBottomDock` and `CommerceTopBar` to construct the app shell.
- `AppSettingsRepository` is injected wherever theme changes are needed.

## Dependencies
- Part 1 (repositories: `UserRepository`, `ProductRepository`, `AuthRepository`, `AppSettingsRepository`).
- Part 3 may provide `SellerProductValidator`, but you can keep a copy in `util/` if not.