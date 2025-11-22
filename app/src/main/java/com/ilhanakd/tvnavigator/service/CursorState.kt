package com.ilhanakd.tvnavigator.service

import android.content.Context
import android.util.Log
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

object CursorState {

    private const val TAG = "CursorState"
    private const val STEP_PX = 25
    private const val CURSOR_DIAMETER_DP = 28

    private val _cursorPosition = MutableStateFlow(IntOffset.Zero)
    val cursorPosition: StateFlow<IntOffset> = _cursorPosition

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var cursorSizePx: Int = 0
    private var service: CursorService? = null

    fun bindService(service: CursorService) {
        this.service = service
        calculateCursorSize(service)
    }

    fun unbindService() {
        service = null
    }

    fun initialize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Skipping cursor initialization because screen bounds are invalid: ${'$'}width x ${'$'}height")
            return
        }
        screenWidth = width
        screenHeight = height
        val startX = (width - cursorSizePx) / 2
        val startY = (height - cursorSizePx) / 2
        _cursorPosition.value = IntOffset(startX, startY)
    }

    private fun calculateCursorSize(context: Context) {
        val density = context.resources.displayMetrics.density
        cursorSizePx = (CURSOR_DIAMETER_DP * density).roundToInt().coerceAtLeast(12)
    }

    fun move(dx: Int, dy: Int) {
        if (screenWidth == 0 || screenHeight == 0) {
            Log.w(TAG, "Ignoring move request because screen bounds are not initialized")
            return
        }
        val current = _cursorPosition.value
        val newX = (current.x + dx * STEP_PX).coerceIn(0, maxOf(screenWidth - cursorSizePx, 0))
        val newY = (current.y + dy * STEP_PX).coerceIn(0, maxOf(screenHeight - cursorSizePx, 0))
        _cursorPosition.value = IntOffset(newX, newY)
    }

    fun performClick() {
        val serviceInstance = CursorAccessibilityService.instance
        val current = _cursorPosition.value
        val x = (current.x + cursorSizePx / 2f)
        val y = (current.y + cursorSizePx / 2f)
        if (serviceInstance != null) {
            serviceInstance.simulateTouch(x, y)
        } else {
            Log.w(TAG, "Accessibility service not connected; cannot perform click")
        }
    }

    fun onBackPressed() {
        val currentService = service
        if (currentService != null) {
            currentService.stopSelf()
            CursorService.requestAppExit(currentService)
        } else {
            Log.w(TAG, "Cursor service not bound; cannot stop")
        }
    }
}
