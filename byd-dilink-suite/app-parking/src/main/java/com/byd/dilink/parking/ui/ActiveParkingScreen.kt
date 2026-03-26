package com.byd.dilink.parking.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.AdaptiveLayout
import com.byd.dilink.core.ui.components.ConfirmDialog
import com.byd.dilink.core.ui.components.SectionCard
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.ParkingBlue
import com.byd.dilink.core.ui.theme.StatusGreen
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.core.ui.theme.StatusYellow
import com.byd.dilink.parking.viewmodel.ParkingUiState
import com.byd.dilink.parking.viewmodel.ParkingViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ActiveParkingScreen(
    onBack: () -> Unit,
    viewModel: ParkingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startLocationUpdates()
        viewModel.startCompass()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationUpdates()
            viewModel.stopCompass()
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Found Your Car?",
            message = "This will clear your saved parking location and stop any timers.",
            confirmText = "Yes, Found It",
            cancelText = "Not Yet",
            isDestructive = false,
            onConfirm = {
                viewModel.clearParking()
                showClearDialog = false
                onBack()
            },
            onDismiss = { showClearDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Active Parking", onBack = onBack)
        },
        containerColor = DiLinkBackground
    ) { padding ->
        AdaptiveLayout(
            portraitContent = {
                ActiveParkingPortrait(
                    uiState = uiState,
                    onStartTimer = { viewModel.startParkingTimer(it) },
                    onStopTimer = { viewModel.stopCountdownTimer() },
                    onStopAlarm = { viewModel.stopAlarm() },
                    onNavigate = {
                        val parking = uiState.activeParking
                        if (parking != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${parking.latitude},${parking.longitude}?q=${parking.latitude},${parking.longitude}(Car)"))
                            context.startActivity(intent)
                        }
                    },
                    onClearParking = { showClearDialog = true },
                    modifier = Modifier.padding(padding)
                )
            },
            landscapeContent = {
                ActiveParkingLandscape(
                    uiState = uiState,
                    onStartTimer = { viewModel.startParkingTimer(it) },
                    onStopTimer = { viewModel.stopCountdownTimer() },
                    onStopAlarm = { viewModel.stopAlarm() },
                    onNavigate = {
                        val parking = uiState.activeParking
                        if (parking != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${parking.latitude},${parking.longitude}?q=${parking.latitude},${parking.longitude}(Car)"))
                            context.startActivity(intent)
                        }
                    },
                    onClearParking = { showClearDialog = true },
                    modifier = Modifier.padding(padding)
                )
            }
        )
    }
}

