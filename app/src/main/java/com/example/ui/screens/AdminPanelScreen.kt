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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.data.repository.CloudSyncStatus
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh

@Composable
fun AdminPanelScreen(
    menuItems: List<MenuItem>,
    orders: List<Order>,
    riders: List<Rider>,
    cloudSyncStatus: CloudSyncStatus = CloudSyncStatus(),
    isRefreshingOrders: Boolean = false,
    onRefreshOrders: () -> Unit = {},
    onTestCloudConnection: () -> Unit = {},
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
    var showCloudGuideDialog by remember { mutableStateOf(false) }
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

                    // Live Cloud Sync Status & 2-Device Multi-Device Link Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (cloudSyncStatus.isConnected) Color(0xFF1B5E20).copy(alpha = 0.9f) else Color(0xFFB71C1C).copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (cloudSyncStatus.isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = if (cloudSyncStatus.isConnected) Color(0xFF81C784) else Color(0xFFFF8A80),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (cloudSyncStatus.isConnected) "Cloud Live 🟢 (2-Device Sync Active)" else "Cloud Notice 🔴 (Check Firebase Rules)",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = onTestCloudConnection,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.NetworkCheck,
                                            contentDescription = "Test Cloud Link",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showCloudGuideDialog = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.HelpOutline,
                                            contentDescription = "Cloud Setup Guide",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (!cloudSyncStatus.errorMessage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cloudSyncStatus.errorMessage,
                                    color = Color(0xFFFFCDD2),
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }

                            if (!cloudSyncStatus.pingResult.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cloudSyncStatus.pingResult,
                                    color = Color(0xFFFFF59D),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = onRefreshOrders,
                                    enabled = !isRefreshingOrders,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.25f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    if (isRefreshingOrders) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                    }
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Sync 🔄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = onTestCloudConnection,
                                    enabled = !cloudSyncStatus.isTestingPing,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF57C00),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    if (cloudSyncStatus.isTestingPing) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(13.dp))
                                    }
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Test Link ⚡", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showCloudGuideDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.25f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Rules 📖", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
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

            // 2. Navigation Tabs (Dashboard, Orders, Menu Rates, Riders)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PolishPrimaryRed,
                edgePadding = 12.dp,
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
                            "📊 Dashboard",
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
                            "🔔 Orders (${orders.size})",
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
                            "🍕 Menu Rates (${menuItems.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 2) PolishPrimaryRed else PolishTextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            "🛵 Riders (${riders.size})",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 3) PolishPrimaryRed else PolishTextMuted
                        )
                    }
                )
            }

            // 3. Tab Contents
            when (selectedTab) {
                0 -> {
                    // Analytics & Income Dashboard Tab
                    AdminAnalyticsDashboard(
                        orders = orders,
                        menuItems = menuItems,
                        isRefreshing = isRefreshingOrders,
                        onRefreshClick = onRefreshOrders
                    )
                }
                1 -> {
                    // Orders Management Tab
                    AdminOrdersList(
                        orders = orders,
                        isRefreshing = isRefreshingOrders,
                        onRefreshClick = onRefreshOrders,
                        onUpdateOrderStatus = onUpdateOrderStatus,
                        onAssignRiderClick = onAssignRiderClick
                    )
                }
                2 -> {
                    // Menu Management Tab
                    AdminMenuList(
                        menuItems = filteredItems,
                        totalCount = menuItems.size,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        onAddNewItem = onAddNewItem,
                        onEditItem = onEditItem,
                        onDeleteItem = { itemPendingDelete = it },
                        onToggleStock = onToggleStock,
                        onResetConfirm = { showResetConfirmDialog = true }
                    )
                }
                3 -> {
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

    // Live Multi-Device Cloud Setup & Rules Guide Dialog
    if (showCloudGuideDialog) {
        AlertDialog(
            onDismissRequest = { showCloudGuideDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = BasilGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Live Multi-Device Connection Guide 🌐",
                    fontWeight = FontWeight.Bold,
                    color = PolishTextDark,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "اگر کسٹمر کے موبائل سے دیا گیا آرڈر آپ کے ایڈمن پینل پر فورا نہیں آ رہا، تو Firebase Console میں درج ذیل 2 آسان کام چیک کریں:",
                        color = PolishTextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        color = PolishBgLight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "1. Firebase Console -> Firestore Database -> Rules:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = PolishPrimaryRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "rules_version = '2';\nservice cloud.firestore {\n  match /databases/{database}/documents {\n    match /{document=**} {\n      allow read, write: if true;\n    }\n  }\n}",
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "(اس سے دونوں موبائلز کا ڈیٹا بغیر کسی رکاوٹ کلاؤڈ پر لائیو سنک ہوتا ہے)",
                                fontSize = 10.5.sp,
                                color = PolishTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = PolishBgLight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "2. Firebase Console -> Authentication -> Sign-in method:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = PolishPrimaryRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "'Anonymous' کو Enable کر دیں۔ اس سے ہر کسٹمر موبائل خود بخود تصدیق ہو کر آن لائن ہو جاتا ہے۔",
                                fontSize = 11.sp,
                                color = PolishTextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Project ID: slice-smile-pizza-shop-2026\nیہ دونوں موبائلز ایک ہی Firebase پروجیکٹ کے ذریعے 1 سیکنڈ میں ریئل ٹائم جڑتے ہیں۔",
                        fontSize = 11.sp,
                        color = PolishTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCloudGuideDialog = false
                        onTestCloudConnection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BasilGreen)
                ) {
                    Text("Test Connection Now ⚡", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCloudGuideDialog = false }) {
                    Text("Close", color = PolishTextMuted)
                }
            }
        )
    }
}

