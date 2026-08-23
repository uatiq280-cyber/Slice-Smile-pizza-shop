package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Rider
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun RiderLoginDialog(
    availableRiders: List<Rider>,
    onDismiss: () -> Unit,
    onLogin: (phoneOrId: String, pin: String) -> Unit
) {
    var selectedRiderId by remember { mutableStateOf(availableRiders.firstOrNull()?.id ?: "rider_tariq") }
    var phoneOrId by remember { mutableStateOf(availableRiders.firstOrNull()?.phone ?: "0303-7448255") }
    var pin by remember { mutableStateOf("1234") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PolishPrimaryContainerSubtle,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = PolishPrimaryRed,
                        modifier = Modifier
                            .padding(14.dp)
                            .size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Rider Delivery Portal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark
                    )
                )

                Text(
                    text = "Login to access assigned customer pizza deliveries",
                    style = MaterialTheme.typography.bodySmall,
                    color = PolishTextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Rider selector pill list
                if (availableRiders.isNotEmpty()) {
                    Text(
                        text = "Select Registered Rider:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PolishTextDark),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableRiders.take(3).forEach { r ->
                            val isSelected = (selectedRiderId == r.id)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PolishPrimaryRed else PolishBgLight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) PolishPrimaryRed else PolishBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                onClick = {
                                    selectedRiderId = r.id
                                    phoneOrId = r.phone
                                    pin = r.pin.ifBlank { "1234" }
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = r.name.split(" ").firstOrNull() ?: r.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else PolishMaroonDark
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = phoneOrId,
                    onValueChange = { phoneOrId = it },
                    label = { Text("Rider Phone or ID") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimaryRed)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rider_login_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("4-Digit Security PIN (Default: 1234)") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PolishPrimaryRed)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rider_login_pin_input")
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = PolishPrimaryRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                            if (phoneOrId.isBlank() || pin.isBlank()) {
                                errorMessage = "Please enter rider phone & PIN"
                            } else {
                                onLogin(phoneOrId.trim(), pin.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("rider_login_submit_btn")
                    ) {
                        Text("Login 🛵", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
