package com.byd.dilink.maintenance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MaintenanceAmber
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.maintenance.viewmodel.MaintenanceViewModel

private val AVAILABLE_ICONS = listOf(
    "oil_barrel", "filter_alt", "air", "disc_full", "thermostat",
    "settings", "electric_bolt", "tire_repair", "battery_full",
    "battery_charging_full", "water", "water_drop", "straighten",
    "directions_car", "build", "ac_unit"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("build") }
    var intervalKmText by remember { mutableStateOf("") }
    var intervalMonthsText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DiLinkCyan,
        cursorColor = DiLinkCyan,
        focusedLabelColor = DiLinkCyan,
        unfocusedTextColor = DiLinkTextPrimary,
        focusedTextColor = DiLinkTextPrimary,
        unfocusedLabelColor = DiLinkTextMuted,
        unfocusedPlaceholderColor = DiLinkTextMuted,
        focusedPlaceholderColor = DiLinkTextMuted
    )

    Scaffold(
        containerColor = DiLinkBackground,
        topBar = { TopBarWithBack(title = "Add Category", onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Category Name *") },
                placeholder = { Text("e.g. Timing Belt") },
                singleLine = true,
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Name is required", color = StatusRed) }
                } else null,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Icon selector
            Text(
                text = "Icon",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = DiLinkTextSecondary
                )
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AVAILABLE_ICONS.forEach { iconName ->
                    FilterChip(
                        selected = selectedIcon == iconName,
                        onClick = { selectedIcon = iconName },
                        label = {
                            Icon(
                                imageVector = iconForName(iconName),
                                contentDescription = iconName,
                                modifier = Modifier.size(24.dp),
                                tint = if (selectedIcon == iconName) MaintenanceAmber else DiLinkTextMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaintenanceAmber.copy(alpha = 0.2f),
                            containerColor = DiLinkSurfaceVariant
                        ),
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            // Intervals
            Text(
                text = "Service Intervals (optional)",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = DiLinkTextSecondary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = intervalKmText,
                    onValueChange = { intervalKmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Every (km)") },
                    placeholder = { Text("e.g. 10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = intervalMonthsText,
                    onValueChange = { intervalMonthsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Every (months)") },
                    placeholder = { Text("e.g. 12") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.weight(1f)
                )
            }

            // Preview
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = iconForName(selectedIcon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaintenanceAmber
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name.ifBlank { "New Category" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    val parts = mutableListOf<String>()
                    intervalKmText.toIntOrNull()?.let { parts.add("${it} km") }
                    intervalMonthsText.toIntOrNull()?.let { parts.add("${it} months") }
                    if (parts.isNotEmpty()) {
                        Text(
                            text = "Every ${parts.joinToString(" or ")}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DiLinkTextMuted
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    viewModel.addCategory(
                        name = name.trim(),
                        iconName = selectedIcon,
                        intervalKm = intervalKmText.toIntOrNull(),
                        intervalMonths = intervalMonthsText.toIntOrNull()
                    )
                    onSaved()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaintenanceAmber)
            ) {
                Text(
                    "Add Category",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = DiLinkBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun AddCategoryScreenPreview() {
    DiLinkTheme {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Add Category Preview",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
