package com.example.budgie_budgetapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgie_budgetapp.data.dao.CategoryDao
import com.example.budgie_budgetapp.data.dao.UserDao
import com.example.budgie_budgetapp.data.entity.Category
import com.example.budgie_budgetapp.data.entity.User

@Database(entities = [User::class, Category::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val DEFAULT_CATEGORIES = listOf(
            "Groceries", "Transport", "Dining", "Entertainment",
            "Utilities", "Health", "Shopping", "Income"
        )

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgie_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        DEFAULT_CATEGORIES.forEach { name ->
                            db.execSQL(
                                "INSERT INTO categories (name, userId) VALUES (?, 0)",
                                arrayOf(name)
                            )
                        }
                    }
                })
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
