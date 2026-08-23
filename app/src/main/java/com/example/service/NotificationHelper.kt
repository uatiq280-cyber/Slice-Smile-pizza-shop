package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.Order

object NotificationHelper {
    const val CHANNEL_ORDERS = "slice_smile_orders_channel"
    const val CHANNEL_STATUS = "slice_smile_status_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // 1. Owner New Order Channel (High Priority with Sound & Vibration)
            val orderChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "New Order Alerts (Owner)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies shop owner immediately with sound when a new pizza order arrives"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(defaultSoundUri, audioAttributes)
                enableLights(true)
            }

            // 2. Order Tracking & Delivery Channel (Customer & Rider)
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS,
                "Order Status & Delivery Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates for customers and riders on pizza preparation, dispatch, and delivery"
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            notificationManager.createNotificationChannel(orderChannel)
            notificationManager.createNotificationChannel(statusChannel)
        }
    }

    fun playOrderNotificationSound(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun notifyOwnerNewOrder(context: Context, order: Order) {
        initNotificationChannels(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            .setContentTitle("🔔 New Pizza Order #${order.orderId} Received!")
            .setContentText("${order.customerName} ordered Rs. ${order.totalAmount} (${order.paymentMethod.title})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Order #${order.orderId}\nCustomer: ${order.customerName} (${order.customerPhone})\nAddress: ${order.deliveryAddress}\nTotal: Rs. ${order.totalAmount}\nPayment: ${order.paymentMethod.title}\n\nItems:\n${order.itemsSummary}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(order.orderId.toInt(), builder.build())
        playOrderNotificationSound(context)
    }

    fun notifyOrderStatusUpdate(context: Context, orderId: Long, statusTitle: String, message: String) {
        initNotificationChannels(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(orderId.toInt(), builder.build())
    }
}
