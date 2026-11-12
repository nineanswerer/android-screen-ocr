package com.screentextocr.service.recognition

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manager for text recognition using Google ML Kit.
 * Handles image processing and text extraction.
 */
interface TextRecognitionManager {

    /**
     * Recognize text in the given bitmap.
     * @param bitmap Input image bitmap
     * @return Result containing Text object or error
     */
    suspend fun recognizeText(bitmap: Bitmap): Result<Text>

    /**
     * Extract text that appears in the center region of the recognized text.
     * This is a heuristic based on bounding box positions.
     * @param text Full recognized text
     * @param imageWidth Width of the source image
     * @param imageHeight Height of the source image
     * @return Text found in the center region
     */
    fun getCenterText(text: Text, imageWidth: Int, imageHeight: Int): String
}

/**
 * Implementation of TextRecognitionManager using ML Kit.
 */
class TextRecognitionManagerImpl(
    private val textRecognizer: TextRecognizer,
    private val ioDispatcher: CoroutineDispatcher
) : TextRecognitionManager {

    override suspend fun recognizeText(bitmap: Bitmap): Result<Text> =
        withContext(ioDispatcher) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = suspendCancellableCoroutine<Text> { continuation ->
                    textRecognizer.process(image)
                        .addOnSuccessListener { recognizedText ->
                            continuation.resume(recognizedText) {}
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun getCenterText(text: Text, imageWidth: Int, imageHeight: Int): String {
        val centerX = imageWidth * 0.5f
        val centerY = imageHeight * 0.5f
        val centerRadius = (imageWidth * 0.3f).coerceAtLeast(imageHeight * 0.3f)

        val centerBlocks = mutableListOf<String>()

        text.textBlocks.forEach { block ->
            val blockBounds = block.boundingBox
            val blockCenterX = blockBounds.centerX()
            val blockCenterY = blockBounds.centerY()

            // Check if block is in center region
            val distance = Math.sqrt(
                Math.pow((blockCenterX - centerX).toDouble(), 2.0) +
                        Math.pow((blockCenterY - centerY).toDouble(), 2.0)
            )

            if (distance <= centerRadius) {
                centerBlocks.add(block.text)
            }
        }

        return centerBlocks.joinToString("\n")
    }

    // Extension to calculate center of Rect
    private val android.graphics.Rect.centerX: Int
        get() = (left + right) / 2

    private val android.graphics.Rect.centerY: Int
        get() = (top + bottom) / 2
}
