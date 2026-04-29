package com.example.budgie_budgetapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgie_budgetapp.data.entity.BudgetGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: BudgetGoal)

    @Query("DELETE FROM budget_goals WHERE id = :goalId")
    suspend fun delete(goalId: Int)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getGoalsForMonth(userId: Int, month: Int, year: Int): List<BudgetGoal>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND categoryId = :categoryId AND month = :month AND year = :year")
    suspend fun getGoalForCategory(userId: Int, categoryId: Int, month: Int, year: Int): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE userId = :userId")
    fun getAllGoalsFlow(userId: Int): Flow<List<BudgetGoal>>
}
