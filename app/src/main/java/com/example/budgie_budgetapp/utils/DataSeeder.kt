package com.example.budgie_budgetapp.utils

import android.content.Context
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.data.entity.BudgetGoal
import com.example.budgie_budgetapp.data.entity.Category
import com.example.budgie_budgetapp.data.entity.Transaction
import java.util.Calendar

object DataSeeder {

    private const val PREF_SEEDED = "data_seeded_v1"

    suspend fun seedIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(PREF_SEEDED, false)) return

        val db = AppDatabase.getDatabase(context)
        val guestUserId = 0

        // ── Categories ──────────────────────────────────────────────────
        val categoryNames = listOf("Food", "Transport", "Entertainment", "Shopping", "Health", "Utilities", "Income")
        val categories = categoryNames.map { Category(name = it, userId = guestUserId) }
        db.categoryDao().insertAll(categories)

        // Re-fetch to get auto-generated IDs
        val saved = db.categoryDao().getCategoriesSync(guestUserId)
        fun catId(name: String) = saved.firstOrNull { it.name == name }?.id ?: 1

        val foodId        = catId("Food")
        val transportId   = catId("Transport")
        val entertainId   = catId("Entertainment")
        val shoppingId    = catId("Shopping")
        val healthId      = catId("Health")
        val utilitiesId   = catId("Utilities")
        val incomeId      = catId("Income")

        // ── Date helpers ─────────────────────────────────────────────────
        fun daysAgo(n: Int): java.util.Date {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -n)
            cal.set(Calendar.HOUR_OF_DAY, 8 + (n % 12))
            cal.set(Calendar.MINUTE, (n * 7) % 60)
            cal.set(Calendar.SECOND, 0)
            return cal.time
        }

        val month  = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year   = Calendar.getInstance().get(Calendar.YEAR)

        // ── Transactions ─────────────────────────────────────────────────
        val transactions = listOf(
            // ── Today ──
            Transaction(amount = 15000.00, categoryId = incomeId,      categoryName = "Income",         type = "income",  description = "Monthly salary deposit",         date = daysAgo(0), startTime = "08:00", endTime = "08:05", userId = guestUserId),
            Transaction(amount = 89.99,   categoryId = foodId,         categoryName = "Food",           type = "expense", description = "Woolworths grocery run",          date = daysAgo(0), userId = guestUserId),
            Transaction(amount = 35.00,   categoryId = transportId,    categoryName = "Transport",      type = "expense", description = "Uber to work",                    date = daysAgo(0), startTime = "07:30", endTime = "08:10", userId = guestUserId),

            // ── Yesterday ──
            Transaction(amount = 120.00,  categoryId = entertainId,    categoryName = "Entertainment",  type = "expense", description = "Netflix & Spotify subscriptions", date = daysAgo(1), userId = guestUserId),
            Transaction(amount = 55.50,   categoryId = foodId,         categoryName = "Food",           type = "expense", description = "Lunch at Vida e Caffè",           date = daysAgo(1), startTime = "12:30", endTime = "13:00", userId = guestUserId),
            Transaction(amount = 500.00,  categoryId = shoppingId,     categoryName = "Shopping",       type = "expense", description = "Clothing from Zara",              date = daysAgo(1), userId = guestUserId),

            // ── 3 days ago ──
            Transaction(amount = 250.00,  categoryId = utilitiesId,    categoryName = "Utilities",      type = "expense", description = "Electricity bill payment",        date = daysAgo(3), userId = guestUserId),
            Transaction(amount = 45.00,   categoryId = transportId,    categoryName = "Transport",      type = "expense", description = "Petrol top-up",                  date = daysAgo(3), userId = guestUserId),
            Transaction(amount = 2500.00, categoryId = incomeId,       categoryName = "Income",         type = "income",  description = "Freelance design project",        date = daysAgo(3), userId = guestUserId),

            // ── 5 days ago ──
            Transaction(amount = 199.00,  categoryId = healthId,       categoryName = "Health",         type = "expense", description = "Gym monthly membership",          date = daysAgo(5), userId = guestUserId),
            Transaction(amount = 75.00,   categoryId = foodId,         categoryName = "Food",           type = "expense", description = "Checkers weekly groceries",       date = daysAgo(5), userId = guestUserId),

            // ── 8 days ago ──
            Transaction(amount = 320.00,  categoryId = shoppingId,     categoryName = "Shopping",       type = "expense", description = "Takealot order — books & tech",   date = daysAgo(8), userId = guestUserId),
            Transaction(amount = 88.00,   categoryId = utilitiesId,    categoryName = "Utilities",      type = "expense", description = "Water & rates municipality",      date = daysAgo(8), userId = guestUserId),

            // ── 12 days ago ──
            Transaction(amount = 160.00,  categoryId = entertainId,    categoryName = "Entertainment",  type = "expense", description = "Movie night + popcorn for two",   date = daysAgo(12), userId = guestUserId),
            Transaction(amount = 1200.00, categoryId = incomeId,       categoryName = "Income",         type = "income",  description = "Side hustle tutoring session",    date = daysAgo(12), userId = guestUserId),
            Transaction(amount = 42.00,   categoryId = transportId,    categoryName = "Transport",      type = "expense", description = "MyCiTi monthly top-up",           date = daysAgo(12), userId = guestUserId),

            // ── 18 days ago ──
            Transaction(amount = 650.00,  categoryId = healthId,       categoryName = "Health",         type = "expense", description = "Doctor visit + medication",       date = daysAgo(18), userId = guestUserId),
            Transaction(amount = 95.00,   categoryId = foodId,         categoryName = "Food",           type = "expense", description = "Spur family dinner",              date = daysAgo(18), startTime = "18:00", endTime = "19:45", userId = guestUserId),

            // ── 22 days ago ──
            Transaction(amount = 110.00,  categoryId = utilitiesId,    categoryName = "Utilities",      type = "expense", description = "Internet bill — Vumatel",         date = daysAgo(22), userId = guestUserId),
            Transaction(amount = 780.00,  categoryId = shoppingId,     categoryName = "Shopping",       type = "expense", description = "Home decor & curtains",           date = daysAgo(22), userId = guestUserId),

            // ── 28 days ago ──
            Transaction(amount = 300.00,  categoryId = entertainId,    categoryName = "Entertainment",  type = "expense", description = "Weekend trip petty cash",         date = daysAgo(28), userId = guestUserId),
            Transaction(amount = 500.00,  categoryId = incomeId,       categoryName = "Income",         type = "income",  description = "Birthday gift received",          date = daysAgo(28), userId = guestUserId)
        )

        transactions.forEach { db.transactionDao().insert(it) }

        // ── Budget Goals (current month) ────────────────────────────────
        val goals = listOf(
            BudgetGoal(categoryId = foodId,       categoryName = "Food",          minBudget = 200.0,  maxBudget = 600.0,  userId = guestUserId, month = month, year = year),
            BudgetGoal(categoryId = transportId,  categoryName = "Transport",     minBudget = 50.0,   maxBudget = 300.0,  userId = guestUserId, month = month, year = year),
            BudgetGoal(categoryId = entertainId,  categoryName = "Entertainment", minBudget = null,   maxBudget = 400.0,  userId = guestUserId, month = month, year = year),
            BudgetGoal(categoryId = shoppingId,   categoryName = "Shopping",      minBudget = null,   maxBudget = 800.0,  userId = guestUserId, month = month, year = year),
            BudgetGoal(categoryId = healthId,     categoryName = "Health",        minBudget = 100.0,  maxBudget = 1000.0, userId = guestUserId, month = month, year = year),
            BudgetGoal(categoryId = utilitiesId,  categoryName = "Utilities",     minBudget = 200.0,  maxBudget = 500.0,  userId = guestUserId, month = month, year = year)
        )

        goals.forEach { db.budgetGoalDao().insert(it) }

        // Mark seeding done
        prefs.edit().putBoolean(PREF_SEEDED, true).apply()
    }
}
