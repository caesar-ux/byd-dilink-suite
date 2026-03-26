package com.byd.dilink.extras.hazard.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.ui.components.LargeIconButton
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurface
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.core.ui.theme.HazardOrange
import com.byd.dilink.extras.core.ui.theme.StatusGreen
import com.byd.dilink.extras.core.ui.theme.StatusRed
import com.byd.dilink.extras.core.ui.theme.StatusYellow
import com.byd.dilink.extras.hazard.model.HazardType
import com.byd.dilink.extras.hazard.viewmodel.HazardViewModel
import com.byd.dilink.extras.hazard.viewmodel.HazardWithDistance
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

fun hazardTypeIcon(type: HazardType): ImageVector = when (type) {
    HazardType.POTHOLE -> Icons.Filled.Warning
    HazardType.SPEED_BUMP -> Icons.Filled.SignalCellularAlt
    HazardType.CHECKPOINT -> Icons.Filled.Shield
    HazardType.CONSTRUCTION -> Icons.Filled.Construction
    HazardType.BAD_ROAD -> Icons.Filled.Dangerous
    HazardType.FLOOD -> Icons.Filled.Water
    HazardType.ANIMAL -> Icons.Filled.Pets
    HazardType.POLICE -> Icons.Filled.LocalPolice
    HazardType.SPEED_CAMERA -> Icons.Filled.Camera
    HazardType.ACCIDENT -> Icons.Filled.CarCrash
    HazardType.OTHER -> Icons.Filled.Place
}

@Composable
fun LiveDriveScreen(
    onNavigateToList: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HazardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showMoreDialog by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startLocationUpdates()
    }

    LaunchedEffect(Unit) {
        if (!state.hasLocationPermission) {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(state.savedMessage) {
        state.savedMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiLinkBackground)
    ) {
        // Top Status Bar
        TopStatusBar(
            isRecording = state.isRecording,
            nearbyCount = state.nearbyCount,
            onToggleRecording = { viewModel.toggleRecording() },
            onNavigateToList = onNavigateToList,
            onNavigateToRoute = onNavigateToRoute,
            onNavigateToSettings = onNavigateToSettings
        )

        // Warning Banner
        AnimatedVisibility(
            visible = state.warningActive && state.closestHazard != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            state.closestHazard?.let { closest ->
                WarningBanner(hazardWithDistance = closest)
            }
        }

        // Radar Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HazardRadar(
                nearbyHazards = state.nearbyHazards,
                heading = state.currentHeading,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }

        // Speed display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.currentSpeed.toInt()} km/h",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = DiLinkTextPrimary
            )
            Text(
                text = if (state.isRecording) "● Recording" else "○ Stopped",
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.isRecording) StatusGreen else DiLinkTextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick-add buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LargeIconButton(
                icon = Icons.Filled.Warning,
                label = "Pothole",
                onClick = { viewModel.addHazard(HazardType.POTHOLE) },
                tintColor = HazardOrange,
                containerColor = HazardOrange.copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )
            LargeIconButton(
                icon = Icons.Filled.SignalCellularAlt,
                label = "Bump",
                onClick = { viewModel.addHazard(HazardType.SPEED_BUMP) },
                tintColor = StatusYellow,
                containerColor = StatusYellow.copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )
            LargeIconButton(
                icon = Icons.Filled.Shield,
                label = "Check",
                onClick = { viewModel.addHazard(HazardType.CHECKPOINT) },
                tintColor = StatusRed,
                containerColor = StatusRed.copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )
            LargeIconButton(
                icon = Icons.Filled.MoreHoriz,
                label = "More",
                onClick = { showMoreDialog = true },
                tintColor = DiLinkTextSecondary,
                containerColor = DiLinkSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // More hazard types dialog
    if (showMoreDialog) {
        MoreHazardsDialog(
            onSelect = { type ->
                viewModel.addHazard(type)
                showMoreDialog = false
            },
            onDismiss = { showMoreDialog = false }
        )
    }
}

@Composable
private fun TopStatusBar(
    isRecording: Boolean,
    nearbyCount: Int,
    onToggleRecording: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DiLinkSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onToggleRecording() }
        ) {
            Icon(
                imageVector = Icons.Filled.FiberManualRecord,
                contentDescription = "Recording indicator",
                tint = if (isRecording) StatusGreen.copy(alpha = pulseAlpha) else DiLinkTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRecording) "Recording" else "Stopped",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isRecording) StatusGreen else DiLinkTextMuted
            )
        }

        Text(
            text = "$nearbyCount hazards nearby",
            style = MaterialTheme.typography.bodyMedium,
            color = DiLinkTextSecondary
        )

        Row {
            IconButton(onClick = onNavigateToList) {
                Icon(Icons.Filled.List, "Hazard List", tint = DiLinkTextPrimary)
            }
            IconButton(onClick = onNavigateToRoute) {
                Icon(Icons.Filled.Route, "Route Hazards", tint = DiLinkTextPrimary)
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Filled.Settings, "Settings", tint = DiLinkTextPrimary)
            }
        }
    }
}

@Composable
private fun WarningBanner(hazardWithDistance: HazardWithDistance) {
    val type = HazardType.fromName(hazardWithDistance.record.type)
    val hazardColor = Color(type.colorLong)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(hazardColor.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = hazardTypeIcon(type),
            contentDescription = type.label,
            tint = hazardColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "⚠ ${type.label} ahead!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DiLinkTextPrimary
            )
            Text(
                text = formatDistance(hazardWithDistance.distanceMeters) +
                        " ${hazardWithDistance.direction}",
                style = MaterialTheme.typography.bodyMedium,
                color = DiLinkTextSecondary
            )
        }
    }
}

