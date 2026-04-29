package com.example.budgie_budgetapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val categoryName: String,
    val minBudget: Double? = null,
    val maxBudget: Double? = null,
    val userId: Int = 0,
    val month: Int,
    val year: Int
)
