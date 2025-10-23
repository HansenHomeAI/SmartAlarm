package com.smartalarm.ui

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.smartalarm.R
import com.smartalarm.alarm.AlarmInfo
import com.smartalarm.data.TodoEntity
import com.smartalarm.ui.theme.BlackBackground
import com.smartalarm.ui.theme.RedPrimary
import com.smartalarm.ui.theme.RedSecondary
import com.smartalarm.ui.theme.SmartAlarmTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    clockState: ClockUiState,
    alarmState: AlarmSetupUiState,
    todoState: TodoUiState,
    canScheduleExactAlarms: Boolean,
    isDimmed: Boolean,
    burnInOffset: IntOffset,
    ringing: AlarmUiState?,
    onAlarmTimeChanged: (Int, Int) -> Unit,
    onAlarmLabelChanged: (String) -> Unit,
    onSaveAlarm: () -> Unit,
    onCancelAlarm: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onTodoDraftChanged: (String) -> Unit,
    onAddTodo: () -> Unit,
    onToggleTodo: (TodoEntity, Boolean) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit,
    onReadTodos: () -> Unit,
    onStopReading: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
    onUserInteraction: () -> Unit
) {
    val locale = Locale.getDefault()
    val timeFormatter = remember(locale) { DateTimeFormatter.ofPattern("hh:mm", locale) }
    val amPmFormatter = remember(locale) { DateTimeFormatter.ofPattern("a", locale) }
    val nextAlarmFormatter = remember(locale) { DateTimeFormatter.ofPattern("h:mm a", locale) }

    val currentInstant = Instant.ofEpochMilli(clockState.currentTimeMillis)
    val currentTime = currentInstant.atZone(ZoneId.systemDefault())
    val timeText = currentTime.format(timeFormatter)
    val amPmText = currentTime.format(amPmFormatter)

    val nextAlarmText = clockState.nextAlarm?.let { formatNextAlarm(it, nextAlarmFormatter) }
        ?: stringResource(R.string.clock_no_alarm)

    val contentAlpha = if (isDimmed) 0.3f else 1f
    val density = LocalDensity.current
    val offsetXDp = with(density) { burnInOffset.x.toDp() }
    val offsetYDp = with(density) { burnInOffset.y.toDp() }

    var showAlarmEditor by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE,
                    MotionEvent.ACTION_POINTER_DOWN -> onUserInteraction()
                }
                false
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(horizontal = 48.dp, vertical = 40.dp)
                .offset(x = offsetXDp, y = offsetYDp)
                .alpha(contentAlpha)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            TimeDisplayBlock(timeText = timeText, amPmText = amPmText.lowercase(locale), nextAlarmText = nextAlarmText)

            AlarmStatusBlock(
                showEditor = showAlarmEditor,
                canScheduleExactAlarms = canScheduleExactAlarms,
                alarmState = alarmState,
                onToggleEditor = {
                    onUserInteraction()
                    showAlarmEditor = !showAlarmEditor
                },
                onAlarmTimeChanged = onAlarmTimeChanged,
                onAlarmLabelChanged = onAlarmLabelChanged,
                onSaveAlarm = {
                    onUserInteraction()
                    onSaveAlarm()
                    showAlarmEditor = false
                },
                onCancelAlarm = {
                    onUserInteraction()
                    onCancelAlarm()
                    showAlarmEditor = false
                },
                onOpenAlarmSettings = {
                    onUserInteraction()
                    onOpenAlarmSettings()
                },
                onUserInteraction = onUserInteraction
            )

            Divider(color = RedSecondary.copy(alpha = 0.4f))

            TodoSection(
                todoState = todoState,
                onTodoDraftChanged = onTodoDraftChanged,
                onAddTodo = onAddTodo,
                onToggleTodo = onToggleTodo,
                onDeleteTodo = onDeleteTodo,
                onReadTodos = onReadTodos,
                onStopReading = onStopReading,
                onUserInteraction = onUserInteraction
            )
        }
    }

    if (ringing != null) {
        RingingOverlay(
            ringing = ringing,
            nextAlarmText = nextAlarmText,
            onSnooze = onSnooze,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun TimeDisplayBlock(timeText: String, amPmText: String, nextAlarmText: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 180.sp, fontWeight = FontWeight.Light, color = RedPrimary)
            )
            Text(
                text = amPmText,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 48.sp, color = RedSecondary)
            )
        }
        Text(
            text = stringResource(R.string.clock_alarm_label, nextAlarmText),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp, color = RedSecondary)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmStatusBlock(
    showEditor: Boolean,
    canScheduleExactAlarms: Boolean,
    alarmState: AlarmSetupUiState,
    onToggleEditor: () -> Unit,
    onAlarmTimeChanged: (Int, Int) -> Unit,
    onAlarmLabelChanged: (String) -> Unit,
    onSaveAlarm: () -> Unit,
    onCancelAlarm: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onUserInteraction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, RedSecondary.copy(alpha = 0.4f), RoundedCornerShape(36.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.clock_adjust_alarm_action),
                style = MaterialTheme.typography.titleLarge.copy(color = RedPrimary)
            )
            TextButton(onClick = onToggleEditor) {
                Text(
                    text = if (showEditor) stringResource(android.R.string.cancel) else stringResource(R.string.clock_adjust_alarm_action),
                    color = RedSecondary
                )
            }
        }

        AnimatedVisibility(visible = showEditor) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val pickerState = rememberTimePickerState(
                    initialHour = alarmState.selectedHour,
                    initialMinute = alarmState.selectedMinute,
                    is24Hour = false
                )
                LaunchedEffect(alarmState.selectedHour, alarmState.selectedMinute) {
                    if (pickerState.hour != alarmState.selectedHour || pickerState.minute != alarmState.selectedMinute) {
                        pickerState.hour = alarmState.selectedHour
                        pickerState.minute = alarmState.selectedMinute
                    }
                }
                LaunchedEffect(pickerState.hour, pickerState.minute) {
                    onAlarmTimeChanged(pickerState.hour, pickerState.minute)
                }

                TimePicker(state = pickerState, modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally))

                OutlinedTextField(
                    value = alarmState.label,
                    onValueChange = {
                        onUserInteraction()
                        onAlarmLabelChanged(it)
                    },
                    label = { Text(stringResource(R.string.alarm_label_hint), color = RedSecondary) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = RedPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = BlackBackground,
                        unfocusedContainerColor = BlackBackground,
                        focusedIndicatorColor = RedSecondary,
                        unfocusedIndicatorColor = RedSecondary,
                        cursorColor = RedSecondary
                    )
                )

                if (!canScheduleExactAlarms) {
                    TextButton(onClick = onOpenAlarmSettings) {
                        Text(text = stringResource(R.string.alarm_permission_settings), color = RedSecondary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            onUserInteraction()
                            onCancelAlarm()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RedSecondary.copy(alpha = 0.2f), contentColor = RedSecondary)
                    ) {
                        Text(text = stringResource(R.string.alarm_cancel))
                    }
                    Button(
                        onClick = {
                            onUserInteraction()
                            onSaveAlarm()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canScheduleExactAlarms,
                        colors = ButtonDefaults.buttonColors(containerColor = RedSecondary, contentColor = BlackBackground)
                    ) {
                        Text(text = stringResource(R.string.alarm_save), fontWeight = FontWeight.Bold)
                    }
                }

                alarmState.errorMessage?.let { error ->
                    Text(text = error, color = Color(0xFFFFB4B4))
                }
            }
        }
    }
}

