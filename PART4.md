# Part 4 – Wallet, Send/Receive, Airtime & Voucher UI

## Scope

Every screen and ViewModel inside the **Wallet tab**, plus a separate **voucher scanning screen** accessible from the quick‑actions menu.

## Files you maintain

- `ui/screens/wallet/WalletScreen.kt`
- `ui/screens/wallet/WalletViewModel.kt`
- `ui/WalletBalanceViewModel.kt` – used by `AuthenticatedApp` for the top‑bar balance label
- `ui/screens/wallet/SendMoneyDialog.kt`
- `ui/screens/wallet/ReceiveMoneySheet.kt`
- `ui/screens/wallet/VoucherScanScreen.kt`
- `ui/screens/wallet/VoucherScanViewModel.kt`
- `util/CampusFormatting.kt` (if not yet placed by Part 1)

## Public API (for other parts)

### WalletBalanceViewModel
    data class WalletBalanceUiState(val balance: Double, val isLoading: Boolean)
    fun refresh()

This ViewModel is injected in `AuthenticatedApp` (Part 6) to obtain the current wallet balance. Even though the top bar no longer shows the balance, it is still used internally to refresh the wallet state after operations like checkout or voucher redemption.

## Internal behaviour

### WalletViewModel
- `uiState: StateFlow<WalletUiState>` containing:
  - `wallet: Wallet`
  - `transactions: List<WalletTransaction>`
  - `isLoading`, `isProcessingTopUp`, `isSendingMoney`, `errorMessage`
- `loadWallet()` – calls `WalletRepository.getWallet()` and `WalletRepository.getTransactions()`.
- `sendMoneyByPhone(phone: String, amount: Double)` – calls `WalletRepository.findUserByPhone(phone)`. If found, calls `WalletRepository.sendMoney(toUid, amount)`. If phone lookup fails, sets an error.
- `sendMoneyByUid(uid: String, amount: Double)` – directly calls `WalletRepository.sendMoney(uid, amount)`.
- `buyAirtime(phoneNumber: String, amount: Double)` – calls `WalletRepository.buyAirtime(phoneNumber, amount)`.
- `generateReceiveQrCode(): String` – returns a string like `"astu_wallet:receive:<uid>"`, where `<uid>` is the current user’s UID.
- `userPhone: StateFlow<String?>` – loads the user’s own phone number from `UserRepository` for the airtime screen.

### WalletScreen
- Top bar using `CommerceTopBar` (from Part 6).
- Hero card showing balance and monthly yield, formatted with `formatCampus()`.
- **Airtime recharge card**:
  - Two filter chips: “My number” / “Other number”.
  - If “Other number” is selected, an `OutlinedTextField` for the phone number appears.
  - If “My number” is selected but the user has no phone on file, an error message is shown.
  - Amount input field (decimal keyboard).
  - “Buy airtime” button – enabled only when the phone number is valid and amount > 0. When pressed, calls `viewModel.buyAirtime(phone, amount)`.
- **Send / Receive buttons**:
  - “Send” opens `SendMoneyDialog`.
  - “Receive” opens `ReceiveMoneySheet`.
- Transaction history list:
  - Each transaction shows its type (e.g. “Send”, “Receive”, “Airtime”) as a title, a description, a date, and the amount. Positive amounts are coloured green, negative amounts red.

### SendMoneyDialog
- Title: “Send CAMPUS”.
- Toggle between “Phone” and “ID” using two `FilterChip` components.
- If “Phone” is selected, a text field for the phone number (phone keyboard). If “ID”, a text field for the user ID.
- A text field for the amount (decimal keyboard).
- “Send” button enabled only when amount > 0 and the phone/id field is not blank.
- On confirm:
  - If phone mode: calls `viewModel.sendMoneyByPhone(phone, amount)`.
  - If ID mode: calls `viewModel.sendMoneyByUid(uid, amount)`.
- “Cancel” button dismisses the dialog.

### ReceiveMoneySheet
- A `ModalBottomSheet` with the title “Receive CAMPUS”.
- Generates a QR code bitmap using `BarcodeEncoder` (from zxing‑embedded) encoding the receive code returned by `viewModel.generateReceiveQrCode()`.
- If the QR bitmap generation fails, a text message is shown instead.
- Displays the receive code as text.
- “Share” button: creates an `Intent.ACTION_SEND` with the text “Send me CAMPUS: <code>” and launches the chooser.
- “Close” button dismisses the sheet.

### VoucherScanScreen & VoucherScanViewModel
- `VoucherScanViewModel`:
  - `redeemVoucher(code: String)`: calls `WalletRepository.topUpFromVoucher(code)`, emits `VoucherScanEvent.Success` or `VoucherScanEvent.Error` events.
- `VoucherScanScreen`:
  - Immediately launches the QR scanner using `ScanContract` (zxing‑embedded) with options for QR codes, no beep, orientation locked.
  - On scan result:
    - If the result contains a code, calls `viewModel.redeemVoucher(result.contents)`.
    - If the user cancels, calls `onBack()`.
  - Shows a loading indicator while the voucher is being redeemed.
  - On success, shows a snackbar “Voucher redeemed! Amount added to wallet.” and triggers `onVoucherRedeemed()` which pops back and refreshes the wallet balance.
  - On error, shows a snackbar with the error message.

## Integration with Part 1
- Injects `WalletRepository` for all wallet operations.
- Injects `UserRepository` to fetch the current user’s phone number.

## Dependencies
- Part 1 (repositories must be complete).
- Part 6 (common composables: `CommerceTopBar`, `CommerceBackdrop`, `CommerceMetricPill`, `CommerceSectionHeader`, `LoadingStatePane`, `MessageStatePane`).
- The project already includes the zxing‑embedded library for QR scanning.