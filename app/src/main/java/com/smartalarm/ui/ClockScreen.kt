package com.smartalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.smartalarm.ui.theme.BlackBackground
import com.smartalarm.ui.theme.RedSecondary
import com.smartalarm.ui.theme.SmartAlarmTheme
import android.view.MotionEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ClockScreen(
    state: ClockUiState,
    isDimmed: Boolean,
    burnInOffset: IntOffset,
    onUserInteraction: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val locale = Locale.getDefault()
    val timeFormatter = remember(locale) { DateTimeFormatter.ofPattern("HH:mm", locale) }
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEE, MMM d", locale) }

    val timeText = formatTime(state.currentTimeMillis, timeFormatter)
    val noAlarmText = stringResource(R.string.clock_no_alarm)
    val nextAlarmText = state.nextAlarm?.let { formatNextAlarm(it, timeFormatter, dateFormatter) } ?: noAlarmText
    val contentAlpha = if (isDimmed) 0.35f else 1f
    val density = LocalDensity.current
    val offsetXDp = with(density) { burnInOffset.x.toDp() }
    val offsetYDp = with(density) { burnInOffset.y.toDp() }

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
                .padding(horizontal = 32.dp, vertical = 48.dp)
                .offset(x = offsetXDp, y = offsetYDp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha)
            ) {
                Text(
                    text = stringResource(R.string.clock_title),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 128.sp)
                )
                Text(
                    text = buildString {
                        append(stringResource(R.string.clock_next_alarm))
                        append(": ")
                        append(nextAlarmText)
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ClockActionButton(
                    text = stringResource(R.string.clock_set_alarm),
                    onClick = {
                        onUserInteraction()
                        onNavigateToAlarm()
                    },
                    enabled = state.canScheduleExactAlarms
                )
                ClockActionButton(
                    text = stringResource(R.string.clock_view_todos),
                    onClick = {
                        onUserInteraction()
                        onNavigateToTodo()
                    },
                    enabled = true
                )
                ClockActionButton(
                    text = stringResource(R.string.clock_open_settings),
                    onClick = {
                        onUserInteraction()
                        onNavigateToSettings()
                    },
                    enabled = true
                )
            }
        }
    }
}

@Composable
private fun ClockActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = RedSecondary,
            contentColor = BlackBackground,
            disabledContainerColor = RedSecondary.copy(alpha = 0.4f),
            disabledContentColor = BlackBackground.copy(alpha = 0.4f)
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp))
    }
}

private fun formatTime(millis: Long, formatter: DateTimeFormatter): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun formatNextAlarm(alarm: AlarmInfo, formatter: DateTimeFormatter, dateFormatter: DateTimeFormatter): String {
    val time = Instant.ofEpochMilli(alarm.triggerAtMillis).atZone(ZoneId.systemDefault())
    val label = alarm.label?.takeIf { it.isNotBlank() }
    return buildString {
        append(time.format(formatter))
        append(" · ")
        append(time.format(dateFormatter))
        label?.let {
            append(" · ")
            append(it)
        }
        if (alarm.isSnoozed) {
            append(" · Snoozed")
        }
    }
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
            onUserInteraction = {},
            onNavigateToTodo = {},
            onNavigateToAlarm = {},
            onNavigateToSettings = {}
        )
    }
}
