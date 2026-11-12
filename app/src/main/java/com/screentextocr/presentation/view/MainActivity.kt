package com.screentextocr.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.screentextocr.presentation.ui.ScreenCaptureScreen
import com.screentextocr.presentation.viewmodel.ScreenCaptureViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity hosting the Compose UI.
 * Handles MediaProjection permission result and delegates to ViewModel.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ScreenCaptureViewModel

    // Activity Result API for MediaProjection permission
    private val mediaProjectionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleActivityResult(
            ScreenCaptureViewModel.REQUEST_CODE_SCREEN_CAPTURE,
            result.resultCode,
            result.data
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = if (darkTheme) {
                    darkColorScheme()
                } else {
                    lightColorScheme()
                }
            ) {
                ScreenCaptureScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Let ViewModel handle cleanup via onCleared
    }
}
