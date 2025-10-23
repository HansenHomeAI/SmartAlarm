package com.smartalarm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartalarm.R
import com.smartalarm.alarm.AlarmInfo
import com.smartalarm.ui.AlarmUiState
import com.smartalarm.ui.theme.BlackBackground
import com.smartalarm.ui.theme.RedSecondary
import com.smartalarm.ui.theme.RedPrimary
import android.view.MotionEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.smartalarm.ui.theme.SmartAlarmTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ClockScreen(
    state: ClockUiState,
    isDimmed: Boolean,
    burnInOffset: IntOffset,
    todoItems: List<String>,
    ringing: AlarmUiState?,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
    onDebugTriggerRing: (() -> Unit)? = null,
    onUserInteraction: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val locale = Locale.getDefault()
    val clockFormatter = remember(locale) { DateTimeFormatter.ofPattern("hh:mm", locale) }
    val amPmFormatter = remember(locale) { DateTimeFormatter.ofPattern("a", locale) }
    val alarmFormatter = remember(locale) { DateTimeFormatter.ofPattern("h:mma", locale) }

    val timeText = formatTime(state.currentTimeMillis, clockFormatter)
    val amPmText = formatTime(state.currentTimeMillis, amPmFormatter).lowercase(locale)
    val noAlarmText = stringResource(R.string.clock_no_alarm)
    val nextAlarmText = state.nextAlarm?.let { formatNextAlarm(it, alarmFormatter) } ?: noAlarmText
    val contentAlpha = if (isDimmed) 0.35f else 1f
    val density = LocalDensity.current
    val offsetXDp = with(density) { burnInOffset.x.toDp() }
    val offsetYDp = with(density) { burnInOffset.y.toDp() }

    val alarmLabel = state.nextAlarm?.let { stringResource(R.string.clock_alarm_label, nextAlarmText) }
        ?: stringResource(R.string.clock_alarm_none_label)

    // Debug logging
    android.util.Log.d("ClockScreen", "timeText: $timeText, amPmText: $amPmText, alarmLabel: $alarmLabel, todoItems: $todoItems")

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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(horizontal = 32.dp, vertical = 48.dp)
                .offset(x = offsetXDp, y = offsetYDp)
                .alpha(contentAlpha),
            horizontalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text(
                        text = alarmLabel,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Medium,
                            color = RedPrimary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        ClockActionPill(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.clock_snooze_action)
                        ) {
                            onUserInteraction()
                            onNavigateToAlarm()
                        }
                        ClockActionPill(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.clock_stop_action)
                        ) {
                            onUserInteraction()
                            onNavigateToSettings()
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 200.sp,
                                fontWeight = FontWeight.Light,
                                color = RedPrimary
                            ),
                            modifier = Modifier.then(
                                if (onDebugTriggerRing != null) {
                                    Modifier.combinedClickable(
                                        onClick = {},
                                        onLongClick = { onDebugTriggerRing() }
                                    )
                                } else Modifier
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.padding(bottom = 32.dp)) {
                            Text(
                                text = amPmText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = RedSecondary
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    ClockHelperButton(text = stringResource(R.string.clock_adjust_alarm_action)) {
                        onUserInteraction()
                        onNavigateToAlarm()
                    }
                    ClockHelperButton(text = stringResource(R.string.clock_settings_action)) {
                        onUserInteraction()
                        onNavigateToSettings()
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            FocusPanel(
                items = todoItems,
                onManageTodos = {
                    onUserInteraction()
                    onNavigateToTodo()
                }
            )
        }
    }

    // Ringing overlay
    if (ringing != null) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ringing.label ?: stringResource(com.smartalarm.R.string.alarm_label_default),
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 36.sp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = nextAlarmText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ClockActionPill(modifier = Modifier.weight(1f), text = stringResource(com.smartalarm.R.string.alarm_snooze)) {
                        onSnooze()
                    }
                    ClockActionPill(modifier = Modifier.weight(1f), text = stringResource(com.smartalarm.R.string.alarm_dismiss)) {
                        onDismiss()
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockHelperButton(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = RedSecondary)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp))
    }
}

@Composable
private fun FocusPanel(items: List<String>, onManageTodos: () -> Unit) {
    val shape = RoundedCornerShape(48.dp)
    Column(
        modifier = Modifier
            .width(400.dp)
            .fillMaxHeight()
            .border(width = 4.dp, color = RedSecondary, shape = shape)
            .padding(horizontal = 36.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = stringResource(R.string.clock_focus_header),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 56.sp, color = RedPrimary)
            )
            if (items.isEmpty()) {
                Text(
                    text = stringResource(R.string.clock_no_todos),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp, color = RedSecondary)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp, color = RedPrimary)
                        )
                    }
                }
            }
        }

        Button(
            onClick = onManageTodos,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = RedSecondary,
                contentColor = BlackBackground
            )
        ) {
            Text(
                text = stringResource(R.string.clock_manage_todos),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ClockActionPill(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(60.dp),
        border = BorderStroke(4.dp, RedPrimary),
        modifier = modifier
            .height(96.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = RedPrimary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
        )
    }
}

private fun formatTime(millis: Long, formatter: DateTimeFormatter): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun formatNextAlarm(alarm: AlarmInfo, formatter: DateTimeFormatter): String {
    val locale = Locale.getDefault()
    return Instant.ofEpochMilli(alarm.triggerAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
        .lowercase(locale)
}

@Preview
@Composable
private fun ClockScreenPreview() {
    SmartAlarmTheme {
        ClockScreen(
            state = ClockUiState(
                currentTimeMillis = System.currentTimeMillis(),
                nextAlarm = AlarmInfo(
                    triggerAtMillis = System.currentTimeMillis() + 60 * 60 * 1000,
                    label = "Morning Alarm",
                    isSnoozed = false
                ),
                canScheduleExactAlarms = true
            ),
            isDimmed = false,
            burnInOffset = IntOffset.Zero,
            todoItems = listOf(
                "Creating an Alarm Clock",
                "Building a revolutionary company @ 9:18pm",
                "Preparing tomorrow's tasks"
            ),
            ringing = null,
            onSnooze = {},
            onDismiss = {},
            onDebugTriggerRing = null,
            onUserInteraction = {},
            onNavigateToTodo = {},
            onNavigateToAlarm = {},
            onNavigateToSettings = {}
        )
    }
}
