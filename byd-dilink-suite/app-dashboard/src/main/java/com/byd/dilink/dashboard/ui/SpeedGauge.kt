package com.byd.dilink.dashboard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.dilink.core.ui.theme.DashboardGreen
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.core.ui.theme.StatusYellow

@Composable
fun SpeedGauge(
    speed: Double,
    unit: String,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val speedColor by animateColorAsState(
        targetValue = when {
            speed < 60.0 -> DashboardGreen
            speed < 120.0 -> DiLinkTextPrimary
            speed < 140.0 -> StatusYellow
            else -> StatusRed
        },
        animationSpec = tween(durationMillis = 300),
        label = "speed_color"
    )

    val fontSize = if (isLandscape) 80.sp else 96.sp
    val speedText = if (speed < 1.0) "0" else speed.toInt().toString()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = speedText,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = speedColor,
                shadow = Shadow(
                    color = speedColor.copy(alpha = 0.4f),
                    offset = Offset(0f, 0f),
                    blurRadius = 24f
                )
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.titleMedium,
            color = DiLinkTextSecondary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun SpeedGaugeLandscapePreview() {
    DiLinkTheme {
        SpeedGauge(speed = 87.0, unit = "km/h", isLandscape = true)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun SpeedGaugePortraitPreview() {
    DiLinkTheme {
        SpeedGauge(speed = 135.0, unit = "km/h", isLandscape = false)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun SpeedGaugeHighSpeedPreview() {
    DiLinkTheme {
        SpeedGauge(speed = 165.0, unit = "km/h", isLandscape = true)
    }
}
