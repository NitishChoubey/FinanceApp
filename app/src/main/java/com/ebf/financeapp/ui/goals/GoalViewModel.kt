package com.ebf.financeapp.ui.goals


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebf.financeapp.data.model.Category
import com.ebf.financeapp.data.model.Goal
import com.ebf.financeapp.data.model.GoalWithCategory
import com.ebf.financeapp.data.repository.CategoryRepository
import com.ebf.financeapp.data.repository.GoalRepository
import com.ebf.financeapp.data.repository.TransactionRepository
import com.ebf.financeapp.util.DateFormatter

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Rich goal model with live spending attached ─────────────────────────────

data class GoalProgress(
    val goal: GoalWithCategory,
    val amountSpent: Double,
    val budgetLimit: Double,
    val progress: Float,          // 0f–1f
    val isOverBudget: Boolean,
    val isNearLimit: Boolean,     // >80% spent
    val remainingAmount: Double
)

// ─── Alert banner shown at top of Goals screen ───────────────────────────────

data class BudgetAlert(
    val categoryName: String,
    val percentSpent: Int,
    val isOver: Boolean
)

data class GoalsUiState(
    val goalProgressList: List<GoalProgress> = emptyList(),
    val alerts: List<BudgetAlert> = emptyList(),
    val totalBudgeted: Double = 0.0,
    val totalSpent: Double = 0.0,
    val overallProgress: Float = 0f,
    val currentMonthLabel: String = "",
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val error: String? = null
)

data class AddGoalUiState(
    val budgetAmount: String = "",
    val selectedCategoryId: Int = -1,
    val availableCategories: List<Category> = emptyList(), // categories without a goal this month
    val amountError: String? = null,
    val categoryError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepo: GoalRepository,
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _uiState    = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val _addGoalState = MutableStateFlow(AddGoalUiState())
    val addGoalState: StateFlow<AddGoalUiState> = _addGoalState.asStateFlow()

    private val currentMonth = DateFormatter.getCurrentMonth()
    private val currentYear  = DateFormatter.getCurrentYear()
    private val currentYearStr = DateFormatter.getCurrentYearString()

    init {
        observeGoals()
    }

    private fun observeGoals() {
        viewModelScope.launch {
            goalRepo.getGoalsForMonth(currentMonth, currentYear)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { goals ->
                    buildGoalProgress(goals)
                }
        }
    }

    private suspend fun buildGoalProgress(goals: List<GoalWithCategory>) {
        val progressList = goals.map { gwc ->
            // Live spending for this category this month
            val spent = transactionRepo
                .getSpentForCategoryInMonth(gwc.goal.categoryId, currentMonth, currentYearStr)
                .first()

            val limit    = gwc.goal.budgetLimit
            val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1.5f) else 0f

            GoalProgress(
                goal            = gwc,
                amountSpent     = spent,
                budgetLimit     = limit,
                progress        = progress,
                isOverBudget    = spent > limit,
                isNearLimit     = (spent / limit) >= 0.8 && spent <= limit,
                remainingAmount = (limit - spent).coerceAtLeast(0.0)
            )
        }

        // Build alerts for over/near-limit goals
        val alerts = progressList
            .filter { it.isOverBudget || it.isNearLimit }
            .map { gp ->
                BudgetAlert(
                    categoryName = gp.goal.category.name,
                    percentSpent = ((gp.amountSpent / gp.budgetLimit) * 100).toInt(),
                    isOver       = gp.isOverBudget
                )
            }

        val totalBudgeted = progressList.sumOf { it.budgetLimit }
        val totalSpent    = progressList.sumOf { it.amountSpent }
        val overallProg   = if (totalBudgeted > 0)
            (totalSpent / totalBudgeted).toFloat().coerceIn(0f, 1f)
        else 0f

        _uiState.update {
            it.copy(
                goalProgressList    = progressList,
                alerts              = alerts,
                totalBudgeted       = totalBudgeted,
                totalSpent          = totalSpent,
                overallProgress     = overallProg,
                currentMonthLabel   = DateFormatter.formatMonthYear(System.currentTimeMillis()),
                isLoading           = false,
                isEmpty             = progressList.isEmpty()
            )
        }
    }

    // ─── Add Goal sheet ─────────────────────────────────────────────────────

    fun loadAvailableCategories() {
        viewModelScope.launch {
            val allCats = categoryRepo.getAllCategories().first()
            // Filter out categories that already have a goal this month
            val available = allCats.filter { cat ->
                !goalRepo.goalExistsForCategory(cat.id, currentMonth, currentYear)
            }
            _addGoalState.update {
                it.copy(availableCategories = available)
            }
        }
    }

    fun onBudgetAmountChange(value: String) =
        _addGoalState.update { it.copy(budgetAmount = value, amountError = null) }

    fun onCategorySelected(id: Int) =
        _addGoalState.update { it.copy(selectedCategoryId = id, categoryError = null) }

    fun saveGoal() {
        val state = _addGoalState.value
        var isValid = true

        val amountError = when {
            state.budgetAmount.isBlank()              -> "Budget amount is required"
            state.budgetAmount.toDoubleOrNull() == null -> "Enter a valid number"
            state.budgetAmount.toDouble() <= 0        -> "Amount must be greater than 0"
            else                                      -> null
        }
        val categoryError = if (state.selectedCategoryId == -1) "Select a category" else null

        if (amountError != null || categoryError != null) {
            isValid = false
            _addGoalState.update {
                it.copy(amountError = amountError, categoryError = categoryError)
            }
        }

        if (!isValid) return

        viewModelScope.launch {
            _addGoalState.update { it.copy(isLoading = true) }
            goalRepo.addGoal(
                Goal(
                    categoryId   = state.selectedCategoryId,
                    budgetLimit  = state.budgetAmount.toDouble(),
                    month        = currentMonth,
                    year         = currentYear
                )
            )
            _addGoalState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }



    fun deleteGoal(goal: Goal) {
        viewModelScope.launch { goalRepo.deleteGoal(goal) }
    }

    fun updateGoalAmount(goal: Goal, newAmount: Double) {
        viewModelScope.launch {
            goalRepo.updateGoal(goal.copy(budgetLimit = newAmount))
        }
    }

    fun resetAddGoalState() {
        _addGoalState.update {
            AddGoalUiState(availableCategories = it.availableCategories)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}