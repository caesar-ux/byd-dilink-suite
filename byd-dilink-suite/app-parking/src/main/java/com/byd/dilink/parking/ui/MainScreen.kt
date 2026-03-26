package com.byd.dilink.parking.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.AdaptiveLayout
import com.byd.dilink.core.ui.components.LargeIconButton
import com.byd.dilink.core.ui.components.SectionCard
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.ParkingBlue
import com.byd.dilink.parking.viewmodel.ParkingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MainScreen(
    onNavigateToActive: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: ParkingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeParking by viewModel.activeParking.collectAsState()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (hasLocationPermission) {
            viewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            viewModel.startLocationUpdates()
            viewModel.startCompass()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationUpdates()
            viewModel.stopCompass()
        }
    }

    AdaptiveLayout(
        portraitContent = {
            MainScreenPortrait(
                uiState = uiState,
                hasActiveParking = activeParking != null,
                hasLocation = uiState.hasLocation,
                onSaveParking = { viewModel.saveParking() },
                onNavigateToActive = onNavigateToActive,
                onNavigateToFavorites = onNavigateToFavorites,
                onNavigateToHistory = onNavigateToHistory
            )
        },
        landscapeContent = {
            MainScreenLandscape(
                uiState = uiState,
                hasActiveParking = activeParking != null,
                hasLocation = uiState.hasLocation,
                onSaveParking = { viewModel.saveParking() },
                onNavigateToActive = onNavigateToActive,
                onNavigateToFavorites = onNavigateToFavorites,
                onNavigateToHistory = onNavigateToHistory
            )
        }
    )
}

@Composable
private fun MainScreenPortrait(
    uiState: com.byd.dilink.parking.viewmodel.ParkingUiState,
    hasActiveParking: Boolean,
    hasLocation: Boolean,
    onSaveParking: () -> Unit,
    onNavigateToActive: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.LocalParking,
            contentDescription = "Parking",
            tint = ParkingBlue,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Parking Assistant",
            style = MaterialTheme.typography.displaySmall,
            color = DiLinkTextPrimary
        )

        Spacer(modifier = Modifier.height(32.dp))

        LargeIconButton(
            icon = Icons.Default.DirectionsCar,
            label = "SAVE PARKING",
            onClick = onSaveParking,
            containerColor = ParkingBlue,
            contentColor = DiLinkTextPrimary,
            enabled = hasLocation && !hasActiveParking,
            modifier = Modifier.height(80.dp)
        )

        if (!hasLocation) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Waiting for GPS signal...",
                style = MaterialTheme.typography.bodySmall,
                color = DiLinkTextMuted,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hasActiveParking && uiState.activeParking != null) {
            ActiveParkingSummaryCard(
                parking = uiState.activeParking!!,
                elapsedMs = uiState.elapsedTimeMs,
                onView = onNavigateToActive
            )
        } else {
            SectionCard(title = "Status") {
                Text(
                    text = "No parking saved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DiLinkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToFavorites,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
            ) {
                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Favorites", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("History", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MainScreenLandscape(
    uiState: com.byd.dilink.parking.viewmodel.ParkingUiState,
    hasActiveParking: Boolean,
    hasLocation: Boolean,
    onSaveParking: () -> Unit,
    onNavigateToActive: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left column: save button + status
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalParking,
                contentDescription = "Parking",
                tint = ParkingBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Parking Assistant",
                style = MaterialTheme.typography.headlineMedium,
                color = DiLinkTextPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))
            LargeIconButton(
                icon = Icons.Default.DirectionsCar,
                label = "SAVE PARKING",
                onClick = onSaveParking,
                containerColor = ParkingBlue,
                contentColor = DiLinkTextPrimary,
                enabled = hasLocation && !hasActiveParking,
                modifier = Modifier.height(80.dp)
            )
            if (!hasLocation) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Waiting for GPS signal...",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiLinkTextMuted
                )
            }
        }

        // Right column: active parking or nav buttons
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasActiveParking && uiState.activeParking != null) {
                ActiveParkingSummaryCard(
                    parking = uiState.activeParking!!,
                    elapsedMs = uiState.elapsedTimeMs,
                    onView = onNavigateToActive
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                SectionCard(title = "Status") {
                    Text(
                        text = "No parking saved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DiLinkTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToFavorites,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Favorites", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onNavigateToHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("History", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ActiveParkingSummaryCard(
    parking: com.byd.dilink.core.data.entities.ParkingRecord,
    elapsedMs: Long,
    onView: () -> Unit
) {
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs) % 60
    val timeAgo = if (hours > 0) "${hours}h ${minutes}m ago" else "${minutes}m ago"

    SectionCard(title = "Active Parking") {
        Text(
            text = "Parked $timeAgo",
            style = MaterialTheme.typography.bodyLarge,
            color = DiLinkTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format(Locale.US, "%.6f, %.6f", parking.latitude, parking.longitude),
            style = MaterialTheme.typography.bodySmall,
            color = DiLinkTextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onView,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ParkingBlue)
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("VIEW", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 800)
@Composable
fun MainScreenPortraitPreview() {
    DiLinkTheme {
        MainScreenPortrait(
            uiState = com.byd.dilink.parking.viewmodel.ParkingUiState(hasLocation = true),
            hasActiveParking = false,
            hasLocation = true,
            onSaveParking = {},
            onNavigateToActive = {},
            onNavigateToFavorites = {},
            onNavigateToHistory = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 800, heightDp = 400)
@Composable
fun MainScreenLandscapePreview() {
    DiLinkTheme {
        MainScreenLandscape(
            uiState = com.byd.dilink.parking.viewmodel.ParkingUiState(hasLocation = true),
            hasActiveParking = false,
            hasLocation = true,
            onSaveParking = {},
            onNavigateToActive = {},
            onNavigateToFavorites = {},
            onNavigateToHistory = {}
        )
    }
}
