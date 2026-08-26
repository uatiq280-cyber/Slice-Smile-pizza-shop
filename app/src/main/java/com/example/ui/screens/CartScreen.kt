package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.LoyaltyProfile
import com.example.model.Order
import com.example.model.PaymentMethod
import com.example.ui.components.WhatsAppOrderHelper
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
import com.example.ui.theme.WhatsAppGreen

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    loyaltyProfile: LoyaltyProfile,
    applyCoinsDiscount: Boolean,
    cartSubtotal: Int,
    potentialCoinsEarned: Int,
    redeemableCoinsDiscount: Int,
    coinsToRedeemCount: Int,
    deliveryFee: Int,
    grandTotal: Int,
    customerName: String,
    customerPhone: String,
    deliveryAddress: String,
    areaLandmark: String,
    orderNote: String,
    selectedPaymentMethod: PaymentMethod,
    easypaisaTrxId: String,
    onQuantityDelta: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onToggleCoinsDiscount: () -> Unit,
    onCustomerNameChanged: (String) -> Unit,
    onCustomerPhoneChanged: (String) -> Unit,
    onOrderNoteChanged: (String) -> Unit,
    onPaymentMethodChanged: (PaymentMethod) -> Unit,
    onOpenEasypaisaModal: () -> Unit,
    onOpenLocationModal: () -> Unit,
    onPlaceOrder: () -> Unit,
    onNavigateToMenu: () -> Unit
) {
    val context = LocalContext.current

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PolishBgLight)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(PolishPrimaryContainerSubtle)
                        .border(1.dp, PolishBorder, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(46.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Your Cart is Empty",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Explore our oven-hot pizzas, loaded burgers, and super saver deals!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PolishTextMuted
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToMenu,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimaryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("cart_empty_browse_btn")
                ) {
                    Text("Browse Menu & Deals 🍕", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBgLight),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. Order Items Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Order (${cartItems.sumOf { it.quantity }})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishMaroonDark,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "Slice Smile Pizzeria",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PolishPrimaryRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(cartItems, key = { it.cartItemId }) { item ->
            CartItemCard(
                item = item,
                onQuantityDelta = { delta -> onQuantityDelta(item.cartItemId, delta) },
                onRemove = { onRemoveItem(item.cartItemId) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Smile Coins Loyalty Box
        item {
            Spacer(modifier = Modifier.height(6.dp))
            LoyaltyCoinsCheckoutCard(
                loyaltyProfile = loyaltyProfile,
                applyCoinsDiscount = applyCoinsDiscount,
                potentialCoinsEarned = potentialCoinsEarned,
                redeemableCoinsDiscount = redeemableCoinsDiscount,
                coinsToRedeemCount = coinsToRedeemCount,
                cartSubtotal = cartSubtotal,
                onToggleDiscount = onToggleCoinsDiscount
            )
        }

        // 3. Customer Contact Info
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customer Details",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = onCustomerNameChanged,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        label = { Text("Customer Full Name *") },
                        placeholder = { Text("e.g. Muhammad Ali", color = PolishTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimaryRed) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input"),
                        shape = RoundedCornerShape(16.dp),
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
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = onCustomerPhoneChanged,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        label = { Text("Contact Phone / WhatsApp *") },
                        placeholder = { Text("e.g. 03001234567", color = PolishTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimaryRed) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input"),
                        shape = RoundedCornerShape(16.dp),
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
                        )
                    )
                }
            }
        }

        // 4. Delivery Address Section
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PolishPrimaryRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delivery Address",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishMaroonDark
                                )
                            )
                        }
                        Text(
                            text = "Change",
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onOpenLocationModal() }
                                .padding(4.dp)
                                .testTag("edit_location_btn")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.5.dp, PolishBorder, RoundedCornerShape(16.dp))
                            .clickable { onOpenLocationModal() }
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = deliveryAddress.ifBlank { "Chowk Nazir Wala" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                            if (areaLandmark.isNotBlank()) {
                                Text(
                                    text = "Landmark: $areaLandmark",
                                    style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = orderNote,
                        onValueChange = onOrderNoteChanged,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Normal),
                        label = { Text("Special Delivery Notes / Rider Instructions") },
                        placeholder = { Text("e.g. Ring bell, extra sauce, hot delivery", color = PolishTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 2,
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
                }
            }
        }

        // 5. Payment Method Selector
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // COD Option (Styled as per Professional Polish)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY) PolishPrimaryContainerSubtle
                                else PolishBgLight
                            )
                            .border(
                                width = if (selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY) 2.dp else 1.dp,
                                color = if (selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY) PolishPrimaryRed else PolishBorder,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onPaymentMethodChanged(PaymentMethod.CASH_ON_DELIVERY) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY,
                            onClick = { onPaymentMethodChanged(PaymentMethod.CASH_ON_DELIVERY) },
                            colors = RadioButtonDefaults.colors(selectedColor = PolishPrimaryRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = PolishPrimaryRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cash on Delivery (COD)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishMaroonDark
                                )
                            )
                            Text(
                                text = "Pay cash when rider delivers to your doorstep",
                                style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Easypaisa Option (Styled as per Professional Polish)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selectedPaymentMethod == PaymentMethod.EASYPAISA) PolishPrimaryContainer
                                else PolishBgLight
                            )
                            .border(
                                width = if (selectedPaymentMethod == PaymentMethod.EASYPAISA) 2.dp else 1.dp,
                                color = if (selectedPaymentMethod == PaymentMethod.EASYPAISA) PolishPrimaryRed else PolishBorder,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onPaymentMethodChanged(PaymentMethod.EASYPAISA) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == PaymentMethod.EASYPAISA,
                            onClick = { onPaymentMethodChanged(PaymentMethod.EASYPAISA) },
                            colors = RadioButtonDefaults.colors(selectedColor = PolishPrimaryRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = PolishPrimaryRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "03254946190 EasyPaisa",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishMaroonDark
                                )
                            )
                            Text(
                                text = "Online direct mobile payment",
                                style = MaterialTheme.typography.bodySmall.copy(color = PolishTextMuted)
                            )
                        }
                        if (selectedPaymentMethod == PaymentMethod.EASYPAISA) {
                            OutlinedButton(
                                onClick = onOpenEasypaisaModal,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PolishPrimaryRed),
                                modifier = Modifier.testTag("open_easypaisa_details_btn")
                            ) {
                                Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PolishPrimaryRed)
                            }
                        }
                    }

                    if (selectedPaymentMethod == PaymentMethod.EASYPAISA && easypaisaTrxId.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PolishPrimaryContainerSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderStrong),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "✅ TRX ID Recorded: $easypaisaTrxId",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PolishPrimaryRed,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Bill Breakdown
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bill Summary",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    BillRow(label = "Subtotal", value = "Rs $cartSubtotal")

                    if (redeemableCoinsDiscount > 0) {
                        BillRow(
                            label = "Smile Coins Discount ($coinsToRedeemCount coins)",
                            value = "-Rs $redeemableCoinsDiscount",
                            valueColor = PolishPrimaryRed
                        )
                    }

                    BillRow(
                        label = "Delivery Fee (Within 3 KM)",
                        value = if (deliveryFee == 0) "FREE" else "Rs $deliveryFee",
                        valueColor = if (deliveryFee == 0) Color(0xFF2E7D32) else PolishTextDark
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = PolishBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Payable",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark
                            )
                        )
                        Text(
                            text = "Rs $grandTotal",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishPrimaryRed,
                                fontSize = 22.sp
                            )
                        )
                    }

                    if (potentialCoinsEarned > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🪙 You will earn +$potentialCoinsEarned Smile Coins on this order!",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PolishPrimaryRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 7. Place Order & WhatsApp Buttons
        item {
            Spacer(modifier = Modifier.height(18.dp))

            // Primary In-App Place Order Button (Matching Professional Polish theme)
            Button(
                onClick = onPlaceOrder,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishMaroonDark,
                    contentColor = PolishPrimaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .testTag("checkout_place_order_btn")
            ) {
                Text(
                    text = "Place Order • Rs $grandTotal",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = PolishPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct WhatsApp Order Button
            Button(
                onClick = {
                    val tempOrder = Order(
                        orderId = System.currentTimeMillis() % 100000,
                        itemsSummary = cartItems.joinToString("\n") { item ->
                            val details = mutableListOf<String>()
                            item.selectedSize?.let { details.add(it.label) }
                            item.selectedCrust?.let { details.add(it.displayName) }
                            if (item.selectedToppings.isNotEmpty()) {
                                details.add("Toppings: " + item.selectedToppings.joinToString(", ") { it.name })
                            }
                            if (item.extraCheese) details.add("+Extra Cheese")
                            if (item.spiceLevel != "Normal") details.add(item.spiceLevel)
                            val detailsStr = if (details.isNotEmpty()) " (${details.joinToString(" • ")})" else ""
                            "• ${item.quantity}x ${item.menuItem.name}$detailsStr = Rs. ${item.totalItemPrice}"
                        },
                        itemsCount = cartItems.sumOf { it.quantity },
                        subtotal = cartSubtotal,
                        discount = redeemableCoinsDiscount,
                        deliveryFee = deliveryFee,
                        totalAmount = grandTotal,
                        paymentMethod = selectedPaymentMethod,
                        easypaisaTrxId = if (selectedPaymentMethod == PaymentMethod.EASYPAISA) easypaisaTrxId.ifBlank { "03254946190" } else null,
                        customerName = customerName.ifBlank { "Customer" },
                        customerPhone = customerPhone.ifBlank { "0300-1234567" },
                        deliveryAddress = deliveryAddress.ifBlank { "Chowk Nazir Wala" },
                        areaLandmark = areaLandmark,
                        orderNote = orderNote,
                        coinsEarned = potentialCoinsEarned,
                        coinsRedeemed = coinsToRedeemCount
                    )
                    WhatsAppOrderHelper.sendOrderToWhatsApp(context, tempOrder)
                    onPlaceOrder()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .testTag("checkout_whatsapp_order_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Order Directly on WhatsApp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityDelta: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.menuItem.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = PolishMaroonDark
                    )
                )
                if (item.selectedSize != null) {
                    Text(
                        text = "Size: ${item.selectedSize.label}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                if (item.selectedCrust != null) {
                    Text(
                        text = "Crust: ${item.selectedCrust.displayName}${if (item.selectedCrust.priceModifier > 0) " (+Rs ${item.selectedCrust.priceModifier})" else ""}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishMaroonDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                if (item.selectedToppings.isNotEmpty()) {
                    Text(
                        text = "+ ${item.selectedToppings.joinToString(", ") { it.name }} (+Rs ${item.selectedToppings.sumOf { it.price }})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishTextDark,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                if (item.extraCheese) {
                    Text(
                        text = "+ Extra Mozzarella Cheese (+Rs 120)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                if (item.spiceLevel != "Normal") {
                    Text(
                        text = "Spice: ${item.spiceLevel}",
                        style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                    )
                }
                if (item.specialInstructions.isNotBlank()) {
                    Text(
                        text = "Note: ${item.specialInstructions}",
                        style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rs ${item.totalItemPrice}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = PolishPrimaryRed
                    )
                )
            }

            // Quantity Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PolishBgLight)
                    .border(1.dp, PolishBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = { onQuantityDelta(-1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    if (item.quantity == 1) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = PolishPrimaryRed, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Remove, contentDescription = "Minus", tint = PolishTextDark, modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = PolishTextDark),
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                IconButton(
                    onClick = { onQuantityDelta(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", tint = PolishPrimaryRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun LoyaltyCoinsCheckoutCard(
    loyaltyProfile: LoyaltyProfile,
    applyCoinsDiscount: Boolean,
    potentialCoinsEarned: Int,
    redeemableCoinsDiscount: Int,
    coinsToRedeemCount: Int,
    cartSubtotal: Int,
    onToggleDiscount: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderStrong),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = PolishPrimaryRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Smile Coins Rewards",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "🪙 ${loyaltyProfile.currentCoins} Coins",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = PolishMaroonDark
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Earn coins on min Rs. 1,500 order\n• 100 Coins = Rs 10 Discount on checkout",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = PolishTextMuted,
                    lineHeight = 16.sp
                )
            )

            if (loyaltyProfile.currentCoins >= 100 && cartSubtotal > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Apply Coins Discount",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishMaroonDark
                            )
                        )
                        Text(
                            text = "Save Rs. ${loyaltyProfile.discountEquivalentRs} using ${loyaltyProfile.currentCoins} coins",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PolishPrimaryRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Switch(
                        checked = applyCoinsDiscount,
                        onCheckedChange = { onToggleDiscount() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimaryRed
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BillRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PolishTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (valueColor != Color.Unspecified) valueColor else PolishTextDark
            )
        )
    }
}

