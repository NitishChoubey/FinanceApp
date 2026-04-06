package com.ebf.financeapp.data.local



import androidx.room.*
import com.ebf.financeapp.data.model.CategorySpending
import com.ebf.financeapp.data.model.Transaction
import com.ebf.financeapp.data.model.TransactionType
import com.ebf.financeapp.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // ─── Insert / Update / Delete ──────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    // ─── Fetch All (with Category join)

    @androidx.room.Transaction
    @Query("""
        SELECT * FROM transactions
        ORDER BY date DESC
    """)
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    // ─── Fetch by Type

    @androidx.room.Transaction
    @Query("""
        SELECT * FROM transactions
        WHERE type = :type
        ORDER BY date DESC
    """)
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>>

    // ─── Fetch Single ───

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionWithCategory?

    // ─── Search ────────

    @androidx.room.Transaction
    @Query("""
        SELECT t.* FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.title LIKE '%' || :query || '%'
           OR t.note  LIKE '%' || :query || '%'
           OR c.name  LIKE '%' || :query || '%'
        ORDER BY t.date DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>>

    // ─── Monthly Summaries ───────

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'INCOME'
          AND strftime('%m', date / 1000, 'unixepoch') = printf('%02d', :month)
          AND strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getMonthlyIncome(month: Int, year: String): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'EXPENSE'
          AND strftime('%m', date / 1000, 'unixepoch') = printf('%02d', :month)
          AND strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getMonthlyExpense(month: Int, year: String): Flow<Double>

    // ─── Category Spending (for Goals + Insights) ───────────────────────────

    @Query("""
        SELECT t.categoryId,
               c.name  AS categoryName,
               c.colorHex,
               SUM(t.amount) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE'
          AND strftime('%m', t.date / 1000, 'unixepoch') = printf('%02d', :month)
          AND strftime('%Y', t.date / 1000, 'unixepoch') = :year
        GROUP BY t.categoryId
        ORDER BY totalAmount DESC
    """)
    fun getSpendingByCategory(month: Int, year: String): Flow<List<CategorySpending>>

    // ─── Weekly Trend (last 7 days, daily totals) ───────────────────────────

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'EXPENSE'
          AND date >= :fromEpoch
          AND date <= :toEpoch
    """)
    fun getExpenseBetween(fromEpoch: Long, toEpoch: Long): Flow<Double>

    // ─── All-time Balance ───────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0.0) FROM transactions")
    fun getTotalBalance(): Flow<Double>

    // ─── Recent Transactions (for Home dashboard) ───────────────────────────

    @androidx.room.Transaction
    @Query("""
        SELECT * FROM transactions
        ORDER BY date DESC
        LIMIT :limit
    """)
    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionWithCategory>>

    // ─── Transactions for a specific category + month ──────────────────────

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'EXPENSE'
          AND categoryId = :categoryId
          AND strftime('%m', date / 1000, 'unixepoch') = printf('%02d', :month)
          AND strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getSpentForCategoryInMonth(
        categoryId: Int,
        month: Int,
        year: String
    ): Flow<Double>

    // ─── Insights: this week vs last week ──────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'EXPENSE'
          AND date >= :from AND date < :to
    """)
    suspend fun getTotalExpenseInRange(from: Long, to: Long): Double

    // ─── Daily expense for last N days (bar chart data) ────────────────────

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', date / 1000, 'unixepoch') AS day,
            SUM(amount) AS total
        FROM transactions
        WHERE type = 'EXPENSE'
          AND date >= :fromEpoch
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getDailyExpenses(fromEpoch: Long): List<DailyExpense>
}

// Lightweight projection for bar chart
data class DailyExpense(
    val day: String,
    val total: Double
)