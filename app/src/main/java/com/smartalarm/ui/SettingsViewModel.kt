package com.smartalarm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.smartalarm.settings.AppSettings
import com.smartalarm.settings.AppSettingsRepository
import com.smartalarm.settings.PreferredTtsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val repository: AppSettingsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { settings ->
                _uiState.value = SettingsUiState(
                    dimTimeoutSeconds = settings.dimTimeoutSeconds,
                    activeBrightness = settings.activeBrightness,
                    dimBrightness = settings.dimBrightness,
                    preferredTtsEngine = settings.preferredTtsEngine
                )
            }
        }
    }

    fun setDimTimeout(seconds: Int) {
        viewModelScope.launch {
            repository.setDimTimeoutSeconds(seconds)
        }
    }

    fun setPreferredEngine(engine: PreferredTtsEngine) {
        viewModelScope.launch {
            repository.setPreferredTtsEngine(engine)
        }
    }

    fun setActiveBrightness(value: Float) {
        viewModelScope.launch {
            repository.setActiveBrightness(value)
        }
    }

    fun setDimBrightness(value: Float) {
        viewModelScope.launch {
            repository.setDimBrightness(value)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AppSettingsRepository(application)
                SettingsViewModel(application, repository)
            }
        }
    }
}

data class SettingsUiState(
    val dimTimeoutSeconds: Int = AppSettings.DEFAULT_DIM_TIMEOUT_SECONDS,
    val activeBrightness: Float = AppSettings.DEFAULT_ACTIVE_BRIGHTNESS,
    val dimBrightness: Float = AppSettings.DEFAULT_DIM_BRIGHTNESS,
    val preferredTtsEngine: PreferredTtsEngine = PreferredTtsEngine.Auto
)
