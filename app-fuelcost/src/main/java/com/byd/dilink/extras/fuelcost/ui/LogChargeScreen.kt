package com.byd.dilink.extras.fuelcost.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.data.entities.ChargeRecord
import com.byd.dilink.extras.core.ui.components.TopBarWithBack
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.fuelcost.viewmodel.FuelCostViewModel
import com.byd.dilink.extras.fuelcost.viewmodel.LogFormViewModel

@Composable
fun LogChargeScreen(
    onBack: () -> Unit,
    fuelCostViewModel: FuelCostViewModel = hiltViewModel(),
    logFormViewModel: LogFormViewModel = hiltViewModel()
) {
    val formState by logFormViewModel.chargeForm.collectAsState()
    val dashState by fuelCostViewModel.dashboardState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Log Charging Session", onBack = onBack)
        },
        containerColor = DiLinkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Odometer
            ChargeTextField(
                value = formState.odometerKm,
                onValueChange = logFormViewModel::updateChargeOdometer,
                label = "Odometer (km)",
                keyboardType = KeyboardType.Number,
                isRequired = true
            )

            // kWh Charged
            ChargeTextField(
                value = formState.kwhCharged,
                onValueChange = logFormViewModel::updateChargeKwh,
                label = "kWh Charged",
                keyboardType = KeyboardType.Decimal
            )

            // SOC Auto-calc
            Text(
                text = "Or calculate from SOC %",
                style = MaterialTheme.typography.bodySmall,
                color = DiLinkTextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChargeTextField(
                    value = formState.startSocPercent,
                    onValueChange = logFormViewModel::updateChargeStartSoc,
                    label = "Start SOC %",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                ChargeTextField(
                    value = formState.endSocPercent,
                    onValueChange = logFormViewModel::updateChargeEndSoc,
                    label = "End SOC %",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = {
                    logFormViewModel.autoCalculateKwhFromSoc(dashState.batteryCapacityKwh)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate kWh (${dashState.batteryCapacityKwh} kWh battery)")
            }

            if (formState.isAutoCalcKwh) {
                Text(
                    text = "kWh auto-calculated from SOC",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiLinkCyan
                )
            }

            // Cost fields
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChargeTextField(
                    value = formState.totalCost,
                    onValueChange = logFormViewModel::updateChargeTotalCost,
                    label = "Total Cost (${dashState.currency})",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                ChargeTextField(
                    value = formState.costPerKwh,
                    onValueChange = logFormViewModel::updateChargeCostPerKwh,
                    label = "Cost/kWh",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            // Source
            Text(
                text = "Charging Source",
                style = MaterialTheme.typography.bodyMedium,
                color = DiLinkTextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Home", "Public", "Free").forEach { source ->
                    FilterChip(
                        selected = formState.source == source,
                        onClick = { logFormViewModel.updateChargeSource(source) },
                        label = { Text(source) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DiLinkSurfaceVariant,
                            selectedContainerColor = DiLinkCyan.copy(alpha = 0.2f),
                            labelColor = DiLinkTextSecondary,
                            selectedLabelColor = DiLinkCyan
                        )
                    )
                }
            }

            // Duration
            ChargeTextField(
                value = formState.durationMin,
                onValueChange = logFormViewModel::updateChargeDuration,
                label = "Duration (minutes, optional)",
                keyboardType = KeyboardType.Number
            )

            // Notes
            ChargeTextField(
                value = formState.notes,
                onValueChange = logFormViewModel::updateChargeNotes,
                label = "Notes (optional)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val odometer = formState.odometerKm.toIntOrNull()
                    val kwh = formState.kwhCharged.toDoubleOrNull()

                    if (odometer == null) {
                        Toast.makeText(context, "Odometer is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (kwh == null || kwh <= 0) {
                        Toast.makeText(context, "kWh charged is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val totalCost = formState.totalCost.toDoubleOrNull() ?: 0.0
                    val costPerKwh = formState.costPerKwh.toDoubleOrNull()
                        ?: if (kwh > 0 && totalCost > 0) totalCost / kwh else dashState.defaultElectricityPrice

                    val record = ChargeRecord(
                        date = formState.date,
                        odometerKm = odometer,
                        kwhCharged = kwh,
                        totalCostIqd = totalCost,
                        costPerKwh = costPerKwh,
                        source = formState.source,
                        startSocPercent = formState.startSocPercent.toIntOrNull(),
                        endSocPercent = formState.endSocPercent.toIntOrNull(),
                        durationMin = formState.durationMin.toIntOrNull(),
                        notes = formState.notes.ifBlank { null }
                    )

                    fuelCostViewModel.insertChargeRecord(record)
                    logFormViewModel.resetChargeForm()
                    Toast.makeText(context, "Charge record saved", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiLinkCyan)
            ) {
                Text(
                    text = "Save Charge Record",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DiLinkBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChargeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isRequired: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = if (isRequired) "$label *" else label,
                color = if (isRequired) DiLinkCyan else DiLinkTextMuted
            )
        },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DiLinkTextPrimary,
            unfocusedTextColor = DiLinkTextPrimary,
            focusedBorderColor = DiLinkCyan,
            unfocusedBorderColor = DiLinkTextMuted,
            focusedLabelColor = DiLinkCyan,
            unfocusedLabelColor = DiLinkTextMuted,
            cursorColor = DiLinkCyan
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun LogChargeScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
        ) {
            TopBarWithBack(title = "Log Charging Session", onBack = {})
            Column(modifier = Modifier.padding(16.dp)) {
                ChargeTextField(value = "", onValueChange = {}, label = "Odometer (km) *")
                Spacer(modifier = Modifier.height(8.dp))
                ChargeTextField(value = "", onValueChange = {}, label = "kWh Charged")
            }
        }
    }
}
