package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.Rider
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
fun RiderPortalScreen(
    rider: Rider,
    assignedOrders: List<Order>,
    onMarkOutForDelivery: (orderId: Long) -> Unit,
    onMarkDelivered: (orderId: Long) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val activeOrders = assignedOrders.filter { it.status != OrderStatus.DELIVERED }
    val completedOrders = assignedOrders.filter { it.status == OrderStatus.DELIVERED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBgLight)
    ) {
        // Rider Header Bar
        Surface(
            color = PolishMaroonDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PolishPrimaryRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = rider.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF2E7D32)
                                ) {
                                    Text(
                                        text = "Active Rider 🛵",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${rider.phone} • ${rider.vehicle}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PolishPrimaryContainerSubtle,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("rider_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Performance Summary Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RiderMetricCard(
                        title = "Assigned Today",
                        value = "${activeOrders.size} active",
                        color = Color(0xFFFFF3E0),
                        textColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    RiderMetricCard(
                        title = "Completed",
                        value = "${completedOrders.size} delivered",
                        color = Color(0xFFE8F5E9),
                        textColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    RiderMetricCard(
                        title = "Rating",
                        value = "⭐ ${rider.rating}",
                        color = PolishPrimaryContainerSubtle,
                        textColor = PolishPrimaryRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tabs: Active vs Delivered
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PolishPrimaryRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PolishPrimaryRed,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "🛵 Active Deliveries (${activeOrders.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) PolishPrimaryRed else PolishTextMuted
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "✅ Completed History (${completedOrders.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) PolishPrimaryRed else PolishTextMuted
                    )
                }
            )
        }

        // Orders List
        val currentOrdersList = if (selectedTab == 0) activeOrders else completedOrders

        if (currentOrdersList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Moped else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PolishTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedTab == 0) "No active orders assigned currently." else "No completed deliveries yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedTab == 0) "New customer orders will appear here in real-time." else "Completed trips will be archived here.",
                        style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(currentOrdersList, key = { it.orderId }) { order ->
                    RiderOrderCard(
                        order = order,
                        rider = rider,
                        onMarkOutForDelivery = { onMarkOutForDelivery(order.orderId) },
                        onMarkDelivered = { onMarkDelivered(order.orderId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RiderMetricCard(
    title: String,
    value: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = textColor,
                    fontWeight = FontWeight.Black
                )
            )
        }
    }
}

@Composable
private fun RiderOrderCard(
    order: Order,
    rider: Rider,
    onMarkOutForDelivery: () -> Unit,
    onMarkDelivered: () -> Unit
) {
    val context = LocalContext.current
    val isDelivered = order.status == OrderStatus.DELIVERED
    val isOutForDelivery = order.status == OrderStatus.OUT_FOR_DELIVERY

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rider_order_card_${order.orderId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isOutForDelivery) PolishPrimaryRed else if (isDelivered) Color(0xFFA5D6A7) else PolishBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Order ID and Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.status) {
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        OrderStatus.OUT_FOR_DELIVERY -> PolishPrimaryContainerSubtle
                        OrderStatus.READY_FOR_PICKUP -> Color(0xFFFFF3E0)
                        else -> PolishBgLight
                    }
                ) {
                    Text(
                        text = "${order.status.iconEmoji} ${order.status.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = when (order.status) {
                                OrderStatus.DELIVERED -> Color(0xFF2E7D32)
                                OrderStatus.OUT_FOR_DELIVERY -> PolishPrimaryRed
                                OrderStatus.READY_FOR_PICKUP -> Color(0xFFE65100)
                                else -> PolishMaroonDark
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = PolishBorder)

            // Customer Info & Direct Contact Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Customer: ${order.customerName}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark
                        )
                    )
                    Text(
                        text = order.customerPhone,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (rider.canCallCustomer) {
                        IconButton(
                            onClick = {
                                WhatsAppOrderHelper.sendRawWhatsAppMessage(
                                    context,
                                    order.customerPhone,
                                    "Assalam-o-Alaikum ${order.customerName}! I am your Slice Smile Rider approaching your address with your hot pizza order #${order.orderId}."
                                )
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp Customer",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                WhatsAppOrderHelper.makePhoneCall(context, order.customerPhone)
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PolishPrimaryRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Customer",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Delivery Address Box with Maps Direction Action
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PolishBgLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PolishPrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = order.deliveryAddress,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTextDark
                                )
                            )
                            if (order.areaLandmark.isNotBlank()) {
                                Text(
                                    text = "Landmark: ${order.areaLandmark}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                                )
                            }
                        }
                    }

                    if (rider.canViewDirections) {
                        IconButton(
                            onClick = {
                                try {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${Uri.encode(order.deliveryAddress + ", Sadiqabad")}"))
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${Uri.encode(order.deliveryAddress + ", Sadiqabad")}"))
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PolishMaroonDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Open Maps Navigation",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items Summary
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall.copy(color = PolishTextDark, lineHeight = 18.sp),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment / Amount Collection Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) Color(0xFFFFCC80) else Color(0xFFA5D6A7)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) "CASH TO COLLECT ON DELIVERY:" else "PAYMENT STATUS:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) Color(0xFFE65100) else Color(0xFF2E7D32)
                            )
                        )
                        Text(
                            text = if (order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) "Collect Rs. ${order.totalAmount}" else "Paid Online via Easypaisa (${order.easypaisaTrxId ?: "Verified"})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark
                            )
                        )
                    }

                    Text(
                        text = "Rs. ${order.totalAmount}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishPrimaryRed
                        )
                    )
                }
            }

            // Rider Action Buttons with Granular Permission Checks
            if (!isDelivered) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (order.status != OrderStatus.OUT_FOR_DELIVERY && rider.canPickOrder) {
                        Button(
                            onClick = onMarkOutForDelivery,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PolishPrimaryRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("rider_mark_out_btn_${order.orderId}")
                        ) {
                            Text("Start Delivery 🛵", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (rider.canMarkDelivered) {
                        Button(
                            onClick = onMarkDelivered,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("rider_mark_delivered_btn_${order.orderId}")
                        ) {
                            Text("Mark Delivered 🎉", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
