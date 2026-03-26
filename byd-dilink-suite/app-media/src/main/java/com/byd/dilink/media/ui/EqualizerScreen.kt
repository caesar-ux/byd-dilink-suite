package com.byd.dilink.media.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MediaPurple
import com.byd.dilink.media.data.EqPreset
import com.byd.dilink.media.viewmodel.EqualizerViewModel
import com.byd.dilink.media.viewmodel.MediaViewModel

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    equalizerViewModel: EqualizerViewModel = hiltViewModel(),
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val bandLevels by equalizerViewModel.bandLevels.collectAsState()
    val currentPreset by equalizerViewModel.currentPreset.collectAsState()
    val bassBoostEnabled by equalizerViewModel.bassBoostEnabled.collectAsState()
    val bassBoostStrength by equalizerViewModel.bassBoostStrength.collectAsState()
    val virtualizerEnabled by equalizerViewModel.virtualizerEnabled.collectAsState()
    val virtualizerStrength by equalizerViewModel.virtualizerStrength.collectAsState()
    val eqEnabled by equalizerViewModel.eqEnabled.collectAsState()

    // Attach to audio session when available
    LaunchedEffect(Unit) {
        val sessionId = mediaViewModel.getAudioSessionId()
        if (sessionId > 0) {
            equalizerViewModel.attachToAudioSession(sessionId)
        }
    }

    Scaffold(
        containerColor = DiLinkBackground,
        topBar = {
            TopBarWithBack(
                title = "Equalizer",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { equalizerViewModel.saveCustomPreset() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save Custom",
                            tint = DiLinkCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
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

            // EQ Enable toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = DiLinkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = eqEnabled,
                        onCheckedChange = { equalizerViewModel.toggleEq() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DiLinkCyan,
                            checkedTrackColor = DiLinkCyan.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Preset chips
            Text(
                text = "Presets",
                style = MaterialTheme.typography.titleSmall.copy(color = DiLinkTextSecondary)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row of presets
                val firstRow = EqPreset.ALL_PRESETS.take(4)
                firstRow.forEach { preset ->
                    FilterChip(
                        selected = currentPreset.name == preset.name,
                        onClick = { equalizerViewModel.applyPreset(preset) },
                        label = {
                            Text(
                                preset.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 14.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MediaPurple.copy(alpha = 0.3f),
                            selectedLabelColor = MediaPurple,
                            containerColor = DiLinkSurfaceVariant,
                            labelColor = DiLinkTextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val secondRow = EqPreset.ALL_PRESETS.drop(4)
                secondRow.forEach { preset ->
                    FilterChip(
                        selected = currentPreset.name == preset.name,
                        onClick = { equalizerViewModel.applyPreset(preset) },
                        label = {
                            Text(
                                preset.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 14.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MediaPurple.copy(alpha = 0.3f),
                            selectedLabelColor = MediaPurple,
                            containerColor = DiLinkSurfaceVariant,
                            labelColor = DiLinkTextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if needed
                if (EqPreset.ALL_PRESETS.drop(4).size < 4) {
                    repeat(4 - EqPreset.ALL_PRESETS.drop(4).size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Band sliders (vertical representation using horizontal sliders)
            Text(
                text = "Frequency Bands",
                style = MaterialTheme.typography.titleSmall.copy(color = DiLinkTextSecondary)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = DiLinkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    bandLevels.forEachIndexed { index, level ->
                        BandSlider(
                            frequency = EqPreset.BAND_FREQUENCIES.getOrElse(index) { "Band $index" },
                            level = level,
                            onLevelChange = { newLevel ->
                                equalizerViewModel.setBandLevel(index, newLevel)
                            },
                            enabled = eqEnabled
                        )
                        if (index < bandLevels.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Bass Boost
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = DiLinkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bass Boost", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = bassBoostEnabled,
                            onCheckedChange = { equalizerViewModel.toggleBassBoost() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MediaPurple,
                                checkedTrackColor = MediaPurple.copy(alpha = 0.3f)
                            )
                        )
                    }
                    if (bassBoostEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = bassBoostStrength / 1000f,
                            onValueChange = {
                                equalizerViewModel.setBassBoostStrength((it * 1000).toInt())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MediaPurple,
                                activeTrackColor = MediaPurple,
                                inactiveTrackColor = DiLinkSurfaceVariant
                            )
                        )
                        Text(
                            text = "Strength: ${bassBoostStrength / 10}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DiLinkTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            // Virtualizer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = DiLinkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Virtualizer", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = virtualizerEnabled,
                            onCheckedChange = { equalizerViewModel.toggleVirtualizer() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MediaPurple,
                                checkedTrackColor = MediaPurple.copy(alpha = 0.3f)
                            )
                        )
                    }
                    if (virtualizerEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = virtualizerStrength / 1000f,
                            onValueChange = {
                                equalizerViewModel.setVirtualizerStrength((it * 1000).toInt())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MediaPurple,
                                activeTrackColor = MediaPurple,
                                inactiveTrackColor = DiLinkSurfaceVariant
                            )
                        )
                        Text(
                            text = "Strength: ${virtualizerStrength / 10}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DiLinkTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BandSlider(
    frequency: String,
    level: Int,
    onLevelChange: (Int) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = frequency,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = if (enabled) DiLinkTextSecondary else DiLinkTextMuted,
                fontSize = 13.sp
            ),
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.width(8.dp))
        Slider(
            value = (level + 1500f) / 3000f, // -1500 to +1500 -> 0 to 1
            onValueChange = { fraction ->
                val newLevel = ((fraction * 3000f) - 1500f).toInt()
                onLevelChange(newLevel)
            },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) MediaPurple else DiLinkTextMuted,
                activeTrackColor = if (enabled) MediaPurple else DiLinkTextMuted,
                inactiveTrackColor = DiLinkSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${if (level >= 0) "+" else ""}${level / 100}dB",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = if (enabled) DiLinkTextSecondary else DiLinkTextMuted
            ),
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun BandSliderPreview() {
    DiLinkTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BandSlider(
                frequency = "60Hz",
                level = 400,
                onLevelChange = {},
                enabled = true
            )
            BandSlider(
                frequency = "910Hz",
                level = -200,
                onLevelChange = {},
                enabled = true
            )
        }
    }
}
