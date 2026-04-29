package com.example.budgie_budgetapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.data.entity.BudgetGoal
import com.example.budgie_budgetapp.data.entity.Category
import com.example.budgie_budgetapp.utils.KEY_USER_ID
import com.example.budgie_budgetapp.utils.PREFS_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class BudgetGoalsActivity : AppCompatActivity() {

    private lateinit var budgetGoalsContainer: LinearLayout
    private lateinit var tvMonthYear: TextView

    private var userId = 0
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private var categories: List<Category> = emptyList()
    private var budgets: List<BudgetGoal> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budgetgoals)

        userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_USER_ID, 0)

        initializeViews()
        setupBackButton()
        setupMonthNavigation()
        setupNavigationButtons()
        loadData()
    }

    private fun initializeViews() {
        budgetGoalsContainer = findViewById(R.id.budgetGoalsContainer)
        tvMonthYear = findViewById(R.id.tvMonthYear)
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton?.setOnClickListener {
            finish()
        }
    }

    private fun setupMonthNavigation() {
        updateMonthYearDisplay()

        val prevButton = findViewById<ImageButton>(R.id.prevMonthButton)
        val nextButton = findViewById<ImageButton>(R.id.nextMonthButton)

        prevButton?.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(currentYear, currentMonth, 1)
            calendar.add(Calendar.MONTH, -1)
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            updateMonthYearDisplay()
            loadData()
        }

        nextButton?.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(currentYear, currentMonth, 1)
            calendar.add(Calendar.MONTH, 1)
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            updateMonthYearDisplay()
            loadData()
        }
    }

    private fun updateMonthYearDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        tvMonthYear.text = monthYearFormat.format(calendar.time)
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@BudgetGoalsActivity)

                categories = withContext(Dispatchers.IO) {
                    db.categoryDao().getCategoriesSync(userId)
                }.filter { it.name != "Income" }

                budgets = withContext(Dispatchers.IO) {
                    db.budgetGoalDao().getGoalsForMonth(userId, currentMonth + 1, currentYear)
                }

                withContext(Dispatchers.Main) {
                    displayBudgetGoals()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@BudgetGoalsActivity, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayBudgetGoals() {
        budgetGoalsContainer.removeAllViews()

        if (categories.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No categories available.\nAdd categories first in Manage Categories."
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 14f
                setPadding(16, 32, 16, 32)
                gravity = android.view.Gravity.CENTER
            }
            budgetGoalsContainer.addView(emptyText)
            return
        }

        val budgetMap = budgets.associateBy { it.categoryId }

        for (category in categories) {
            val budget = budgetMap[category.id]
            val card = createBudgetCard(category, budget)
            budgetGoalsContainer.addView(card)
        }
    }

    private fun createBudgetCard(category: Category, budget: BudgetGoal?): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.input_background)
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            isClickable = true
            setOnClickListener {
                showBudgetDialog(category, budget)
            }
        }

        // Header row with category name
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val categoryName = TextView(this).apply {
            text = category.name
            setTextColor(resources.getColor(R.color.yellow_accent, null))
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        headerRow.addView(categoryName)

        val statusHint = TextView(this).apply {
            text = if (budget != null) "📝 tap to edit" else "➕ tap to add budget"
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 11f
        }
        headerRow.addView(statusHint)

        card.addView(headerRow)

        // Budget details section
        val detailsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        }

        if (budget != null) {
            // Min Budget row
            val minRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val minLabel = TextView(this).apply {
                text = "Minimum Budget:"
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            minRow.addView(minLabel)

            val minValue = TextView(this).apply {
                text = if (budget.minBudget != null) {
                    "R${String.format("%.2f", budget.minBudget)}"
                } else {
                    "Not set"
                }
                setTextColor(resources.getColor(R.color.text_primary, null))
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            minRow.addView(minValue)
            detailsContainer.addView(minRow)

            // Max Budget row
            val maxRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
            }

            val maxLabel = TextView(this).apply {
                text = "Maximum Budget:"
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            maxRow.addView(maxLabel)

            val maxValue = TextView(this).apply {
                text = if (budget.maxBudget != null) {
                    "R${String.format("%.2f", budget.maxBudget)}"
                } else {
                    "Not set"
                }
                setTextColor(resources.getColor(R.color.text_primary, null))
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            maxRow.addView(maxValue)
            detailsContainer.addView(maxRow)
        } else {
            val noBudgetText = TextView(this).apply {
                text = "No budgets set"
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }
            detailsContainer.addView(noBudgetText)
        }

        card.addView(detailsContainer)
        return card
    }

    private fun showBudgetDialog(category: Category, existingBudget: BudgetGoal?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_budget_setup, null)
        val etMinBudget = dialogView.findViewById<EditText>(R.id.etMinBudget)
        val etMaxBudget = dialogView.findViewById<EditText>(R.id.etMaxBudget)
        val cbSetMin = dialogView.findViewById<CheckBox>(R.id.cbSetMin)
        val cbSetMax = dialogView.findViewById<CheckBox>(R.id.cbSetMax)

        // If editing existing budget, populate the fields
        if (existingBudget != null) {
            if (existingBudget.minBudget != null) {
                cbSetMin.isChecked = true
                etMinBudget.setText(existingBudget.minBudget.toString())
                etMinBudget.isEnabled = true
            }
            if (existingBudget.maxBudget != null) {
                cbSetMax.isChecked = true
                etMaxBudget.setText(existingBudget.maxBudget.toString())
                etMaxBudget.isEnabled = true
            }
        }

        cbSetMin.setOnCheckedChangeListener { _, isChecked ->
            etMinBudget.isEnabled = isChecked
            if (!isChecked) etMinBudget.text.clear()
        }

        cbSetMax.setOnCheckedChangeListener { _, isChecked ->
            etMaxBudget.isEnabled = isChecked
            if (!isChecked) etMaxBudget.text.clear()
        }

        AlertDialog.Builder(this)
            .setTitle(if (existingBudget != null) "Edit Budget for ${category.name}" else "Set Budget for ${category.name}")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val minBudget = if (cbSetMin.isChecked) {
                    etMinBudget.text.toString().toDoubleOrNull()
                } else null

                val maxBudget = if (cbSetMax.isChecked) {
                    etMaxBudget.text.toString().toDoubleOrNull()
                } else null

                if ((minBudget == null || minBudget <= 0) && (maxBudget == null || maxBudget <= 0)) {
                    Toast.makeText(this, "Please set at least one valid budget amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (minBudget != null && minBudget <= 0) {
                    Toast.makeText(this, "Minimum budget must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (maxBudget != null && maxBudget <= 0) {
                    Toast.makeText(this, "Maximum budget must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    if (existingBudget != null) {
                        updateBudgetGoal(existingBudget.id, category.id, category.name, minBudget, maxBudget)
                    } else {
                        createBudgetGoal(category.id, category.name, minBudget, maxBudget)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete") { _, _ ->
                if (existingBudget != null) {
                    confirmDeleteBudget(category, existingBudget)
                } else {
                    Toast.makeText(this, "No budget to delete", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private suspend fun createBudgetGoal(categoryId: Int, categoryName: String, minBudget: Double?, maxBudget: Double?) {
        val db = AppDatabase.getDatabase(this)
        val newBudget = BudgetGoal(
            categoryId = categoryId,
            categoryName = categoryName,
            minBudget = minBudget,
            maxBudget = maxBudget,
            userId = userId,
            month = currentMonth + 1,
            year = currentYear
        )

        withContext(Dispatchers.IO) {
            db.budgetGoalDao().insert(newBudget)
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@BudgetGoalsActivity, "Budget saved for $categoryName", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }

    private suspend fun updateBudgetGoal(budgetId: Int, categoryId: Int, categoryName: String, minBudget: Double?, maxBudget: Double?) {
        val db = AppDatabase.getDatabase(this)
        val updatedBudget = BudgetGoal(
            id = budgetId,
            categoryId = categoryId,
            categoryName = categoryName,
            minBudget = minBudget,
            maxBudget = maxBudget,
            userId = userId,
            month = currentMonth + 1,
            year = currentYear
        )

        withContext(Dispatchers.IO) {
            db.budgetGoalDao().insert(updatedBudget)
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@BudgetGoalsActivity, "Budget updated for $categoryName", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }

    private fun confirmDeleteBudget(category: Category, budget: BudgetGoal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Budget")
            .setMessage("Delete budget for ${category.name}? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(this@BudgetGoalsActivity)
                    withContext(Dispatchers.IO) {
                        db.budgetGoalDao().delete(budget.id)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@BudgetGoalsActivity, "Budget deleted for ${category.name}", Toast.LENGTH_SHORT).show()
                        loadData()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupNavigationButtons() {
        try {
            val navHome = findViewById<ImageButton>(R.id.navHome)
            val navHistory = findViewById<ImageButton>(R.id.navHistory)
            val navReports = findViewById<ImageButton>(R.id.navReports)
            val navProfile = findViewById<ImageButton>(R.id.navProfile)
            val menuButton = findViewById<ImageButton>(R.id.menuButton)

            navHome?.setOnClickListener {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }

            navHistory?.setOnClickListener {
                startActivity(Intent(this, HistoryActivity::class.java))
                finish()
            }

            navReports?.setOnClickListener {
                finish()
            }

            navProfile?.setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }

            menuButton?.setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
