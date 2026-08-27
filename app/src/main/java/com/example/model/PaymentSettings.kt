package com.example.model

data class PaymentSettings(
    val isCodEnabled: Boolean = true,
    val isEasypaisaEnabled: Boolean = true,
    val easypaisaNumber: String = "03254946190",
    val easypaisaTitle: String = "Slice Smile Pizza / Tariq Mahmood",
    val isJazzcashEnabled: Boolean = true,
    val jazzcashNumber: String = "03037448255",
    val jazzcashTitle: String = "Slice Smile Pizza",
    val isBankTransferEnabled: Boolean = true,
    val bankName: String = "Meezan Bank Ltd",
    val bankAccountTitle: String = "Slice Smile Pizza",
    val bankIban: String = "PK36MEZN0001234567890101"
)

data class CustomerUsageStats(
    val customerKey: String,
    val name: String,
    val phone: String,
    val totalOrders: Int,
    val totalSpent: Int,
    val lastOrderTimestamp: Long,
    val lastOrderStatus: OrderStatus,
    val lastOrderSummary: String
)
