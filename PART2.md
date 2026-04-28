# Part 2 – Auth & Onboarding UI

## Scope

Sign‑in screen, sign‑up screen, auth state management, and the app’s **root navigation** (auth vs  
main flow).

## Files you maintain

- `ui/screens/auth/AuthViewModel.kt`
- `ui/screens/auth/SignInScreen.kt`
- `ui/screens/auth/SignUpScreen.kt`
- `ui/navigation/NavGraph.kt`
- `util/AuthFormValidator.kt` (optional, can be here or in Part 6)

## Public API (what other parts will use)

None. This part owns the root navigation. After a successful login, the rest of the app is  
displayed via `AuthenticatedApp` (owned by Part 6).

## Internal behaviour

### AuthViewModel
- Holds `authState: StateFlow<AuthState>` where `AuthState` is a sealed class:
  - `CheckingSession`
  - `Authenticated`
  - `Unauthenticated`
- On init, checks if a Firebase user is already signed in and emits `Authenticated` immediately  
  if so.
- `signIn(email, password)` – calls `AuthRepository.signInWithEmail` and updates state.
- `signUp(registration)` – calls `AuthRepository.signUpWithEmail` and updates state.
- `signOut()` – calls `AuthRepository.signOut()`.

### SignInScreen
- Two text fields (email, password), a “Sign In” button, and a link to navigate to SignUp.
- Validates input using `AuthFormValidator`.

### SignUpScreen
- Collects:
  - email, password, display name
  - phone number, address
  - role (BUYER / SELLER / RUNNER)
  - optional store name and bio
- Builds a `UserRegistration` object and calls `viewModel.signUp(registration)`.
- Validation is done before submission.

### NavGraph
- Observes `authState` from `AuthViewModel`.
- `CheckingSession` → shows a loading indicator.
- `Unauthenticated` → shows a `NavHost` with `SignIn` and `SignUp` destinations.
- `Authenticated` → directly shows `AuthenticatedApp` (from Part 6).

## Integration with Part 1
- Injects `AuthRepository`.
- Optionally injects `UserRepository` if you need to fetch profile data after sign‑in (not  
  required, but can be used for a “Welcome” message).

## Dependencies
- Part 1 (repositories).
- Part 6 (common composables like `LoadingStatePane`), but you can use basic Compose until  
  Part 6 is ready.