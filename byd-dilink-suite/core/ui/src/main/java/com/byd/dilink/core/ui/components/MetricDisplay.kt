package com.byd.dilink.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme

@Composable
fun MetricDisplay(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = DiLinkTextPrimary,
    valueFontSize: Float = 40f
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = valueFontSize.sp,
                color = valueColor,
                lineHeight = (valueFontSize * 1.1f).sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = DiLinkTextSecondary,
                modifier = Modifier.padding(bottom = (valueFontSize * 0.1f).dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = DiLinkTextSecondary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun MetricDisplayPreview() {
    DiLinkTheme {
        MetricDisplay(
            value = "87",
            unit = "km/h",
            label = "Current Speed"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun MetricDisplayLargePreview() {
    DiLinkTheme {
        MetricDisplay(
            value = "342",
            unit = "m",
            label = "Altitude",
            valueFontSize = 28f
        )
    }
}
