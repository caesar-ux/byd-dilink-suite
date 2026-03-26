package com.byd.dilink.parking.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.data.entities.FavoriteLocation
import com.byd.dilink.core.ui.components.ConfirmDialog
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurface
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.ParkingBlue
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.parking.viewmodel.ParkingViewModel
import java.util.Locale

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    viewModel: ParkingViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<FavoriteLocation?>(null) }
    val context = LocalContext.current

    if (showAddDialog) {
        AddFavoriteDialog(
            currentLatitude = if (uiState.hasLocation) uiState.currentLatitude else null,
            currentLongitude = if (uiState.hasLocation) uiState.currentLongitude else null,
            onAdd = { name, lat, lon, notes ->
                viewModel.addFavorite(name, lat, lon, notes)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "Delete Favorite?",
            message = "Remove \"${target.name}\" from your favorites?",
            confirmText = "Delete",
            cancelText = "Keep",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteFavorite(target)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Favorites", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ParkingBlue,
                contentColor = DiLinkTextPrimary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Favorite", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = DiLinkBackground
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DiLinkTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No favorites saved",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DiLinkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap + to add a location",
                        style = MaterialTheme.typography.bodySmall,
                        color = DiLinkTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(favorites, key = { it.id }) { favorite ->
                    FavoriteLocationItem(
                        location = favorite,
                        onNavigate = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:${favorite.latitude},${favorite.longitude}?q=${favorite.latitude},${favorite.longitude}(${Uri.encode(favorite.name)})")
                            )
                            context.startActivity(intent)
                        },
                        onDelete = { deleteTarget = favorite }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun FavoriteLocationItem(
    location: FavoriteLocation,
    onNavigate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
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
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = ParkingBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = DiLinkTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = String.format(Locale.US, "%.6f, %.6f", location.latitude, location.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = DiLinkTextMuted
                )
                if (!location.notes.isNullOrBlank()) {
                    Text(
                        text = location.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = DiLinkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onNavigate,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = "Navigate",
                    tint = ParkingBlue,
                    modifier = Modifier.size(28.dp)
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

@Composable
private fun AddFavoriteDialog(
    currentLatitude: Double?,
    currentLongitude: Double?,
    onAdd: (name: String, lat: Double, lon: Double, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf(currentLatitude?.let { String.format(Locale.US, "%.6f", it) } ?: "") }
    var lonText by remember { mutableStateOf(currentLongitude?.let { String.format(Locale.US, "%.6f", it) } ?: "") }
    var notes by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DiLinkCyan,
        unfocusedBorderColor = DiLinkSurfaceVariant,
        focusedTextColor = DiLinkTextPrimary,
        unfocusedTextColor = DiLinkTextPrimary,
        cursorColor = DiLinkCyan,
        focusedLabelColor = DiLinkCyan,
        unfocusedLabelColor = DiLinkTextSecondary
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiLinkSurface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Add Favorite Location",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DiLinkTextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (currentLatitude != null && currentLongitude != null) {
                    Button(
                        onClick = {
                            latText = String.format(Locale.US, "%.6f", currentLatitude)
                            lonText = String.format(Locale.US, "%.6f", currentLongitude)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Current Location", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it },
                        label = { Text("Longitude") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiLinkSurfaceVariant)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val lat = latText.toDoubleOrNull()
                            val lon = lonText.toDoubleOrNull()
                            if (name.isNotBlank() && lat != null && lon != null) {
                                onAdd(name, lat, lon, notes.ifBlank { null })
                            }
                        },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = name.isNotBlank() && latText.toDoubleOrNull() != null && lonText.toDoubleOrNull() != null,
                        colors = ButtonDefaults.buttonColors(containerColor = ParkingBlue)
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun FavoriteLocationItemPreview() {
    DiLinkTheme {
        FavoriteLocationItem(
            location = FavoriteLocation(
                id = 1,
                name = "Home",
                latitude = 23.123456,
                longitude = 113.654321,
                notes = "Underground parking B2"
            ),
            onNavigate = {},
            onDelete = {}
        )
    }
}
