package com.byd.dilink.extras.fuelcost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.ui.components.SectionCard
import com.byd.dilink.extras.core.ui.components.TopBarWithBack
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.fuelcost.viewmodel.FuelCostViewModel
import kotlinx.coroutines.launch

@Composable
fun FuelSettingsScreen(
    onBack: () -> Unit,
    viewModel: FuelCostViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    val scope = rememberCoroutineScope()
    var fuelPriceText by remember { mutableStateOf(state.defaultFuelPrice.toString()) }
    var electricPriceText by remember { mutableStateOf(state.defaultElectricityPrice.toString()) }
    var benchmarkText by remember { mutableStateOf(state.benchmarkConsumption.toString()) }

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Fuel Settings", onBack = onBack)
        },
        containerColor = DiLinkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Battery capacity
            SectionCard(title = "Battery Capacity") {
                Text(
                    text = "Select your BYD DM-i variant:",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiLinkTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(8.3, 18.3).forEach { capacity ->
                        val isSelected = state.batteryCapacityKwh == capacity
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    scope.launch { viewModel.updateBatteryCapacity(capacity) }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DiLinkCyan,
                                    unselectedColor = DiLinkTextMuted
                                )
                            )
                            Text(
                                text = "$capacity kWh",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) DiLinkTextPrimary else DiLinkTextSecondary
                            )
                        }
                    }
                }
            }

            // Default fuel price
            SectionCard(title = "Default Fuel Price") {
                OutlinedTextField(
                    value = fuelPriceText,
                    onValueChange = { fuelPriceText = it },
                    label = { Text("IQD per Liter") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                Button(
                    onClick = {
                        fuelPriceText.toDoubleOrNull()?.let {
                            scope.launch { viewModel.updateDefaultFuelPrice(it) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                ) {
                    Text("Save", color = DiLinkTextPrimary)
                }
            }

            // Default electricity price
            SectionCard(title = "Default Electricity Price") {
                OutlinedTextField(
                    value = electricPriceText,
                    onValueChange = { electricPriceText = it },
                    label = { Text("IQD per kWh") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                Button(
                    onClick = {
                        electricPriceText.toDoubleOrNull()?.let {
                            scope.launch { viewModel.updateDefaultElectricityPrice(it) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                ) {
                    Text("Save", color = DiLinkTextPrimary)
                }
            }

            // Benchmark consumption
            SectionCard(title = "Benchmark Consumption") {
                Text(
                    text = "Reference petrol consumption for savings calculation",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiLinkTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = benchmarkText,
                    onValueChange = { benchmarkText = it },
                    label = { Text("L/100km") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                Button(
                    onClick = {
                        benchmarkText.toDoubleOrNull()?.let {
                            scope.launch { viewModel.updateBenchmarkConsumption(it) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                ) {
                    Text("Save", color = DiLinkTextPrimary)
                }
            }

            // Currency
            SectionCard(title = "Currency") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("IQD", "USD").forEach { curr ->
                        FilterChip(
                            selected = state.currency == curr,
                            onClick = {
                                scope.launch { viewModel.updateCurrency(curr) }
                            },
                            label = { Text(curr) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DiLinkSurfaceVariant,
                                selectedContainerColor = DiLinkCyan.copy(alpha = 0.2f),
                                labelColor = DiLinkTextSecondary,
                                selectedLabelColor = DiLinkCyan
                            )
                        )
                    }
                }
            }

            // Distance unit (read-only for now)
            SectionCard(title = "Distance Unit") {
                Text(
                    text = "Kilometers (km)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DiLinkTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun FuelSettingsScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
        ) {
            TopBarWithBack(title = "Fuel Settings", onBack = {})
            Text(
                text = "Settings",
                color = DiLinkTextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
