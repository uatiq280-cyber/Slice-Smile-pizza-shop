package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishInputBorder
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun AdminChangePinDialog(
    currentOwnerIdValue: String = "admin",
    onDismiss: () -> Unit,
    onSubmitChange: (currentPin: String, newOwnerId: String, newPin: String) -> Boolean
) {
    var newOwnerId by remember { mutableStateOf(currentOwnerIdValue) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleSave = {
        if (currentPin.isBlank()) {
            errorMessage = "Please enter your current Password / PIN"
        } else if (newPin.trim().length < 4) {
            errorMessage = "New Password must be at least 4 characters long"
        } else if (newPin != confirmPin) {
            errorMessage = "New Password and Confirm Password do not match!"
        } else {
            val success = onSubmitChange(currentPin.trim(), newOwnerId.trim(), newPin.trim())
            if (!success) {
                errorMessage = "Current Password is incorrect!"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(PolishPrimaryContainerSubtle, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = "Change Credentials",
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Change Owner Credentials 🔑",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "اپنا نیا اونر یوزرنیم اور پاس ورڈ سیٹ کریں",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newOwnerId,
                    onValueChange = {
                        newOwnerId = it
                        errorMessage = null
                    },
                    label = { Text("Owner ID / Email (اونر آئی ڈی)") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = PolishPrimaryRed)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it
                        errorMessage = null
                    },
                    label = { Text("Current Password (موجودہ پاس ورڈ)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        errorMessage = null
                    },
                    label = { Text("New Password (نیا پاس ورڈ)") },
                    placeholder = { Text("Min 4 characters") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New Password (دوبارہ لکھیں)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = PolishPrimaryRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = handleSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishPrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Credentials 💾", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
            ) {
                Text("Cancel", color = PolishTextMuted)
            }
        }
    )
}
