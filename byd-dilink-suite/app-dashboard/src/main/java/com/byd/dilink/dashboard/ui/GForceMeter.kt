package com.byd.dilink.dashboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.core.ui.theme.StatusYellow
import java.util.Locale
import kotlin.math.sqrt

@Composable
fun GForceMeter(
    gForceX: Float,
    gForceY: Float,
    peakG: Float,
    modifier: Modifier = Modifier
) {
    val totalG = sqrt(gForceX * gForceX + gForceY * gForceY)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = minOf(cx, cy) * 0.85f

            // Background circle
            drawCircle(
                color = DiLinkSurfaceVariant.copy(alpha = 0.3f),
                radius = radius,
                center = Offset(cx, cy)
            )

            // 0.5g ring
            drawCircle(
                color = DiLinkTextMuted.copy(alpha = 0.4f),
                radius = radius * 0.5f,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )

            // 1.0g ring
            drawCircle(
                color = DiLinkTextMuted.copy(alpha = 0.6f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            // Cross-hairs
            drawLine(
                color = DiLinkTextMuted.copy(alpha = 0.3f),
                start = Offset(cx - radius, cy),
                end = Offset(cx + radius, cy),
                strokeWidth = 1f
            )
            drawLine(
                color = DiLinkTextMuted.copy(alpha = 0.3f),
                start = Offset(cx, cy - radius),
                end = Offset(cx, cy + radius),
                strokeWidth = 1f
            )

            // Scale labels
            val scalePaint = android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = radius * 0.12f
                isAntiAlias = true
                color = android.graphics.Color.argb(255, 102, 102, 102) // DiLinkTextMuted
            }
            drawContext.canvas.nativeCanvas.drawText(
                "0.5g", cx + radius * 0.5f + scalePaint.textSize, cy - scalePaint.textSize * 0.4f, scalePaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                "1.0g", cx + radius - scalePaint.textSize * 0.5f, cy - scalePaint.textSize * 0.4f, scalePaint
            )

            // G-force dot position (clamped to 1.0g for display)
            val clampedX = gForceX.coerceIn(-1f, 1f)
            val clampedY = gForceY.coerceIn(-1f, 1f)
            val dotX = cx + clampedX * radius
            val dotY = cy - clampedY * radius  // Invert Y so forward is up

            // Dot color based on magnitude
            val dotColor = when {
                totalG < 0.3f -> DiLinkCyan
                totalG < 0.7f -> StatusYellow
                else -> StatusRed
            }

            // Trail (dimmer outer ring)
            drawCircle(
                color = dotColor.copy(alpha = 0.2f),
                radius = radius * 0.08f,
                center = Offset(dotX, dotY)
            )

            // G-force dot
            drawCircle(
                color = dotColor,
                radius = radius * 0.06f,
                center = Offset(dotX, dotY)
            )

            // Dot glow
            drawCircle(
                color = dotColor.copy(alpha = 0.3f),
                radius = radius * 0.12f,
                center = Offset(dotX, dotY)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(Locale.US, "%.2fg", totalG),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = DiLinkTextPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = String.format(Locale.US, "Peak: %.2fg", peakG),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = DiLinkTextSecondary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 200, heightDp = 250)
@Composable
fun GForceMeterPreview() {
    DiLinkTheme {
        GForceMeter(
            gForceX = 0.3f,
            gForceY = -0.15f,
            peakG = 0.8f
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 150, heightDp = 200)
@Composable
fun GForceMeterSmallPreview() {
    DiLinkTheme {
        GForceMeter(
            gForceX = 0.0f,
            gForceY = 0.0f,
            peakG = 0.2f
        )
    }
}
