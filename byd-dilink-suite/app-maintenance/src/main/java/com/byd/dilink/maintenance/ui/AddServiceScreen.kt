package com.byd.dilink.maintenance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.maintenance.viewmodel.MaintenanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    preselectedCategoryId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val categoriesWithStatus by viewModel.categoriesWithStatus.collectAsState()
    val vehicleProfile by viewModel.vehicleProfile.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var selectedCategoryId by remember { mutableLongStateOf(preselectedCategoryId ?: -1L) }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var odometerText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var odometerError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf(false) }

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
        topBar = { TopBarWithBack(title = "Log New Service", onBack = onBack) }
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

            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it }
            ) {
                val selectedCategory = categoriesWithStatus.find { it.category.id == selectedCategoryId }
                OutlinedTextField(
                    value = selectedCategory?.category?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    isError = categoryError,
                    supportingText = if (categoryError) { { Text("Please select a category") } } else null,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categoriesWithStatus.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(iconForName(item.category.iconName), null, Modifier.size(24.dp), tint = DiLinkCyan)
                                    Spacer(Modifier.width(12.dp))
                                    Text(item.category.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            },
                            onClick = {
                                selectedCategoryId = item.category.id
                                categoryDropdownExpanded = false
                                categoryError = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = dateFormat.format(Date(dateMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date *") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Pick Date", tint = DiLinkCyan)
                    }
                },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = odometerText,
                onValueChange = {
                    odometerText = it.filter { c -> c.isDigit() }
                    odometerError = null
                },
                label = { Text("Odometer (km) *") },
                placeholder = { Text("Last: ${NumberFormat.getNumberInstance().format(vehicleProfile?.currentOdometerKm ?: 0)} km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = odometerError != null,
                supportingText = odometerError?.let { { Text(it, color = StatusRed) } },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = costText,
                onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Cost (optional)") },
                prefix = { Text("¥ ", style = MaterialTheme.typography.bodyLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop / Mechanic (optional)") },
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                minLines = 3,
                maxLines = 5,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    var hasError = false
                    if (selectedCategoryId == -1L) { categoryError = true; hasError = true }
                    val odometerValue = odometerText.toIntOrNull()
                    if (odometerValue == null) { odometerError = "Enter a valid odometer reading"; hasError = true }
                    else if (odometerValue < 0) { odometerError = "Cannot be negative"; hasError = true }

                    if (!hasError && odometerValue != null) {
                        viewModel.addService(
                            categoryId = selectedCategoryId,
                            datePerformed = dateMillis,
                            odometerKm = odometerValue,
                            cost = costText.toDoubleOrNull(),
                            shopName = shopName.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiLinkCyan)
            ) {
                Text("Save Service Record", style = MaterialTheme.typography.labelLarge.copy(color = DiLinkBackground))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis = it }
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
private fun AddServiceScreenPreview() {
    DiLinkTheme {
        Column(Modifier.padding(16.dp)) { Text("Add Service Preview", style = MaterialTheme.typography.headlineSmall) }
    }
}
