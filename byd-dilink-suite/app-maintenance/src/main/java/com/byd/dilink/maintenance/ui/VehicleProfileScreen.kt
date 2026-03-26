package com.byd.dilink.maintenance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.SectionCard
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MaintenanceAmber
import com.byd.dilink.core.data.entities.VehicleProfile
import com.byd.dilink.maintenance.viewmodel.MaintenanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileScreen(
    onBack: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val vehicleProfile by viewModel.vehicleProfile.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    val totalServices by viewModel.totalServices.collectAsState()

    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var odometerText by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(vehicleProfile) {
        vehicleProfile?.let { profile ->
            if (!initialized) {
                name = profile.name
                year = profile.year.toString()
                odometerText = if (profile.currentOdometerKm > 0) profile.currentOdometerKm.toString() else ""
                vin = profile.vin ?: ""
                licensePlate = profile.licensePlate ?: ""
                purchaseDate = profile.purchaseDate
                initialized = true
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DiLinkCyan, cursorColor = DiLinkCyan, focusedLabelColor = DiLinkCyan,
        unfocusedTextColor = DiLinkTextPrimary, focusedTextColor = DiLinkTextPrimary, unfocusedLabelColor = DiLinkTextMuted
    )

    fun saveProfile() {
        vehicleProfile?.let { current ->
            viewModel.updateVehicleProfile(
                current.copy(
                    name = name.ifBlank { "BYD Destroyer 05" },
                    year = year.toIntOrNull() ?: 2025,
                    currentOdometerKm = odometerText.toIntOrNull() ?: current.currentOdometerKm,
                    lastOdometerUpdate = if (odometerText.toIntOrNull() != null && odometerText.toIntOrNull() != current.currentOdometerKm) {
                        System.currentTimeMillis()
                    } else current.lastOdometerUpdate,
                    vin = vin.ifBlank { null },
                    licensePlate = licensePlate.ifBlank { null },
                    purchaseDate = purchaseDate
                )
            )
        }
    }

    Scaffold(
        containerColor = DiLinkBackground,
        topBar = { TopBarWithBack(title = "Vehicle Profile", onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Vehicle Name") },
                placeholder = { Text("BYD Destroyer 05") }, singleLine = true, colors = textFieldColors,
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused && initialized) saveProfile() }
            )

            OutlinedTextField(
                value = year, onValueChange = { year = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Year") }, placeholder = { Text("2025") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, colors = textFieldColors,
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused && initialized) saveProfile() }
            )

            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = DiLinkSurfaceElevated) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Current Odometer", style = MaterialTheme.typography.labelMedium.copy(color = DiLinkTextMuted))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = numberFormat.format(odometerText.toIntOrNull() ?: 0),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = DiLinkCyan
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text("km", style = MaterialTheme.typography.titleLarge.copy(color = DiLinkTextMuted))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = odometerText, onValueChange = { odometerText = it.filter { c -> c.isDigit() } },
                        label = { Text("Edit Odometer") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused && initialized) saveProfile() }
                    )
                }
            }

            OutlinedTextField(
                value = vin, onValueChange = { vin = it.uppercase().take(17) },
                label = { Text("VIN (optional)") }, singleLine = true, colors = textFieldColors,
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused && initialized) saveProfile() }
            )

            OutlinedTextField(
                value = licensePlate, onValueChange = { licensePlate = it },
                label = { Text("License Plate (optional)") }, singleLine = true, colors = textFieldColors,
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused && initialized) saveProfile() }
            )

            OutlinedTextField(
                value = purchaseDate?.let { dateFormat.format(Date(it)) } ?: "",
                onValueChange = {}, readOnly = true, label = { Text("Purchase Date (optional)") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Pick Date", tint = DiLinkCyan)
                    }
                },
                colors = textFieldColors, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionCard(modifier = Modifier.weight(1f)) {
                    Text(
                        "$totalServices",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaintenanceAmber
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Services Logged", style = MaterialTheme.typography.labelMedium.copy(color = DiLinkTextMuted))
                }
                SectionCard(modifier = Modifier.weight(1f)) {
                    Text(
                        "¥${numberFormat.format(totalCost)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaintenanceAmber, fontSize = 26.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Total Cost", style = MaterialTheme.typography.labelMedium.copy(color = DiLinkTextMuted))
                }
            }

            purchaseDate?.let { pd ->
                val daysSincePurchase = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - pd)
                val years = daysSincePurchase / 365
                val months = (daysSincePurchase % 365) / 30
                SectionCard {
                    Text(
                        if (years > 0) "${years}y ${months}m" else "${months}m ${daysSincePurchase % 30}d",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaintenanceAmber
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Time Since Purchase", style = MaterialTheme.typography.labelMedium.copy(color = DiLinkTextMuted))
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDate ?: System.currentTimeMillis())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { purchaseDate = it; saveProfile() }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun VehicleProfileScreenPreview() {
    DiLinkTheme {
        Column(Modifier.padding(16.dp)) { Text("Vehicle Profile Preview", style = MaterialTheme.typography.headlineSmall) }
    }
}
