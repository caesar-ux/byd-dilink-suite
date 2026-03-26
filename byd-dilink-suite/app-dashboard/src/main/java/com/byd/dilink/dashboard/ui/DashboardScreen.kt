package com.byd.dilink.dashboard.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.AdaptiveLayout
import com.byd.dilink.core.ui.components.SectionCard
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.dashboard.viewmodel.DashboardState
import com.byd.dilink.dashboard.viewmodel.DashboardViewModel
import com.byd.dilink.dashboard.viewmodel.TripData
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.startSensors()
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.startSensors()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSensors()
        }
    }

    AdaptiveLayout(
        portraitContent = {
            DashboardPortrait(
                state = state,
                onResetTripA = { viewModel.resetTripA() },
                onResetTripB = { viewModel.resetTripB() },
                onResetMaxSpeed = { viewModel.resetMaxSpeed() },
                onToggleSpeedUnit = { viewModel.toggleSpeedUnit() },
                modifier = modifier
            )
        },
        landscapeContent = {
            DashboardLandscape(
                state = state,
                onResetTripA = { viewModel.resetTripA() },
                onResetTripB = { viewModel.resetTripB() },
                onResetMaxSpeed = { viewModel.resetMaxSpeed() },
                onToggleSpeedUnit = { viewModel.toggleSpeedUnit() },
                modifier = modifier
            )
        }
    )
}

