package com.smartalarm.ui

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.smartalarm.R
import com.smartalarm.alarm.AlarmRepository
import com.smartalarm.alarm.AlarmScheduler
import com.smartalarm.alarm.SnoozeManager
import com.smartalarm.alarm.AlarmReceiver
import com.smartalarm.MainActivity
import com.smartalarm.ui.theme.SmartAlarmTheme
import kotlinx.coroutines.launch

data class AlarmUiState(
    val triggerAtMillis: Long = System.currentTimeMillis(),
    val label: String? = null,
    val isSnooze: Boolean = false
)

class AlarmTriggerActivity : ComponentActivity() {

    private val repository by lazy { AlarmRepository(applicationContext) }
    private val scheduler by lazy { AlarmScheduler(applicationContext, repository) }
    private val snoozeManager by lazy { SnoozeManager(scheduler, repository) }

    private val alarmState = mutableStateOf(AlarmUiState())
    private val processingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateFromIntent(intent)
        configureWindowForAlarm()

        setContent {
            SmartAlarmTheme(darkTheme = true) {
                AlarmTriggerScreen(
                    state = alarmState.value,
                    isProcessing = processingState.value,
                    onSnooze = { handleSnooze() },
                    onDismiss = { handleDismiss() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        updateFromIntent(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }

    private fun configureWindowForAlarm() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        enableImmersiveMode()
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }

    private fun updateFromIntent(intent: Intent) {
        val action = intent.action
        if (action != null && action != AlarmReceiver.ACTION_TRIGGER_ALARM) {
            Log.w(TAG, "Ignoring intent with unexpected action: $action")
            return
        }

        val triggerAt = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_TRIGGER_AT, System.currentTimeMillis())
        val label = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL)
        val isSnooze = intent.getBooleanExtra(AlarmReceiver.EXTRA_ALARM_IS_SNOOZE, false)

        alarmState.value = AlarmUiState(triggerAtMillis = triggerAt, label = label, isSnooze = isSnooze)
    }

    private fun handleDismiss() {
        if (processingState.value) return
        processingState.value = true
        lifecycleScope.launch {
            runCatching { scheduler.cancelAlarm() }
                .onFailure { error ->
                    Log.e(TAG, "Failed to dismiss alarm", error)
                    showToast(R.string.alarm_error_dismiss)
                    processingState.value = false
                }
                .onSuccess {
                    navigateHome()
                }
        }
    }

    private fun handleSnooze() {
        if (processingState.value) return
        processingState.value = true
        lifecycleScope.launch {
            runCatching { snoozeManager.snooze() }
                .onFailure { error ->
                    Log.e(TAG, "Failed to snooze alarm", error)
                    showToast(R.string.alarm_error_snooze)
                    processingState.value = false
                }
                .onSuccess {
                    navigateHome()
                }
        }
    }

    private fun navigateHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addCategory(Intent.CATEGORY_DEFAULT)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finishAndRemoveTaskSafely()
    }

    private fun finishAndRemoveTaskSafely() {
        if (!isFinishing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                finish()
            }
        }
    }

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "AlarmTriggerActivity"
    }
}
