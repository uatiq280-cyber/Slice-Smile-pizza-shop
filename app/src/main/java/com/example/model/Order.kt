package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaymentMethod(val title: String, val description: String) {
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "Pay cash to the rider upon delivery"),
    EASYPAISA("Easypaisa Online", "Transfer to Easypaisa account & submit TRX ID"),
    JAZZCASH("JazzCash Online", "Transfer to JazzCash account & submit TRX ID"),
    BANK_TRANSFER("Bank Transfer / Raast", "Direct bank transfer & submit Ref ID")
}

enum class OrderStatus(
    val label: String,
    val stepIndex: Int,
    val description: String,
    val iconEmoji: String
) {
    ORDER_RECEIVED("Order Received", 0, "Order received & confirmed at Slice Smile Pizza", "📥"),
    PREPARING_PIZZA("Preparing", 1, "Dough freshly rolled, sauce & toppings layered, baking hot", "🧑‍🍳"),
    READY_FOR_PICKUP("Ready", 2, "Fresh out of oven, packed & ready for rider pickup", "🍕"),
    OUT_FOR_DELIVERY("Out for Delivery", 3, "Rider dispatched with insulated thermal pizza bag", "🛵"),
    DELIVERED("Delivered", 4, "Order delivered hot & fresh to your doorstep. Enjoy!", "🎉"),
    CANCELLED("Cancelled", -1, "This order was cancelled", "❌");

    val title: String get() = label

    companion object {
        val PLACED = ORDER_RECEIVED
        val PREPARING = PREPARING_PIZZA
        val READY = READY_FOR_PICKUP
    }
}

data class Order(
    val orderId: Long,
    val userId: String = "guest_user",
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
    val riderId: String? = null,
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

    val formattedTime: String get() = formattedPlacedTime

    val estimatedArrivalFormatted: String get() {
        val arrivalTimeMillis = timestamp + (estimatedDeliveryMinutes * 60 * 1000L)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(arrivalTimeMillis))
    }

    val estimatedTimeRemainingText: String get() {
        return when (status) {
            OrderStatus.ORDER_RECEIVED -> "Received: ~30-35 mins (ETA $estimatedArrivalFormatted)"
            OrderStatus.PREPARING_PIZZA -> "Preparing: ~18-22 mins (Baking in oven 🔥)"
            OrderStatus.READY_FOR_PICKUP -> "Ready: ~10-15 mins (Ready for rider pickup 🍕)"
            OrderStatus.OUT_FOR_DELIVERY -> "Out for Delivery: ~5-10 mins (Rider on the way 🛵)"
            OrderStatus.DELIVERED -> "Delivered Successfully (${formattedPlacedTime})"
            OrderStatus.CANCELLED -> "Order Cancelled"
        }
    }

    val progressPercent: Float get() {
        return when (status) {
            OrderStatus.ORDER_RECEIVED -> 0.20f
            OrderStatus.PREPARING_PIZZA -> 0.45f
            OrderStatus.READY_FOR_PICKUP -> 0.68f
            OrderStatus.OUT_FOR_DELIVERY -> 0.88f
            OrderStatus.DELIVERED -> 1.0f
            OrderStatus.CANCELLED -> 0.0f
        }
    }
}

