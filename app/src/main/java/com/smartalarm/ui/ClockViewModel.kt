package com.smartalarm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.smartalarm.alarm.AlarmInfo
import com.smartalarm.alarm.AlarmRepository
import com.smartalarm.alarm.AlarmScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClockViewModel(
    application: Application,
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : AndroidViewModel(application) {

    private val currentTimeMillis: MutableStateFlow<Long> = MutableStateFlow(System.currentTimeMillis())
    private var tickerJob: Job? = null

    val uiState: StateFlow<ClockUiState> = combine(
        currentTimeMillis,
        alarmRepository.alarmFlow
    ) { now, alarm ->
        ClockUiState(
            currentTimeMillis = now,
            nextAlarm = alarm,
            canScheduleExactAlarms = alarmScheduler.canScheduleExactAlarms()
        )
    }.stateIn(viewModelScope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = ClockUiState())

    init {
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                currentTimeMillis.update { System.currentTimeMillis() }
                delay(1_000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AlarmRepository(application)
                val scheduler = AlarmScheduler(application, repository)
                ClockViewModel(application, repository, scheduler)
            }
        }
    }
}

data class ClockUiState(
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val nextAlarm: AlarmInfo? = null,
    val canScheduleExactAlarms: Boolean = true
)
