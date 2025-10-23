package com.smartalarm.ui

import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class BurnInPreventionManager(
    private val scope: CoroutineScope,
    private val intervalMillis: Long = FIVE_MINUTES_MS,
    private val maxOffsetPx: Int = DEFAULT_MAX_OFFSET_PX
) {

    private val random = Random(System.currentTimeMillis())
    private val _offset = MutableStateFlow(IntOffset.Zero)
    val offset: StateFlow<IntOffset> = _offset

    private var job: Job? = null
    private var dimmed = false

    fun start() {
        if (job != null) return
        job = scope.launch {
            while (isActive) {
                delay(intervalMillis)
                if (!dimmed) {
                    _offset.value = nextOffset()
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _offset.value = IntOffset.Zero
    }

    fun setDimmed(isDimmed: Boolean) {
        dimmed = isDimmed
        if (isDimmed) {
            _offset.value = IntOffset.Zero
        }
    }

    private fun nextOffset(): IntOffset {
        val dx = random.nextInt(-maxOffsetPx, maxOffsetPx + 1)
        val dy = random.nextInt(-maxOffsetPx, maxOffsetPx + 1)
        return IntOffset(dx, dy)
    }

    companion object {
        private const val FIVE_MINUTES_MS = 5 * 60 * 1000L
        private const val DEFAULT_MAX_OFFSET_PX = 5
    }
}
