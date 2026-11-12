package com.screentextocr.domain.repository

import android.graphics.Bitmap
import com.screentextocr.domain.model.RecognitionResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing screen capture and text recognition operations.
 */
interface ScreenCaptureRepository {

    /**
     * Check if screen capture is ready.
     */
    fun isReady(): Boolean

    /**
     * Start capturing screen and performing text recognition.
     * Emits RecognitionResult each time a successful capture and recognition occurs.
     */
    fun startCapture(): Flow<RecognitionResult>

    /**
     * Stop the capture process.
     */
    suspend fun stopCapture()

    /**
     * Request MediaProjection permission.
     */
    fun requestPermission(activity: android.app.Activity, requestCode: Int)

    /**
     * Set MediaProjection after permission is granted.
     */
    fun setMediaProjection(resultCode: Int, resultData: android.content.Intent?)
}
