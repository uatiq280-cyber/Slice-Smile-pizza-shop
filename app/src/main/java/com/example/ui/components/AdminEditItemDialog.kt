package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.PortionSize
import com.example.model.SizeOption
import com.example.ui.theme.BasilGreen
import com.example.ui.theme.CheeseAmber
import com.example.ui.theme.CheeseGold
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminEditItemDialog(
    itemToEdit: MenuItem?, // null if adding new
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    val isNew = itemToEdit == null
    val context = LocalContext.current

    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(itemToEdit?.category ?: MenuCategory.DEALS) }
    var customCategoryName by remember { mutableStateOf(itemToEdit?.customCategoryName ?: "") }
    var imageUrl by remember { mutableStateOf(itemToEdit?.imageUrl ?: "") }
    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var basePriceText by remember { mutableStateOf(itemToEdit?.basePrice?.toString() ?: "500") }
    var tag by remember { mutableStateOf(itemToEdit?.tag ?: "") }
    var isPopular by remember { mutableStateOf(itemToEdit?.isPopular ?: false) }
    var isSpicy by remember { mutableStateOf(itemToEdit?.isSpicy ?: false) }
    var isAvailable by remember { mutableStateOf(itemToEdit?.isAvailable ?: true) }

    // Preset food images
    val presetImages = listOf(
        "Pizza" to "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80",
        "Burger" to "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
        "Shawarma" to "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=600&auto=format&fit=crop&q=80",
        "Pasta" to "https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=600&auto=format&fit=crop&q=80",
        "Pakistani Food" to "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=600&auto=format&fit=crop&q=80",
        "Sweets & Mithai" to "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=600&auto=format&fit=crop&q=80",
        "Dessert & Cake" to "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop&q=80",
        "Cold Drink" to "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600&auto=format&fit=crop&q=80",
        "Broast & Fries" to "https://images.unsplash.com/photo-1562967914-608f82629710?w=600&auto=format&fit=crop&q=80",
        "Ice Cream" to "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=600&auto=format&fit=crop&q=80"
    )

    // Quick category suggestions
    val categorySuggestions = listOf(
        "Sweets",
        "Desserts",
        "Pakistani Food",
        "Barbecue / BBQ",
        "Ice Cream & Shakes",
        "Biryani & Rice",
        "Chaat & Snacks",
        "Salads & Soups"
    )

    // Pizza size prices
    var hasSizes by remember {
        mutableStateOf(
            itemToEdit?.sizeOptions?.isNotEmpty() == true ||
            selectedCategory == MenuCategory.PIZZA ||
            selectedCategory == MenuCategory.SPECIAL_PIZZA
        )
    }

    var smallPriceText by remember {
        mutableStateOf(
            itemToEdit?.sizeOptions?.find { it.size == PortionSize.SMALL }?.price?.toString() ?: "450"
        )
    }
    var mediumPriceText by remember {
        mutableStateOf(
            itemToEdit?.sizeOptions?.find { it.size == PortionSize.MEDIUM }?.price?.toString() ?: "850"
        )
    }
    var largePriceText by remember {
        mutableStateOf(
            itemToEdit?.sizeOptions?.find { it.size == PortionSize.LARGE }?.price?.toString() ?: "1200"
        )
    }
    var xlPriceText by remember {
        mutableStateOf(
            itemToEdit?.sizeOptions?.find { it.size == PortionSize.EXTRA_LARGE }?.price?.toString() ?: "1550"
        )
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleSave = {
        if (name.isBlank()) {
            errorMessage = "Please enter an Item Name"
        } else {
            val basePrice = basePriceText.toIntOrNull() ?: 0

            val sizeOptions = if (hasSizes) {
                listOfNotNull(
                    smallPriceText.toIntOrNull()?.let { SizeOption(PortionSize.SMALL, it) },
                    mediumPriceText.toIntOrNull()?.let { SizeOption(PortionSize.MEDIUM, it) },
                    largePriceText.toIntOrNull()?.let { SizeOption(PortionSize.LARGE, it) },
                    xlPriceText.toIntOrNull()?.let { SizeOption(PortionSize.EXTRA_LARGE, it) }
                )
            } else {
                emptyList()
            }

            val dealItems = if (description.contains("+")) {
                description.split("+").map { it.trim() }.filter { it.isNotEmpty() }
            } else if (description.contains(",")) {
                description.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                listOf(description)
            }

            val finalId = itemToEdit?.id ?: "item_${System.currentTimeMillis()}"

            val finalItem = MenuItem(
                id = finalId,
                name = name.trim(),
                category = selectedCategory,
                customCategoryName = customCategoryName.trim().ifBlank { null },
                imageUrl = imageUrl.trim().ifBlank { null },
                description = description.trim(),
                basePrice = if (hasSizes && sizeOptions.isNotEmpty()) sizeOptions.first().price else basePrice,
                sizeOptions = sizeOptions,
                dealIncludes = if (selectedCategory == MenuCategory.DEALS || selectedCategory == MenuCategory.FAMILY_DEALS || selectedCategory == MenuCategory.BIRTHDAY_DEALS) dealItems else emptyList(),
                isSpicy = isSpicy,
                isPopular = isPopular,
                tag = tag.trim().ifBlank { null },
                imageDrawableRes = itemToEdit?.imageDrawableRes,
                isAvailable = isAvailable
            )

            onSave(finalItem)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(PolishPrimaryContainerSubtle, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isNew) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isNew) "Add Menu Item / Category" else "Edit Item & Rate",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = if (isNew) "نیا مینو آئٹم یا نئی کیٹیگری شامل کریں" else "قیمت اور تفصیلات تبدیل کریں",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    textStyle = TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    label = { Text("Item / Deal Name (نام) *") },
                    placeholder = { Text("e.g. Chicken Biryani or Gulab Jamun", color = PolishTextMuted) },
                    singleLine = true,
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
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_item_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (customCategoryName.isNotBlank()) "Custom: $customCategoryName" else selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        label = { Text("Main Category (بنیادی کیٹیگری)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
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
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        MenuCategory.entries.filter { it != MenuCategory.ALL }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedCategory = cat
                                    if (cat == MenuCategory.PIZZA || cat == MenuCategory.SPECIAL_PIZZA) {
                                        hasSizes = true
                                    }
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Category Name Input
                OutlinedTextField(
                    value = customCategoryName,
                    onValueChange = { customCategoryName = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
                    label = { Text("Custom Category Name (نئی کیٹیگری مثلاً Sweets / Desserts)") },
                    placeholder = { Text("e.g. Sweets, Desserts, Pakistani Food, BBQ", color = PolishTextMuted) },
                    singleLine = true,
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
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Category Suggestion Chips
                Text(
                    text = "Quick Categories (فوری منتخب کریں):",
                    style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categorySuggestions.forEach { suggestion ->
                        val isChosen = customCategoryName.equals(suggestion, ignoreCase = true)
                        FilterChip(
                            selected = isChosen,
                            onClick = {
                                customCategoryName = if (isChosen) "" else suggestion
                            },
                            label = { Text(suggestion, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishPrimaryContainerSubtle,
                                selectedLabelColor = PolishPrimaryRed,
                                containerColor = PolishBgLight,
                                labelColor = PolishTextDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Item Picture / Image URL
                Text(
                    text = "Item Photo / Picture (تصویر):",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishMaroonDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Normal),
                    label = { Text("Image URL (آن لائن تصویر کا لنک یا نیچے سے چنیں)") },
                    placeholder = { Text("https://example.com/item.jpg", color = PolishTextMuted) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = PolishPrimaryRed)
                    },
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
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Preset Photo Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetImages.forEach { (label, url) ->
                        val isSelected = imageUrl == url
                        FilterChip(
                            selected = isSelected,
                            onClick = { imageUrl = url },
                            label = { Text(label, fontSize = 10.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishPrimaryRed,
                                selectedLabelColor = Color.White,
                                containerColor = PolishBgLight,
                                labelColor = PolishTextDark
                            )
                        )
                    }
                }

                if (imageUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, PolishBorder, RoundedCornerShape(12.dp))
                            .background(PolishBgLight)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Description / Deal Ingredients
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Normal),
                    label = { Text("Description / Includes (تفصیل / ڈیل کے اجزاء)") },
                    placeholder = { Text("e.g. Freshly cooked with premium spices", color = PolishTextMuted) },
                    minLines = 2,
                    maxLines = 4,
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
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Pizza Sizes Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PolishBgLight, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Multiple Sizes (Small/Med/Large)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishTextDark,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "سائز کے مطابق الگ الگ ریٹس",
                            style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted, fontSize = 10.sp)
                        )
                    }
                    Switch(
                        checked = hasSizes,
                        onCheckedChange = { hasSizes = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimaryRed
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price Inputs
                if (hasSizes) {
                    Text(
                        text = "Size Rates (روپے):",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = smallPriceText,
                            onValueChange = { smallPriceText = it },
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            label = { Text("Small (S)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
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
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mediumPriceText,
                            onValueChange = { mediumPriceText = it },
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            label = { Text("Medium (M)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
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
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = largePriceText,
                            onValueChange = { largePriceText = it },
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            label = { Text("Large (L)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
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
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = xlPriceText,
                            onValueChange = { xlPriceText = it },
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            label = { Text("XL (Extra)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
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
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = basePriceText,
                        onValueChange = { basePriceText = it },
                        textStyle = TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        label = { Text("Price (قیمت - Rs.)") },
                        placeholder = { Text("e.g. 450", color = PolishTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = null,
                                tint = PolishPrimaryRed
                            )
                        },
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
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_base_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tag (Super Saver, Best Value, etc.)
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    label = { Text("Badge / Tag (اختیاری ٹیگ)") },
                    placeholder = { Text("e.g. Super Saver / Hot Deal", color = PolishTextMuted) },
                    singleLine = true,
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
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Availability & Flags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PolishBgLight, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isAvailable) "Item is In Stock (دستیاب ہے) ✅" else "Out of Stock (ختم ہے) ❌",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) BasilGreen else PolishPrimaryRed
                        )
                    )
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BasilGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isPopular,
                        onClick = { isPopular = !isPopular },
                        label = { Text("⭐ Popular Deal", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CheeseGold,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isSpicy,
                        onClick = { isSpicy = !isSpicy },
                        label = { Text("🌶️ Spicy", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishPrimaryContainer,
                            selectedLabelColor = PolishPrimaryRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = PolishPrimaryRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = handleSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishPrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("admin_save_item_btn")
            ) {
                Text("Save & Apply Live ✅", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
            ) {
                Text("Cancel", color = PolishTextMuted)
            }
        }
    )
}
