package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.repository.PizzaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OrderBackgroundSyncService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("OrderBgService", "OrderBackgroundSyncService created")
        NotificationHelper.initNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("OrderBgService", "OrderBackgroundSyncService started")

        // Start Foreground to ensure Android OS never kills the background listener while phone is locked
        try {
            val notification = createForegroundNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("OrderBgService", "Failed to startForeground: ${e.message}", e)
        }

        // Initialize repository to ensure real-time Firestore order listener is active
        serviceScope.launch {
            try {
                val repository = PizzaRepository.getInstance(applicationContext)
                repository.refreshOrdersFromCloud()
                Log.d("OrderBgService", "Repository initialized and active for background orders listening")
            } catch (e: Exception) {
                Log.e("OrderBgService", "Error activating repository in background service", e)
            }
        }

        return START_STICKY
    }

    private fun createForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_BACKGROUND_SERVICE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Slice Smile Live Order Service")
            .setContentText("Order notifications active in background 🍕")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("OrderBgService", "OrderBackgroundSyncService destroyed")
    }

    companion object {
        private const val NOTIFICATION_ID = 9001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, OrderBackgroundSyncService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("OrderBgService", "Failed to start OrderBackgroundSyncService: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, OrderBackgroundSyncService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e("OrderBgService", "Failed to stop OrderBackgroundSyncService: ${e.message}")
            }
        }
    }
}
