package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles live location tracking, current location retrieval, and distance calculations
 * for the live order location tracking feature.
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    companion object {
        private const val TAG = "LocationManager"
        // Shop Coordinates (Chowk Nazir Wala)
        const val SHOP_LATITUDE = 31.5204
        const val SHOP_LONGITUDE = 74.3587
        const val FREE_DELIVERY_RADIUS_KM = 3.0
    }

    /**
     * Checks if location permissions are granted.
     */
    fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    /**
     * Gets the current last known location or fetches a fresh location fix asynchronously.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted.")
            return null
        }

        return try {
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                lastLocation
            } else {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining current location", e)
            null
        }
    }

    /**
     * Emits continuous live location updates as a Flow for live order / rider delivery tracking.
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 5000L, minUpdateIntervalMs: Long = 2500L): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Cannot start location updates: permission not granted.")
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(minUpdateIntervalMs)
            .setWaitForAccurateLocation(false)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location updates", e)
            close(e)
        }

        awaitClose {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing location updates", e)
            }
        }
    }

    /**
     * Calculates distance from the shop to the user's coordinates in kilometers.
     */
    fun getDistanceKm(userLat: Double, userLon: Double): Double {
        return calculateDistanceInKm(SHOP_LATITUDE, SHOP_LONGITUDE, userLat, userLon)
    }

    /**
     * Calculates geodesic distance in kilometers between two coordinates using the Haversine formula.
     */
    fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    /**
     * Checks if a target location is within the shop's free delivery radius.
     */
    fun isWithinFreeDeliveryRadius(userLat: Double, userLon: Double): Boolean {
        val distance = calculateDistanceInKm(SHOP_LATITUDE, SHOP_LONGITUDE, userLat, userLon)
        return distance <= FREE_DELIVERY_RADIUS_KM
    }
}
