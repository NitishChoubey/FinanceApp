package com.ebf.financeapp.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebf.financeapp.data.local.DailyExpense
import com.ebf.financeapp.data.model.CategorySpending
import com.ebf.financeapp.data.remote.ApiRepository
import com.ebf.financeapp.data.remote.ApiResult
import com.ebf.financeapp.data.remote.FinancialTip
import com.ebf.financeapp.data.repository.TransactionRepository
import com.ebf.financeapp.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class WeekComparison(
    val thisWeekTotal: Double,
    val lastWeekTotal: Double,
    val changePercent: Double,
    val isHigher: Boolean
)

data class InsightsUiState(
    val categorySpending: List<CategorySpending> = emptyList(),
    val topCategory: CategorySpending? = null,
    val weekComparison: WeekComparison? = null,
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val chartDayLabels: List<String> = emptyList(),
    val thisMonthExpense: Double = 0.0,
    val lastMonthExpense: Double = 0.0,
    val monthChangePercent: Double = 0.0,
    val isMonthHigher: Boolean = false,
    val currentMonthLabel: String = "",
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val error: String? = null,
    // ── AI Tips ──────────────────────────────────────────────
    val financialTips: List<FinancialTip> = emptyList(),
    val tipsLoading: Boolean = false
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val apiRepo: ApiRepository          // ← injected here
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private val currentMonth = DateFormatter.getCurrentMonth()
    private val currentYear  = DateFormatter.getCurrentYearString()

    init {
        loadInsights()
        loadTips()          // ← called on init
    }

    private fun loadInsights() {
        viewModelScope.launch {
            transactionRepo.getSpendingByCategory(currentMonth, currentYear)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { categoryList ->
                    val weekComparison  = buildWeekComparison()
                    val dailyData       = buildDailyExpenses()
                    val monthComparison = buildMonthComparison()

                    _uiState.update {
                        it.copy(
                            categorySpending   = categoryList,
                            topCategory        = categoryList.firstOrNull(),
                            weekComparison     = weekComparison,
                            dailyExpenses      = dailyData.first,
                            chartDayLabels     = dailyData.second,
                            thisMonthExpense   = monthComparison.first,
                            lastMonthExpense   = monthComparison.second,
                            monthChangePercent = monthComparison.third,
                            isMonthHigher      = monthComparison.first > monthComparison.second,
                            currentMonthLabel  = DateFormatter.formatMonthYear(System.currentTimeMillis()),
                            isLoading          = false,
                            isEmpty            = categoryList.isEmpty()
                        )
                    }
                }
        }
    }

    private fun loadTips() {
        viewModelScope.launch {
            _uiState.update { it.copy(tipsLoading = true) }
            when (val result = apiRepo.getFinancialTips()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            financialTips = result.data.tips,
                            tipsLoading   = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(tipsLoading = false) }
                }
                else -> {
                    _uiState.update { it.copy(tipsLoading = false) }
                }
            }
        }
    }

    private suspend fun buildWeekComparison(): WeekComparison {
        val now   = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val thisWeekStart = cal.timeInMillis

        val lastWeekEnd   = thisWeekStart - 1
        val lastWeekStart = thisWeekStart - (7 * dayMs)

        val thisWeek = transactionRepo.getTotalExpenseInRange(thisWeekStart, now)
        val lastWeek = transactionRepo.getTotalExpenseInRange(lastWeekStart, lastWeekEnd)

        val changePercent = when {
            lastWeek > 0  -> ((thisWeek - lastWeek) / lastWeek) * 100
            thisWeek > 0  -> 100.0
            else          -> 0.0
        }

        return WeekComparison(
            thisWeekTotal = thisWeek,
            lastWeekTotal = lastWeek,
            changePercent = changePercent,
            isHigher      = thisWeek > lastWeek
        )
    }

    private suspend fun buildDailyExpenses(): Pair<List<DailyExpense>, List<String>> {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 86_400_000L)
        val raw          = transactionRepo.getDailyExpenses(sevenDaysAgo)
        val dayLabels    = DateFormatter.lastNDays(7)
        val rawMap       = raw.associateBy { it.day }

        val filled = dayLabels.map { (epochStart, _) ->
            val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date(epochStart))
            DailyExpense(day = dateKey, total = rawMap[dateKey]?.total ?: 0.0)
        }

        return Pair(filled, dayLabels.map { it.second })
    }

    private suspend fun buildMonthComparison(): Triple<Double, Double, Double> {
        val cal = Calendar.getInstance()

        val thisMonthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val thisMonthEnd = System.currentTimeMillis()

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, -1)
        val lastMonthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val lastMonthEnd = cal.timeInMillis

        val thisMo = transactionRepo.getTotalExpenseInRange(thisMonthStart, thisMonthEnd)
        val lastMo = transactionRepo.getTotalExpenseInRange(lastMonthStart, lastMonthEnd)
        val change = if (lastMo > 0) ((thisMo - lastMo) / lastMo) * 100 else 0.0

        return Triple(thisMo, lastMo, change)
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}