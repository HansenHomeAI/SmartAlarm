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
                val alarmTriggerActivityState: AlarmUiState? = null // placeholder; unify later with a shared source
                ClockScreen(
                    state = state,
                    isDimmed = isDimmed,
                    burnInOffset = burnInOffset,
                    todoItems = topTodos,
                    ringing = alarmTriggerActivityState,
                    onSnooze = { /* integrate with SnoozeManager */ },
                    onDismiss = { /* integrate with AlarmScheduler cancel */ },
                    onDebugTriggerRing = null,
                    onUserInteraction = handleInteraction,
                    onNavigateToTodo = { /* TODO: implement inline todo editing */ },
                    onNavigateToAlarm = { /* TODO: implement inline alarm setup */ },
                    onNavigateToSettings = { /* TODO: implement inline settings */ }
                )
            }
            // Removed separate routes - everything is now unified in ClockScreen
        }
    }
}
