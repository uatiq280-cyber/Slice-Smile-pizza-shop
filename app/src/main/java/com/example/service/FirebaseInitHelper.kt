package com.example.service

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object FirebaseInitHelper {
    const val APP_ID = "1:109568220296:android:d6c26f3161b3c39f4bd33d"
    const val API_KEY = "AIzaSyCu1dbfnq8z7ukCNIuQM89TDynaY5Paaqk"
    const val PROJECT_ID = "slice-smile-pizza-shop-2026"
    const val STORAGE_BUCKET = "slice-smile-pizza-shop-2026.firebasestorage.app"
    const val GCM_SENDER_ID = "109568220296"

    @Synchronized
    fun getOrInitFirebaseApp(context: Context): FirebaseApp {
        val apps = FirebaseApp.getApps(context)
        if (apps.isNotEmpty()) {
            return FirebaseApp.getInstance()
        }

        return try {
            val app = FirebaseApp.initializeApp(context)
            if (app != null) app else initExplicit(context)
        } catch (e: Throwable) {
            Log.w("FirebaseInitHelper", "Default initializeApp failed, initializing with explicit FirebaseOptions: ${e.message}")
            initExplicit(context)
        }
    }

    private fun initExplicit(context: Context): FirebaseApp {
        val options = FirebaseOptions.Builder()
            .setApplicationId(APP_ID)
            .setApiKey(API_KEY)
            .setProjectId(PROJECT_ID)
            .setStorageBucket(STORAGE_BUCKET)
            .setGcmSenderId(GCM_SENDER_ID)
            .build()
        return FirebaseApp.initializeApp(context.applicationContext, options)
    }

    fun getFirestore(context: Context): FirebaseFirestore {
        val app = getOrInitFirebaseApp(context)
        val firestore = FirebaseFirestore.getInstance(app)
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore.firestoreSettings = settings
        } catch (e: Exception) {
            Log.d("FirebaseInitHelper", "Firestore settings note: ${e.message}")
        }
        return firestore
    }

    fun getAuth(context: Context): FirebaseAuth {
        val app = getOrInitFirebaseApp(context)
        return FirebaseAuth.getInstance(app)
    }
}
