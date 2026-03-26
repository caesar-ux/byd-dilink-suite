package com.byd.dilink.extras.fuelcost.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.ui.components.LargeIconButton
import com.byd.dilink.extras.core.ui.components.MetricDisplay
import com.byd.dilink.extras.core.ui.components.SectionCard
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurface
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.core.ui.theme.FuelGreen
import com.byd.dilink.extras.core.ui.theme.StatusGreen
import com.byd.dilink.extras.core.ui.theme.StatusOrange
import com.byd.dilink.extras.fuelcost.viewmodel.FuelCostViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToLogFuel: () -> Unit,
    onNavigateToLogCharge: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: FuelCostViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    var showOdometerDialog by remember { mutableStateOf(false) }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiLinkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    "DM-i Cost Tracker",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            actions = {
                IconButton(onClick = onNavigateToStatistics) {
                    Icon(Icons.Filled.BarChart, "Statistics", tint = DiLinkTextPrimary)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Filled.Settings, "Settings", tint = DiLinkTextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DiLinkSurface,
                titleContentColor = DiLinkTextPrimary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cost per km cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = FuelGreen.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.LocalGasStation,
                            contentDescription = null,
                            tint = StatusOrange,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        MetricDisplay(
                            value = String.format("%.1f", state.costPerKmFuel),
                            unit = "${state.currency}/km",
                            label = "Petrol Cost",
                            valueColor = StatusOrange
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = DiLinkCyan.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.BatteryChargingFull,
                            contentDescription = null,
                            tint = DiLinkCyan,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        MetricDisplay(
                            value = String.format("%.1f", state.costPerKmElectric),
                            unit = "${state.currency}/km",
                            label = "Electric Cost",
                            valueColor = DiLinkCyan
                        )
                    }
                }
            }

            // Savings card
            SectionCard(title = "Monthly Savings") {
                Text(
                    text = "${numberFormat.format(state.monthlySavings.toLong())} ${state.currency}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (state.monthlySavings >= 0) StatusGreen else Color(0xFFFF5252)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (state.monthlySavings >= 0) "Saved vs pure petrol" else "Spending more than petrol",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiLinkTextSecondary
                )
            }

            // EV vs Petrol bar
            EVPercentageBar(
                evPercentage = state.evPercentage.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            // Odometer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DiLinkSurfaceElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Odometer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DiLinkTextSecondary
                        )
                        Text(
                            text = "${numberFormat.format(state.currentOdometer)} km",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DiLinkTextPrimary
                        )
                    }
                    IconButton(onClick = { showOdometerDialog = true }) {
                        Icon(Icons.Filled.Edit, "Update Odometer", tint = DiLinkCyan)
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LargeIconButton(
                    icon = Icons.Filled.LocalGasStation,
                    label = "Log Fuel",
                    onClick = onNavigateToLogFuel,
                    tintColor = StatusOrange,
                    containerColor = StatusOrange.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
                LargeIconButton(
                    icon = Icons.Filled.BatteryChargingFull,
                    label = "Log Charge",
                    onClick = onNavigateToLogCharge,
                    tintColor = DiLinkCyan,
                    containerColor = DiLinkCyan.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
                LargeIconButton(
                    icon = Icons.Filled.Speed,
                    label = "Update KM",
                    onClick = { showOdometerDialog = true },
                    tintColor = DiLinkTextSecondary,
                    containerColor = DiLinkSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            // Recent entries
            if (state.recentFuelRecords.isNotEmpty() || state.recentChargeRecords.isNotEmpty()) {
                SectionCard(title = "Recent Entries") {
                    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

                    state.recentFuelRecords.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LocalGasStation,
                                    contentDescription = null,
                                    tint = StatusOrange,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column {
                                    Text(
                                        text = "${record.liters}L ${record.fuelType}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DiLinkTextPrimary
                                    )
                                    Text(
                                        text = dateFormat.format(Date(record.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DiLinkTextMuted
                                    )
                                }
                            }
                            Text(
                                text = "${numberFormat.format(record.totalCostIqd.toLong())} ${state.currency}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusOrange
                            )
                        }
                    }

                    state.recentChargeRecords.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = DiLinkCyan,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column {
                                    Text(
                                        text = "${record.kwhCharged} kWh (${record.source})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DiLinkTextPrimary
                                    )
                                    Text(
                                        text = dateFormat.format(Date(record.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DiLinkTextMuted
                                    )
                                }
                            }
                            Text(
                                text = "${numberFormat.format(record.totalCostIqd.toLong())} ${state.currency}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = DiLinkCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Odometer dialog
    if (showOdometerDialog) {
        OdometerDialog(
            currentOdometer = state.currentOdometer,
            onSave = { km ->
                viewModel.updateOdometer(km)
                showOdometerDialog = false
            },
            onDismiss = { showOdometerDialog = false }
        )
    }
}

@Composable
fun EVPercentageBar(
    evPercentage: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "EV: ${evPercentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = DiLinkCyan
            )
            Text(
                text = "Petrol: ${(100 - evPercentage).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = StatusOrange
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            val barHeight = size.height
            val barWidth = size.width
            val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

            // Background
            drawRoundRect(
                color = Color(0xFF2A2A2A),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )

            // EV portion (green/cyan)
            val evWidth = barWidth * (evPercentage / 100f).coerceIn(0f, 1f)
            if (evWidth > 0) {
                drawRoundRect(
                    color = Color(0xFF00BCD4),
                    size = Size(evWidth, barHeight),
                    cornerRadius = cornerRadius
                )
            }

            // Petrol portion (orange) — drawn as overlay from right side
            val petrolWidth = barWidth - evWidth
            if (petrolWidth > 0 && evPercentage < 100f) {
                drawRoundRect(
                    color = Color(0xFFFF9800),
                    topLeft = Offset(evWidth, 0f),
                    size = Size(petrolWidth, barHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}

@Composable
private fun OdometerDialog(
    currentOdometer: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var odometerText by remember { mutableStateOf(if (currentOdometer > 0) currentOdometer.toString() else "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiLinkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Update Odometer",
                    style = MaterialTheme.typography.titleLarge,
                    color = DiLinkTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it.filter { c -> c.isDigit() } },
                    label = { Text("Odometer (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DiLinkTextPrimary,
                        unfocusedTextColor = DiLinkTextPrimary,
                        focusedBorderColor = DiLinkCyan,
                        unfocusedBorderColor = DiLinkTextMuted,
                        focusedLabelColor = DiLinkCyan,
                        unfocusedLabelColor = DiLinkTextMuted
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                    ) {
                        Text("Cancel", color = DiLinkTextSecondary)
                    }
                    Button(
                        onClick = {
                            odometerText.toIntOrNull()?.let { onSave(it) }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 800)
@Composable
private fun DashboardScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = FuelGreen.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    MetricDisplay(
                        value = "42.5",
                        unit = "IQD/km",
                        label = "Petrol Cost",
                        valueColor = StatusOrange,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DiLinkCyan.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    MetricDisplay(
                        value = "18.3",
                        unit = "IQD/km",
                        label = "Electric Cost",
                        valueColor = DiLinkCyan,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            EVPercentageBar(evPercentage = 68f)
        }
    }
}
