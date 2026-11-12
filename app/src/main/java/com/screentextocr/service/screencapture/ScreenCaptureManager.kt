package com.screentextocr.service.screencapture

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

/**
 * Manages screen capture using MediaProjection API.
 * Handles permission request, virtual display creation, and bitmap capture.
 */
interface ScreenCaptureManager {

    /**
     * Request MediaProjection permission from the user.
     * This should be called from an Activity.
     */
    fun requestMediaProjection(activity: Activity, requestCode: Int)

    /**
     * Set the MediaProjection object after permission is granted.
     * Should be called from Activity's onActivityResult.
     */
    fun setMediaProjection(resultCode: Int, resultData: android.content.Intent?)

    /**
     * Start capturing screen and emit bitmaps through the flow.
     * Returns a flow of captured bitmaps.
     */
    fun startCapture(): Flow<Bitmap?>

    /**
     * Stop capturing screen and release resources.
     */
    suspend fun stopCapture()

    /**
     * Extract the center region from a bitmap.
     * @param bitmap Source bitmap
     * @param centerRatio Ratio of center region (default 0.6 = 60%)
     * @return Cropped bitmap containing center region
     */
    suspend fun getCenterRegion(bitmap: Bitmap, centerRatio: Double = 0.6): Bitmap

    /**
     * Check if MediaProjection is available and ready.
     */
    fun isReady(): Boolean

    /**
     * Release all resources. Should be called when manager is no longer needed.
     */
    fun release()
}
