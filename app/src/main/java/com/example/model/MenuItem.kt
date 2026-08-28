package com.example.model

enum class MenuCategory(val displayName: String, val iconResName: String) {
    ALL("All Items", "local_pizza"),
    DEALS("Deals", "local_offer"),
    FAMILY_DEALS("Family Deals", "groups"),
    BIRTHDAY_DEALS("Birthday Deals", "cake"),
    PIZZA("Pizza", "local_pizza"),
    SPECIAL_PIZZA("Special Pizza", "star"),
    BURGER("Burgers", "lunch_dining"),
    SHAWARMA("Shawarma", "kebab_dining"),
    BROAST("Broast & Leg Piece", "restaurant"),
    WINGS_FRIES("Wings & Fries", "fastfood"),
    PASTA("Pasta", "dinner_dining"),
    CHINESE("Chinese", "ramen_dining"),
    JUICES("Fresh Juices 🧃", "local_bar"),
    MILKSHAKES("Milkshakes 🥤", "blender"),
    BEVERAGES("Beverages 🍹", "local_drink"),
    COLD_DRINKS("Cold Drinks", "local_drink"),
    JUICES_SHAKES("Juices & Shakes", "blender"),
    WRAP("Wraps & Rolls", "takeout_dining"),
    SWEETS("Sweets & Mithai 🍬", "cake"),
    DESSERTS("Desserts & Ice Cream 🍨", "icecream"),
    PAKISTANI_FOOD("Pakistani Food 🍛", "restaurant"),
    FAST_FOOD("Fast Food 🍔", "fastfood"),
    BBQ("BBQ & Grills 🔥", "outdoor_grill"),
    CUSTOM("Custom Category ✨", "category")
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
    val category: MenuCategory = MenuCategory.ALL,
    val customCategoryName: String? = null,
    val description: String,
    val basePrice: Int,
    val sizeOptions: List<SizeOption> = emptyList(),
    val dealIncludes: List<String> = emptyList(),
    val isSpicy: Boolean = false,
    val isPopular: Boolean = false,
    val tag: String? = null,
    val discountPercent: Int = 0,
    val originalPrice: Int? = null,
    val imageDrawableRes: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true
) {
    val effectiveCategoryName: String
        get() = customCategoryName?.takeIf { it.isNotBlank() } ?: category.displayName

    val defaultPrice: Int
        get() = if (sizeOptions.isNotEmpty()) sizeOptions.first().price else basePrice

    val hasDiscount: Boolean
        get() = discountPercent > 0 || (originalPrice != null && originalPrice > defaultPrice)

    val effectiveOriginalPrice: Int
        get() = originalPrice?.takeIf { it > defaultPrice }
            ?: if (discountPercent in 1..95) {
                ((defaultPrice.toDouble() / (100 - discountPercent)) * 100).toInt()
            } else defaultPrice

    val effectiveDiscountPercent: Int
        get() = if (discountPercent > 0) {
            discountPercent
        } else if (originalPrice != null && originalPrice > defaultPrice && originalPrice > 0) {
            (((originalPrice - defaultPrice).toDouble() / originalPrice) * 100).toInt()
        } else {
            0
        }
}
