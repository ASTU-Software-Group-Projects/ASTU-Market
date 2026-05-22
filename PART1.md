# Part 1 – Backend & Data Layer

You own **all data models, Firebase Auth / Firestore integration, and the Hilt dependency  
injection module**.

## Scope

- Define every data class used in the app (User, Product, Wallet, Order …).
- Implement **repositories** that expose clean `Result<T>` functions for:
  - Authentication (sign up / sign in / sign out)
  - User profile
  - Products (load, create, update)
  - Cart (add, remove, get)
  - Orders (checkout, status transitions, dispute)
  - Delivery / Runner (gigs, activation)
  - Wallet (balance, transactions, top‑up, send money, voucher redemption, airtime purchase)
  - App settings (theme persistence)
- Wire everything with **Hilt** (`AppModule`).

## Files you maintain

- `data/model/*.kt` – all model classes and enums
- `data/repository/*.kt` – all repository classes
- `di/AppModule.kt` – Hilt module providing `FirebaseAuth` and `FirebaseFirestore`
- `google-services.json`, `firebase.json`, `firestore.rules`, `firestore.indexes.json`

## Public API (the only thing other parts may call)

All repository functions return `kotlin.Result<T>` and throw `IllegalStateException` for  
invalid state.

### `AuthRepository`
- `fun getCurrentUser(): FirebaseUser?`
- `fun isUserAuthenticated(): Boolean`
- `suspend fun signUpWithEmail(registration: UserRegistration): Result<Unit>`
- `suspend fun signInWithEmail(email: String, password: String): Result<Unit>`
- `fun signOut()`

### `UserRepository`
- `suspend fun getCurrentUserProfile(): Result<User>`
- `suspend fun getUserProfile(uid: String): Result<User>`
- `suspend fun activateRunnerProtocol(): Result<User>`

### `ProductRepository`
- `suspend fun getProducts(): Result<List<Product>>`
- `suspend fun getProduct(productId: String): Result<Product?>`
- `suspend fun getSellerProducts(sellerId: String): Result<List<Product>>`
- `suspend fun createSellerProduct(input: SellerProductInput, seller: User): Result<Unit>`
- `suspend fun updateSellerProduct(productId: String, input: SellerProductInput, seller: User): Result<Unit>`

### `CartRepository`
- `suspend fun getCartItemsList(): Result<List<CartItem>>`
- `suspend fun addProductToCart(product: Product): Result<Unit>`
- `suspend fun removeFromCart(productId: String): Result<Unit>`

### `OrderRepository`
- `suspend fun getOrdersForCurrentUser(): Result<List<MarketOrder>>`
- `suspend fun checkoutCart(): Result<Int>` – returns number of orders created
- `suspend fun markPickedUp(orderId: String): Result<Unit>`
- `suspend fun markInTransit(orderId: String): Result<Unit>`
- `suspend fun markDelivered(orderId: String, confirmationCode: String, proofNote: String): Result<Unit>`
- `suspend fun cancelOrder(orderId: String, reason: String): Result<Unit>`
- `suspend fun openDispute(orderId: String, reason: String): Result<Unit>`

### `DeliveryRepository`
- `suspend fun getRunnerProfile(): Result<User>`
- `suspend fun getAvailableGigs(): Result<List<DeliveryGig>>`
- `suspend fun activateRunner(): Result<Unit>`
- `suspend fun acceptGig(orderId: String): Result<Unit>`

### `WalletRepository`
- `suspend fun getWallet(): Result<Wallet>`
- `suspend fun getTransactions(limit: Long = 25): Result<List<WalletTransaction>>`
- `suspend fun topUp(amount: Double): Result<Unit>` – direct balance load
- `suspend fun findUserByPhone(phone: String): Result<User>`
- `suspend fun sendMoney(toUid: String, amount: Double): Result<Unit>`
- `suspend fun topUpFromVoucher(voucherCode: String): Result<Unit>` – voucher redemption
- `suspend fun buyAirtime(phoneNumber: String, amount: Double): Result<Unit>` – deducts balance

### `AppSettingsRepository`
- `val themeMode: Flow<ThemeMode>`
- `suspend fun setThemeMode(themeMode: ThemeMode)`

## What you are NOT required to do

- **No UI code** – other parts build all screens.
- **No ViewModels** – those are in Parts 2‑6.
- **No business‑logic helpers** – some utility functions (ordering, pricing) will be created by  
  the UI teams, but you can place them in `util/` if you prefer.

## Integration notes

- Every repository uses `FirebaseAuth.currentUser?.uid` to identify the current user.
- `checkoutCart()` and `sendMoney()` must use **Firestore transactions** to keep data consistent.
- The `vouchers` collection is assumed to contain documents with fields `amount`, `redeemed`,  
  `redeemedBy`, `redeemedAt`.
- When `signUpWithEmail` creates a user, it must also create a wallet document and a welcome  
  transaction.

## Dependencies

None
