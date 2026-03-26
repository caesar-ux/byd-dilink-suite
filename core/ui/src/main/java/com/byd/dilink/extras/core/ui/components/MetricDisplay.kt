package com.byd.dilink.extras.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary

@Composable
fun MetricDisplay(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueFontSize: TextUnit = 36.sp
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = valueFontSize
            ),
            color = valueColor
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodyMedium,
            color = DiLinkTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = DiLinkTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun MetricDisplayPreview() {
    DiLinkExtrasTheme {
        MetricDisplay(
            value = "42.5",
            unit = "IQD/km",
            label = "Petrol Cost"
        )
    }
}