@Composable
private fun DashboardLandscape(
    state: DashboardState,
    onResetTripA: () -> Unit,
    onResetTripB: () -> Unit,
    onResetMaxSpeed: () -> Unit,
    onToggleSpeedUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top row: Speed + Compass
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Speed quadrant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpeedGauge(
                        speed = state.displaySpeed,
                        unit = state.speedUnit,
                        isLandscape = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleSpeedUnit() }
                    ) {
                        Text(
                            text = "Max: ${state.displayMaxSpeed.toInt()} ${state.speedUnit}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = DiLinkTextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onResetMaxSpeed,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reset Max Speed",
                                tint = DiLinkTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Compass quadrant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                CompassView(
                    heading = state.heading,
                    cardinalDirection = state.cardinalDirection,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }
        }

        // Middle: Trip computers side by side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TripComputerCard(
                title = "Trip A",
                trip = state.tripA,
                distanceUnit = state.distanceUnit,
                speedUnit = state.speedUnit,
                onReset = onResetTripA,
                modifier = Modifier.weight(1f)
            )
            TripComputerCard(
                title = "Trip B",
                trip = state.tripB,
                distanceUnit = state.distanceUnit,
                speedUnit = state.speedUnit,
                onReset = onResetTripB,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom status bar
        StatusBar(
            altitude = state.displayAltitude,
            altitudeUnit = state.altitudeUnit,
            satellites = state.satellites,
            accuracy = state.accuracy,
            gForceX = state.gForceX,
            gForceY = state.gForceY,
            peakG = state.peakG,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DashboardPortrait(
    state: DashboardState,
    onResetTripA: () -> Unit,
    onResetTripB: () -> Unit,
    onResetMaxSpeed: () -> Unit,
    onToggleSpeedUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speed
        SpeedGauge(
            speed = state.displaySpeed,
            unit = state.speedUnit,
            isLandscape = false
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onToggleSpeedUnit() }
        ) {
            Text(
                text = "Max: ${state.displayMaxSpeed.toInt()} ${state.speedUnit}",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = DiLinkTextSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onResetMaxSpeed,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset Max Speed",
                    tint = DiLinkTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compass
        CompassView(
            heading = state.heading,
            cardinalDirection = state.cardinalDirection,
            modifier = Modifier.fillMaxWidth(0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Trip computers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TripComputerCard(
                title = "Trip A",
                trip = state.tripA,
                distanceUnit = state.distanceUnit,
                speedUnit = state.speedUnit,
                onReset = onResetTripA,
                modifier = Modifier.weight(1f)
            )
            TripComputerCard(
                title = "Trip B",
                trip = state.tripB,
                distanceUnit = state.distanceUnit,
                speedUnit = state.speedUnit,
                onReset = onResetTripB,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status bar
        StatusBar(
            altitude = state.displayAltitude,
            altitudeUnit = state.altitudeUnit,
            satellites = state.satellites,
            accuracy = state.accuracy,
            gForceX = state.gForceX,
            gForceY = state.gForceY,
            peakG = state.peakG,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TripComputerCard(
    title: String,
    trip: TripData,
    distanceUnit: String,
    speedUnit: String,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMi = distanceUnit == "mi"
    val isMph = speedUnit == "mph"
    val conversionFactor = if (isMi) 0.621371 else 1.0
    val displayDistance = trip.distanceKm * conversionFactor
    val displayAvg = trip.avgSpeedKmh * conversionFactor
    val displayMax = trip.maxSpeedKmh * conversionFactor

    val hours = TimeUnit.MILLISECONDS.toHours(trip.elapsedMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(trip.elapsedMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(trip.elapsedMs) % 60
    val timeString = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)

    SectionCard(title = title, modifier = modifier) {
        Column {
            TripMetricRow("Dist:", String.format(Locale.US, "%.1f %s", displayDistance, distanceUnit))
            TripMetricRow("Time:", timeString)
            TripMetricRow("Avg:", String.format(Locale.US, "%.1f %s", displayAvg, speedUnit))
            TripMetricRow("Max:", String.format(Locale.US, "%d %s", displayMax.toInt(), speedUnit))

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(18.dp),
                    tint = DiLinkTextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun TripMetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DiLinkTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = DiLinkTextPrimary
        )
    }
}

@Composable
private fun StatusBar(
    altitude: Double,
    altitudeUnit: String,
    satellites: Int,
    accuracy: Float,
    gForceX: Float,
    gForceY: Float,
    peakG: Float,
    modifier: Modifier = Modifier
) {
    val totalG = kotlin.math.sqrt(gForceX * gForceX + gForceY * gForceY)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusItem(label = "Alt", value = String.format(Locale.US, "%.0f%s", altitude, altitudeUnit))
        StatusDivider()
        StatusItem(label = "Sat", value = "$satellites")
        StatusDivider()
        StatusItem(label = "Acc", value = String.format(Locale.US, "±%.0fm", accuracy))
        StatusDivider()
        StatusItem(label = "G", value = String.format(Locale.US, "%.2fg", totalG))
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            color = DiLinkTextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DiLinkTextMuted
        )
    }
}

@Composable
private fun StatusDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(DiLinkSurfaceVariant)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 960, heightDp = 540)
@Composable
fun DashboardLandscapePreview() {
    DiLinkTheme {
        DashboardLandscape(
            state = DashboardState(
                speedKmh = 87.0,
                maxSpeedKmh = 127.0,
                heading = 247f,
                altitude = 342.0,
                satellites = 12,
                accuracy = 3f,
                gForceX = 0.15f,
                gForceY = -0.08f,
                peakG = 0.45f,
                tripA = TripData(distanceKm = 45.3, elapsedMs = 2_535_000, avgSpeedKmh = 64.7, maxSpeedKmh = 127.0, isRunning = true),
                tripB = TripData(distanceKm = 1247.8, elapsedMs = 66_912_000, avgSpeedKmh = 67.2, maxSpeedKmh = 135.0, isRunning = true),
                displaySpeed = 87.0,
                displayMaxSpeed = 127.0,
                displayAltitude = 342.0,
                cardinalDirection = "WSW"
            ),
            onResetTripA = {},
            onResetTripB = {},
            onResetMaxSpeed = {},
            onToggleSpeedUnit = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 800)
@Composable
fun DashboardPortraitPreview() {
    DiLinkTheme {
        DashboardPortrait(
            state = DashboardState(
                speedKmh = 87.0,
                maxSpeedKmh = 127.0,
                heading = 247f,
                altitude = 342.0,
                satellites = 12,
                accuracy = 3f,
                gForceX = 0.15f,
                gForceY = -0.08f,
                peakG = 0.45f,
                tripA = TripData(distanceKm = 45.3, elapsedMs = 2_535_000, avgSpeedKmh = 64.7, maxSpeedKmh = 127.0, isRunning = true),
                tripB = TripData(distanceKm = 1247.8, elapsedMs = 66_912_000, avgSpeedKmh = 67.2, maxSpeedKmh = 135.0, isRunning = true),
                displaySpeed = 87.0,
                displayMaxSpeed = 127.0,
                displayAltitude = 342.0,
                cardinalDirection = "WSW"
            ),
            onResetTripA = {},
            onResetTripB = {},
            onResetMaxSpeed = {},
            onToggleSpeedUnit = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 960, heightDp = 100)
@Composable
fun StatusBarPreview() {
    DiLinkTheme {
        StatusBar(
            altitude = 342.0,
            altitudeUnit = "m",
            satellites = 12,
            accuracy = 3f,
            gForceX = 0.15f,
            gForceY = 0.08f,
            peakG = 0.45f
        )
    }
}
