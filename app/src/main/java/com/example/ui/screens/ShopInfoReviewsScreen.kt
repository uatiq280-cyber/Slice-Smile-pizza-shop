package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MenuDataSource
import com.example.model.CustomerFeedback
import com.example.ui.components.RatingBar
import com.example.ui.components.WhatsAppOrderHelper
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShopInfoReviewsScreen(
    reviews: List<CustomerFeedback>
) {
    val context = LocalContext.current
    val averageRating = if (reviews.isNotEmpty()) reviews.map { it.overallRating }.average() else 4.9

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBgLight),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. Shop Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_info_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PolishPrimaryContainerSubtle)
                                .border(1.dp, PolishBorder, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = PolishPrimaryRed,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Slice Smile Pizza Shop",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PolishMaroonDark,
                                    fontSize = 19.sp
                                )
                            )
                            Text(
                                text = "Pizzeria & Fast Food • Chowk Nazir Wala",
                                style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location detail
                    ShopDetailRow(
                        icon = Icons.Default.LocationOn,
                        title = "Location",
                        subtitle = "Chowk Nazir Wala, Main Food Point",
                        tint = PolishPrimaryRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Delivery radius
                    ShopDetailRow(
                        icon = Icons.Default.DeliveryDining,
                        title = "Free Home Delivery",
                        subtitle = "Min Order Rs. 500 (Free within 3 KM radius)",
                        tint = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Easypaisa Account
                    ShopDetailRow(
                        icon = Icons.Default.Payments,
                        title = "Easypaisa Online Account",
                        subtitle = "03254946190 (Title: Slice Smile)",
                        tint = PolishPrimaryRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timings
                    ShopDetailRow(
                        icon = Icons.Default.AccessTime,
                        title = "Opening Hours",
                        subtitle = "12:00 PM – 02:00 AM (7 Days Open)",
                        tint = PolishMaroonDark
                    )
                }
            }
        }

        // 2. Direct Contact & Ordering Numbers (From user menu)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📞 Order & WhatsApp Hotline",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    MenuDataSource.PHONE_NUMBERS.forEachIndexed { index, phone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(PolishBgLight)
                                .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (index == 0) "Primary Hotline (WhatsApp)" else "Order Line ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (index == 0) WhatsAppGreen else PolishTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = phone,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishMaroonDark,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            Row {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreen.copy(alpha = 0.15f))
                                        .clickable {
                                            WhatsAppOrderHelper.sendRawWhatsAppMessage(
                                                context,
                                                phone,
                                                "Salam! I want to order food from Slice Smile Pizza Shop."
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp",
                                        tint = WhatsAppGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PolishPrimaryContainerSubtle)
                                        .clickable {
                                            WhatsAppOrderHelper.makePhoneCall(context, phone)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = PolishPrimaryRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        if (index < MenuDataSource.PHONE_NUMBERS.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // 3. Customer Reviews Section Header
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customer Reviews",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark
                            )
                        )
                        Text(
                            text = "Based on verified customer orders",
                            style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale.US, "%.1f", averageRating),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PolishMaroonDark,
                                    fontSize = 24.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = PolishPrimaryRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "${reviews.size} Reviews",
                            style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 4. Individual Customer Reviews List
        items(reviews, key = { it.id }) { review ->
            CustomerReviewCard(feedback = review)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CustomerReviewCard(feedback: CustomerFeedback) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(feedback.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(1.dp)
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishPrimaryContainerSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = feedback.customerName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishPrimaryRed
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = feedback.customerName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishMaroonDark
                            )
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                        )
                    }
                }

                RatingBar(
                    rating = feedback.overallRating,
                    onRatingChanged = {},
                    starSize = 16
                )
            }

            if (feedback.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = feedback.comment,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PolishTextDark,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ShopDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishMaroonDark
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PolishTextMuted
                )
            )
        }
    }
}

