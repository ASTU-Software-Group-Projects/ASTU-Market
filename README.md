# ASTU Market – Campus Commerce App

A complete Android marketplace app built with Jetpack Compose, Firebase (Auth + Firestore), Hilt DI, and QR scanning (Zxing / CameraX).

Development is split into **six parts** that can be built in parallel. Each part has its own `PART*.md` file explaining the exact scope, public API (function signatures), and integration notes.

## Parts overview

| Part | Focus | Consumed by |
|------|-------|-------------|
| 1 | Backend & Data Layer | All other parts |
| 2 | Auth & Onboarding UI | None (uses Part 1) |
| 3 | Home, Discover, Product Detail UI | None (uses Part 1) |
| 4 | Wallet, Send/Receive, Airtime & Voucher UI | None (uses Part 1) |
| 5 | Cart, Orders & Delivery UI | None (uses Part 1) |
| 6 | Seller Inventory, Profile, Common Components & Theme | All UI parts |

## How to use these docs

- Each `PART*.md` file defines the exact responsibilities for one team member.
- The **Public API** sections list the functions / composables that other parts will call. These are the **contracts** between team members.
- Start with Part 1 – all UI parts depend on the repositories defined there.

## Part files

- [Part 1 – Backend & Data Layer](./PART1.md)
- [Part 2 – Auth & Onboarding UI](./PART2.md)
- [Part 3 – Home, Discover, Product Detail UI](./PART3.md)
- [Part 4 – Wallet, Send/Receive, Airtime & Voucher UI](./PART4.md)
- [Part 5 – Cart, Orders & Delivery UI](./PART5.md)
- [Part 6 – Seller Inventory, Profile, Common Components & Theme](./PART6.md)

## Build & run

1. Clone the repository.
2. Open the project in Android Studio.
3. Add your own `google-services.json` (Firebase project).
4. Sync Gradle and run on device/emulator (min SDK 24).

## Project structure (simplified)

    app/src/main/java/com/market/astu/
    ├── data/
    │   ├── model/          # All data classes (User, Product, Wallet, etc.)
    │   └── repository/     # Firestore & Auth repositories
    ├── di/                 # Hilt module
    ├── ui/
    │   ├── navigation/     # NavGraph, AuthenticatedApp, QuickActions
    │   ├── screens/
    │   │   ├── auth/       # SignIn, SignUp
    │   │   ├── cart/       # Shopping cart & checkout
    │   │   ├── delivery/   # Gig board, delivery verification
    │   │   ├── detail/     # Product detail
    │   │   ├── discover/   # Browse / search products
    │   │   ├── home/       # Home screen
    │   │   ├── orders/     # Order timeline
    │   │   ├── profile/    # User profile
    │   │   ├── seller/     # Seller inventory
    │   │   └── wallet/     # Wallet, send/receive, airtime, voucher scan
    │   ├── common/         # Shared Composables (TopBar, BottomDock, Backdrop...)
    │   └── theme/          # Color, Typography, Theme
    └── util/               # Helper functions (formatting, pricing, validation)

## Reference

- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Zxing-embedded (QR scanning)](https://github.com/journeyapps/zxing-android-embedded)