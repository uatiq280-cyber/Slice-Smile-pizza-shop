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

        // 2. Initialize Firebase App using bulletproof FirebaseInitHelper
        try {
            val firebaseApp = com.example.service.FirebaseInitHelper.getOrInitFirebaseApp(this)
            Log.d("SliceSmileApp", "FirebaseApp initialized successfully: ${firebaseApp.name}")

            // Configure Firestore settings for optimal real-time sync
            val db = com.example.service.FirebaseInitHelper.getFirestore(this)
            Log.d("SliceSmileApp", "Firestore initialized successfully")
        } catch (e: Exception) {
            Log.w("SliceSmileApp", "FirebaseApp init notice: ${e.message}")
        }

        // 3. Setup Firebase Cloud Messaging (FCM) Topics (Only if Google Play Services is available)
        try {
            val playServicesAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this)
            if (playServicesAvailability == com.google.android.gms.common.ConnectionResult.SUCCESS) {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        Log.d("SliceSmileApp", "FCM Device Token: $token")
                    }
                    .addOnFailureListener { e ->
                        Log.d("SliceSmileApp", "FCM Token notice: ${e.message}")
                    }
            } else {
                Log.d("SliceSmileApp", "Google Play Services not available, using Firestore real-time listener.")
            }
        } catch (e: Throwable) {
            Log.d("SliceSmileApp", "FCM setup notice: ${e.message}")
        }
    }
}
