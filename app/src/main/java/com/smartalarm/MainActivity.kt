package com.smartalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartalarm.ui.theme.SmartAlarmTheme
import com.smartalarm.ui.TodoListScreen
import com.smartalarm.ui.TodoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartAlarmTheme {
                val todoViewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory)
                val state = todoViewModel.uiState.collectAsStateWithLifecycle().value
                TodoListScreen(
                    state = state,
                    onDraftChanged = todoViewModel::updateDraft,
                    onAddTodo = todoViewModel::addTodo,
                    onToggleCompleted = todoViewModel::toggleCompleted,
                    onDeleteTodo = todoViewModel::deleteTodo,
                    onReadTodos = todoViewModel::readTodos,
                    onStopReading = todoViewModel::stopReading
                )
            }
        }
    }
}
