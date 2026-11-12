package com.screentextocr.data.repository

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import com.screentextocr.domain.model.RecognitionResult
import com.screentextocr.domain.repository.ScreenCaptureRepository
import com.screentextocr.domain.usecase.CaptureAndRecognizeUseCase
import com.screentextocr.service.screencapture.ScreenCaptureManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository implementation that coordinates screen capture and text recognition.
 * Injects managers and use cases via Hilt.
 */
class ScreenCaptureRepositoryImpl @Inject constructor(
    private val screenCaptureManager: ScreenCaptureManager,
    private val captureAndRecognizeUseCase: CaptureAndRecognizeUseCase
) : ScreenCaptureRepository {

    override fun isReady(): Boolean = screenCaptureManager.isReady()

    override fun startCapture(): Flow<RecognitionResult> = captureAndRecognizeUseCase()

    override suspend fun stopCapture() {
        screenCaptureManager.stopCapture()
    }

    override fun requestPermission(activity: Activity, requestCode: Int) {
        screenCaptureManager.requestMediaProjection(activity, requestCode)
    }

    override fun setMediaProjection(resultCode: Int, resultData: Intent?) {
        screenCaptureManager.setMediaProjection(resultCode, resultData)
    }
}
