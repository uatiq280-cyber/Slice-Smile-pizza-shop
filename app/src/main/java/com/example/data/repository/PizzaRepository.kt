package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AdminConfigEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CustomMenuItemEntity
import com.example.data.local.FeedbackEntity
import com.example.data.local.LoyaltyEntity
import com.example.data.local.OrderEntity
import com.example.data.local.RiderEntity
import com.example.data.local.UserSessionEntity
import com.example.model.CustomerFeedback
import com.example.model.LoyaltyProfile
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

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

    private var firestore: FirebaseFirestore? = null
    private var firestoreOrdersListener: ListenerRegistration? = null
    private var firestoreRidersListener: ListenerRegistration? = null
    private var firestoreFeedbackListener: ListenerRegistration? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Keep track of order IDs that have already triggered a sound/notification on this device
    private val appLaunchTime = System.currentTimeMillis()
    private val notifiedOrderIds = Collections.synchronizedSet(HashSet<Long>())
    @Volatile
    private var isAdminActive: Boolean = false

    fun setAdminActive(active: Boolean) {
        isAdminActive = active
    }

    init {
        initFirebaseAndListeners()
    }

    private fun initFirebaseAndListeners() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firestore = FirebaseFirestore.getInstance()
            listenToFirestoreOrders()
            listenToFirestoreRiders()
            listenToFirestoreFeedback()
            // Initial fetch to load existing cloud orders and admin credentials immediately
            repositoryScope.launch {
                refreshOrdersFromCloud()
                syncAdminCredentialsFromCloud()
            }
            Log.d("PizzaRepository", "Firestore listeners and initial fetch initialized successfully.")
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Firestore not available or in local-first fallback mode: ${e.message}")
        }
    }

    // ================= REAL-TIME FIRESTORE ORDER SYNC =================
    private fun listenToFirestoreOrders() {
        try {
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
            firestoreOrdersListener?.remove()
            // Listen to orders collection directly without restrictive orderBy to prevent index errors
            firestoreOrdersListener = db.collection("orders")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("PizzaRepository", "Firestore orders listen error", error)
                        return@addSnapshotListener
                    }
                    if (snapshots == null) return@addSnapshotListener

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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
            val snapshot = com.google.android.gms.tasks.Tasks.await(db.collection("orders").get())
            val orders = snapshot.documents.mapNotNull { parseOrderFromDoc(it) }
            if (orders.isNotEmpty()) {
                val entities = orders.map { OrderEntity.fromDomain(it) }
                orderDao.insertOrders(entities)
            }
            Log.d("PizzaRepository", "Cloud orders refreshed: ${orders.size} orders synced.")
            orders
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Failed to refresh orders from Firestore", e)
            emptyList()
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
                            RiderEntity(
                                id = id,
                                name = name,
                                phone = phone,
                                pin = pin,
                                vehicle = vehicle,
                                isEnabled = isEnabled,
                                rating = rating,
                                totalDeliveries = totalDeliveries
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            FeedbackEntity(
                                id = id,
                                orderId = orderId,
                                customerName = customerName,
                                overallRating = overallRating,
                                foodTasteRating = foodTasteRating,
                                deliverySpeedRating = deliverySpeedRating,
                                comment = comment,
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

    val userSessionFlow: Flow<UserSession> = userSessionDao.getUserSessionFlow().map { entity ->
        entity?.toDomain() ?: UserSession()
    }

    val loyaltyProfile: Flow<LoyaltyProfile> = loyaltyDao.getLoyaltyProfileFlow().map { entity ->
        entity?.toDomain() ?: LoyaltyProfile()
    }

    val allFeedback: Flow<List<CustomerFeedback>> = feedbackDao.getAllFeedback().map { list ->
        list.map { it.toDomain() }
    }

    val adminPinFlow: Flow<String> = adminDao.getConfigFlow("admin_pin").map {
        it ?: "Hamza9181@"
    }

    val ownerIdFlow: Flow<String> = adminDao.getConfigFlow("owner_id").map {
        it ?: "Owner@slicesmile.com"
    }

    val customMenuItemsFlow: Flow<List<CustomMenuItemEntity>> = customMenuItemDao.getAllCustomMenuItemsFlow()

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

    suspend fun syncAdminCredentialsFromCloud() = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
            val auth = FirebaseAuth.getInstance()
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
            val map = hashMapOf(
                "id" to rider.id,
                "name" to rider.name,
                "phone" to rider.phone,
                "pin" to rider.pin,
                "vehicle" to rider.vehicle,
                "isEnabled" to rider.isEnabled,
                "rating" to rider.rating.toDouble(),
                "totalDeliveries" to rider.totalDeliveries
            )
            db.collection("riders").document(rider.id).set(map)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore saveRider error", e)
        }
    }

    suspend fun deleteRider(riderId: String) {
        riderDao.deleteRider(riderId)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
            db.collection("riders").document(riderId).delete()
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore deleteRider error", e)
        }
    }

    suspend fun setRiderEnabled(riderId: String, isEnabled: Boolean) {
        riderDao.setRiderEnabled(riderId, isEnabled)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
            db.collection("riders").document(riderId).update("isEnabled", isEnabled)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore setRiderEnabled error", e)
        }
    }

    suspend fun assignRiderToOrder(orderId: Long, rider: Rider) {
        orderDao.assignRider(orderId, rider.id, rider.name, rider.phone, rider.vehicle)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
            val map = hashMapOf(
                "id" to item.id,
                "name" to item.name,
                "categoryName" to item.category.name,
                "description" to item.description,
                "basePrice" to item.basePrice,
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
            db.collection("orders").document(generatedId.toString()).set(orderMap)
                .addOnSuccessListener {
                    Log.d("PizzaRepository", "Order #$generatedId successfully published to /orders/{orderId} in Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e("PizzaRepository", "Failed to publish Order #$generatedId to Firestore", e)
                }

            // 2. Write to /users/{userId}/orders/{orderId} for Customer Order History
            if (finalOrder.userId.isNotBlank()) {
                db.collection("users").document(finalOrder.userId).collection("orders").document(generatedId.toString()).set(orderMap)
                    .addOnSuccessListener {
                        Log.d("PizzaRepository", "Order #$generatedId recorded under /users/${finalOrder.userId}/orders")
                    }
                    .addOnFailureListener { e ->
                        Log.e("PizzaRepository", "Failed to record customer order history in Firestore", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore placeOrder sync error", e)
        }

        // Update loyalty profile if coins redeemed or earned
        val currentProfile = loyaltyDao.getLoyaltyProfile() ?: LoyaltyEntity()
        val newCurrentCoins = (currentProfile.currentCoins - order.coinsRedeemed + order.coinsEarned).coerceAtLeast(0)
        val newLifetimeEarned = currentProfile.totalCoinsEarnedLifetime + order.coinsEarned
        val newLifetimeRedeemed = currentProfile.totalCoinsRedeemedLifetime + order.coinsRedeemed
        val newOrdersCount = currentProfile.totalOrdersCount + 1
        val newTotalSpent = currentProfile.totalSpent + order.totalAmount

        loyaltyDao.insertOrUpdateProfile(
            currentProfile.copy(
                currentCoins = newCurrentCoins,
                totalCoinsEarnedLifetime = newLifetimeEarned,
                totalCoinsRedeemedLifetime = newLifetimeRedeemed,
                totalOrdersCount = newOrdersCount,
                totalSpent = newTotalSpent
            )
        )

        return generatedId
    }

    // ================= STATUS ADVANCEMENT =================
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status.name)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
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
            val db = firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
            val feedbackMap = hashMapOf(
                "id" to feedback.id,
                "orderId" to feedback.orderId,
                "customerName" to feedback.customerName,
                "overallRating" to feedback.overallRating,
                "foodTasteRating" to feedback.foodTasteRating,
                "deliverySpeedRating" to feedback.deliverySpeedRating,
                "comment" to feedback.comment,
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
}
