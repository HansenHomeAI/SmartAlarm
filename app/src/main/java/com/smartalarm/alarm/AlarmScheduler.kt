package com.smartalarm.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.smartalarm.alarm.AlarmReceiver.Companion.ACTION_TRIGGER_ALARM
import com.smartalarm.alarm.AlarmReceiver.Companion.EXTRA_ALARM_IS_SNOOZE
import com.smartalarm.alarm.AlarmReceiver.Companion.EXTRA_ALARM_LABEL
import com.smartalarm.alarm.AlarmReceiver.Companion.EXTRA_ALARM_TRIGGER_AT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmScheduler(
    private val context: Context,
    private val repository: AlarmRepository,
    private val alarmManagerWrapper: AlarmManagerWrapper = AlarmManagerWrapper(context),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun scheduleAlarm(
        triggerAtMillis: Long,
        label: String? = null,
        isSnooze: Boolean = false,
        useAlarmClockUi: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    ) {
        require(triggerAtMillis > System.currentTimeMillis()) {
            "Alarm time must be in the future"
        }

        if (!canScheduleExactAlarms()) {
            throw IllegalStateException("Exact alarm permission is not granted")
        }

        val pendingIntent = createOrUpdatePendingIntent(
            triggerAtMillis = triggerAtMillis,
            label = label,
            isSnooze = isSnooze
        )

        withContext(ioDispatcher) {
            repository.saveAlarm(
                AlarmInfo(
                    triggerAtMillis = triggerAtMillis,
                    label = label,
                    isSnoozed = isSnooze
                )
            )
        }

        alarmManagerWrapper.scheduleExactAlarm(
            triggerAtMillis = triggerAtMillis,
            pendingIntent = pendingIntent,
            useAlarmClock = useAlarmClockUi
        )
    }

    suspend fun cancelAlarm() {
        withContext(ioDispatcher) {
            repository.clearAlarm()
        }

        val pendingIntent = getPendingIntent(flags = PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.let {
            alarmManagerWrapper.cancel(it)
            it.cancel()
        }
    }

    suspend fun currentAlarm(): AlarmInfo? = withContext(ioDispatcher) { repository.currentAlarm() }

    fun canScheduleExactAlarms(): Boolean = alarmManagerWrapper.canScheduleExactAlarms()

    private fun createOrUpdatePendingIntent(
        triggerAtMillis: Long,
        label: String?,
        isSnooze: Boolean
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_TRIGGER_AT, triggerAtMillis)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_ALARM_IS_SNOOZE, isSnooze)
        }

        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getPendingIntent(flags: Int): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply { action = ACTION_TRIGGER_ALARM }
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        @VisibleForTesting
        internal const val ALARM_REQUEST_CODE = 1201
    }
}
