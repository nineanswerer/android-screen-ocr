package com.screentextocr.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screentextocr.domain.model.RecognitionResult
import com.screentextocr.domain.repository.ScreenCaptureRepository
import com.screentextocr.presentation.viewmodel.ScreenCaptureState.Capturing
import com.screentextocr.presentation.viewmodel.ScreenCaptureState.Idle
import com.screentextocr.presentation.viewmodel.ScreenCaptureState.RequestingPermission
import com.screentextocr.presentation.viewmodel.ScreenCaptureState.StartingService
import com.screentextocr.service.foreground.ScreenCaptureForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for screen capture feature.
 */
sealed class ScreenCaptureState {
    object Idle : ScreenCaptureState()
    object RequestingPermission : ScreenCaptureState()
    object StartingService : ScreenCaptureState()
    object Capturing : ScreenCaptureState()
    data class Error(val message: String) : ScreenCaptureState()
    data class RecognizedText(val text: String) : ScreenCaptureState()
}

/**
 * ViewModel managing screen capture and text recognition.
 */
@HiltViewModel
class ScreenCaptureViewModel @Inject constructor(
    private val screenCaptureRepository: ScreenCaptureRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        const val REQUEST_CODE_SCREEN_CAPTURE = 1001
        private const val TAG = "ScreenCaptureVM"
    }

    private val _uiState = MutableStateFlow<ScreenCaptureState>(Idle)
    val uiState: StateFlow<ScreenCaptureState> = _uiState.asStateFlow()

    private val _recognizedTexts = mutableListOf<String>()
    val recognizedTexts: List<String> get() = _recognizedTexts

    init {
        startObservation()
    }

    /**
     * Handle result from permission request activity.
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                screenCaptureRepository.setMediaProjection(resultCode, data)
                startForegroundService()
            } else {
                _uiState.value = ScreenCaptureState.Error("Permission denied")
            }
        }
    }

    /**
     * Start the capture process.
     * 1. Request MediaProjection permission if not ready
     * 2. Start foreground service
     * 3. Begin capturing and recognizing
     */
    fun startCapture(activity: Activity) {
        viewModelScope.launch {
            if (!screenCaptureRepository.isReady()) {
                _uiState.value = RequestingPermission
                screenCaptureRepository.requestPermission(activity, REQUEST_CODE_SCREEN_CAPTURE)
            } else {
                _uiState.value = StartingService
                startForegroundService()
            }
        }
    }

    /**
     * Stop the capture process.
     */
    fun stopCapture() {
        viewModelScope.launch {
            screenCaptureRepository.stopCapture()
            _uiState.value = Idle
        }
    }

    private fun startForegroundService() {
        _uiState.value = Capturing
        // Start foreground service with application context
        ScreenCaptureForegroundService.startService(appContext)
        // The actual capture flow is started in the foreground service
    }

    private fun startObservation() {
        viewModelScope.launch {
            screenCaptureRepository.startCapture().collectLatest { result ->
                if (result.fullText.isNotBlank()) {
                    _recognizedTexts.add(0, result.fullText)
                    if (_recognizedTexts.size > 100) {
                        _recognizedTexts.removeAt(_recognizedTexts.lastIndex)
                    }
                    _uiState.value = ScreenCaptureState.RecognizedText(result.fullText)
                }
            }
        }
    }

    fun clearError() {
        if (_uiState.value is ScreenCaptureState.Error) {
            _uiState.value = Idle
        }
    }

    fun clearHistory() {
        _recognizedTexts.clear()
    }

    override fun onCleared() {
        super.onCleared()
        stopCapture()
    }
}
