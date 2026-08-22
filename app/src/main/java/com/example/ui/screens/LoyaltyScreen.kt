package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LoyaltyProfile
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishBorderStrong
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@Composable
fun LoyaltyScreen(
    loyaltyProfile: LoyaltyProfile,
    onNavigateToMenu: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBgLight),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. VIP Card Header (Dark Maroon Gradient with Gold accents)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loyalty_vip_card"),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PolishMaroonDark,
                                    Color(0xFF6B1D15),
                                    Color(0xFF2B0002)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SLICE SMILE CLUB",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PolishPrimaryContainer,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.8.sp
                                    )
                                )
                                Text(
                                    text = "Loyalty Coins Balance",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PolishPrimaryContainer.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = PolishPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${loyaltyProfile.currentCoins}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 44.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Smile Coins",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PolishPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🪙 Value: Rs ${loyaltyProfile.discountEquivalentRs} Off (100 coins = Rs 10)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PolishPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Stats Grid
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LoyaltyStatCard(
                    title = "Lifetime Earned",
                    value = "${loyaltyProfile.totalCoinsEarnedLifetime} 🪙",
                    modifier = Modifier.weight(1f)
                )
                LoyaltyStatCard(
                    title = "Redeemed",
                    value = "${loyaltyProfile.totalCoinsRedeemedLifetime} 🪙",
                    modifier = Modifier.weight(1f)
                )
                LoyaltyStatCard(
                    title = "Total Orders",
                    value = "${loyaltyProfile.totalOrdersCount} 🍕",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. How it Works (Rules & Rewards)
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
                        text = "Loyalty Program Rules",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LoyaltyRuleItem(
                        icon = Icons.Default.MonetizationOn,
                        title = "Minimum Rs. 1,500 Order",
                        description = "Every order worth Rs 1,500 or more earns 100 Smile Coins automatically."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LoyaltyRuleItem(
                        icon = Icons.Default.CardGiftcard,
                        title = "100 Coins = Rs 10 Cash Discount",
                        description = "Every 100 coins gives you Rs 10 off during checkout on your next pizza order."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LoyaltyRuleItem(
                        icon = Icons.Default.ShoppingBag,
                        title = "1-Click Auto Redemption",
                        description = "Toggle the coin discount switch directly in the Cart to apply instant savings."
                    )
                }
            }
        }

        // 4. Action Button
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onNavigateToMenu,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishMaroonDark,
                    contentColor = PolishPrimaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .testTag("loyalty_order_pizza_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.LocalPizza,
                    contentDescription = null,
                    tint = PolishPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Order Now to Earn Coins 🍕",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PolishPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LoyaltyStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = PolishTextMuted
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = PolishPrimaryRed
                )
            )
        }
    }
}

@Composable
fun LoyaltyRuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PolishPrimaryContainerSubtle)
                .border(1.dp, PolishBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PolishPrimaryRed,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishMaroonDark
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PolishTextMuted,
                    lineHeight = 17.sp
                )
            )
        }
    }
}

