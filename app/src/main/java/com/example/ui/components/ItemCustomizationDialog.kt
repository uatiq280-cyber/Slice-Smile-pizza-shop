package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.CrustType
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.PizzaCustomizationDataSource
import com.example.model.PizzaTopping
import com.example.model.PortionSize
import com.example.model.ToppingCategory
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishBorderStrong
import com.example.ui.theme.PolishInputBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemCustomizationDialog(
    item: MenuItem,
    onDismiss: () -> Unit,
    onConfirmAddToCart: (
        item: MenuItem,
        size: PortionSize?,
        crust: CrustType?,
        toppings: List<PizzaTopping>,
        unitPrice: Int,
        quantity: Int,
        extraCheese: Boolean,
        spiceLevel: String,
        drinkChoice: String,
        specialInstructions: String,
        dealSummary: String
    ) -> Unit
) {
    val context = LocalContext.current
    val effectiveImageUrl = getEffectiveImageUrl(item)
    val isDealItem = item.category == MenuCategory.DEALS ||
            item.category == MenuCategory.FAMILY_DEALS ||
            item.category == MenuCategory.BIRTHDAY_DEALS ||
            item.dealIncludes.isNotEmpty()
    val isPizzaItem = item.category == MenuCategory.PIZZA ||
            item.category == MenuCategory.SPECIAL_PIZZA ||
            (item.sizeOptions.isNotEmpty() && !isDealItem)

    // 1. Size state
    var selectedSize by remember {
        mutableStateOf(item.sizeOptions.firstOrNull()?.size)
    }

    val currentBasePrice = remember(selectedSize, item) {
        if (selectedSize != null) {
            item.sizeOptions.find { it.size == selectedSize }?.price ?: item.basePrice
        } else {
            item.basePrice
        }
    }

    // 2. Crust state (for pizza items)
    var selectedCrust by remember {
        mutableStateOf<CrustType?>(if (isPizzaItem) CrustType.PAN_THICK else null)
    }

    // 3. Toppings state (for pizza items)
    var selectedToppings by remember {
        mutableStateOf<List<PizzaTopping>>(emptyList())
    }

    var selectedToppingTab by remember {
        mutableStateOf(ToppingCategory.VEGETABLES)
    }

    // Deal Customization selections (Pizza flavor, Shawarma flavor, etc.)
    val pizzaFlavors = listOf("Chicken Tikka", "Chicken Fajita", "Creamy Malai Boti", "Super Supreme", "Cheese Lover", "BBQ Feast")
    var selectedDealPizzaFlavor by remember { mutableStateOf(pizzaFlavors.first()) }

    val shawarmaFlavors = listOf("Zinger Shawarma", "Classic Chicken Shawarma", "Spicy Shawarma", "Platter Shawarma")
    var selectedDealShawarmaFlavor by remember { mutableStateOf(shawarmaFlavors.first()) }

    // 4. Extras & preferences
    var quantity by remember { mutableIntStateOf(1) }
    var extraCheese by remember { mutableStateOf(false) }
    var spiceLevel by remember { mutableStateOf("Normal") }
    var drinkChoice by remember { mutableStateOf("Regular Coke") }
    var specialInstructions by remember { mutableStateOf("") }

    // Price calculation
    val crustPrice = selectedCrust?.priceModifier ?: 0
    val toppingsPrice = selectedToppings.sumOf { it.price }
    val extraCheesePrice = if (extraCheese) 120 else 0

    val unitPriceWithAddons = currentBasePrice + crustPrice + toppingsPrice + extraCheesePrice
    val totalPrice = unitPriceWithAddons * quantity

    val dealCustomizationSummary = remember(isDealItem, selectedDealPizzaFlavor, selectedDealShawarmaFlavor, drinkChoice, selectedSize) {
        if (isDealItem) {
            val parts = mutableListOf<String>()
            parts.add("Pizza: $selectedDealPizzaFlavor")
            if (item.name.contains("Shawarma", ignoreCase = true) || item.description.contains("Shawarma", ignoreCase = true) || item.dealIncludes.any { it.contains("Shawarma", ignoreCase = true) }) {
                parts.add("Shawarma: $selectedDealShawarmaFlavor")
            }
            if (selectedSize != null) {
                parts.add("Size: ${selectedSize?.label}")
            }
            parts.add("Drink: $drinkChoice")
            parts.joinToString(" • ")
        } else {
            ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PolishPrimaryContainerSubtle)
                                .border(1.dp, PolishBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!effectiveImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(effectiveImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.name,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPizzaItem) Icons.Default.LocalPizza else Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = PolishPrimaryRed,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = PolishMaroonDark
                                )
                            )
                            val catLabel = if (!item.customCategoryName.isNullOrBlank()) {
                                item.customCategoryName
                            } else if (isPizzaItem) {
                                "Customize Crust, Toppings & Size"
                            } else {
                                item.category.displayName
                            }
                            Text(
                                text = catLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PolishPrimaryRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_customization_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PolishTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PolishTextMuted,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                )

                // Deal Items & Flavor Customization (Pizza flavor, Shawarma flavor, Drinks)
                if (isDealItem) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Deal Items & Customization / ڈیل کے آئٹمز منتخب کریں",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PolishBgLight)
                            .border(1.dp, PolishBorder, RoundedCornerShape(18.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Pizza Flavor Selection for Deal
                        Text(
                            text = "🍕 Select Pizza Flavor / پیزا فلیور منتخب کریں:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(pizzaFlavors) { flavor ->
                                val isSelected = selectedDealPizzaFlavor == flavor
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDealPizzaFlavor = flavor },
                                    label = { Text(flavor, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PolishPrimaryRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // 2. Shawarma Selection (if applicable or included)
                        Text(
                            text = "🌯 Select Shawarma Flavor / شوارما فلیور:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(shawarmaFlavors) { flavor ->
                                val isSelected = selectedDealShawarmaFlavor == flavor
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDealShawarmaFlavor = flavor },
                                    label = { Text(flavor, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PolishPrimaryRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Deal Summary Preview
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PolishPrimaryContainerSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                        ) {
                            Text(
                                text = "Selected: $dealCustomizationSummary",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishMaroonDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 1. Size Selection
                if (item.sizeOptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "1. Choose Pizza Size *",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PolishBgLight)
                            .border(1.dp, PolishBorder, RoundedCornerShape(18.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item.sizeOptions.forEach { opt ->
                            val isSelected = selectedSize == opt.size
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PolishPrimaryContainerSubtle else Color.White)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PolishPrimaryRed else PolishBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedSize = opt.size }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedSize = opt.size },
                                        colors = RadioButtonDefaults.colors(selectedColor = PolishPrimaryRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = opt.size.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                color = if (isSelected) PolishMaroonDark else PolishTextDark
                                            )
                                        )
                                        val serving = when (opt.size) {
                                            PortionSize.SMALL -> "4 Slices • 1 Person"
                                            PortionSize.MEDIUM -> "6 Slices • 2-3 Persons"
                                            PortionSize.LARGE -> "8 Slices • 3-4 Persons"
                                            PortionSize.EXTRA_LARGE -> "10 Slices • 4-6 Persons"
                                            else -> "Standard Serving"
                                        }
                                        Text(
                                            text = serving,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = PolishTextMuted
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "Rs. ${opt.price}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishPrimaryRed
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Crust Type Selection (For Pizzas)
                if (isPizzaItem) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Select Crust Type *",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark
                            )
                        )
                        selectedCrust?.let { crust ->
                            if (crust.priceModifier > 0) {
                                Text(
                                    text = "+Rs. ${crust.priceModifier}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PolishPrimaryRed
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PolishBgLight)
                            .border(1.dp, PolishBorder, RoundedCornerShape(18.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PizzaCustomizationDataSource.availableCrusts.forEach { crust ->
                            val isSelected = selectedCrust == crust
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PolishPrimaryContainerSubtle else Color.White)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PolishPrimaryRed else PolishBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCrust = crust }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedCrust = crust },
                                        colors = RadioButtonDefaults.colors(selectedColor = PolishPrimaryRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${crust.emoji} ${crust.displayName}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                    color = if (isSelected) PolishMaroonDark else PolishTextDark
                                                )
                                            )
                                        }
                                        Text(
                                            text = crust.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = PolishTextMuted
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = if (crust.priceModifier == 0) "FREE" else "+Rs. ${crust.priceModifier}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (crust.priceModifier == 0) Color(0xFF2E7D32) else PolishPrimaryRed
                                    )
                                )
                            }
                        }
                    }

                    // 3. Toppings Selection (Vegetables, Meats, Cheeses & Sauces)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Customize Toppings",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark
                            )
                        )
                        if (selectedToppings.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PolishPrimaryContainer
                            ) {
                                Text(
                                    text = "${selectedToppings.size} Added (+Rs $toppingsPrice)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishPrimaryRed
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Category tabs for Toppings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ToppingCategory.entries.forEach { category ->
                            val isSelected = selectedToppingTab == category
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PolishMaroonDark else PolishBgLight,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedToppingTab = category }
                            ) {
                                Text(
                                    text = "${category.iconEmoji} ${category.title}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) PolishPrimaryContainer else PolishTextDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toppings Grid for current tab
                    val currentCategoryToppings = PizzaCustomizationDataSource.availableToppings.filter {
                        it.category == selectedToppingTab
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PolishBgLight)
                            .border(1.dp, PolishBorder, RoundedCornerShape(18.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentCategoryToppings.forEach { topping ->
                            val isChecked = selectedToppings.any { it.id == topping.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isChecked) PolishPrimaryContainerSubtle else Color.White)
                                    .border(
                                        width = if (isChecked) 1.5.dp else 1.dp,
                                        color = if (isChecked) PolishPrimaryRed else PolishBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedToppings = if (isChecked) {
                                            selectedToppings.filter { it.id != topping.id }
                                        } else {
                                            selectedToppings + topping
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedToppings = if (checked) {
                                                selectedToppings + topping
                                            } else {
                                                selectedToppings.filter { it.id != topping.id }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = PolishPrimaryRed)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = "${topping.emoji} ${topping.name}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isChecked) PolishMaroonDark else PolishTextDark
                                            )
                                        )
                                        if (topping.description.isNotBlank()) {
                                            Text(
                                                text = topping.description,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.5.sp,
                                                    color = PolishTextMuted
                                                )
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "+Rs. ${topping.price}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PolishPrimaryRed
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Extra Cheese & Spice Level
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isPizzaItem) "4. Cheese & Spice Preferences" else "Add-ons & Preferences",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PolishBgLight)
                        .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                        .clickable { extraCheese = !extraCheese }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = extraCheese,
                            onCheckedChange = { extraCheese = it },
                            colors = CheckboxDefaults.colors(checkedColor = PolishPrimaryRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🧀 Extra Double Mozzarella",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishTextDark
                            )
                        )
                    }
                    Text(
                        text = "+Rs. 120",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Mild", "Normal", "Spicy", "Extra Fiery").forEach { spice ->
                        val isSelected = spiceLevel == spice
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) PolishPrimaryContainerSubtle
                                    else PolishBgLight
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PolishPrimaryRed else PolishBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { spiceLevel = spice }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = spice,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PolishPrimaryRed else PolishTextDark
                                )
                            )
                        }
                    }
                }

                // 5. Drink Choice for Deals
                if (item.dealIncludes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Included Drink Choice",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Coke", "Sprite", "Dew", "Fanta").forEach { drink ->
                            val isSelected = drinkChoice.contains(drink)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) PolishPrimaryContainerSubtle
                                        else PolishBgLight
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PolishPrimaryRed else PolishBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { drinkChoice = drink }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = drink,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PolishPrimaryRed else PolishTextDark
                                    )
                                )
                            }
                        }
                    }
                }

                // 6. Special Instructions
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Normal),
                    label = { Text("Special kitchen instructions (ہدایات)") },
                    placeholder = { Text("e.g. Well-done bake, extra spicy, no onion", color = PolishTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed,
                        unfocusedLabelColor = PolishTextMuted,
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    )
                )

                // 7. Quantity & Add to Cart
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quantity Stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(PolishBgLight)
                            .border(1.dp, PolishBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = PolishMaroonDark)
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = PolishMaroonDark),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = PolishMaroonDark)
                        }
                    }

                    // Confirm Add to Cart button
                    Button(
                        onClick = {
                            onConfirmAddToCart(
                                item,
                                selectedSize,
                                selectedCrust,
                                selectedToppings,
                                currentBasePrice,
                                quantity,
                                extraCheese,
                                spiceLevel,
                                drinkChoice,
                                specialInstructions,
                                dealCustomizationSummary
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishMaroonDark,
                            contentColor = PolishPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .testTag("confirm_add_to_cart_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PolishPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add to Cart • Rs. $totalPrice",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = PolishPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
