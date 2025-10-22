package com.smartalarm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY sortOrder ASC")
    fun observeTodos(): Flow<List<TodoEntity>>

    @Query("SELECT MAX(sortOrder) FROM todos")
    suspend fun getMaxOrder(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TodoEntity?

    @Query("UPDATE todos SET sortOrder = :newOrder WHERE id = :id")
    suspend fun updateOrder(id: Long, newOrder: Int)

    @Transaction
    suspend fun reorderTodos(idsInOrder: List<Long>) {
        idsInOrder.forEachIndexed { index, id ->
            updateOrder(id = id, newOrder = index)
        }
    }
}
