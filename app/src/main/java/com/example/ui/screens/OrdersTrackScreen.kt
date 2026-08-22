package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MenuDataSource
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.ui.components.WhatsAppOrderHelper
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishBorderStrong
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.WhatsAppGreen

@Composable
fun OrdersTrackScreen(
    orders: List<Order>,
    onOpenFeedback: (Order) -> Unit,
    onStatusAdvance: (orderId: Long, nextStatus: OrderStatus) -> Unit,
    onNavigateToMenu: () -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PolishBgLight)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(PolishPrimaryContainerSubtle)
                        .border(1.dp, PolishBorder, RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Moped,
                        contentDescription = null,
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "No Orders to Track",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Place an order from Slice Smile Pizza Shop to experience live real-time tracking!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PolishTextMuted,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = onNavigateToMenu,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimaryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("orders_empty_menu_btn")
                ) {
                    Text("Explore Menu & Order 🍕", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBgLight),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Order Tracking",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "Slice Smile Pizza Shop • Chowk Nazir Wala",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PolishPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderStrong)
                ) {
                    Text(
                        text = "${orders.size} Orders",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishMaroonDark,
                            fontWeight = FontWeight.Black
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        items(orders, key = { it.orderId }) { order ->
            RealTimeOrderTrackingCard(
                order = order,
                onOpenFeedback = { onOpenFeedback(order) },
                onStatusAdvance = { nextStatus -> onStatusAdvance(order.orderId, nextStatus) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RealTimeOrderTrackingCard(
    order: Order,
    onOpenFeedback: () -> Unit,
    onStatusAdvance: (OrderStatus) -> Unit
) {
    val context = LocalContext.current
    val isDelivered = order.status == OrderStatus.DELIVERED
    val isCancelled = order.status == OrderStatus.CANCELLED

    // Progress animation
    val progressAnimated by animateFloatAsState(
        targetValue = order.progressPercent,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "order_progress"
    )

    // Pulsing transition for live active order
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderId}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Order ID, Status Badge & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PolishPrimaryContainerSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.status.iconEmoji,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Order #${order.orderId}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "Placed at ${order.formattedPlacedTime}",
                            style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                        )
                    }
                }

                // Status Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (order.status) {
                        OrderStatus.ORDER_RECEIVED -> Color(0xFFE3F2FD)
                        OrderStatus.PREPARING_PIZZA -> PolishPrimaryContainer
                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFFFF3E0)
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        OrderStatus.CANCELLED -> Color(0xFFFFEBEE)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = when (order.status) {
                            OrderStatus.ORDER_RECEIVED -> Color(0xFF90CAF9)
                            OrderStatus.PREPARING_PIZZA -> PolishBorderStrong
                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFFFB74D)
                            OrderStatus.DELIVERED -> Color(0xFFA5D6A7)
                            OrderStatus.CANCELLED -> Color(0xFFEF9A9A)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isDelivered && !isCancelled) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(PolishPrimaryRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = order.status.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = when (order.status) {
                                    OrderStatus.ORDER_RECEIVED -> Color(0xFF1565C0)
                                    OrderStatus.PREPARING_PIZZA -> PolishMaroonDark
                                    OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFE65100)
                                    OrderStatus.DELIVERED -> Color(0xFF2E7D32)
                                    OrderStatus.CANCELLED -> Color(0xFFC62828)
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Estimated Delivery Time Live Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDelivered) Color(0xFFF1F8E9) else PolishPrimaryContainer,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDelivered) Color(0xFFA5D6A7) else PolishBorderStrong
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDelivered) Color(0xFF2E7D32) else PolishPrimaryRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDelivered) "Delivered Successfully" else "Estimated Delivery Time",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PolishTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = order.estimatedTimeRemainingText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Multi-Step Real-Time Stepper
            RealTimeStatusStepper(
                currentStatus = order.status,
                progress = progressAnimated
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Rider & Dispatch Information (Active during preparation & delivery)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PolishBgLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PolishPrimaryContainerSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = PolishPrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Rider: ${order.riderName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PolishMaroonDark
                                    )
                                )
                                Text(
                                    text = order.riderVehicle,
                                    style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                                )
                            }
                        }

                        // Call & WhatsApp quick actions
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    WhatsAppOrderHelper.sendRawWhatsAppMessage(
                                        context,
                                        MenuDataSource.PRIMARY_WHATSAPP,
                                        "Salam! Checking live tracking status for my Order #${order.orderId} at Slice Smile Pizza Shop."
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "WhatsApp Rider",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    WhatsAppOrderHelper.makePhoneCall(context, order.riderPhone)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PolishPrimaryRed)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Rider",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Ordered Items with Customization Breakdown
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PolishBgLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Itemized Order (${order.itemsCount} items)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark
                            )
                        )
                        Text(
                            text = "Total: Rs. ${order.totalAmount}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishPrimaryRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = order.itemsSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextDark,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    if (order.orderNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📝 Note: ${order.orderNote}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PolishTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Delivery Address & Payment Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PolishPrimaryRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.deliveryAddress,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark
                        ),
                        maxLines = 1
                    )
                    if (order.areaLandmark.isNotBlank()) {
                        Text(
                            text = "Landmark: ${order.areaLandmark}",
                            style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.paymentMethod == PaymentMethod.EASYPAISA) PolishPrimaryContainerSubtle else PolishBgLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Text(
                        text = if (order.paymentMethod == PaymentMethod.EASYPAISA) "Easypaisa (Paid)" else "COD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (order.paymentMethod == PaymentMethod.EASYPAISA) PolishPrimaryRed else PolishMaroonDark
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (order.coinsEarned > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🪙 +${order.coinsEarned} Smile Coins added to your club wallet",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PolishPrimaryRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = PolishBorder)

            // 7. Post-Delivery Feedback or Live Actions
            if (isDelivered) {
                if (order.feedbackSubmitted) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PolishPrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderStrong),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = PolishPrimaryRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Your Rating: ${order.rating}/5 Stars ⭐",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishMaroonDark
                                    )
                                )
                                if (order.reviewComment.isNotBlank()) {
                                    Text(
                                        text = "\"${order.reviewComment}\"",
                                        style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onOpenFeedback,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishMaroonDark,
                            contentColor = PolishPrimaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("leave_feedback_btn_${order.orderId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = PolishPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rate Pizza & Delivery Experience ⭐",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            color = PolishPrimaryContainer
                        )
                    }
                }
            } else {
                // Interactive Demo Status Advance simulation button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PolishBgLight)
                        .clickable {
                            val next = when (order.status) {
                                OrderStatus.ORDER_RECEIVED -> OrderStatus.PREPARING_PIZZA
                                OrderStatus.PREPARING_PIZZA -> OrderStatus.OUT_FOR_DELIVERY
                                OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
                                else -> OrderStatus.DELIVERED
                            }
                            onStatusAdvance(next)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Advance Status: ${order.status.label} ➔ Next Stage",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RealTimeStatusStepper(
    currentStatus: OrderStatus,
    progress: Float
) {
    val steps = listOf(
        Triple("Order Received", OrderStatus.ORDER_RECEIVED, "📥"),
        Triple("Preparing Pizza", OrderStatus.PREPARING_PIZZA, "🧑‍🍳"),
        Triple("Out for Delivery", OrderStatus.OUT_FOR_DELIVERY, "🛵"),
        Triple("Delivered", OrderStatus.DELIVERED, "🎉")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PolishBgLight)
            .border(1.dp, PolishBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = PolishPrimaryRed,
            trackColor = PolishBorder
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Step Icons and Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, (label, status, emoji) ->
                val isActive = currentStatus.stepIndex >= status.stepIndex
                val isCurrent = currentStatus == status

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) PolishPrimaryRed
                                else if (isActive) Color(0xFF2E7D32)
                                else Color.White
                            )
                            .border(
                                width = if (isCurrent || isActive) 0.dp else 1.dp,
                                color = PolishBorder,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive && !isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = emoji,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) PolishPrimaryRed else if (isActive) PolishMaroonDark else PolishTextMuted
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
