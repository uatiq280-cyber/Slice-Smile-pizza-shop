package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.PortionSize
import com.example.model.SizeOption
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "admin_config")
data class AdminConfigEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

@Dao
interface AdminDao {
    @Query("SELECT value FROM admin_config WHERE `key` = :key LIMIT 1")
    fun getConfigFlow(key: String): Flow<String?>

    @Query("SELECT value FROM admin_config WHERE `key` = :key LIMIT 1")
    suspend fun getConfig(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(entity: AdminConfigEntity)
}

@Entity(tableName = "custom_menu_items")
data class CustomMenuItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val categoryName: String,
    val customCategoryName: String? = null,
    val description: String,
    val basePrice: Int,
    val sizeOptionsSerialized: String = "", // e.g. "SMALL:450;MEDIUM:850;LARGE:1200;EXTRA_LARGE:1500"
    val dealIncludesSerialized: String = "", // e.g. "1 Zinger Burger|||1 Regular Fries|||1 Reg Coke"
    val isSpicy: Boolean = false,
    val isPopular: Boolean = false,
    val tag: String? = null,
    val imageDrawableRes: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val isDeleted: Boolean = false
) {
    fun toDomain(): MenuItem {
        val cat = try {
            MenuCategory.valueOf(categoryName)
        } catch (e: Exception) {
            MenuCategory.CUSTOM
        }

        val sizes = if (sizeOptionsSerialized.isNotBlank()) {
            sizeOptionsSerialized.split(";").mapNotNull { part ->
                val tokens = part.split(":")
                if (tokens.size == 2) {
                    try {
                        val sizeEnum = PortionSize.valueOf(tokens[0].trim())
                        val price = tokens[1].trim().toIntOrNull() ?: 0
                        SizeOption(size = sizeEnum, price = price)
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
        } else {
            emptyList()
        }

        val deals = if (dealIncludesSerialized.isNotBlank()) {
            dealIncludesSerialized.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        return MenuItem(
            id = id,
            name = name,
            category = cat,
            customCategoryName = customCategoryName,
            description = description,
            basePrice = basePrice,
            sizeOptions = sizes,
            dealIncludes = deals,
            isSpicy = isSpicy,
            isPopular = isPopular,
            tag = tag,
            imageDrawableRes = imageDrawableRes,
            imageUrl = imageUrl,
            isAvailable = isAvailable
        )
    }

    companion object {
        fun fromDomain(item: MenuItem, isDeleted: Boolean = false): CustomMenuItemEntity {
            val sizesStr = item.sizeOptions.joinToString(";") { "${it.size.name}:${it.price}" }
            val dealsStr = item.dealIncludes.joinToString("|||")

            return CustomMenuItemEntity(
                id = item.id,
                name = item.name,
                categoryName = item.category.name,
                customCategoryName = item.customCategoryName,
                description = item.description,
                basePrice = item.basePrice,
                sizeOptionsSerialized = sizesStr,
                dealIncludesSerialized = dealsStr,
                isSpicy = item.isSpicy,
                isPopular = item.isPopular,
                tag = item.tag,
                imageDrawableRes = item.imageDrawableRes,
                imageUrl = item.imageUrl,
                isAvailable = item.isAvailable,
                isDeleted = isDeleted
            )
        }
    }
}

@Dao
interface CustomMenuItemDao {
    @Query("SELECT * FROM custom_menu_items")
    fun getAllCustomMenuItemsFlow(): Flow<List<CustomMenuItemEntity>>

    @Query("SELECT * FROM custom_menu_items")
    suspend fun getAllCustomMenuItems(): List<CustomMenuItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CustomMenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomMenuItemEntity>)

    @Query("DELETE FROM custom_menu_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM custom_menu_items")
    suspend fun deleteAll()
}
