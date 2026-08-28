package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.model.Rider
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "riders")
data class RiderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val vehicle: String = "Honda 125 (Thermal Box)",
    val pin: String = "1234",
    val isAvailable: Boolean = true,
    val isEnabled: Boolean = true,
    val rating: Double = 5.0,
    val totalDeliveries: Int = 0,
    val canAcceptOrder: Boolean = true,
    val canPickOrder: Boolean = true,
    val canMarkDelivered: Boolean = true,
    val canCallCustomer: Boolean = true,
    val canViewDirections: Boolean = true
) {
    fun toDomain(activeCount: Int = 0): Rider {
        return Rider(
            id = id,
            name = name,
            phone = phone,
            vehicle = vehicle,
            pin = pin,
            isAvailable = isAvailable,
            isEnabled = isEnabled,
            rating = rating,
            totalDeliveries = totalDeliveries,
            activeOrdersCount = activeCount,
            canAcceptOrder = canAcceptOrder,
            canPickOrder = canPickOrder,
            canMarkDelivered = canMarkDelivered,
            canCallCustomer = canCallCustomer,
            canViewDirections = canViewDirections
        )
    }

    companion object {
        fun fromDomain(rider: Rider): RiderEntity {
            return RiderEntity(
                id = rider.id,
                name = rider.name,
                phone = rider.phone,
                vehicle = rider.vehicle,
                pin = rider.pin,
                isAvailable = rider.isAvailable,
                isEnabled = rider.isEnabled,
                rating = rider.rating,
                totalDeliveries = rider.totalDeliveries,
                canAcceptOrder = rider.canAcceptOrder,
                canPickOrder = rider.canPickOrder,
                canMarkDelivered = rider.canMarkDelivered,
                canCallCustomer = rider.canCallCustomer,
                canViewDirections = rider.canViewDirections
            )
        }
    }
}

@Dao
interface RiderDao {
    @Query("SELECT * FROM riders ORDER BY name ASC")
    fun getAllRidersFlow(): Flow<List<RiderEntity>>

    @Query("SELECT * FROM riders ORDER BY name ASC")
    suspend fun getAllRiders(): List<RiderEntity>

    @Query("SELECT * FROM riders WHERE id = :id LIMIT 1")
    suspend fun getRiderById(id: String): RiderEntity?

    @Query("SELECT * FROM riders WHERE phone = :phone LIMIT 1")
    suspend fun getRiderByPhone(phone: String): RiderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rider: RiderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(riders: List<RiderEntity>)

    @Query("DELETE FROM riders WHERE id = :id")
    suspend fun deleteRider(id: String)

    @Query("UPDATE riders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRiderEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE riders SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setRiderAvailable(id: String, isAvailable: Boolean)
}
