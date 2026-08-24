package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.Rider
import com.example.ui.components.WhatsAppOrderHelper
import com.example.ui.theme.BasilGreen
import com.example.ui.theme.CheeseAmber
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishBorderStrong
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.WhatsAppGreen

@Composable
fun AdminPanelScreen(
    menuItems: List<MenuItem>,
    orders: List<Order>,
    riders: List<Rider>,
    onAddNewItem: () -> Unit,
    onEditItem: (MenuItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onToggleStock: (MenuItem, Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    onChangePinClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    onUpdateOrderStatus: (orderId: Long, nextStatus: OrderStatus) -> Unit,
    onAssignRiderClick: (Order) -> Unit,
    onAddRiderClick: () -> Unit,
    onEditRiderClick: (Rider) -> Unit,
    onDeleteRiderClick: (String) -> Unit,
    onToggleRiderEnabled: (Rider, Boolean) -> Unit,
    onTestNotificationSound: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Orders, 1: Menu & Prices, 2: Riders Fleet
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MenuCategory?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var itemPendingDelete by remember { mutableStateOf<MenuItem?>(null) }
    var riderPendingDelete by remember { mutableStateOf<Rider?>(null) }

    val filteredItems = remember(menuItems, searchQuery, selectedCategory) {
        menuItems.filter { item ->
            val matchesCategory = selectedCategory == null || selectedCategory == MenuCategory.ALL || item.category == selectedCategory
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                item.name.lowercase().contains(q) ||
                item.description.lowercase().contains(q) ||
                item.dealIncludes.any { it.lowercase().contains(q) } ||
                (item.tag?.lowercase()?.contains(q) == true)
            }
            matchesCategory && matchesSearch
        }
    }

    val pendingOrdersCount = orders.count { it.status != OrderStatus.DELIVERED }
    val outOfStockCount = menuItems.count { !it.isAvailable }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = onAddNewItem,
                    containerColor = PolishPrimaryRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("admin_add_item_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Deal")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Item ➕",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = onAddRiderClick,
                    containerColor = PolishPrimaryRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("admin_add_rider_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rider")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Rider 🛵",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PolishBgLight)
                .padding(innerPadding)
        ) {
            // 1. Header Banner
            Surface(
                color = PolishMaroonDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Owner / Admin Portal 👑",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                )
                                Text(
                                    text = "Orders, Sound Alert & Rider Management",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Logout button
                        OutlinedButton(
                            onClick = onLogoutClick,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminStatChip(
                            title = "Active Orders",
                            value = "$pendingOrdersCount",
                            isAlert = pendingOrdersCount > 0,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatChip(
                            title = "Menu Items",
                            value = "${menuItems.size}",
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatChip(
                            title = "Riders Fleet",
                            value = "${riders.size}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Top Action Buttons (Test Sound & Change PIN)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTestNotificationSound,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PolishPrimaryRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Ring/Sound 🔔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onChangePinClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change PIN 🔑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PolishPrimaryRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PolishPrimaryRed,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Orders (${orders.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) PolishPrimaryRed else PolishTextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Menu Rates (${menuItems.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) PolishPrimaryRed else PolishTextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Riders (${riders.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 2) PolishPrimaryRed else PolishTextMuted
                        )
                    }
                )
            }

            // 3. Tab Contents
            when (selectedTab) {
                0 -> {
                    // Orders Management Tab
                    AdminOrdersList(
                        orders = orders,
                        onUpdateOrderStatus = onUpdateOrderStatus,
                        onAssignRiderClick = onAssignRiderClick
                    )
                }
                1 -> {
                    // Menu Management Tab
                    AdminMenuList(
                        menuItems = filteredItems,
                        totalCount = menuItems.size,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        onEditItem = onEditItem,
                        onDeleteItem = { itemPendingDelete = it },
                        onToggleStock = onToggleStock,
                        onResetConfirm = { showResetConfirmDialog = true }
                    )
                }
                2 -> {
                    // Riders Fleet Tab
                    AdminRidersList(
                        riders = riders,
                        onAddRider = onAddRiderClick,
                        onEditRider = onEditRiderClick,
                        onDeleteRider = { riderPendingDelete = it },
                        onToggleRiderEnabled = onToggleRiderEnabled
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Resetting Menu
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = PolishPrimaryRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Reset All Menu & Rates?",
                    fontWeight = FontWeight.Bold,
                    color = PolishTextDark
                )
            },
            text = {
                Text(
                    text = "All items and rates will be reset to default settings. Are you sure you want to proceed?",
                    color = PolishTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetDefaults()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryRed)
                ) {
                    Text("Yes, Reset Menu 🔄", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = PolishTextMuted)
                }
            }
        )
    }

    // Confirmation Dialog for Deleting Item
    itemPendingDelete?.let { itemToDelete ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = PolishPrimaryRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Delete '${itemToDelete.name}'?",
                    fontWeight = FontWeight.Bold,
                    color = PolishTextDark
                )
            },
            text = {
                Text(
                    text = "This item will be permanently removed from your active restaurant menu.",
                    color = PolishTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = itemToDelete.id
                        itemPendingDelete = null
                        onDeleteItem(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryRed)
                ) {
                    Text("Delete 🗑️", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemPendingDelete = null }) {
                    Text("Cancel", color = PolishTextMuted)
                }
            }
        )
    }

    // Confirmation Dialog for Deleting Rider
    riderPendingDelete?.let { riderToDelete ->
        AlertDialog(
            onDismissRequest = { riderPendingDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = PolishPrimaryRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Remove Rider '${riderToDelete.name}'?",
                    fontWeight = FontWeight.Bold,
                    color = PolishTextDark
                )
            },
            text = {
                Text(
                    text = "This rider will be removed from your delivery fleet.",
                    color = PolishTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = riderToDelete.id
                        riderPendingDelete = null
                        onDeleteRiderClick(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryRed)
                ) {
                    Text("Remove 🗑️", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { riderPendingDelete = null }) {
                    Text("Cancel", color = PolishTextMuted)
                }
            }
        )
    }
}