@Composable
fun HazardRadar(
    nearbyHazards: List<HazardWithDistance>,
    heading: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hazard_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hazard_pulse_alpha"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = min(centerX, centerY) * 0.9f
        val radarMaxDistanceM = 5000.0

        val circleDistances = listOf(500.0, 1000.0, 2000.0, 5000.0)
        val circleLabels = listOf("500m", "1km", "2km", "5km")

        // Draw concentric circles
        for (i in circleDistances.indices) {
            val fraction = (circleDistances[i] / radarMaxDistanceM).toFloat()
            val radius = maxRadius * fraction

            drawCircle(
                color = Color(0xFF2A2A2A),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )

            // Draw label
            drawContext.canvas.nativeCanvas.drawText(
                circleLabels[i],
                centerX + radius * 0.02f + 4f,
                centerY - radius + 16f,
                android.graphics.Paint().apply {
                    color = 0xFF666666.toInt()
                    textSize = 28f
                    isAntiAlias = true
                }
            )
        }

        // Draw crosshairs
        drawLine(
            color = Color(0xFF333333),
            start = Offset(centerX, centerY - maxRadius),
            end = Offset(centerX, centerY + maxRadius),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0xFF333333),
            start = Offset(centerX - maxRadius, centerY),
            end = Offset(centerX + maxRadius, centerY),
            strokeWidth = 1f
        )

        // Draw N indicator
        rotate(degrees = -heading, pivot = Offset(centerX, centerY)) {
            drawContext.canvas.nativeCanvas.drawText(
                "N",
                centerX - 8f,
                centerY - maxRadius - 8f,
                android.graphics.Paint().apply {
                    color = 0xFFFF5252.toInt()
                    textSize = 36f
                    isFakeBoldText = true
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        // Draw car at center
        drawCircle(
            color = Color(0xFF00BCD4),
            radius = 8f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color(0xFF00BCD4),
            radius = 14f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )

        // Draw hazard dots
        for (hazard in nearbyHazards) {
            val type = HazardType.fromName(hazard.record.type)
            val hazardColor = Color(type.colorLong)
            val fraction = (hazard.distanceMeters / radarMaxDistanceM).toFloat()
            if (fraction > 1f) continue

            val radius = maxRadius * fraction

            // Calculate position: bearing relative to heading
            val relativeBearing = Math.toRadians((hazard.bearing - heading).toDouble())
            val dotX = centerX + radius * sin(relativeBearing).toFloat()
            val dotY = centerY - radius * cos(relativeBearing).toFloat()

            val dotRadius = if (hazard.distanceMeters <= 500) 10f else 7f
            val alpha = if (hazard.distanceMeters <= 500) pulseAlpha else 1f

            // Outer glow for close hazards
            if (hazard.distanceMeters <= 500) {
                drawCircle(
                    color = hazardColor.copy(alpha = 0.3f * alpha),
                    radius = dotRadius * 2.5f,
                    center = Offset(dotX, dotY)
                )
            }

            drawCircle(
                color = hazardColor.copy(alpha = alpha),
                radius = dotRadius,
                center = Offset(dotX, dotY)
            )
        }
    }
}

@Composable
private fun MoreHazardsDialog(
    onSelect: (HazardType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiLinkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Select Hazard Type",
                    style = MaterialTheme.typography.titleMedium,
                    color = DiLinkTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(HazardType.entries) { type ->
                        val color = Color(type.colorLong)
                        LargeIconButton(
                            icon = hazardTypeIcon(type),
                            label = type.label,
                            onClick = { onSelect(type) },
                            tintColor = color,
                            containerColor = color.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}

fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        String.format("%.1f km", meters / 1000)
    } else {
        "${meters.toInt()}m"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 800)
@Composable
private fun LiveDriveScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
        ) {
            TopStatusBar(
                isRecording = true,
                nearbyCount = 5,
                onToggleRecording = {},
                onNavigateToList = {},
                onNavigateToRoute = {},
                onNavigateToSettings = {}
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                HazardRadar(
                    nearbyHazards = emptyList(),
                    heading = 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("87 km/h", color = DiLinkTextPrimary, fontSize = 24.sp)
                Text("● Recording", color = StatusGreen, fontSize = 18.sp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LargeIconButton(
                    icon = Icons.Filled.Warning,
                    label = "Pothole",
                    onClick = {},
                    tintColor = HazardOrange,
                    modifier = Modifier.weight(1f)
                )
                LargeIconButton(
                    icon = Icons.Filled.SignalCellularAlt,
                    label = "Bump",
                    onClick = {},
                    tintColor = StatusYellow,
                    modifier = Modifier.weight(1f)
                )
                LargeIconButton(
                    icon = Icons.Filled.Shield,
                    label = "Check",
                    onClick = {},
                    tintColor = StatusRed,
                    modifier = Modifier.weight(1f)
                )
                LargeIconButton(
                    icon = Icons.Filled.MoreHoriz,
                    label = "More",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 300, heightDp = 300)
@Composable
private fun HazardRadarPreview() {
    DiLinkExtrasTheme {
        HazardRadar(
            nearbyHazards = emptyList(),
            heading = 45f,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}
