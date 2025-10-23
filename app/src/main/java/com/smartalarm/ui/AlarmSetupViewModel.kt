package com.smartalarm.ui

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.smartalarm.alarm.AlarmInfo
import com.smartalarm.alarm.AlarmRepository
import com.smartalarm.alarm.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmSetupViewModel(
    application: Application,
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AlarmSetupUiState())
    val uiState: StateFlow<AlarmSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = repository.currentAlarm()
            if (current != null) {
                _uiState.value = _uiState.value.copy(
                    selectedHour = current.triggerHour,
                    selectedMinute = current.triggerMinute,
                    label = current.label.orEmpty(),
                    isSnoozed = current.isSnoozed,
                    nextAlarm = current
                )
            }
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(selectedHour = hour, selectedMinute = minute)
    }

    fun updateLabel(label: String) {
        _uiState.value = _uiState.value.copy(label = label)
    }

    fun scheduleAlarm() {
        val state = _uiState.value
        val triggerAt = AlarmInfo.computeNextTriggerMillis(state.selectedHour, state.selectedMinute)
        viewModelScope.launch {
            runCatching {
                scheduler.scheduleAlarm(triggerAtMillis = triggerAt, label = state.label)
            }.onSuccess {
                _uiState.value = state.copy(nextAlarm = AlarmInfo(triggerAt, state.label, false))
            }.onFailure {
                _uiState.value = state.copy(errorMessage = it.message)
            }
        }
    }

    fun cancelAlarm() {
        viewModelScope.launch {
            scheduler.cancelAlarm()
            _uiState.value = _uiState.value.copy(nextAlarm = null)
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun canScheduleExactAlarms(): Boolean = scheduler.canScheduleExactAlarms()

    fun openExactAlarmSettingsIntent() = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AlarmRepository(application)
                val scheduler = AlarmScheduler(application, repository)
                AlarmSetupViewModel(application, repository, scheduler)
            }
        }
    }
}

data class AlarmSetupUiState(
    val selectedHour: Int = 6,
    val selectedMinute: Int = 0,
    val label: String = "",
    val isSnoozed: Boolean = false,
    val nextAlarm: AlarmInfo? = null,
    val errorMessage: String? = null
)

private val AlarmInfo.triggerHour: Int
    get() = java.time.Instant.ofEpochMilli(triggerAtMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .hour

private val AlarmInfo.triggerMinute: Int
    get() = java.time.Instant.ofEpochMilli(triggerAtMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .minute
