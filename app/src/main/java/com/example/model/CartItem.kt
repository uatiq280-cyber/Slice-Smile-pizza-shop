package com.example.model

data class CartItem(
    val cartItemId: String,
    val menuItem: MenuItem,
    val selectedSize: PortionSize? = null,
    val selectedCrust: CrustType? = null,
    val selectedToppings: List<PizzaTopping> = emptyList(),
    val unitPrice: Int,
    val quantity: Int = 1,
    val extraCheese: Boolean = false,
    val spiceLevel: String = "Normal",
    val drinkChoice: String = "Regular Coke",
    val specialInstructions: String = "",
    val dealCustomizationSummary: String = ""
) {
    val crustPrice: Int get() = selectedCrust?.priceModifier ?: 0
    val toppingsPrice: Int get() = selectedToppings.sumOf { it.price }
    val extraCheesePrice: Int get() = if (extraCheese) 120 else 0
    val singleItemPrice: Int get() = unitPrice + crustPrice + toppingsPrice + extraCheesePrice
    val totalItemPrice: Int get() = singleItemPrice * quantity

    val customizationDescription: String get() {
        if (dealCustomizationSummary.isNotBlank()) {
            return dealCustomizationSummary
        }
        val parts = mutableListOf<String>()
        selectedSize?.let { parts.add(it.label) }
        selectedCrust?.let { parts.add(it.displayName) }
        if (selectedToppings.isNotEmpty()) {
            parts.add("Toppings: " + selectedToppings.joinToString(", ") { it.name })
        }
        if (extraCheese) {
            parts.add("+ Extra Mozzarella")
        }
        if (spiceLevel != "Normal") {
            parts.add("Spice: $spiceLevel")
        }
        return parts.joinToString(" • ")
    }
}