// ---------------- TAB 0: ORDERS MANAGEMENT ----------------
@Composable
private fun AdminOrdersList(
    orders: List<Order>,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onUpdateOrderStatus: (orderId: Long, nextStatus: OrderStatus) -> Unit,
    onAssignRiderClick: (Order) -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Cloud Sync & Realtime Status Header
        Surface(
            color = Color(0xFFF0FDF4),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cloud Firestore Live (${orders.size} Orders)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            fontSize = 12.sp
                        )
                    )
                }

                OutlinedButton(
                    onClick = onRefreshClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF15803D),
                        containerColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("admin_refresh_orders_btn")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color(0xFF15803D),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Orders",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRefreshing) "Syncing..." else "Refresh 🔄",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

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
                        text = "No Customer Orders Received Yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "When customers on Mobile B, C, or any phone place orders, they will instantly appear here in real-time with sound notifications!",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRefreshClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync All Orders Now 🔄", fontWeight = FontWeight.Bold)
                    }
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
                        text = "📞 ${order.customerPhone}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.SemiBold
                        )
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = { WhatsAppOrderHelper.makePhoneCall(context, order.customerPhone) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PolishPrimaryRed)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Delivery Location & Address (Kahan Deliver Karna Hai)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF8E1),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Delivery Address",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Delivery Location (ڈلیوری ایڈریس):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = order.deliveryAddress,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PolishTextDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (!order.areaLandmark.isNullOrBlank()) {
                            Text(
                                text = "Landmark: ${order.areaLandmark}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PolishTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        if (!order.orderNote.isNullOrBlank()) {
                            Text(
                                text = "Note: ${order.orderNote}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFC62828),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items summary (Kiya Order Ha)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PolishBgLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalPizza,
                            contentDescription = "Items Ordered",
                            tint = PolishPrimaryRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ordered Items (${order.itemsCount}x):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishMaroonDark
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.itemsSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextDark,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PREPARING_PIZZA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .testTag("admin_accept_order_btn_${order.orderId}")
                        ) {
                            Text("✅ Accept & Bake 🧑‍🍳", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PolishPrimaryRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishPrimaryRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("admin_reject_order_btn_${order.orderId}")
                        ) {
                            Text("❌ Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
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
    onAddNewItem: () -> Unit,
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
                    placeholder = { Text("Search deal, pizza, burger, drink to edit rate...") },
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
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Menu Items (${menuItems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = PolishTextDark)
                    )
                    TextButton(onClick = onResetConfirm) {
                        Text("Reset Defaults 🔄", color = PolishPrimaryRed, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onAddNewItem,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_menu_add_item_top_btn"),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Item / Cold Drink / Deal ➕", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

// ================= ADMIN ANALYTICS & INCOME DASHBOARD =================
data class IncomeHistoryRecord(
    val title: String,
    val orderCount: Int,
    val totalIncome: Int
)

@Composable
fun AdminAnalyticsDashboard(
    orders: List<Order>,
    menuItems: List<MenuItem>,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit
) {
    var selectedBreakdownTab by remember { mutableIntStateOf(0) } // 0: Daily, 1: Monthly, 2: Yearly, 3: Payment & Items

    // Calculate Dates and Calendars
    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = todayCal.timeInMillis

    val monthCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfMonth = monthCal.timeInMillis

    val yearCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfYear = yearCal.timeInMillis

    // Filter valid orders (Delivered or active in pipeline - excludes cancelled for income)
    val validIncomeOrders = orders.filter { it.status != OrderStatus.CANCELLED }

    val todayOrders = validIncomeOrders.filter { it.timestamp >= startOfToday }
    val todayIncome = todayOrders.sumOf { it.totalAmount }
    val todayCount = todayOrders.size

    val monthOrders = validIncomeOrders.filter { it.timestamp >= startOfMonth }
    val monthIncome = monthOrders.sumOf { it.totalAmount }
    val monthCount = monthOrders.size

    val yearOrders = validIncomeOrders.filter { it.timestamp >= startOfYear }
    val yearIncome = yearOrders.sumOf { it.totalAmount }
    val yearCount = yearOrders.size

    val totalLifetimeIncome = validIncomeOrders.sumOf { it.totalAmount }
    val totalLifetimeCount = validIncomeOrders.size
    val aov = if (totalLifetimeCount > 0) totalLifetimeIncome / totalLifetimeCount else 0

    // Payment Methods
    val codOrders = validIncomeOrders.filter { it.paymentMethod == PaymentMethod.CASH_ON_DELIVERY }
    val codIncome = codOrders.sumOf { it.totalAmount }

    val easypaisaOrders = validIncomeOrders.filter { it.paymentMethod == PaymentMethod.EASYPAISA }
    val easypaisaIncome = easypaisaOrders.sumOf { it.totalAmount }

    // Date Formatters
    val daySdf = SimpleDateFormat("dd MMM yyyy (EEE)", Locale.getDefault())
    val monthSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val yearSdf = SimpleDateFormat("yyyy", Locale.getDefault())

    // Grouping for Historical Lists
    val dailyBreakdown: List<IncomeHistoryRecord> = validIncomeOrders
        .groupBy { daySdf.format(Date(it.timestamp)) }
        .map { (dayStr, dayOrdersList) ->
            IncomeHistoryRecord(
                title = dayStr,
                orderCount = dayOrdersList.size,
                totalIncome = dayOrdersList.sumOf { it.totalAmount }
            )
        }

    val monthlyBreakdown: List<IncomeHistoryRecord> = validIncomeOrders
        .groupBy { monthSdf.format(Date(it.timestamp)) }
        .map { (monthStr, monthOrdersList) ->
            IncomeHistoryRecord(
                title = monthStr,
                orderCount = monthOrdersList.size,
                totalIncome = monthOrdersList.sumOf { it.totalAmount }
            )
        }

    val yearlyBreakdown: List<IncomeHistoryRecord> = validIncomeOrders
        .groupBy { yearSdf.format(Date(it.timestamp)) }
        .map { (yearStr, yearOrdersList) ->
            IncomeHistoryRecord(
                title = "Year $yearStr",
                orderCount = yearOrdersList.size,
                totalIncome = yearOrdersList.sumOf { it.totalAmount }
            )
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PolishBgLight),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Dashboard Header & Live Refresh
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Business Revenue Dashboard 📊",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishTextDark,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "آمدنی، ڈیلی، ماہانہ اور سالانہ آرڈرز کا مکمل ریکارڈ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextMuted,
                            fontSize = 11.5.sp
                        )
                    )
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier.background(Color.White, CircleShape)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PolishPrimaryRed, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PolishPrimaryRed)
                    }
                }
            }
        }

        // 2. Primary 4-Grid Income KPI Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Today Income Card
                    DashboardKpiCard(
                        title = "Today's Income 📅",
                        subtitle = "آج کی آمدنی",
                        amount = "Rs. $todayIncome",
                        orderCount = "$todayCount Orders",
                        bgGradient = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
                        modifier = Modifier.weight(1f)
                    )

                    // Monthly Income Card
                    DashboardKpiCard(
                        title = "This Month 📆",
                        subtitle = "ماہانہ آمدنی",
                        amount = "Rs. $monthIncome",
                        orderCount = "$monthCount Orders",
                        bgGradient = listOf(Color(0xFFE65100), Color(0xFFBF360C)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Yearly Income Card
                    DashboardKpiCard(
                        title = "This Year 🗓️",
                        subtitle = "سالانہ آمدنی",
                        amount = "Rs. $yearIncome",
                        orderCount = "$yearCount Orders",
                        bgGradient = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C)),
                        modifier = Modifier.weight(1f)
                    )

                    // Total Lifetime Income Card
                    DashboardKpiCard(
                        title = "Total Lifetime 💰",
                        subtitle = "کل ریکارڈ آمدنی",
                        amount = "Rs. $totalLifetimeIncome",
                        orderCount = "$totalLifetimeCount Total",
                        bgGradient = listOf(PolishPrimaryRed, PolishMaroonDark),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Quick Performance Highlights Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Avg Order Value",
                            style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted, fontSize = 11.sp)
                        )
                        Text(
                            text = "Rs. $aov",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = PolishTextDark)
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(PolishBorder))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Completed Rate",
                            style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted, fontSize = 11.sp)
                        )
                        val completedCount = orders.count { it.status == OrderStatus.DELIVERED }
                        val rate = if (orders.isNotEmpty()) (completedCount * 100) / orders.size else 100
                        Text(
                            text = "$rate%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(PolishBorder))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Menu Items Active",
                            style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted, fontSize = 11.sp)
                        )
                        Text(
                            text = "${menuItems.count { it.isAvailable }}/${menuItems.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = PolishTextDark)
                        )
                    }
                }
            }
        }

        // 4. Breakdown Filter Chips (Daily / Monthly / Yearly / Payments)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Income & Order History Breakdown:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PolishTextDark)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedBreakdownTab == 0,
                        onClick = { selectedBreakdownTab = 0 },
                        label = { Text("Daily (روزانہ)", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishPrimaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedBreakdownTab == 1,
                        onClick = { selectedBreakdownTab = 1 },
                        label = { Text("Monthly (ماہانہ)", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishPrimaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedBreakdownTab == 2,
                        onClick = { selectedBreakdownTab = 2 },
                        label = { Text("Yearly (سالانہ)", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishPrimaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedBreakdownTab == 3,
                        onClick = { selectedBreakdownTab = 3 },
                        label = { Text("Payments", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishPrimaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 5. Selected Breakdown Content
        when (selectedBreakdownTab) {
            0 -> {
                // DAILY BREAKDOWN
                if (dailyBreakdown.isEmpty()) {
                    item {
                        EmptyDashboardRecordsCard("No daily orders recorded yet.")
                    }
                } else {
                    items(dailyBreakdown) { record ->
                        HistoryRecordRow(
                            title = record.title,
                            badge = "${record.orderCount} Orders",
                            income = "Rs. ${record.totalIncome}",
                            icon = Icons.Default.CalendarToday,
                            accentColor = Color(0xFF2E7D32)
                        )
                    }
                }
            }
            1 -> {
                // MONTHLY BREAKDOWN
                if (monthlyBreakdown.isEmpty()) {
                    item {
                        EmptyDashboardRecordsCard("No monthly order records yet.")
                    }
                } else {
                    items(monthlyBreakdown) { record ->
                        HistoryRecordRow(
                            title = record.title,
                            badge = "${record.orderCount} Orders",
                            income = "Rs. ${record.totalIncome}",
                            icon = Icons.Default.Assessment,
                            accentColor = Color(0xFFE65100)
                        )
                    }
                }
            }
            2 -> {
                // YEARLY BREAKDOWN
                if (yearlyBreakdown.isEmpty()) {
                    item {
                        EmptyDashboardRecordsCard("No yearly records yet.")
                    }
                } else {
                    items(yearlyBreakdown) { record ->
                        HistoryRecordRow(
                            title = record.title,
                            badge = "${record.orderCount} Total Orders",
                            income = "Rs. ${record.totalIncome}",
                            icon = Icons.Default.TrendingUp,
                            accentColor = Color(0xFF6A1B9A)
                        )
                    }
                }
            }
            3 -> {
                // PAYMENT METHOD BREAKDOWN
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Payment Methods Breakdown 💳",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PolishTextDark)
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                val codFraction = if (totalLifetimeIncome > 0) codIncome.toFloat() / totalLifetimeIncome.toFloat() else 0f
                                PaymentProgressRow(
                                    label = "Cash on Delivery (کیش آن ڈیلیوری)",
                                    amount = "Rs. $codIncome",
                                    count = "${codOrders.size} orders",
                                    fraction = codFraction,
                                    barColor = Color(0xFF2E7D32)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val epFraction = if (totalLifetimeIncome > 0) easypaisaIncome.toFloat() / totalLifetimeIncome.toFloat() else 0f
                                PaymentProgressRow(
                                    label = "Easypaisa / JazzCash",
                                    amount = "Rs. $easypaisaIncome",
                                    count = "${easypaisaOrders.size} orders",
                                    fraction = epFraction,
                                    barColor = Color(0xFF00897B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardKpiCard(
    title: String,
    subtitle: String,
    amount: String,
    orderCount: String,
    bgGradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgGradient.first())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(bgGradient),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = orderCount,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordRow(
    title: String,
    badge: String,
    income: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark,
                            fontSize = 13.5.sp
                        )
                    )
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Text(
                text = income,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Composable
private fun PaymentProgressRow(
    label: String,
    amount: String,
    count: String,
    fraction: Float,
    barColor: Color
) {
    val cleanFraction = if (fraction.isNaN() || fraction < 0f) 0f else if (fraction > 1f) 1f else fraction
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PolishTextDark)
            )
            Text(
                text = "$amount ($count)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = barColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { cleanFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = PolishBgLight
        )
    }
}

@Composable
private fun EmptyDashboardRecordsCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📊 $message",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PolishTextMuted,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
