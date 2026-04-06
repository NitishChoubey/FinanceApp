package com.ebf.financeapp.ui.insights




import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ebf.financeapp.data.remote.FinancialTip
import com.ebf.financeapp.ui.components.EmptyState

@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = 16.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Header ─────────────────────────────────────────────────────────
        item {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = state.currentMonthLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        // ── Empty state ────────────────────────────────────────────────────
        if (state.isEmpty) {
            item {
                EmptyState(
                    icon     = Icons.Filled.BarChart,
                    title    = "No insights yet",
                    subtitle = "Add some transactions to see\nyour spending patterns",
                    modifier = Modifier.height(300.dp)
                )
            }
            // Still show tips even when no transactions
            item {
                TipsSection(
                    tips    = state.financialTips,
                    loading = state.tipsLoading
                )
            }
            return@LazyColumn
        }

        // ── Week vs last week ──────────────────────────────────────────────
        state.weekComparison?.let { wc ->
            item { WeekComparisonCard(comparison = wc) }
        }

        // ── Month comparison ───────────────────────────────────────────────
        item {
            MonthComparisonCard(state = state)
        }

        // ── 7-day bar chart ────────────────────────────────────────────────
        if (state.dailyExpenses.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Last 7 Days", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        DailyBarChart(
                            dailyAmounts = state.dailyExpenses.map { it.total.toFloat() },
                            labels       = state.chartDayLabels,
                            barColor     = MaterialTheme.colorScheme.primary.toArgb()
                        )
                    }
                }
            }
        }

        // ── Top category ───────────────────────────────────────────────────
        state.topCategory?.let { top ->
            item {
                TopCategoryCard(
                    name     = top.categoryName,
                    amount   = top.totalAmount,
                    colorHex = top.colorHex
                )
            }
        }

        // ── Category breakdown list ────────────────────────────────────────
        if (state.categorySpending.isNotEmpty()) {
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Spending Breakdown",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        val totalSpend = state.categorySpending.sumOf { it.totalAmount }
                        state.categorySpending.forEach { cs ->
                            CategoryBreakdownRow(
                                name     = cs.categoryName,
                                amount   = cs.totalAmount,
                                colorHex = cs.colorHex,
                                percent  = if (totalSpend > 0)
                                    (cs.totalAmount / totalSpend * 100).toFloat()
                                else 0f
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        // ── AI Financial Tips ──────────────────────────────────────────────
        item {
            TipsSection(
                tips    = state.financialTips,
                loading = state.tipsLoading
            )
        }
    }
}

// ─── AI Tips section ──────────────────────────────────────────────────────────

@Composable
private fun TipsSection(
    tips: List<FinancialTip>,
    loading: Boolean
) {
    when {
        loading -> {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color    = Color(0xFF8B5CF6),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Loading AI tips...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        tips.isNotEmpty() -> {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    // Header row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint     = Color(0xFF8B5CF6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "AI Financial Tips",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Powered by Finance API",
                                modifier = Modifier.padding(
                                    horizontal = 8.dp, vertical = 3.dp
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Tip cards
                    tips.forEach { tip ->
                        TipCard(tip = tip)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        else -> {
            // tips loaded but empty — show nothing
        }
    }
}

@Composable
private fun TipCard(tip: FinancialTip) {
    val tipColor = remember(tip.color) {
        try { Color(AndroidColor.parseColor(tip.color)) }
        catch (e: Exception) { Color(0xFF8B5CF6) }
    }

    val tipEmoji = when (tip.icon) {
        "savings"      -> "💰"
        "timer"        -> "⏱️"
        "trending_up"  -> "📈"
        "receipt_long" -> "📊"
        "security"     -> "🛡️"
        else           -> "💡"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = tipColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tipColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(tipEmoji, fontSize = 18.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text  = tip.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = tipColor
                    )
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text  = tip.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ─── Week comparison card ─────────────────────────────────────────────────────

@Composable
private fun WeekComparisonCard(comparison: WeekComparison) {
    val isHigher   = comparison.isHigher
    val trendColor = if (isHigher) MaterialTheme.colorScheme.error else Color(0xFF1D9E75)
    val icon       = if (isHigher) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Week vs Last Week", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "This week",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "₹${"%.0f".format(comparison.thisWeekTotal)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = trendColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "${"%.1f".format(Math.abs(comparison.changePercent))}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = trendColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Last week",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "₹${"%.0f".format(comparison.lastWeekTotal)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// ─── Month comparison card ────────────────────────────────────────────────────

@Composable
private fun MonthComparisonCard(state: InsightsUiState) {
    val isHigher   = state.isMonthHigher
    val trendColor = if (isHigher) MaterialTheme.colorScheme.error else Color(0xFF1D9E75)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "This Month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "₹${"%.0f".format(state.thisMonthExpense)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = trendColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${if (isHigher) "+" else "-"}${"%.1f".format(
                        Math.abs(state.monthChangePercent)
                    )}% vs last month",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = trendColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Last Month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "₹${"%.0f".format(state.lastMonthExpense)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ─── Top category card ────────────────────────────────────────────────────────

@Composable
private fun TopCategoryCard(name: String, amount: Double, colorHex: String) {
    val catColor = remember(colorHex) {
        try { Color(AndroidColor.parseColor(colorHex)) }
        catch (e: Exception) { Color(0xFF378ADD) }
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = catColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint     = catColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Top spending category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color      = catColor
                    )
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "₹${"%.0f".format(amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = catColor
            )
        }
    }
}

// ─── Category breakdown row ───────────────────────────────────────────────────

@Composable
private fun CategoryBreakdownRow(
    name: String,
    amount: Double,
    colorHex: String,
    percent: Float
) {
    val catColor = remember(colorHex) {
        try { Color(AndroidColor.parseColor(colorHex)) }
        catch (e: Exception) { Color(0xFF378ADD) }
    }
    val animatedPercent by animateFloatAsState(
        targetValue    = percent / 100f,
        animationSpec  = tween(700, easing = EaseOutCubic),
        label          = "cat_$name"
    )
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(catColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(name, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                Text(
                    "${percent.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "₹${"%.0f".format(amount)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedPercent)
                    .clip(RoundedCornerShape(50))
                    .background(catColor)
            )
        }
    }
}

// ─── 7-day bar chart ──────────────────────────────────────────────────────────

@Composable
private fun DailyBarChart(
    dailyAmounts: List<Float>,
    labels: List<String>,
    barColor: Int
) {
    AndroidView(
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled       = false
                legend.isEnabled            = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled    = false
                setExtraOffsets(0f, 8f, 0f, 8f)

                xAxis.apply {
                    position        = XAxis.XAxisPosition.BOTTOM
                    granularity     = 1f
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    textColor       = AndroidColor.parseColor("#888888")
                    textSize        = 11f
                    valueFormatter  = IndexAxisValueFormatter(labels)
                }
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor       = AndroidColor.parseColor("#22000000")
                    setDrawAxisLine(false)
                    textColor       = AndroidColor.parseColor("#888888")
                    textSize        = 11f
                    axisMinimum     = 0f
                    valueFormatter  = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float) =
                            if (value >= 1000f) "₹${(value / 1000).toInt()}K"
                            else "₹${value.toInt()}"
                    }
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = dailyAmounts.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
            val dataSet = BarDataSet(entries, "Expenses").apply {
                color = barColor
                setDrawValues(false)
            }
            chart.data = BarData(dataSet).apply { barWidth = 0.6f }
            chart.animateY(500)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}