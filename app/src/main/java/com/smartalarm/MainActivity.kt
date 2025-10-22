package com.smartalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smartalarm.ui.SmartAlarmApp
import com.smartalarm.ui.theme.SmartAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartAlarmTheme {
                SmartAlarmApp()
            }
        }
    }
}
