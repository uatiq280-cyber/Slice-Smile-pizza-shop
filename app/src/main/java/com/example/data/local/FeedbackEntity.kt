package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.model.CustomerFeedback
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "customer_feedback")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val customerName: String,
    val overallRating: Int,
    val foodTasteRating: Int,
    val deliverySpeedRating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): CustomerFeedback {
        return CustomerFeedback(
            id = id,
            orderId = orderId,
            customerName = customerName,
            overallRating = overallRating,
            foodTasteRating = foodTasteRating,
            deliverySpeedRating = deliverySpeedRating,
            comment = comment,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(feedback: CustomerFeedback): FeedbackEntity {
            return FeedbackEntity(
                id = feedback.id,
                orderId = feedback.orderId,
                customerName = feedback.customerName,
                overallRating = feedback.overallRating,
                foodTasteRating = feedback.foodTasteRating,
                deliverySpeedRating = feedback.deliverySpeedRating,
                comment = feedback.comment,
                timestamp = feedback.timestamp
            )
        }
    }
}

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM customer_feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity): Long
}
