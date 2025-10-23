package com.smartalarm.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import com.smartalarm.data.TodoEntity
import com.smartalarm.settings.PreferredTtsEngine
import com.smartalarm.ui.theme.BlackBackground

@Composable
fun SmartAlarmApp(
    screenDimManager: ScreenDimManager,
    burnInPreventionManager: BurnInPreventionManager
) {
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val todoViewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory)
    val clockViewModel: ClockViewModel = viewModel(factory = ClockViewModel.Factory)
    val alarmViewModel: AlarmSetupViewModel = viewModel(factory = AlarmSetupViewModel.Factory)

    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val todoState by todoViewModel.uiState.collectAsStateWithLifecycle()
    val clockState by clockViewModel.uiState.collectAsStateWithLifecycle()
    val alarmState by alarmViewModel.uiState.collectAsStateWithLifecycle()
    val isDimmed by screenDimManager.isDimmed.collectAsStateWithLifecycle()
    val burnInOffset by burnInPreventionManager.offset.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val handleInteraction = { screenDimManager.resetTimer() }

    LaunchedEffect(settingsState) {
        screenDimManager.updateConfiguration(
            dimDelaySeconds = settingsState.dimTimeoutSeconds,
            active = settingsState.activeBrightness,
            dim = settingsState.dimBrightness
        )
    }

    LaunchedEffect(settingsState.preferredTtsEngine) {
        todoViewModel.updatePreferredEngine(settingsState.preferredTtsEngine)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        ClockScreen(
            clockState = clockState,
            alarmState = alarmState,
            todoState = todoState,
            canScheduleExactAlarms = alarmViewModel.canScheduleExactAlarms(),
            isDimmed = isDimmed,
            burnInOffset = burnInOffset,
            ringing = null,
            onAlarmTimeChanged = { hour, minute ->
                handleInteraction()
                alarmViewModel.updateTime(hour, minute)
            },
            onAlarmLabelChanged = {
                handleInteraction()
                alarmViewModel.updateLabel(it)
            },
            onSaveAlarm = {
                handleInteraction()
                if (alarmViewModel.canScheduleExactAlarms()) {
                    alarmViewModel.scheduleAlarm()
                }
            },
            onCancelAlarm = {
                handleInteraction()
                alarmViewModel.cancelAlarm()
            },
            onOpenAlarmSettings = {
                handleInteraction()
                val intent = Intent(alarmViewModel.openExactAlarmSettingsIntent())
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            onTodoDraftChanged = {
                handleInteraction()
                todoViewModel.updateDraft(it)
            },
            onAddTodo = {
                if (todoState.newTodoText.isNotBlank()) {
                    handleInteraction()
                    todoViewModel.addTodo()
                }
            },
            onToggleTodo = { todo: TodoEntity, completed: Boolean ->
                handleInteraction()
                todoViewModel.toggleCompleted(todo.id, completed)
            },
            onDeleteTodo = { todo: TodoEntity ->
                handleInteraction()
                todoViewModel.deleteTodo(todo)
            },
            onReadTodos = {
                handleInteraction()
                todoViewModel.readTodos()
            },
            onStopReading = {
                handleInteraction()
                todoViewModel.stopReading()
            },
            onSnooze = {
                handleInteraction()
                // TODO: integrate with SnoozeManager when ringing overlay is wired up
            },
            onDismiss = {
                handleInteraction()
                // TODO: integrate with alarm dismissal when overlay is wired up
            },
            onUserInteraction = handleInteraction
        )
    }
}
