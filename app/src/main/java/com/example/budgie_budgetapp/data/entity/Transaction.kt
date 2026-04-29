package com.example.budgie_budgetapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val categoryId: Int,
    val categoryName: String,
    val type: String,
    val description: String,
    val date: Date,
    val startTime: String? = null,
    val endTime: String? = null,
    val photoPath: String? = null,
    val userId: Int = 0
)
