package com.byd.dilink.parking.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.data.entities.ParkingRecord
import com.byd.dilink.core.ui.components.ConfirmDialog
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.parking.viewmodel.ParkingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: ParkingViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState()
    var deleteTarget by remember { mutableStateOf<ParkingRecord?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "Delete Record?",
            message = "Remove this parking record from history?",
            confirmText = "Delete",
            cancelText = "Keep",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteHistoryRecord(target)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    if (showClearAllDialog) {
        ConfirmDialog(
            title = "Clear All History?",
            message = "This will permanently remove all parking history records. This cannot be undone.",
            confirmText = "Clear All",
            cancelText = "Cancel",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllHistory()
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Parking History", onBack = onBack)
        },
        containerColor = DiLinkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (history.isNotEmpty()) {
                Button(
                    onClick = { showClearAllDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All History", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = DiLinkTextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No parking history",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DiLinkTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(history, key = { it.id }) { record ->
                        HistoryItem(
                            record = record,
                            onDelete = { deleteTarget = record }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    record: ParkingRecord,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val parkedDate = dateFormat.format(Date(record.parkedAt))

    val durationMs = (record.clearedAt ?: record.parkedAt) - record.parkedAt
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    val durationText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DiLinkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = DiLinkTextMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parkedDate,
                    style = MaterialTheme.typography.titleSmall,
                    color = DiLinkTextPrimary
                )
                Row {
                    Text(
                        text = "Duration: $durationText",
                        style = MaterialTheme.typography.bodySmall,
                        color = DiLinkTextSecondary
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.6f, %.6f", record.latitude, record.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = DiLinkTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = StatusRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun HistoryItemPreview() {
    DiLinkTheme {
        HistoryItem(
            record = ParkingRecord(
                id = 1,
                latitude = 23.123456,
                longitude = 113.654321,
                address = null,
                parkedAt = System.currentTimeMillis() - 7200000,
                clearedAt = System.currentTimeMillis() - 3600000,
                timerDurationMin = null,
                notes = null
            ),
            onDelete = {}
        )
    }
}
