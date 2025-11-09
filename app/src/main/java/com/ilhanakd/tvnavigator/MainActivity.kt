package com.ilhanakd.tvnavigator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ilhanakd.tvnavigator.service.CursorService
import com.ilhanakd.tvnavigator.ui.theme.TvMouseNavigatorTheme

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                ensureAccessibilityEnabled()
                startCursorService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvMouseNavigatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ControlScreen(
                        onRequestOverlay = { requestOverlayPermission() },
                        onStartService = {
                            if (ensureAccessibilityEnabled()) {
                                startCursorService()
                            }
                        },
                        onStopService = { stopCursorService() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(EXTRA_EXIT, false) == true) {
            stopCursorService()
            finish()
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun ensureAccessibilityEnabled(): Boolean {
        val isEnabled = CursorService.isAccessibilityEnabled(this)
        if (!isEnabled) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        return isEnabled
    }

    private fun startCursorService() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        val intent = Intent(this, CursorService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopCursorService() {
        val intent = Intent(this, CursorService::class.java)
        stopService(intent)
    }

    companion object {
        const val EXTRA_EXIT = "com.ilhanakd.tvnavigator.EXTRA_EXIT"
    }
}

@Composable
private fun ControlScreen(
    onRequestOverlay: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val context = LocalContext.current
    val overlayGranted = Settings.canDrawOverlays(context)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            if (Settings.canDrawOverlays(context)) {
                onStartService()
            } else {
                onRequestOverlay()
            }
        }) {
            Text(text = stringResource(id = R.string.start_service))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStopService) {
            Text(text = stringResource(id = R.string.stop_service))
        }
        if (!overlayGranted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.overlay_permission_rationale), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.accessibility_enable_instructions),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