// ---------------- TAB 0: ORDERS MANAGEMENT ----------------
@Composable
private fun AdminOrdersList(
    orders: List<Order>,
    onUpdateOrderStatus: (orderId: Long, nextStatus: OrderStatus) -> Unit,
    onAssignRiderClick: (Order) -> Unit
) {
    val context = LocalContext.current

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Moped,
                    contentDescription = null,
                    tint = PolishTextMuted,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Customer Orders Placed Yet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishMaroonDark
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When customers place orders, you will receive real-time push alerts and sound notifications here!",
                    style = MaterialTheme.typography.bodySmall,
                    color = PolishTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(orders, key = { it.orderId }) { order ->
                AdminOrderCard(
                    order = order,
                    onUpdateStatus = { nextStatus -> onUpdateOrderStatus(order.orderId, nextStatus) },
                    onAssignRider = { onAssignRiderClick(order) }
                )
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
    onAssignRider: () -> Unit
) {
    val context = LocalContext.current
    val isDelivered = order.status == OrderStatus.DELIVERED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.orderId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDelivered) Color(0xFFA5D6A7) else PolishBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.status) {
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        OrderStatus.OUT_FOR_DELIVERY -> PolishPrimaryContainerSubtle
                        OrderStatus.READY_FOR_PICKUP -> Color(0xFFFFF3E0)
                        OrderStatus.PREPARING_PIZZA -> Color(0xFFFFFDE7)
                        else -> PolishBgLight
                    }
                ) {
                    Text(
                        text = "${order.status.iconEmoji} ${order.status.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = when (order.status) {
                                OrderStatus.DELIVERED -> Color(0xFF2E7D32)
                                OrderStatus.OUT_FOR_DELIVERY -> PolishPrimaryRed
                                OrderStatus.READY_FOR_PICKUP -> Color(0xFFE65100)
                                else -> PolishMaroonDark
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = PolishBorder)

            // Customer details & quick contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Customer: ${order.customerName}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark
                        )
                    )
                    Text(
                        text = "${order.customerPhone} • ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted),
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            WhatsAppOrderHelper.sendRawWhatsAppMessage(
                                context,
                                order.customerPhone,
                                "Assalam-o-Alaikum ${order.customerName}! Your order #${order.orderId} is being prepared at Slice Smile Pizza Shop."
                            )
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = { WhatsAppOrderHelper.makePhoneCall(context, order.customerPhone) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PolishPrimaryRed)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items summary
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall.copy(color = PolishTextDark, lineHeight = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PolishBgLight, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment & Assigned Rider Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: Rs. ${order.totalAmount} (${if (order.paymentMethod == PaymentMethod.EASYPAISA) "Easypaisa" else "COD"})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishPrimaryRed
                        )
                    )
                    Text(
                        text = if (order.riderName.isNotBlank() && order.riderName != "Slice Smile Express Delivery") "Rider: ${order.riderName} (${order.riderPhone})" else "No rider assigned yet",
                        style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                    )
                }

                OutlinedButton(
                    onClick = onAssignRider,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishPrimaryRed)
                ) {
                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = PolishPrimaryRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assign Rider 🛵", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PolishPrimaryRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Quick Workflow Action Button
            when (order.status) {
                OrderStatus.ORDER_RECEIVED -> {
                    Button(
                        onClick = { onUpdateStatus(OrderStatus.PREPARING_PIZZA) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("admin_accept_order_btn_${order.orderId}")
                    ) {
                        Text("✅ Accept Order & Start Baking 🧑‍🍳", fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                    }
                }
                OrderStatus.PREPARING_PIZZA -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.READY_FOR_PICKUP) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE65100),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Mark Ready 🍕", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.OUT_FOR_DELIVERY) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PolishPrimaryRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Dispatch 🛵", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                OrderStatus.READY_FOR_PICKUP -> {
                    Button(
                        onClick = { onUpdateStatus(OrderStatus.OUT_FOR_DELIVERY) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Dispatch / Out for Delivery 🛵", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                OrderStatus.OUT_FOR_DELIVERY -> {
                    Button(
                        onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Mark Delivered 🎉", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                OrderStatus.DELIVERED -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🎉 Order Delivered Successfully", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 12.5.sp)
                        }
                    }
                }
                OrderStatus.CANCELLED -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFEBEE),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("❌ Order Cancelled", fontWeight = FontWeight.Bold, color = Color(0xFFC62828), fontSize = 12.5.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Manual Status Buttons Row (All 5 Stages)
            Text(
                text = "Manual Status Override:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PolishTextDark)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OrderStatus.entries.forEach { status ->
                    val isCurrent = order.status == status
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) PolishPrimaryRed else PolishBgLight,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) PolishPrimaryRed else PolishBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onUpdateStatus(status) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(status.iconEmoji, fontSize = 13.sp)
                            Text(
                                text = when (status) {
                                    OrderStatus.ORDER_RECEIVED -> "Received"
                                    OrderStatus.PREPARING_PIZZA -> "Preparing"
                                    OrderStatus.READY_FOR_PICKUP -> "Ready"
                                    OrderStatus.OUT_FOR_DELIVERY -> "Out"
                                    OrderStatus.DELIVERED -> "Delivered"
                                    OrderStatus.CANCELLED -> "Cancelled"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Medium,
                                    color = if (isCurrent) Color.White else PolishMaroonDark
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 1: MENU & RATES MANAGEMENT ----------------
@Composable
private fun AdminMenuList(
    menuItems: List<MenuItem>,
    totalCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: MenuCategory?,
    onCategoryChange: (MenuCategory?) -> Unit,
    onEditItem: (MenuItem) -> Unit,
    onDeleteItem: (MenuItem) -> Unit,
    onToggleStock: (MenuItem, Boolean) -> Unit,
    onResetConfirm: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search deal, pizza, burger to edit rate...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PolishTextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_search_bar")
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null || selectedCategory == MenuCategory.ALL,
                            onClick = { onCategoryChange(MenuCategory.ALL) },
                            label = { Text("All ($totalCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishPrimaryRed,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            )
                        )
                    }
                    items(MenuCategory.entries.filter { it != MenuCategory.ALL }) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishPrimaryRed,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu Items (${menuItems.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = PolishTextDark)
                )
                TextButton(onClick = onResetConfirm) {
                    Text("Reset Menu Defaults 🔄", color = PolishPrimaryRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(menuItems, key = { it.id }) { item ->
            AdminItemManagementCard(
                item = item,
                onEditClick = { onEditItem(item) },
                onDeleteClick = { onDeleteItem(item) },
                onToggleStock = { inStock -> onToggleStock(item, inStock) }
            )
        }
    }
}

// ---------------- TAB 2: RIDERS FLEET MANAGEMENT ----------------
@Composable
private fun AdminRidersList(
    riders: List<Rider>,
    onAddRider: () -> Unit,
    onEditRider: (Rider) -> Unit,
    onDeleteRider: (Rider) -> Unit,
    onToggleRiderEnabled: (Rider, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delivery Riders (${riders.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark
                    )
                )

                Button(
                    onClick = onAddRider,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New Rider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(riders, key = { it.id }) { rider ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_rider_card_${rider.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PolishPrimaryContainerSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = PolishPrimaryRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = rider.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishTextDark
                                    )
                                )
                                Text(
                                    text = "${rider.phone} • PIN: ${rider.pin}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = PolishPrimaryRed, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (rider.isEnabled) "Enabled" else "Disabled",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (rider.isEnabled) BasilGreen else PolishPrimaryRed
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = rider.isEnabled,
                                onCheckedChange = { onToggleRiderEnabled(rider, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BasilGreen
                                ),
                                modifier = Modifier.size(width = 38.dp, height = 24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Vehicle: ${rider.vehicle} • Deliveries: ${rider.totalDeliveries} • Rating: ⭐ ${rider.rating}",
                        style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onEditRider(rider) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryRed),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Rider ✏️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onDeleteRider(rider) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishPrimaryRed),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatChip(
    title: String,
    value: String,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) PolishPrimaryRed else Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 16.sp
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun AdminItemManagementCard(
    item: MenuItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleStock: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("admin_item_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isAvailable) Color.White else Color(0xFFF1F5F9)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isAvailable) PolishBorder else PolishBorderStrong
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category Badge + Tag + Stock Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(PolishPrimaryContainerSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.category.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimaryRed,
                                fontSize = 10.sp
                            )
                        )
                    }

                    item.tag?.let { tagText ->
                        Box(
                            modifier = Modifier
                                .background(CheeseAmber.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tagText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishMaroonDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.isAvailable) "In Stock" else "Out of Stock",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isAvailable) BasilGreen else PolishPrimaryRed,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = onToggleStock,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BasilGreen
                        ),
                        modifier = Modifier.size(width = 38.dp, height = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = PolishTextDark,
                    fontSize = 15.sp
                )
            )

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PolishTextMuted,
                        fontSize = 12.sp
                    ),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PolishBgLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                if (item.sizeOptions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.sizeOptions.forEach { opt ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = opt.size.label.take(1).uppercase(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PolishTextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = "Rs. ${opt.price}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishTextDark,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Item Rate (قیمت):",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PolishTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Rs. ${item.basePrice}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishPrimaryRed,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimaryRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_edit_btn_${item.id}"),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Rate / Deal ✏️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDeleteClick,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishPrimaryRed),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
