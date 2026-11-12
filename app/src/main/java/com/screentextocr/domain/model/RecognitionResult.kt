package com.screentextocr.domain.model

/**
 * Domain model representing a text recognition result.
 */
data class RecognitionResult(
    val fullText: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
