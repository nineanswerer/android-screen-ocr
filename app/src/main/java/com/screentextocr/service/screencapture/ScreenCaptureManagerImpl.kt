package com.screentextocr.service.screencapture

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Implementation of ScreenCaptureManager using MediaProjection API.
 * Handles API level differences for API 31+ (Android 12+).
 */
class ScreenCaptureManagerImpl(
    private val mediaProjectionManager: MediaProjectionManager,
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ScreenCaptureManager {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val _bitmapFlow = MutableSharedFlow<Bitmap?>(replay = 0, extraBufferCapacity = 1)
    override val bitmapFlow: Flow<Bitmap?> = _bitmapFlow.asSharedFlow()

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var screenDensity: Int = 0

    companion object {
        private const val CAPTURE_INTERVAL_MS = 2000L // Capture every 2 seconds
    }

    override fun requestMediaProjection(activity: Activity, requestCode: Int) {
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(permissionIntent, requestCode)
    }

    override fun setMediaProjection(resultCode: Int, resultData: android.content.Intent?) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
            initializeDisplayMetrics()
        } else {
            mediaProjection = null
        }
    }

    private fun initializeDisplayMetrics() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
    }

    override fun startCapture(): Flow<Bitmap?> {
        startVirtualDisplay()
        return bitmapFlow
    }

    private fun startVirtualDisplay() {
        mediaProjection?.let { projection ->
            // For API 31+, use RGBA_8888 for better quality
            val pixelFormat = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                PixelFormat.RGBA_8888
            } else {
                PixelFormat.RGBA_8888 // Still works on older versions
            }

            imageReader = ImageReader.newInstance(
                screenWidth,
                screenHeight,
                pixelFormat,
                2 // Max images
            ).apply {
                setOnImageAvailableListener({ reader ->
                    val image = reader.acquireLatestImage()
                    image?.let { processImage(it) }
                }, handler)
            }

            virtualDisplay = projection.createVirtualDisplay(
                "ScreenCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )
        } ?: throw IllegalStateException("MediaProjection not initialized. Call setMediaProjection first.")
    }

    private fun processImage(image: Image) {
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth

            // Create bitmap with padding
            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            // Emit the bitmap
            _bitmapFlow.tryEmit(bitmap)
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        } finally {
            image.close()
        }
    }

    override suspend fun stopCapture() = withContext(ioDispatcher) {
        releaseResources()
    }

    private fun releaseResources() {
        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.close()
        imageReader = null

        mediaProjection?.stop()
        mediaProjection = null
    }

    override suspend fun getCenterRegion(bitmap: Bitmap, centerRatio: Double): Bitmap =
        withContext(ioDispatcher) {
            val centerX = (bitmap.width * 0.5).toInt()
            val centerY = (bitmap.height * 0.5).toInt()
            val cropWidth = (bitmap.width * centerRatio).toInt()
            val cropHeight = (bitmap.height * centerRatio).toInt()

            val left = (centerX - cropWidth / 2).coerceAtLeast(0)
            val top = (centerY - cropHeight / 2).coerceAtLeast(0)
            val width = cropWidth.coerceAtMost(bitmap.width - left)
            val height = cropHeight.coerceAtMost(bitmap.height - top)

            Bitmap.createBitmap(bitmap, left, top, width, height)
        }

    override fun isReady(): Boolean = mediaProjection != null && virtualDisplay != null

    override fun release() {
        releaseResources()
        _bitmapFlow.resetReplayCache()
    }
}
