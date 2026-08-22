package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaymentMethod(val title: String, val description: String) {
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "Pay cash to the rider upon delivery"),
    EASYPAISA("Easypaisa Online", "Transfer to 03254946190 & submit TRX ID")
}

enum class OrderStatus(
    val label: String,
    val stepIndex: Int,
    val description: String,
    val iconEmoji: String
) {
    ORDER_RECEIVED("Order Received", 0, "Order verified and queued at Slice Smile Pizza Workshop", "📥"),
    PREPARING_PIZZA("Preparing Pizza", 1, "Dough freshly rolled, sauce & toppings layered, baking at 450°F", "🧑‍🍳"),
    OUT_FOR_DELIVERY("Out for Delivery", 2, "Rider dispatched with thermal heat-insulated pizza bag", "🛵"),
    DELIVERED("Delivered", 3, "Order delivered hot & fresh to your doorstep. Enjoy your meal!", "🎉"),
    CANCELLED("Cancelled", -1, "This order was cancelled", "❌");

    companion object {
        val PLACED = ORDER_RECEIVED
        val PREPARING = PREPARING_PIZZA
    }
}

data class Order(
    val orderId: Long,
    val itemsSummary: String,
    val itemsCount: Int,
    val subtotal: Int,
    val discount: Int,
    val deliveryFee: Int,
    val totalAmount: Int,
    val paymentMethod: PaymentMethod,
    val easypaisaTrxId: String? = null,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val areaLandmark: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val orderNote: String = "",
    val coinsEarned: Int = 0,
    val coinsRedeemed: Int = 0,
    val status: OrderStatus = OrderStatus.ORDER_RECEIVED,
    val timestamp: Long = System.currentTimeMillis(),
    val estimatedDeliveryMinutes: Int = 35,
    val riderName: String = "Tariq Mahmood",
    val riderPhone: String = "0303-7448255",
    val riderVehicle: String = "Honda 125 • Thermal Insulated Box",
    val rating: Int = 0,
    val reviewComment: String = "",
    val feedbackSubmitted: Boolean = false
) {
    val formattedPlacedTime: String get() {
        val sdf = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    val estimatedArrivalFormatted: String get() {
        val arrivalTimeMillis = timestamp + (estimatedDeliveryMinutes * 60 * 1000L)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(arrivalTimeMillis))
    }

    val estimatedTimeRemainingText: String get() {
        return when (status) {
            OrderStatus.ORDER_RECEIVED -> "Estimated Delivery: ~30-35 mins (ETA $estimatedArrivalFormatted)"
            OrderStatus.PREPARING_PIZZA -> "Estimated Delivery: ~18-22 mins (Baking in oven 🔥)"
            OrderStatus.OUT_FOR_DELIVERY -> "Estimated Delivery: ~5-10 mins (Rider on the way 🛵)"
            OrderStatus.DELIVERED -> "Delivered Successfully (${formattedPlacedTime})"
            OrderStatus.CANCELLED -> "Order Cancelled"
        }
    }

    val progressPercent: Float get() {
        return when (status) {
            OrderStatus.ORDER_RECEIVED -> 0.25f
            OrderStatus.PREPARING_PIZZA -> 0.55f
            OrderStatus.OUT_FOR_DELIVERY -> 0.85f
            OrderStatus.DELIVERED -> 1.0f
            OrderStatus.CANCELLED -> 0.0f
        }
    }
}

