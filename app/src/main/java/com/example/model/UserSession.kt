package com.example.model

enum class AuthType {
    GUEST,
    PHONE_OTP,
    GOOGLE_GMAIL
}

data class UserSession(
    val userId: String = "guest_user",
    val name: String = "Guest Customer",
    val phone: String = "",
    val email: String = "",
    val authType: AuthType = AuthType.GUEST,
    val isVerified: Boolean = false,
    val deliveryAddress: String = "Sadiqabad (Jinnah Town / Kausar Colony)",
    val isOwner: Boolean = false
) {
    val displaySubtitle: String
        get() = when (authType) {
            AuthType.GUEST -> "Guest Foodie 🍕"
            AuthType.PHONE_OTP -> if (phone.isNotBlank()) phone else "Mobile Verified ✅"
            AuthType.GOOGLE_GMAIL -> if (email.isNotBlank()) email else "Google Account 📧"
        }
}
