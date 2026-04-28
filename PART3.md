# Part 3 – Home, Discover & Product Detail UI

## Scope

Product browsing, search, category/sort filtering, and the product detail screen with “Add to  
cart”.

## Files you maintain

- `ui/screens/home/HomeScreen.kt`
- `ui/screens/home/HomeViewModel.kt`
- `ui/screens/discover/DiscoverScreen.kt`
- `ui/screens/detail/ProductDetailScreen.kt`
- `ui/screens/detail/ProductDetailViewModel.kt`

## Public API

None. These are screens that are called by the navigation host. They do not expose functions  
to other parts.

## Internal behaviour

### HomeViewModel
- Loads all products (`ProductRepository.getProducts()`) and the current user profile.
- Computes a list of distinct categories from the products.
- Exposes `uiState: StateFlow<HomeUiState>` with:
  - `products`, `categories`, `shopper` (User?), `notificationCount`, `isLoading`, `errorMessage`.

### HomeScreen
- Uses `CommerceTopBar` (from Part 6) with search toggle.
- Shows a hero card, maybe a few product highlights, and quick links to Discover, Wallet, Orders.
- Tapping a product navigates to `ProductDetailScreen`.

### DiscoverScreen
- Top bar with a live search field (bypassing the toggle – always visible).
- Category chips (from `uiState.categories` plus an “All” option).
- Sort chips (`BestMatch`, `LowestPrice`, `HighestPrice`).
- Filtered product list assembled **locally**:
  - `matchesCategory`: product.category equals selected category or “All”.
  - `matchesQuery`: product name/category/description/seller/pickupLocation contain the search  
    text.
  - Sorting:
    - BestMatch orders by flash‑deal discount descending, then by name.
    - LowestPrice / HighestPrice by `product.price`.
- Each product row is clickable → navigates to `ProductDetailScreen`.
- Removed all verbose section headers; only chips and the list are shown.

### ProductDetailScreen / ViewModel
- Receives `productId` as a navigation argument.
- Loads product from `ProductRepository.getProduct(productId)`.
- Displays:
  - Image (using `MarketAsyncImage`)
  - Name, price, discount badge, description
  - Seller name, pickup location, stock quantity
- “Add to cart” button – calls `CartRepository.addProductToCart(product)`.
- Shows loading/error states with `LoadingStatePane` / `MessageStatePane`.

## Integration with Part 1
- Injects `ProductRepository`, `CartRepository`, `UserRepository`.

## Dependencies
- Part 1 (repositories).
- Part 6 (common composables: `CommerceTopBar`, `CommerceBackdrop`, `MarketAsyncImage`,  
  `LoadingStatePane`, `MessageStatePane`).