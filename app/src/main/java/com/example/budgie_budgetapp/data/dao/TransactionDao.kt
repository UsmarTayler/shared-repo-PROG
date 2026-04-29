package com.example.budgie_budgetapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgie_budgetapp.data.entity.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsForUser(userId: Int): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDatesLive(userId: Int, startDate: Date, endDate: Date): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsBetweenDates(userId: Int, startDate: Date, endDate: Date): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTransactionsByType(userId: Int, type: String, startDate: Date, endDate: Date): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByType(userId: Int, type: String, startDate: Date, endDate: Date): Double?

    @Query("SELECT categoryName, SUM(amount) as total FROM transactions WHERE userId = :userId AND type = 'expense' AND date BETWEEN :startDate AND :endDate GROUP BY categoryName ORDER BY total DESC")
    suspend fun getExpenseByCategory(userId: Int, startDate: Date, endDate: Date): List<CategoryExpense>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDatesFlow(userId: Int, startDate: Date, endDate: Date): Flow<List<Transaction>>

    @Query("SELECT categoryName, SUM(amount) as total, type FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND type = :type GROUP BY categoryName")
    suspend fun getCategoryTotals(userId: Int, startDate: Date, endDate: Date, type: String): List<CategoryTotal>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentTransactions(userId: Int, limit: Int): List<Transaction>
}

data class CategoryExpense(val categoryName: String, val total: Double)
data class CategoryTotal(val categoryName: String, val total: Double, val type: String)
