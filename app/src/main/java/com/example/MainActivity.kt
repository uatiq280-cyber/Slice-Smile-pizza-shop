package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.EasypaisaPaymentDialog
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.ItemCustomizationDialog
import com.example.ui.components.LocationSelectorSheet
import com.example.ui.components.PizzaTopBar
import com.example.ui.navigation.Screen
import com.example.ui.navigation.navigationScreens
import com.example.ui.screens.CartScreen
import com.example.ui.screens.LoyaltyScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.OrdersTrackScreen
import com.example.ui.screens.ShopInfoReviewsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PizzaRed
import com.example.viewmodel.PizzaShopViewModel
import com.example.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SliceSmilePizzaApp()
            }
        }
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

    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val deliveryAddress by viewModel.deliveryAddress.collectAsState()
    val areaLandmark by viewModel.areaLandmark.collectAsState()
    val orderNote by viewModel.orderNote.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val easypaisaTrxId by viewModel.easypaisaTrxId.collectAsState()

    val ordersList by viewModel.ordersList.collectAsState()
    val activeOrdersCount = ordersList.count { it.status != OrderStatus.DELIVERED }

    val loyaltyProfile by viewModel.loyaltyProfile.collectAsState()
    val customerReviews by viewModel.customerReviews.collectAsState()

    // Modals
    val customizingItem by viewModel.customizingItem.collectAsState()
    val isShowingEasypaisaModal by viewModel.isShowingEasypaisaModal.collectAsState()
    val isLocationSelectorVisible by viewModel.isLocationSelectorVisible.collectAsState()
    val selectedOrderForFeedback by viewModel.selectedOrderForFeedback.collectAsState()

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
                currentAddress = deliveryAddress
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = com.example.ui.theme.PolishBgLight,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_navigation_bar")
                    .drawBehind {
                        drawLine(
                            color = com.example.ui.theme.PolishBorder,
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
                                        Badge(containerColor = com.example.ui.theme.PolishPrimaryRed, contentColor = Color.White) {
                                            Text(text = cartCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (screen == Screen.Orders && activeOrdersCount > 0) {
                                        Badge(containerColor = com.example.ui.theme.PolishPrimaryRed, contentColor = Color.White) {
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
                            selectedIconColor = com.example.ui.theme.PolishMaroonDark,
                            selectedTextColor = com.example.ui.theme.PolishMaroonDark,
                            indicatorColor = com.example.ui.theme.PolishPrimaryContainer,
                            unselectedIconColor = com.example.ui.theme.PolishTextMuted,
                            unselectedTextColor = com.example.ui.theme.PolishTextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
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
                        orders = ordersList,
                        onOpenFeedback = viewModel::setFeedbackOrder,
                        onStatusAdvance = viewModel::updateManualOrderStatus,
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
                        reviews = customerReviews
                    )
                }
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
