package com.screentextocr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScreenTextOCRApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization if needed
    }
}
