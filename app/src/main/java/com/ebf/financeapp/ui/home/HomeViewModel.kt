package com.ebf.financeapp.ui.home



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebf.financeapp.data.model.TransactionWithCategory
import com.ebf.financeapp.data.remote.FinancialTip
import com.ebf.financeapp.data.repository.TransactionRepository
import com.ebf.financeapp.ui.insights.SpendingForecast
import com.ebf.financeapp.ui.insights.buildForecast
import com.ebf.financeapp.util.DateFormatter

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlySavings: Double = 0.0,
    val savingsRate: Float = 0f,              // 0f–1f for progress bar
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val spendingByCategory: List<CategoryPieSlice> = emptyList(),
    val currentMonthLabel: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val healthScore: HealthScore? = null,
    val forecast: SpendingForecast? = null,
    val financialTips: List<FinancialTip> = emptyList(),
    val hasTransactionsThisWeek: Boolean = false
)

data class CategoryPieSlice(
    val name: String,
    val amount: Double,
    val colorHex: String,
    val percentage: Float   // 0f–100f
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentMonth = DateFormatter.getCurrentMonth()
    private val currentYear  = DateFormatter.getCurrentYearString()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                currentMonthLabel = DateFormatter.formatMonthYear(System.currentTimeMillis())
            )}

            // Combine all 4 flows into one atomic update
            combine(
                transactionRepo.getTotalBalance(),
                transactionRepo.getMonthlyIncome(currentMonth, currentYear),
                transactionRepo.getMonthlyExpense(currentMonth, currentYear),
                transactionRepo.getRecentTransactions(5),
                transactionRepo.getSpendingByCategory(currentMonth, currentYear),

            ) { balance, income, expense, recent, categorySpending ->

                val savings    = income - expense
                val savingsRate = if (income > 0) (savings / income).toFloat().coerceIn(0f, 1f) else 0f

                // Build pie slices with percentages
                val totalSpend = categorySpending.sumOf { it.totalAmount }
                val slices = categorySpending.map { cs ->
                    CategoryPieSlice(
                        name       = cs.categoryName,
                        amount     = cs.totalAmount,
                        colorHex   = cs.colorHex,
                        percentage = if (totalSpend > 0)
                            ((cs.totalAmount / totalSpend) * 100f).toFloat()
                        else 0f
                    )
                }

                val hasThisWeek = recent.any {
                    it.transaction.date >= System.currentTimeMillis() - (7 * 86_400_000L)
                }
                val healthScore = calculateHealthScore(
                    savingsRate      = savingsRate,
                    budgetAdherence  = 1f,   // updated when GoalViewModel provides it
                    hasTransactionsThisWeek = hasThisWeek
                )
                val forecast = buildForecast(expense, income)



                HomeUiState(
                    totalBalance         = balance,
                    monthlyIncome        = income,
                    monthlyExpense       = expense,
                    monthlySavings       = savings,
                    savingsRate          = savingsRate,
                    recentTransactions   = recent,
                    spendingByCategory   = slices,
                    currentMonthLabel    = DateFormatter.formatMonthYear(System.currentTimeMillis()),
                    isLoading            = false ,
                    healthScore        = healthScore,
                    forecast           = forecast ,
                )




            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { state -> _uiState.value = state }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}