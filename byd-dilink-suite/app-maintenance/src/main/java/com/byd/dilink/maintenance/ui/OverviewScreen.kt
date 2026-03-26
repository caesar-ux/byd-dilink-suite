package com.byd.dilink.maintenance.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.SectionCard
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurface
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MaintenanceAmber
import com.byd.dilink.core.ui.theme.StatusGreen
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.core.ui.theme.StatusYellow
import com.byd.dilink.maintenance.viewmodel.CategoryWithStatus
import com.byd.dilink.maintenance.viewmodel.MaintenanceStatus
import com.byd.dilink.maintenance.viewmodel.MaintenanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun iconForName(iconName: String): ImageVector {
    return when (iconName) {
        "water_drop" -> Icons.Default.WaterDrop
        "filter_alt" -> Icons.Default.FilterAlt
        "air" -> Icons.Default.Air
        "ac_unit" -> Icons.Default.AcUnit
        "oil_barrel" -> Icons.Default.OilBarrel
        "build" -> Icons.Default.Build
        "disc_full" -> Icons.Default.DiscFull
        "thermostat" -> Icons.Default.Thermostat
        "settings" -> Icons.Default.Settings
        "electric_bolt" -> Icons.Default.ElectricBolt
        "tire_repair" -> Icons.Default.TireRepair
        "battery_full" -> Icons.Default.BatteryFull
        "battery_charging_full" -> Icons.Default.BatteryChargingFull
        "water" -> Icons.Default.Water
        "straighten" -> Icons.Default.Straighten
        "directions_car" -> Icons.Default.DirectionsCar
        else -> Icons.Default.Build
    }
}

fun statusColor(status: MaintenanceStatus): Color {
    return when (status) {
        MaintenanceStatus.OK -> StatusGreen
        MaintenanceStatus.SOON -> StatusYellow
        MaintenanceStatus.OVERDUE -> StatusRed
    }
}

fun statusLabel(status: MaintenanceStatus): String {
    return when (status) {
        MaintenanceStatus.OK -> "OK"
        MaintenanceStatus.SOON -> "Due Soon"
        MaintenanceStatus.OVERDUE -> "Overdue"
    }
}

