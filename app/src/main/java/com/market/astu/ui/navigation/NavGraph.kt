package com.market.astu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.common.LoadingStatePane
import com.market.astu.ui.screens.auth.AuthState
import com.market.astu.ui.screens.auth.AuthViewModel
import com.market.astu.ui.screens.auth.SignInScreen
import com.market.astu.ui.screens.auth.SignUpScreen

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    when (authState) {
        AuthState.CheckingSession -> {
            LoadingStatePane(message = "Checking your session...")
        }
        is AuthState.Authenticated -> {
            AuthenticatedApp(
                onSignOut = viewModel::signOut,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onThemeModeSelected = onThemeModeSelected
            )
        }
        else -> {
            NavHost(navController = navController, startDestination = Screen.SignIn.route) {
                composable(Screen.SignIn.route) {
                    SignInScreen(
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
                    )
                }
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateToSignIn = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
