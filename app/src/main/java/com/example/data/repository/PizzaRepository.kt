package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AdminConfigEntity
import com.example.data.local.AdminUserEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CustomMenuItemEntity
import com.example.data.local.FeedbackEntity
import com.example.data.local.LoyaltyEntity
import com.example.data.local.OrderEntity
import com.example.data.local.RiderEntity
import com.example.data.local.UserSessionEntity
import com.example.model.AdminRole
import com.example.model.AdminUser
import com.example.model.CustomerFeedback
import com.example.model.LoyaltyProfile
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentSettings
import com.example.model.Rider
import com.example.model.UserSession
import com.example.service.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

data class CloudSyncStatus(
    val isConnected: Boolean = false,
    val lastSyncTime: Long = 0L,
    val cloudOrdersCount: Int = 0,
    val authUid: String? = null,
    val errorMessage: String? = null,
    val isTestingPing: Boolean = false,
    val pingResult: String? = null
)

class PizzaRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val orderDao = database.orderDao()
    private val loyaltyDao = database.loyaltyDao()
    private val feedbackDao = database.feedbackDao()
    private val adminDao = database.adminDao()
    private val customMenuItemDao = database.customMenuItemDao()
    private val userSessionDao = database.userSessionDao()
    private val riderDao = database.riderDao()
    private val adminUserDao = database.adminUserDao()

    private var firestore: FirebaseFirestore? = null
    private var firestoreOrdersListener: ListenerRegistration? = null
    private var firestoreRidersListener: ListenerRegistration? = null
    private var firestoreFeedbackListener: ListenerRegistration? = null
    private var firestoreAdminUsersListener: ListenerRegistration? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _cloudSyncStatus = MutableStateFlow(CloudSyncStatus())
    val cloudSyncStatus: StateFlow<CloudSyncStatus> = _cloudSyncStatus.asStateFlow()

    // Keep track of order IDs that have already triggered a sound/notification on this device
    private val appLaunchTime = System.currentTimeMillis()
    private val notifiedOrderIds = Collections.synchronizedSet(HashSet<Long>())
    @Volatile
    private var isAdminActive: Boolean = false

    fun setAdminActive(active: Boolean) {
        isAdminActive = active
    }

    private fun getDb(): FirebaseFirestore {
        return firestore ?: com.example.service.FirebaseInitHelper.getFirestore(context).also { firestore = it }
    }

    init {
        initFirebaseAndListeners()
    }

    private fun initFirebaseAndListeners() {
        try {
            val app = com.example.service.FirebaseInitHelper.getOrInitFirebaseApp(context)
            firestore = com.example.service.FirebaseInitHelper.getFirestore(context)
            try {
                firestore?.enableNetwork()
            } catch (e: Exception) {
                Log.d("PizzaRepository", "Enable network note: ${e.message}")
            }

            // Auto-sign-in anonymously on startup so requests pass Firebase Security Rules if rules require request.auth != null
            try {
                val auth = com.example.service.FirebaseInitHelper.getAuth(context)
                if (auth.currentUser == null) {
                    auth.signInAnonymously()
                        .addOnSuccessListener { result ->
                            Log.d("PizzaRepository", "Firebase Anonymous Auth Connected. UID: ${result.user?.uid}")
                            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                                isConnected = true,
                                authUid = result.user?.uid,
                                errorMessage = null
                            )
                        }
                        .addOnFailureListener { err ->
                            Log.w("PizzaRepository", "Firebase Anonymous Auth notice: ${err.message}")
                            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                                isConnected = false,
                                errorMessage = "Auth Notice: ${err.message}"
                            )
                        }
                } else {
                    _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                        isConnected = true,
                        authUid = auth.currentUser?.uid
                    )
                }
            } catch (e: Exception) {
                Log.w("PizzaRepository", "Firebase Auth init notice: ${e.message}")
            }

            listenToFirestoreOrders()
            listenToFirestoreRiders()
            listenToFirestoreFeedback()
            listenToFirestoreAdminUsers()
            listenToFirestorePaymentSettings()
            // Initial fetch to load existing cloud orders and admin credentials immediately
            repositoryScope.launch {
                refreshOrdersFromCloud()
                syncAdminCredentialsFromCloud()
                syncAdminUsersFromCloud()
                syncPaymentSettingsFromCloud()
            }
            Log.d("PizzaRepository", "Firestore listeners and initial fetch initialized successfully.")
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Firestore not available or in local-first fallback mode: ${e.message}")
            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                isConnected = false,
                errorMessage = "Firestore Init Error: ${e.message}"
            )
        }
    }

    // ================= REAL-TIME FIRESTORE ORDER SYNC =================
    private fun listenToFirestoreOrders() {
        try {
            val db = getDb()
            firestoreOrdersListener?.remove()
            // Listen to orders collection directly without restrictive orderBy to prevent index errors
            firestoreOrdersListener = db.collection("orders")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("PizzaRepository", "Firestore orders listen error", error)
                        _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                            isConnected = false,
                            errorMessage = "Firestore Sync Error: ${error.localizedMessage ?: error.message}"
                        )
                        return@addSnapshotListener
                    }
                    if (snapshots == null) return@addSnapshotListener

                    _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                        isConnected = true,
                        lastSyncTime = System.currentTimeMillis(),
                        cloudOrdersCount = snapshots.size(),
                        errorMessage = null
                    )

                    repositoryScope.launch {
                        try {
                            // 1. Process all documents to ensure Room has all cloud orders synced
                            val allOrders = snapshots.documents.mapNotNull { parseOrderFromDoc(it) }
                            if (allOrders.isNotEmpty()) {
                                val orderEntities = allOrders.map { OrderEntity.fromDomain(it) }
                                orderDao.insertOrders(orderEntities)
                            }

                            // 2. Process changes for sound/notifications
                            for (change in snapshots.documentChanges) {
                                val doc = change.document
                                val order = parseOrderFromDoc(doc) ?: continue

                                when (change.type) {
                                    DocumentChange.Type.ADDED -> {
                                        // Live new order received from customer
                                        if (!notifiedOrderIds.contains(order.orderId)) {
                                            notifiedOrderIds.add(order.orderId)
                                            if (order.status == OrderStatus.ORDER_RECEIVED &&
                                                order.timestamp > (appLaunchTime - 600000) // Within last 10 mins
                                            ) {
                                                try {
                                                    Log.d("PizzaRepository", "NEW ORDER ARRIVED FOR OWNER -> Triggering Owner Notification & Sound for #${order.orderId}")
                                                    NotificationHelper.notifyOwnerNewOrder(context, order)
                                                } catch (e: Exception) {
                                                    Log.e("PizzaRepository", "Failed to trigger owner notification", e)
                                                }
                                            }
                                        }
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        // Status update received (e.g. RECEIVED -> PREPARING -> READY -> OUT_FOR_DELIVERY -> DELIVERED)
                                        try {
                                            val currentSession = userSessionDao.getUserSession()
                                            val isCustomerOrder = currentSession != null && (
                                                currentSession.userId == order.userId ||
                                                (currentSession.phone.isNotBlank() && currentSession.phone.trim() == order.customerPhone.trim())
                                            )
                                            if (isCustomerOrder) {
                                                val statusTitle = when (order.status) {
                                                    OrderStatus.ORDER_RECEIVED -> "Order Confirmed 📥"
                                                    OrderStatus.PREPARING_PIZZA -> "Baking in Oven 🧑‍🍳"
                                                    OrderStatus.READY_FOR_PICKUP -> "Packed & Ready 🍕"
                                                    OrderStatus.OUT_FOR_DELIVERY -> "Out for Delivery 🛵"
                                                    OrderStatus.DELIVERED -> "Delivered! Enjoy your meal 🎉"
                                                    OrderStatus.CANCELLED -> "Order Cancelled ❌"
                                                }
                                                val message = "Order #${order.orderId} is now: $statusTitle"
                                                NotificationHelper.notifyOrderStatusUpdate(context, order.orderId, statusTitle, message)
                                            }
                                        } catch (e: Exception) {
                                            Log.e("PizzaRepository", "Error on order modified notification", e)
                                        }
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        orderDao.deleteOrder(order.orderId)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PizzaRepository", "Error processing Firestore orders snapshot", e)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Error starting Firestore orders listener", e)
        }
    }

    suspend fun refreshOrdersFromCloud(): List<Order> = withContext(Dispatchers.IO) {
        try {
            val db = getDb()
            val snapshot = com.google.android.gms.tasks.Tasks.await(db.collection("orders").get())
            val orders = snapshot.documents.mapNotNull { parseOrderFromDoc(it) }
            if (orders.isNotEmpty()) {
                val entities = orders.map { OrderEntity.fromDomain(it) }
                orderDao.insertOrders(entities)
            }
            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                isConnected = true,
                lastSyncTime = System.currentTimeMillis(),
                cloudOrdersCount = orders.size,
                errorMessage = null
            )
            Log.d("PizzaRepository", "Cloud orders refreshed: ${orders.size} orders synced.")
            orders
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Failed to refresh orders from Firestore", e)
            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                isConnected = false,
                errorMessage = "Cloud Refresh Error: ${e.localizedMessage ?: e.message}"
            )
            emptyList()
        }
    }

    suspend fun testCloudConnection(): String = withContext(Dispatchers.IO) {
        try {
            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(isTestingPing = true)
            val db = getDb()

            // 1. Try pinging cloud diagnostics document
            val pingDoc = mapOf(
                "testTime" to System.currentTimeMillis(),
                "deviceDate" to SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date()),
                "status" to "ONLINE_ACTIVE",
                "appId" to "com.aistudio.slicesmile.pkpizza"
            )
            com.google.android.gms.tasks.Tasks.await(
                db.collection("cloud_diagnostics").document("ping_test").set(pingDoc)
            )

            // 2. Query cloud orders
            val snapshot = com.google.android.gms.tasks.Tasks.await(db.collection("orders").get())
            val count = snapshot.size()

            val successMsg = "✅ Cloud Link Successful! Connected to Firebase project (slice-smile-pizza-shop-2026). Total $count cloud orders synced. Real-time multi-device sync is Active!"
            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                isConnected = true,
                lastSyncTime = System.currentTimeMillis(),
                cloudOrdersCount = count,
                errorMessage = null,
                isTestingPing = false,
                pingResult = successMsg
            )
            successMsg
        } catch (e: Exception) {
            val errorMsg = "❌ Cloud Error: ${e.localizedMessage ?: e.message}.\n\nFirebase Console میں Firestore Rules چیک کریں۔ اگر PERMISSION_DENIED کا مسئلہ ہے تو Firebase Console -> Firestore Database -> Rules میں یہ لکھیں:\nallow read, write: if true;"
            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                isConnected = false,
                errorMessage = errorMsg,
                isTestingPing = false,
                pingResult = errorMsg
            )
            errorMsg
        }
    }

    private fun parseOrderFromDoc(doc: DocumentSnapshot): Order? {
        return try {
            val orderId = when (val v = doc.get("orderId")) {
                is Number -> v.toLong()
                is String -> v.toLongOrNull()
                else -> null
            } ?: doc.id.toLongOrNull() ?: return null

            val userId = doc.getString("userId") ?: doc.get("userId")?.toString() ?: "guest_user"
            val itemsSummary = doc.getString("itemsSummary") ?: doc.get("itemsSummary")?.toString() ?: ""
            
            val itemsCount = when (val v = doc.get("itemsCount")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 1
                else -> 1
            }
            val subtotal = when (val v = doc.get("subtotal")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> 0
            }
            val discount = when (val v = doc.get("discount")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> 0
            }
            val deliveryFee = when (val v = doc.get("deliveryFee")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> 0
            }
            val totalAmount = when (val v = doc.get("totalAmount")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> (subtotal - discount + deliveryFee).coerceAtLeast(0)
            }
            val paymentMethodName = doc.getString("paymentMethodName") ?: doc.get("paymentMethodName")?.toString() ?: "CASH_ON_DELIVERY"
            val paymentMethod = try {
                PaymentMethod.valueOf(paymentMethodName)
            } catch (e: Exception) {
                PaymentMethod.CASH_ON_DELIVERY
            }
            val easypaisaTrxId = doc.getString("easypaisaTrxId") ?: doc.get("easypaisaTrxId")?.toString()
            val customerName = doc.getString("customerName") ?: doc.get("customerName")?.toString() ?: "Customer"
            val customerPhone = doc.getString("customerPhone") ?: doc.get("customerPhone")?.toString() ?: ""
            val deliveryAddress = doc.getString("deliveryAddress") ?: doc.get("deliveryAddress")?.toString() ?: ""
            val areaLandmark = doc.getString("areaLandmark") ?: doc.get("areaLandmark")?.toString() ?: ""
            val orderNote = doc.getString("orderNote") ?: doc.get("orderNote")?.toString() ?: ""
            
            val coinsEarned = when (val v = doc.get("coinsEarned")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> 0
            }
            val coinsRedeemed = when (val v = doc.get("coinsRedeemed")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> 0
            }
            val statusName = doc.getString("statusName") ?: doc.get("statusName")?.toString() ?: OrderStatus.ORDER_RECEIVED.name
            val status = when (statusName) {
                "PLACED", "ORDER_RECEIVED" -> OrderStatus.ORDER_RECEIVED
                "PREPARING", "PREPARING_PIZZA" -> OrderStatus.PREPARING_PIZZA
                "READY", "READY_FOR_PICKUP" -> OrderStatus.READY_FOR_PICKUP
                "OUT_FOR_DELIVERY" -> OrderStatus.OUT_FOR_DELIVERY
                "DELIVERED" -> OrderStatus.DELIVERED
                "CANCELLED" -> OrderStatus.CANCELLED
                else -> try {
                    OrderStatus.valueOf(statusName)
                } catch (e: Exception) {
                    OrderStatus.ORDER_RECEIVED
                }
            }
            val timestamp = when (val v = doc.get("timestamp")) {
                is Number -> v.toLong()
                is com.google.firebase.Timestamp -> v.toDate().time
                is String -> v.toLongOrNull() ?: System.currentTimeMillis()
                else -> System.currentTimeMillis()
            }
            val riderId = doc.getString("riderId") ?: doc.get("riderId")?.toString()
            val riderName = doc.getString("riderName") ?: doc.get("riderName")?.toString() ?: "Slice Smile Express Delivery"
            val riderPhone = doc.getString("riderPhone") ?: doc.get("riderPhone")?.toString() ?: "0303-7448255"
            val riderVehicle = doc.getString("riderVehicle") ?: doc.get("riderVehicle")?.toString() ?: "Motorcycle"
            val rating = when (val v = doc.get("rating")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> 0
            }
            val reviewComment = doc.getString("reviewComment") ?: doc.get("reviewComment")?.toString() ?: ""
            val feedbackSubmitted = when (val v = doc.get("feedbackSubmitted")) {
                is Boolean -> v
                is String -> v.toBoolean()
                else -> false
            }

            Order(
                orderId = orderId,
                userId = userId,
                itemsSummary = itemsSummary,
                itemsCount = itemsCount,
                subtotal = subtotal,
                discount = discount,
                deliveryFee = deliveryFee,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                easypaisaTrxId = easypaisaTrxId,
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress,
                areaLandmark = areaLandmark,
                orderNote = orderNote,
                coinsEarned = coinsEarned,
                coinsRedeemed = coinsRedeemed,
                status = status,
                timestamp = timestamp,
                riderId = riderId,
                riderName = riderName,
                riderPhone = riderPhone,
                riderVehicle = riderVehicle,
                rating = rating,
                reviewComment = reviewComment,
                feedbackSubmitted = feedbackSubmitted
            )
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Error parsing Firestore order doc ${doc.id}", e)
            null
        }
    }

    // ================= REAL-TIME FIRESTORE RIDERS SYNC =================
    private fun listenToFirestoreRiders() {
        try {
            val db = getDb()
            firestoreRidersListener?.remove()
            firestoreRidersListener = db.collection("riders").addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener
                repositoryScope.launch {
                    val riderEntities = snapshots.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: ""
                            val phone = doc.getString("phone") ?: ""
                            val pin = doc.getString("pin") ?: "1234"
                            val vehicle = doc.getString("vehicle") ?: "Motorcycle"
                            val isEnabled = doc.getBoolean("isEnabled") ?: true
                            val rating = doc.getDouble("rating") ?: 5.0
                            val totalDeliveries = doc.getLong("totalDeliveries")?.toInt() ?: 0
                            val canAcceptOrder = doc.getBoolean("canAcceptOrder") ?: true
                            val canPickOrder = doc.getBoolean("canPickOrder") ?: true
                            val canMarkDelivered = doc.getBoolean("canMarkDelivered") ?: true
                            val canCallCustomer = doc.getBoolean("canCallCustomer") ?: true
                            val canViewDirections = doc.getBoolean("canViewDirections") ?: true
                            RiderEntity(
                                id = id,
                                name = name,
                                phone = phone,
                                pin = pin,
                                vehicle = vehicle,
                                isEnabled = isEnabled,
                                rating = rating,
                                totalDeliveries = totalDeliveries,
                                canAcceptOrder = canAcceptOrder,
                                canPickOrder = canPickOrder,
                                canMarkDelivered = canMarkDelivered,
                                canCallCustomer = canCallCustomer,
                                canViewDirections = canViewDirections
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (riderEntities.isNotEmpty()) {
                        for (r in riderEntities) {
                            riderDao.insertOrUpdate(r)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Firestore riders listener error: ${e.message}")
        }
    }

    // ================= REAL-TIME FIRESTORE FEEDBACK SYNC =================
    private fun listenToFirestoreFeedback() {
        try {
            val db = getDb()
            firestoreFeedbackListener?.remove()
            firestoreFeedbackListener = db.collection("customer_feedback").addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener
                repositoryScope.launch {
                    val feedbacks = snapshots.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                            val orderId = doc.getLong("orderId") ?: 0L
                            val customerName = doc.getString("customerName") ?: "Customer"
                            val overallRating = doc.getLong("overallRating")?.toInt() ?: 5
                            val foodTasteRating = doc.getLong("foodTasteRating")?.toInt() ?: 5
                            val deliverySpeedRating = doc.getLong("deliverySpeedRating")?.toInt() ?: 5
                            val comment = doc.getString("comment") ?: ""
                            val photoUri = doc.getString("photoUri")
                            val photoUrl = doc.getString("photoUrl")
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            FeedbackEntity(
                                id = id,
                                orderId = orderId,
                                customerName = customerName,
                                overallRating = overallRating,
                                foodTasteRating = foodTasteRating,
                                deliverySpeedRating = deliverySpeedRating,
                                comment = comment,
                                photoUri = photoUri,
                                photoUrl = photoUrl,
                                timestamp = timestamp
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (feedbacks.isNotEmpty()) {
                        for (f in feedbacks) {
                            feedbackDao.insertFeedback(f)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Firestore feedback listener error: ${e.message}")
        }
    }

    // ================= REAL-TIME FIRESTORE ADMIN USERS / PARTNERS SYNC =================
    private fun listenToFirestoreAdminUsers() {
        try {
            val db = getDb()
            firestoreAdminUsersListener?.remove()
            firestoreAdminUsersListener = db.collection("admin_users").addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener
                repositoryScope.launch {
                    val users = snapshots.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val username = doc.getString("username") ?: id
                            val name = doc.getString("name") ?: username
                            val phone = doc.getString("phone") ?: ""
                            val pin = doc.getString("pin") ?: "1234"
                            val roleName = doc.getString("roleName") ?: "PARTNER"
                            val isActive = doc.getBoolean("isActive") ?: true
                            val canManageMenu = doc.getBoolean("canManageMenu") ?: true
                            val canManageOrders = doc.getBoolean("canManageOrders") ?: true
                            val canViewReports = doc.getBoolean("canViewReports") ?: true
                            val canManageRiders = doc.getBoolean("canManageRiders") ?: true
                            val canManagePartners = doc.getBoolean("canManagePartners") ?: false
                            val canManagePayments = doc.getBoolean("canManagePayments") ?: false
                            val canManageDeals = doc.getBoolean("canManageDeals") ?: true
                            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                            AdminUserEntity(
                                id = id,
                                username = username,
                                name = name,
                                phone = phone,
                                pin = pin,
                                roleName = roleName,
                                isActive = isActive,
                                canManageMenu = canManageMenu,
                                canManageOrders = canManageOrders,
                                canViewReports = canViewReports,
                                canManageRiders = canManageRiders,
                                canManagePartners = canManagePartners,
                                canManagePayments = canManagePayments,
                                canManageDeals = canManageDeals,
                                createdAt = createdAt
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (users.isNotEmpty()) {
                        for (u in users) {
                            adminUserDao.insertOrUpdate(u)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Firestore admin users listener error: ${e.message}")
        }
    }

    private fun listenToFirestorePaymentSettings() {
        try {
            val db = getDb()
            db.collection("admin_config").document("payment_settings")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("PizzaRepository", "Firestore payment settings listener notice: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        repositoryScope.launch(Dispatchers.IO) {
                            snapshot.getBoolean("isCodEnabled")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_cod_enabled", it.toString()))
                            }
                            snapshot.getBoolean("isEasypaisaEnabled")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_easypaisa_enabled", it.toString()))
                            }
                            snapshot.getString("easypaisaNumber")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_easypaisa_number", it))
                            }
                            snapshot.getString("easypaisaTitle")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_easypaisa_title", it))
                            }
                            snapshot.getBoolean("isJazzcashEnabled")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_jazzcash_enabled", it.toString()))
                            }
                            snapshot.getString("jazzcashNumber")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_jazzcash_number", it))
                            }
                            snapshot.getString("jazzcashTitle")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_jazzcash_title", it))
                            }
                            snapshot.getBoolean("isBankTransferEnabled")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_bank_enabled", it.toString()))
                            }
                            snapshot.getString("bankName")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_bank_name", it))
                            }
                            snapshot.getString("bankAccountTitle")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_bank_title", it))
                            }
                            snapshot.getString("bankIban")?.let {
                                adminDao.setConfig(AdminConfigEntity("pay_bank_iban", it))
                            }
                            Log.d("PizzaRepository", "Payment settings updated from Cloud Firestore")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Payment settings listener setup error: ${e.message}")
        }
    }

    // ================= ORDER QUERIES =================
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders().map { list ->
        list.map { it.toDomain() }
    }

    fun getOrdersForUser(userId: String): Flow<List<Order>> {
        return orderDao.getOrdersByUserId(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getOrdersForRider(riderId: String): Flow<List<Order>> {
        return orderDao.getOrdersByRiderId(riderId).map { list ->
            list.map { it.toDomain() }
        }
    }

    val allRiders: Flow<List<Rider>> = riderDao.getAllRidersFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getAllRidersOnce(): List<Rider> = withContext(Dispatchers.IO) {
        riderDao.getAllRiders().map { it.toDomain() }
    }

    val userSessionFlow: Flow<UserSession> = userSessionDao.getUserSessionFlow().map { entity ->
        entity?.toDomain() ?: UserSession()
    }

    val loyaltyProfile: Flow<LoyaltyProfile> = loyaltyDao.getLoyaltyProfileFlow().map { entity ->
        entity?.toDomain() ?: LoyaltyProfile()
    }

    val allFeedback: Flow<List<CustomerFeedback>> = feedbackDao.getAllFeedback().map { list ->
        list.map { it.toDomain() }
    }

    val allAdminUsersFlow: Flow<List<AdminUser>> = adminUserDao.getAllAdminUsersFlow().map { list ->
        list.map { it.toDomain() }
    }

    val adminPinFlow: Flow<String> = adminUserDao.getAllAdminUsersFlow().map { list ->
    list.firstOrNull { it.role == com.example.model.AdminRole.SUPER_ADMIN && it.isActive }?.pin ?: "1234"
}

val ownerIdFlow: Flow<String> = adminUserDao.getAllAdminUsersFlow().map { list ->
    list.firstOrNull { it.role == com.example.model.AdminRole.SUPER_ADMIN && it.isActive }?.username ?: "admin"
}

    suspend fun getAllAdminUsersOnce(): List<AdminUser> = withContext(Dispatchers.IO) {
        adminUserDao.getAllAdminUsers().map { it.toDomain() }
    }

    suspend fun saveAdminUser(user: AdminUser) = withContext(Dispatchers.IO) {
        adminUserDao.insertOrUpdate(AdminUserEntity.fromDomain(user))
        try {
            val db = getDb()
            val map = hashMapOf(
                "id" to user.id,
                "username" to user.username,
                "name" to user.name,
                "phone" to user.phone,
                "pin" to user.pin,
                "roleName" to user.role.name,
                "isActive" to user.isActive,
                "canManageMenu" to user.canManageMenu,
                "canManageOrders" to user.canManageOrders,
                "canViewReports" to user.canViewReports,
                "canManageRiders" to user.canManageRiders,
                "canManagePartners" to user.canManagePartners,
                "canManagePayments" to user.canManagePayments,
                "canManageDeals" to user.canManageDeals,
                "createdAt" to user.createdAt
            )
            db.collection("admin_users").document(user.id).set(map)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore saveAdminUser error", e)
        }
    }

    suspend fun deleteAdminUser(userId: String) = withContext(Dispatchers.IO) {
        adminUserDao.deleteUser(userId)
        try {
            val db = getDb()
            db.collection("admin_users").document(userId).delete()
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore deleteAdminUser error", e)
        }
    }

    suspend fun authenticateAdmin(usernameOrPhone: String, pin: String): AdminUser? = withContext(Dispatchers.IO) {
        val cleanInput = usernameOrPhone.trim()
        val cleanPin = pin.trim()

        // 1. Check in Room admin_users
        val localUsers = adminUserDao.getAllAdminUsers()
        val matchedUser = localUsers.firstOrNull { 
            (it.username.equals(cleanInput, ignoreCase = true) || it.phone == cleanInput || it.id.equals(cleanInput, ignoreCase = true)) && it.pin == cleanPin && it.isActive 
        }
        if (matchedUser != null) {
            return@withContext matchedUser.toDomain()
        }

        // 2. Check legacy admin_config
        val savedOwnerId = adminDao.getConfig("owner_id") ?: "admin"
        val savedPin = adminDao.getConfig("admin_pin") ?: "1234"
        if ((cleanInput.equals(savedOwnerId, ignoreCase = true) || cleanInput.equals("admin", ignoreCase = true)) && cleanPin == savedPin) {
            val defaultSuper = AdminUser(
                id = "admin_owner",
                username = savedOwnerId,
                name = "Main Admin / Owner",
                pin = savedPin,
                role = com.example.model.AdminRole.SUPER_ADMIN
            )
            adminUserDao.insertOrUpdate(AdminUserEntity.fromDomain(defaultSuper))
            return@withContext defaultSuper
        }

        // 3. Fallback check from Firestore if freshly installed
        try {
            val db = getDb()
            val snapshot = com.google.android.gms.tasks.Tasks.await(db.collection("admin_users").get())
            for (doc in snapshot.documents) {
                val uname = doc.getString("username") ?: ""
                val pNumber = doc.getString("phone") ?: ""
                val docPin = doc.getString("pin") ?: ""
                val active = doc.getBoolean("isActive") ?: true
                if ((uname.equals(cleanInput, ignoreCase = true) || pNumber == cleanInput) && docPin == cleanPin && active) {
                    val roleStr = doc.getString("roleName") ?: "PARTNER"
                    val roleEnum = try { com.example.model.AdminRole.valueOf(roleStr) } catch(e:Exception) { com.example.model.AdminRole.PARTNER }
                    val user = AdminUser(
                        id = doc.getString("id") ?: doc.id,
                        username = uname,
                        name = doc.getString("name") ?: uname,
                        phone = pNumber,
                        pin = docPin,
                        role = roleEnum,
                        isActive = active,
                        canManageMenu = doc.getBoolean("canManageMenu") ?: true,
                        canManageOrders = doc.getBoolean("canManageOrders") ?: true,
                        canViewReports = doc.getBoolean("canViewReports") ?: true,
                        canManageRiders = doc.getBoolean("canManageRiders") ?: true,
                        canManagePartners = doc.getBoolean("canManagePartners") ?: false,
                        canManagePayments = doc.getBoolean("canManagePayments") ?: false,
                        canManageDeals = doc.getBoolean("canManageDeals") ?: true
                    )
                    adminUserDao.insertOrUpdate(AdminUserEntity.fromDomain(user))
                    return@withContext user
                }
            }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Cloud admin auth check: ${e.message}")
        }

        null
    }

    suspend fun syncAdminUsersFromCloud() = withContext(Dispatchers.IO) {
        try {
            val db = getDb()
            val snapshot = com.google.android.gms.tasks.Tasks.await(db.collection("admin_users").get())
            val users = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val username = doc.getString("username") ?: id
                    val name = doc.getString("name") ?: username
                    val phone = doc.getString("phone") ?: ""
                    val pin = doc.getString("pin") ?: "1234"
                    val roleName = doc.getString("roleName") ?: "PARTNER"
                    val isActive = doc.getBoolean("isActive") ?: true
                    AdminUserEntity(
                        id = id,
                        username = username,
                        name = name,
                        phone = phone,
                        pin = pin,
                        roleName = roleName,
                        isActive = isActive,
                        canManageMenu = doc.getBoolean("canManageMenu") ?: true,
                        canManageOrders = doc.getBoolean("canManageOrders") ?: true,
                        canViewReports = doc.getBoolean("canViewReports") ?: true,
                        canManageRiders = doc.getBoolean("canManageRiders") ?: true,
                        canManagePartners = doc.getBoolean("canManagePartners") ?: false,
                        canManagePayments = doc.getBoolean("canManagePayments") ?: false,
                        canManageDeals = doc.getBoolean("canManageDeals") ?: true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (users.isNotEmpty()) {
                adminUserDao.insertAll(users)
            }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Notice: syncAdminUsersFromCloud ${e.message}")
        }
    }

    suspend fun applyReferralCode(code: String): Boolean = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()
        val profile = loyaltyDao.getLoyaltyProfile() ?: LoyaltyEntity()
        if (cleanCode.isNotBlank() && cleanCode != profile.referralCode) {
            loyaltyDao.insertOrUpdateProfile(
                profile.copy(
                    hasPendingReferralDiscount = true,
                    currentCoins = profile.currentCoins + 100 // Bonus 100 Slice coins + 10% welcome discount!
                )
            )
            // Also notify and credit the inviter on Cloud Firestore
            try {
                val db = getDb()
                val referralLog = hashMapOf(
                    "referredByCode" to cleanCode,
                    "joinedTimestamp" to System.currentTimeMillis(),
                    "status" to "CODE_APPLIED"
                )
                db.collection("referral_logs").add(referralLog)
            } catch (e: Exception) {
                Log.d("PizzaRepository", "Referral cloud log notice: ${e.message}")
            }
            true
        } else {
            false
        }
    }

    suspend fun rewardReferrerForOrder(referralCode: String) = withContext(Dispatchers.IO) {
        val cleanCode = referralCode.trim().uppercase()
        if (cleanCode.isBlank()) return@withContext

        try {
            val db = getDb()
            // 1. Log referral completion to Firestore
            val referralDoc = hashMapOf(
                "referralCode" to cleanCode,
                "rewardGranted" to "10%_DISCOUNT_NEXT_ORDER",
                "bonusCoins" to 200,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("successful_referrals").add(referralDoc)

            // 2. Check if local profile is the owner of this referral code
            val currentProfile = loyaltyDao.getLoyaltyProfile() ?: LoyaltyEntity()
            if (currentProfile.referralCode.equals(cleanCode, ignoreCase = true)) {
                loyaltyDao.insertOrUpdateProfile(
                    currentProfile.copy(
                        successfulReferralsCount = currentProfile.successfulReferralsCount + 1,
                        availableReferralDiscountsCount = currentProfile.availableReferralDiscountsCount + 1,
                        totalReferralDiscountsEarned = currentProfile.totalReferralDiscountsEarned + 1,
                        currentCoins = currentProfile.currentCoins + 200,
                        totalCoinsEarnedLifetime = currentProfile.totalCoinsEarnedLifetime + 200
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Error rewarding referral inviter: ${e.message}")
        }
    }

    suspend fun updateCustomReferralCode(newCode: String) = withContext(Dispatchers.IO) {
        val clean = newCode.trim().uppercase()
        if (clean.isNotBlank()) {
            val current = loyaltyDao.getLoyaltyProfile() ?: LoyaltyEntity()
            loyaltyDao.insertOrUpdateProfile(current.copy(referralCode = clean))
        }
    }

    val customMenuItemsFlow: Flow<List<CustomMenuItemEntity>> = customMenuItemDao.getAllCustomMenuItemsFlow()

    val paymentSettingsFlow: Flow<PaymentSettings> = adminDao.getAllConfigsFlow().map { configs ->
        val configMap = configs.associate { it.key to it.value }
        PaymentSettings(
            isCodEnabled = configMap["pay_cod_enabled"]?.toBooleanStrictOrNull() ?: true,
            isEasypaisaEnabled = configMap["pay_easypaisa_enabled"]?.toBooleanStrictOrNull() ?: true,
            easypaisaNumber = configMap["pay_easypaisa_number"] ?: "03254946190",
            easypaisaTitle = configMap["pay_easypaisa_title"] ?: "Slice Smile Pizza / Tariq Mahmood",
            isJazzcashEnabled = configMap["pay_jazzcash_enabled"]?.toBooleanStrictOrNull() ?: true,
            jazzcashNumber = configMap["pay_jazzcash_number"] ?: "03037448255",
            jazzcashTitle = configMap["pay_jazzcash_title"] ?: "Slice Smile Pizza",
            isBankTransferEnabled = configMap["pay_bank_enabled"]?.toBooleanStrictOrNull() ?: true,
            bankName = configMap["pay_bank_name"] ?: "Meezan Bank Ltd",
            bankAccountTitle = configMap["pay_bank_title"] ?: "Slice Smile Pizza",
            bankIban = configMap["pay_bank_iban"] ?: "PK36MEZN0001234567890101"
        )
    }

    suspend fun savePaymentSettings(settings: PaymentSettings) = withContext(Dispatchers.IO) {
        adminDao.setConfig(AdminConfigEntity("pay_cod_enabled", settings.isCodEnabled.toString()))
        adminDao.setConfig(AdminConfigEntity("pay_easypaisa_enabled", settings.isEasypaisaEnabled.toString()))
        adminDao.setConfig(AdminConfigEntity("pay_easypaisa_number", settings.easypaisaNumber))
        adminDao.setConfig(AdminConfigEntity("pay_easypaisa_title", settings.easypaisaTitle))
        adminDao.setConfig(AdminConfigEntity("pay_jazzcash_enabled", settings.isJazzcashEnabled.toString()))
        adminDao.setConfig(AdminConfigEntity("pay_jazzcash_number", settings.jazzcashNumber))
        adminDao.setConfig(AdminConfigEntity("pay_jazzcash_title", settings.jazzcashTitle))
        adminDao.setConfig(AdminConfigEntity("pay_bank_enabled", settings.isBankTransferEnabled.toString()))
        adminDao.setConfig(AdminConfigEntity("pay_bank_name", settings.bankName))
        adminDao.setConfig(AdminConfigEntity("pay_bank_title", settings.bankAccountTitle))
        adminDao.setConfig(AdminConfigEntity("pay_bank_iban", settings.bankIban))

        try {
            val db = getDb()
            val map = hashMapOf(
                "isCodEnabled" to settings.isCodEnabled,
                "isEasypaisaEnabled" to settings.isEasypaisaEnabled,
                "easypaisaNumber" to settings.easypaisaNumber,
                "easypaisaTitle" to settings.easypaisaTitle,
                "isJazzcashEnabled" to settings.isJazzcashEnabled,
                "jazzcashNumber" to settings.jazzcashNumber,
                "jazzcashTitle" to settings.jazzcashTitle,
                "isBankTransferEnabled" to settings.isBankTransferEnabled,
                "bankName" to settings.bankName,
                "bankAccountTitle" to settings.bankAccountTitle,
                "bankIban" to settings.bankIban,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("admin_config").document("payment_settings").set(map)
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Payment settings cloud sync notice: ${e.message}")
        }
    }

    suspend fun syncPaymentSettingsFromCloud() = withContext(Dispatchers.IO) {
        try {
            val db = getDb()
            val doc = com.google.android.gms.tasks.Tasks.await(db.collection("admin_config").document("payment_settings").get())
            if (doc != null && doc.exists()) {
                doc.getBoolean("isCodEnabled")?.let { adminDao.setConfig(AdminConfigEntity("pay_cod_enabled", it.toString())) }
                doc.getBoolean("isEasypaisaEnabled")?.let { adminDao.setConfig(AdminConfigEntity("pay_easypaisa_enabled", it.toString())) }
                doc.getString("easypaisaNumber")?.let { adminDao.setConfig(AdminConfigEntity("pay_easypaisa_number", it)) }
                doc.getString("easypaisaTitle")?.let { adminDao.setConfig(AdminConfigEntity("pay_easypaisa_title", it)) }
                doc.getBoolean("isJazzcashEnabled")?.let { adminDao.setConfig(AdminConfigEntity("pay_jazzcash_enabled", it.toString())) }
                doc.getString("jazzcashNumber")?.let { adminDao.setConfig(AdminConfigEntity("pay_jazzcash_number", it)) }
                doc.getString("jazzcashTitle")?.let { adminDao.setConfig(AdminConfigEntity("pay_jazzcash_title", it)) }
                doc.getBoolean("isBankTransferEnabled")?.let { adminDao.setConfig(AdminConfigEntity("pay_bank_enabled", it.toString())) }
                doc.getString("bankName")?.let { adminDao.setConfig(AdminConfigEntity("pay_bank_name", it)) }
                doc.getString("bankAccountTitle")?.let { adminDao.setConfig(AdminConfigEntity("pay_bank_title", it)) }
                doc.getString("bankIban")?.let { adminDao.setConfig(AdminConfigEntity("pay_bank_iban", it)) }
                Log.d("PizzaRepository", "Payment settings synced from Cloud Firestore on startup")
            }
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Notice: Payment settings cloud sync: ${e.message}")
        }
    }

    suspend fun saveUserSession(session: UserSession) {
        userSessionDao.saveUserSession(UserSessionEntity.fromDomain(session))
    }

    suspend fun clearUserSession() {
        userSessionDao.clearSession()
    }

    suspend fun getAdminPin(): String {
        return adminDao.getConfig("admin_pin") ?: "Hamza9181@"
    }

    suspend fun getOwnerId(): String {
        return adminDao.getConfig("owner_id") ?: "Owner@slicesmile.com"
    }

    suspend fun getAdminConfig(key: String): String? {
        return adminDao.getConfig(key)
    }

    suspend fun setAdminConfig(key: String, value: String) {
        adminDao.setConfig(AdminConfigEntity(key = key, value = value))
    }

    suspend fun syncAdminCredentialsFromCloud() = withContext(Dispatchers.IO) {
        try {
            val db = getDb()
            val doc = com.google.android.gms.tasks.Tasks.await(db.collection("admin_config").document("credentials").get())
            if (doc != null && doc.exists()) {
                val cloudOwnerId = doc.getString("ownerId")
                val cloudPassword = doc.getString("password")
                if (!cloudOwnerId.isNullOrBlank()) {
                    adminDao.setConfig(AdminConfigEntity(key = "owner_id", value = cloudOwnerId))
                }
                if (!cloudPassword.isNullOrBlank()) {
                    adminDao.setConfig(AdminConfigEntity(key = "admin_pin", value = cloudPassword))
                }
                Log.d("PizzaRepository", "Admin credentials synced from Cloud Firestore: $cloudOwnerId")
            }
        } catch (e: Exception) {
            Log.d("PizzaRepository", "Notice: Admin config cloud sync: ${e.message}")
        }
    }

    suspend fun updateAdminPin(newPin: String) {
        val currentOwnerId = getOwnerId()
        updateOwnerCredentials(currentOwnerId, newPin)
    }

    suspend fun updateOwnerCredentials(ownerId: String, newPin: String) = withContext(Dispatchers.IO) {
        val cleanOwnerId = ownerId.trim()
        val cleanPin = newPin.trim()

        // 1. Save to local Room Database
        adminDao.setConfig(AdminConfigEntity(key = "owner_id", value = cleanOwnerId))
        adminDao.setConfig(AdminConfigEntity(key = "admin_pin", value = cleanPin))

        // 2. Sync to Firestore admin_config/credentials document
        try {
            val db = getDb()
            val credMap = hashMapOf(
                "ownerId" to cleanOwnerId,
                "password" to cleanPin,
                "lastUpdated" to System.currentTimeMillis(),
                "updatedBy" to "AdminAppClient",
                "authType" to "FIREBASE_AUTH_SYNCED"
            )
            db.collection("admin_config").document("credentials").set(credMap)
                .addOnSuccessListener {
                    Log.d("PizzaRepository", "Admin credentials updated in Firestore successfully!")
                }
                .addOnFailureListener { e ->
                    Log.w("PizzaRepository", "Failed to update admin credentials in Firestore", e)
                }
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Error syncing admin credentials to Firestore", e)
        }

        // 3. Update or Create in Firebase Auth
        try {
            val auth = com.example.service.FirebaseInitHelper.getAuth(context)
            val emailForAuth = if (cleanOwnerId.contains("@")) cleanOwnerId else "$cleanOwnerId@slicesmile.com"
            val currentUser = auth.currentUser

            if (currentUser != null && currentUser.email.equals(emailForAuth, ignoreCase = true)) {
                currentUser.updatePassword(cleanPin).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("PizzaRepository", "Firebase Auth password updated successfully for $emailForAuth")
                    } else {
                        Log.w("PizzaRepository", "Firebase Auth password update failed: ${task.exception?.message}")
                    }
                }
            } else {
                // Try signing in and updating, or create admin user if doesn't exist
                auth.signInWithEmailAndPassword(emailForAuth, cleanPin)
                    .addOnFailureListener {
                        auth.createUserWithEmailAndPassword(emailForAuth, cleanPin)
                            .addOnSuccessListener {
                                Log.d("PizzaRepository", "Firebase Auth created new Admin user: $emailForAuth")
                            }
                            .addOnFailureListener { err ->
                                Log.d("PizzaRepository", "Firebase Auth Notice: ${err.message}")
                            }
                    }
            }
        } catch (e: Exception) {
            Log.d("PizzaRepository", "Firebase Auth update notice: ${e.message}")
        }
    }

    // ================= RIDER MANAGEMENT =================
    suspend fun saveRider(rider: Rider) {
        riderDao.insertOrUpdate(RiderEntity.fromDomain(rider))
        try {
            val db = getDb()
            val map = hashMapOf(
                "id" to rider.id,
                "name" to rider.name,
                "phone" to rider.phone,
                "pin" to rider.pin,
                "vehicle" to rider.vehicle,
                "isEnabled" to rider.isEnabled,
                "rating" to rider.rating.toDouble(),
                "totalDeliveries" to rider.totalDeliveries,
                "canAcceptOrder" to rider.canAcceptOrder,
                "canPickOrder" to rider.canPickOrder,
                "canMarkDelivered" to rider.canMarkDelivered,
                "canCallCustomer" to rider.canCallCustomer,
                "canViewDirections" to rider.canViewDirections
            )
            db.collection("riders").document(rider.id).set(map)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore saveRider error", e)
        }
    }

    suspend fun deleteRider(riderId: String) {
        riderDao.deleteRider(riderId)
        try {
            val db = getDb()
            db.collection("riders").document(riderId).delete()
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore deleteRider error", e)
        }
    }

    suspend fun setRiderEnabled(riderId: String, isEnabled: Boolean) {
        riderDao.setRiderEnabled(riderId, isEnabled)
        try {
            val db = getDb()
            db.collection("riders").document(riderId).update("isEnabled", isEnabled)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore setRiderEnabled error", e)
        }
    }

    suspend fun assignRiderToOrder(orderId: Long, rider: Rider) {
        orderDao.assignRider(orderId, rider.id, rider.name, rider.phone, rider.vehicle)
        try {
            val db = getDb()
            val riderUpdateMap = mapOf(
                "riderId" to rider.id,
                "riderName" to rider.name,
                "riderPhone" to rider.phone,
                "riderVehicle" to rider.vehicle
            )
            db.collection("orders").document(orderId.toString()).update(riderUpdateMap)
            
            // Also sync to user's order record
            val localOrder = orderDao.getOrderById(orderId)
            if (localOrder != null && localOrder.userId.isNotBlank()) {
                db.collection("users").document(localOrder.userId).collection("orders").document(orderId.toString()).update(riderUpdateMap)
            }
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore assignRider error", e)
        }
    }

    suspend fun getRiderByPhone(phone: String): Rider? {
        return riderDao.getRiderByPhone(phone)?.toDomain()
    }

    suspend fun getRiderById(id: String): Rider? {
        return riderDao.getRiderById(id)?.toDomain()
    }

    // ================= CUSTOM MENU ITEMS =================
    suspend fun saveCustomMenuItem(item: MenuItem, isDeleted: Boolean = false) {
        customMenuItemDao.insertOrUpdate(CustomMenuItemEntity.fromDomain(item, isDeleted))
        try {
            val db = getDb()
            val map = hashMapOf(
                "id" to item.id,
                "name" to item.name,
                "categoryName" to item.category.name,
                "customCategoryName" to (item.customCategoryName ?: ""),
                "description" to item.description,
                "basePrice" to item.basePrice,
                "originalPrice" to item.originalPrice,
                "discountPercent" to item.discountPercent,
                "imageUrl" to (item.imageUrl ?: ""),
                "isAvailable" to item.isAvailable,
                "isDeleted" to isDeleted
            )
            db.collection("menu_items").document(item.id).set(map)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore saveCustomMenuItem error", e)
        }
    }

    suspend fun deleteCustomMenuItem(itemId: String) {
        customMenuItemDao.insertOrUpdate(
            CustomMenuItemEntity(
                id = itemId,
                name = "",
                categoryName = "DEALS",
                description = "",
                basePrice = 0,
                isDeleted = true
            )
        )
        try {
            val db = getDb()
            db.collection("menu_items").document(itemId).update("isDeleted", true)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore deleteCustomMenuItem error", e)
        }
    }

    suspend fun resetAllMenuToDefaults() {
        customMenuItemDao.deleteAll()
    }

    // ================= ORDER PLACEMENT =================
    suspend fun placeOrder(order: Order): Long {
        val uniqueOrderId = if (order.orderId > 1000) order.orderId else ((100000..999999).random().toLong())
        val finalOrder = order.copy(orderId = uniqueOrderId)
        val entity = OrderEntity.fromDomain(finalOrder)
        orderDao.insertOrder(entity)
        val generatedId = uniqueOrderId

        // Mark as already notified on local device placing it
        notifiedOrderIds.add(generatedId)

        // Sync directly to Firestore for Owner/Admin and Rider devices
        try {
            val db = getDb()
            val orderMap = hashMapOf(
                "orderId" to generatedId,
                "userId" to finalOrder.userId,
                "itemsSummary" to finalOrder.itemsSummary,
                "itemsCount" to finalOrder.itemsCount,
                "subtotal" to finalOrder.subtotal,
                "discount" to finalOrder.discount,
                "deliveryFee" to finalOrder.deliveryFee,
                "totalAmount" to finalOrder.totalAmount,
                "paymentMethodName" to finalOrder.paymentMethod.name,
                "easypaisaTrxId" to (finalOrder.easypaisaTrxId ?: ""),
                "customerName" to finalOrder.customerName,
                "customerPhone" to finalOrder.customerPhone,
                "deliveryAddress" to finalOrder.deliveryAddress,
                "areaLandmark" to finalOrder.areaLandmark,
                "orderNote" to finalOrder.orderNote,
                "coinsEarned" to finalOrder.coinsEarned,
                "coinsRedeemed" to finalOrder.coinsRedeemed,
                "statusName" to finalOrder.status.name,
                "timestamp" to finalOrder.timestamp,
                "riderId" to (finalOrder.riderId ?: ""),
                "riderName" to finalOrder.riderName,
                "riderPhone" to finalOrder.riderPhone,
                "riderVehicle" to finalOrder.riderVehicle,
                "rating" to finalOrder.rating,
                "reviewComment" to finalOrder.reviewComment,
                "feedbackSubmitted" to finalOrder.feedbackSubmitted
            )
            
            // 1. Write to /orders/{orderId} for Admin & Global dispatch
            try {
                com.google.android.gms.tasks.Tasks.await(
                    db.collection("orders").document(generatedId.toString()).set(orderMap)
                )
                Log.d("PizzaRepository", "Order #$generatedId successfully published to /orders/{orderId} in Firestore!")
                _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                    isConnected = true,
                    lastSyncTime = System.currentTimeMillis(),
                    errorMessage = null
                )
            } catch (writeErr: Exception) {
                Log.e("PizzaRepository", "Failed to publish Order #$generatedId to Firestore", writeErr)
                _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                    isConnected = false,
                    errorMessage = "Firestore Upload Error: ${writeErr.localizedMessage ?: writeErr.message}"
                )
            }

            // 2. Write to /users/{userId}/orders/{orderId} for Customer Order History
            if (finalOrder.userId.isNotBlank()) {
                try {
                    db.collection("users").document(finalOrder.userId).collection("orders").document(generatedId.toString()).set(orderMap)
                } catch (e: Exception) {
                    Log.d("PizzaRepository", "User subcollection sync note: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore placeOrder sync error", e)
            _cloudSyncStatus.value = _cloudSyncStatus.value.copy(
                isConnected = false,
                errorMessage = "Firestore Sync Error: ${e.localizedMessage ?: e.message}"
            )
        }

        // Update loyalty profile if coins redeemed or earned, and consume referral discount if used
        val currentProfile = loyaltyDao.getLoyaltyProfile() ?: LoyaltyEntity()
        val newCurrentCoins = (currentProfile.currentCoins - order.coinsRedeemed + order.coinsEarned).coerceAtLeast(0)
        val newLifetimeEarned = currentProfile.totalCoinsEarnedLifetime + order.coinsEarned
        val newLifetimeRedeemed = currentProfile.totalCoinsRedeemedLifetime + order.coinsRedeemed
        val newOrdersCount = currentProfile.totalOrdersCount + 1
        val newTotalSpent = currentProfile.totalSpent + order.totalAmount
        val newAvailableDiscounts = if (order.discount > 0 && currentProfile.availableReferralDiscountsCount > 0) {
            currentProfile.availableReferralDiscountsCount - 1
        } else {
            currentProfile.availableReferralDiscountsCount
        }
        val newPendingDiscount = if (order.discount > 0) false else currentProfile.hasPendingReferralDiscount

        loyaltyDao.insertOrUpdateProfile(
            currentProfile.copy(
                currentCoins = newCurrentCoins,
                totalCoinsEarnedLifetime = newLifetimeEarned,
                totalCoinsRedeemedLifetime = newLifetimeRedeemed,
                totalOrdersCount = newOrdersCount,
                totalSpent = newTotalSpent,
                availableReferralDiscountsCount = newAvailableDiscounts,
                hasPendingReferralDiscount = newPendingDiscount
            )
        )

        return generatedId
    }

    // ================= STATUS ADVANCEMENT =================
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status.name)
        try {
            val db = getDb()
            val statusUpdateMap = mapOf(
                "statusName" to status.name,
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection("orders").document(orderId.toString()).update(statusUpdateMap)
                .addOnSuccessListener {
                    Log.d("PizzaRepository", "Order #$orderId status updated to ${status.name} in Firestore")
                }.addOnFailureListener { e ->
                    Log.e("PizzaRepository", "Firestore status update failed for #$orderId", e)
                }

            // Sync to user's order subcollection
            val localOrder = orderDao.getOrderById(orderId)
            if (localOrder != null && localOrder.userId.isNotBlank()) {
                db.collection("users").document(localOrder.userId).collection("orders").document(orderId.toString()).update(statusUpdateMap)
            }
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore updateOrderStatus error", e)
        }
    }

    suspend fun submitFeedback(feedback: CustomerFeedback) {
        feedbackDao.insertFeedback(FeedbackEntity.fromDomain(feedback))
        orderDao.submitOrderFeedback(feedback.orderId, feedback.overallRating, feedback.comment)
        try {
            val db = getDb()
            val feedbackMap = hashMapOf(
                "id" to feedback.id,
                "orderId" to feedback.orderId,
                "customerName" to feedback.customerName,
                "overallRating" to feedback.overallRating,
                "foodTasteRating" to feedback.foodTasteRating,
                "deliverySpeedRating" to feedback.deliverySpeedRating,
                "comment" to feedback.comment,
                "photoUri" to (feedback.photoUri ?: ""),
                "photoUrl" to (feedback.photoUrl ?: ""),
                "timestamp" to feedback.timestamp
            )
            db.collection("customer_feedback").document(feedback.id.toString()).set(feedbackMap)
            db.collection("orders").document(feedback.orderId.toString()).update(
                mapOf(
                    "rating" to feedback.overallRating,
                    "reviewComment" to feedback.comment,
                    "feedbackSubmitted" to true
                )
            )
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore submitFeedback error", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PizzaRepository? = null

        fun getInstance(context: Context): PizzaRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val instance = PizzaRepository(context.applicationContext, db)
                INSTANCE = instance
                instance
            }
        }
    }
}
