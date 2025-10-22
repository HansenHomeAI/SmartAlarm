package com.smartalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartalarm.ui.theme.BlackBackground
import com.smartalarm.ui.theme.RedSecondary
import com.smartalarm.ui.theme.SmartAlarmTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun AlarmTriggerScreen(
    state: AlarmUiState,
    isProcessing: Boolean,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentTimeMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val locale = Locale.getDefault()
    val timeFormatter = remember(locale) { DateTimeFormatter.ofPattern("HH:mm", locale) }
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEE, MMM d", locale) }

    val scheduledTimeText = remember(state.triggerAtMillis) {
        formatInstant(state.triggerAtMillis, timeFormatter)
    }
    val scheduledDateText = remember(state.triggerAtMillis) {
        formatInstant(state.triggerAtMillis, dateFormatter)
    }
    val currentTimeText = remember(currentTimeMillis) {
        formatInstant(currentTimeMillis, timeFormatter)
    }

    val labelText = state.label?.takeIf { it.isNotBlank() } ?: stringResource(com.smartalarm.R.string.alarm_label_default)
    val snoozedLabel = stringResource(com.smartalarm.R.string.alarm_label_snoozed)
    val currentTimeLabel = stringResource(com.smartalarm.R.string.alarm_current_time).uppercase(locale)
    val scheduledLabel = stringResource(com.smartalarm.R.string.alarm_scheduled_time).uppercase(locale)
    val snoozeText = stringResource(com.smartalarm.R.string.alarm_snooze)
    val dismissText = stringResource(com.smartalarm.R.string.alarm_dismiss)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(horizontal = 32.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp)
                )
                if (state.isSnooze) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = snoozedLabel,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTimeLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = currentTimeText,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = scheduledLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = scheduledTimeText,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scheduledDateText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AlarmActionButton(
                    modifier = Modifier.weight(1f),
                    text = snoozeText,
                    onClick = onSnooze,
                    enabled = !isProcessing
                )
                AlarmActionButton(
                    modifier = Modifier.weight(1f),
                    text = dismissText,
                    onClick = onDismiss,
                    enabled = !isProcessing
                )
            }
        }
    }
}

@Composable
private fun AlarmActionButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(72.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = RedSecondary,
            contentColor = BlackBackground,
            disabledContainerColor = RedSecondary.copy(alpha = 0.5f),
            disabledContentColor = BlackBackground.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )
    }
}

private fun formatInstant(millis: Long, formatter: DateTimeFormatter): String {
    return if (millis <= 0L) {
        "--:--"
    } else {
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}

@Preview
@Composable
private fun AlarmTriggerScreenPreview() {
    SmartAlarmTheme(darkTheme = true) {
        AlarmTriggerScreen(
            state = AlarmUiState(
                triggerAtMillis = System.currentTimeMillis() + 30 * 60 * 1000L,
                label = "Morning Alarm",
                isSnooze = false
            ),
            isProcessing = false,
            onSnooze = {},
            onDismiss = {}
        )
    }
}
