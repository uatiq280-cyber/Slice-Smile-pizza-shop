package com.example.data.repository

import com.example.data.local.AdminConfigEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CustomMenuItemEntity
import com.example.data.local.FeedbackEntity
import com.example.data.local.LoyaltyEntity
import com.example.data.local.OrderEntity
import com.example.data.local.UserSessionEntity
import com.example.model.CustomerFeedback
import com.example.model.LoyaltyProfile
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PizzaRepository(private val database: AppDatabase) {
    private val orderDao = database.orderDao()
    private val loyaltyDao = database.loyaltyDao()
    private val feedbackDao = database.feedbackDao()
    private val adminDao = database.adminDao()
    private val customMenuItemDao = database.customMenuItemDao()
    private val userSessionDao = database.userSessionDao()

    val allOrders: Flow<List<Order>> = orderDao.getAllOrders().map { list ->
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

    suspend fun saveCustomMenuItem(item: MenuItem, isDeleted: Boolean = false) {
        customMenuItemDao.insertOrUpdate(CustomMenuItemEntity.fromDomain(item, isDeleted))
    }

    suspend fun deleteCustomMenuItem(itemId: String) {
        // Mark as deleted so default items are hidden, or delete from custom table
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
    }

    suspend fun submitFeedback(feedback: CustomerFeedback) {
        feedbackDao.insertFeedback(FeedbackEntity.fromDomain(feedback))
        orderDao.submitOrderFeedback(feedback.orderId, feedback.overallRating, feedback.comment)
    }
}
