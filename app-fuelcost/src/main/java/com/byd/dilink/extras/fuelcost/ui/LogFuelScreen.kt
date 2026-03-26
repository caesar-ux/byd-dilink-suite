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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.byd.dilink.extras.core.data.entities.FuelRecord
import com.byd.dilink.extras.core.ui.components.SectionCard
import com.byd.dilink.extras.core.ui.components.TopBarWithBack
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.core.ui.theme.StatusOrange
import com.byd.dilink.extras.fuelcost.viewmodel.FuelCostViewModel
import com.byd.dilink.extras.fuelcost.viewmodel.LogFormViewModel

@Composable
fun LogFuelScreen(
    onBack: () -> Unit,
    fuelCostViewModel: FuelCostViewModel = hiltViewModel(),
    logFormViewModel: LogFormViewModel = hiltViewModel()
) {
    val formState by logFormViewModel.fuelForm.collectAsState()
    val dashState by fuelCostViewModel.dashboardState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Log Fuel Purchase", onBack = onBack)
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
            FuelTextField(
                value = formState.odometerKm,
                onValueChange = logFormViewModel::updateFuelOdometer,
                label = "Odometer (km)",
                keyboardType = KeyboardType.Number,
                isRequired = true
            )

            // Liters
            FuelTextField(
                value = formState.liters,
                onValueChange = logFormViewModel::updateFuelLiters,
                label = "Liters Filled",
                keyboardType = KeyboardType.Decimal,
                isRequired = true
            )

            // Total Cost
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FuelTextField(
                    value = formState.totalCost,
                    onValueChange = logFormViewModel::updateFuelTotalCost,
                    label = "Total Cost (${dashState.currency})",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FuelTextField(
                    value = formState.pricePerLiter,
                    onValueChange = logFormViewModel::updateFuelPricePerLiter,
                    label = "Price/Liter",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            if (formState.isAutoCalcCost) {
                Text(
                    text = "Total cost auto-calculated",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiLinkCyan
                )
            }
            if (formState.isAutoCalcPrice) {
                Text(
                    text = "Price/liter auto-calculated",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiLinkCyan
                )
            }

            // Fuel type
            Text(
                text = "Fuel Type",
                style = MaterialTheme.typography.bodyMedium,
                color = DiLinkTextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Regular", "Premium").forEach { type ->
                    FilterChip(
                        selected = formState.fuelType == type,
                        onClick = { logFormViewModel.updateFuelType(type) },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DiLinkSurfaceVariant,
                            selectedContainerColor = StatusOrange.copy(alpha = 0.2f),
                            labelColor = DiLinkTextSecondary,
                            selectedLabelColor = StatusOrange
                        )
                    )
                }
            }

            // Full tank toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Full Tank?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DiLinkTextPrimary
                )
                Switch(
                    checked = formState.isFullTank,
                    onCheckedChange = logFormViewModel::updateFullTank,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DiLinkCyan,
                        checkedTrackColor = DiLinkCyan.copy(alpha = 0.3f)
                    )
                )
            }

            // Station name
            FuelTextField(
                value = formState.stationName,
                onValueChange = logFormViewModel::updateFuelStation,
                label = "Station Name (optional)"
            )

            // Notes
            FuelTextField(
                value = formState.notes,
                onValueChange = logFormViewModel::updateFuelNotes,
                label = "Notes (optional)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val odometer = formState.odometerKm.toIntOrNull()
                    val liters = formState.liters.toDoubleOrNull()
                    val totalCost = formState.totalCost.toDoubleOrNull()
                    val pricePerLiter = formState.pricePerLiter.toDoubleOrNull()

                    if (odometer == null || liters == null) {
                        Toast.makeText(context, "Odometer and liters are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val finalCost = totalCost ?: 0.0
                    val finalPrice = pricePerLiter ?: if (liters > 0 && finalCost > 0) finalCost / liters else dashState.defaultFuelPrice

                    val record = FuelRecord(
                        date = formState.date,
                        odometerKm = odometer,
                        liters = liters,
                        totalCostIqd = finalCost,
                        pricePerLiter = finalPrice,
                        fuelType = formState.fuelType,
                        isFullTank = formState.isFullTank,
                        stationName = formState.stationName.ifBlank { null },
                        notes = formState.notes.ifBlank { null }
                    )

                    fuelCostViewModel.insertFuelRecord(record)
                    logFormViewModel.resetFuelForm()
                    Toast.makeText(context, "Fuel record saved", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusOrange)
            ) {
                Text(
                    text = "Save Fuel Record",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FuelTextField(
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
                color = if (isRequired) StatusOrange else DiLinkTextMuted
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
private fun LogFuelScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
        ) {
            TopBarWithBack(title = "Log Fuel Purchase", onBack = {})
            Column(modifier = Modifier.padding(16.dp)) {
                FuelTextField(value = "", onValueChange = {}, label = "Odometer (km) *")
                Spacer(modifier = Modifier.height(8.dp))
                FuelTextField(value = "", onValueChange = {}, label = "Liters Filled *")
            }
        }
    }
}