@Composable
fun OverviewScreen(
    onNavigateToCategoryDetail: (Long) -> Unit,
    onNavigateToVehicleProfile: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val categoriesWithStatus by viewModel.categoriesWithStatus.collectAsState()
    val vehicleProfile by viewModel.vehicleProfile.collectAsState()
    var showOdometerDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DiLinkBackground,
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToAddCategory,
                    containerColor = MaintenanceAmber,
                    contentColor = DiLinkBackground,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Category",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DiLinkSurface,
                contentColor = DiLinkTextPrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "Overview",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Overview", style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DiLinkCyan,
                        selectedTextColor = DiLinkCyan,
                        unselectedIconColor = DiLinkTextMuted,
                        unselectedTextColor = DiLinkTextMuted,
                        indicatorColor = DiLinkSurfaceElevated
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToVehicleProfile()
                    },
                    icon = {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = "Vehicle Profile",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Vehicle", style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DiLinkCyan,
                        selectedTextColor = DiLinkCyan,
                        unselectedIconColor = DiLinkTextMuted,
                        unselectedTextColor = DiLinkTextMuted,
                        indicatorColor = DiLinkSurfaceElevated
                    )
                )
            }
        }
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val columns = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
        val overdueCount = categoriesWithStatus.count { it.status == MaintenanceStatus.OVERDUE }
        val soonCount = categoriesWithStatus.count { it.status == MaintenanceStatus.SOON }
        val attentionCount = overdueCount + soonCount

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SummaryCard(
                    attentionCount = attentionCount,
                    overdueCount = overdueCount,
                    currentOdometer = vehicleProfile?.currentOdometerKm ?: 0,
                    onUpdateOdometer = { showOdometerDialog = true }
                )
            }

            items(categoriesWithStatus, key = { it.category.id }) { item ->
                MaintenanceCategoryCard(
                    item = item,
                    onClick = { onNavigateToCategoryDetail(item.category.id) }
                )
            }
        }

        if (showOdometerDialog) {
            UpdateOdometerDialog(
                currentOdometer = vehicleProfile?.currentOdometerKm ?: 0,
                onDismiss = { showOdometerDialog = false },
                onConfirm = { newOdometer ->
                    viewModel.updateOdometer(newOdometer)
                    showOdometerDialog = false
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(
    attentionCount: Int,
    overdueCount: Int,
    currentOdometer: Int,
    onUpdateOdometer: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DiLinkSurfaceElevated
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (attentionCount > 0) {
                Text(
                    text = "$attentionCount item${if (attentionCount != 1) "s" else ""} need attention",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = if (overdueCount > 0) StatusRed else StatusYellow
                    )
                )
            } else {
                Text(
                    text = "All maintenance up to date",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = StatusGreen
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Odometer",
                        style = MaterialTheme.typography.labelMedium.copy(color = DiLinkTextMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${NumberFormat.getNumberInstance().format(currentOdometer)} km",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DiLinkTextPrimary
                        )
                    )
                }
                Button(
                    onClick = onUpdateOdometer,
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkCyan)
                ) {
                    Text("Update", style = MaterialTheme.typography.labelLarge.copy(color = DiLinkBackground))
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCategoryCard(
    item: CategoryWithStatus,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    SectionCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconForName(item.category.iconName),
                contentDescription = item.category.name,
                modifier = Modifier.size(32.dp),
                tint = MaintenanceAmber
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.category.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (item.lastServiceDate != null) {
            Text(
                text = "Last: ${dateFormat.format(Date(item.lastServiceDate))} at ${numberFormat.format(item.lastServiceOdometer)} km",
                style = MaterialTheme.typography.bodySmall.copy(color = DiLinkTextSecondary, fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = "No service recorded",
                style = MaterialTheme.typography.bodySmall.copy(color = DiLinkTextMuted, fontSize = 14.sp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        if (item.nextDueKm != null || item.nextDueDate != null) {
            val nextParts = mutableListOf<String>()
            item.nextDueKm?.let { nextParts.add("at ${numberFormat.format(it)} km") }
            item.nextDueDate?.let { nextParts.add(dateFormat.format(Date(it))) }
            Text(
                text = "Next: ${nextParts.joinToString(" or ")}",
                style = MaterialTheme.typography.bodySmall.copy(color = DiLinkTextSecondary, fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor(item.status))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusLabel(item.status),
                style = MaterialTheme.typography.labelLarge.copy(color = statusColor(item.status))
            )
        }
    }
}

@Composable
private fun UpdateOdometerDialog(
    currentOdometer: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var odometerText by remember { mutableStateOf(if (currentOdometer > 0) currentOdometer.toString() else "") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = DiLinkSurface) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Update Odometer", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = {
                        odometerText = it.filter { c -> c.isDigit() }
                        isError = false
                    },
                    label = { Text("Current Odometer (km)") },
                    placeholder = { Text("e.g. 15000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Must be >= current value") }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiLinkCyan,
                        cursorColor = DiLinkCyan,
                        focusedLabelColor = DiLinkCyan,
                        unfocusedTextColor = DiLinkTextPrimary,
                        focusedTextColor = DiLinkTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceElevated)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val value = odometerText.toIntOrNull()
                            if (value != null && value >= currentOdometer) {
                                onConfirm(value)
                            } else {
                                isError = true
                            }
                        },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiLinkCyan)
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelLarge.copy(color = DiLinkBackground))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SummaryCardPreview() {
    DiLinkTheme {
        SummaryCard(attentionCount = 3, overdueCount = 1, currentOdometer = 45320, onUpdateOdometer = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SummaryCardOkPreview() {
    DiLinkTheme {
        SummaryCard(attentionCount = 0, overdueCount = 0, currentOdometer = 12500, onUpdateOdometer = {})
    }
}
