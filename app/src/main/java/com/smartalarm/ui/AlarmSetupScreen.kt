package com.smartalarm.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartalarm.R
import com.smartalarm.alarm.AlarmInfo
import com.smartalarm.ui.theme.BlackBackground
import com.smartalarm.ui.theme.RedSecondary
import com.smartalarm.ui.theme.SmartAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSetupScreen(
    state: AlarmSetupUiState,
    canScheduleExactAlarms: Boolean,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    onLabelChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancelAlarm: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onUserInteraction: () -> Unit,
    onDismissError: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = state.selectedHour,
        initialMinute = state.selectedMinute,
        is24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
    )

    LaunchedEffect(state.selectedHour, state.selectedMinute) {
        if (timePickerState.hour != state.selectedHour || timePickerState.minute != state.selectedMinute) {
            timePickerState.hour = state.selectedHour
            timePickerState.minute = state.selectedMinute
        }
    }

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChanged(timePickerState.hour, timePickerState.minute)
    }

    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    onUserInteraction()
                    onBack()
                }) {
                    Text(text = stringResource(R.string.todo_back), color = RedSecondary)
                }
                Text(
                    text = stringResource(R.string.alarm_schedule_title),
                    style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(0.1f))
            }

            TimePicker(
                state = timePickerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = state.label,
                onValueChange = {
                    onUserInteraction()
                    onLabelChanged(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.alarm_label_hint), color = RedSecondary) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = RedSecondary),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = BlackBackground,
                    unfocusedContainerColor = BlackBackground,
                    focusedIndicatorColor = RedSecondary,
                    unfocusedIndicatorColor = RedSecondary,
                    cursorColor = RedSecondary
                )
            )

            if (!canScheduleExactAlarms) {
                WarningCard(
                    text = stringResource(R.string.alarm_permission_warning),
                    actionText = stringResource(R.string.alarm_permission_settings),
                    onAction = {
                        onUserInteraction()
                        onOpenSettings()
                    }
                )
            }

            AlarmSummaryCard(state.nextAlarm)

            state.errorMessage?.let { message ->
                Surface(
                    color = Color(0x33FF0000),
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = message, color = Color.White, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            onUserInteraction()
                            onDismissError()
                        }) {
                            Text(text = stringResource(android.R.string.ok), color = Color.White)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                    onClick = {
                        onUserInteraction()
                        onCancelAlarm()
                    }
                ) {
                    Text(text = stringResource(R.string.alarm_cancel))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RedSecondary, contentColor = BlackBackground),
                    enabled = canScheduleExactAlarms,
                    onClick = {
                        onUserInteraction()
                        onSave()
                    }
                ) {
                    Text(text = stringResource(R.string.alarm_save), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WarningCard(text: String, actionText: String, onAction: () -> Unit) {
    Surface(
        color = Color(0x55FF0000),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, color = Color.White, modifier = Modifier.weight(1f))
            TextButton(onClick = onAction) {
                Text(text = actionText, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AlarmSummaryCard(nextAlarm: AlarmInfo?) {
    Surface(
        color = Color(0x22FF0000),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.alarm_upcoming_header), color = Color.White, fontWeight = FontWeight.Bold)
            if (nextAlarm == null) {
                Text(text = stringResource(R.string.clock_no_alarm), color = Color.White)
            } else {
                val time = java.time.Instant.ofEpochMilli(nextAlarm.triggerAtMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                Text(text = time.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d HH:mm")), color = Color.White)
                nextAlarm.label?.let {
                    Text(text = it, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlarmSetupPreview() {
    SmartAlarmTheme {
        AlarmSetupScreen(
            state = AlarmSetupUiState(),
            canScheduleExactAlarms = true,
            onTimeChanged = { _, _ -> },
            onLabelChanged = {},
            onSave = {},
            onCancelAlarm = {},
            onBack = {},
            onOpenSettings = {},
            onUserInteraction = {},
            onDismissError = {}
        )
    }
}
