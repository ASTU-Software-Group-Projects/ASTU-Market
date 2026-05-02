# 🛒 ASTU Market – Campus Commerce App


**ASTU Market** is a comprehensive Android marketplace designed specifically for the Adama Science and Technology University (ASTU) community. It empowers students to buy and sell goods within the campus ecosystem, featuring a secure wallet, role-based interfaces, and integrated delivery tracking.

---

## 🚀 Key Features

### 👥 Role-Based Experience
- **Buyer Role**: Browse products, manage a shopping cart, and track orders.
- **Seller Role**: Manage inventory, track sales, and fulfill orders.
- **Switching**: Users can easily transition between roles depending on their needs.

### 🏠 Dynamic Dashboard
- **Welcome Page**: Personalized greeting and quick access to active orders or featured products.
- **Quick Actions**: Shortcuts to wallet, scanning, and top categories.

### 🔍 Product Discovery
- **Categorized Browsing**: Filter by Clothes, Electronics, Books, Stationery, and more.
- **Advanced Search**: Find exactly what you need with real-time filtering.
- **Product Details**: Rich descriptions, pricing, and seller information.

### 💳 Secure Campus Wallet
- **Balance Tracking**: Real-time view of your current balance.
- **Peer-to-Peer Transfers**: Send money to other students instantly using QR codes.
- **Airtime & Vouchers**: Purchase airtime or redeem campus vouchers directly within the app.

### 📦 Order & Delivery Management
- **Tracking**: Real-time status updates for your purchases.
- **Seller Inventory**: Sellers can add, edit, or remove products from their shop.
- **Delivery Verification**: QR-based verification to ensure safe and successful handoffs.

---

## 🛠 Tech Stack

- **Core**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI)
- **Dependency Injection**: [Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Backend**: [Firebase](https://firebase.google.com/) (Authentication & Cloud Firestore)
- **QR Scanning**: [Zxing-embedded](https://github.com/journeyapps/zxing-android-embedded) / CameraX
- **Asynchronous Flow**: Kotlin Coroutines & Flow

---

## 📂 Project Structure

```text
app/src/main/java/com/market/astu/
├── data/
│   ├── model/          # Data classes (User, Product, Wallet, Order)
│   └── repository/     # Firestore & Auth repositories
├── di/                 # Hilt Modules for dependency injection
├── ui/
│   ├── navigation/     # NavGraph & AuthenticatedApp structure
│   ├── screens/        # Screen-specific Composables (Auth, Home, Wallet, etc.)
│   ├── common/         # Shared UI components (TopBar, BottomDock, Backdrop)
│   └── theme/          # Design system (Color, Typography, Theme)
└── util/               # Helpers (Pricing, Validation, Formatting)
```

---

## 🗺 Development Roadmap

The project is structured into six parallel parts to streamline development:

| Part | Focus | Description |
|------|-------|-------------|
| [Part 1](./PART1.md) | **Backend & Data** | Firestore schemas, Repositories, and Data Models. |
| [Part 2](./PART2.md) | **Auth & Onboarding** | Sign-in, Sign-up, and Role selection flow. |
| [Part 3](./PART3.md) | **Home & Discovery** | Product listings, Search, and Detail views. |
| [Part 4](./PART4.md) | **Wallet & Payments** | Balance, Transfers, and QR Voucher scanning. |
| [Part 5](./PART5.md) | **Cart & Orders** | Checkout process and Order timeline tracking. |
| [Part 6](./PART6.md) | **Seller & Common** | Inventory management and shared UI components. |

---

## 📱 Visual Gallery

### 🔐 Authentication & Setup
| Sign In | Seller: Post Product |
|:---:|:---:|
| ![Sign In](https://i.8upload.com/image/9bad2c4b887b824a/sign-in.jpg) | ![Seller Post](https://i.8upload.com/image/d741c55d25e27534/sellers-post-product.jpg) |

### 🏠 App Experience (Light vs. Dark)

| Feature | Light Mode | Dark Mode |
|:---:|:---:|:---:|
| **Dashboard** | ![Dashboard Light](https://i.8upload.com/image/98cdb06b8ea41684/dashboard.jpg) | ![Dashboard Dark](https://i.8upload.com/image/cacb22f58560269a/dashboard-dark.jpg) |
| **Discovery** | ![Discovery Light](https://i.8upload.com/image/80d85a2f69fb3f97/discovery.jpg) | ![Discovery Dark](https://i.8upload.com/image/d23384e5faa011d9/discovery-dark.jpg) |
| **Wallet** | ![Wallet Light](https://i.8upload.com/image/873b0637ecdfb735/wallet.jpg) | ![Wallet Dark](https://i.8upload.com/image/9731264c5221bcde/wallet-dark.jpg) |
| **Account** | ![Account Light](https://i.8upload.com/image/3340f5ae4147cc9f/account.jpg) | ![Account Dark](https://i.8upload.com/image/16b7bb429ee13775/account-dark.jpg) |

---

## ⚙️ Setup & Installation

1. **Clone the Repo**:
   ```bash
   git clone https://github.com/yourusername/ASTU-Market.git
   ```
2. **Firebase Configuration**:
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Enable **Email/Password Auth** and **Cloud Firestore**.
   - Download your `google-services.json` and place it in the `app/` directory.
3. **Build**:
   - Open in Android Studio (Ladybug or newer recommended).
   - Sync Gradle and run on a device/emulator (Min SDK 24).

---

## 📸 QR Scanning
The app uses **Zxing** for high-performance QR code processing. This is used for:
- Peer-to-peer wallet transfers.
- Redeeming airtime vouchers.
- Verifying order deliveries between buyers and sellers.

---

