package com.smartalarm.alarm

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

private val Context.alarmDataStore by preferencesDataStore(name = "alarm_prefs")

class AlarmRepository(private val context: Context) {

    private val dataStore = context.alarmDataStore

    val alarmFlow: Flow<AlarmInfo?> = dataStore.data.map { preferences ->
        preferences.toAlarmInfo()
    }

    suspend fun currentAlarm(): AlarmInfo? = alarmFlow.firstOrNull()

    suspend fun saveAlarm(info: AlarmInfo) {
        dataStore.edit { preferences ->
            preferences[Keys.triggerAtMillis] = info.triggerAtMillis
            preferences[Keys.label] = info.label ?: ""
            preferences[Keys.isSnoozed] = info.isSnoozed
        }
    }

    suspend fun clearAlarm() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.triggerAtMillis)
            preferences.remove(Keys.label)
            preferences.remove(Keys.isSnoozed)
        }
    }

    private object Keys {
        val triggerAtMillis: Preferences.Key<Long> = longPreferencesKey("alarm_trigger_at")
        val label: Preferences.Key<String> = stringPreferencesKey("alarm_label")
        val isSnoozed: Preferences.Key<Boolean> = booleanPreferencesKey("alarm_is_snoozed")
    }

    private fun Preferences.toAlarmInfo(): AlarmInfo? {
        val trigger = this[Keys.triggerAtMillis] ?: return null
        val labelValue = this[Keys.label]?.takeIf { it.isNotBlank() }
        val snoozed = this[Keys.isSnoozed] ?: false
        return AlarmInfo(triggerAtMillis = trigger, label = labelValue, isSnoozed = snoozed)
    }
}

data class AlarmInfo(
    val triggerAtMillis: Long,
    val label: String?,
    val isSnoozed: Boolean
) {
    companion object {
        fun computeNextTriggerMillis(hour: Int, minute: Int): Long {
            val now = java.time.ZonedDateTime.now()
            var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (target.isBefore(now) || target == now) {
                target = target.plusDays(1)
            }
            return target.toInstant().toEpochMilli()
        }
    }
}
