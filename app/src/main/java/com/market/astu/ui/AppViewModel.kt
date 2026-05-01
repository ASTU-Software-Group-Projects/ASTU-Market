package com.market.astu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.ThemeMode
import com.market.astu.data.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = appSettingsRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM
    )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            appSettingsRepository.setThemeMode(themeMode)
        }
    }

    fun toggleThemeMode() {
        val nextMode = if (themeMode.value == ThemeMode.DARK) {
            ThemeMode.LIGHT
        } else {
            ThemeMode.DARK
        }
        setThemeMode(nextMode)
    }
}
