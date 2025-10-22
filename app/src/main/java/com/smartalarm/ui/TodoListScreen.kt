package com.smartalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartalarm.data.TodoEntity
import com.smartalarm.ui.theme.BlackBackground
import com.smartalarm.ui.theme.RedPrimary
import com.smartalarm.ui.theme.RedSecondary
import com.smartalarm.ui.theme.SmartAlarmTheme
import androidx.compose.ui.res.stringResource
import com.smartalarm.R

@Composable
fun TodoListScreen(
    state: TodoUiState,
    onDraftChanged: (String) -> Unit,
    onAddTodo: () -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit,
    onReadTodos: () -> Unit,
    onStopReading: () -> Unit,
    onBack: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = onBack
                ) {
                    Text(text = stringResource(R.string.todo_back), fontSize = 20.sp, color = RedSecondary)
                }
                Text(
                    text = stringResource(R.string.todo_title),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 64.sp, color = RedPrimary),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            TodoInputRow(
                text = state.newTodoText,
                onTextChanged = onDraftChanged,
                onSubmit = onAddTodo
            )

            TodoActionsRow(
                isReading = state.isReading,
                hasTodos = state.todos.isNotEmpty(),
                onReadTodos = onReadTodos,
                onStopReading = onStopReading
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.todos, key = { it.id }) { todo ->
                    TodoListItem(
                        todo = todo,
                        onToggle = { checked -> onToggleCompleted(todo.id, checked) },
                        onDelete = { onDeleteTodo(todo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodoInputRow(
    text: String,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = RedPrimary, fontSize = 24.sp),
            label = { Text(stringResource(R.string.todo_new_item_label), color = RedSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RedPrimary,
                unfocusedBorderColor = RedSecondary,
                cursorColor = RedPrimary,
                focusedContainerColor = BlackBackground,
                unfocusedContainerColor = BlackBackground
            )
        )
        Button(
            onClick = onSubmit,
            enabled = text.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = RedSecondary,
                contentColor = BlackBackground,
                disabledContainerColor = RedSecondary.copy(alpha = 0.4f),
                disabledContentColor = BlackBackground.copy(alpha = 0.4f)
            )
        ) {
            Text(stringResource(R.string.todo_add), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TodoActionsRow(
    isReading: Boolean,
    hasTodos: Boolean,
    onReadTodos: () -> Unit,
    onStopReading: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onReadTodos,
            enabled = hasTodos && !isReading,
            colors = ButtonDefaults.buttonColors(
                containerColor = RedSecondary,
                contentColor = BlackBackground,
                disabledContainerColor = RedSecondary.copy(alpha = 0.4f),
                disabledContentColor = BlackBackground.copy(alpha = 0.4f)
            )
        ) {
            Text(stringResource(R.string.todo_read_all), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onStopReading,
            enabled = isReading,
            colors = ButtonDefaults.buttonColors(
                containerColor = RedSecondary,
                contentColor = BlackBackground,
                disabledContainerColor = RedSecondary.copy(alpha = 0.4f),
                disabledContentColor = BlackBackground.copy(alpha = 0.4f)
            )
        ) {
            Text(stringResource(R.string.todo_stop), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TodoListItem(
    todo: TodoEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BlackBackground),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = RedPrimary,
                uncheckedColor = RedSecondary,
                checkmarkColor = BlackBackground
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.text,
                style = TextStyle(
                    color = RedPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            )
        }
        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedSecondary)
        }
    }
}

@Preview
@Composable
private fun TodoListScreenPreview() {
    SmartAlarmTheme {
        val sampleTodos = listOf(
            TodoEntity(id = 1, text = "Set alarm for 6 AM", createdAt = 0L, sortOrder = 0),
            TodoEntity(id = 2, text = "Charge phone", createdAt = 0L, isCompleted = true, sortOrder = 1)
        )
        TodoListScreen(
            state = TodoUiState(todos = sampleTodos),
            onDraftChanged = {},
            onAddTodo = {},
            onToggleCompleted = { _, _ -> },
            onDeleteTodo = {},
            onReadTodos = {},
            onStopReading = {},
            onBack = {}
        )
    }
}
