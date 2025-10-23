package com.smartalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.smartalarm.ui.BurnInPreventionManager
import com.smartalarm.ui.ScreenDimManager
import com.smartalarm.ui.SmartAlarmApp
import com.smartalarm.ui.theme.SmartAlarmTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val screenDimManager by lazy { ScreenDimManager(this) }
    private val burnInPreventionManager by lazy { BurnInPreventionManager(lifecycleScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            screenDimManager.isDimmed.collect { dimmed ->
                burnInPreventionManager.setDimmed(dimmed)
            }
        }
        setContent {
            SmartAlarmTheme {
                SmartAlarmApp(
                    screenDimManager = screenDimManager,
                    burnInPreventionManager = burnInPreventionManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        screenDimManager.start()
        burnInPreventionManager.start()
    }

    override fun onPause() {
        super.onPause()
        screenDimManager.stop()
        burnInPreventionManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        screenDimManager.cleanup()
        burnInPreventionManager.stop()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        screenDimManager.resetTimer()
    }
}
