package com.example.budgie_budgetapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgie_budgetapp.data.entity.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE userId = 0 OR userId = :userId ORDER BY name ASC")
    fun getCategoriesForUser(userId: Int): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = 0 OR userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesSync(userId: Int): List<Category>

    @Query("SELECT name FROM categories WHERE userId = 0 OR userId = :userId ORDER BY name ASC")
    suspend fun getCategoryNames(userId: Int): List<String>
}
