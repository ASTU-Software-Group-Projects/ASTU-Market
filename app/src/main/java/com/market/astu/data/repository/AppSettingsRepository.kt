package com.market.astu.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.market.astu.data.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.appSettingsDataStore.data.map { preferences ->
        ThemeMode.fromStorage(preferences[themeModeKey])
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[themeModeKey] = themeMode.name
        }
    }
}
