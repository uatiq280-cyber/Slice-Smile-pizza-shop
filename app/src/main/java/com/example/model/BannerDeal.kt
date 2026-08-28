package com.example.model

data class BannerDeal(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String = "🔥 HOT DEAL",
    val discountText: String = "SAVE 20%",
    val targetCategory: MenuCategory = MenuCategory.DEALS,
    val gradientStartHex: Long = 0xFF8B1E1E,
    val gradientEndHex: Long = 0xFFE53935,
    val iconEmoji: String = "🍕",
    val isVideo: Boolean = false,
    val mediaUrl: String? = null,
    val actionText: String = "Order Now ➔"
)