@Composable
private fun TodoSection(
    todoState: TodoUiState,
    onTodoDraftChanged: (String) -> Unit,
    onAddTodo: () -> Unit,
    onToggleTodo: (TodoEntity, Boolean) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit,
    onReadTodos: () -> Unit,
    onStopReading: () -> Unit,
    onUserInteraction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, RedSecondary.copy(alpha = 0.4f), RoundedCornerShape(36.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.todo_title),
                style = MaterialTheme.typography.titleLarge.copy(color = RedPrimary)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onClick = {
                    onUserInteraction()
                    onReadTodos()
                }) {
                    Text(text = stringResource(R.string.todo_read_all), color = RedSecondary)
                }
                if (todoState.isReading) {
                    TextButton(onClick = {
                        onUserInteraction()
                        onStopReading()
                    }) {
                        Text(text = stringResource(R.string.todo_stop), color = RedSecondary)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
           OutlinedTextField(
                value = todoState.newTodoText,
                onValueChange = {
                    onUserInteraction()
                    onTodoDraftChanged(it)
                },
                label = { Text(stringResource(R.string.todo_new_item_label), color = RedSecondary) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("todoInput"),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = RedPrimary),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = BlackBackground,
                    unfocusedContainerColor = BlackBackground,
                    focusedIndicatorColor = RedSecondary,
                    unfocusedIndicatorColor = RedSecondary,
                    cursorColor = RedSecondary
                )
            )
           Button(
                onClick = {
                    onUserInteraction()
                    onAddTodo()
                },
                enabled = todoState.newTodoText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RedSecondary, contentColor = BlackBackground),
                modifier = Modifier.testTag("addTodoButton")
            ) {
                Text(text = stringResource(R.string.todo_add), fontWeight = FontWeight.Bold)
            }
        }

        if (todoState.todos.isEmpty()) {
            Text(
                text = stringResource(R.string.clock_no_todos),
                style = MaterialTheme.typography.bodyLarge.copy(color = RedSecondary)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                todoState.todos.sortedBy { it.sortOrder }.forEach { todo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = todo.isCompleted,
                                onCheckedChange = {
                                    onUserInteraction()
                                    onToggleTodo(todo, it)
                                },
                                colors = androidx.compose.material3.CheckboxDefaults.colors(
                                    checkedColor = RedSecondary,
                                    uncheckedColor = RedSecondary,
                                    checkmarkColor = BlackBackground
                                )
                            )
                           Text(
                                text = "• ${todo.text}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (todo.isCompleted) RedSecondary.copy(alpha = 0.6f) else RedPrimary,
                                    fontSize = 26.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = {
                            onUserInteraction()
                            onDeleteTodo(todo)
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = RedSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RingingOverlay(
    ringing: AlarmUiState,
    nextAlarmText: String,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ringing.label ?: stringResource(R.string.alarm_label_default),
                style = MaterialTheme.typography.headlineLarge.copy(color = RedPrimary)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = nextAlarmText,
                    style = MaterialTheme.typography.titleLarge.copy(color = RedSecondary)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RedSecondary, contentColor = BlackBackground)
                ) {
                    Text(text = stringResource(R.string.alarm_snooze), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RedSecondary.copy(alpha = 0.3f), contentColor = RedSecondary)
                ) {
                    Text(text = stringResource(R.string.alarm_dismiss), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatNextAlarm(alarmInfo: AlarmInfo, formatter: DateTimeFormatter): String {
    val zoned = Instant.ofEpochMilli(alarmInfo.triggerAtMillis).atZone(ZoneId.systemDefault())
    val base = formatter.format(zoned)
    return if (!alarmInfo.label.isNullOrBlank()) {
        "$base • ${alarmInfo.label}"
    } else {
        base
    }
}

@Preview
@Composable
private fun ClockScreenPreview() {
    val clockState = ClockUiState(
        currentTimeMillis = System.currentTimeMillis(),
        nextAlarm = AlarmInfo(
            triggerAtMillis = System.currentTimeMillis() + 60 * 60 * 1000,
            label = "Morning Alarm",
            isSnoozed = false
        )
    )
    val alarmState = AlarmSetupUiState(selectedHour = 6, selectedMinute = 30, label = "Morning")
    val todoState = TodoUiState(
        todos = listOf(
            TodoEntity(id = 1, text = "Make coffee", createdAt = 0L, sortOrder = 0),
            TodoEntity(id = 2, text = "Review notes", createdAt = 0L, sortOrder = 1)
        ),
        newTodoText = "Add new item"
    )
    SmartAlarmTheme {
        ClockScreen(
            clockState = clockState,
            alarmState = alarmState,
            todoState = todoState,
            canScheduleExactAlarms = true,
            isDimmed = false,
            burnInOffset = IntOffset.Zero,
            ringing = null,
            onAlarmTimeChanged = { _, _ -> },
            onAlarmLabelChanged = {},
            onSaveAlarm = {},
            onCancelAlarm = {},
            onOpenAlarmSettings = {},
            onTodoDraftChanged = {},
            onAddTodo = {},
            onToggleTodo = { _, _ -> },
            onDeleteTodo = {},
            onReadTodos = {},
            onStopReading = {},
            onSnooze = {},
            onDismiss = {},
            onUserInteraction = {}
        )
    }
}
