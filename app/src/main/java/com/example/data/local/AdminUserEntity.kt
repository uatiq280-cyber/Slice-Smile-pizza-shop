package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.model.AdminRole
import com.example.model.AdminUser
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "admin_users")
data class AdminUserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val name: String,
    val phone: String,
    val pin: String,
    val roleName: String,
    val isActive: Boolean = true,
    val canManageMenu: Boolean = true,
    val canManageOrders: Boolean = true,
    val canViewReports: Boolean = true,
    val canManageRiders: Boolean = true,
    val canManagePartners: Boolean = false,
    val canManagePayments: Boolean = false,
    val canManageDeals: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): AdminUser {
        val roleEnum = try {
            AdminRole.valueOf(roleName)
        } catch (e: Exception) {
            AdminRole.PARTNER
        }
        return AdminUser(
            id = id,
            username = username,
            name = name,
            phone = phone,
            pin = pin,
            role = roleEnum,
            isActive = isActive,
            canManageMenu = canManageMenu,
            canManageOrders = canManageOrders,
            canViewReports = canViewReports,
            canManageRiders = canManageRiders,
            canManagePartners = canManagePartners,
            canManagePayments = canManagePayments,
            canManageDeals = canManageDeals,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(user: AdminUser): AdminUserEntity {
            return AdminUserEntity(
                id = user.id,
                username = user.username,
                name = user.name,
                phone = user.phone,
                pin = user.pin,
                roleName = user.role.name,
                isActive = user.isActive,
                canManageMenu = user.canManageMenu,
                canManageOrders = user.canManageOrders,
                canViewReports = user.canViewReports,
                canManageRiders = user.canManageRiders,
                canManagePartners = user.canManagePartners,
                canManagePayments = user.canManagePayments,
                canManageDeals = user.canManageDeals,
                createdAt = user.createdAt
            )
        }
    }
}

@Dao
interface AdminUserDao {
    @Query("SELECT * FROM admin_users ORDER BY createdAt ASC")
    fun getAllAdminUsersFlow(): Flow<List<AdminUserEntity>>

    @Query("SELECT * FROM admin_users ORDER BY createdAt ASC")
    suspend fun getAllAdminUsers(): List<AdminUserEntity>

    @Query("SELECT * FROM admin_users WHERE username = :username AND pin = :pin AND isActive = 1 LIMIT 1")
    suspend fun authenticateUser(username: String, pin: String): AdminUserEntity?

    @Query("SELECT * FROM admin_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): AdminUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: AdminUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<AdminUserEntity>)

    @Query("DELETE FROM admin_users WHERE id = :id")
    suspend fun deleteUser(id: String)
}
