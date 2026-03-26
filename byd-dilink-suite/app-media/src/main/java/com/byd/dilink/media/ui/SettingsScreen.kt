package com.byd.dilink.media.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MediaPurple
import com.byd.dilink.media.data.MediaFile

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var resumeOnLaunch by remember { mutableStateOf(false) }
    var audioFocusEnabled by remember { mutableStateOf(true) }
    var eqEnabled by remember { mutableStateOf(true) }

    val audioExtensions = remember { MediaFile.AUDIO_EXTENSIONS.toList() }
    val videoExtensions = remember { MediaFile.VIDEO_EXTENSIONS.toList() }
    var enabledAudioExts by remember { mutableStateOf(audioExtensions.toSet()) }
    var enabledVideoExts by remember { mutableStateOf(videoExtensions.toSet()) }

    Scaffold(
        containerColor = DiLinkBackground,
        topBar = {
            TopBarWithBack(title = "Settings", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Default browse path
            SettingItem(
                title = "Default Browse Path",
                subtitle = "/storage/emulated/0/Music"
            ) {
                // Read-only display; could be made interactive with a file browser
            }

            // Resume on launch
            SettingToggle(
                title = "Resume Playback on Launch",
                subtitle = "Continue from last played track when app opens",
                checked = resumeOnLaunch,
                onCheckedChange = { resumeOnLaunch = it }
            )

            // Audio focus
            SettingToggle(
                title = "Audio Focus Handling",
                subtitle = "Pause when navigation speaks or other apps play audio",
                checked = audioFocusEnabled,
                onCheckedChange = { audioFocusEnabled = it }
            )

            // EQ enabled
            SettingToggle(
                title = "Equalizer Enabled",
                subtitle = "Apply equalizer effects to playback",
                checked = eqEnabled,
                onCheckedChange = { eqEnabled = it }
            )

            // Audio file types
            Text(
                text = "Audio File Types",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = DiLinkTextSecondary
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = DiLinkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    audioExtensions.forEach { ext ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = ext in enabledAudioExts,
                                onCheckedChange = { checked ->
                                    enabledAudioExts = if (checked) {
                                        enabledAudioExts + ext
                                    } else {
                                        enabledAudioExts - ext
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MediaPurple,
                                    uncheckedColor = DiLinkTextMuted,
                                    checkmarkColor = DiLinkBackground
                                )
                            )
                            Text(
                                text = ".${ext.uppercase()}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = DiLinkTextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Video file types
            Text(
                text = "Video File Types",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = DiLinkTextSecondary
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = DiLinkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    videoExtensions.forEach { ext ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = ext in enabledVideoExts,
                                onCheckedChange = { checked ->
                                    enabledVideoExts = if (checked) {
                                        enabledVideoExts + ext
                                    } else {
                                        enabledVideoExts - ext
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MediaPurple,
                                    uncheckedColor = DiLinkTextMuted,
                                    checkmarkColor = DiLinkBackground
                                )
                            )
                            Text(
                                text = ".${ext.uppercase()}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = DiLinkTextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DiLinkSurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DiLinkTextMuted
                    )
                )
            }
            trailing()
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DiLinkSurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DiLinkTextMuted
                    )
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DiLinkCyan,
                    checkedTrackColor = DiLinkCyan.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingTogglePreview() {
    DiLinkTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingToggle(
                title = "Resume on Launch",
                subtitle = "Continue from last played track",
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}
