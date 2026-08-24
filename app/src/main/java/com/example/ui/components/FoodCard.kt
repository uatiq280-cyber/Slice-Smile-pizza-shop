package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishBorderStrong
import com.example.ui.theme.PolishInputBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodCard(
    item: MenuItem,
    onQuickAdd: () -> Unit,
    onCustomizeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCustomizeClick() }
            .testTag("food_card_${item.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main Top Content: Icon Container + Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Leading Icon in Polish Soft Peach Container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PolishPrimaryContainerSubtle)
                        .border(1.dp, PolishBorder, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(item.category),
                        contentDescription = item.name,
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title & Price Section
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                color = PolishMaroonDark
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (item.sizeOptions.isEmpty()) {
                            Text(
                                text = "Rs ${item.basePrice}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PolishPrimaryRed,
                                    fontSize = 16.sp
                                )
                            )
                        } else {
                            Text(
                                text = "Rs ${item.sizeOptions.first().price}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PolishPrimaryRed,
                                    fontSize = 14.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp,
                            color = PolishTextMuted
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tags row (Category Tag, Spicy, Deals)
            if (item.tag != null || item.isSpicy) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.tag != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PolishPrimaryContainer,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, PolishBorderStrong)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = PolishPrimaryRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.tag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PolishPrimaryRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    if (item.isSpicy) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFECE8)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Spicy",
                                    tint = PolishPrimaryRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Spicy",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PolishPrimaryRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Deal contents bullet list if present
            if (item.dealIncludes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PolishBgLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Deal Includes:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimaryRed,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item.dealIncludes.forEach { dealPart ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PolishPrimaryRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = dealPart,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = PolishTextDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Size Options Chips if available
            if (item.sizeOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.sizeOptions.forEach { opt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PolishBgLight)
                                .border(1.dp, PolishBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${opt.size.label}: Rs ${opt.price}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PolishTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCustomizeClick,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishInputBorder),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("customize_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Customize",
                        tint = PolishTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.sizeOptions.isNotEmpty()) "Select Size" else "Customize",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onQuickAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimaryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("quick_add_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to Cart",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: MenuCategory): ImageVector {
    return when (category) {
        MenuCategory.DEALS, MenuCategory.FAMILY_DEALS, MenuCategory.BIRTHDAY_DEALS -> Icons.Default.Fastfood
        MenuCategory.PIZZA, MenuCategory.SPECIAL_PIZZA -> Icons.Default.LocalPizza
        MenuCategory.BURGER, MenuCategory.SHAWARMA -> Icons.Default.LunchDining
        MenuCategory.BROAST, MenuCategory.WINGS_FRIES, MenuCategory.WRAP -> Icons.Default.Fastfood
        MenuCategory.PASTA -> Icons.Default.DinnerDining
        MenuCategory.CHINESE -> Icons.Default.RamenDining
        MenuCategory.COLD_DRINKS, MenuCategory.JUICES_SHAKES -> Icons.Default.LocalDrink
        else -> Icons.Default.LocalPizza
    }
}

