package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.Moped
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Menu : Screen("menu", "Menu & Deals", Icons.Filled.LocalPizza, Icons.Outlined.LocalPizza)
    data object Cart : Screen("cart", "My Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    data object Orders : Screen("orders", "Track Order", Icons.Filled.Moped, Icons.Outlined.Moped)
    data object Loyalty : Screen("loyalty", "Smile Coins", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard)
    data object ShopInfo : Screen("shop_info", "Shop & Reviews", Icons.Filled.Info, Icons.Outlined.Info)
    data object Admin : Screen("admin", "Owner Portal", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

val navigationScreens = listOf(
    Screen.Menu,
    Screen.Cart,
    Screen.Orders,
    Screen.Loyalty,
    Screen.ShopInfo
)
