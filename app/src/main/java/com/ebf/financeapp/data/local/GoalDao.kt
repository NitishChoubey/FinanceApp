package com.ebf.financeapp.data.local



import androidx.room.*
import com.ebf.financeapp.data.model.Goal
import com.ebf.financeapp.data.model.GoalWithCategory

import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Int)

    // All goals for a given month/year (with their category)
    @Transaction
    @Query("""
        SELECT * FROM goals
        WHERE month = :month AND year = :year
        ORDER BY categoryId ASC
    """)
    fun getGoalsForMonth(month: Int, year: Int): Flow<List<GoalWithCategory>>

    // Check if a goal already exists for category + month
    @Query("""
        SELECT COUNT(*) FROM goals
        WHERE categoryId = :categoryId
          AND month = :month
          AND year = :year
    """)
    suspend fun goalExistsForCategory(categoryId: Int, month: Int, year: Int): Int

    // Get a single goal by id
    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Int): GoalWithCategory?

    // All goals ever (useful for summary)
    @Transaction
    @Query("SELECT * FROM goals ORDER BY year DESC, month DESC")
    fun getAllGoals(): Flow<List<GoalWithCategory>>
}