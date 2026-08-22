package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.model.LoyaltyProfile
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "loyalty_profile")
data class LoyaltyEntity(
    @PrimaryKey
    val profileId: Int = 1,
    val currentCoins: Int = 0,
    val totalCoinsEarnedLifetime: Int = 0,
    val totalCoinsRedeemedLifetime: Int = 0,
    val totalOrdersCount: Int = 0,
    val totalSpent: Int = 0
) {
    fun toDomain(): LoyaltyProfile {
        return LoyaltyProfile(
            currentCoins = currentCoins,
            totalCoinsEarnedLifetime = totalCoinsEarnedLifetime,
            totalCoinsRedeemedLifetime = totalCoinsRedeemedLifetime,
            totalOrdersCount = totalOrdersCount,
            totalSpent = totalSpent
        )
    }
}

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM loyalty_profile WHERE profileId = 1")
    fun getLoyaltyProfileFlow(): Flow<LoyaltyEntity?>

    @Query("SELECT * FROM loyalty_profile WHERE profileId = 1")
    suspend fun getLoyaltyProfile(): LoyaltyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: LoyaltyEntity)
}
