package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.Order

object NotificationHelper {
    const val CHANNEL_ORDERS = "slice_smile_orders_channel_v2"
    const val CHANNEL_STATUS = "slice_smile_status_channel_v2"
    const val CHANNEL_BACKGROUND_SERVICE = "slice_smile_bg_service_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 1. Owner New Order Channel (High Priority with Sound & Vibration & Lock Screen Public)
            val orderChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "New Order Alerts (Owner)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies shop owner immediately with sound and wakes screen when a new pizza order arrives"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(defaultSoundUri, audioAttributes)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }

            // 2. Order Tracking & Delivery Channel (Customer & Rider)
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS,
                "Order Status & Delivery Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Updates for customers and riders on pizza preparation, dispatch, and delivery"
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // 3. Low-priority Background Service Sync Channel
            val serviceChannel = NotificationChannel(
                CHANNEL_BACKGROUND_SERVICE,
                "Order Sync Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps real-time order notifications active in the background when phone is locked"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }

            notificationManager.createNotificationChannel(orderChannel)
            notificationManager.createNotificationChannel(statusChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun wakeUpScreen(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isScreenOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                powerManager?.isInteractive ?: true
            } else {
                @Suppress("DEPRECATION")
                powerManager?.isScreenOn ?: true
            }

            @Suppress("DEPRECATION")
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "SliceSmile:NewOrderWakeLock"
            )
            wakeLock?.acquire(10000L) // Hold wake lock for 10 seconds to ensure alert is delivered
            Log.d("NotificationHelper", "Screen woke up successfully. Was interactive: $isScreenOn")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error waking up screen", e)
        }
    }

    fun playOrderNotificationSound(context: Context) {
        try {
            // 1. Play Alarm/Notification Sound
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            ringtone?.play()

            // 2. Trigger phone vibration
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error playing notification sound/vibration", e)
        }
    }

    fun notifyOwnerNewOrder(context: Context, order: Order) {
        initNotificationChannels(context)

        // Wake screen up even if phone is locked
        wakeUpScreen(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_SCREEN", "admin")
            putExtra("ORDER_ID", order.orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            order.orderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ORDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔔 New Order #${order.orderId} - Rs. ${order.totalAmount}")
            .setContentText("${order.customerName} (${order.customerPhone}) - ${order.deliveryAddress}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Order #${order.orderId}\nCustomer: ${order.customerName} (${order.customerPhone})\nAddress: ${order.deliveryAddress}\nTotal: Rs. ${order.totalAmount}\nPayment: ${order.paymentMethod.title}\n\nItems:\n${order.itemsSummary}")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Heads-up display over lock screen!

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(order.orderId.toInt(), builder.build())
        playOrderNotificationSound(context)
    }

    fun notifyOrderStatusUpdate(context: Context, orderId: Long, statusTitle: String, message: String) {
        initNotificationChannels(context)

        wakeUpScreen(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_SCREEN", "orders")
            putExtra("ORDER_ID", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🍕 Order #$orderId: $statusTitle")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(orderId.toInt(), builder.build())
        playOrderNotificationSound(context)
    }
}
