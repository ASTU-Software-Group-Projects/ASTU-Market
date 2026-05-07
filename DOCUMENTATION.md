# 🛒 ASTU Market – Campus Commerce App

> **A full-featured Android marketplace built by ASTU students, for ASTU students.**

ASTU Market is a comprehensive Android marketplace designed and developed exclusively for the **Adama Science and Technology University (ASTU)** community. We built this application from the ground up to solve a problem we lived firsthand: there was no reliable, trusted, campus-aware platform for students to buy and sell goods within the university ecosystem. Informal WhatsApp groups and notice boards were the norm — fragmented, insecure, and inefficient. ASTU Market replaces all of that with a single, unified platform featuring a secure digital wallet, role-based interfaces, QR-powered delivery verification, and real-time order tracking.

This is not just a marketplace. It is a campus commerce ecosystem — one that understands the rhythms of student life, the trust dynamics of a campus community, and the practical constraints of peer-to-peer trade between students who may or may not know each other.

---

## 📋 Table of Contents

- [App Demo](#-app-demo)
- [App Experience](#-app-experience-light-vs-dark)
- [Motivation and Background](#-motivation-and-background)
- [Key Features](#-key-features)
- [User Roles and Flows](#-user-roles-and-flows)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Database Design](#-database-design)
- [Project Structure](#-project-structure)
- [Module Breakdown](#-module-breakdown)
- [Development Roadmap](#-development-roadmap)
- [Getting Started](#-getting-started)
- [QR Scanning System](#-qr-scanning-system)
- [Wallet and Payments](#-wallet-and-payments)
- [Security Model](#-security-model)
- [Testing Strategy](#-testing-strategy)
- [Known Limitations and Future Work](#-known-limitations-and-future-work)
- [Contributing](#-contributing)
- [Team](#-team)

---

## 🎬 App Demo

Watch ASTU Market in action:

▶️ [Click here to watch the Video Demo](#)

The demo walkthrough covers:
- Buyer registration and product browsing
- Adding items to cart and completing a purchase
- Wallet top-up and peer-to-peer transfer via QR code
- Seller inventory management and order fulfillment
- QR-based delivery verification between buyer and seller

---

## 🏠 App Experience (Light vs. Dark)

ASTU Market is fully optimized for both **Light Mode** and **Dark Mode** to ensure a comfortable experience across different environments — whether students are browsing products during the day or checking orders late at night in dorm rooms.

The UI theme automatically adapts based on the user's system settings, while still maintaining consistent branding, readability, and navigation patterns.

| Feature | Light Mode | Dark Mode |
|:---:|:---:|:---:|
| **Dashboard** | ![Dashboard Light](https://i.8upload.com/image/98cdb06b8ea41684/dashboard.jpg) | ![Dashboard Dark](https://i.8upload.com/image/cacb22f58560269a/dashboard-dark.jpg) |
| **Discovery** | ![Discovery Light](https://i.8upload.com/image/80d85a2f69fb3f97/discovery.jpg) | ![Discovery Dark](https://i.8upload.com/image/d23384e5faa011d9/discovery-dark.jpg) |
| **Wallet** | ![Wallet Light](https://i.8upload.com/image/873b0637ecdfb735/wallet.jpg) | ![Wallet Dark](https://i.8upload.com/image/9731264c5221bcde/wallet-dark.jpg) |
| **Account** | ![Account Light](https://i.8upload.com/image/3340f5ae4147cc9f/account.jpg) | ![Account Dark](https://i.8upload.com/image/16b7bb429ee13775/account-dark.jpg) |

### Theme Design Goals

Both themes were carefully designed with the following principles:

- **Accessibility-first contrast ratios** for readability in all lighting conditions  
- **Consistent navigation hierarchy** regardless of theme  
- **Reduced eye strain** in dark environments with softened backgrounds and controlled accent brightness  
- **Brand continuity** using ASTU Market's primary color palette across both themes  

Dark mode is especially useful for students using the app at night, while light mode provides a clean and familiar browsing experience during daytime use.

---

## 💡 Motivation and Background

ASTU is a residential university with thousands of students who constantly buy, sell, and trade items within the campus — textbooks at semester end, electronics, clothing, stationery, handmade goods, and much more. Before ASTU Market, students had to rely on:

- **WhatsApp groups** — no search, no categories, no transaction history, no trust mechanism
- **Physical notice boards** — static, unupdatable, weather-dependent
- **Word of mouth** — limited reach, zero accountability
- **Off-campus platforms** — not campus-aware, no delivery coordination, potentially unsafe for students

These methods worked well enough for one-off transactions between close friends, but they fell apart completely for strangers, for higher-value goods, and for any transaction that required a payment trail or dispute resolution.

We identified three core pain points through informal interviews and observation:

1. **Discoverability** — students had no way to browse what was available for sale across campus in an organized way
2. **Trust and payment security** — handing cash to a stranger or a near-stranger on campus is uncomfortable; there was no escrow or verification mechanism
3. **Delivery coordination** — arranging pickup and confirming successful handoff was informal and prone to disputes

ASTU Market addresses all three. Products are organized and searchable. Payments go through a campus wallet with peer-to-peer transfer. Delivery is confirmed through a mutual QR scan that acts as a digital handshake — both parties must be present and willing.

---

## 🚀 Key Features

### 👥 Role-Based Experience

We built the app around two core roles — **Buyer** and **Seller** — that shape the entire user experience from the dashboard down to the navigation structure.

**Buyer role** gives students:
- A discovery-first home screen optimized for browsing and searching
- A shopping cart that persists across sessions
- Order tracking with real-time status updates
- A wallet for managing balance and sending payments

**Seller role** gives students:
- An inventory dashboard for managing active listings
- Sales analytics showing order history and revenue
- Incoming order notifications with one-tap fulfillment
- QR code generation for delivery verification

**Role switching** is a first-class feature. A student might be a seller on Monday (offloading last semester's books) and a buyer on Friday (picking up a new charger). We do not force users to choose permanently — the switch is instant and the interface adapts completely.

The role system is enforced at both the UI and the Firestore security rule level, so a buyer cannot accidentally access seller-only data and vice versa.

---

### 🏠 Dynamic Dashboard

The dashboard is the command center of ASTU Market. It is personalized, contextual, and action-oriented.

For buyers, it shows:
- A personalized greeting using the student's display name
- Active orders and their current status at a glance
- Featured products curated by category
- Quick-action shortcuts to the wallet, scanner, and top categories

For sellers, it shows:
- Pending orders requiring action
- Inventory health — how many items are in stock, low stock, or sold out
- Recent sales activity
- Shortcuts to add a new listing or manage existing ones

The dashboard data is fetched in real time from Cloud Firestore and updates without requiring a manual refresh.

---

### 🔍 Product Discovery

Students can browse the marketplace in two modes: **category browsing** and **search**.

**Category browsing** organizes products into six main categories:
- 📚 Books and Study Materials
- 💻 Electronics and Accessories
- 👕 Clothing and Fashion
- 🖊️ Stationery and Office Supplies
- 🍱 Food and Snacks
- 🧴 Personal Care and Lifestyle

Each category page shows products sorted by listing recency by default, with the option to sort by price.

**Search** uses Firestore's native querying capabilities to provide real-time filtering as the user types. Search works across product titles and seller display names. We intentionally kept the search simple and fast rather than building heavyweight full-text search — for a campus marketplace, relevance by recency and category is usually sufficient.

**Product detail pages** include:
- Product photos (up to four images)
- Full description written by the seller
- Price with clear formatting
- Seller name, profile photo, and seller rating
- A one-tap "Add to Cart" or "Buy Now" action
- A contact seller button that opens an in-app message thread

---

### 💳 Secure Campus Wallet

The wallet is the financial backbone of ASTU Market. Rather than integrating external payment gateways (which would require banking relationships, compliance overhead, and user verification beyond a student ID), we built a campus-scoped digital wallet.

**Balance management:** Students can view their current wallet balance on the dedicated wallet screen and within the dashboard. Balance updates in real time via Firestore listeners — no manual refresh needed.

**Peer-to-peer transfers via QR code:** This is the primary payment mechanism for marketplace transactions. When a buyer initiates payment:
1. The seller opens their wallet and displays their unique QR code
2. The buyer scans it using the in-app QR scanner
3. The buyer confirms the transfer amount
4. Funds move instantly from buyer wallet to seller wallet
5. Both parties receive a transaction confirmation

The QR code encodes the seller's user ID and wallet address — not the balance or any sensitive financial data. The transfer itself is processed server-side through Firestore transactions, which are atomic and cannot result in partial transfers.

**Airtime purchase:** Students can purchase mobile airtime directly through the wallet. This feature uses a voucher-based system where the app generates a purchase request, the voucher code is delivered digitally, and the amount is deducted from the wallet balance.

**Campus voucher redemption:** Students who receive campus vouchers (from the university, from promotions, or from other students as gifts) can redeem them by scanning the voucher QR code. The wallet balance is credited instantly.

**Transaction history:** Every wallet event — top-up, transfer sent, transfer received, voucher redemption — is logged with a timestamp, amount, and counterparty. Students have a full audit trail of their campus financial activity.

---

### 📦 Order and Delivery Management

**For buyers:**

Order tracking is real-time and uses a clear status timeline:
1. **Order Placed** — payment received, seller notified
2. **Confirmed** — seller has acknowledged the order
3. **Ready for Pickup** — item is prepared and the seller is available
4. **In Transit** — if delivery is involved, the item is on its way
5. **Delivered** — QR verification completed, order closed

Each status change triggers a push notification to the buyer. The timeline view on the order detail screen shows when each status was reached and by whom.

**For sellers:**

Sellers manage their inventory through a dedicated inventory screen. Each listing shows:
- Product photo, title, and current price
- Quantity in stock
- Number of active orders for that item
- Quick-edit and quick-delete actions

When a new order comes in, the seller receives a notification and sees it at the top of their orders queue. Accepting an order moves it to "Confirmed" and begins the delivery coordination flow.

**Delivery verification via QR:**

This is one of our most important features for building trust between strangers on campus. When a seller marks an order as "Ready for Pickup," the system generates a unique delivery QR code for that order. The flow works as follows:

1. Buyer and seller meet at the agreed location
2. Seller displays the delivery QR code on their device
3. Buyer scans it using the in-app scanner
4. The system verifies the QR against the order record in Firestore
5. If verified, the order is marked "Delivered" and the transaction is complete

This mutual scan requirement means both parties must be physically present with their devices for delivery to be confirmed. It prevents false delivery claims and gives both parties a clear, timestamped record of the handoff.

---

### 🔔 Notifications

The app uses Firebase Cloud Messaging (FCM) for push notifications. Notifications are sent for:
- New order placed (seller)
- Order confirmed (buyer)
- Order ready for pickup (buyer)
- Order delivered (buyer and seller)
- Wallet transfer received (recipient)
- New message in chat thread (both parties)

Notification preferences can be managed in the app settings. All notification data is stored locally so that missed notifications are visible in the in-app notification center even if the push was dismissed.

---

## 👤 User Roles and Flows

### Registration and Onboarding

New users register with their university email address and a password. We use Firebase Authentication for credential management. After registration:

1. **Email verification** — Firebase sends a verification email; unverified accounts have read-only access
2. **Profile setup** — display name, profile photo, and campus department
3. **Role selection** — the user chooses their initial role (Buyer or Seller); this can be changed later

The onboarding flow is designed to be completable in under two minutes. We deliberately kept the required information minimal — a display name and a role selection are all that is needed to start using the app.

### Buyer Flow

```
Register / Log In
    ↓
Browse categories or search
    ↓
View product detail
    ↓
Add to cart → Review cart → Proceed to payment
    ↓
Scan seller QR code → Confirm transfer
    ↓
Track order status → Meet seller → Scan delivery QR
    ↓
Order complete → Leave review
```

### Seller Flow

```
Register / Log In → Switch to Seller role
    ↓
Create listing (title, description, photos, price, quantity)
    ↓
Listing goes live in marketplace
    ↓
Receive order notification → Accept order
    ↓
Prepare item → Mark as Ready → Generate delivery QR
    ↓
Meet buyer → Buyer scans QR → Order complete
    ↓
Funds credited to wallet
```

---

## 🛠 Tech Stack

| Layer | Technology | Rationale |
|---|---|---|
| Language | Kotlin | Modern, concise, null-safe; first-class Android support |
| UI Framework | Jetpack Compose | Declarative UI allows rapid iteration and clean state management |
| Dependency Injection | Dagger Hilt | Compile-time verified DI with minimal boilerplate |
| Authentication | Firebase Auth | Email/password auth with email verification out of the box |
| Database | Cloud Firestore | Real-time listeners, offline persistence, flexible document model |
| Storage | Firebase Storage | Photo upload and retrieval for product listings and profiles |
| Notifications | Firebase Cloud Messaging | Cross-device push notifications with topic support |
| QR Scanning | ZXing-embedded / CameraX | High-performance, license-compatible QR processing |
| Async | Kotlin Coroutines + Flow | Structured concurrency and reactive streams in idiomatic Kotlin |
| Navigation | Jetpack Navigation Compose | Type-safe navigation graph with deep link support |
| Image Loading | Coil | Coroutine-native image loading with Compose integration |

**Why Jetpack Compose?** Our team made the deliberate decision to go 100% Compose rather than mixing it with the legacy XML View system. Compose's unidirectional data flow model made it much easier to reason about UI state — especially important in a role-switching app where the same screen might need to render very differently depending on context. It also accelerated our iteration speed significantly during the prototyping phases.

**Why Firebase?** As a university project team without dedicated backend engineers, Firebase gave us a production-quality backend with authentication, real-time database, file storage, and push notifications managed by Google's infrastructure. The real-time listeners in Firestore were particularly valuable for the order tracking and wallet features, where we needed the UI to update without polling.

---

## 🏗 System Architecture

ASTU Market follows a clean, layered architecture based on the principles of separation of concerns and unidirectional data flow.

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Jetpack Compose)            │
│  Screens → Composables → ViewModels → UI State          │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                         │
│  Use Cases → Domain Models → Repository Interfaces      │
├─────────────────────────────────────────────────────────┤
│                    Data Layer                           │
│  Repositories → Data Sources → DTOs                    │
├─────────────────────────────────────────────────────────┤
│              External Services (Firebase)               │
│  Firestore │ Auth │ Storage │ FCM                       │
└─────────────────────────────────────────────────────────┘
```

**UI Layer** contains all Composable screens and their associated ViewModels. Each screen has a single ViewModel that exposes a `StateFlow` of UI state. The screen observes this flow and recomposes reactively. User interactions are sent to the ViewModel as events.

**Domain Layer** contains use cases that encode the business logic of the application. Each use case is a single Kotlin class with an `invoke` operator. They depend on repository interfaces, not concrete implementations, which makes them independently testable.

**Data Layer** contains the concrete implementations of the repository interfaces. Each repository is responsible for a single data domain (products, orders, wallet, users). Repositories coordinate between Firestore and local state as needed.

**Dependency Injection** is handled entirely by Dagger Hilt. Hilt modules provide bindings for all repository implementations, Firebase instances, and use cases. The component hierarchy follows the standard Android Hilt setup: `SingletonComponent` for app-scoped bindings, `ViewModelComponent` for ViewModel-scoped bindings.

---

## 🗄 Database Design

Our Firestore schema is organized around five top-level collections:

### `users` collection
```
users/{userId}
  ├── displayName: String
  ├── email: String
  ├── photoUrl: String?
  ├── role: "buyer" | "seller"
  ├── department: String
  ├── walletBalance: Number
  ├── walletId: String
  ├── createdAt: Timestamp
  └── fcmToken: String
```

### `products` collection
```
products/{productId}
  ├── title: String
  ├── description: String
  ├── price: Number
  ├── category: String
  ├── quantity: Number
  ├── sellerId: String
  ├── sellerName: String
  ├── imageUrls: Array<String>
  ├── isActive: Boolean
  ├── createdAt: Timestamp
  └── updatedAt: Timestamp
```

### `orders` collection
```
orders/{orderId}
  ├── buyerId: String
  ├── sellerId: String
  ├── productId: String
  ├── productTitle: String
  ├── quantity: Number
  ├── totalAmount: Number
  ├── status: "placed" | "confirmed" | "ready" | "delivered" | "cancelled"
  ├── deliveryQrCode: String?
  ├── placedAt: Timestamp
  ├── updatedAt: Timestamp
  └── timeline: Array<{ status, timestamp, actorId }>
```

### `transactions` collection
```
transactions/{transactionId}
  ├── senderId: String
  ├── recipientId: String
  ├── amount: Number
  ├── type: "transfer" | "topup" | "voucher" | "airtime"
  ├── reference: String
  ├── createdAt: Timestamp
  └── note: String?
```

### `chats` collection
```
chats/{chatId}
  ├── participants: Array<String>   (buyerId, sellerId)
  ├── orderId: String?
  ├── lastMessage: String
  ├── updatedAt: Timestamp
  └── messages/{messageId}
        ├── senderId: String
        ├── text: String
        └── createdAt: Timestamp
```

**Design decisions:** We denormalized `sellerName` and `productTitle` into the orders collection to avoid extra reads when displaying order history. Wallet balance is stored directly on the user document rather than in a separate collection because it is always fetched as part of the user profile. All balance-modifying operations use Firestore transactions to prevent race conditions.

---

## 📂 Project Structure

```
app/src/main/java/com/market/astu/
│
├── data/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Product.kt
│   │   ├── Order.kt
│   │   ├── Transaction.kt
│   │   ├── Wallet.kt
│   │   └── Message.kt
│   │
│   └── repository/
│       ├── AuthRepository.kt
│       ├── AuthRepositoryImpl.kt
│       ├── ProductRepository.kt
│       ├── ProductRepositoryImpl.kt
│       ├── OrderRepository.kt
│       ├── OrderRepositoryImpl.kt
│       ├── WalletRepository.kt
│       ├── WalletRepositoryImpl.kt
│       └── UserRepository.kt
│
├── domain/
│   └── usecase/
│       ├── auth/
│       │   ├── SignInUseCase.kt
│       │   ├── SignUpUseCase.kt
│       │   └── SignOutUseCase.kt
│       ├── product/
│       │   ├── GetProductsUseCase.kt
│       │   ├── GetProductsByCategoryUseCase.kt
│       │   ├── CreateProductUseCase.kt
│       │   └── UpdateProductUseCase.kt
│       ├── order/
│       │   ├── PlaceOrderUseCase.kt
│       │   ├── UpdateOrderStatusUseCase.kt
│       │   └── GetOrdersUseCase.kt
│       └── wallet/
│           ├── TransferFundsUseCase.kt
│           ├── RedeemVoucherUseCase.kt
│           └── GetTransactionHistoryUseCase.kt
│
├── di/
│   ├── AppModule.kt
│   ├── FirebaseModule.kt
│   └── RepositoryModule.kt
│
├── ui/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   ├── Screen.kt
│   │   └── AuthenticatedApp.kt
│   │
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── SignInScreen.kt
│   │   │   ├── SignUpScreen.kt
│   │   │   └── AuthViewModel.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── FeaturedProductsSection.kt
│   │   ├── product/
│   │   │   ├── ProductListScreen.kt
│   │   │   ├── ProductDetailScreen.kt
│   │   │   ├── CreateListingScreen.kt
│   │   │   └── ProductViewModel.kt
│   │   ├── cart/
│   │   │   ├── CartScreen.kt
│   │   │   ├── CartViewModel.kt
│   │   │   └── CheckoutScreen.kt
│   │   ├── orders/
│   │   │   ├── OrderListScreen.kt
│   │   │   ├── OrderDetailScreen.kt
│   │   │   └── OrderViewModel.kt
│   │   ├── wallet/
│   │   │   ├── WalletScreen.kt
│   │   │   ├── WalletViewModel.kt
│   │   │   ├── TransferScreen.kt
│   │   │   └── TransactionHistoryScreen.kt
│   │   ├── scanner/
│   │   │   ├── QrScannerScreen.kt
│   │   │   └── QrGeneratorScreen.kt
│   │   ├── seller/
│   │   │   ├── SellerDashboardScreen.kt
│   │   │   ├── InventoryScreen.kt
│   │   │   └── SellerViewModel.kt
│   │   └── profile/
│   │       ├── ProfileScreen.kt
│   │       └── ProfileViewModel.kt
│   │
│   ├── common/
│   │   ├── TopBar.kt
│   │   ├── BottomDock.kt
│   │   ├── Backdrop.kt
│   │   ├── ProductCard.kt
│   │   ├── OrderCard.kt
│   │   ├── LoadingIndicator.kt
│   │   ├── EmptyState.kt
│   │   └── ErrorState.kt
│   │
│   └── theme/
│       ├── Color.kt
│       ├── Typography.kt
│       ├── Theme.kt
│       └── Shape.kt
│
└── util/
    ├── PricingUtils.kt
    ├── ValidationUtils.kt
    ├── FormattingUtils.kt
    ├── QrUtils.kt
    └── NotificationUtils.kt
```

---

## 🧩 Module Breakdown

### Data Models (`data/model/`)

Each data model is a Kotlin `data class` that serves as both the domain model and the DTO for Firestore serialization. We use Firestore's `toObject()` and `set()` with property mapping rather than custom serializers to keep things straightforward.

Key modeling decisions:
- `Order` contains a `timeline` list that records every status change with its timestamp and the ID of the user who triggered it — this gives us a complete audit trail without a separate collection
- `Wallet` is not a separate document; it is embedded as fields on the `User` document to reduce read operations on the most frequently accessed data
- `Product.imageUrls` is a list of Firebase Storage download URLs, not Storage paths — this avoids an extra resolution step on every product page load

### Repositories (`data/repository/`)

Each repository follows the same pattern:
- An interface defining the contract (return types are `Flow<Result<T>>` for observable queries, `Result<T>` for one-shot operations)
- A concrete implementation backed by Firestore
- Error handling that wraps Firebase exceptions into typed `Result.Failure` objects

The repository layer is the only place in the app that knows Firebase exists. Use cases and ViewModels interact only with the repository interfaces.

### Dependency Injection (`di/`)

Three Hilt modules:

`AppModule` provides application-level singletons that do not depend on Firebase — coroutine dispatchers, shared preferences, and utility classes.

`FirebaseModule` provides Firebase instances — `FirebaseAuth`, `FirebaseFirestore`, and `FirebaseStorage` — as singletons. These are thread-safe and meant to be shared across the app.

`RepositoryModule` binds repository interfaces to their implementations using `@Binds` for maximum efficiency (no wrapper object creation at runtime).

### UI Screens (`ui/screens/`)

Each screen follows a consistent pattern:

1. **Screen composable** — accepts a `NavController` and a `ViewModel` (provided by Hilt)
2. **ViewModel** — exposes a `StateFlow<ScreenUiState>` and one or more event handler functions
3. **UI State** — a sealed class or data class representing every possible state the screen can be in: `Loading`, `Success(data)`, `Error(message)`

This pattern ensures the UI never needs to make decisions about data — all logic lives in the ViewModel, and the composable is a pure function of state.

### Common Components (`ui/common/`)

Shared UI components are extracted here to avoid duplication. The most important ones:

`ProductCard` — used in category screens, search results, and the home featured section. Accepts a `Product` and an `onClick` lambda.

`OrderCard` — used in both buyer and seller order lists. The same component renders differently depending on a `role` parameter passed to it.

`BottomDock` — the main navigation bar. Its items and their icons change depending on the active role.

`Backdrop` — a custom sheet component used for filter panels, confirmation dialogs, and the cart preview. We built this ourselves rather than using `ModalBottomSheet` because we needed finer control over the animation and dismissal behavior.

---

## 🗺 Development Roadmap

We split development into six parallel tracks so team members could work independently without blocking each other. Each part had a designated lead, a shared interface contract, and integration checkpoints every two weeks.

| Part | Focus | Lead | Status |
|---|---|---|---|
| Part 1 | Backend & Data | - | ✅ Complete |
| Part 2 | Auth & Onboarding | - | ✅ Complete |
| Part 3 | Home & Discovery | - | ✅ Complete |
| Part 4 | Wallet & Payments | - | ✅ Complete |
| Part 5 | Cart & Orders | - | ✅ Complete |
| Part 6 | Seller & Common | - | ✅ Complete |

### Part 1 — Backend and Data

Firestore schema design and implementation, security rules, repository interfaces and implementations, data model classes, and Hilt module setup. This part set the foundation that all other parts depended on. The most important deliverable was the Firestore security rules document, which defined access control at the data level.

### Part 2 — Authentication and Onboarding

Sign-in, sign-up, email verification, profile creation, and role selection flows. This part also owns the `AuthRepository` and the session management logic that determines whether the user sees the auth flow or the main app on launch.

### Part 3 — Home and Discovery

Home screen, category browsing, product listings, search, and product detail pages. This was the most user-facing part during early testing and received the most design iteration. The featured products algorithm (sorting by recency within each category, weighted by seller rating) was designed and implemented here.

### Part 4 — Wallet and Payments

Wallet screen, balance display, peer-to-peer transfer flow, QR code generation and scanning for payments, voucher redemption, airtime purchase, and transaction history. This part required the most coordination with Part 1 because wallet operations need atomic Firestore transactions, which had to be designed carefully to avoid race conditions.

### Part 5 — Cart and Orders

Shopping cart, checkout flow, order placement, order list (buyer and seller views), order detail screen with timeline, and the delivery QR code generation and verification flow. This part also owns the push notification integration for order status changes.

### Part 6 — Seller Tools and Common Components

Seller dashboard, inventory management, listing creation and editing, shared UI components (TopBar, BottomDock, ProductCard, OrderCard, EmptyState, etc.), and the application theme. This part was also responsible for the final integration pass — pulling all six parts together into a coherent navigation graph.

---

## ⚙️ Getting Started

### Prerequisites

- Android Studio **Ladybug** (2024.2.1) or newer
- JDK 17 or higher
- Android SDK with API level 34 (target) and API level 24 (minimum)
- A Firebase project with Email/Password Authentication and Cloud Firestore enabled
- A physical Android device or emulator running Android 7.0 (Nougat) or higher

The app targets **Min SDK 24** to cover approximately 98% of active Android devices while still being able to use modern Compose APIs.

### Firebase Setup

**Step 1:** Go to the [Firebase Console](https://console.firebase.google.com) and create a new project. Name it `astu-market` or similar.

**Step 2:** In the Firebase Console, add an Android app to the project. Use the package name `com.market.astu`. Firebase will generate a `google-services.json` file for you.

**Step 3:** Download `google-services.json` and place it in the `app/` directory of the project. This file must not be committed to version control (it is already in `.gitignore`).

**Step 4:** In the Firebase Console:
- Enable **Email/Password** authentication under Authentication → Sign-in method
- Create a **Cloud Firestore** database. Start in test mode for development; apply the security rules from `firestore.rules` before moving to production.
- Enable **Firebase Storage** for product image uploads.
- Set up **Firebase Cloud Messaging** — no additional configuration is needed beyond the `google-services.json`.

**Step 5:** Apply the Firestore security rules. The `firestore.rules` file in the repository root contains the complete rule set. Deploy it using the Firebase CLI:

```bash
firebase deploy --only firestore:rules
```

### Build and Run

```bash
# Clone the repository
git clone https://github.com/ASTU-Software-Group-Projects/ASTU-Market.git
cd ASTU-Market

# Open in Android Studio and let Gradle sync
# Then run on device or emulator:
./gradlew installDebug
```

For a production release build:

```bash
./gradlew assembleRelease
```

Note: The release build requires a signing keystore. See `app/build.gradle` for the signing configuration placeholders. Store your keystore credentials in `local.properties` or as environment variables — never in the repository.

### First-Time Setup

On first launch, the app checks for an authenticated Firebase session. If none exists, it routes to the sign-in screen. During development, you can create a test account through the app's registration flow. For testing with pre-loaded data, a Firestore data seeding script is available in `scripts/seed_data.py`.

---

## 📸 QR Scanning System

QR codes are central to three distinct flows in ASTU Market. We use **ZXing-embedded** for QR decoding and **CameraX** for camera management.

### Payment Transfer QR

Each user has a permanent wallet QR code that encodes their user ID. This code is displayed on the wallet screen and can be shared externally (as an image) for peer-to-peer payments.

QR content format:
```
astu-market://transfer?uid={userId}&display={displayName}
```

When a buyer scans a seller's wallet QR:
1. The scanner decodes the QR and parses the `uid` parameter
2. The app opens the transfer confirmation screen pre-filled with the seller's display name
3. The buyer enters the amount and confirms
4. `TransferFundsUseCase` executes a Firestore transaction: debit buyer, credit seller, write transaction records for both

### Voucher Redemption QR

Campus vouchers are encoded QR codes issued by university administration or generated by the app for promotional purposes.

QR content format:
```
astu-market://voucher?code={voucherCode}&amount={amount}&expiry={timestamp}
```

When scanned, `RedeemVoucherUseCase` validates the code against the `vouchers` Firestore collection, checks expiry, marks it as used (atomic operation), and credits the user's wallet.

### Delivery Verification QR

When a seller marks an order as "Ready for Pickup," the system generates a one-time delivery QR code for that order.

QR content format:
```
astu-market://delivery?orderId={orderId}&secret={hmac}
```

The `secret` is an HMAC-SHA256 signature of the order ID using a server-side key. This prevents buyers from fabricating delivery QR codes. When scanned:
1. The app sends the `orderId` and `secret` to a Firebase Cloud Function
2. The function validates the HMAC, checks that the order is in "ready" state, and updates it to "delivered"
3. Both buyer and seller receive a completion notification

### Camera Implementation

We use CameraX's `ImageAnalysis` use case rather than capturing full photos, which keeps memory usage low and scan latency minimal. The analyzer runs ZXing on every frame in a background thread and reports results on the main thread via a callback. The scanner screen shows a live viewfinder with a centered scanning region overlay and an animated scan line.

---

## 💰 Wallet and Payments

### Atomicity and Consistency

All wallet balance mutations use Firestore transactions, which are atomic and serialized. A peer-to-peer transfer involves:

1. Read both user documents (buyer and seller)
2. Validate that buyer balance ≥ transfer amount
3. Write updated balance to buyer document (debit)
4. Write updated balance to seller document (credit)
5. Write transaction records for both parties

If any step fails, the entire transaction is rolled back. The UI reflects the outcome via `Result.Success` or `Result.Failure` from `TransferFundsUseCase`.

### Wallet Top-Up

In the current version, wallet top-up is handled offline — a student contacts a campus wallet administrator who adds funds directly to the user's Firestore document. We designed the wallet model to support future integration with a real payment gateway (e.g., Telebirr or CBE Birr) — the top-up flow is stubbed and the data model includes a `topupReference` field for external transaction IDs.

### Balance Visibility

Wallet balance is only visible to the authenticated owner. Firestore security rules enforce:

```
allow read: if request.auth.uid == userId;
allow write: if false; // all writes through Cloud Functions
```

All production wallet writes go through Cloud Functions rather than direct Firestore writes, which allows us to enforce business rules (minimum transfer amount, daily limits) server-side without trusting the client.

---

## 🔒 Security Model

### Authentication

Firebase Authentication manages all credential handling. Passwords are never stored in Firestore — only Firebase Auth UIDs are referenced. Sessions are managed by Firebase's SDK and persist across app restarts.

### Firestore Security Rules

Our rules follow the principle of least privilege:
- Users can only read and write their own documents
- Products can be read by any authenticated user but only written by the product's `sellerId`
- Orders can be read by the `buyerId` or `sellerId` on that order, and status updates are validated by role
- Wallet operations go through Cloud Functions only — direct client writes to balance fields are rejected

### Data Validation

Client-side validation (using `ValidationUtils`) catches obvious errors before they reach Firestore. Server-side validation in Cloud Functions is the authoritative check — we never trust client-sent data for financial operations.

### QR Code Security

Payment QR codes encode only user IDs, not balances or keys. Delivery QR codes use HMAC signatures validated server-side. This means even if a QR code is intercepted or screenshotted, it cannot be used to withdraw funds or fake deliveries without also compromising the server-side key.

---

## 🧪 Testing Strategy

### Unit Tests

Unit tests cover all use cases in the domain layer and all utility functions. We mock repository interfaces using Mockito and test use cases in isolation. Test coverage targets:

- All happy paths for each use case
- All documented failure modes (insufficient balance, product not found, invalid QR, etc.)
- Edge cases in validation and formatting utilities

Test location: `app/src/test/`

### Integration Tests

Integration tests cover repository implementations against a Firestore emulator. The Firebase Local Emulator Suite runs Firestore, Auth, and Functions locally, allowing us to test the full data layer without touching production.

To run integration tests:

```bash
# Start the Firebase emulator suite
firebase emulators:start

# Run integration tests
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.market.astu.integration
```

### UI Tests

We use Jetpack Compose's `ComposeTestRule` for UI tests on critical flows: sign-in, product browsing, cart and checkout, and wallet transfer. These tests run on an emulator via `./gradlew connectedAndroidTest`.

### Manual Test Checklist

Before each release, we run through a manual test checklist covering:
- [ ] Register a new account, verify email, complete onboarding
- [ ] Switch between buyer and seller roles
- [ ] Create a product listing with photos
- [ ] Complete a full purchase cycle end-to-end
- [ ] Execute a wallet transfer via QR code
- [ ] Redeem a test voucher QR code
- [ ] Complete a delivery with QR verification
- [ ] Test on both light and dark mode
- [ ] Test on minimum supported SDK (API 24)

---

## ⚠️ Known Limitations and Future Work

### Current Limitations

**No real payment gateway integration:** Wallet top-up currently requires manual administrator action. We designed for eventual integration with Ethiopian payment APIs (Telebirr, CBE Birr) but this was out of scope for the initial release.

**No in-app chat notifications when app is in foreground:** FCM notifications are currently only shown when the app is in the background. In-foreground message indicators require an additional listener that is partially implemented.

**Search is limited to Firestore's native querying:** Full-text search across product descriptions requires Algolia or a similar dedicated search service, which would add cost and complexity. The current search covers titles only.

**Single image upload per order:** The delivery verification system currently supports one photo attachment per order for documentation purposes. Multiple photos are planned.

**No dispute resolution system:** If a buyer claims a product was not delivered or was not as described, there is currently no in-app dispute flow. Disputes are handled offline via campus administration.

### Planned Enhancements

**v2.0 — Payment Integration**
- Telebirr and CBE Birr integration for wallet top-up
- Automated cashout for sellers to bank accounts
- Transaction limits configurable by university administration

**v2.1 — Discovery Improvements**
- Algolia-powered full-text search across titles and descriptions
- Seller ratings and review system
- Promoted listings for registered campus vendors

**v2.2 — Social Features**
- Follow sellers to see new listings in a personalized feed
- Share product listings via deep links
- In-app referral system for new user onboarding

**v2.3 — Operational Tools**
- University administration panel for voucher issuance and wallet oversight
- Seller analytics dashboard with sales trends and top products
- Buyer spending analysis and budgeting tools

**v3.0 — Campus Integration**
- SSO integration with the ASTU student information system
- Department-specific product categories
- Integration with campus delivery staff for managed delivery

---

## 🤝 Contributing

ASTU Market is a collaborative project. If you are an ASTU student or faculty member interested in contributing:

1. **Fork** the repository to your own GitHub account
2. **Create a feature branch** from `main`: `git checkout -b feature/your-feature-name`
3. **Follow the code style** — we use ktlint with the default Kotlin code style; run `./gradlew ktlintCheck` before submitting
4. **Write tests** for any new use cases or utility functions
5. **Open a pull request** with a clear description of what changes you made and why

For larger contributions, please open an issue first to discuss the approach with the team before starting implementation.

### Code Review Process

All pull requests require at least one approving review from a team lead before merge. We review for:
- Correctness of business logic
- Adherence to the layered architecture
- Firestore security implications of any data model changes
- UI consistency with the existing design system

---

## 👥 Team

Built by students of **Adama Science and Technology University**.

Each member owned a complete development track from planning and implementation to testing and integration.

| Member | Track | Responsibilities |
|---|---|---|
| Team Lead | Architecture & Integration | System design, Hilt setup, final integration, CI/CD |
| Member 2 | Backend & Data | Firestore schema, security rules, repositories, data models |
| Member 3 | Auth & Onboarding | Firebase Auth, sign-in/up flows, session management |
| Member 4 | Home & Discovery | Home screen, categories, search, product detail |
| Member 5 | Wallet & Payments | Wallet UI, QR transfer, vouchers, transaction history |
| Member 6 | Cart, Orders & Seller Tools | Cart, checkout, order lifecycle, seller dashboard, inventory, shared UI |


---
