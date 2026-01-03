package com.patrolshield.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val SYNC_OVER_WIFI_ONLY_KEY = booleanPreferencesKey("sync_over_wifi_only")
    private val GPS_INTERVAL_KEY = stringPreferencesKey("gps_interval")

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val syncOverWifiOnlyFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNC_OVER_WIFI_ONLY_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setSyncOverWifiOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_OVER_WIFI_ONLY_KEY] = enabled
        }
    }
}