@Composable
private fun ActiveParkingPortrait(
    uiState: ParkingUiState,
    onStartTimer: (Int) -> Unit,
    onStopTimer: () -> Unit,
    onStopAlarm: () -> Unit,
    onNavigate: () -> Unit,
    onClearParking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Elapsed time
        ElapsedTimeDisplay(elapsedMs = uiState.elapsedTimeMs)

        Spacer(modifier = Modifier.height(16.dp))

        // Parking timer
        ParkingTimerSection(
            timerActive = uiState.timerActive,
            timerRemainingMs = uiState.timerRemainingMs,
            alarmPlaying = uiState.alarmPlaying,
            onStartTimer = onStartTimer,
            onStopTimer = onStopTimer,
            onStopAlarm = onStopAlarm
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Compass
        SectionCard(title = "Direction to Car") {
            CompassRose(
                heading = uiState.compassHeading,
                bearingToCar = uiState.bearingToCar,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDistance(uiState.distanceToCar),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                color = DiLinkTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation button
        Button(
            onClick = onNavigate,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ParkingBlue)
        ) {
            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Navigate to Car", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Found my car button
        Button(
            onClick = onClearParking,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Found My Car", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ActiveParkingLandscape(
    uiState: ParkingUiState,
    onStartTimer: (Int) -> Unit,
    onStopTimer: () -> Unit,
    onStopAlarm: () -> Unit,
    onNavigate: () -> Unit,
    onClearParking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left: Timer + elapsed
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElapsedTimeDisplay(elapsedMs = uiState.elapsedTimeMs)
            Spacer(modifier = Modifier.height(12.dp))
            ParkingTimerSection(
                timerActive = uiState.timerActive,
                timerRemainingMs = uiState.timerRemainingMs,
                alarmPlaying = uiState.alarmPlaying,
                onStartTimer = onStartTimer,
                onStopTimer = onStopTimer,
                onStopAlarm = onStopAlarm
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ParkingBlue)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Navigate", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClearParking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Found My Car", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Right: Compass + distance
        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SectionCard(title = "Direction to Car") {
                CompassRose(
                    heading = uiState.compassHeading,
                    bearingToCar = uiState.bearingToCar,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDistance(uiState.distanceToCar),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                    color = DiLinkTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ElapsedTimeDisplay(elapsedMs: Long) {
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMs) % 60
    val timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Parked for",
            style = MaterialTheme.typography.bodyMedium,
            color = DiLinkTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = timeString,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = DiLinkTextPrimary
        )
    }
}

@Composable
private fun ParkingTimerSection(
    timerActive: Boolean,
    timerRemainingMs: Long,
    alarmPlaying: Boolean,
    onStartTimer: (Int) -> Unit,
    onStopTimer: () -> Unit,
    onStopAlarm: () -> Unit
) {
    SectionCard(title = "Parking Timer") {
        if (alarmPlaying) {
            // Alarm is playing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TIME'S UP!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = StatusRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onStopAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Icon(Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Alarm", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else if (timerActive && timerRemainingMs >= 0) {
            // Countdown active
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(timerRemainingMs)
            val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(timerRemainingMs) % 60
            val remainingText = String.format(Locale.US, "%02d:%02d", remainingMinutes, remainingSeconds)
            val isUrgent = timerRemainingMs < 5 * 60 * 1000L
            val countdownColor by animateColorAsState(
                targetValue = if (isUrgent) StatusRed else DiLinkTextPrimary,
                label = "countdown_color"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = remainingText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = countdownColor
                )
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiLinkTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onStopTimer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                ) {
                    Icon(Icons.Default.AlarmOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Timer", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else {
            // No timer — show preset buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(30 to "30m", 60 to "1h", 120 to "2h").forEach { (minutes, label) ->
                    Button(
                        onClick = { onStartTimer(minutes) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompassRose(
    heading: Float,
    bearingToCar: Float,
    modifier: Modifier = Modifier
) {
    val arrowAngle = (bearingToCar - heading + 360f) % 360f

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy) * 0.85f

        // Outer circle
        drawCircle(
            color = DiLinkSurfaceVariant,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 3f)
        )

        // Inner circle
        drawCircle(
            color = DiLinkSurfaceVariant.copy(alpha = 0.5f),
            radius = radius * 0.5f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )

        // Cardinal direction marks
        val cardinals = listOf(
            0f to "N", 90f to "E", 180f to "S", 270f to "W"
        )
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = radius * 0.18f
            isAntiAlias = true
        }

        // Tick marks every 30 degrees
        for (angle in 0 until 360 step 15) {
            val radAngle = Math.toRadians((angle - heading).toDouble())
            val isCardinal = angle % 90 == 0
            val isMajor = angle % 45 == 0
            val tickInner = if (isCardinal) 0.75f else if (isMajor) 0.82f else 0.88f
            val tickOuter = 0.95f

            val startX = cx + radius * tickInner * sin(radAngle).toFloat()
            val startY = cy - radius * tickInner * cos(radAngle).toFloat()
            val endX = cx + radius * tickOuter * sin(radAngle).toFloat()
            val endY = cy - radius * tickOuter * cos(radAngle).toFloat()

            drawLine(
                color = if (isCardinal) DiLinkTextPrimary else DiLinkTextMuted,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isCardinal) 3f else 1.5f,
                cap = StrokeCap.Round
            )
        }

        // Cardinal letters
        for ((angle, letter) in cardinals) {
            val radAngle = Math.toRadians((angle - heading).toDouble())
            val textRadius = radius * 0.65f
            val tx = cx + textRadius * sin(radAngle).toFloat()
            val ty = cy - textRadius * cos(radAngle).toFloat()

            paint.color = if (letter == "N") android.graphics.Color.RED else android.graphics.Color.WHITE
            paint.isFakeBoldText = true
            drawContext.canvas.nativeCanvas.drawText(letter, tx, ty + paint.textSize / 3f, paint)
        }

        // Arrow pointing to car
        rotate(arrowAngle, Offset(cx, cy)) {
            val arrowLength = radius * 0.6f
            val arrowWidth = radius * 0.12f

            // Arrow shaft
            drawLine(
                color = ParkingBlue,
                start = Offset(cx, cy),
                end = Offset(cx, cy - arrowLength),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Arrow head
            val headY = cy - arrowLength
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, headY - arrowWidth * 0.8f)
                lineTo(cx - arrowWidth, headY + arrowWidth * 0.3f)
                lineTo(cx + arrowWidth, headY + arrowWidth * 0.3f)
                close()
            }
            drawPath(path, color = ParkingBlue)
        }

        // Center dot
        drawCircle(
            color = DiLinkTextPrimary,
            radius = 6f,
            center = Offset(cx, cy)
        )
    }
}

private fun formatDistance(meters: Float): String {
    return when {
        meters < 1f -> "—"
        meters < 1000f -> String.format(Locale.US, "%.0f m", meters)
        else -> String.format(Locale.US, "%.1f km", meters / 1000f)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 800)
@Composable
fun ActiveParkingPortraitPreview() {
    DiLinkTheme {
        ActiveParkingPortrait(
            uiState = ParkingUiState(
                elapsedTimeMs = 3_723_000,
                compassHeading = 45f,
                bearingToCar = 190f,
                distanceToCar = 250f,
                hasLocation = true
            ),
            onStartTimer = {},
            onStopTimer = {},
            onStopAlarm = {},
            onNavigate = {},
            onClearParking = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 300, heightDp = 300)
@Composable
fun CompassRosePreview() {
    DiLinkTheme {
        CompassRose(
            heading = 30f,
            bearingToCar = 180f,
            modifier = Modifier.size(300.dp)
        )
    }
}
