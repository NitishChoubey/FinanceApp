package com.ebf.financeapp.ui.home



import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HealthScore(
    val total: Int,              // 0–100
    val savingsScore: Int,       // 0–30
    val budgetScore: Int,        // 0–40
    val consistencyScore: Int,   // 0–30
    val label: String,
    val color: Color,
    val tips: List<String>
)

fun calculateHealthScore(
    savingsRate: Float,
    budgetAdherence: Float,     // 0f = over, 1f = perfect
    hasTransactionsThisWeek: Boolean
): HealthScore {
    val savingsScore      = (savingsRate * 30f).toInt().coerceIn(0, 30)
    val budgetScore       = (budgetAdherence * 40f).toInt().coerceIn(0, 40)
    val consistencyScore  = if (hasTransactionsThisWeek) 30 else 10
    val total             = savingsScore + budgetScore + consistencyScore

    val (label, color) = when {
        total >= 80 -> "Excellent" to Color(0xFF1D9E75)
        total >= 60 -> "Good"      to Color(0xFF378ADD)
        total >= 40 -> "Fair"      to Color(0xFFEF9F27)
        else        -> "Needs work"    to Color(0xFFE24B4A)
    }

    val tips = buildList {
        if (savingsRate < 0.2f) add("Try to save at least 20% of your income")
        if (budgetAdherence < 0.7f) add("Review your budget goals — some are overrun")
        if (!hasTransactionsThisWeek) add("Log your transactions regularly for better insights")
        if (total >= 80) add("Great discipline! Keep up the good habits")
    }

    return HealthScore(total, savingsScore, budgetScore, consistencyScore, label, color, tips)
}

@Composable
fun HealthScoreCard(score: HealthScore , modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score.total.toFloat(),
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "health_score"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Financial Health Score", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated arc score ring
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(110.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val arcSize     = Size(size.width - strokeWidth,
                            size.height - strokeWidth)
                        val topLeft     = Offset(strokeWidth / 2, strokeWidth / 2)
                        val sweepAngle  = (animatedScore / 100f) * 240f

                        // Track arc
                        drawArc(
                            color      = Color.LightGray.copy(alpha = 0.25f),
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter  = false,
                            topLeft    = topLeft,
                            size       = arcSize,
                            style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        // Score arc
                        drawArc(
                            color      = score.color,
                            startAngle = 150f,
                            sweepAngle = sweepAngle,
                            useCenter  = false,
                            topLeft    = topLeft,
                            size       = arcSize,
                            style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = animatedScore.toInt().toString(),
                            fontSize     = 28.sp,
                            fontWeight   = FontWeight.Bold,
                            color        = score.color
                        )
                        Text(
                            text  = score.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = score.color
                        )
                    }
                }

                Spacer(Modifier.width(20.dp))

                // Score breakdown
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreRow("Savings",     score.savingsScore,     30, Color(0xFF1D9E75))
                    ScoreRow("Budget",      score.budgetScore,      40, Color(0xFF378ADD))
                    ScoreRow("Consistency", score.consistencyScore, 30, Color(0xFF8B5CF6))
                }
            }

            // Tips
            if (score.tips.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                score.tips.take(2).forEach { tip ->
                    Row(
                        Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.Lightbulb, null,
                            tint = Color(0xFFEF9F27),
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Int, max: Int, color: Color) {
    val animated by animateFloatAsState(
        targetValue = score.toFloat() / max.toFloat(),
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "score_$label"
    )
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("$score/$max", style = MaterialTheme.typography.labelSmall,
                color = color, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(3.dp))
        LinearProgressIndicator(
            progress   = { animated },
            modifier   = Modifier.fillMaxWidth().height(5.dp)
                .then(Modifier.wrapContentHeight()),
            color      = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap  = StrokeCap.Round
        )
    }
}