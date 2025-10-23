package com.smartalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartalarm.R
import com.smartalarm.ui.theme.BlackBackground

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
    val isDimmed by screenDimManager.isDimmed.collectAsStateWithLifecycle()
    val burnInOffset by burnInPreventionManager.offset.collectAsStateWithLifecycle()
    val handleInteraction: () -> Unit = { screenDimManager.resetTimer() }

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = CLOCK_ROUTE) {
            composable(CLOCK_ROUTE) {
                val clockViewModel: ClockViewModel = viewModel(factory = ClockViewModel.Factory)
                val state by clockViewModel.uiState.collectAsStateWithLifecycle()
                ClockScreen(
                    state = state,
                    isDimmed = isDimmed,
                    burnInOffset = burnInOffset,
                    onUserInteraction = handleInteraction,
                    onNavigateToTodo = { navController.navigate(TODO_ROUTE) },
                    onNavigateToAlarm = { navController.navigate(ALARM_ROUTE) },
                    onNavigateToSettings = { navController.navigate(SETTINGS_ROUTE) }
                )
            }
            composable(TODO_ROUTE) {
                val todoViewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory)
                val state by todoViewModel.uiState.collectAsStateWithLifecycle()
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
                ComingSoonScreen(
                    text = stringResource(R.string.alarm_setup_title),
                    onBack = {
                        handleInteraction()
                        navController.popBackStack()
                    },
                    onUserInteraction = handleInteraction
                )
            }
            composable(SETTINGS_ROUTE) {
                ComingSoonScreen(
                    text = stringResource(R.string.settings_title),
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

@Composable
private fun ComingSoonScreen(text: String, onBack: () -> Unit, onUserInteraction: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(32.dp)
        ) {
            TextButton(
                onClick = {
                    onUserInteraction()
                    onBack()
                },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(text = stringResource(R.string.todo_back))
            }
            Text(text = text, modifier = Modifier.align(Alignment.Center))
        }
    }
}
