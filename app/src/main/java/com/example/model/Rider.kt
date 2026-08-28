package com.example.model

data class Rider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val vehicle: String = "Honda 125 (Thermal Box)",
    val pin: String = "1234",
    val isAvailable: Boolean = true,
    val isEnabled: Boolean = true,
    val rating: Double = 5.0,
    val totalDeliveries: Int = 0,
    val activeOrdersCount: Int = 0,
    val canAcceptOrder: Boolean = true,
    val canPickOrder: Boolean = true,
    val canMarkDelivered: Boolean = true,
    val canCallCustomer: Boolean = true,
    val canViewDirections: Boolean = true
)

