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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(itemToEdit?.category ?: MenuCategory.DEALS) }
    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var basePriceText by remember { mutableStateOf(itemToEdit?.basePrice?.toString() ?: "500") }
    var tag by remember { mutableStateOf(itemToEdit?.tag ?: "") }
    var isPopular by remember { mutableStateOf(itemToEdit?.isPopular ?: false) }
    var isSpicy by remember { mutableStateOf(itemToEdit?.isSpicy ?: false) }
    var isAvailable by remember { mutableStateOf(itemToEdit?.isAvailable ?: true) }

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
                        text = if (isNew) "Add New Deal / Item" else "Edit Item & Rate",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextDark,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = if (isNew) "نیا مینو آئٹم یا ڈیل شامل کریں" else "قیمت اور تفصیلات تبدیل کریں",
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
                    label = { Text("Item / Deal Name (نام)") },
                    placeholder = { Text("e.g. Deal No 7 or Chicken Tikka Pizza") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
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
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category (کیٹیگری)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
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

                // 3. Description / Deal Ingredients
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Includes (تفصیل / ڈیل کے اجزاء)") },
                    placeholder = { Text("e.g. 1 Large Pizza + 2 Zingers + 1 Litre Coke") },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Pizza Sizes Toggle
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
                            text = "Multiple Pizza Sizes (Small/Med/Large)",
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
                        text = "Pizza Size Rates (روپے):",
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
                            label = { Text("Small (S)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PolishPrimaryRed,
                                unfocusedBorderColor = PolishInputBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mediumPriceText,
                            onValueChange = { mediumPriceText = it },
                            label = { Text("Medium (M)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
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
                            label = { Text("Large (L)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PolishPrimaryRed,
                                unfocusedBorderColor = PolishInputBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = xlPriceText,
                            onValueChange = { xlPriceText = it },
                            label = { Text("XL (Extra)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
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
                        label = { Text("Price (قیمت - Rs.)") },
                        placeholder = { Text("e.g. 450") },
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
                    label = { Text("Badge / Tag (اختیاری ٹیگ)") },
                    placeholder = { Text("e.g. Super Saver / Hot Deal") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
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
