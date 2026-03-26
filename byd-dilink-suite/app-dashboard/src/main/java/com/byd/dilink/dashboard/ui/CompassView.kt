package com.byd.dilink.dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassView(
    heading: Float,
    cardinalDirection: String,
    modifier: Modifier = Modifier
) {
    // Smooth heading animation — handle wrap-around 359→0
    val targetRotation = remember(heading) { heading }
    val animatedHeading by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 200),
        label = "compass_rotation"
    )

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
            val radius = minOf(cx, cy) * 0.9f

            // Outer circle
            drawCircle(
                color = DiLinkSurfaceVariant,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )

            // Inner circle
            drawCircle(
                color = DiLinkSurfaceVariant.copy(alpha = 0.3f),
                radius = radius * 0.6f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            // Tick marks every 15°
            for (i in 0 until 360 step 15) {
                val angle = i.toFloat()
                val drawAngle = angle - animatedHeading
                val radians = Math.toRadians(drawAngle.toDouble())

                val isCardinal = i % 90 == 0
                val is45 = i % 45 == 0

                val innerRatio = when {
                    isCardinal -> 0.72f
                    is45 -> 0.78f
                    else -> 0.85f
                }
                val outerRatio = 0.93f

                val startX = cx + radius * innerRatio * sin(radians).toFloat()
                val startY = cy - radius * innerRatio * cos(radians).toFloat()
                val endX = cx + radius * outerRatio * sin(radians).toFloat()
                val endY = cy - radius * outerRatio * cos(radians).toFloat()

                val tickColor = when {
                    isCardinal -> DiLinkTextPrimary
                    is45 -> DiLinkTextSecondary
                    else -> DiLinkTextMuted
                }
                val tickWidth = when {
                    isCardinal -> 3f
                    is45 -> 2f
                    else -> 1.2f
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // Cardinal letters
            val cardinals = listOf(0f to "N", 90f to "E", 180f to "S", 270f to "W")
            val paint = android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = radius * 0.16f
                isAntiAlias = true
                isFakeBoldText = true
            }

            for ((angle, letter) in cardinals) {
                val drawAngle = angle - animatedHeading
                val radians = Math.toRadians(drawAngle.toDouble())
                val textRadius = radius * 0.58f
                val tx = cx + textRadius * sin(radians).toFloat()
                val ty = cy - textRadius * cos(radians).toFloat()

                paint.color = if (letter == "N") {
                    android.graphics.Color.argb(255, 255, 82, 82)  // StatusRed
                } else {
                    android.graphics.Color.argb(255, 238, 238, 238)  // DiLinkTextPrimary
                }

                drawContext.canvas.nativeCanvas.drawText(
                    letter, tx, ty + paint.textSize / 3f, paint
                )
            }

            // Current heading indicator (triangle at top)
            val indicatorPath = Path().apply {
                moveTo(cx, cy - radius + 2f)
                lineTo(cx - radius * 0.06f, cy - radius - radius * 0.1f)
                lineTo(cx + radius * 0.06f, cy - radius - radius * 0.1f)
                close()
            }
            drawPath(indicatorPath, color = DiLinkCyan)

            // Center heading text
            val headingPaint = android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = radius * 0.22f
                isAntiAlias = true
                isFakeBoldText = true
                color = android.graphics.Color.argb(255, 0, 188, 212) // DiLinkCyan
                typeface = android.graphics.Typeface.MONOSPACE
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${animatedHeading.toInt()}°",
                cx, cy + headingPaint.textSize / 3f, headingPaint
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${heading.toInt()}° $cardinalDirection",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
            color = DiLinkTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 300, heightDp = 350)
@Composable
fun CompassViewPreview() {
    DiLinkTheme {
        CompassView(heading = 247f, cardinalDirection = "WSW")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 200, heightDp = 250)
@Composable
fun CompassViewSmallPreview() {
    DiLinkTheme {
        CompassView(heading = 0f, cardinalDirection = "N")
    }
}
