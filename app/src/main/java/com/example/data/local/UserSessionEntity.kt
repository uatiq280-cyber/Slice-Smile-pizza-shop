package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.model.AuthType
import com.example.model.UserSession
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey
    val id: Int = 1,
    val userId: String,
    val name: String,
    val phone: String,
    val email: String,
    val authType: String,
    val isVerified: Boolean,
    val deliveryAddress: String
) {
    fun toDomain(): UserSession {
        val type = try {
            AuthType.valueOf(authType)
        } catch (e: Exception) {
            AuthType.GUEST
        }
        return UserSession(
            userId = userId,
            name = name,
            phone = phone,
            email = email,
            authType = type,
            isVerified = isVerified,
            deliveryAddress = deliveryAddress
        )
    }

    companion object {
        fun fromDomain(session: UserSession): UserSessionEntity {
            return UserSessionEntity(
                id = 1,
                userId = session.userId,
                name = session.name,
                phone = session.phone,
                email = session.email,
                authType = session.authType.name,
                isVerified = session.isVerified,
                deliveryAddress = session.deliveryAddress
            )
        }
    }
}

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    fun getUserSessionFlow(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    suspend fun getUserSession(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}
