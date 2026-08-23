package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val orderId: Long = 0,
    val userId: String = "guest_user",
    val itemsSummary: String,
    val itemsCount: Int,
    val subtotal: Int,
    val discount: Int,
    val deliveryFee: Int,
    val totalAmount: Int,
    val paymentMethodName: String,
    val easypaisaTrxId: String? = null,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val areaLandmark: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val orderNote: String = "",
    val coinsEarned: Int = 0,
    val coinsRedeemed: Int = 0,
    val statusName: String = OrderStatus.ORDER_RECEIVED.name,
    val timestamp: Long = System.currentTimeMillis(),
    val riderId: String? = null,
    val riderName: String = "Tariq Mahmood",
    val riderPhone: String = "0303-7448255",
    val riderVehicle: String = "Honda 125 • Thermal Insulated Box",
    val rating: Int = 0,
    val reviewComment: String = "",
    val feedbackSubmitted: Boolean = false
) {
    fun toDomain(): Order {
        val pm = try {
            PaymentMethod.valueOf(paymentMethodName)
        } catch (e: Exception) {
            PaymentMethod.CASH_ON_DELIVERY
        }
        val st = when (statusName) {
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
        return Order(
            orderId = orderId,
            userId = userId,
            itemsSummary = itemsSummary,
            itemsCount = itemsCount,
            subtotal = subtotal,
            discount = discount,
            deliveryFee = deliveryFee,
            totalAmount = totalAmount,
            paymentMethod = pm,
            easypaisaTrxId = easypaisaTrxId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            areaLandmark = areaLandmark,
            latitude = latitude,
            longitude = longitude,
            orderNote = orderNote,
            coinsEarned = coinsEarned,
            coinsRedeemed = coinsRedeemed,
            status = st,
            timestamp = timestamp,
            riderId = riderId,
            riderName = riderName,
            riderPhone = riderPhone,
            riderVehicle = riderVehicle,
            rating = rating,
            reviewComment = reviewComment,
            feedbackSubmitted = feedbackSubmitted
        )
    }

    companion object {
        fun fromDomain(order: Order): OrderEntity {
            return OrderEntity(
                orderId = order.orderId,
                userId = order.userId,
                itemsSummary = order.itemsSummary,
                itemsCount = order.itemsCount,
                subtotal = order.subtotal,
                discount = order.discount,
                deliveryFee = order.deliveryFee,
                totalAmount = order.totalAmount,
                paymentMethodName = order.paymentMethod.name,
                easypaisaTrxId = order.easypaisaTrxId,
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                deliveryAddress = order.deliveryAddress,
                areaLandmark = order.areaLandmark,
                latitude = order.latitude,
                longitude = order.longitude,
                orderNote = order.orderNote,
                coinsEarned = order.coinsEarned,
                coinsRedeemed = order.coinsRedeemed,
                statusName = order.status.name,
                timestamp = order.timestamp,
                riderId = order.riderId,
                riderName = order.riderName,
                riderPhone = order.riderPhone,
                riderVehicle = order.riderVehicle,
                rating = order.rating,
                reviewComment = order.reviewComment,
                feedbackSubmitted = order.feedbackSubmitted
            )
        }
    }
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY timestamp DESC")
    fun getOrdersByUserId(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE riderId = :riderId ORDER BY timestamp DESC")
    fun getOrdersByRiderId(riderId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :id LIMIT 1")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET statusName = :status WHERE orderId = :id")
    suspend fun updateOrderStatus(id: Long, status: String)

    @Query("UPDATE orders SET riderId = :riderId, riderName = :riderName, riderPhone = :riderPhone, riderVehicle = :riderVehicle WHERE orderId = :id")
    suspend fun assignRider(id: Long, riderId: String, riderName: String, riderPhone: String, riderVehicle: String)

    @Query("UPDATE orders SET rating = :rating, reviewComment = :comment, feedbackSubmitted = 1 WHERE orderId = :id")
    suspend fun submitOrderFeedback(id: Long, rating: Int, comment: String)

    @Query("DELETE FROM orders WHERE orderId = :id")
    suspend fun deleteOrder(id: Long)
}
