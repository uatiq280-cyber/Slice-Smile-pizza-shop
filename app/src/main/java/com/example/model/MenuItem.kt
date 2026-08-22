package com.example.model

enum class MenuCategory(val displayName: String, val iconResName: String) {
    ALL("All Items", "local_pizza"),
    DEALS("Deals", "local_offer"),
    FAMILY_DEALS("Family Deals", "groups"),
    BIRTHDAY_DEALS("Birthday Deals", "cake"),
    PIZZA("Pizza", "local_pizza"),
    SPECIAL_PIZZA("Special Pizza", "star"),
    BURGER("Burger", "lunch_dining"),
    SHAWARMA("Shawarma", "kebab_dining"),
    BROAST("Broast", "restaurant"),
    WINGS_FRIES("Wings & Fries", "fastfood"),
    PASTA("Pasta", "dinner_dining"),
    WRAP("Wrap", "takeout_dining")
}

enum class PortionSize(val label: String) {
    REGULAR("Regular"),
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large"),
    EXTRA_LARGE("Extra Large")
}

data class SizeOption(
    val size: PortionSize,
    val price: Int
)

data class MenuItem(
    val id: String,
    val name: String,
    val category: MenuCategory,
    val description: String,
    val basePrice: Int,
    val sizeOptions: List<SizeOption> = emptyList(),
    val dealIncludes: List<String> = emptyList(),
    val isSpicy: Boolean = false,
    val isPopular: Boolean = false,
    val tag: String? = null,
    val imageDrawableRes: String? = null,
    val isAvailable: Boolean = true
) {
    val defaultPrice: Int
        get() = if (sizeOptions.isNotEmpty()) sizeOptions.first().price else basePrice
}
