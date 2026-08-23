package com.example

import android.app.Application
import android.util.Log
import com.example.service.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging

class SliceSmileApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Notification Channels
        try {
            NotificationHelper.initNotificationChannels(this)
        } catch (e: Exception) {
            Log.e("SliceSmileApp", "Error initializing notification channels", e)
        }

        // 2. Initialize Firebase App if needed
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("SliceSmileApp", "FirebaseApp initialized successfully")

            // Configure Firestore settings for optimal real-time sync
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w("SliceSmileApp", "FirebaseApp init notice: ${e.message}")
        }

        // 3. Setup Firebase Cloud Messaging (FCM) Topics
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("SliceSmileApp", "FCM Device Token: $token")
                }
            }

            // Subscribe to order notification topics
            FirebaseMessaging.getInstance().subscribeToTopic("all_orders")
            FirebaseMessaging.getInstance().subscribeToTopic("owner_orders")
            FirebaseMessaging.getInstance().subscribeToTopic("riders")
        } catch (e: Exception) {
            Log.w("SliceSmileApp", "FCM setup notice: ${e.message}")
        }
    }
}
