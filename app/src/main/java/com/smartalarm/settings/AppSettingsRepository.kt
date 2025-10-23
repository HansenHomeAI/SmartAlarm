package com.smartalarm.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(private val context: Context) {

    private val dataStore = context.settingsDataStore

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        prefs.toSettings()
    }

    suspend fun setDimTimeoutSeconds(seconds: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.dimTimeoutSeconds] = seconds
        }
    }

    suspend fun setPreferredTtsEngine(engine: PreferredTtsEngine) {
        dataStore.edit { prefs ->
            prefs[Keys.preferredTtsEngine] = engine.name
        }
    }

    suspend fun setActiveBrightness(value: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.activeBrightness] = value
        }
    }

    suspend fun setDimBrightness(value: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.dimBrightness] = value
        }
    }

    private object Keys {
        val dimTimeoutSeconds: Preferences.Key<Int> = intPreferencesKey("dim_timeout_seconds")
        val preferredTtsEngine: Preferences.Key<String> = stringPreferencesKey("preferred_tts_engine")
        val activeBrightness: Preferences.Key<Float> = floatPreferencesKey("active_brightness")
        val dimBrightness: Preferences.Key<Float> = floatPreferencesKey("dim_brightness")
    }

    private fun Preferences.toSettings(): AppSettings {
        val timeout = this[Keys.dimTimeoutSeconds] ?: AppSettings.DEFAULT_DIM_TIMEOUT_SECONDS
        val tts = this[Keys.preferredTtsEngine]?.let { runCatching { PreferredTtsEngine.valueOf(it) }.getOrNull() }
            ?: PreferredTtsEngine.Auto
        val active = this[Keys.activeBrightness] ?: AppSettings.DEFAULT_ACTIVE_BRIGHTNESS
        val dim = this[Keys.dimBrightness] ?: AppSettings.DEFAULT_DIM_BRIGHTNESS
        return AppSettings(
            dimTimeoutSeconds = timeout,
            activeBrightness = active,
            dimBrightness = dim,
            preferredTtsEngine = tts
        )
    }
}

data class AppSettings(
    val dimTimeoutSeconds: Int,
    val activeBrightness: Float,
    val dimBrightness: Float,
    val preferredTtsEngine: PreferredTtsEngine
) {
    companion object {
        const val DEFAULT_DIM_TIMEOUT_SECONDS = 10
        const val DEFAULT_ACTIVE_BRIGHTNESS = 0.3f
        const val DEFAULT_DIM_BRIGHTNESS = 0.02f
    }
}

enum class PreferredTtsEngine { Auto, SherpaOnly, AndroidOnly }
