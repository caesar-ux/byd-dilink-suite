package com.byd.dilink.extras.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme

@Composable
fun AdaptiveLayout(
    modifier: Modifier = Modifier,
    portraitContent: @Composable () -> Unit,
    landscapeContent: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Box(modifier = modifier) {
        if (isLandscape) {
            landscapeContent()
        } else {
            portraitContent()
        }
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 800, heightDp = 480)
@Composable
private fun AdaptiveLayoutLandscapePreview() {
    DiLinkExtrasTheme {
        AdaptiveLayout(
            portraitContent = { /* portrait content */ },
            landscapeContent = { /* landscape content */ }
        )
    }
}
