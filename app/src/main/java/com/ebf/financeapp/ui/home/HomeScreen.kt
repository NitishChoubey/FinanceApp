package com.ebf.financeapp.ui.home



import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ebf.financeapp.ui.components.EmptyState
import com.ebf.financeapp.ui.components.HomeScreenShimmer
import com.ebf.financeapp.ui.components.SummaryCard
import com.ebf.financeapp.ui.components.TransactionCard
import com.ebf.financeapp.ui.insights.ForecastCard
import com.ebf.financeapp.ui.navigation.Routes
import com.ebf.financeapp.util.CurrencyFormatter


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        HomeScreenShimmer()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Gradient balance card ──────────────────────────────────────────
        item {
            BalanceCard(state = state)
        }

        // ── Income / Expense summary ───────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    label  = "Income",
                    amount = CurrencyFormatter.formatCompact(state.monthlyIncome),
                    icon   = Icons.Filled.ArrowDownward,
                    iconTint = Color(0xFF1D9E75),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label  = "Expenses",
                    amount = CurrencyFormatter.formatCompact(state.monthlyExpense),
                    icon   = Icons.Filled.ArrowUpward,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Savings progress ───────────────────────────────────────────────
        item {
            SavingsProgressCard(state = state)

        }

        item {
            state.healthScore?.let { HealthScoreCard(score = it) }
        }

        item {
            state.forecast?.let { ForecastCard(forecast = it) }

        }





        // ── Spending breakdown donut ───────────────────────────────────────
        if (state.spendingByCategory.isNotEmpty()) {
            item {
                SpendingBreakdownCard(slices = state.spendingByCategory)
            }
        }

        // ── Recent transactions header ─────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { navController.navigate(Routes.TRANSACTIONS) }) {
                    Text("See all")
                }
            }
        }

        // ── Recent transaction list ────────────────────────────────────────
        if (state.recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Receipt,
                    title = "No transactions yet",
                    subtitle = "Add your first transaction\nto see it here",
                    actionLabel = "Add transaction",
                    onAction = { navController.navigate(Routes.ADD_TRANSACTION) },
                    modifier = Modifier.height(220.dp)
                )
            }
        } else {
            items(state.recentTransactions) { item ->
                TransactionCard(
                    item = item,
                    onEdit = {
                        navController.navigate(Routes.editTransaction(item.transaction.id))
                    },
                    onDelete = {},  // No delete from home — just navigate to transactions
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─── Balance hero card ────────────────────────────────────────────────────────

@Composable
private fun BalanceCard(state: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF185FA5),
                        Color(0xFF1D9E75)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = state.currentMonthLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.format(state.totalBalance),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // Mini savings indicator inside balance card
            val savingsPct = (state.savingsRate * 100).toInt()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Savings,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Saving $savingsPct% of income this month",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── Savings progress ─────────────────────────────────────────────────────────

@Composable
private fun SavingsProgressCard(state: HomeUiState) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.savingsRate,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "savings_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Monthly Savings", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = CurrencyFormatter.format(state.monthlySavings),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (state.monthlySavings >= 0) Color(0xFF1D9E75)
                        else MaterialTheme.colorScheme.error
                    )
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF1D9E75),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${(state.savingsRate * 100).toInt()}% of income saved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// ─── Spending breakdown ───────────────────────────────────────────────────────

@Composable
private fun SpendingBreakdownCard(slices: List<CategoryPieSlice>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            // Simple horizontal bar visualization
            slices.take(5).forEach { slice ->
                val animatedWidth by animateFloatAsState(
                    targetValue = slice.percentage / 100f,
                    animationSpec = tween(700, easing = EaseOutCubic),
                    label = "bar_${slice.name}"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                Color(android.graphics.Color.parseColor(slice.colorHex))
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = slice.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(100.dp),
                        maxLines = 1
                    )
                    Spacer(Modifier.width(8.dp))
                    // Animated bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedWidth)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Color(android.graphics.Color.parseColor(slice.colorHex))
                                )
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${slice.percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}