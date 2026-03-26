package com.byd.dilink.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkTheme

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    color: Color = DiLinkSurfaceElevated,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = color,
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = color
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                content()
            }
        }
    }
}

@Preview
@Composable
private fun SectionCardPreview() {
    DiLinkTheme {
        SectionCard(title = "Engine Oil") {
            Text("Last: 01/01/2025 at 15,000 km")
        }
    }
}
