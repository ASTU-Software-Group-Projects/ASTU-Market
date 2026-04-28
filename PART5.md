# Part 5 – Cart, Orders & Delivery UI

## Scope

Shopping cart, checkout flow, order timeline with status updates, runner gig board, and delivery verification (QR passes + scan).

## Files you maintain

- `ui/screens/cart/CartScreen.kt`
- `ui/screens/cart/CartViewModel.kt`
- `ui/screens/orders/OrdersScreen.kt`
- `ui/screens/orders/OrdersViewModel.kt`
- `ui/screens/delivery/DeliveryVerificationScreen.kt`
- `ui/screens/delivery/DeliveryVerificationViewModel.kt`
- `ui/screens/delivery/GigBoardScreen.kt`
- `ui/screens/delivery/GigBoardViewModel.kt`
- Utility helpers (if not provided by Part 1):
  - `util/OrderLifecycle.kt`
  - `util/OrderPricing.kt`
  - `util/RunnerTrust.kt`

## Public API (utility helpers that other parts may use)

### OrderLifecycle
    fun OrderStatus.isLive(): Boolean
    fun MarketOrder.canBeMarkedPickedUpBy(userId: String?): Boolean
    fun MarketOrder.canBeMarkedInTransitBy(userId: String?): Boolean
    fun MarketOrder.canBeDeliveredBy(userId: String?): Boolean
    fun MarketOrder.canBeCancelledBy(userId: String?): Boolean
    fun MarketOrder.canBeDisputedBy(userId: String?): Boolean
    fun MarketOrder.actorFor(userId: String?): ActorType

### OrderPricing
    data class OrderPricingBreakdown(
        val buyerCharge: Double,
        val protocolFee: Double,
        val runnerReward: Double,
        val sellerPayout: Double
    )
    fun calculateOrderPricing(lineTotal: Double): OrderPricingBreakdown

### RunnerTrust
    data class TrustBand(val label: String, val maxOrderValue: Double)
    fun runnerTrustBand(trustScore: Int): TrustBand

## Internal behaviour

### CartViewModel / CartScreen
- `uiState: StateFlow<CartUiState>` containing:
  - `cartItems: List<CartItem>`
  - `isLoading: Boolean`, `isCheckingOut: Boolean`, `errorMessage: String?`
- `loadCart()` – calls `CartRepository.getCartItemsList()`.
- `removeFromCart(productId: String)` – calls `CartRepository.removeFromCart(productId)` and reloads cart.
- `checkout()` – calls `OrderRepository.checkoutCart()`, emits `CartEvent.CheckoutComplete(count: Int)`.
- `total: Double` – computed property: sum of `item.price * item.quantity`.
- Screen:
  - Top bar using `CommerceTopBar`.
  - Hero card with cart summary and total.
  - List of cart items: each row shows image, name, quantity, line total, remove button.
  - Checkout preview card: shows buyer charge, protocol fee, runner reward, seller payout using `OrderPricing`.
  - “Create Escrow Orders” button – enabled when cart is not empty and not already checking out.
  - On successful checkout, a snackbar appears with “View orders” action that navigates to orders.

### OrdersViewModel / OrdersScreen
- `uiState: StateFlow<OrdersUiState>` containing:
  - `currentUserId: String?`
  - `orders: List<MarketOrder>`
  - `isLoading: Boolean`, `activeOrderId: String?`, `errorMessage: String?`
- `loadOrders()` – calls `UserRepository.getCurrentUserProfile()` and `OrderRepository.getOrdersForCurrentUser()`.
- `markPickedUp(orderId)`, `markInTransit(orderId)`, `markDelivered(orderId, code, note)`,
  `cancelOrder(orderId, reason)`, `openDispute(orderId, reason)` – each calls the corresponding `OrderRepository` method, then reloads orders.
- Screen:
  - Top bar.
  - Hero card with total order count and live order count.
  - List of order cards:
    - Product name, creation date, actor role text.
    - Status chip (coloured by status).
    - Inline metrics (escrow amount, pickup location).
    - Payment/payout status chips.
    - If buyer, shows delivery code when order is in appropriate status.
    - Action buttons (Picked up, In transit, Deliver, Cancel, Dispute) – visibility controlled by `OrderLifecycle` helpers.
  - Dialogs for delivery proof (enter code + note), cancellation reason, dispute reason.

### GigBoardViewModel / GigBoardScreen
- `uiState: StateFlow<GigBoardUiState>` containing:
  - `runner: User?`
  - `gigs: List<DeliveryGig>`
  - `isLoading: Boolean`, `isActivating: Boolean`, `isAcceptingGigId: String?`, `errorMessage: String?`
- `loadBoard()` – calls `DeliveryRepository.getRunnerProfile()` and `DeliveryRepository.getAvailableGigs()`.
- `activateRunner()` – calls `DeliveryRepository.activateRunner()`, reloads board.
- `acceptGig(orderId: String)` – calls `DeliveryRepository.acceptGig(orderId)`, reloads board.
- Screen:
  - Top bar.
  - If runner profile is not active: runner activation card with “Pass quiz and activate” button.
  - Otherwise:
    - Hero card with trust score, band label, max gig value.
    - List of gig cards showing product name, pickup location, reward, escrow amount, trust tier, “Accept” button.
    - Only gigs within the runner’s trust band are shown.

### DeliveryVerificationViewModel / DeliveryVerificationScreen
- `uiState: StateFlow<DeliveryVerificationUiState>` containing:
  - `currentUser: User?`
  - `orders: List<MarketOrder>`
  - `isLoading: Boolean`, `activeOrderId: String?`, `errorMessage: String?`
- `loadVerificationState()` – loads user profile and orders.
- `markDelivered(orderId: String, confirmationCode: String)` – calls `OrderRepository.markDelivered(orderId, confirmationCode, "Verified via QR code.")`, reloads.
- Screen:
  - Top bar with back button.
  - **Buyer view**: for orders where `buyerId == currentUser` and status is `RUNNER_ASSIGNED`, `PICKED_UP`, or `IN_TRANSIT`, shows a card with a QR code of `deliveryToken` (generated using `BarcodeEncoder`), and the token text.
  - **Runner view**: for orders that can be delivered by the current user (`canBeDeliveredBy`), shows a card with “Scan buyer QR” button. Uses `ScanContract` to open the camera. Scanned code is passed to `markDelivered`.

## Integration with Part 1
- Injects `CartRepository`, `OrderRepository`, `DeliveryRepository`, `UserRepository`.

## Dependencies
- Part 1 (repositories).
- Part 6 (common composables: `CommerceTopBar`, `CommerceBackdrop`, `CommerceSectionHeader`, `CommerceMetricPill`, `MarketAsyncImage`, `LoadingStatePane`, `MessageStatePane`, `CommerceBottomDock` may also be used if navigation is handled here, but usually not).