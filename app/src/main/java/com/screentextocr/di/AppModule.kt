package com.screentextocr.di

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.core.content.getSystemService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.screentextocr.service.screencapture.ScreenCaptureManager
import com.screentextocr.service.screencapture.ScreenCaptureManagerImpl
import com.screentextocr.service.recognition.TextRecognitionManager
import com.screentextocr.service.recognition.TextRecognitionManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMediaProjectionManager(
        @ApplicationContext context: Context
    ): MediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    @Provides
    @Singleton
    fun provideTextRecognizer(): TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @Provides
    @Singleton
    fun provideIODispatcher(): ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Provides
    @Singleton
    fun provideMainDispatcher(): ExecutorCoroutineDispatcher =
        Dispatchers.Main

    @Provides
    @Singleton
    fun provideScreenCaptureManager(
        mediaProjectionManager: MediaProjectionManager,
        @ApplicationContext context: Context,
        ioDispatcher: ExecutorCoroutineDispatcher
    ): ScreenCaptureManager =
        ScreenCaptureManagerImpl(
            mediaProjectionManager,
            context,
            ioDispatcher
        )

    @Provides
    @Singleton
    fun provideTextRecognitionManager(
        textRecognizer: TextRecognizer,
        ioDispatcher: ExecutorCoroutineDispatcher
    ): TextRecognitionManager =
        TextRecognitionManagerImpl(
            textRecognizer,
            ioDispatcher
        )

    @Provides
    fun provideInputImage(
        @ApplicationContext context: Context
    ): (Bitmap) -> InputImage = { bitmap ->
        InputImage.fromBitmap(bitmap, 0)
    }

    @Provides
    @Singleton
    fun provideCaptureScreenUseCase(
        screenCaptureManager: ScreenCaptureManager
    ): CaptureScreenUseCase = CaptureScreenUseCase(screenCaptureManager)

    @Provides
    @Singleton
    fun provideRecognizeCenterTextUseCase(
        textRecognitionManager: TextRecognitionManager
    ): RecognizeCenterTextUseCase = RecognizeCenterTextUseCase(textRecognitionManager)

    @Provides
    @Singleton
    fun provideCaptureAndRecognizeUseCase(
        captureScreenUseCase: CaptureScreenUseCase,
        recognizeCenterTextUseCase: RecognizeCenterTextUseCase
    ): CaptureAndRecognizeUseCase = CaptureAndRecognizeUseCase(
        captureScreenUseCase,
        recognizeCenterTextUseCase
    )
}
