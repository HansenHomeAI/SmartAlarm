package com.smartalarm.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember

private const val CLOCK_ROUTE = "clock"
private const val TODO_ROUTE = "todo"
private const val ALARM_ROUTE = "alarm"
private const val SETTINGS_ROUTE = "settings"

@Composable
fun SmartAlarmApp(
    screenDimManager: ScreenDimManager,
    burnInPreventionManager: BurnInPreventionManager,
    navController: NavHostController = rememberNavController()
) {
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val isDimmed by screenDimManager.isDimmed.collectAsStateWithLifecycle()
    val burnInOffset by burnInPreventionManager.offset.collectAsStateWithLifecycle()
    val handleInteraction: () -> Unit = { screenDimManager.resetTimer() }

    androidx.compose.runtime.LaunchedEffect(settingsState) {
        screenDimManager.updateConfiguration(
            dimDelaySeconds = settingsState.dimTimeoutSeconds,
            active = settingsState.activeBrightness,
            dim = settingsState.dimBrightness
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = CLOCK_ROUTE) {
            composable(CLOCK_ROUTE) {
                val clockViewModel: ClockViewModel = viewModel(factory = ClockViewModel.Factory)
                val state by clockViewModel.uiState.collectAsStateWithLifecycle()
                val todoViewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory)
                val todoState by todoViewModel.uiState.collectAsStateWithLifecycle()
                val topTodos = remember(todoState.todos) {
                    todoState.todos
                        .sortedBy { it.sortOrder }
                        .take(3)
                        .map { it.text }
                }
                ClockScreen(
                    state = state,
                    isDimmed = isDimmed,
                    burnInOffset = burnInOffset,
                    todoItems = topTodos,
                    onUserInteraction = handleInteraction,
                    onNavigateToTodo = { navController.navigate(TODO_ROUTE) },
                    onNavigateToAlarm = { navController.navigate(ALARM_ROUTE) },
                    onNavigateToSettings = { navController.navigate(SETTINGS_ROUTE) }
                )
            }
            composable(TODO_ROUTE) {
                val todoViewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory)
                val state by todoViewModel.uiState.collectAsStateWithLifecycle()
                androidx.compose.runtime.LaunchedEffect(settingsState.preferredTtsEngine) {
                    todoViewModel.updatePreferredEngine(settingsState.preferredTtsEngine)
                }
                TodoListScreen(
                    state = state,
                    onDraftChanged = todoViewModel::updateDraft,
                    onAddTodo = todoViewModel::addTodo,
                    onToggleCompleted = todoViewModel::toggleCompleted,
                    onDeleteTodo = todoViewModel::deleteTodo,
                    onReadTodos = todoViewModel::readTodos,
                    onStopReading = todoViewModel::stopReading,
                    onBack = { navController.popBackStack() },
                    onUserInteraction = handleInteraction
                )
            }
            composable(ALARM_ROUTE) {
                val alarmViewModel: AlarmSetupViewModel = viewModel(factory = AlarmSetupViewModel.Factory)
                val state by alarmViewModel.uiState.collectAsStateWithLifecycle()
                AlarmSetupScreen(
                    state = state,
                    canScheduleExactAlarms = alarmViewModel.canScheduleExactAlarms(),
                    onTimeChanged = { hour, minute ->
                        handleInteraction()
                        alarmViewModel.updateTime(hour, minute)
                    },
                    onLabelChanged = {
                        handleInteraction()
                        alarmViewModel.updateLabel(it)
                    },
                    onSave = {
                        handleInteraction()
                        if (alarmViewModel.canScheduleExactAlarms()) {
                            alarmViewModel.scheduleAlarm()
                        }
                    },
                    onCancelAlarm = {
                        handleInteraction()
                        alarmViewModel.cancelAlarm()
                    },
                    onBack = {
                        handleInteraction()
                        navController.popBackStack()
                    },
                    onOpenSettings = {
                        handleInteraction()
                        navController.context.startActivity(
                            Intent(alarmViewModel.openExactAlarmSettingsIntent()).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    },
                    onUserInteraction = handleInteraction,
                    onDismissError = {
                        handleInteraction()
                        alarmViewModel.consumeError()
                    }
                )
            }
            composable(SETTINGS_ROUTE) {
                SettingsScreen(
                    state = settingsState,
                    onDimTimeoutChanged = {
                        handleInteraction()
                        settingsViewModel.setDimTimeout(it)
                    },
                    onPreferredEngineChanged = {
                        handleInteraction()
                        settingsViewModel.setPreferredEngine(it)
                    },
                    onActiveBrightnessChanged = {
                        handleInteraction()
                        settingsViewModel.setActiveBrightness(it)
                    },
                    onDimBrightnessChanged = {
                        handleInteraction()
                        settingsViewModel.setDimBrightness(it)
                    },
                    onBack = {
                        handleInteraction()
                        navController.popBackStack()
                    },
                    onUserInteraction = handleInteraction
                )
            }
        }
    }
}
