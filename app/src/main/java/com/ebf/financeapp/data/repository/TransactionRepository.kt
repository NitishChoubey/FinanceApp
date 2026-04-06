package com.ebf.financeapp.data.repository




import com.ebf.financeapp.data.local.DailyExpense
import com.ebf.financeapp.data.local.TransactionDao
import com.ebf.financeapp.data.model.CategorySpending
import com.ebf.financeapp.data.model.Transaction
import com.ebf.financeapp.data.model.TransactionType
import com.ebf.financeapp.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {

    // ─── Write ──────────────────────────────────────────────────────────────

    suspend fun addTransaction(transaction: Transaction) = dao.insert(transaction)

    suspend fun updateTransaction(transaction: Transaction) = dao.update(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = dao.delete(transaction)

    suspend fun deleteById(id: Int) = dao.deleteById(id)

    // ─── Read ───────────────────────────────────────────────────────────────

    fun getAllTransactions(): Flow<List<TransactionWithCategory>> =
        dao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> =
        dao.getTransactionsByType(type)

    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>> =
        dao.searchTransactions(query)

    suspend fun getTransactionById(id: Int): TransactionWithCategory? =
        dao.getTransactionById(id)

    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionWithCategory>> =
        dao.getRecentTransactions(limit)

    // ─── Dashboard Summaries ────────────────────────────────────────────────

    fun getTotalBalance(): Flow<Double> = dao.getTotalBalance()

    fun getMonthlyIncome(month: Int, year: String): Flow<Double> =
        dao.getMonthlyIncome(month, year)

    fun getMonthlyExpense(month: Int, year: String): Flow<Double> =
        dao.getMonthlyExpense(month, year)

    // ─── Goals / Insights ───────────────────────────────────────────────────

    fun getSpendingByCategory(month: Int, year: String): Flow<List<CategorySpending>> =
        dao.getSpendingByCategory(month, year)

    fun getSpentForCategoryInMonth(
        categoryId: Int,
        month: Int,
        year: String
    ): Flow<Double> = dao.getSpentForCategoryInMonth(categoryId, month, year)

    suspend fun getTotalExpenseInRange(from: Long, to: Long): Double =
        dao.getTotalExpenseInRange(from, to)

    suspend fun getDailyExpenses(fromEpoch: Long): List<DailyExpense> =
        dao.getDailyExpenses(fromEpoch)
}