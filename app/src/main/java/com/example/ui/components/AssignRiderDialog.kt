package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Order
import com.example.model.Rider
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun AssignRiderDialog(
    order: Order,
    availableRiders: List<Rider>,
    onDismiss: () -> Unit,
    onAssign: (orderId: Long, rider: Rider) -> Unit
) {
    var selectedRider by remember { mutableStateOf(availableRiders.find { it.id == order.riderId } ?: availableRiders.firstOrNull()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "Assign Delivery Rider",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark
                    )
                )

                Text(
                    text = "Order #${order.orderId} • ${order.customerName} (Rs. ${order.totalAmount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = PolishPrimaryRed,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (availableRiders.isEmpty()) {
                    Text(
                        text = "No active riders found. Please add a rider in the Rider Management tab first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PolishTextMuted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableRiders) { rider ->
                            val isSelected = selectedRider?.id == rider.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PolishPrimaryContainerSubtle else PolishBgLight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) PolishPrimaryRed else PolishBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRider = rider }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) PolishPrimaryRed else Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TwoWheeler,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else PolishPrimaryRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = rider.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PolishTextDark
                                                )
                                            )
                                            Text(
                                                text = "${rider.phone} • ${rider.vehicle}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(PolishPrimaryRed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = PolishTextMuted)
                    }

                    Button(
                        onClick = {
                            selectedRider?.let { r ->
                                onAssign(order.orderId, r)
                            }
                        },
                        enabled = selectedRider != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_assign_rider_btn")
                    ) {
                        Text("Confirm Assign 🛵", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
