package com.smartalarm.ui

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScreenDimManager(private val activity: Activity) {

    private val handler = Handler(Looper.getMainLooper())
    private val dimRunnable = Runnable { applyDim() }

    private val _isDimmed = MutableStateFlow(false)
    val isDimmed: StateFlow<Boolean> = _isDimmed

    private var started = false
    private var originalBrightness: Float = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

    fun start() {
        if (started) return
        started = true
        originalBrightness = activity.window.attributes.screenBrightness
        resetTimer()
    }

    fun resetTimer() {
        if (!started) return
        handler.removeCallbacks(dimRunnable)
        if (_isDimmed.value) {
            setBrightness(ACTIVE_BRIGHTNESS)
            _isDimmed.value = false
        }
        handler.postDelayed(dimRunnable, AUTO_DIM_DELAY_MS)
    }

    fun stop() {
        if (!started) return
        handler.removeCallbacks(dimRunnable)
        setBrightness(originalBrightness)
        _isDimmed.value = false
        started = false
    }

    fun cleanup() {
        stop()
    }

    private fun applyDim() {
        if (!started) return
        setBrightness(DIM_BRIGHTNESS)
        _isDimmed.value = true
    }

    private fun setBrightness(value: Float) {
        activity.runOnUiThread {
            val params = activity.window.attributes
            params.screenBrightness = value
            activity.window.attributes = params
        }
    }

    companion object {
        private const val AUTO_DIM_DELAY_MS = 10_000L
        private const val DIM_BRIGHTNESS = 0.02f
        private const val ACTIVE_BRIGHTNESS = 0.3f
    }
}
