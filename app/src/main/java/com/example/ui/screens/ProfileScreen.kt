package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MenuDataSource
import com.example.model.AuthType
import com.example.model.CustomerFeedback
import com.example.model.LoyaltyProfile
import com.example.model.UserSession
import com.example.ui.components.QrCodeView
import com.example.ui.theme.PolishAccentGold
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCardBg
import com.example.ui.theme.PolishGreenSuccess
import com.example.ui.theme.PolishInputBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishOrangeAlert
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted
import com.example.util.QrCodeGenerator

@Composable
fun ProfileScreen(
    userSession: UserSession,
    loyaltyProfile: LoyaltyProfile,
    reviews: List<CustomerFeedback>,
    isAdminLoggedIn: Boolean,
    isRiderLoggedIn: Boolean = false,
    onOpenAuthDialog: () -> Unit,
    onLogoutCustomer: () -> Unit,
    onChangeAddressClick: () -> Unit,
    onOpenAdminPortal: () -> Unit,
    onOpenRiderPortal: () -> Unit = {},
    onNavigateToOrders: () -> Unit,
    onNavigateToLoyalty: () -> Unit,
    onShareQrCode: () -> Unit = {},
    onShareInviteText: () -> Unit = {},
    onCopyReferralCode: () -> Unit = {},
    onApplyReferralCode: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var friendReferralInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBgLight),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. Customer Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(PolishPrimaryRed, PolishMaroonDark)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (userSession.name.isNotBlank()) userSession.name.take(1).uppercase() else "P",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = userSession.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextDark,
                                        fontSize = 17.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Surface(
                                    color = when (userSession.authType) {
                                        AuthType.GUEST -> PolishPrimaryContainerSubtle
                                        AuthType.PHONE_OTP -> PolishGreenSuccess.copy(alpha = 0.15f)
                                        AuthType.GOOGLE_GMAIL -> Color(0xFFE8F0FE)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = userSession.displaySubtitle,
                                        color = when (userSession.authType) {
                                            AuthType.GUEST -> PolishPrimaryRed
                                            AuthType.PHONE_OTP -> PolishGreenSuccess
                                            AuthType.GOOGLE_GMAIL -> Color(0xFF1967D2)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onOpenAuthDialog,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PolishPrimaryRed.copy(alpha = 0.1f),
                                contentColor = PolishPrimaryRed
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (userSession.authType == AuthType.GUEST) "Login 🔑" else "Switch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Coins & Orders Quick Glance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToLoyalty() },
                            color = PolishAccentGold.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PolishAccentGold.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${loyaltyProfile.currentCoins} Coins",
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextDark,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "= Rs ${loyaltyProfile.discountValueInRupees} Off",
                                        color = PolishAccentGold,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToOrders() },
                            color = PolishPrimaryRed.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PolishPrimaryRed.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📦", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${loyaltyProfile.totalOrdersCount} Orders",
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextDark,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "View Status",
                                        color = PolishPrimaryRed,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    if (userSession.authType != AuthType.GUEST) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Log out from account",
                                color = PolishTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onLogoutCustomer() }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. QR Code Referral & Invite Section (10% Discount Promotion)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("referral_qr_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Banner with discount callout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PolishPrimaryContainerSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎁", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Invite Friends • Earn 10% OFF",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishMaroonDark,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Both get 10% Discount on orders!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PolishPrimaryRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                        ) {
                            Text(
                                text = "10% OFF",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Show this QR code to friends or share your invite link. When they order with your code, they get 10% OFF and you unlock a 10% discount on your next pizza!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextMuted,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code View with custom centered pizza emblem
                    QrCodeView(
                        data = loyaltyProfile.referralShareLink,
                        size = 190.dp,
                        darkColor = PolishMaroonDark,
                        modifier = Modifier.testTag("customer_profile_qr_code")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Referral Code Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (onCopyReferralCode != {}) {
                                    onCopyReferralCode()
                                } else {
                                    QrCodeGenerator.copyToClipboard(context, loyaltyProfile.referralCode)
                                }
                            }
                            .testTag("copy_referral_code_box"),
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, PolishAccentGold)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "YOUR REFERRAL CODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishMaroonDark,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = loyaltyProfile.referralCode,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PolishPrimaryRed
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PolishPrimaryRed.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = PolishPrimaryRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PolishPrimaryRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sharing Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (onShareQrCode != {}) {
                                    onShareQrCode()
                                } else {
                                    QrCodeGenerator.shareReferralQrImage(
                                        context,
                                        loyaltyProfile.referralCode,
                                        userSession.name
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_qr_image_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PolishMaroonDark,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share QR Card 📸",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (onShareInviteText != {}) {
                                    onShareInviteText()
                                } else {
                                    QrCodeGenerator.shareReferralText(
                                        context,
                                        loyaltyProfile.referralCode,
                                        userSession.name
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_invite_text_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Invite Friends 💬",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Referral Reward Stats Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishBgLight)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${loyaltyProfile.successfulReferralsCount}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = PolishMaroonDark
                            )
                            Text(
                                text = "Friends Invited",
                                fontSize = 11.sp,
                                color = PolishTextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(PolishBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${loyaltyProfile.availableReferralDiscountsCount}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = PolishPrimaryRed
                            )
                            Text(
                                text = "10% Discounts Ready",
                                fontSize = 11.sp,
                                color = PolishTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Redeem Friend's Code Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFBF9F7))
                            .border(1.dp, PolishBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Got an Invite from a Friend? 🎟️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PolishMaroonDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = friendReferralInput,
                                onValueChange = { friendReferralInput = it.uppercase() },
                                placeholder = { Text("Enter SMILE-XXXX", fontSize = 12.sp, color = PolishTextMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("friend_referral_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = PolishPrimaryRed,
                                    unfocusedBorderColor = PolishInputBorder
                                )
                            )

                            Button(
                                onClick = {
                                    if (friendReferralInput.isNotBlank()) {
                                        onApplyReferralCode(friendReferralInput.trim())
                                        friendReferralInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PolishPrimaryRed,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier.testTag("apply_friend_referral_btn")
                            ) {
                                Text(
                                    text = "Claim",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Delivery Address Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PolishPrimaryContainerSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PolishPrimaryRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Delivery Address (ڈلیوری ایڈریس)",
                                fontWeight = FontWeight.Bold,
                                color = PolishTextDark,
                                fontSize = 13.sp
                            )
                            Text(
                                text = userSession.deliveryAddress,
                                color = PolishTextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onChangeAddressClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Address",
                            tint = PolishPrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Customer Quick Contact / Help Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Customer Support & Helpline 📞",
                        fontWeight = FontWeight.Bold,
                        color = PolishTextDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${MenuDataSource.SHOP_PHONE}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PolishGreenSuccess,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Call Shop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val url = "https://wa.me/${MenuDataSource.SHOP_WHATSAPP}?text=Hello%20Slice%20Smile,%20I%20need%20help%20with%20my%20order"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "💬 WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
