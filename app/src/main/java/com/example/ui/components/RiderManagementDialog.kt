package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Rider
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun RiderManagementDialog(
    rider: Rider?,
    onDismiss: () -> Unit,
    onSave: (Rider) -> Unit
) {
    val isNew = (rider == null || rider.id.isBlank())
    var name by remember { mutableStateOf(rider?.name ?: "") }
    var phone by remember { mutableStateOf(rider?.phone ?: "0303-") }
    var vehicle by remember { mutableStateOf(rider?.vehicle ?: "Honda 125 (Thermal Box)") }
    var pin by remember { mutableStateOf(rider?.pin ?: "1234") }
    var isEnabled by remember { mutableStateOf(rider?.isEnabled ?: true) }

    // Granular Permissions
    var canAcceptOrder by remember { mutableStateOf(rider?.canAcceptOrder ?: true) }
    var canPickOrder by remember { mutableStateOf(rider?.canPickOrder ?: true) }
    var canMarkDelivered by remember { mutableStateOf(rider?.canMarkDelivered ?: true) }
    var canCallCustomer by remember { mutableStateOf(rider?.canCallCustomer ?: true) }
    var canViewDirections by remember { mutableStateOf(rider?.canViewDirections ?: true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isNew) "Add Delivery Rider 🛵" else "Edit Rider Details 🛵",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark
                    )
                )

                Text(
                    text = "Manage rider account, credentials, and custom access permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = PolishTextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Rider Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimaryRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rider_edit_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorMessage = null },
                    label = { Text("Mobile Number (WhatsApp/Calls)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimaryRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rider_edit_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it; errorMessage = null },
                    label = { Text("Vehicle & Thermal Box Info") },
                    leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = PolishPrimaryRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rider_edit_vehicle_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it; errorMessage = null },
                    label = { Text("Rider 4-Digit Login PIN") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PolishPrimaryRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rider_edit_pin_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Granular Permissions
                Text(
                    text = "Rider Screen Access Permissions (اختیارات)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishMaroonDark
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                RiderPermissionRow(
                    title = "Can Accept Available Ready Orders",
                    checked = canAcceptOrder,
                    onCheckedChange = { canAcceptOrder = it }
                )

                RiderPermissionRow(
                    title = "Can Mark 'Order Picked' (Out for Delivery)",
                    checked = canPickOrder,
                    onCheckedChange = { canPickOrder = it }
                )

                RiderPermissionRow(
                    title = "Can Mark 'Order Delivered' to Customer",
                    checked = canMarkDelivered,
                    onCheckedChange = { canMarkDelivered = it }
                )

                RiderPermissionRow(
                    title = "Can Call Customer directly from App",
                    checked = canCallCustomer,
                    onCheckedChange = { canCallCustomer = it }
                )

                RiderPermissionRow(
                    title = "Can View Google Maps Navigation / Directions",
                    checked = canViewDirections,
                    onCheckedChange = { canViewDirections = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnabled) "Status: Enabled (Can deliver)" else "Status: Disabled (Inactive)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimaryRed
                        )
                    )
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = PolishPrimaryRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
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
                            if (name.isBlank() || phone.isBlank()) {
                                errorMessage = "Please enter rider name & mobile number."
                            } else {
                                val savedRider = Rider(
                                    id = rider?.id ?: "rider_${System.currentTimeMillis() % 10000}",
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    vehicle = vehicle.trim().ifBlank { "Honda 125" },
                                    pin = pin.trim().ifBlank { "1234" },
                                    isEnabled = isEnabled,
                                    rating = rider?.rating ?: 5.0,
                                    totalDeliveries = rider?.totalDeliveries ?: 0,
                                    canAcceptOrder = canAcceptOrder,
                                    canPickOrder = canPickOrder,
                                    canMarkDelivered = canMarkDelivered,
                                    canCallCustomer = canCallCustomer,
                                    canViewDirections = canViewDirections
                                )
                                onSave(savedRider)
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
                            .testTag("rider_save_submit_btn")
                    ) {
                        Text("Save Rider 🛵", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RiderPermissionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PolishPrimaryRed,
                checkmarkColor = Color.White
            )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                color = if (checked) PolishTextDark else PolishTextMuted
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
