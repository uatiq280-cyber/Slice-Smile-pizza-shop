package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.ui.components.FoodCard
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

@Composable
fun MenuScreen(
    menuItems: List<MenuItem>,
    selectedCategory: MenuCategory,
    searchQuery: String,
    cartCount: Int,
    cartSubtotal: Int,
    onCategorySelected: (MenuCategory) -> Unit,
    onSearchChanged: (String) -> Unit,
    onQuickAdd: (MenuItem) -> Unit,
    onCustomizeClick: (MenuItem) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToLoyalty: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(PolishBgLight)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Hero Pizzeria Banner
            item {
                PizzeriaHeroBanner(onLoyaltyClick = onNavigateToLoyalty)
            }

            // 2. Loyalty Highlight Card (Professional Polish Style)
            item {
                LoyaltyQuickBanner(onLoyaltyClick = onNavigateToLoyalty)
            }

            // 3. Search Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = { Text("Search pizzas, burgers, shawarma, deals...", fontSize = 13.5.sp, color = PolishTextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PolishPrimaryRed
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChanged("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = PolishTextMuted)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("menu_search_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimaryRed,
                            unfocusedBorderColor = PolishInputBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = PolishTextDark,
                            unfocusedTextColor = PolishTextDark
                        )
                    )
                }
            }

            // 4. Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MenuCategory.entries.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PolishMaroonDark else Color.White,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onCategorySelected(category) }
                                .testTag("category_chip_${category.name}")
                        ) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = if (isSelected) PolishPrimaryContainer else PolishTextMuted,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Section Title / Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results" else selectedCategory.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "${menuItems.size} items",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PolishTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Menu Items List
            if (menuItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🍕", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No items found matching '$searchQuery'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PolishTextMuted
                            )
                        }
                    }
                }
            } else {
                items(menuItems, key = { it.id }) { item ->
                    FoodCard(
                        item = item,
                        onQuickAdd = { onQuickAdd(item) },
                        onCustomizeClick = { onCustomizeClick(item) }
                    )
                }
            }
        }

        // Floating Cart Bar (if items in cart)
        if (cartCount > 0) {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCart,
                containerColor = PolishMaroonDark,
                contentColor = PolishPrimaryContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .testTag("floating_cart_fab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        tint = PolishPrimaryContainer
                    )
                    Text(
                        text = "$cartCount Item${if (cartCount > 1) "s" else ""} • Rs. $cartSubtotal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PolishPrimaryContainer
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Proceed",
                        tint = PolishPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoyaltyQuickBanner(onLoyaltyClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onLoyaltyClick() }
            .testTag("menu_loyalty_quick_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderStrong),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Rewards",
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Loyalty Rewards",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishMaroonDark
                    )
                }
                Text(
                    text = "Earn coins on min Rs. 1,500 order",
                    fontSize = 11.5.sp,
                    color = PolishTextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "100 Coins = Rs 10 Discount",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishPrimaryRed
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SMILE CLUB",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishPrimaryRed,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "REWARDS →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = PolishMaroonDark
                )
            }
        }
    }
}

@Composable
fun PizzeriaHeroBanner(onLoyaltyClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.pizza_hero_banner),
                contentDescription = "Slice Smile Pizza Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content inside banner
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Free Home Delivery Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishPrimaryRed
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "FREE DELIVERY (Min Rs 500 in 3KM)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Smile Coins Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishPrimaryContainer,
                        modifier = Modifier.clickable { onLoyaltyClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🪙 VIP Club",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PolishMaroonDark
                                )
                            )
                        }
                    }
                }

                // Title & Subtitle
                Column {
                    Text(
                        text = "SLICE SMILE PIZZA",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Hot & Fresh Pizzas, Zinger Burgers, Shawarma & Deals",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "📍 Chowk Nazir Wala • 📞 0303-7448255 / 0303-5574979",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }
    }
}

