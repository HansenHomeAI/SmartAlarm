package com.smartalarm.alarm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SnoozeManager(
    private val alarmScheduler: AlarmScheduler,
    private val alarmRepository: AlarmRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun snooze(currentTimeMillis: Long = System.currentTimeMillis()) {
        val existingAlarm = withContext(ioDispatcher) { alarmRepository.currentAlarm() }
        val label = existingAlarm?.label

        alarmScheduler.cancelAlarm()

        val nextTrigger = currentTimeMillis + SNOOZE_INTERVAL_MILLIS
        alarmScheduler.scheduleAlarm(
            triggerAtMillis = nextTrigger,
            label = label,
            isSnooze = true
        )
    }

    companion object {
        private const val SNOOZE_INTERVAL_MILLIS = 9 * 60 * 1000L
    }
}
