package com.screentextocr.domain.usecase

import android.graphics.Bitmap
import com.screentextocr.service.screencapture.ScreenCaptureManager
import kotlinx.coroutines.flow.Flow

/**
 * Use case for capturing screen bitmap.
 */
class CaptureScreenUseCase(
    private val screenCaptureManager: ScreenCaptureManager
) {

    operator fun invoke(): Flow<Bitmap?> = screenCaptureManager.startCapture()
}
