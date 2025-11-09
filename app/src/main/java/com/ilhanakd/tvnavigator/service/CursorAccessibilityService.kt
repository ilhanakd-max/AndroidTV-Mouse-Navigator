package com.ilhanakd.tvnavigator.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Path
import android.hardware.input.InputManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

class CursorAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used but required override
    }

    override fun onInterrupt() {
        // Not used but required override
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.onKeyEvent(event)
        }
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                CursorState.move(0, -1)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                CursorState.move(0, 1)
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                CursorState.move(-1, 0)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                CursorState.move(1, 0)
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                CursorState.performClick()
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                CursorState.onBackPressed()
                true
            }
            else -> super.onKeyEvent(event)
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    fun simulateTouch(x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        val upEvent = MotionEvent.obtain(downTime, downTime + 50, MotionEvent.ACTION_UP, x, y, 0)
        try {
            val inputManager = InputManager::class.java.getDeclaredMethod("getInstance").invoke(null) as InputManager
            val injectMethod = InputManager::class.java.getDeclaredMethod(
                "injectInputEvent",
                android.view.InputEvent::class.java,
                Int::class.javaPrimitiveType
            )
            injectMethod.isAccessible = true
            injectMethod.invoke(inputManager, downEvent, 0)
            injectMethod.invoke(inputManager, upEvent, 0)
        } catch (throwable: Throwable) {
            Log.w(TAG, "Falling back to gesture dispatch", throwable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val path = Path().apply {
                    moveTo(x, y)
                    lineTo(x, y)
                }
                val stroke = android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 100)
                val gesture = android.accessibilityservice.GestureDescription.Builder()
                    .addStroke(stroke)
                    .build()
                dispatchGesture(gesture, null, null)
            }
        } finally {
            downEvent.recycle()
            upEvent.recycle()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        private const val TAG = "CursorAccessibility"
        var instance: CursorAccessibilityService? = null
            private set

        fun isServiceEnabled(context: Context): Boolean {
            val accessibilityManager = context.getSystemService<AccessibilityManager>() ?: return false
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            return enabledServices.any { serviceInfo ->
                val info = serviceInfo.resolveInfo?.serviceInfo
                info != null && info.packageName == context.packageName &&
                    info.name == CursorAccessibilityService::class.qualifiedName
            }
        }
    }
}
