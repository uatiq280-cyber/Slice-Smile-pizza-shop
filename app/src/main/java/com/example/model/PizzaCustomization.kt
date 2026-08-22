package com.example.model

enum class CrustType(
    val displayName: String,
    val description: String,
    val priceModifier: Int,
    val emoji: String
) {
    THIN_CRUST("Thin Crust", "Crispy, light & flaky artisan base", 0, "🍕"),
    PAN_THICK("Pan / Thick Crust", "Fluffy, golden pan baked crust", 0, "🍞"),
    CHEESE_STUFFED("Cheese Stuffed Crust", "Gooey mozzarella stuffed in rim", 150, "🧀"),
    KABAB_STUFFER("Kabab Stuffer Crust", "Spiced chicken seekh kabab in crust", 200, "🍢"),
    CROWN_CRUST("Royal Crown Crust", "Crown petals with cheese & dip pockets", 220, "👑")
}

enum class ToppingCategory(val title: String, val iconEmoji: String) {
    VEGETABLES("Vegetables", "🫑"),
    MEATS("Meats & Chicken", "🍗"),
    CHEESES_SAUCES("Cheeses & Sauces", "🧀")
}

data class PizzaTopping(
    val id: String,
    val name: String,
    val category: ToppingCategory,
    val price: Int,
    val emoji: String,
    val description: String = ""
)

object PizzaCustomizationDataSource {
    val availableCrusts: List<CrustType> = CrustType.entries

    val availableToppings: List<PizzaTopping> = listOf(
        // Vegetables
        PizzaTopping("top_mushrooms", "Fresh Mushrooms", ToppingCategory.VEGETABLES, 60, "🍄", "Tender sliced button mushrooms"),
        PizzaTopping("top_olives", "Black Olives", ToppingCategory.VEGETABLES, 50, "🫒", "Imported sliced Spanish black olives"),
        PizzaTopping("top_bell_peppers", "Bell Peppers", ToppingCategory.VEGETABLES, 40, "🫑", "Crisp diced green capsicum"),
        PizzaTopping("top_onions", "Red Onions", ToppingCategory.VEGETABLES, 30, "🧅", "Fresh crunchy red onion rings"),
        PizzaTopping("top_jalapenos", "Spicy Jalapeños", ToppingCategory.VEGETABLES, 50, "🌶️", "Fiery pickled jalapeno slices"),
        PizzaTopping("top_corn", "Sweet Golden Corn", ToppingCategory.VEGETABLES, 50, "🌽", "Juicy golden sweet corn niblets"),
        PizzaTopping("top_tomatoes", "Diced Tomatoes", ToppingCategory.VEGETABLES, 30, "🍅", "Ripe juicy Italian style tomatoes"),

        // Meats
        PizzaTopping("top_fajita", "Spicy Chicken Fajita", ToppingCategory.MEATS, 120, "🍗", "Tender marinated fajita chicken chunks"),
        PizzaTopping("top_tikka", "Smoked Chicken Tikka", ToppingCategory.MEATS, 120, "🔥", "Aromatic charcoal smoked desi tikka"),
        PizzaTopping("top_pepperoni", "Beef Pepperoni", ToppingCategory.MEATS, 140, "🥓", "Classic cured beef pepperoni slices"),
        PizzaTopping("top_bihari", "Bihari Minced Meat", ToppingCategory.MEATS, 130, "🥩", "Desi aromatic spicy minced bihari boti"),
        PizzaTopping("top_sausages", "Smoked Sausages", ToppingCategory.MEATS, 110, "🌭", "Premium seasoned chicken sausages"),

        // Cheeses & Sauces
        PizzaTopping("top_extra_mozzarella", "Extra Mozzarella Layer", ToppingCategory.CHEESES_SAUCES, 120, "🧀", "Double stringy melted mozzarella"),
        PizzaTopping("top_cheddar_blend", "Cheddar Cheese Blend", ToppingCategory.CHEESES_SAUCES, 90, "🧀", "Sharp cheddar & mozzarella blend"),
        PizzaTopping("top_garlic_ranch", "Garlic Ranch Drizzle", ToppingCategory.CHEESES_SAUCES, 60, "🧄", "Creamy herb garlic ranch swirl"),
        PizzaTopping("top_chipotle", "Spicy Chipotle Swirl", ToppingCategory.CHEESES_SAUCES, 60, "🌶️", "Smoky fiery chipotle sauce finish")
    )
}
