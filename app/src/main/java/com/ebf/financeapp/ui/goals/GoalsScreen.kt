package com.ebf.financeapp.ui.goals

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ebf.financeapp.data.model.Goal
import com.ebf.financeapp.data.model.GoalWithCategory
import com.ebf.financeapp.ui.components.EmptyState
import com.ebf.financeapp.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // ── Sheet visibility states ────────────────────────────────────────────────
    var showAddSheet   by remember { mutableStateOf(false) }
    var editingGoal    by remember { mutableStateOf<GoalWithCategory?>(null) }

    // ── Add Goal sheet ─────────────────────────────────────────────────────────
    if (showAddSheet) {
        AddGoalSheet(
            viewModel = viewModel,
            onDismiss = {
                showAddSheet = false
                viewModel.resetAddGoalState()
            }
        )
    }

    // ── Edit Goal sheet ────────────────────────────────────────────────────────
    editingGoal?.let { gwc ->
        EditGoalSheet(
            current   = gwc,
            viewModel = viewModel,
            onDismiss = { editingGoal = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Goals") },
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.loadAvailableCategories()
                    showAddSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Goal",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                top = 8.dp, bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Budget alerts ──────────────────────────────────────────────────
            if (state.alerts.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.alerts.forEach { alert ->
                            AlertBanner(alert = alert)
                        }
                    }
                }
            }

            // ── Overall summary card ───────────────────────────────────────────
            if (state.goalProgressList.isNotEmpty()) {
                item {
                    OverallBudgetCard(state = state)
                }
            }

            // ── Month label ────────────────────────────────────────────────────
            item {
                Text(
                    text  = state.currentMonthLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // ── Empty state ────────────────────────────────────────────────────
            if (state.isEmpty) {
                item {
                    EmptyState(
                        icon        = Icons.Filled.TrackChanges,
                        title       = "No budget goals yet",
                        subtitle    = "Set monthly limits for each spending category\nto track where your money goes",
                        actionLabel = "Set first goal",
                        onAction    = {
                            viewModel.loadAvailableCategories()
                            showAddSheet = true
                        },
                        modifier = Modifier.height(300.dp)
                    )
                }
            }

            // ── Goal cards ─────────────────────────────────────────────────────
            items(
                items = state.goalProgressList,
                key   = { it.goal.goal.id }
            ) { gp ->
                GoalProgressCard(
                    goalProgress = gp,
                    onDelete     = { viewModel.deleteGoal(gp.goal.goal) },
                    onEdit       = { editingGoal = it }
                )
            }
        }
    }
}

// ─── Alert banner ─────────────────────────────────────────────────────────────

@Composable
private fun AlertBanner(alert: BudgetAlert) {
    val isOver    = alert.isOver
    val bgColor   = if (isOver)
        MaterialTheme.colorScheme.errorContainer
    else
        Color(0xFFFFF3CD)
    val textColor = if (isOver)
        MaterialTheme.colorScheme.onErrorContainer
    else
        Color(0xFF856404)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = if (isOver) Icons.Filled.Warning else Icons.Filled.Info,
            contentDescription = null,
            tint               = textColor,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text  = if (isOver)
                "${alert.categoryName} is ${alert.percentSpent - 100}% over budget!"
            else
                "${alert.categoryName} is at ${alert.percentSpent}% of budget",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}

// ─── Overall budget summary ────────────────────────────────────────────────────

