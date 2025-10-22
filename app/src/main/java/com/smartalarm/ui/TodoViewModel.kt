package com.smartalarm.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.smartalarm.data.TodoDatabase
import com.smartalarm.data.TodoEntity
import com.smartalarm.data.TodoRepository
import com.smartalarm.tts.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoViewModel(
    application: Application,
    private val repository: TodoRepository,
    private val ttsManager: TtsManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.todos.collect { todos ->
                _uiState.update { it.copy(todos = todos) }
            }
        }
    }

    fun updateDraft(text: String) {
        _uiState.update { it.copy(newTodoText = text) }
    }

    fun addTodo() {
        val text = _uiState.value.newTodoText
        if (text.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.addTodo(text) }
                .onSuccess {
                    _uiState.update { it.copy(newTodoText = "") }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to add todo", throwable)
                }
        }
    }

    fun toggleCompleted(todoId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            runCatching { repository.toggleCompleted(todoId, isCompleted) }
                .onFailure { throwable -> Log.e(TAG, "Failed to toggle todo", throwable) }
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            runCatching { repository.deleteTodo(todo) }
                .onFailure { throwable -> Log.e(TAG, "Failed to delete todo", throwable) }
        }
    }

    fun readTodos() {
        val snapshot = _uiState.value.todos
        if (snapshot.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isReading = true) }
            val speech = buildSpeechFromTodos(snapshot)
            runCatching { ttsManager.speak(speech) }
                .onFailure { throwable -> Log.e(TAG, "Failed to speak todos", throwable) }
            _uiState.update { it.copy(isReading = false) }
        }
    }

    fun stopReading() {
        viewModelScope.launch {
            ttsManager.stop()
            _uiState.update { it.copy(isReading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.close()
    }

    private fun buildSpeechFromTodos(todos: List<TodoEntity>): String {
        val builder = StringBuilder()
        builder.append("You have ${todos.size} to-do items.")
        todos.forEachIndexed { index, todo ->
            builder.append(' ')
            builder.append(index + 1)
            builder.append('.')
            builder.append(' ')
            if (todo.isCompleted) {
                builder.append("Completed: ")
            }
            builder.append(todo.text)
            builder.append('.')
        }
        return builder.toString().ifBlank { "Your to-do list is empty." }
    }

    companion object {
        private const val TAG = "TodoViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val database = TodoDatabase.get(application)
                val repository = TodoRepository(database.todoDao())
                val ttsManager = TtsManager(application)
                TodoViewModel(application, repository, ttsManager)
            }
        }
    }
}

data class TodoUiState(
    val todos: List<TodoEntity> = emptyList(),
    val newTodoText: String = "",
    val isReading: Boolean = false
)
