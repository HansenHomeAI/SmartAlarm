package com.smartalarm.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TodoRepository(
    private val dao: TodoDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val todos: Flow<List<TodoEntity>> = dao.observeTodos()

    suspend fun addTodo(text: String) = withContext(ioDispatcher) {
        val sanitized = text.trim()
        require(sanitized.isNotEmpty()) { "Todo text cannot be blank" }

        val nextOrder = (dao.getMaxOrder() ?: -1) + 1
        val newTodo = TodoEntity(
            text = sanitized,
            createdAt = System.currentTimeMillis(),
            sortOrder = nextOrder
        )
        dao.insert(newTodo)
    }

    suspend fun updateTodo(todo: TodoEntity) = withContext(ioDispatcher) {
        dao.update(todo)
    }

    suspend fun toggleCompleted(todoId: Long, isCompleted: Boolean) = withContext(ioDispatcher) {
        val existing = dao.getById(todoId) ?: return@withContext
        dao.update(existing.copy(isCompleted = isCompleted))
    }

    suspend fun deleteTodo(todo: TodoEntity) = withContext(ioDispatcher) {
        dao.delete(todo)
    }

    suspend fun reorder(idsInOrder: List<Long>) = withContext(ioDispatcher) {
        dao.reorderTodos(idsInOrder)
    }
}