@Composable
private fun OverallBudgetCard(state: GoalsUiState) {
    val animatedProgress by animateFloatAsState(
        targetValue   = state.overallProgress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "overall_budget"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text  = "Overall Budget",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        CurrencyFormatter.formatCompact(state.totalSpent),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        CurrencyFormatter.formatCompact(state.totalBudgeted),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress      = { animatedProgress },
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color         = MaterialTheme.colorScheme.primary,
                trackColor    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                strokeCap     = StrokeCap.Round
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "${(state.overallProgress * 100).toInt()}% of total budget used",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Individual goal card ─────────────────────────────────────────────────────

@Composable
private fun GoalProgressCard(
    goalProgress: GoalProgress,
    onDelete: () -> Unit,
    onEdit: (GoalWithCategory) -> Unit
) {
    val gp       = goalProgress
    val catColor = Color(android.graphics.Color.parseColor(gp.goal.category.colorHex))

    val progressColor = when {
        gp.isOverBudget -> MaterialTheme.colorScheme.error
        gp.isNearLimit  -> Color(0xFFE5A000)
        else            -> catColor
    }

    val animatedProgress by animateFloatAsState(
        targetValue   = gp.progress.coerceIn(0f, 1f),
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "goal_${gp.goal.goal.id}"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            // ── Top row: icon + name + status badge ───────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = com.ebf.financeapp.ui.components
                            .getCategoryEmoji(gp.goal.category.icon),
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text  = gp.goal.category.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text  = when {
                            gp.isOverBudget -> "Over by ${
                                CurrencyFormatter.format(gp.amountSpent - gp.budgetLimit)
                            }"
                            gp.isNearLimit  -> "${
                                CurrencyFormatter.format(gp.remainingAmount)
                            } remaining"
                            else            -> "${
                                CurrencyFormatter.format(gp.remainingAmount)
                            } left to spend"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = progressColor.copy(alpha = 0.85f)
                    )
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = progressColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text     = when {
                            gp.isOverBudget -> "Over"
                            gp.isNearLimit  -> "Near limit"
                            else            -> "On track"
                        },
                        modifier = Modifier.padding(
                            horizontal = 10.dp, vertical = 4.dp
                        ),
                        style    = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color    = progressColor
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Animated progress bar ─────────────────────────────────────────
            LinearProgressIndicator(
                progress   = { animatedProgress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color      = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap  = StrokeCap.Round
            )

            Spacer(Modifier.height(8.dp))

            // ── Spent / Limit labels ──────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "${CurrencyFormatter.format(gp.amountSpent)} spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Text(
                    text  = "of ${CurrencyFormatter.format(gp.budgetLimit)} budget",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // ── Edit / Delete action row ──────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Edit button
                TextButton(
                    onClick        = { onEdit(gp.goal) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Edit,
                        contentDescription = "Edit goal",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "Edit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Delete button
                TextButton(
                    onClick        = onDelete,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Delete,
                        contentDescription = "Delete goal",
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "Delete",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ─── Add Goal Bottom Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalSheet(
    viewModel: GoalViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.addGoalState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDismiss()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Set Budget Goal", style = MaterialTheme.typography.titleLarge)

            // ── Category selector ──────────────────────────────────────────────
            Text(
                "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (state.availableCategories.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text     = "All categories already have goals this month!",
                        modifier = Modifier.padding(16.dp),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.availableCategories.forEach { cat ->
                        val isSelected = cat.id == state.selectedCategoryId
                        val catColor   = Color(
                            android.graphics.Color.parseColor(cat.colorHex)
                        )
                        FilterChip(
                            selected  = isSelected,
                            onClick   = { viewModel.onCategorySelected(cat.id) },
                            label     = {
                                Text(
                                    "${com.ebf.financeapp.ui.components
                                        .getCategoryEmoji(cat.icon)} ${cat.name.split(" ")[0]}"
                                )
                            },
                            colors    = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.2f),
                                selectedLabelColor     = catColor
                            )
                        )
                    }
                }
                if (state.categoryError != null) {
                    Text(
                        text  = state.categoryError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // ── Budget amount ──────────────────────────────────────────────────
            OutlinedTextField(
                value           = state.budgetAmount,
                onValueChange   = viewModel::onBudgetAmountChange,
                label           = { Text("Monthly budget limit") },
                leadingIcon     = { Text("₹",
                    style = MaterialTheme.typography.titleMedium) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError         = state.amountError != null,
                supportingText  = state.amountError?.let { { Text(it) } },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true
            )

            // ── Save button ────────────────────────────────────────────────────
            Button(
                onClick  = viewModel::saveGoal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = !state.isLoading && state.availableCategories.isNotEmpty()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Goal", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Edit Goal Bottom Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGoalSheet(
    current: GoalWithCategory,
    viewModel: GoalViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember {
        mutableStateOf(current.goal.budgetLimit.toBigDecimal()
            .stripTrailingZeros().toPlainString())
    }
    var error  by remember { mutableStateOf<String?>(null) }
    val catColor = Color(
        android.graphics.Color.parseColor(current.category.colorHex)
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header ─────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        com.ebf.financeapp.ui.components
                            .getCategoryEmoji(current.category.icon),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Edit Budget",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        current.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = catColor
                    )
                }
            }

            // ── Amount field ───────────────────────────────────────────────────
            OutlinedTextField(
                value           = amount,
                onValueChange   = { amount = it; error = null },
                label           = { Text("New budget limit") },
                leadingIcon     = { Text("₹",
                    style = MaterialTheme.typography.titleMedium) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError         = error != null,
                supportingText  = error?.let { { Text(it) } },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true
            )

            // ── Current budget info ────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info, null,
                        tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Current limit: ${
                            CurrencyFormatter.format(current.goal.budgetLimit)
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // ── Update button ──────────────────────────────────────────────────
            Button(
                onClick  = {
                    val d = amount.toDoubleOrNull()
                    when {
                        d == null || d <= 0 -> error = "Enter a valid amount greater than 0"
                        else -> {
                            viewModel.updateGoalAmount(current.goal, d)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Update Goal", style = MaterialTheme.typography.titleMedium)
            }

            // ── Cancel button ──────────────────────────────────────────────────
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}