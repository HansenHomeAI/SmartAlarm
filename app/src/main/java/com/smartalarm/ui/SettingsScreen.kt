package com.smartalarm.ui

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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartalarm.R
import com.smartalarm.settings.PreferredTtsEngine
import com.smartalarm.ui.theme.BlackBackground
import com.smartalarm.ui.theme.RedSecondary
import com.smartalarm.ui.theme.SmartAlarmTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onDimTimeoutChanged: (Int) -> Unit,
    onPreferredEngineChanged: (PreferredTtsEngine) -> Unit,
    onActiveBrightnessChanged: (Float) -> Unit,
    onDimBrightnessChanged: (Float) -> Unit,
    onBack: () -> Unit,
    onUserInteraction: () -> Unit
) {
    val scrollState = rememberScrollState()
    val timeoutOptions = remember { listOf(5, 10, 15, 20, 30) }

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
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(0.1f))
            }

            SettingsSection(title = stringResource(id = R.string.setting_dim_timeout_title)) {
                timeoutOptions.forEach { seconds ->
                    SettingRadioRow(
                        selected = state.dimTimeoutSeconds == seconds,
                        label = stringResource(id = R.string.setting_dim_timeout_option, seconds),
                        onSelected = {
                            onUserInteraction()
                            onDimTimeoutChanged(seconds)
                        }
                    )
                }
            }

            SettingsSection(title = stringResource(id = R.string.setting_tts_engine_title)) {
                PreferredTtsEngine.values().forEach { engine ->
                    SettingRadioRow(
                        selected = state.preferredTtsEngine == engine,
                        label = engine.toDisplayString(),
                        onSelected = {
                            onUserInteraction()
                            onPreferredEngineChanged(engine)
                        }
                    )
                }
            }

            SettingsSection(title = stringResource(id = R.string.setting_brightness_active_title)) {
                BrightnessSlider(
                    value = state.activeBrightness,
                    onValueChange = {
                        onUserInteraction()
                        onActiveBrightnessChanged(it)
                    }
                )
            }

            SettingsSection(title = stringResource(id = R.string.setting_brightness_dim_title)) {
                BrightnessSlider(
                    value = state.dimBrightness,
                    onValueChange = {
                        onUserInteraction()
                        onDimBrightnessChanged(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, color = Color.White, style = MaterialTheme.typography.titleLarge)
        Surface(color = Color(0x22FF0000), tonalElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingRadioRow(selected: Boolean, label: String, onSelected: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White)
        RadioButton(
            selected = selected,
            onClick = onSelected,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = RedSecondary, unselectedColor = Color.White)
        )
    }
}

@Composable
private fun BrightnessSlider(value: Float, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.0f..1.0f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = RedSecondary,
                activeTrackColor = RedSecondary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
        Text(
            text = String.format("%.2f", value),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun PreferredTtsEngine.toDisplayString(): String = when (this) {
    PreferredTtsEngine.Auto -> "Auto (Sherpa, fallback to Android)"
    PreferredTtsEngine.SherpaOnly -> "SherpaTTS only"
    PreferredTtsEngine.AndroidOnly -> "Android TextToSpeech"
}

@Preview
@Composable
private fun SettingsPreview() {
    SmartAlarmTheme {
        SettingsScreen(
            state = SettingsUiState(),
            onDimTimeoutChanged = {},
            onPreferredEngineChanged = {},
            onActiveBrightnessChanged = {},
            onDimBrightnessChanged = {},
            onBack = {},
            onUserInteraction = {}
        )
    }
}
