package com.example.data.repository

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
import com.example.model.Rider
import com.example.model.UserRole
import com.example.model.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PizzaRepository(private val database: AppDatabase) {
    private val orderDao = database.orderDao()
    private val loyaltyDao = database.loyaltyDao()
    private val feedbackDao = database.feedbackDao()
    private val adminDao = database.adminDao()
    private val customMenuItemDao = database.customMenuItemDao()
    private val userSessionDao = database.userSessionDao()
    private val riderDao = database.riderDao()

    private var firestore: FirebaseFirestore? = null
    private var firestoreOrdersListener: ListenerRegistration? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            listenToFirestoreOrders()
        } catch (e: Exception) {
            Log.w("PizzaRepository", "Firestore not available, operating in local-first database mode: ${e.message}")
        }
    }

    private fun listenToFirestoreOrders() {
        try {
            firestoreOrdersListener = firestore?.collection("orders")
                ?.addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("PizzaRepository", "Firestore orders listen error", error)
                        return@addSnapshotListener
                    }
                    snapshots?.let { docs ->
                        repositoryScope.launch {
                            val orderEntities = docs.documents.mapNotNull { doc ->
                                try {
                                    val orderId = doc.getLong("orderId") ?: return@mapNotNull null
                                    val userId = doc.getString("userId") ?: "guest_user"
                                    val itemsSummary = doc.getString("itemsSummary") ?: ""
                                    val itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1
                                    val subtotal = doc.getLong("subtotal")?.toInt() ?: 0
                                    val discount = doc.getLong("discount")?.toInt() ?: 0
                                    val deliveryFee = doc.getLong("deliveryFee")?.toInt() ?: 0
                                    val totalAmount = doc.getLong("totalAmount")?.toInt() ?: 0
                                    val paymentMethodName = doc.getString("paymentMethodName") ?: "CASH_ON_DELIVERY"
                                    val easypaisaTrxId = doc.getString("easypaisaTrxId")
                                    val customerName = doc.getString("customerName") ?: ""
                                    val customerPhone = doc.getString("customerPhone") ?: ""
                                    val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                                    val areaLandmark = doc.getString("areaLandmark") ?: ""
                                    val orderNote = doc.getString("orderNote") ?: ""
                                    val coinsEarned = doc.getLong("coinsEarned")?.toInt() ?: 0
                                    val coinsRedeemed = doc.getLong("coinsRedeemed")?.toInt() ?: 0
                                    val statusName = doc.getString("statusName") ?: OrderStatus.ORDER_RECEIVED.name
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    val riderId = doc.getString("riderId")
                                    val riderName = doc.getString("riderName") ?: "Tariq Mahmood"
                                    val riderPhone = doc.getString("riderPhone") ?: "0303-7448255"
                                    val riderVehicle = doc.getString("riderVehicle") ?: "Honda 125 • Thermal Insulated Box"
                                    val rating = doc.getLong("rating")?.toInt() ?: 0
                                    val reviewComment = doc.getString("reviewComment") ?: ""
                                    val feedbackSubmitted = doc.getBoolean("feedbackSubmitted") ?: false

                                    OrderEntity(
                                        orderId = orderId,
                                        userId = userId,
                                        itemsSummary = itemsSummary,
                                        itemsCount = itemsCount,
                                        subtotal = subtotal,
                                        discount = discount,
                                        deliveryFee = deliveryFee,
                                        totalAmount = totalAmount,
                                        paymentMethodName = paymentMethodName,
                                        easypaisaTrxId = easypaisaTrxId,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        deliveryAddress = deliveryAddress,
                                        areaLandmark = areaLandmark,
                                        orderNote = orderNote,
                                        coinsEarned = coinsEarned,
                                        coinsRedeemed = coinsRedeemed,
                                        statusName = statusName,
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
                                    null
                                }
                            }
                            if (orderEntities.isNotEmpty()) {
                                orderDao.insertOrders(orderEntities)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Error setting up Firestore listener", e)
        }
    }

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
        it ?: "1234"
    }

    val ownerIdFlow: Flow<String> = adminDao.getConfigFlow("owner_id").map {
        it ?: "admin"
    }

    val customMenuItemsFlow: Flow<List<CustomMenuItemEntity>> = customMenuItemDao.getAllCustomMenuItemsFlow()

    suspend fun saveUserSession(session: UserSession) {
        userSessionDao.saveUserSession(UserSessionEntity.fromDomain(session))
    }

    suspend fun clearUserSession() {
        userSessionDao.clearSession()
    }

    suspend fun getAdminPin(): String {
        return adminDao.getConfig("admin_pin") ?: "1234"
    }

    suspend fun getOwnerId(): String {
        return adminDao.getConfig("owner_id") ?: "admin"
    }

    suspend fun updateAdminPin(newPin: String) {
        adminDao.setConfig(AdminConfigEntity(key = "admin_pin", value = newPin))
    }

    suspend fun updateOwnerCredentials(ownerId: String, newPin: String) {
        adminDao.setConfig(AdminConfigEntity(key = "owner_id", value = ownerId))
        adminDao.setConfig(AdminConfigEntity(key = "admin_pin", value = newPin))
    }

    // ================= RIDER MANAGEMENT =================
    suspend fun saveRider(rider: Rider) {
        riderDao.insertOrUpdate(RiderEntity.fromDomain(rider))
        try {
            firestore?.collection("riders")?.document(rider.id)?.set(rider)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore saveRider error", e)
        }
    }

    suspend fun deleteRider(riderId: String) {
        riderDao.deleteRider(riderId)
        try {
            firestore?.collection("riders")?.document(riderId)?.delete()
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore deleteRider error", e)
        }
    }

    suspend fun setRiderEnabled(riderId: String, isEnabled: Boolean) {
        riderDao.setRiderEnabled(riderId, isEnabled)
        try {
            firestore?.collection("riders")?.document(riderId)?.update("isEnabled", isEnabled)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore setRiderEnabled error", e)
        }
    }

    suspend fun assignRiderToOrder(orderId: Long, rider: Rider) {
        orderDao.assignRider(orderId, rider.id, rider.name, rider.phone, rider.vehicle)
        try {
            firestore?.collection("orders")?.document(orderId.toString())?.update(
                mapOf(
                    "riderId" to rider.id,
                    "riderName" to rider.name,
                    "riderPhone" to rider.phone,
                    "riderVehicle" to rider.vehicle
                )
            )
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

    suspend fun saveCustomMenuItem(item: MenuItem, isDeleted: Boolean = false) {
        customMenuItemDao.insertOrUpdate(CustomMenuItemEntity.fromDomain(item, isDeleted))
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
    }

    suspend fun resetAllMenuToDefaults() {
        customMenuItemDao.deleteAll()
    }

    suspend fun placeOrder(order: Order): Long {
        val entity = OrderEntity.fromDomain(order)
        val generatedId = orderDao.insertOrder(entity)
        val finalOrder = order.copy(orderId = generatedId)

        // Sync to Firestore
        try {
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
            firestore?.collection("orders")?.document(generatedId.toString())?.set(orderMap)
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

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status.name)
        try {
            firestore?.collection("orders")?.document(orderId.toString())?.update("statusName", status.name)
        } catch (e: Exception) {
            Log.e("PizzaRepository", "Firestore updateOrderStatus error", e)
        }
    }

    suspend fun submitFeedback(feedback: CustomerFeedback) {
        feedbackDao.insertFeedback(FeedbackEntity.fromDomain(feedback))
        orderDao.submitOrderFeedback(feedback.orderId, feedback.overallRating, feedback.comment)
        try {
            firestore?.collection("orders")?.document(feedback.orderId.toString())?.update(
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
