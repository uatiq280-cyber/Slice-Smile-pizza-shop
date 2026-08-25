package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.OrderStatus
import com.example.service.NotificationHelper
import com.example.ui.components.AdminChangePinDialog
import com.example.ui.components.AdminEditItemDialog
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.AssignRiderDialog
import com.example.ui.components.CustomerAuthDialog
import com.example.ui.components.EasypaisaPaymentDialog
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.ItemCustomizationDialog
import com.example.ui.components.LocationSelectorSheet
import com.example.ui.components.PizzaTopBar
import com.example.ui.components.RiderLoginDialog
import com.example.ui.components.RiderManagementDialog
import com.example.ui.components.RoleSelectionWelcomeDialog
import com.example.ui.navigation.Screen
import com.example.ui.navigation.navigationScreens
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.LoyaltyScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.OrdersTrackScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RiderPortalScreen
import com.example.ui.screens.ShopInfoReviewsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextMuted
import com.example.viewmodel.PizzaShopViewModel
import com.example.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHelper.initNotificationChannels(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 0. Ensure FirebaseApp is initialized early
        try {
            com.example.service.FirebaseInitHelper.getOrInitFirebaseApp(this)
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Firebase early init: ${e.message}")
        }

        // Initialize Notification Channels
        NotificationHelper.initNotificationChannels(this)

        // Request POST_NOTIFICATIONS on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                SliceSmilePizzaApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun SliceSmilePizzaApp(viewModel: PizzaShopViewModel = viewModel()) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Menu.route

    // ViewModel State Collection
    val menuItems by viewModel.filteredMenuItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }
    val cartSubtotal by viewModel.cartSubtotal.collectAsState()
    val potentialCoinsEarned by viewModel.potentialCoinsEarned.collectAsState()
    val redeemableCoinsDiscount by viewModel.redeemableCoinsDiscount.collectAsState()
    val coinsToRedeemCount by viewModel.coinsToRedeemCount.collectAsState()
    val deliveryFee by viewModel.deliveryFee.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val applyCoinsDiscount by viewModel.applyCoinsDiscount.collectAsState()

    val userSession by viewModel.userSession.collectAsState()
    val isShowingAuthDialog by viewModel.isShowingAuthDialog.collectAsState()
    val ownerId by viewModel.ownerId.collectAsState()

    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val deliveryAddress by viewModel.deliveryAddress.collectAsState()
    val areaLandmark by viewModel.areaLandmark.collectAsState()
    val orderNote by viewModel.orderNote.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val easypaisaTrxId by viewModel.easypaisaTrxId.collectAsState()

    val ordersList by viewModel.ordersList.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val isRefreshingOrders by viewModel.isRefreshingOrders.collectAsState()
    val customerOrders by viewModel.customerOrders.collectAsState()
    val activeOrdersCount = customerOrders.count { it.status != OrderStatus.DELIVERED }

    val loyaltyProfile by viewModel.loyaltyProfile.collectAsState()
    val customerReviews by viewModel.customerReviews.collectAsState()

    // Role Selector & Admin & Menu Management States
    val isShowingRoleSelector by viewModel.isShowingRoleSelector.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isShowingAdminLogin by viewModel.isShowingAdminLogin.collectAsState()
    val isShowingChangePinDialog by viewModel.isShowingChangePinDialog.collectAsState()
    val isShowingEditItemDialog by viewModel.isShowingEditItemDialog.collectAsState()
    val editingItem by viewModel.editingItem.collectAsState()
    val allMenuItems by viewModel.allMenuItems.collectAsState()

    // Rider Fleet & Portal States
    val allRiders by viewModel.allRiders.collectAsState()
    val currentRider by viewModel.currentRider.collectAsState()
    val isRiderLoggedIn by viewModel.isRiderLoggedIn.collectAsState()
    val riderOrders by viewModel.riderOrders.collectAsState()
    val isShowingRiderLogin by viewModel.isShowingRiderLogin.collectAsState()
    val isShowingRiderDialog by viewModel.isShowingRiderDialog.collectAsState()
    val editingRider by viewModel.editingRider.collectAsState()
    val isShowingAssignRiderModal by viewModel.isShowingAssignRiderModal.collectAsState()
    val selectedOrderForRiderAssign by viewModel.selectedOrderForRiderAssign.collectAsState()

    // Modals
    val customizingItem by viewModel.customizingItem.collectAsState()
    val isShowingEasypaisaModal by viewModel.isShowingEasypaisaModal.collectAsState()
    val isLocationSelectorVisible by viewModel.isLocationSelectorVisible.collectAsState()
    val selectedOrderForFeedback by viewModel.selectedOrderForFeedback.collectAsState()

    // Handle Deep-Link / Notification Click
    LaunchedEffect(Unit) {
        val openScreen = (context as? ComponentActivity)?.intent?.getStringExtra("OPEN_SCREEN")
        if (openScreen == "admin") {
            viewModel.refreshOrdersFromCloud()
            if (isAdminLoggedIn) {
                navController.navigate(Screen.Admin.route)
            } else {
                viewModel.showAdminLoginDialog(true)
            }
        }
    }

    // Event Flow Toast Listener
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.OrderPlacedSuccess -> {
                    Toast.makeText(
                        context,
                        "Order #${event.order.orderId} Placed Successfully! 🍕",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (currentRoute != Screen.Admin.route && currentRoute != Screen.RiderPortal.route) {
                PizzaTopBar(
                    cartCount = cartCount,
                    coinsCount = loyaltyProfile.currentCoins,
                    onCartClick = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onLocationClick = { viewModel.showLocationSelector(true) },
                    onAdminClick = {
                        if (isAdminLoggedIn) {
                            navController.navigate(Screen.Admin.route)
                        } else {
                            viewModel.showAdminLoginDialog(true)
                        }
                    },
                    currentAddress = deliveryAddress
                )
            }
        },
        bottomBar = {
            if (currentRoute != Screen.RiderPortal.route && currentRoute != Screen.Admin.route) {
                NavigationBar(
                    containerColor = PolishBgLight,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .testTag("bottom_navigation_bar")
                        .drawBehind {
                            drawLine(
                                color = PolishBorder,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                ) {
                    navigationScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (screen == Screen.Cart && cartCount > 0) {
                                            Badge(containerColor = PolishPrimaryRed, contentColor = Color.White) {
                                                Text(text = cartCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (screen == Screen.Orders && activeOrdersCount > 0) {
                                            Badge(containerColor = PolishPrimaryRed, contentColor = Color.White) {
                                                Text(text = activeOrdersCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PolishMaroonDark,
                                selectedTextColor = PolishMaroonDark,
                                indicatorColor = PolishPrimaryContainer,
                                unselectedIconColor = PolishTextMuted,
                                unselectedTextColor = PolishTextMuted
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Menu.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Menu.route) {
                    MenuScreen(
                        menuItems = menuItems,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        cartCount = cartCount,
                        cartSubtotal = cartSubtotal,
                        onCategorySelected = viewModel::setCategory,
                        onSearchChanged = viewModel::setSearchQuery,
                        onQuickAdd = viewModel::quickAddToCart,
                        onCustomizeClick = viewModel::openItemCustomization,
                        onNavigateToCart = {
                            navController.navigate(Screen.Cart.route)
                        },
                        onNavigateToLoyalty = {
                            navController.navigate(Screen.Loyalty.route)
                        }
                    )
                }

                composable(Screen.Cart.route) {
                    CartScreen(
                        cartItems = cartItems,
                        loyaltyProfile = loyaltyProfile,
                        applyCoinsDiscount = applyCoinsDiscount,
                        cartSubtotal = cartSubtotal,
                        potentialCoinsEarned = potentialCoinsEarned,
                        redeemableCoinsDiscount = redeemableCoinsDiscount,
                        coinsToRedeemCount = coinsToRedeemCount,
                        deliveryFee = deliveryFee,
                        grandTotal = grandTotal,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        deliveryAddress = deliveryAddress,
                        areaLandmark = areaLandmark,
                        orderNote = orderNote,
                        selectedPaymentMethod = selectedPaymentMethod,
                        easypaisaTrxId = easypaisaTrxId,
                        onQuantityDelta = viewModel::updateCartItemQuantity,
                        onRemoveItem = viewModel::removeCartItem,
                        onToggleCoinsDiscount = viewModel::toggleCoinsDiscount,
                        onCustomerNameChanged = viewModel::setCustomerName,
                        onCustomerPhoneChanged = viewModel::setCustomerPhone,
                        onOrderNoteChanged = viewModel::setOrderNote,
                        onPaymentMethodChanged = viewModel::setPaymentMethod,
                        onOpenEasypaisaModal = { viewModel.showEasypaisaModal(true) },
                        onOpenLocationModal = { viewModel.showLocationSelector(true) },
                        onPlaceOrder = {
                            viewModel.placeOrder { _ -> }
                        },
                        onNavigateToMenu = {
                            navController.navigate(Screen.Menu.route)
                        }
                    )
                }

                composable(Screen.Orders.route) {
                    OrdersTrackScreen(
                        orders = customerOrders,
                        onOpenFeedback = viewModel::setFeedbackOrder,
                        onNavigateToMenu = {
                            navController.navigate(Screen.Menu.route)
                        }
                    )
                }

                composable(Screen.Loyalty.route) {
                    LoyaltyScreen(
                        loyaltyProfile = loyaltyProfile,
                        onNavigateToMenu = {
                            navController.navigate(Screen.Menu.route)
                        }
                    )
                }

                composable(Screen.ShopInfo.route) {
                    ShopInfoReviewsScreen(
                        reviews = customerReviews,
                        onOpenAdminPortal = {
                            if (isAdminLoggedIn) {
                                navController.navigate(Screen.Admin.route)
                            } else {
                                viewModel.showAdminLoginDialog(true)
                            }
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        userSession = userSession,
                        loyaltyProfile = loyaltyProfile,
                        reviews = customerReviews,
                        isAdminLoggedIn = isAdminLoggedIn,
                        isRiderLoggedIn = isRiderLoggedIn,
                        onOpenAuthDialog = { viewModel.showAuthDialog(true) },
                        onLogoutCustomer = { viewModel.logoutCustomer() },
                        onChangeAddressClick = { viewModel.showLocationSelector(true) },
                        onOpenAdminPortal = {
                            if (isAdminLoggedIn) {
                                navController.navigate(Screen.Admin.route)
                            } else {
                                viewModel.showAdminLoginDialog(true)
                            }
                        },
                        onOpenRiderPortal = {
                            if (isRiderLoggedIn) {
                                navController.navigate(Screen.RiderPortal.route)
                            } else {
                                viewModel.showRiderLoginDialog(true)
                            }
                        },
                        onNavigateToOrders = {
                            navController.navigate(Screen.Orders.route)
                        },
                        onNavigateToLoyalty = {
                            navController.navigate(Screen.Loyalty.route)
                        }
                    )
                }

                composable(Screen.Admin.route) {
                    if (isAdminLoggedIn) {
                        AdminPanelScreen(
                            menuItems = allMenuItems,
                            orders = ordersList,
                            riders = allRiders,
                            cloudSyncStatus = cloudSyncStatus,
                            isRefreshingOrders = isRefreshingOrders,
                            onRefreshOrders = { viewModel.refreshOrdersFromCloud() },
                            onTestCloudConnection = { viewModel.testCloudConnection() },
                            onAddNewItem = { viewModel.openAdminEditItem(null) },
                            onEditItem = { item -> viewModel.openAdminEditItem(item) },
                            onDeleteItem = { id -> viewModel.deleteMenuItem(id) },
                            onToggleStock = { item, inStock -> viewModel.toggleItemStock(item, inStock) },
                            onResetDefaults = { viewModel.resetMenuToDefaults() },
                            onChangePinClick = { viewModel.showChangePinDialog(true) },
                            onLogoutClick = {
                                viewModel.logoutAdmin()
                                navController.popBackStack(Screen.Menu.route, false)
                            },
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onUpdateOrderStatus = { orderId, nextStatus ->
                                viewModel.updateManualOrderStatus(orderId, nextStatus)
                            },
                            onAssignRiderClick = { order ->
                                viewModel.openAssignRiderModal(order)
                            },
                            onAddRiderClick = {
                                viewModel.openRiderDialog(null)
                            },
                            onEditRiderClick = { rider ->
                                viewModel.openRiderDialog(rider)
                            },
                            onDeleteRiderClick = { riderId ->
                                viewModel.deleteRider(riderId)
                            },
                            onToggleRiderEnabled = { rider, enabled ->
                                viewModel.toggleRiderEnabled(rider, enabled)
                            },
                            onTestNotificationSound = {
                                viewModel.testOwnerNotificationSound()
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            viewModel.showAdminLoginDialog(true)
                            navController.popBackStack(Screen.Menu.route, false)
                        }
                    }
                }

                composable(Screen.RiderPortal.route) {
                    if (isRiderLoggedIn && currentRider != null) {
                        RiderPortalScreen(
                            rider = currentRider!!,
                            assignedOrders = riderOrders,
                            onMarkOutForDelivery = { orderId ->
                                viewModel.riderMarkOutForDelivery(orderId)
                            },
                            onMarkDelivered = { orderId ->
                                viewModel.riderMarkDelivered(orderId)
                            },
                            onLogout = {
                                viewModel.logoutRider()
                                navController.popBackStack(Screen.Profile.route, false)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            viewModel.showRiderLoginDialog(true)
                            navController.popBackStack(Screen.Profile.route, false)
                        }
                    }
                }
            }

            // App Startup Role Selection Dialog (Customer vs Admin Mode)
            if (isShowingRoleSelector) {
                RoleSelectionWelcomeDialog(
                    onSelectCustomer = {
                        viewModel.showRoleSelector(false)
                    },
                    onSelectAdmin = {
                        viewModel.showRoleSelector(false)
                        viewModel.showAdminLoginDialog(true)
                    }
                )
            }

            // Customer Authentication Modal (Guest, Mobile OTP, Gmail)
            if (isShowingAuthDialog) {
                CustomerAuthDialog(
                    currentName = customerName,
                    currentPhone = customerPhone,
                    currentAddress = deliveryAddress,
                    onDismiss = { viewModel.showAuthDialog(false) },
                    onContinueAsGuest = { name ->
                        viewModel.loginAsGuest(name)
                    },
                    onRequestOtp = { phone ->
                        viewModel.requestPhoneOtp(phone)
                    },
                    onVerifyOtpAndLogin = { phone, otp, name ->
                        viewModel.verifyAndLoginWithPhone(phone, otp, name)
                    },
                    onLoginWithGoogle = { email, name ->
                        viewModel.loginWithGoogle(email, name)
                    }
                )
            }

            // Admin / Owner Dialogs
            if (isShowingAdminLogin) {
                AdminLoginDialog(
                    onDismiss = { viewModel.showAdminLoginDialog(false) },
                    onLoginSubmit = { enteredOwnerId, enteredPin ->
                        val isValid = viewModel.verifyAndLoginAdmin(enteredOwnerId, enteredPin)
                        if (isValid) {
                            navController.navigate(Screen.Admin.route)
                        }
                        isValid
                    }
                )
            }

            if (isShowingChangePinDialog) {
                AdminChangePinDialog(
                    currentOwnerIdValue = ownerId,
                    onDismiss = { viewModel.showChangePinDialog(false) },
                    onSubmitChange = { currentPin, newOwnerId, newPin ->
                        viewModel.changeOwnerCredentials(currentPin, newOwnerId, newPin)
                    }
                )
            }

            if (isShowingEditItemDialog) {
                AdminEditItemDialog(
                    itemToEdit = editingItem,
                    onDismiss = { viewModel.closeAdminEditItem() },
                    onSave = { item ->
                        viewModel.saveMenuItem(item)
                    }
                )
            }

            // Rider Dialogs
            if (isShowingRiderLogin) {
                RiderLoginDialog(
                    availableRiders = allRiders,
                    onDismiss = { viewModel.showRiderLoginDialog(false) },
                    onLogin = { phone, pin ->
                        val success = viewModel.verifyAndLoginRider(phone, pin)
                        if (success) {
                            navController.navigate(Screen.RiderPortal.route)
                        }
                    }
                )
            }

            if (isShowingRiderDialog) {
                RiderManagementDialog(
                    rider = editingRider,
                    onDismiss = { viewModel.closeRiderDialog() },
                    onSave = { rider ->
                        viewModel.saveRider(rider)
                    }
                )
            }

            if (isShowingAssignRiderModal && selectedOrderForRiderAssign != null) {
                AssignRiderDialog(
                    order = selectedOrderForRiderAssign!!,
                    availableRiders = allRiders,
                    onDismiss = { viewModel.closeAssignRiderModal() },
                    onAssign = { orderId, rider ->
                        viewModel.assignRiderToOrder(orderId, rider)
                    }
                )
            }

            // Customization Dialog
            customizingItem?.let { item ->
                ItemCustomizationDialog(
                    item = item,
                    onDismiss = viewModel::closeItemCustomization,
                    onConfirmAddToCart = { menuItem, size, crust, toppings, unitPrice, qty, extraCheese, spice, drink, notes ->
                        viewModel.addToCart(
                            menuItem = menuItem,
                            size = size,
                            crust = crust,
                            toppings = toppings,
                            unitPrice = unitPrice,
                            quantity = qty,
                            extraCheese = extraCheese,
                            spiceLevel = spice,
                            drinkChoice = drink,
                            specialInstructions = notes
                        )
                    }
                )
            }

            // Easypaisa Dialog
            if (isShowingEasypaisaModal) {
                EasypaisaPaymentDialog(
                    totalPayable = grandTotal,
                    currentTrxId = easypaisaTrxId,
                    onTrxIdChanged = viewModel::setEasypaisaTrxId,
                    onDismiss = { viewModel.showEasypaisaModal(false) },
                    onConfirmOrder = {
                        viewModel.showEasypaisaModal(false)
                        viewModel.placeOrder { _ -> }
                    }
                )
            }

            // Location Selector Sheet
            if (isLocationSelectorVisible) {
                LocationSelectorSheet(
                    currentAddress = deliveryAddress,
                    currentLandmark = areaLandmark,
                    onSaveLocation = { address, landmark ->
                        viewModel.setDeliveryAddress(address)
                        viewModel.setAreaLandmark(landmark)
                    },
                    onDismiss = { viewModel.showLocationSelector(false) }
                )
            }

            // Post-Delivery Feedback Dialog
            selectedOrderForFeedback?.let { order ->
                FeedbackDialog(
                    order = order,
                    onDismiss = { viewModel.setFeedbackOrder(null) },
                    onSubmit = { overall, taste, delivery, comment ->
                        viewModel.submitFeedback(
                            orderId = order.orderId,
                            overallRating = overall,
                            foodTaste = taste,
                            deliverySpeed = delivery,
                            comment = comment
                        )
                    }
                )
            }
        }
    }
}
