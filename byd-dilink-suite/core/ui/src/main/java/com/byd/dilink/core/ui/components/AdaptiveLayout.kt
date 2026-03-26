package com.byd.dilink.core.ui.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import com.byd.dilink.core.ui.theme.DiLinkTheme

@Composable
fun AdaptiveLayout(
    portraitContent: @Composable () -> Unit,
    landscapeContent: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        landscapeContent()
    } else {
        portraitContent()
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun AdaptiveLayoutLandscapePreview() {
    DiLinkTheme {
        AdaptiveLayout(
            portraitContent = {},
            landscapeContent = {}
        )
    }
}
