package com.screentextocr.domain.usecase

import android.graphics.Bitmap
import com.screentextocr.service.recognition.TextRecognitionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Use case for recognizing text in the center region of a bitmap.
 */
class RecognizeCenterTextUseCase(
    private val textRecognitionManager: TextRecognitionManager
) {

    private val _recognitionResult = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val recognitionResult = _recognitionResult.asSharedFlow()

    private var isProcessing = AtomicBoolean(false)

    suspend operator fun invoke(bitmap: Bitmap): Result<String> {
        // Prevent concurrent processing
        if (!isProcessing.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("Already processing"))
        }

        return try {
            val result = textRecognitionManager.recognizeText(bitmap)
            result.fold(
                onSuccess = { text ->
                    val centerText = textRecognitionManager.getCenterText(text, bitmap.width, bitmap.height)
                    _recognitionResult.emit(centerText)
                    Result.success(centerText)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } finally {
            isProcessing.set(false)
        }
    }
}
