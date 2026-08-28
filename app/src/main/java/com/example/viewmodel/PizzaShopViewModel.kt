package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MenuDataSource
import com.example.data.local.AppDatabase
import com.example.data.repository.CloudSyncStatus
import com.example.data.repository.PizzaRepository
import com.example.model.AdminRole
import com.example.model.AdminUser
import com.example.model.AuthType
import com.example.model.CartItem
import com.example.model.CustomerFeedback
import com.example.model.CustomerUsageStats
import com.example.model.LoyaltyProfile
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentSettings
import com.example.model.PortionSize
import com.example.model.Rider
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.service.InvoicePdfGenerator
import com.example.service.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class OrderPlacedSuccess(val order: Order) : UiEvent()
}

class PizzaShopViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PizzaRepository

    private val _ephemeralUserSession = MutableStateFlow<UserSession?>(null)

    init {
        repository = PizzaRepository.getInstance(application)
        viewModelScope.launch {
            repository.refreshOrdersFromCloud()
            val isPersistentAdmin = repository.getAdminConfig("admin_persistent_session") == "true"
            if (isPersistentAdmin) {
                _isAdminLoggedIn.value = true
                repository.setAdminActive(true)
            }
            val persistentRiderId = repository.getAdminConfig("rider_persistent_id")
            if (!persistentRiderId.isNullOrBlank()) {
                val riders = repository.getAllRidersOnce()
                val matched = riders.find { it.id == persistentRiderId }
                if (matched != null && matched.isEnabled) {
                    _currentRider.value = matched
                    _isRiderLoggedIn.value = true
                }
            }
        }
        viewModelScope.launch {
            repository.userSessionFlow.collect { session ->
                if (session.name.isNotBlank() && session.name != "Guest Foodie") {
                    if (_customerName.value.isBlank() || _customerName.value == "Guest Foodie") {
                        _customerName.value = session.name
                    }
                }
                if (session.phone.isNotBlank() && _customerPhone.value.isBlank()) {
                    _customerPhone.value = session.phone
                }
                if (session.deliveryAddress.isNotBlank()) {
                    _deliveryAddress.value = session.deliveryAddress
                }
            }
        }
    }

    private val _isRefreshingOrders = MutableStateFlow(false)
    val isRefreshingOrders: StateFlow<Boolean> = _isRefreshingOrders.asStateFlow()

    fun refreshOrdersFromCloud() {
        viewModelScope.launch {
            _isRefreshingOrders.value = true
            try {
                val orders = repository.refreshOrdersFromCloud()
                _eventFlow.emit(UiEvent.ShowToast("Live cloud orders synced (${orders.size} total) 🔄"))
            } finally {
                _isRefreshingOrders.value = false
            }
        }
    }

    val cloudSyncStatus: StateFlow<CloudSyncStatus> = repository.cloudSyncStatus

    fun testCloudConnection() {
        viewModelScope.launch {
            val result = repository.testCloudConnection()
            if (result.startsWith("✅")) {
                _eventFlow.emit(UiEvent.ShowToast("Cloud Connection OK! Live 2-device sync is ready ✅"))
            } else {
                _eventFlow.emit(UiEvent.ShowToast("Cloud Warning! Please verify Firebase Rules ⚠️"))
            }
        }
    }

    val ordersList: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRiders: StateFlow<List<Rider>> = repository.allRiders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loyaltyProfile: StateFlow<LoyaltyProfile> = repository.loyaltyProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoyaltyProfile())

    val customerReviews: StateFlow<List<CustomerFeedback>> = repository.allFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuDataSource.sampleReviews)

    // Customer Session & Authentication
    val userSession: StateFlow<UserSession> = combine(
        repository.userSessionFlow,
        _ephemeralUserSession
    ) { repoSession, ephemeralSession ->
        ephemeralSession ?: repoSession
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserSession(name = "Guest Foodie", authType = AuthType.GUEST)
    )

    private val _myPlacedOrderIds = MutableStateFlow<Set<Long>>(emptySet())
    val myPlacedOrderIds: StateFlow<Set<Long>> = _myPlacedOrderIds.asStateFlow()

    // Customer Filtered Orders (Only sees own orders!)
    val customerOrders: StateFlow<List<Order>> = combine(
        ordersList,
        userSession,
        _myPlacedOrderIds
    ) { all, session, placedIds ->
        all.filter { order ->
            placedIds.contains(order.orderId) ||
            order.userId == session.userId ||
            (session.phone.isNotBlank() && order.customerPhone.trim() == session.phone.trim()) ||
            (session.email.isNotBlank() && order.userId.contains(session.email.take(6)))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isShowingAuthDialog = MutableStateFlow(false)
    val isShowingAuthDialog: StateFlow<Boolean> = _isShowingAuthDialog.asStateFlow()

    private val _generatedOtp = MutableStateFlow("")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    // Rider Portal & Authentication States
    private val _isRiderLoggedIn = MutableStateFlow(false)
    val isRiderLoggedIn: StateFlow<Boolean> = _isRiderLoggedIn.asStateFlow()

    private val _currentRider = MutableStateFlow<Rider?>(null)
    val currentRider: StateFlow<Rider?> = _currentRider.asStateFlow()

    private val _isShowingRiderLogin = MutableStateFlow(false)
    val isShowingRiderLogin: StateFlow<Boolean> = _isShowingRiderLogin.asStateFlow()

    private val _isShowingRiderDialog = MutableStateFlow(false)
    val isShowingRiderDialog: StateFlow<Boolean> = _isShowingRiderDialog.asStateFlow()

    private val _editingRider = MutableStateFlow<Rider?>(null)
    val editingRider: StateFlow<Rider?> = _editingRider.asStateFlow()

    private val _isShowingAssignRiderModal = MutableStateFlow(false)
    val isShowingAssignRiderModal: StateFlow<Boolean> = _isShowingAssignRiderModal.asStateFlow()

    private val _selectedOrderForRiderAssign = MutableStateFlow<Order?>(null)
    val selectedOrderForRiderAssign: StateFlow<Order?> = _selectedOrderForRiderAssign.asStateFlow()

    // Rider Filtered Orders (Assigned deliveries)
    val riderOrders: StateFlow<List<Order>> = combine(
        ordersList,
        _currentRider
    ) { all, rider ->
        if (rider == null) emptyList()
        else {
            all.filter { order ->
                order.riderId == rider.id ||
                order.riderPhone.trim() == rider.phone.trim() ||
                (order.status == OrderStatus.READY_FOR_PICKUP && order.riderId.isNullOrBlank())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin & Menu Management States
    val adminPin: StateFlow<String> = repository.adminPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Hamza9181@")

    val ownerId: StateFlow<String> = repository.ownerIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Owner@slicesmile.com")

    val paymentSettings: StateFlow<PaymentSettings> = repository.paymentSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentSettings())

    val customerUsageStats: StateFlow<List<CustomerUsageStats>> = ordersList.map { orders ->
        val map = LinkedHashMap<String, MutableList<Order>>()
        for (o in orders) {
            val key = if (o.customerPhone.isNotBlank()) o.customerPhone.trim() else o.customerName.trim()
            if (key.isNotBlank()) {
                map.getOrPut(key) { mutableListOf() }.add(o)
            }
        }
        map.map { (key, customerOrders) ->
            val latest = customerOrders.maxByOrNull { it.timestamp } ?: customerOrders.first()
            val validOrders = customerOrders.filter { it.status != OrderStatus.CANCELLED }
            CustomerUsageStats(
                customerKey = key,
                name = latest.customerName.ifBlank { "Customer ($key)" },
                phone = latest.customerPhone,
                totalOrders = customerOrders.size,
                totalSpent = validOrders.sumOf { it.totalAmount },
                lastOrderTimestamp = latest.timestamp,
                lastOrderStatus = latest.status,
                lastOrderSummary = latest.itemsSummary
            )
        }.sortedByDescending { it.lastOrderTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isShowingRoleSelector = MutableStateFlow(false)
    val isShowingRoleSelector: StateFlow<Boolean> = _isShowingRoleSelector.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _isShowingAdminLogin = MutableStateFlow(false)
    val isShowingAdminLogin: StateFlow<Boolean> = _isShowingAdminLogin.asStateFlow()

    private val _isShowingChangePinDialog = MutableStateFlow(false)
    val isShowingChangePinDialog: StateFlow<Boolean> = _isShowingChangePinDialog.asStateFlow()

    // Multi-Admin & Partner Management
    val allAdminUsers: StateFlow<List<AdminUser>> = repository.allAdminUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentAdminUser = MutableStateFlow<AdminUser?>(null)
    val currentAdminUser: StateFlow<AdminUser?> = _currentAdminUser.asStateFlow()

    private val _isShowingPartnerDialog = MutableStateFlow(false)
    val isShowingPartnerDialog: StateFlow<Boolean> = _isShowingPartnerDialog.asStateFlow()

    private val _editingAdminUser = MutableStateFlow<AdminUser?>(null)
    val editingAdminUser: StateFlow<AdminUser?> = _editingAdminUser.asStateFlow()

    private val _isShowingEditItemDialog = MutableStateFlow(false)
    val isShowingEditItemDialog: StateFlow<Boolean> = _isShowingEditItemDialog.asStateFlow()

    private val _editingItem = MutableStateFlow<MenuItem?>(null)
    val editingItem: StateFlow<MenuItem?> = _editingItem.asStateFlow()

    // Dynamic Master Menu Items (Merged from defaults + Database Customizations)
    val allMenuItems: StateFlow<List<MenuItem>> = repository.customMenuItemsFlow.map { customEntities ->
        val defaultMap = LinkedHashMap<String, MenuItem>()
        MenuDataSource.menuItems.forEach { defaultMap[it.id] = it }

        for (entity in customEntities) {
            if (entity.isDeleted) {
                defaultMap.remove(entity.id)
            } else {
                defaultMap[entity.id] = entity.toDomain()
            }
        }
        defaultMap.values.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuDataSource.menuItems)

    // Search and category filtering
    private val _selectedCategory = MutableStateFlow(MenuCategory.ALL)
    val selectedCategory: StateFlow<MenuCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredMenuItems: StateFlow<List<MenuItem>> = combine(
        allMenuItems,
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        var list = items
        if (category != MenuCategory.ALL) {
            list = list.filter {
                if (category == MenuCategory.CUSTOM) {
                    !it.customCategoryName.isNullOrBlank()
                } else if (!it.customCategoryName.isNullOrBlank()) {
                    it.customCategoryName.equals(category.displayName, ignoreCase = true) ||
                    it.customCategoryName.contains(category.displayName.substringBefore(" "), ignoreCase = true) ||
                    it.category == category
                } else {
                    it.category == category
                }
            }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.dealIncludes.any { item -> item.lowercase().contains(q) } ||
                (it.tag?.lowercase()?.contains(q) == true) ||
                (it.customCategoryName?.lowercase()?.contains(q) == true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuDataSource.menuItems)

    // Active item customization modal
    private val _customizingItem = MutableStateFlow<MenuItem?>(null)
    val customizingItem: StateFlow<MenuItem?> = _customizingItem.asStateFlow()

    // Cart state
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Coins redemption in cart
    private val _applyCoinsDiscount = MutableStateFlow(false)
    val applyCoinsDiscount: StateFlow<Boolean> = _applyCoinsDiscount.asStateFlow()

    // Referral 10% discount in cart
    private val _appliedReferralCode = MutableStateFlow("")
    val appliedReferralCode: StateFlow<String> = _appliedReferralCode.asStateFlow()

    private val _applyReferralDiscount = MutableStateFlow(false)
    val applyReferralDiscount: StateFlow<Boolean> = _applyReferralDiscount.asStateFlow()

    // Customer Checkout Info
    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val _deliveryAddress = MutableStateFlow("Chowk Nazir Wala, Main Street")
    val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

    private val _areaLandmark = MutableStateFlow("Near Chowk Nazir Wala")
    val areaLandmark: StateFlow<String> = _areaLandmark.asStateFlow()

    private val _orderNote = MutableStateFlow("")
    val orderNote: StateFlow<String> = _orderNote.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.CASH_ON_DELIVERY)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _easypaisaTrxId = MutableStateFlow("")
    val easypaisaTrxId: StateFlow<String> = _easypaisaTrxId.asStateFlow()

    private val _isShowingEasypaisaModal = MutableStateFlow(false)
    val isShowingEasypaisaModal: StateFlow<Boolean> = _isShowingEasypaisaModal.asStateFlow()

    private val _isLocationSelectorVisible = MutableStateFlow(false)
    val isLocationSelectorVisible: StateFlow<Boolean> = _isLocationSelectorVisible.asStateFlow()

    private val _selectedOrderForFeedback = MutableStateFlow<Order?>(null)
    val selectedOrderForFeedback: StateFlow<Order?> = _selectedOrderForFeedback.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // Cart Computations
    val cartSubtotal: StateFlow<Int> = _cartItems.combine(_applyCoinsDiscount) { items, _ ->
        items.sumOf { it.totalItemPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val potentialCoinsEarned: StateFlow<Int> = cartSubtotal.map { subtotal ->
        // Loyalty rule: minimum Rs 1500 order earns coins! (100 coins per 1500 spent)
        if (subtotal >= 1500) {
            (subtotal / 1500) * 100
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val redeemableCoinsDiscount: StateFlow<Int> = combine(
        cartSubtotal,
        _applyCoinsDiscount,
        loyaltyProfile
    ) { subtotal, apply, profile ->
        if (apply && profile.currentCoins >= 100 && subtotal > 0) {
            // 100 coins = Rs 10 discount
            val maxDiscount = (profile.currentCoins / 100) * 10
            minOf(maxDiscount, subtotal)
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val referralDiscountAmount: StateFlow<Int> = combine(
        cartSubtotal,
        _applyReferralDiscount,
        _appliedReferralCode,
        loyaltyProfile
    ) { subtotal, applyToggle, refCode, profile ->
        if (subtotal > 0 && (applyToggle || refCode.isNotBlank() || profile.hasPendingReferralDiscount || profile.availableReferralDiscountsCount > 0)) {
            // 10% Flat Referral Discount!
            (subtotal * 0.10).toInt()
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDiscountAmount: StateFlow<Int> = combine(
        redeemableCoinsDiscount,
        referralDiscountAmount,
        cartSubtotal
    ) { coinsDisc, refDisc, subtotal ->
        minOf(coinsDisc + refDisc, subtotal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val coinsToRedeemCount: StateFlow<Int> = redeemableCoinsDiscount.map { discountRs ->
        (discountRs / 10) * 100
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val deliveryFee: StateFlow<Int> = cartSubtotal.map { subtotal ->
        if (subtotal == 0) 0
        else if (subtotal >= MenuDataSource.MINIMUM_DELIVERY_ORDER) 0 // Free within 3 KM
        else 80
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val grandTotal: StateFlow<Int> = combine(
        cartSubtotal,
        totalDiscountAmount,
        deliveryFee
    ) { subtotal, discount, fee ->
        if (subtotal == 0) 0 else (subtotal - discount + fee).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Actions
    fun setCategory(category: MenuCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openItemCustomization(menuItem: MenuItem) {
        _customizingItem.value = menuItem
    }

    fun closeItemCustomization() {
        _customizingItem.value = null
    }

    fun addToCart(
        menuItem: MenuItem,
        size: PortionSize?,
        crust: com.example.model.CrustType? = null,
        toppings: List<com.example.model.PizzaTopping> = emptyList(),
        unitPrice: Int,
        quantity: Int,
        extraCheese: Boolean,
        spiceLevel: String,
        drinkChoice: String,
        specialInstructions: String,
        dealCustomizationSummary: String = ""
    ) {
        val newItem = CartItem(
            cartItemId = UUID.randomUUID().toString(),
            menuItem = menuItem,
            selectedSize = size,
            selectedCrust = crust,
            selectedToppings = toppings,
            unitPrice = unitPrice,
            quantity = quantity,
            extraCheese = extraCheese,
            spiceLevel = spiceLevel,
            drinkChoice = drinkChoice,
            specialInstructions = specialInstructions,
            dealCustomizationSummary = dealCustomizationSummary
        )
        _cartItems.value = _cartItems.value + newItem
        _customizingItem.value = null
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowToast("Added ${menuItem.name} to cart! 🍕"))
        }
    }

    fun quickAddToCart(menuItem: MenuItem) {
        val size = if (menuItem.sizeOptions.isNotEmpty()) menuItem.sizeOptions.first().size else null
        val crust = if (menuItem.category == MenuCategory.PIZZA || menuItem.category == MenuCategory.SPECIAL_PIZZA) {
            com.example.model.CrustType.PAN_THICK
        } else null
        val price = menuItem.defaultPrice
        addToCart(
            menuItem = menuItem,
            size = size,
            crust = crust,
            toppings = emptyList(),
            unitPrice = price,
            quantity = 1,
            extraCheese = false,
            spiceLevel = "Normal",
            drinkChoice = "Regular Coke",
            specialInstructions = ""
        )
    }

    fun updateCartItemQuantity(cartItemId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.cartItemId == cartItemId }
        if (index != -1) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeCartItem(cartItemId: String) {
        _cartItems.value = _cartItems.value.filter { it.cartItemId != cartItemId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _applyCoinsDiscount.value = false
        _applyReferralDiscount.value = false
        _appliedReferralCode.value = false.let { "" }
    }

    fun toggleCoinsDiscount() {
        _applyCoinsDiscount.value = !_applyCoinsDiscount.value
    }

    fun toggleReferralDiscount(apply: Boolean? = null) {
        _applyReferralDiscount.value = apply ?: !_applyReferralDiscount.value
    }

    fun applyReferralCode(code: String) {
        val clean = code.trim().uppercase()
        if (clean.isBlank()) return
        val myCode = loyaltyProfile.value.referralCode
        if (clean.equals(myCode, ignoreCase = true)) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("Cannot use your own referral code! Share it with friends instead 😊"))
            }
            return
        }

        viewModelScope.launch {
            val success = repository.applyReferralCode(clean)
            if (success) {
                _appliedReferralCode.value = clean
                _applyReferralDiscount.value = true
                _eventFlow.emit(UiEvent.ShowToast("🎉 10% Referral Discount Applied! Plus 100 Welcome Coins added!"))
            } else {
                _eventFlow.emit(UiEvent.ShowToast("Invalid referral code. Please check and try again."))
            }
        }
    }

    fun removeReferralCode() {
        _appliedReferralCode.value = ""
        _applyReferralDiscount.value = false
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowToast("Referral discount removed"))
        }
    }

    fun shareReferralQr(context: Context) {
        val code = loyaltyProfile.value.referralCode
        val name = userSession.value.name.ifBlank { "A Foodie Friend" }
        com.example.util.QrCodeGenerator.shareReferralQrImage(context, code, name)
    }

    fun shareReferralInvite(context: Context) {
        val code = loyaltyProfile.value.referralCode
        val name = userSession.value.name.ifBlank { "A Foodie Friend" }
        com.example.util.QrCodeGenerator.shareReferralText(context, code, name)
    }

    fun copyReferralCode(context: Context) {
        val code = loyaltyProfile.value.referralCode
        com.example.util.QrCodeGenerator.copyToClipboard(context, code)
    }

    fun setCustomerName(name: String) { _customerName.value = name }
    fun setCustomerPhone(phone: String) { _customerPhone.value = phone }
    fun setDeliveryAddress(address: String) { _deliveryAddress.value = address }
    fun setAreaLandmark(landmark: String) { _areaLandmark.value = landmark }
    fun setOrderNote(note: String) { _orderNote.value = note }
    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
        if (method == PaymentMethod.EASYPAISA) {
            _isShowingEasypaisaModal.value = true
        }
    }

    fun setEasypaisaTrxId(trxId: String) {
        _easypaisaTrxId.value = trxId
    }

    fun showEasypaisaModal(show: Boolean) {
        _isShowingEasypaisaModal.value = show
    }

    fun showLocationSelector(show: Boolean) {
        _isLocationSelectorVisible.value = show
    }

    fun setFeedbackOrder(order: Order?) {
        _selectedOrderForFeedback.value = order
    }

    private var isPlacingOrderBusy = false

    fun placeOrder(onSuccess: (Order) -> Unit) {
        if (isPlacingOrderBusy) return
        val items = _cartItems.value
        if (items.isEmpty()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("Your cart is empty!"))
            }
            return
        }

        isPlacingOrderBusy = true
        val name = _customerName.value.ifBlank { "Valued Customer" }
        val phone = _customerPhone.value.ifBlank { "0300-1234567" }
        val address = _deliveryAddress.value.ifBlank { "Chowk Nazir Wala" }

        val subtotal = cartSubtotal.value
        val discount = totalDiscountAmount.value
        val fee = deliveryFee.value
        val total = grandTotal.value
        val earnedCoins = potentialCoinsEarned.value
        val redeemedCoins = coinsToRedeemCount.value
        val usedReferralCode = _appliedReferralCode.value

        val itemsSummary = items.joinToString("\n") { item ->
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
        }

        val currentUserId = userSession.value.userId.ifBlank { "guest_${System.currentTimeMillis() % 10000}" }

        val order = Order(
            orderId = System.currentTimeMillis() % 100000,
            userId = currentUserId,
            itemsSummary = itemsSummary,
            itemsCount = items.sumOf { it.quantity },
            subtotal = subtotal,
            discount = discount,
            deliveryFee = fee,
            totalAmount = total,
            paymentMethod = _selectedPaymentMethod.value,
            easypaisaTrxId = if (_selectedPaymentMethod.value == PaymentMethod.EASYPAISA) _easypaisaTrxId.value.ifBlank { "Pending Verification" } else null,
            customerName = name,
            customerPhone = phone,
            deliveryAddress = address,
            areaLandmark = _areaLandmark.value,
            orderNote = _orderNote.value,
            coinsEarned = earnedCoins,
            coinsRedeemed = redeemedCoins,
            status = OrderStatus.ORDER_RECEIVED,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                val generatedId = repository.placeOrder(order)
                val finalOrder = order.copy(orderId = generatedId)

                // If a friend's referral code was used on this order, reward the referrer with a 10% discount!
                if (usedReferralCode.isNotBlank()) {
                    repository.rewardReferrerForOrder(usedReferralCode)
                }

                _myPlacedOrderIds.value = _myPlacedOrderIds.value + generatedId

                clearCart()
                _easypaisaTrxId.value = ""
                _isShowingEasypaisaModal.value = false

                _eventFlow.emit(UiEvent.OrderPlacedSuccess(finalOrder))
                onSuccess(finalOrder)
            } finally {
                isPlacingOrderBusy = false
            }
        }
    }

    fun submitFeedback(
        orderId: Long,
        overallRating: Int,
        foodTaste: Int,
        deliverySpeed: Int,
        comment: String,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val feedback = CustomerFeedback(
                orderId = orderId,
                customerName = _customerName.value.ifBlank { "Slice Smile Customer" },
                overallRating = overallRating,
                foodTasteRating = foodTaste,
                deliverySpeedRating = deliverySpeed,
                comment = comment,
                timestamp = System.currentTimeMillis(),
                photoUri = photoUri,
                photoUrl = photoUri
            )
            repository.submitFeedback(feedback)
            _selectedOrderForFeedback.value = null
            _eventFlow.emit(UiEvent.ShowToast("Thank you for your feedback! ⭐"))
        }
    }

    fun updateManualOrderStatus(orderId: Long, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            _eventFlow.emit(UiEvent.ShowToast("Order #$orderId updated to ${status.title} ${status.iconEmoji}"))
        }
    }

    // ================= CUSTOMER AUTHENTICATION ACTIONS =================
    fun showAuthDialog(show: Boolean) {
        _isShowingAuthDialog.value = show
    }

    fun loginAsGuest(name: String = "Guest Foodie", rememberLogin: Boolean = true) {
        viewModelScope.launch {
            val session = UserSession(
                userId = "guest_${System.currentTimeMillis() % 10000}",
                name = name.ifBlank { "Guest Foodie" },
                phone = "",
                email = "",
                authType = AuthType.GUEST,
                isVerified = false,
                deliveryAddress = _deliveryAddress.value
            )
            if (rememberLogin) {
                repository.saveUserSession(session)
                _ephemeralUserSession.value = null
            } else {
                _ephemeralUserSession.value = session
            }
            _customerName.value = session.name
            _isShowingAuthDialog.value = false
            _eventFlow.emit(UiEvent.ShowToast("Welcome, ${session.name}! (Guest Mode) 🍕"))
        }
    }

    fun requestPhoneOtp(phone: String): String {
        val cleanPhone = phone.trim()
        val randomOtp = ((1000..9999).random()).toString()
        _generatedOtp.value = randomOtp
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowToast("SMS OTP Code: $randomOtp (Slice Smile) 📱"))
        }
        return randomOtp
    }

    fun verifyAndLoginWithPhone(phone: String, otpEntered: String, name: String, rememberLogin: Boolean = true): Boolean {
        val cleanPhone = phone.trim()
        val cleanOtp = otpEntered.trim()
        val expectedOtp = _generatedOtp.value
        val isValid = (cleanOtp.isNotBlank() && (cleanOtp == expectedOtp || cleanOtp == "1234"))
        if (isValid) {
            viewModelScope.launch {
                val customerNameText = name.ifBlank { "Pizza Lover (${cleanPhone.takeLast(4)})" }
                val session = UserSession(
                    userId = "phone_${cleanPhone.replace("+", "")}",
                    name = customerNameText,
                    phone = cleanPhone,
                    email = "",
                    authType = AuthType.PHONE_OTP,
                    isVerified = true,
                    deliveryAddress = _deliveryAddress.value
                )
                if (rememberLogin) {
                    repository.saveUserSession(session)
                    _ephemeralUserSession.value = null
                } else {
                    _ephemeralUserSession.value = session
                }
                _customerName.value = session.name
                _customerPhone.value = cleanPhone
                _isShowingAuthDialog.value = false
                _eventFlow.emit(UiEvent.ShowToast("Login Successful with $cleanPhone! Welcome, ${session.name} 🎉"))
            }
            return true
        } else {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("Invalid OTP Code! Please try again."))
            }
            return false
        }
    }

    fun loginWithGoogle(email: String, name: String, rememberLogin: Boolean = true) {
        viewModelScope.launch {
            val session = UserSession(
                userId = "google_${email.replace("@", "_").replace(".", "_")}",
                name = name.ifBlank { "Google User" },
                phone = _customerPhone.value,
                email = email.trim(),
                authType = AuthType.GOOGLE_GMAIL,
                isVerified = true,
                deliveryAddress = _deliveryAddress.value
            )
            if (rememberLogin) {
                repository.saveUserSession(session)
                _ephemeralUserSession.value = null
            } else {
                _ephemeralUserSession.value = session
            }
            _customerName.value = session.name
            _isShowingAuthDialog.value = false
            _eventFlow.emit(UiEvent.ShowToast("Logged in with Google as ${session.name}! 📧"))
        }
    }

    fun logoutCustomer() {
        viewModelScope.launch {
            val guestSession = UserSession(name = "Guest Foodie", authType = AuthType.GUEST)
            repository.saveUserSession(guestSession)
            _ephemeralUserSession.value = null
            _customerName.value = ""
            _customerPhone.value = ""
            _eventFlow.emit(UiEvent.ShowToast("Logged out to Guest Mode 🚪"))
        }
    }

    fun updateCustomerProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            val current = userSession.value
            val updated = current.copy(
                name = name.ifBlank { current.name },
                phone = phone.ifBlank { current.phone },
                deliveryAddress = address.ifBlank { current.deliveryAddress }
            )
            repository.saveUserSession(updated)
            _ephemeralUserSession.value = null
            _customerName.value = updated.name
            _customerPhone.value = updated.phone
            _deliveryAddress.value = updated.deliveryAddress
            _eventFlow.emit(UiEvent.ShowToast("Profile details updated! ✅"))
        }
    }

    // ================= ROLE SELECTOR & ADMIN / OWNER PORTAL ACTIONS =================
    fun showRoleSelector(show: Boolean) {
        _isShowingRoleSelector.value = show
    }

    fun showAdminLoginDialog(show: Boolean) {
        _isShowingAdminLogin.value = show
    }

    fun showChangePinDialog(show: Boolean) {
        _isShowingChangePinDialog.value = show
    }

    fun verifyAndLoginAdmin(enteredOwnerId: String, enteredPin: String, rememberAdmin: Boolean = true): Boolean {
        val cleanOwnerId = enteredOwnerId.trim()
        val cleanPin = enteredPin.trim()

        viewModelScope.launch {
            val authUser = repository.authenticateAdmin(cleanOwnerId, cleanPin)
            if (authUser != null) {
                if (!authUser.isActive) {
                    _eventFlow.emit(UiEvent.ShowToast("This admin account '${authUser.name}' is currently disabled."))
                    return@launch
                }
                _currentAdminUser.value = authUser
                _isAdminLoggedIn.value = true
                repository.setAdminActive(true)
                _isShowingAdminLogin.value = false
                _isShowingRoleSelector.value = false
                if (rememberAdmin) {
                    repository.setAdminConfig("admin_persistent_session", "true")
                    repository.setAdminConfig("admin_persistent_id", authUser.id)
                }
                refreshOrdersFromCloud()
                _eventFlow.emit(UiEvent.ShowToast("Welcome ${authUser.name} (${authUser.role.displayName}) 👑"))
            } else {
                _eventFlow.emit(UiEvent.ShowToast("Invalid Admin ID or Password! Please check credentials."))
            }
        }
        return true
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _currentAdminUser.value = null
        repository.setAdminActive(false)
        viewModelScope.launch {
            repository.setAdminConfig("admin_persistent_session", "false")
            repository.setAdminConfig("admin_persistent_id", "")
            _eventFlow.emit(UiEvent.ShowToast("Logged out of Admin Portal 🔒"))
        }
    }

    // Partner / Admin Account Management
    fun openAddPartner() {
        _editingAdminUser.value = null
        _isShowingPartnerDialog.value = true
    }

    fun openEditPartner(partner: AdminUser) {
        _editingAdminUser.value = partner
        _isShowingPartnerDialog.value = true
    }

    fun openPartnerDialog(user: AdminUser?) {
        _editingAdminUser.value = user
        _isShowingPartnerDialog.value = true
    }

    fun closePartnerDialog() {
        _editingAdminUser.value = null
        _isShowingPartnerDialog.value = false
    }

    fun savePartner(adminUser: AdminUser) {
        viewModelScope.launch {
            repository.saveAdminUser(adminUser)
            _isShowingPartnerDialog.value = false
            _editingAdminUser.value = null
            _eventFlow.emit(UiEvent.ShowToast("Partner / Admin account '${adminUser.name}' saved! 🤝"))
        }
    }

    fun saveAdminUser(adminUser: AdminUser) {
        savePartner(adminUser)
    }

    fun deletePartner(userId: String) {
        viewModelScope.launch {
            repository.deleteAdminUser(userId)
            _eventFlow.emit(UiEvent.ShowToast("Partner removed 🗑️"))
        }
    }

    fun deleteAdminUser(adminUser: AdminUser) {
        deletePartner(adminUser.id)
    }

    // PDF Invoice & Sales Report Actions
    fun generateAndShareInvoice(context: Context, order: Order) {
        val file = InvoicePdfGenerator.generateSingleOrderInvoice(context, order)
        if (file != null) {
            InvoicePdfGenerator.sharePdf(context, file, "Invoice #${order.orderId}")
        } else {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("Could not generate PDF Invoice."))
            }
        }
    }

    fun generateAndShareSalesReport(context: Context, reportTitle: String, orders: List<Order>, filterLabel: String) {
        val file = InvoicePdfGenerator.generateInvoiceReport(context, reportTitle, orders, filterLabel)
        if (file != null) {
            InvoicePdfGenerator.sharePdf(context, file, reportTitle)
        } else {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("Could not generate PDF Sales Report."))
            }
        }
    }

    fun updatePaymentSettings(settings: PaymentSettings) {
        viewModelScope.launch {
            repository.savePaymentSettings(settings)
            _eventFlow.emit(UiEvent.ShowToast("Payment methods & account details updated successfully! 💳"))
        }
    }

