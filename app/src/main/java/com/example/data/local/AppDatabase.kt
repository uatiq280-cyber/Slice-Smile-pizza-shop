package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OrderEntity::class,
        LoyaltyEntity::class,
        FeedbackEntity::class,
        AdminConfigEntity::class,
        CustomMenuItemEntity::class,
        UserSessionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun loyaltyDao(): LoyaltyDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun adminDao(): AdminDao
    abstract fun customMenuItemDao(): CustomMenuItemDao
    abstract fun userSessionDao(): UserSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "slice_smile_pizza.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial loyalty profile and sample feedback
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            database.adminDao().setConfig(
                                AdminConfigEntity(
                                    key = "admin_pin",
                                    value = "1234"
                                )
                            )
                            database.loyaltyDao().insertOrUpdateProfile(
                                LoyaltyEntity(
                                    profileId = 1,
                                    currentCoins = 100, // Welcome bonus!
                                    totalCoinsEarnedLifetime = 100,
                                    totalCoinsRedeemedLifetime = 0,
                                    totalOrdersCount = 0,
                                    totalSpent = 0
                                )
                            )
                            database.feedbackDao().insertFeedback(
                                FeedbackEntity(
                                    orderId = 101,
                                    customerName = "Ali Raza (Chowk Nazir Wala)",
                                    overallRating = 5,
                                    foodTasteRating = 5,
                                    deliverySpeedRating = 5,
                                    comment = "Crown crust pizza and Zinger burger was super hot, fresh and cheesy! Quick delivery within 20 mins."
                                )
                            )
                            database.feedbackDao().insertFeedback(
                                FeedbackEntity(
                                    orderId = 102,
                                    customerName = "Usman Tariq",
                                    overallRating = 5,
                                    foodTasteRating = 5,
                                    deliverySpeedRating = 4,
                                    comment = "Deal No 2 is incredible value. 2 zingers and 2 shawarmas were top quality."
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
