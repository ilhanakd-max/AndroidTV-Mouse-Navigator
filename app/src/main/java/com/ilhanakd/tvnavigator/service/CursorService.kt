package com.ilhanakd.tvnavigator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.ilhanakd.tvnavigator.MainActivity
import com.ilhanakd.tvnavigator.R

class CursorService : Service() {

    private var windowManager: WindowManager? = null
    private var cursorView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        CursorState.bindService(this)
        setupWindow()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCursor()
        CursorState.unbindService()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setupWindow() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val composeView = ComposeView(this).apply {
            setContent {
                val positionState = CursorState.cursorPosition.collectAsState()
                CursorOverlay(positionState.value)
            }
        }

        cursorView = composeView

        val metrics = calculateDisplaySize(wm)
        if (metrics != null) {
            CursorState.initialize(metrics.width, metrics.height)
        } else {
            Log.w(TAG, "Unable to fetch display metrics; cursor may not move correctly")
        }

        wm.addView(composeView, layoutParams)
    }

    private fun removeCursor() {
        cursorView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Cursor view was not attached to window manager", exception)
            }
        }
        cursorView = null
        windowManager = null
    }

    private fun calculateDisplaySize(windowManager: WindowManager): Size? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars() or
                    WindowInsets.Type.statusBars() or
                    WindowInsets.Type.displayCutout()
            )
            val width = bounds.width() - insets.left - insets.right
            val height = bounds.height() - insets.top - insets.bottom
            if (width > 0 && height > 0) Size(width, height) else null
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay ?: return null
            val displayMetrics = DisplayMetrics()
            display.getRealMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            if (width > 0 && height > 0) Size(width, height) else null
        }
    }

    private fun createNotification(): Notification {
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.start_service))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "cursor_service_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "CursorService"

        fun isAccessibilityEnabled(context: Context): Boolean {
            return CursorAccessibilityService.isServiceEnabled(context)
        }

        fun requestAppExit(context: Context) {
            val exitIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(MainActivity.EXTRA_EXIT, true)
            }
            context.startActivity(exitIntent)
        }
    }
}
