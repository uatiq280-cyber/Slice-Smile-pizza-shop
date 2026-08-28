package com.example.model

enum class AdminRole(val displayName: String, val badgeEmoji: String) {
    SUPER_ADMIN("Owner / Main Admin", "👑"),
    PARTNER("Business Partner", "🤝"),
    MANAGER("Branch Manager", "💼"),
    DISPATCHER("Order Dispatcher", "📋")
}

data class AdminUser(
    val id: String = "admin",
    val username: String = "admin",
    val name: String = "Main Admin / Owner",
    val phone: String = "0325-4946190",
    val pin: String = "1234",
    val role: AdminRole = AdminRole.SUPER_ADMIN,
    val isActive: Boolean = true,
    val canManageMenu: Boolean = true,
    val canManageOrders: Boolean = true,
    val canViewReports: Boolean = true,
    val canManageRiders: Boolean = true,
    val canManagePartners: Boolean = true,
    val canManagePayments: Boolean = true,
    val canManageDeals: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSuperAdmin: Boolean get() = role == AdminRole.SUPER_ADMIN
    val isPartner: Boolean get() = role == AdminRole.PARTNER
}
