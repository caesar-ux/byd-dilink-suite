package com.byd.dilink.extras.hazard.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.ui.components.ConfirmDialog
import com.byd.dilink.extras.core.ui.components.SectionCard
import com.byd.dilink.extras.core.ui.components.TopBarWithBack
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.core.ui.theme.StatusRed
import com.byd.dilink.extras.hazard.viewmodel.HazardViewModel
import kotlinx.coroutines.launch

@Composable
fun HazardSettingsScreen(
    onBack: () -> Unit,
    viewModel: HazardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }
    var showExpiryDropdown by remember { mutableStateOf(false) }

    val warningDistanceOptions = listOf(200, 500, 1000)
    val warningDistanceLabels = listOf("200m", "500m", "1km")
    val expiryOptions = listOf(30, 60, 90, 0)
    val expiryLabels = listOf("30 days", "60 days", "90 days", "Never")

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Hazard Settings", onBack = onBack)
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

            // Warning Distance
            SectionCard(title = "Warning Distance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    warningDistanceOptions.forEachIndexed { index, distance ->
                        val isSelected = state.warningDistanceMeters == distance
                        Button(
                            onClick = {
                                scope.launch { viewModel.updateWarningDistance(distance) }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (isSelected) {
                                ButtonDefaults.buttonColors(containerColor = DiLinkCyan)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                            }
                        ) {
                            Text(
                                text = warningDistanceLabels[index],
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) DiLinkBackground else DiLinkTextSecondary
                            )
                        }
                    }
                }
            }

            // Warning Sound
            SectionCard(title = "Warning Sound") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sound enabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DiLinkTextPrimary
                    )
                    Switch(
                        checked = state.warningSoundEnabled,
                        onCheckedChange = {
                            scope.launch { viewModel.updateWarningSound(it) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DiLinkCyan,
                            checkedTrackColor = DiLinkCyan.copy(alpha = 0.3f)
                        )
                    )
                }

                if (state.warningSoundEnabled) {
                    Text(
                        text = "Volume: ${(state.warningVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = DiLinkTextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Slider(
                        value = state.warningVolume,
                        onValueChange = {
                            scope.launch { viewModel.updateWarningVolume(it) }
                        },
                        valueRange = 0f..1f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = DiLinkCyan,
                            activeTrackColor = DiLinkCyan
                        )
                    )
                }
            }

            // Auto Record
            SectionCard(title = "Auto Record") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Start recording when speed > 10 km/h",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DiLinkTextPrimary
                        )
                    }
                    Switch(
                        checked = state.autoRecord,
                        onCheckedChange = {
                            scope.launch { viewModel.updateAutoRecord(it) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DiLinkCyan,
                            checkedTrackColor = DiLinkCyan.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Hazard Expiry
            SectionCard(title = "Hazard Expiry") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    expiryOptions.forEachIndexed { index, days ->
                        val currentExpiry = 0 // from prefs - simplified
                        Button(
                            onClick = {
                                scope.launch { viewModel.updateHazardExpiry(days) }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                        ) {
                            Text(
                                text = expiryLabels[index],
                                style = MaterialTheme.typography.labelSmall,
                                color = DiLinkTextSecondary
                            )
                        }
                    }
                }
            }

            // Import / Export
            SectionCard(title = "Data") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val file = viewModel.exportToJson()
                            if (file != null) {
                                Toast.makeText(context, "Exported to: ${file.name}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "No hazards to export", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Place hazards JSON in app documents folder", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Hazards", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearConfirm) {
        ConfirmDialog(
            title = "Clear All Hazards?",
            message = "This will permanently delete all saved hazards. This action cannot be undone.",
            confirmText = "Clear All",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteAllHazards()
                showClearConfirm = false
                Toast.makeText(context, "All hazards cleared", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showClearConfirm = false }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HazardSettingsScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
        ) {
            TopBarWithBack(title = "Hazard Settings", onBack = {})
            Text(
                text = "Settings content here",
                color = DiLinkTextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
