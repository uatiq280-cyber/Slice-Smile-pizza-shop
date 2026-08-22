package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Order
import com.example.ui.theme.CheeseGold
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun FeedbackDialog(
    order: Order,
    onDismiss: () -> Unit,
    onSubmit: (overallRating: Int, foodTaste: Int, deliverySpeed: Int, comment: String) -> Unit
) {
    var overallRating by remember { mutableIntStateOf(5) }
    var foodTasteRating by remember { mutableIntStateOf(5) }
    var deliverySpeedRating by remember { mutableIntStateOf(5) }
    var commentText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PolishPrimaryContainerSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = PolishPrimaryRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Order Feedback",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PolishMaroonDark
                                )
                            )
                            Text(
                                text = "Slice Smile Pizza Shop • #${order.orderId}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PolishPrimaryRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_feedback_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PolishTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Overall Star Rating
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainerSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "How was your overall experience?",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishMaroonDark
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        RatingBar(
                            rating = overallRating,
                            onRatingChanged = { overallRating = it },
                            starSize = 34
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (overallRating) {
                                5 -> "Super Delicious! ⭐⭐⭐⭐⭐"
                                4 -> "Great Taste & Service! ⭐⭐⭐⭐"
                                3 -> "Good / Average ⭐⭐⭐"
                                2 -> "Below Expectation ⭐⭐"
                                else -> "Needs Improvement ⭐"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimaryRed
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Food Taste Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍕 Food Taste & Freshness:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark
                        )
                    )
                    RatingBar(
                        rating = foodTasteRating,
                        onRatingChanged = { foodTasteRating = it },
                        starSize = 22
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Delivery Speed Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛵 Delivery Speed & Rider:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark
                        )
                    )
                    RatingBar(
                        rating = deliverySpeedRating,
                        onRatingChanged = { deliverySpeedRating = it },
                        starSize = 22
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comment input
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Write your review / comments (Optional)") },
                    placeholder = { Text("How was the pizza, crust, burger, and delivery?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback_comment_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishBorder,
                        focusedContainerColor = PolishBgLight,
                        unfocusedContainerColor = PolishBgLight
                    ),
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Submit button
                Button(
                    onClick = {
                        onSubmit(overallRating, foodTasteRating, deliverySpeedRating, commentText)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishMaroonDark,
                        contentColor = PolishPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .testTag("submit_feedback_btn")
                ) {
                    Text(
                        text = "Submit Review",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = PolishPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    starSize: Int = 24
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isFilled = i <= rating
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "$i Stars",
                tint = if (isFilled) CheeseGold else PolishBorder,
                modifier = Modifier
                    .size(starSize.dp)
                    .clickable { onRatingChanged(i) }
            )
        }
    }
}

