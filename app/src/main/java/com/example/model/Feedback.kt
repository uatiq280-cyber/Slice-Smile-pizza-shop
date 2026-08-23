package com.example.model

data class CustomerFeedback(
    val id: Long = 0,
    val orderId: Long,
    val customerName: String,
    val overallRating: Int, // 1 to 5
    val foodTasteRating: Int,
    val deliverySpeedRating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LoyaltyProfile(
    val currentCoins: Int = 0,
    val totalCoinsEarnedLifetime: Int = 0,
    val totalCoinsRedeemedLifetime: Int = 0,
    val totalOrdersCount: Int = 0,
    val totalSpent: Int = 0
) {
    // 100 coins = Rs 10 discount
    val discountEquivalentRs: Int get() = (currentCoins / 100) * 10
    val discountValueInRupees: Int get() = discountEquivalentRs
}
