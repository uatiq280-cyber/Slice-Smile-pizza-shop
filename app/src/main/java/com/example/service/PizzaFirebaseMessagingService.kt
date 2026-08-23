package com.example.service

import android.util.Log
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PizzaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("PizzaFCM", "New FCM registration token received: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("PizzaFCM", "Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val type = data["type"] ?: "NEW_ORDER"

        if (type == "NEW_ORDER") {
            val orderId = data["orderId"]?.toLongOrNull() ?: System.currentTimeMillis() % 100000
            val customerName = data["customerName"] ?: "Slice Smile Customer"
            val customerPhone = data["customerPhone"] ?: "0303-7448255"
            val totalAmount = data["totalAmount"]?.toIntOrNull() ?: 0
            val deliveryAddress = data["deliveryAddress"] ?: "Sadiqabad"
            val itemsSummary = data["itemsSummary"] ?: "Pizza Order"
            val paymentMethodStr = data["paymentMethod"] ?: "CASH_ON_DELIVERY"
            val paymentMethod = try {
                PaymentMethod.valueOf(paymentMethodStr)
            } catch (e: Exception) {
                PaymentMethod.CASH_ON_DELIVERY
            }

            val order = Order(
                orderId = orderId,
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress,
                areaLandmark = data["areaLandmark"] ?: "",
                itemsSummary = itemsSummary,
                itemsCount = 1,
                subtotal = totalAmount,
                discount = 0,
                deliveryFee = 0,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                status = OrderStatus.ORDER_RECEIVED
            )

            NotificationHelper.notifyOwnerNewOrder(applicationContext, order)
        } else if (type == "ORDER_STATUS") {
            val orderId = data["orderId"]?.toLongOrNull() ?: 0L
            val title = data["title"] ?: "Order Update"
            val message = data["message"] ?: "Your pizza status has been updated!"
            NotificationHelper.notifyOrderStatusUpdate(applicationContext, orderId, title, message)
        } else {
            // General notification fallback
            remoteMessage.notification?.let {
                NotificationHelper.notifyOrderStatusUpdate(
                    applicationContext,
                    System.currentTimeMillis() % 10000,
                    it.title ?: "Slice Smile Pizza",
                    it.body ?: "Order update received"
                )
            }
        }
    }
}
