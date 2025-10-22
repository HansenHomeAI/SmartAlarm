package com.smartalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smartalarm.ui.AlarmTriggerActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != ACTION_TRIGGER_ALARM) {
            return
        }

        val triggerAt = intent.getLongExtra(EXTRA_ALARM_TRIGGER_AT, System.currentTimeMillis())
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL)
        val isSnooze = intent.getBooleanExtra(EXTRA_ALARM_IS_SNOOZE, false)

        Log.i(TAG, "Alarm fired at $triggerAt (snooze=$isSnooze)")

        val launchIntent = Intent(context, AlarmTriggerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALARM_TRIGGER_AT, triggerAt)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_ALARM_IS_SNOOZE, isSnooze)
        }
        context.startActivity(launchIntent)
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.smartalarm.action.TRIGGER_ALARM"
        const val EXTRA_ALARM_TRIGGER_AT = "extra_alarm_trigger_at"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_ALARM_IS_SNOOZE = "extra_alarm_is_snooze"
        private const val TAG = "SmartAlarmReceiver"
    }
}
