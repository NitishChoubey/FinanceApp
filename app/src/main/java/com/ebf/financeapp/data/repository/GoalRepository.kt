package com.ebf.financeapp.data.repository




import com.ebf.financeapp.data.local.GoalDao
import com.ebf.financeapp.data.model.Goal
import com.ebf.financeapp.data.model.GoalWithCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val dao: GoalDao
) {

    suspend fun addGoal(goal: Goal) = dao.insert(goal)

    suspend fun updateGoal(goal: Goal) = dao.update(goal)

    suspend fun deleteGoal(goal: Goal) = dao.delete(goal)

    suspend fun deleteById(id: Int) = dao.deleteById(id)

    fun getGoalsForMonth(month: Int, year: Int): Flow<List<GoalWithCategory>> =
        dao.getGoalsForMonth(month, year)

    fun getAllGoals(): Flow<List<GoalWithCategory>> = dao.getAllGoals()

    suspend fun getGoalById(id: Int): GoalWithCategory? = dao.getGoalById(id)

    suspend fun goalExistsForCategory(
        categoryId: Int, month: Int, year: Int
    ): Boolean = dao.goalExistsForCategory(categoryId, month, year) > 0
}