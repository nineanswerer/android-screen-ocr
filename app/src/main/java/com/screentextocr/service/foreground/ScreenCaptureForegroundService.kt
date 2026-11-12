package com.screentextocr.service.foreground

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.screentextocr.R
import com.screentextocr.domain.usecase.CaptureAndRecognizeUseCase
import com.screentextocr.presentation.viewmodel.ScreenCaptureAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Foreground service that runs screen capture in the background.
 * Required for MediaProjection on Android 10+.
 */
@AndroidEntryPoint
class ScreenCaptureForegroundService : LifecycleService() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "ScreenCaptureChannel"
        const val CHANNEL_NAME = "Screen Capture Service"
        const val CHANNEL_DESCRIPTION = "Notifications for screen capture service"

        const val ACTION_STOP = "com.screentextocr.action.STOP_SERVICE"

        private const val CAPTURE_INTERVAL_MS = 3000L // 3 seconds

        @JvmStatic
        fun startService(context: Context) {
            val intent = Intent(context, ScreenCaptureForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, ScreenCaptureForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var captureAndRecognizeUseCase: CaptureAndRecognizeUseCase

    private var captureJob: Job? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_STOP -> {
                stopServiceInternal()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startCaptureLoop()
                return START_STICKY
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null // Not a bound service
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, ScreenCaptureForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openAppIntent = Intent(this, Class.forName("com.screentextocr.presentation.view.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_service_notification_title))
            .setContentText(getString(R.string.foreground_service_notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setContentIntent(openAppPendingIntent)
            .build()
    }

    private fun startCaptureLoop() {
        if (isRunning) return

        isRunning = true
        captureJob = lifecycleScope.launch {
            while (isRunning) {
                try {
                    captureAndRecognizeUseCase()                    // This will emit results through StateFlow in ViewModel
                } catch (e: Exception) {
                    // Log error but continue loop
                    e.printStackTrace()
                }

                // Wait before next capture
                kotlinx.coroutines.delay(CAPTURE_INTERVAL_MS)
            }
        }
    }

    private fun stopCaptureLoop() {
        isRunning = false
        captureJob?.cancel()
        captureJob = null
    }

    private fun stopServiceInternal() {
        stopCaptureLoop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopCaptureLoop()
        super.onDestroy()
    }
}
