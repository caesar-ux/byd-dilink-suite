package com.byd.dilink.extras.fuelcost.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.extras.core.ui.components.SectionCard
import com.byd.dilink.extras.core.ui.components.TopBarWithBack
import com.byd.dilink.extras.core.ui.theme.DiLinkBackground
import com.byd.dilink.extras.core.ui.theme.DiLinkCyan
import com.byd.dilink.extras.core.ui.theme.DiLinkExtrasTheme
import com.byd.dilink.extras.core.ui.theme.DiLinkSurface
import com.byd.dilink.extras.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.extras.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.extras.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.extras.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.extras.core.ui.theme.FuelGreen
import com.byd.dilink.extras.core.ui.theme.StatusGreen
import com.byd.dilink.extras.core.ui.theme.StatusOrange
import com.byd.dilink.extras.core.data.repository.FuelCostRepository
import com.byd.dilink.extras.fuelcost.viewmodel.FuelCostViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: FuelCostViewModel = hiltViewModel()
) {
    val statsState by viewModel.statisticsState.collectAsState()
    val dashState by viewModel.dashboardState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    val monthNames = remember {
        arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    Scaffold(
        topBar = {
            TopBarWithBack(title = "Statistics", onBack = onBack)
        },
        containerColor = DiLinkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Stacked bar chart
            if (statsState.monthlyBreakdowns.isNotEmpty()) {
                item {
                    SectionCard(title = "Monthly Costs") {
                        StackedBarChart(
                            breakdowns = statsState.monthlyBreakdowns.take(6).reversed(),
                            monthNames = monthNames,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            // Cost per km trend line chart
            if (statsState.monthlyBreakdowns.size >= 2) {
                item {
                    SectionCard(title = "Cost per km Trend") {
                        CostPerKmLineChart(
                            breakdowns = statsState.monthlyBreakdowns.take(6).reversed(),
                            monthNames = monthNames,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }

            // Pie chart
            item {
                SectionCard(title = "Energy Split") {
                    PieChart(
                        evPercentage = dashState.evPercentage.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            // Monthly breakdown list
            if (statsState.monthlyBreakdowns.isNotEmpty()) {
                item {
                    Text(
                        text = "Monthly Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        color = DiLinkTextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(statsState.monthlyBreakdowns) { breakdown ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DiLinkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${monthNames[breakdown.month]} ${breakdown.year}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DiLinkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem("Fuel", "${numberFormat.format(breakdown.fuelCostIqd.toLong())} ${dashState.currency}", StatusOrange)
                                StatItem("Electric", "${numberFormat.format(breakdown.electricCostIqd.toLong())} ${dashState.currency}", DiLinkCyan)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem("Total km", "${numberFormat.format(breakdown.totalKm)}", DiLinkTextSecondary)
                                StatItem(
                                    "Savings",
                                    "${numberFormat.format(breakdown.savings.toLong())} ${dashState.currency}",
                                    if (breakdown.savings >= 0) StatusGreen else Color(0xFFFF5252)
                                )
                            }
                        }
                    }
                }
            }

            // Lifetime totals
            statsState.lifetimeTotals?.let { totals ->
                item {
                    SectionCard(title = "Lifetime Totals") {
                        LifetimeRow("Total Fuel", "${String.format("%.1f", totals.totalFuelLiters)} L", StatusOrange)
                        LifetimeRow("Fuel Cost", "${numberFormat.format(totals.totalFuelCostIqd.toLong())} ${dashState.currency}", StatusOrange)
                        LifetimeRow("Total Electric", "${String.format("%.1f", totals.totalChargeKwh)} kWh", DiLinkCyan)
                        LifetimeRow("Electric Cost", "${numberFormat.format(totals.totalChargeCostIqd.toLong())} ${dashState.currency}", DiLinkCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        LifetimeRow("Total km", "${numberFormat.format(totals.totalKm)}", DiLinkTextPrimary)
                        LifetimeRow("Avg L/100km", String.format("%.1f", totals.avgLPer100Km), StatusOrange)
                        LifetimeRow("Avg kWh/100km", String.format("%.1f", totals.avgKwhPer100Km), DiLinkCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        LifetimeRow(
                            "Total Savings",
                            "${numberFormat.format(totals.totalSavings.toLong())} ${dashState.currency}",
                            StatusGreen
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DiLinkTextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun LifetimeRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = DiLinkTextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun StackedBarChart(
    breakdowns: List<FuelCostRepository.MonthlyBreakdown>,
    monthNames: Array<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (breakdowns.isEmpty()) return@Canvas

        val maxTotal = breakdowns.maxOf { it.fuelCostIqd + it.electricCostIqd }.coerceAtLeast(1.0)
        val barCount = breakdowns.size
        val padding = 60f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val barWidth = (chartWidth / barCount) * 0.6f
        val barSpacing = chartWidth / barCount

        // Draw axes
        drawLine(
            color = Color(0xFF666666),
            start = Offset(padding, padding),
            end = Offset(padding, size.height - padding),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFF666666),
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 1.5f
        )

        breakdowns.forEachIndexed { index, breakdown ->
            val x = padding + barSpacing * index + (barSpacing - barWidth) / 2
            val fuelHeight = (breakdown.fuelCostIqd / maxTotal * chartHeight).toFloat()
            val electricHeight = (breakdown.electricCostIqd / maxTotal * chartHeight).toFloat()
            val totalHeight = fuelHeight + electricHeight
            val baseY = size.height - padding

            // Fuel bar (bottom, orange)
            if (fuelHeight > 0) {
                drawRoundRect(
                    color = Color(0xFFFF9800),
                    topLeft = Offset(x, baseY - fuelHeight),
                    size = Size(barWidth, fuelHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Electric bar (stacked on top, cyan)
            if (electricHeight > 0) {
                drawRoundRect(
                    color = Color(0xFF00BCD4),
                    topLeft = Offset(x, baseY - totalHeight),
                    size = Size(barWidth, electricHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Month label
            drawContext.canvas.nativeCanvas.drawText(
                monthNames[breakdown.month],
                x + barWidth / 2,
                size.height - padding / 3,
                android.graphics.Paint().apply {
                    color = 0xFFAAAAAA.toInt()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}

@Composable
fun CostPerKmLineChart(
    breakdowns: List<FuelCostRepository.MonthlyBreakdown>,
    monthNames: Array<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (breakdowns.size < 2) return@Canvas

        val padding = 60f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        // Calculate cost per km for each month
        val fuelCostPerKm = breakdowns.map { b ->
            if (b.totalKm > 0) b.fuelCostIqd / b.totalKm else 0.0
        }
        val electricCostPerKm = breakdowns.map { b ->
            if (b.totalKm > 0) b.electricCostIqd / b.totalKm else 0.0
        }
        val allValues = fuelCostPerKm + electricCostPerKm
        val maxVal = allValues.max().coerceAtLeast(1.0)

        // Draw axes
        drawLine(
            color = Color(0xFF666666),
            start = Offset(padding, padding),
            end = Offset(padding, size.height - padding),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFF666666),
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 1.5f
        )

        // Draw grid lines
        for (i in 1..4) {
            val y = size.height - padding - (chartHeight * i / 4)
            drawLine(
                color = Color(0xFF333333),
                start = Offset(padding, y),
                end = Offset(size.width - padding, y),
                strokeWidth = 0.5f
            )
        }

        val stepX = chartWidth / (breakdowns.size - 1).coerceAtLeast(1)

        // Draw fuel cost line (orange)
        val fuelPath = Path()
        fuelCostPerKm.forEachIndexed { index, value ->
            val x = padding + stepX * index
            val y = size.height - padding - (value / maxVal * chartHeight).toFloat()
            if (index == 0) fuelPath.moveTo(x, y) else fuelPath.lineTo(x, y)
        }
        drawPath(fuelPath, Color(0xFFFF9800), style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Draw electric cost line (cyan)
        val electricPath = Path()
        electricCostPerKm.forEachIndexed { index, value ->
            val x = padding + stepX * index
            val y = size.height - padding - (value / maxVal * chartHeight).toFloat()
            if (index == 0) electricPath.moveTo(x, y) else electricPath.lineTo(x, y)
        }
        drawPath(electricPath, Color(0xFF00BCD4), style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Draw data points and labels
        breakdowns.forEachIndexed { index, breakdown ->
            val x = padding + stepX * index

            // Fuel dot
            val fuelY = size.height - padding - (fuelCostPerKm[index] / maxVal * chartHeight).toFloat()
            drawCircle(Color(0xFFFF9800), radius = 5f, center = Offset(x, fuelY))

            // Electric dot
            val elecY = size.height - padding - (electricCostPerKm[index] / maxVal * chartHeight).toFloat()
            drawCircle(Color(0xFF00BCD4), radius = 5f, center = Offset(x, elecY))

            // Month label
            drawContext.canvas.nativeCanvas.drawText(
                monthNames[breakdown.month],
                x,
                size.height - padding / 3,
                android.graphics.Paint().apply {
                    color = 0xFFAAAAAA.toInt()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}

@Composable
fun PieChart(
    evPercentage: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier.size(160.dp)
        ) {
            val strokeWidth = 40f
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            val evSweep = 360f * (evPercentage / 100f).coerceIn(0f, 1f)
            val petrolSweep = 360f - evSweep

            // Petrol arc (orange)
            if (petrolSweep > 0) {
                drawArc(
                    color = Color(0xFFFF9800),
                    startAngle = -90f + evSweep,
                    sweepAngle = petrolSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            }

            // EV arc (cyan)
            if (evSweep > 0) {
                drawArc(
                    color = Color(0xFF00BCD4),
                    startAngle = -90f,
                    sweepAngle = evSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            }

            // Center text
            drawContext.canvas.nativeCanvas.drawText(
                "${evPercentage.toInt()}%",
                center.x,
                center.y + 12f,
                android.graphics.Paint().apply {
                    color = 0xFFEEEEEE.toInt()
                    textSize = 40f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            )
            drawContext.canvas.nativeCanvas.drawText(
                "EV",
                center.x,
                center.y - 20f,
                android.graphics.Paint().apply {
                    color = 0xFFAAAAAA.toInt()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendItem(Color(0xFF00BCD4), "Electric", "${evPercentage.toInt()}%")
            LegendItem(Color(0xFFFF9800), "Petrol", "${(100 - evPercentage).toInt()}%")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = DiLinkTextSecondary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = DiLinkTextPrimary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 300)
@Composable
private fun StackedBarChartPreview() {
    DiLinkExtrasTheme {
        val sampleData = listOf(
            FuelCostRepository.MonthlyBreakdown(2025, 0, 120000.0, 30000.0, 800, 25000.0),
            FuelCostRepository.MonthlyBreakdown(2025, 1, 95000.0, 45000.0, 1100, 32000.0),
            FuelCostRepository.MonthlyBreakdown(2025, 2, 80000.0, 55000.0, 900, 40000.0)
        )
        StackedBarChart(
            breakdowns = sampleData,
            monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 250)
@Composable
private fun PieChartPreview() {
    DiLinkExtrasTheme {
        PieChart(
            evPercentage = 68f,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 200)
@Composable
private fun CostPerKmLineChartPreview() {
    DiLinkExtrasTheme {
        val sampleData = listOf(
            FuelCostRepository.MonthlyBreakdown(2025, 0, 120000.0, 30000.0, 800, 25000.0),
            FuelCostRepository.MonthlyBreakdown(2025, 1, 95000.0, 45000.0, 1100, 32000.0),
            FuelCostRepository.MonthlyBreakdown(2025, 2, 80000.0, 55000.0, 900, 40000.0)
        )
        CostPerKmLineChart(
            breakdowns = sampleData,
            monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(8.dp)
        )
    }
}
