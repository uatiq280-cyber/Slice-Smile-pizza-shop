package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.LocationManager
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishInputBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationSelectorSheet(
    currentAddress: String,
    currentLandmark: String,
    onSaveLocation: (address: String, landmark: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationManager = remember { LocationManager(context) }

    var addressInput by remember { mutableStateOf(currentAddress) }
    var landmarkInput by remember { mutableStateOf(currentLandmark) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationStatusMsg by remember { mutableStateOf<String?>(null) }

    val fetchGpsLocation = {
        isFetchingLocation = true
        locationStatusMsg = "Detecting GPS location..."
        coroutineScope.launch {
            try {
                val loc = locationManager.getCurrentLocation()
                if (loc != null) {
                    val latFormatted = String.format(java.util.Locale.US, "%.5f", loc.latitude)
                    val lngFormatted = String.format(java.util.Locale.US, "%.5f", loc.longitude)
                    val distKm = locationManager.getDistanceKm(loc.latitude, loc.longitude)
                    val distStr = String.format(java.util.Locale.US, "%.1f", distKm)
                    addressInput = "Live GPS: $latFormatted, $lngFormatted (${distStr}km from shop)"
                    landmarkInput = "Near GPS Location (Within ${distStr} KM of Chowk Nazir Wala)"
                    locationStatusMsg = "✅ GPS Acquired! (${distStr} km from shop)"
                } else {
                    addressInput = "Street 4, House 12, Chowk Nazir Wala"
                    landmarkInput = "Near Jamia Masjid, Nazir Wala"
                    locationStatusMsg = "Using default landmark (Chowk Nazir Wala)"
                }
            } catch (e: Exception) {
                addressInput = "Street 4, House 12, Chowk Nazir Wala"
                landmarkInput = "Near Jamia Masjid, Nazir Wala"
                locationStatusMsg = "Location set to default Chowk Nazir Wala"
            } finally {
                isFetchingLocation = false
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            fetchGpsLocation()
        } else {
            addressInput = "Street 4, House 12, Chowk Nazir Wala"
            landmarkInput = "Near Jamia Masjid, Nazir Wala"
            locationStatusMsg = "Permission denied. Using Chowk Nazir Wala default."
        }
    }

    val quickAreas = listOf(
        "Chowk Nazir Wala",
        "Main Bazar Nazir Wala",
        "Circular Road",
        "Govt Degree College Road",
        "Civil Hospital Road",
        "Gulberg Colony",
        "Railway Road",
        "Al-Rehman Town",
        "Housing Scheme #1"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PolishPrimaryContainerSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PolishPrimaryRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Delivery Location",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_location_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PolishTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Free Delivery Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainerSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PolishPrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Free Home Delivery on min order Rs. 500 within 3 KM radius of Chowk Nazir Wala!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PolishMaroonDark,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detect GPS button
                OutlinedButton(
                    onClick = {
                        if (locationManager.hasLocationPermission()) {
                            fetchGpsLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("use_gps_location_btn"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PolishPrimaryRed,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = PolishPrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFetchingLocation) "Acquiring GPS Signal..." else "Use My Current GPS Location",
                        color = PolishPrimaryRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (!locationStatusMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = locationStatusMsg!!,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishPrimaryRed,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Popular Areas
                Text(
                    text = "Quick Select Nearby Area:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishMaroonDark
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAreas.forEach { area ->
                        val isSelected = addressInput.contains(area) || landmarkInput.contains(area)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) PolishPrimaryContainerSubtle
                                    else PolishBgLight
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PolishPrimaryRed else PolishBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    landmarkInput = area
                                    if (addressInput.isBlank()) {
                                        addressInput = "$area, Street 2"
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = if (isSelected) PolishPrimaryRed else PolishTextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = area,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PolishPrimaryRed else PolishTextDark
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Street Address
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
                    label = { Text("Complete Street Address / House No. *") },
                    placeholder = { Text("e.g. House 45, Street 3, Chowk Nazir Wala", color = PolishTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed,
                        unfocusedLabelColor = PolishTextMuted,
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Nearby Landmark
                OutlinedTextField(
                    value = landmarkInput,
                    onValueChange = { landmarkInput = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
                    label = { Text("Nearby Famous Landmark / Gate / Shop") },
                    placeholder = { Text("e.g. Opposite Nazir General Store", color = PolishTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("landmark_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed,
                        unfocusedLabelColor = PolishTextMuted,
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishInputBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Save button
                Button(
                    onClick = {
                        onSaveLocation(addressInput.ifBlank { "Chowk Nazir Wala" }, landmarkInput)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishMaroonDark,
                        contentColor = PolishPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .testTag("save_location_btn")
                ) {
                    Text(
                        text = "Set Delivery Location",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = PolishPrimaryContainer
                    )
                }
            }
        }
    }
}

