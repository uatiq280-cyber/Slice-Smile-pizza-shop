package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishAccentGold
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishGreenSuccess
import com.example.ui.theme.PolishInputBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun CustomerAuthDialog(
    currentName: String = "",
    currentPhone: String = "",
    currentAddress: String = "",
    onDismiss: () -> Unit,
    onContinueAsGuest: (name: String) -> Unit,
    onRequestOtp: (phone: String) -> String,
    onVerifyOtpAndLogin: (phone: String, otp: String, name: String) -> Boolean,
    onLoginWithGoogle: (email: String, name: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Guest, 1: Mobile + OTP, 2: Gmail

    // Guest Mode State
    var guestName by remember { mutableStateOf(currentName.ifBlank { "Guest Foodie" }) }

    // Phone OTP State
    var phoneInput by remember { mutableStateOf(currentPhone.ifBlank { "03001234567" }) }
    var customerNameInput by remember { mutableStateOf(currentName.ifBlank { "Foodie Customer" }) }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var sentOtpHint by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Gmail State
    var gmailInput by remember { mutableStateOf("user@gmail.com") }
    var gmailNameInput by remember { mutableStateOf(currentName.ifBlank { "Google User" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PolishPrimaryContainerSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🍕", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Slice Smile Account",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTextDark,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "آرڈر اور لائلٹی کوائنز کے لیے سائن ان کریں",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PolishTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PolishBgLight,
                    contentColor = PolishPrimaryRed,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PolishPrimaryRed
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Guest 🏃",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Mobile OTP 📱",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "Gmail 📧",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // GUEST MODE
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Continue as Guest: You can browse the entire menu, customize pizzas, and place Cash on Delivery or Easypaisa orders directly without password!",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PolishTextMuted,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = guestName,
                                onValueChange = { guestName = it },
                                label = { Text("Your Name (آپ کا نام)") },
                                placeholder = { Text("e.g. Ali Ahmed") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PolishPrimaryRed)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PolishPrimaryRed,
                                    unfocusedBorderColor = PolishInputBorder
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    1 -> {
                        // MOBILE OTP MODE
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customerNameInput,
                                onValueChange = { customerNameInput = it },
                                label = { Text("Full Name (نام)") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PolishPrimaryRed)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PolishPrimaryRed,
                                    unfocusedBorderColor = PolishInputBorder
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = {
                                    phoneInput = it
                                    phoneError = null
                                },
                                label = { Text("Mobile Number (موبائل نمبر)") },
                                placeholder = { Text("0300 1234567") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Send),
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PolishPrimaryRed)
                                },
                                trailingIcon = {
                                    TextButton(
                                        onClick = {
                                            if (phoneInput.trim().length < 10) {
                                                phoneError = "Please enter a valid Pakistani mobile number"
                                            } else {
                                                val otp = onRequestOtp(phoneInput.trim())
                                                isOtpSent = true
                                                sentOtpHint = otp
                                                otpInput = otp // Auto-fill for ultra smooth testing!
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = if (isOtpSent) "Resend" else "Get OTP",
                                            color = PolishPrimaryRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PolishPrimaryRed,
                                    unfocusedBorderColor = PolishInputBorder
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            AnimatedVisibility(visible = isOtpSent) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        color = PolishGreenSuccess.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, PolishGreenSuccess.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PolishGreenSuccess, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SMS Sent! Code is: $sentOtpHint (Auto-filled)",
                                                color = PolishGreenSuccess,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = otpInput,
                                        onValueChange = {
                                            otpInput = it
                                            phoneError = null
                                        },
                                        label = { Text("4-Digit OTP Code") },
                                        placeholder = { Text("e.g. $sentOtpHint") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.Pin, contentDescription = null, tint = PolishPrimaryRed)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PolishPrimaryRed,
                                            unfocusedBorderColor = PolishInputBorder
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            if (phoneError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = phoneError!!,
                                    color = PolishPrimaryRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }

                    2 -> {
                        // GMAIL MODE
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Sign in with your Google / Gmail account for 1-tap instant orders & earning Smile Coins on every pizza:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PolishTextMuted,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = gmailNameInput,
                                onValueChange = { gmailNameInput = it },
                                label = { Text("Your Name (نام)") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PolishPrimaryRed)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PolishPrimaryRed,
                                    unfocusedBorderColor = PolishInputBorder
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = gmailInput,
                                onValueChange = { gmailInput = it },
                                label = { Text("Gmail Address") },
                                placeholder = { Text("your.email@gmail.com") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                leadingIcon = {
                                    Text(text = "G", fontWeight = FontWeight.Bold, color = PolishPrimaryRed, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PolishPrimaryRed,
                                    unfocusedBorderColor = PolishInputBorder
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> onContinueAsGuest(guestName)
                        1 -> {
                            if (!isOtpSent) {
                                val otp = onRequestOtp(phoneInput.trim())
                                isOtpSent = true
                                sentOtpHint = otp
                                otpInput = otp
                            } else {
                                val success = onVerifyOtpAndLogin(phoneInput, otpInput, customerNameInput)
                                if (!success) {
                                    phoneError = "Invalid OTP code. Please retry!"
                                }
                            }
                        }
                        2 -> onLoginWithGoogle(gmailInput, gmailNameInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishPrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("auth_confirm_btn")
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "Continue as Guest 🍕"
                        1 -> if (isOtpSent) "Verify & Login ✅" else "Send OTP 📲"
                        2 -> "Sign In with Google 🚀"
                        else -> "Confirm"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Text(text = "Close", color = PolishTextMuted)
            }
        }
    )
}
