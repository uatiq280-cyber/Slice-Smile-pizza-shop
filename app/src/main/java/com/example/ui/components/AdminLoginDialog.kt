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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishInputBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLoginSubmit: (ownerId: String, pin: String) -> Boolean
) {
    var ownerIdText by remember { mutableStateOf("") }
    var pinText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleLogin = {
        if (pinText.isBlank()) {
            errorMessage = "Please enter your Owner Password / PIN"
        } else {
            val id = if (ownerIdText.isBlank()) "admin" else ownerIdText.trim()
            val success = onLoginSubmit(id, pinText.trim())
            if (!success) {
                errorMessage = "Incorrect Owner ID or Password! Please try again."
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
                        .size(46.dp)
                        .background(PolishPrimaryContainerSubtle, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Owner Lock",
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Owner / Admin Portal 👑",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "دکان کے مالک اور مینیجر کے لیے الگ پورٹل",
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
                Text(
                    text = "Sign in with your Owner Credentials to edit rates, add deals, and manage live stock.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PolishTextMuted,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Owner ID Input
                OutlinedTextField(
                    value = ownerIdText,
                    onValueChange = {
                        ownerIdText = it
                        errorMessage = null
                    },
                    label = { Text("Owner ID / Username") },
                    placeholder = { Text("admin or owner@slicesmile.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = PolishPrimaryRed
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_owner_id_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Owner PIN / Password Input
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        pinText = it
                        errorMessage = null
                    },
                    label = { Text("Owner Password / PIN (پاس ورڈ)") },
                    placeholder = { Text("Default PIN: 1234") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { handleLogin() }),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = PolishPrimaryRed
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle PIN Visibility",
                                tint = PolishTextMuted
                            )
                        }
                    },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = PolishPrimaryRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Default: ID: admin | Password: 1234",
                                color = PolishTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = handleLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishPrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("admin_login_btn")
            ) {
                Text(
                    text = "Login to Portal 👑",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
            ) {
                Text(
                    text = "Cancel",
                    color = PolishTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
