package com.byd.dilink.extras.hazard.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.ui.components.SectionCard
import com.byd.dilink.extras.core.ui.components.TopBarWithBack
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurface
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.hazard.model.HazardType
import com.byd.dilink.extras.hazard.viewmodel.HazardViewModel
import com.byd.dilink.extras.hazard.viewmodel.HazardWithDistance

@Composable
fun RouteHazardsScreen(
    onBack: () -> Unit,
    viewModel: HazardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var fromLat by remember { mutableStateOf("") }
    var fromLon by remember { mutableStateOf("") }
    var toLat by remember { mutableStateOf("") }
    var toLon by remember { mutableStateOf("") }
    var useCurrentAsFrom by remember { mutableStateOf(false) }

    val corridorOptions = listOf(500.0, 1000.0, 2000.0)
    val corridorLabels = listOf("500m", "1km", "2km")
    var selectedCorridorIndex by remember { mutableStateOf(1) }

    var routeHazards by remember { mutableStateOf<List<HazardWithDistance>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Route Hazards", onBack = onBack)
        },
        containerColor = DiLinkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // From location
                SectionCard(title = "From") {
                    OutlinedButton(
                        onClick = {
                            useCurrentAsFrom = true
                            fromLat = String.format("%.6f", state.currentLatitude)
                            fromLon = String.format("%.6f", state.currentLongitude)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Current Location")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fromLat,
                            onValueChange = { fromLat = it; useCurrentAsFrom = false },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
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
                        OutlinedTextField(
                            value = fromLon,
                            onValueChange = { fromLon = it; useCurrentAsFrom = false },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
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
                    }
                }
            }

            item {
                // To location
                SectionCard(title = "To") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = toLat,
                            onValueChange = { toLat = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
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
                        OutlinedTextField(
                            value = toLon,
                            onValueChange = { toLon = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
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
                    }
                }
            }

            item {
                // Corridor width selector
                Text(
                    text = "Corridor Width",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DiLinkTextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    corridorLabels.forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedCorridorIndex == index,
                            onClick = { selectedCorridorIndex = index },
                            label = { Text(label) },
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

            item {
                // Search button
                Button(
                    onClick = {
                        val fLat = fromLat.toDoubleOrNull() ?: return@Button
                        val fLon = fromLon.toDoubleOrNull() ?: return@Button
                        val tLat = toLat.toDoubleOrNull() ?: return@Button
                        val tLon = toLon.toDoubleOrNull() ?: return@Button
                        val corridor = corridorOptions[selectedCorridorIndex]

                        routeHazards = viewModel.getRouteHazards(fLat, fLon, tLat, tLon, corridor)
                        hasSearched = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Route", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (hasSearched) {
                // Summary
                item {
                    SectionCard(title = "Route Summary — ${routeHazards.size} hazards found") {
                        val typeCounts = routeHazards.groupBy {
                            HazardType.fromName(it.record.type)
                        }.mapValues { it.value.size }

                        if (typeCounts.isEmpty()) {
                            Text(
                                text = "No hazards along this corridor.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DiLinkTextMuted
                            )
                        } else {
                            typeCounts.forEach { (type, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = hazardTypeIcon(type),
                                            contentDescription = null,
                                            tint = Color(type.colorLong),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = type.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DiLinkTextPrimary
                                        )
                                    }
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(type.colorLong)
                                    )
                                }
                            }
                        }
                    }
                }

                // Hazard list
                items(routeHazards) { hazard ->
                    val type = HazardType.fromName(hazard.record.type)
                    val hazardColor = Color(type.colorLong)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DiLinkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = hazardTypeIcon(type),
                                contentDescription = null,
                                tint = hazardColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DiLinkTextPrimary
                                )
                                Text(
                                    text = "${formatDistance(hazard.distanceMeters)} from start · ${hazard.direction}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DiLinkTextSecondary
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun RouteHazardsScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
        ) {
            TopBarWithBack(title = "Route Hazards", onBack = {})
            Text(
                text = "Enter route coordinates",
                color = DiLinkTextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
