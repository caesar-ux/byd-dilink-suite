package com.byd.dilink.maintenance.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.ConfirmDialog
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MaintenanceAmber
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.core.data.entities.ServiceRecord
import com.byd.dilink.maintenance.viewmodel.CategoryWithStatus
import com.byd.dilink.maintenance.viewmodel.MaintenanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: Long,
    onBack: () -> Unit,
    onNavigateToAddService: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val categoriesWithStatus by viewModel.categoriesWithStatus.collectAsState()
    val services by viewModel.getServicesForCategory(categoryId).collectAsState(initial = emptyList())
    val categoryItem = categoriesWithStatus.find { it.category.id == categoryId }
    var deleteTarget by remember { mutableStateOf<ServiceRecord?>(null) }

    Scaffold(
        containerColor = DiLinkBackground,
        topBar = {
            TopBarWithBack(
                title = categoryItem?.category?.name ?: "Category",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddService,
                containerColor = MaintenanceAmber,
                contentColor = DiLinkBackground,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log New Service", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (categoryItem != null) {
                item { CategoryHeaderCard(categoryItem = categoryItem) }
                item { StatusCard(categoryItem = categoryItem) }
                item {
                    Text(
                        text = "Service History",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (services.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = DiLinkSurfaceElevated
                    ) {
                        Text(
                            text = "No services recorded yet.\nTap + to log your first service.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = DiLinkTextMuted),
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }

            items(services, key = { it.id }) { record ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            deleteTarget = record
                            false
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color by animateColorAsState(
                            targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                StatusRed.copy(alpha = 0.3f)
                            } else DiLinkBackground,
                            label = "swipe_bg"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, MaterialTheme.shapes.medium)
                                .padding(end = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(28.dp))
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    ServiceRecordCard(record = record)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    deleteTarget?.let { record ->
        ConfirmDialog(
            title = "Delete Service Record?",
            message = "This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteService(record)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun CategoryHeaderCard(categoryItem: CategoryWithStatus) {
    val cat = categoryItem.category
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DiLinkSurfaceElevated
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconForName(cat.iconName),
                contentDescription = cat.name,
                modifier = Modifier.size(48.dp),
                tint = MaintenanceAmber
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = cat.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(4.dp))
                val intervalParts = mutableListOf<String>()
                cat.intervalKm?.let { intervalParts.add("${NumberFormat.getNumberInstance().format(it)} km") }
                cat.intervalMonths?.let { intervalParts.add("$it months") }
                if (intervalParts.isNotEmpty()) {
                    Text(
                        text = "Every ${intervalParts.joinToString(" or ")}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = DiLinkTextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(categoryItem: CategoryWithStatus) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DiLinkSurfaceElevated
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(statusColor(categoryItem.status))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = statusLabel(categoryItem.status),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = statusColor(categoryItem.status),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            categoryItem.nextDueKm?.let { km ->
                Text(
                    text = "Next due at ${numberFormat.format(km)} km",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = DiLinkTextPrimary)
                )
            }
            categoryItem.nextDueDate?.let { date ->
                Text(
                    text = "Next due by ${dateFormat.format(Date(date))}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = DiLinkTextPrimary)
                )
            }
            if (categoryItem.nextDueKm == null && categoryItem.nextDueDate == null) {
                Text(
                    text = "No schedule available",
                    style = MaterialTheme.typography.bodyLarge.copy(color = DiLinkTextMuted)
                )
            }
        }
    }
}

@Composable
private fun ServiceRecordCard(record: ServiceRecord) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DiLinkSurfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(record.datePerformed)),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${numberFormat.format(record.odometerKm)} km",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = DiLinkCyan)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                record.shopName?.let { shop ->
                    Text(text = shop, style = MaterialTheme.typography.bodySmall.copy(color = DiLinkTextSecondary, fontSize = 15.sp))
                }
                record.cost?.let { cost ->
                    Text(
                        text = "¥${String.format("%.2f", cost)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaintenanceAmber, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            record.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = notes, style = MaterialTheme.typography.bodySmall.copy(color = DiLinkTextMuted, fontSize = 14.sp), maxLines = 2)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun ServiceRecordCardPreview() {
    DiLinkTheme {
        ServiceRecordCard(
            record = ServiceRecord(
                id = 1, categoryId = 1, datePerformed = System.currentTimeMillis(),
                odometerKm = 15000, cost = 350.0, shopName = "BYD Service Center", notes = "Regular service"
            )
        )
    }
}
