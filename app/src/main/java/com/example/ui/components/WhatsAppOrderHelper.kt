package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.MenuDataSource
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.PaymentMethod
import java.net.URLEncoder

object WhatsAppOrderHelper {

    fun formatOrderWhatsAppMessage(order: Order): String {
        val sb = StringBuilder()
        sb.append("🍕 *SLICE SMILE PIZZA WORKSHOP* 🍕\n")
        sb.append("📍 Location: Chowk Nazir Wala\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🛒 *NEW ONLINE ORDER* (#${order.orderId})\n")
        sb.append("👤 *Customer:* ${order.customerName}\n")
        sb.append("📞 *Phone:* ${order.customerPhone}\n")
        sb.append("🏠 *Address:* ${order.deliveryAddress}\n")
        if (order.areaLandmark.isNotBlank()) {
            sb.append("📍 *Landmark/Area:* ${order.areaLandmark}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📋 *ITEMS ORDERED:*\n")
        sb.append("${order.itemsSummary}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💵 *Subtotal:* Rs. ${order.subtotal}\n")
        if (order.discount > 0) {
            sb.append("🪙 *Coins Discount:* -Rs. ${order.discount} (${order.coinsRedeemed} coins)\n")
        }
        sb.append("🛵 *Delivery Fee:* ${if (order.deliveryFee == 0) "FREE (Within 3 KM)" else "Rs. ${order.deliveryFee}"}\n")
        sb.append("💰 *TOTAL AMOUNT:* Rs. ${order.totalAmount}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💳 *Payment Method:* ${order.paymentMethod.title}\n")
        if (order.paymentMethod == PaymentMethod.EASYPAISA && !order.easypaisaTrxId.isNullOrBlank()) {
            sb.append("✅ *Easypaisa TRX ID:* ${order.easypaisaTrxId}\n")
            sb.append("📱 *Paid to Easypaisa:* ${MenuDataSource.EASYPAISA_ACCOUNT_NUMBER}\n")
        }
        if (order.coinsEarned > 0) {
            sb.append("🎁 *Smile Coins Earned:* +${order.coinsEarned} coins!\n")
        }
        if (order.orderNote.isNotBlank()) {
            sb.append("📝 *Special Note:* ${order.orderNote}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Please confirm my order and start preparation. Thank you! 🍕😊")

        return sb.toString()
    }

    fun sendOrderToWhatsApp(context: Context, order: Order, customNumber: String = MenuDataSource.PRIMARY_WHATSAPP) {
        val message = formatOrderWhatsAppMessage(order)
        sendRawWhatsAppMessage(context, customNumber, message)
    }

    fun openDirectShopWhatsApp(context: Context, customMessage: String = "Salam! I want to order food from Slice Smile Pizza Shop 🍕") {
        sendRawWhatsAppMessage(context, MenuDataSource.PRIMARY_WHATSAPP, customMessage)
    }

    fun sendRawWhatsAppMessage(context: Context, phoneNumber: String, message: String) {
        try {
            // Clean phone number (e.g. 0303-7448255 / 03037448255 -> 923037448255)
            var cleanPhone = phoneNumber.replace("+", "").replace("-", "").replace(" ", "").trim()
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "92" + cleanPhone.substring(1)
            }
            if (!cleanPhone.startsWith("92")) {
                cleanPhone = "92$cleanPhone"
            }
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://wa.me/$cleanPhone?text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to make phone call: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
