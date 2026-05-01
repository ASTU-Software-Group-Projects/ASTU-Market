package com.market.astu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.market.astu.data.model.ThemeMode
import com.market.astu.ui.AppViewModel
import com.market.astu.ui.navigation.AppNavGraph
import com.market.astu.ui.theme.ASTUMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()

            ASTUMarketTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    themeMode = themeMode,
                    onToggleTheme = appViewModel::toggleThemeMode,
                    onThemeModeSelected = appViewModel::setThemeMode
                )
            }
        }
    }
}
