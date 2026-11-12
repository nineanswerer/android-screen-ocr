package com.screentextocr.domain.usecase

import android.graphics.Bitmap
import com.screentextocr.domain.model.RecognitionResult
import com.screentextocr.domain.repository.ScreenCaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Composite use case that combines screen capture and text recognition.
 * This is the main entry point for the application.
 */
class CaptureAndRecognizeUseCase(
    private val captureScreenUseCase: CaptureScreenUseCase,
    private val recognizeCenterTextUseCase: RecognizeCenterTextUseCase
) {

    operator fun invoke(): Flow<RecognitionResult> = flow {
        val captureFlow = captureScreenUseCase()

        // Process each captured bitmap
        captureFlow.collect { bitmap ->
            bitmap?.let { bmp ->
                val result = recognizeCenterTextUseCase(bmp)
                result.fold(
                    onSuccess = { text ->
                        if (text.isNotBlank()) {
                            emit(
                                RecognitionResult(
                                    fullText = text,
                                    confidence = 1.0f
                                )
                            )
                        }
                    },
                    onFailure = { /* Log error */ }
                )
            }
        }
    }
}
