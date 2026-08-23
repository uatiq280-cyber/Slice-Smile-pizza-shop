package com.example.model

enum class AuthType {
    GUEST,
    PHONE_OTP,
    GOOGLE_GMAIL
}

enum class UserRole {
    CUSTOMER,
    OWNER,
    RIDER
}

data class UserSession(
    val userId: String = "guest_user",
    val name: String = "Guest Customer",
    val phone: String = "",
    val email: String = "",
    val authType: AuthType = AuthType.GUEST,
    val isVerified: Boolean = false,
    val deliveryAddress: String = "Sadiqabad (Jinnah Town / Kausar Colony)",
    val role: UserRole = UserRole.CUSTOMER,
    val riderId: String? = null
) {
    val isOwner: Boolean get() = role == UserRole.OWNER
    val isRider: Boolean get() = role == UserRole.RIDER
    val isCustomer: Boolean get() = role == UserRole.CUSTOMER

    val displaySubtitle: String
        get() = when (role) {
            UserRole.OWNER -> "Shop Owner & Manager 👑"
            UserRole.RIDER -> "Active Delivery Rider 🛵"
            UserRole.CUSTOMER -> when (authType) {
                AuthType.GUEST -> "Guest Foodie 🍕"
                AuthType.PHONE_OTP -> if (phone.isNotBlank()) phone else "Mobile Verified ✅"
                AuthType.GOOGLE_GMAIL -> if (email.isNotBlank()) email else "Google Account 📧"
            }
        }
}
