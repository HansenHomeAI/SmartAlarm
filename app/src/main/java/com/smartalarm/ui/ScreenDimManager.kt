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
    private var dimDelayMs: Long = DEFAULT_DIM_DELAY_MS
    private var activeBrightness: Float = DEFAULT_ACTIVE_BRIGHTNESS
    private var dimBrightness: Float = DEFAULT_DIM_BRIGHTNESS

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
            setBrightness(activeBrightness)
            _isDimmed.value = false
        } else {
            setBrightness(activeBrightness)
        }
        handler.postDelayed(dimRunnable, dimDelayMs)
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
        setBrightness(dimBrightness)
        _isDimmed.value = true
    }

    private fun setBrightness(value: Float) {
        activity.runOnUiThread {
            val params = activity.window.attributes
            params.screenBrightness = value
            activity.window.attributes = params
        }
    }

    fun updateConfiguration(dimDelaySeconds: Int, active: Float, dim: Float) {
        dimDelayMs = dimDelaySeconds * 1000L
        activeBrightness = active.coerceIn(0.0f, 1.0f)
        dimBrightness = dim.coerceIn(0.0f, 1.0f)
        if (started) {
            resetTimer()
        }
    }

    companion object {
        private const val DEFAULT_DIM_DELAY_MS = 10_000L
        private const val DEFAULT_DIM_BRIGHTNESS = 0.02f
        private const val DEFAULT_ACTIVE_BRIGHTNESS = 0.3f
    }
}
