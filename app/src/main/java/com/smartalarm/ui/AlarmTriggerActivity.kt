package com.smartalarm.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.smartalarm.ui.theme.SmartAlarmTheme

/**
 * Temporary placeholder that will be replaced with the full-screen alarm UI.
 */
class AlarmTriggerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartAlarmTheme {
                Surface { }
            }
        }
    }
}
