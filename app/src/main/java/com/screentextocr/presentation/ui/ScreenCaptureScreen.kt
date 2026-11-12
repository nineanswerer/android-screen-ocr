package com.screentextocr.presentation.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.fontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.screentextocr.presentation.viewmodel.ScreenCaptureState
import com.screentextocr.presentation.viewmodel.ScreenCaptureViewModel
import kotlinx.coroutines.launch

/**
 * Main UI screen for screen text OCR application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCaptureScreen(
    viewModel: ScreenCaptureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recognizedTexts = viewModel.recognizedTexts

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle permission request callback
    DisposableEffect(Unit) {
        onDispose { }
    }

    LaunchedEffect(Unit) {
        // Check if we need to handle result from permission request
        // This would need to be wired up in Activity's onActivityResult
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Text OCR") },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear History")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when (uiState) {
                is ScreenCaptureState.Idle,
                is ScreenCaptureState.Error -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            activity?.let {
                                viewModel.startCapture(it)
                            } ?: run {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Activity not available")
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        text = { Text("Start Capture") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                is ScreenCaptureState.Capturing,
                is ScreenCaptureState.StartingService -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.stopCapture() },
                        icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                        text = { Text("Stop") },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                }
                else -> { /* Do nothing */ }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState) {
                        is ScreenCaptureState.Capturing -> MaterialTheme.colorScheme.primaryContainer
                        is ScreenCaptureState.Error -> MaterialTheme.colorScheme.errorContainer
                        is ScreenCaptureState.RequestingPermission -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = when (uiState) {
                        is ScreenCaptureState.Idle -> "Ready to capture"
                        is ScreenCaptureState.RequestingPermission -> "Requesting permission..."
                        is ScreenCaptureState.StartingService -> "Starting service..."
                        is ScreenCaptureState.Capturing -> "Capturing screen..."
                        is ScreenCaptureState.Error -> (uiState as ScreenCaptureState.Error).message
                        is ScreenCaptureState.RecognizedText -> "Text detected!"
                    },
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Latest recognized text
            if (uiState is ScreenCaptureState.RecognizedText) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Latest Recognition",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as ScreenCaptureState.RecognizedText).text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // History list
            if (recognizedTexts.isNotEmpty()) {
                Text(
                    text = "Recognition History (${recognizedTexts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recognizedTexts) { text ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else if (!(uiState is ScreenCaptureState.Capturing)) {
                Text(
                    text = "No text recognized yet.\nTap Start Capture to begin.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }

            // Loading indicator
            if (uiState is ScreenCaptureState.Capturing) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Processing...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
