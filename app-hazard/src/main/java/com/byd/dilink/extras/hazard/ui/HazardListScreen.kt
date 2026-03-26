package com.byd.dilink.extras.hazard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.ui.components.ConfirmDialog
import com.byd.dilink.extras.core.ui.components.TopBarWithBack
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurface
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.core.ui.theme.StatusRed
import com.byd.dilink.extras.hazard.model.HazardType
import com.byd.dilink.extras.hazard.viewmodel.HazardViewModel
import com.byd.dilink.extras.hazard.viewmodel.HazardWithDistance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HazardListScreen(
    onBack: () -> Unit,
    viewModel: HazardViewModel = hiltViewModel()
) {
    val hazardsWithDistance by viewModel.allHazardsWithDistance.collectAsState()
    val selectedTypes = remember { mutableStateListOf<HazardType>() }
    var selectedHazard by remember { mutableStateOf<HazardWithDistance?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }

    val filteredHazards = if (selectedTypes.isEmpty()) {
        hazardsWithDistance
    } else {
        hazardsWithDistance.filter { hwd ->
            HazardType.fromName(hwd.record.type) in selectedTypes
        }
    }

    Scaffold(
        topBar = {
            TopBarWithBack(title = "All Hazards", onBack = onBack)
        },
        containerColor = DiLinkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HazardType.entries.forEach { type ->
                    val hazardColor = Color(type.colorLong)
                    val isSelected = type in selectedTypes

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedTypes.remove(type)
                            else selectedTypes.add(type)
                        },
                        label = {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = hazardTypeIcon(type),
                                contentDescription = null,
                                tint = hazardColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DiLinkSurfaceVariant,
                            selectedContainerColor = hazardColor.copy(alpha = 0.2f),
                            labelColor = DiLinkTextSecondary,
                            selectedLabelColor = hazardColor
                        )
                    )
                }
            }

            // Hazard list
            if (filteredHazards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hazards recorded yet.\nStart recording and tap hazard buttons while driving.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DiLinkTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = filteredHazards,
                        key = { it.record.id }
                    ) { hazardWithDist ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    showDeleteConfirm = hazardWithDist.record.id
                                }
                                false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(StatusRed.copy(alpha = 0.2f))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = StatusRed
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true
                        ) {
                            HazardListItem(
                                hazardWithDistance = hazardWithDist,
                                onClick = { selectedHazard = hazardWithDist }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail bottom sheet
    selectedHazard?.let { hazard ->
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { selectedHazard = null },
            sheetState = sheetState,
            containerColor = DiLinkSurfaceElevated
        ) {
            HazardDetailSheet(
                hazardWithDistance = hazard,
                onDelete = {
                    showDeleteConfirm = hazard.record.id
                    selectedHazard = null
                },
                onDismiss = { selectedHazard = null }
            )
        }
    }

    // Delete confirmation
    showDeleteConfirm?.let { id ->
        ConfirmDialog(
            title = "Delete Hazard",
            message = "Remove this hazard permanently?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteHazard(id)
                showDeleteConfirm = null
            },
            onDismiss = { showDeleteConfirm = null }
        )
    }
}

@Composable
private fun HazardListItem(
    hazardWithDistance: HazardWithDistance,
    onClick: () -> Unit
) {
    val type = HazardType.fromName(hazardWithDistance.record.type)
    val hazardColor = Color(type.colorLong)
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 2.dp),
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
                contentDescription = type.label,
                tint = hazardColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DiLinkTextPrimary
                )
                Row {
                    Text(
                        text = formatDistance(hazardWithDistance.distanceMeters),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = hazardColor
                    )
                    Text(
                        text = " ${hazardWithDistance.direction}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DiLinkTextSecondary
                    )
                    Text(
                        text = " · ${dateFormat.format(Date(hazardWithDistance.record.timestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DiLinkTextMuted
                    )
                }
                hazardWithDistance.record.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = DiLinkTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HazardDetailSheet(
    hazardWithDistance: HazardWithDistance,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val record = hazardWithDistance.record
    val type = HazardType.fromName(record.type)
    val hazardColor = Color(type.colorLong)
    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = hazardTypeIcon(type),
                    contentDescription = null,
                    tint = hazardColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DiLinkTextPrimary
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, "Close", tint = DiLinkTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow("Distance", formatDistance(hazardWithDistance.distanceMeters) + " ${hazardWithDistance.direction}")
        DetailRow("Time", dateFormat.format(Date(record.timestamp)))
        DetailRow("GPS", String.format("%.6f, %.6f", record.latitude, record.longitude))
        DetailRow("Speed", "${record.speed.toInt()} km/h when reported")
        DetailRow("Heading", "${record.heading.toInt()}°")
        DetailRow("Confirmed", "${record.confirmed}× reported")

        record.notes?.let {
            if (it.isNotBlank()) {
                DetailRow("Notes", it)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Hazard", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DiLinkTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = DiLinkTextPrimary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HazardListScreenPreview() {
    DiLinkExtrasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DiLinkBackground)
        ) {
            TopBarWithBack(title = "All Hazards", onBack = {})
            Text(
                text = "No hazards recorded yet.",
                color = DiLinkTextMuted,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}
