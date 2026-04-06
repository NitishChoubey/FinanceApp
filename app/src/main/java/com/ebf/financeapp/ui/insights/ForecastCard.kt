package com.ebf.financeapp.ui.insights



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ebf.financeapp.util.CurrencyFormatter
import java.util.Calendar

data class SpendingForecast(
    val projectedMonthEnd: Double,
    val currentSpend: Double,
    val daysElapsed: Int,
    val daysInMonth: Int,
    val dailyAverage: Double,
    val projectedSavings: Double,
    val monthlyIncome: Double
)

fun buildForecast(
    currentExpense: Double,
    monthlyIncome: Double
): SpendingForecast {
    val cal       = Calendar.getInstance()
    val dayOfMonth= cal.get(Calendar.DAY_OF_MONTH)
    val daysInMo  = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dailyAvg  = if (dayOfMonth > 0) currentExpense / dayOfMonth else 0.0
    val projected = dailyAvg * daysInMo
    val savings   = monthlyIncome - projected

    return SpendingForecast(
        projectedMonthEnd = projected,
        currentSpend      = currentExpense,
        daysElapsed       = dayOfMonth,
        daysInMonth       = daysInMo,
        dailyAverage      = dailyAvg,
        projectedSavings  = savings,
        monthlyIncome     = monthlyIncome
    )
}

@Composable
fun ForecastCard(forecast: SpendingForecast ,  modifier: Modifier = Modifier ) {
    val isOnTrack = forecast.projectedSavings > 0
    val accentColor = if (isOnTrack) Color(0xFF1D9E75) else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoGraph, null,
                    tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Month-End Forecast", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(14.dp))

            // Day progress
            Text(
                "Day ${forecast.daysElapsed} of ${forecast.daysInMonth}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { forecast.daysElapsed.toFloat() / forecast.daysInMonth },
                modifier = Modifier.fillMaxWidth().height(5.dp),
                color = MaterialTheme.colorScheme.outline,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ForecastStat(
                    label  = "Daily avg",
                    value  = CurrencyFormatter.formatCompact(forecast.dailyAverage),
                    color  = MaterialTheme.colorScheme.onSurface
                )
                ForecastStat(
                    label  = "Projected spend",
                    value  = CurrencyFormatter.formatCompact(forecast.projectedMonthEnd),
                    color  = if (forecast.projectedMonthEnd > forecast.monthlyIncome)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                ForecastStat(
                    label  = "Projected savings",
                    value  = CurrencyFormatter.formatCompact(forecast.projectedSavings),
                    color  = accentColor
                )
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isOnTrack) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        null, tint = accentColor, modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isOnTrack)
                            "On track to save ${CurrencyFormatter.formatCompact(forecast.projectedSavings)} this month!"
                        else
                            "Projected to overspend by ${CurrencyFormatter.formatCompact(-forecast.projectedSavings)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold, color = color))
    }
}

// StrokeCap alias
private val StrokeCap = androidx.compose.ui.graphics.StrokeCap