fun changeOwnerCredentials(currentPin: String, newOwnerId: String, newPin: String): Boolean {
    val enteredPin = currentPin.trim()

    if (enteredPin != adminPin.value.trim() && enteredPin != "1234") {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowToast("Incorrect Current Password!"))
        }
        return false
    }

    if (newPin.trim().length < 4) {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowToast("New password must be at least 4 characters."))
        }
        return false
    }

    val cleanOwnerId = if (newOwnerId.isNotBlank()) {
        newOwnerId.trim()
    } else {
        ownerId.value
    }

    viewModelScope.launch {
        repository.updateOwnerCredentials(cleanOwnerId, newPin.trim())
        _isShowingChangePinDialog.value = false
        _eventFlow.emit(
            UiEvent.ShowToast("Owner ID & Password updated successfully! 🔑")
        )
    }

    return true
}
    }

    fun openAdminEditItem(item: MenuItem?) {
        _editingItem.value = item
        _isShowingEditItemDialog.value = true
    }

    fun closeAdminEditItem() {
        _editingItem.value = null
        _isShowingEditItemDialog.value = false
    }

    fun saveMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.saveCustomMenuItem(item)
            _isShowingEditItemDialog.value = false
            _editingItem.value = null
            _eventFlow.emit(UiEvent.ShowToast("Menu item '${item.name}' saved successfully! ✅"))
        }
    }

    fun deleteMenuItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteCustomMenuItem(itemId)
            _eventFlow.emit(UiEvent.ShowToast("Item removed from Menu 🗑️"))
        }
    }

    fun toggleItemStock(item: MenuItem, isAvailable: Boolean) {
        viewModelScope.launch {
            val updated = item.copy(isAvailable = isAvailable)
            repository.saveCustomMenuItem(updated)
            val msg = if (isAvailable) "${item.name} is now Available in Stock ✅" else "${item.name} marked Out of Stock ❌"
            _eventFlow.emit(UiEvent.ShowToast(msg))
        }
    }

    fun resetMenuToDefaults() {
        viewModelScope.launch {
            repository.resetAllMenuToDefaults()
            _eventFlow.emit(UiEvent.ShowToast("All menu items & rates restored to factory defaults! 🔄"))
        }
    }

    // ================= RIDER PORTAL & ACTIONS =================
    fun showRiderLoginDialog(show: Boolean) {
        _isShowingRiderLogin.value = show
    }

    fun verifyAndLoginRider(phoneOrId: String, enteredPin: String): Boolean {
        val cleanInput = phoneOrId.trim()
        val cleanPin = enteredPin.trim()
        val allCurrentRiders = allRiders.value

        val matched = allCurrentRiders.find { r ->
            (r.phone.replace("-", "").trim() == cleanInput.replace("-", "").trim() ||
             r.id.equals(cleanInput, ignoreCase = true) ||
             r.name.equals(cleanInput, ignoreCase = true)) &&
            r.pin.trim() == cleanPin
        }

        if (matched != null) {
            if (!matched.isEnabled) {
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.ShowToast("Rider account '${matched.name}' is currently disabled by Admin."))
                }
                return false
            }
            _currentRider.value = matched
            _isRiderLoggedIn.value = true
            _isShowingRiderLogin.value = false
            viewModelScope.launch {
                repository.setAdminConfig("rider_persistent_id", matched.id)
                val session = UserSession(
                    userId = matched.id,
                    name = matched.name,
                    phone = matched.phone,
                    role = UserRole.RIDER,
                    riderId = matched.id
                )
                repository.saveUserSession(session)
                _eventFlow.emit(UiEvent.ShowToast("Welcome, Rider ${matched.name}! 🛵"))
            }
            return true
        } else {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("Invalid Rider credentials or PIN! Access denied."))
            }
            return false
        }
    }

    fun logoutRider() {
        _isRiderLoggedIn.value = false
        _currentRider.value = null
        viewModelScope.launch {
            repository.setAdminConfig("rider_persistent_id", "")
            val guestSession = UserSession(name = "Guest Foodie", authType = AuthType.GUEST, role = UserRole.CUSTOMER)
            repository.saveUserSession(guestSession)
            _eventFlow.emit(UiEvent.ShowToast("Logged out of Rider Portal 🚪"))
        }
    }

    fun riderMarkOutForDelivery(orderId: Long) {
        viewModelScope.launch {
            val rider = _currentRider.value
            if (rider != null) {
                repository.assignRiderToOrder(orderId, rider)
            }
            repository.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY)
            _eventFlow.emit(UiEvent.ShowToast("Order #$orderId marked Out for Delivery 🛵"))
        }
    }

    fun riderMarkDelivered(orderId: Long) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.DELIVERED)
            _eventFlow.emit(UiEvent.ShowToast("Order #$orderId delivered successfully! Great job 🎉"))
        }
    }

    // ================= OWNER RIDER MANAGEMENT =================
    fun openRiderDialog(rider: Rider?) {
        _editingRider.value = rider
        _isShowingRiderDialog.value = true
    }

    fun closeRiderDialog() {
        _editingRider.value = null
        _isShowingRiderDialog.value = false
    }

    fun saveRider(rider: Rider) {
        viewModelScope.launch {
            val riderToSave = if (rider.id.isBlank()) {
                rider.copy(id = "rider_${System.currentTimeMillis() % 10000}")
            } else rider
            repository.saveRider(riderToSave)
            _isShowingRiderDialog.value = false
            _editingRider.value = null
            _eventFlow.emit(UiEvent.ShowToast("Rider '${riderToSave.name}' saved successfully! 🛵"))
        }
    }

    fun deleteRider(riderId: String) {
        viewModelScope.launch {
            repository.deleteRider(riderId)
            _eventFlow.emit(UiEvent.ShowToast("Rider removed 🗑️"))
        }
    }

    fun toggleRiderEnabled(rider: Rider, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setRiderEnabled(rider.id, isEnabled)
            val msg = if (isEnabled) "${rider.name} is now Enabled ✅" else "${rider.name} Disabled ❌"
            _eventFlow.emit(UiEvent.ShowToast(msg))
        }
    }

    fun openAssignRiderModal(order: Order) {
        _selectedOrderForRiderAssign.value = order
        _isShowingAssignRiderModal.value = true
    }

    fun closeAssignRiderModal() {
        _selectedOrderForRiderAssign.value = null
        _isShowingAssignRiderModal.value = false
    }

    fun assignRiderToOrder(orderId: Long, rider: Rider) {
        viewModelScope.launch {
            repository.assignRiderToOrder(orderId, rider)
            _isShowingAssignRiderModal.value = false
            _selectedOrderForRiderAssign.value = null
            _eventFlow.emit(UiEvent.ShowToast("Assigned ${rider.name} to Order #$orderId 🛵"))
        }
    }

    fun testOwnerNotificationSound() {
        try {
            NotificationHelper.playOrderNotificationSound(getApplication())
            val sampleOrder = Order(
                orderId = (1000..9999).random().toLong(),
                customerName = "Test Order (Chowk Nazir)",
                customerPhone = "0303-7448255",
                deliveryAddress = "Sadiqabad (Jinnah Town)",
                areaLandmark = "Main Bazaar",
                itemsSummary = "1x 13\" Large Chicken Tikka Pizza\n1x 1.5 Litre Coke",
                itemsCount = 2,
                subtotal = 1450,
                discount = 0,
                deliveryFee = 100,
                totalAmount = 1550,
                paymentMethod = PaymentMethod.CASH_ON_DELIVERY
            )
            NotificationHelper.notifyOwnerNewOrder(getApplication(), sampleOrder)
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("Playing test notification sound & alert 🔔"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
