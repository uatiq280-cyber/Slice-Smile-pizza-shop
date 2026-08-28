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
    val totalSpent: Int = 0,
    val referralCode: String = "SMILE-786",
    val successfulReferralsCount: Int = 0,
    val hasPendingReferralDiscount: Boolean = false,
    val availableReferralDiscountsCount: Int = 0,
    val totalReferralDiscountsEarned: Int = 0
) {
    fun toDomain(): LoyaltyProfile {
        return LoyaltyProfile(
            currentCoins = currentCoins,
            totalCoinsEarnedLifetime = totalCoinsEarnedLifetime,
            totalCoinsRedeemedLifetime = totalCoinsRedeemedLifetime,
            totalOrdersCount = totalOrdersCount,
            totalSpent = totalSpent,
            referralCode = referralCode,
            successfulReferralsCount = successfulReferralsCount,
            hasPendingReferralDiscount = hasPendingReferralDiscount,
            availableReferralDiscountsCount = availableReferralDiscountsCount,
            totalReferralDiscountsEarned = totalReferralDiscountsEarned
        )
    }

    companion object {
        fun fromDomain(domain: LoyaltyProfile): LoyaltyEntity {
            return LoyaltyEntity(
                profileId = 1,
                currentCoins = domain.currentCoins,
                totalCoinsEarnedLifetime = domain.totalCoinsEarnedLifetime,
                totalCoinsRedeemedLifetime = domain.totalCoinsRedeemedLifetime,
                totalOrdersCount = domain.totalOrdersCount,
                totalSpent = domain.totalSpent,
                referralCode = domain.referralCode,
                successfulReferralsCount = domain.successfulReferralsCount,
                hasPendingReferralDiscount = domain.hasPendingReferralDiscount,
                availableReferralDiscountsCount = domain.availableReferralDiscountsCount,
                totalReferralDiscountsEarned = domain.totalReferralDiscountsEarned
            )
        }
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
