package com.example.budgie_budgetapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.data.dao.CategoryTotal
import com.example.budgie_budgetapp.data.entity.BudgetGoal
import com.example.budgie_budgetapp.data.entity.Category
import com.example.budgie_budgetapp.utils.DateRangeHelper
import com.example.budgie_budgetapp.utils.KEY_USER_ID
import com.example.budgie_budgetapp.utils.PREFS_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var categoryBreakdownContainer: LinearLayout
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var filterSpinner: Spinner
    private lateinit var btnSetBudget: Button

    private var currentFilter = "Month"
    private var userId = 0
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_USER_ID, 0)

        initializeViews()
        setupFilterSpinner()
        setupBudgetButton()
        setupNavigation()
        setupSettingsButton()
        loadReports()
    }

    private fun initializeViews() {
        categoryBreakdownContainer = findViewById(R.id.categoryBreakdownContainer)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        filterSpinner = findViewById(R.id.filterSpinner)
        btnSetBudget = findViewById(R.id.btnSetBudget)
    }

    private fun setupFilterSpinner() {
        val filters = arrayOf("Month", "Week", "Day")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        filterSpinner.adapter = adapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentFilter = filters[position]
                loadReports()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupBudgetButton() {
        findViewById<Button>(R.id.btnSetBudget).setOnClickListener {
            startActivity(Intent(this, BudgetGoalsActivity::class.java))
        }
    }

    private fun showBudgetDialog() {
        val budgetOptions = arrayOf("Set Min Budget", "Set Max Budget (Warning)", "View/Edit Budgets", "Delete Budget")

        AlertDialog.Builder(this)
            .setTitle("Budget Goals")
            .setItems(budgetOptions) { _, which ->
                when (which) {
                    0 -> loadCategoriesAndShowBudgetDialog("min")
                    1 -> loadCategoriesAndShowBudgetDialog("max")
                    2 -> showViewBudgetsDialog()
                    3 -> showDeleteBudgetDialog()
                }
            }
            .show()
    }

    private fun loadCategoriesAndShowBudgetDialog(type: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ReportsActivity)
            val categories = withContext(Dispatchers.IO) {
                db.categoryDao().getCategoriesSync(userId)
            }.filter { it.name != "Income" }

            showSetBudgetDialog(categories, type)
        }
    }

    private fun showSetBudgetDialog(categories: List<Category>, type: String) {
        val categoryNames = categories.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Category for ${if (type == "min") "Minimum" else "Maximum"} Budget")
            .setItems(categoryNames) { _, which ->
                val selectedCategory = categories[which]
                val promptLabel = if (type == "min") "Minimum monthly budget (R)" else "Maximum monthly budget (R - warning threshold)"

                val input = EditText(this).apply {
                    inputType = android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    hint = "Enter amount"
                }

                AlertDialog.Builder(this)
                    .setTitle("${if (type == "min") "Min" else "Max"} Budget for ${selectedCategory.name}")
                    .setMessage(promptLabel)
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val amount = input.text.toString().toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            lifecycleScope.launch {
                                saveBudgetGoal(selectedCategory.id, selectedCategory.name, amount, type)
                            }
                        } else {
                            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }

    private suspend fun saveBudgetGoal(categoryId: Int, categoryName: String, amount: Double, type: String) {
        val db = AppDatabase.getDatabase(this)
        val existingGoal = withContext(Dispatchers.IO) {
            db.budgetGoalDao().getGoalForCategory(userId, categoryId, currentMonth, currentYear)
        }

        val updatedGoal = if (existingGoal != null) {
            if (type == "min") {
                existingGoal.copy(minBudget = amount)
            } else {
                existingGoal.copy(maxBudget = amount)
            }
        } else {
            BudgetGoal(
                categoryId = categoryId,
                categoryName = categoryName,
                minBudget = if (type == "min") amount else null,
                maxBudget = if (type == "max") amount else null,
                userId = userId,
                month = currentMonth,
                year = currentYear
            )
        }

        withContext(Dispatchers.IO) {
            db.budgetGoalDao().insert(updatedGoal)
        }

        Toast.makeText(this, "Budget saved for $categoryName", Toast.LENGTH_SHORT).show()
        loadReports()
    }

    private fun showViewBudgetsDialog() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ReportsActivity)
            val goals = withContext(Dispatchers.IO) {
                db.budgetGoalDao().getGoalsForMonth(userId, currentMonth, currentYear)
            }

            if (goals.isEmpty()) {
                Toast.makeText(this@ReportsActivity, "No budgets set for this month", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val goalText = goals.joinToString("\n\n") { goal ->
                buildString {
                    appendLine("📊 ${goal.categoryName}")
                    if (goal.minBudget != null) {
                        appendLine("   Minimum: R${String.format("%.2f", goal.minBudget)}")
                    }
                    if (goal.maxBudget != null) {
                        appendLine("   Maximum: R${String.format("%.2f", goal.maxBudget)}")
                    }
                    if (goal.minBudget == null && goal.maxBudget == null) {
                        appendLine("   No budgets set")
                    }
                }
            }

            AlertDialog.Builder(this@ReportsActivity)
                .setTitle("Current Budget Goals")
                .setMessage(goalText)
                .setPositiveButton("Close", null)
                .show()
        }
    }

    private fun showDeleteBudgetDialog() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ReportsActivity)
            val goals = withContext(Dispatchers.IO) {
                db.budgetGoalDao().getGoalsForMonth(userId, currentMonth, currentYear)
            }

            if (goals.isEmpty()) {
                Toast.makeText(this@ReportsActivity, "No budgets to delete", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val goalNames = goals.map { it.categoryName }.toTypedArray()

            AlertDialog.Builder(this@ReportsActivity)
                .setTitle("Delete Budget For...")
                .setItems(goalNames) { _, which ->
                    val goalToDelete = goals[which]
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            db.budgetGoalDao().delete(goalToDelete.id)
                        }
                        Toast.makeText(this@ReportsActivity, "Budget deleted", Toast.LENGTH_SHORT).show()
                        loadReports()
                    }
                }
                .show()
        }
    }

    private fun loadReports() {
        val (startDate, endDate) = DateRangeHelper.getDateRange(currentFilter)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ReportsActivity)

            val totalIncome = withContext(Dispatchers.IO) {
                db.transactionDao().getTotalByType(userId, "income", startDate, endDate) ?: 0.0
            }
            val totalExpenses = withContext(Dispatchers.IO) {
                db.transactionDao().getTotalByType(userId, "expense", startDate, endDate) ?: 0.0
            }
            val categoryTotals = withContext(Dispatchers.IO) {
                db.transactionDao().getCategoryTotals(userId, startDate, endDate, "expense")
            }
            val budgetGoals = withContext(Dispatchers.IO) {
                db.budgetGoalDao().getGoalsForMonth(userId, currentMonth, currentYear)
            }

            withContext(Dispatchers.Main) {
                displaySummary(totalIncome, totalExpenses)
                displayCategoryBreakdown(categoryTotals, budgetGoals)
            }
        }
    }

    private fun displaySummary(totalIncome: Double, totalExpenses: Double) {
        tvTotalIncome.text = "R${String.format("%.2f", totalIncome)}"
        tvTotalExpenses.text = "R${String.format("%.2f", totalExpenses)}"
    }

    private fun displayCategoryBreakdown(categoryTotals: List<CategoryTotal>, budgetGoals: List<BudgetGoal>) {
        categoryBreakdownContainer.removeAllViews()

        if (categoryTotals.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No expenses recorded for this period"
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 14f
                setPadding(16, 32, 16, 32)
                gravity = android.view.Gravity.CENTER
            }
            categoryBreakdownContainer.addView(emptyText)
            return
        }

        for (total in categoryTotals) {
            val goal = budgetGoals.find { it.categoryName.equals(total.categoryName, ignoreCase = true) }
            val layout = createCategoryCard(total, goal)
            categoryBreakdownContainer.addView(layout)
        }
    }

    private fun createCategoryCard(total: CategoryTotal, goal: BudgetGoal?): LinearLayout {
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
        }

        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val nameText = TextView(this).apply {
            text = total.categoryName
            setTextColor(resources.getColor(R.color.text_primary, null))
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        headerRow.addView(nameText)

        val amountText = TextView(this).apply {
            text = "-R${String.format("%.2f", total.total)}"
            setTextColor(resources.getColor(R.color.expense_red, null))
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        headerRow.addView(amountText)

        card.addView(headerRow)

        if (goal != null && (goal.minBudget != null || goal.maxBudget != null)) {
            val budgetSection = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 12
                }
            }

            if (goal.minBudget != null) {
                val minRow = createBudgetRow("Minimum Budget", goal.minBudget, total.total, "min")
                budgetSection.addView(minRow)
            }

            if (goal.maxBudget != null) {
                val maxRow = createBudgetRow("Maximum Budget", goal.maxBudget, total.total, "max")
                budgetSection.addView(maxRow)
            }

            card.addView(budgetSection)
        } else {
            val noBudgetText = TextView(this).apply {
                text = "💡 No budget set. Tap 'Set Budget Goals' above to set min/max budgets."
                textSize = 12f
                setTextColor(resources.getColor(R.color.text_secondary, null))
                setPadding(0, 12, 0, 0)
            }
            card.addView(noBudgetText)
        }

        return card
    }

    private fun createBudgetRow(label: String, budgetAmount: Double, actualSpent: Double, type: String): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
        }

        val labelRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val labelText = TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        labelRow.addView(labelText)

        val budgetText = TextView(this).apply {
            text = "R${String.format("%.2f", budgetAmount)}"
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_primary, null))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        labelRow.addView(budgetText)

        row.addView(labelRow)

        if (type == "max") {
            val percentage = (actualSpent / budgetAmount * 100).coerceIn(0.0, 100.0)

            val progressBar = ProgressBar(this).apply {
                this.progress = percentage.toInt()
                max = 100
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    8
                ).apply {
                    topMargin = 4
                }
                progressTintList = android.content.res.ColorStateList.valueOf(
                    when {
                        percentage >= 100 -> resources.getColor(R.color.expense_red, null)
                        percentage >= 80 -> resources.getColor(R.color.yellow_accent, null)
                        else -> resources.getColor(R.color.income_green, null)
                    }
                )
            }
            row.addView(progressBar)

            val statusText = TextView(this).apply {
                text = when {
                    actualSpent >= budgetAmount -> "⚠️ Exceeded budget by R${String.format("%.2f", actualSpent - budgetAmount)}"
                    percentage >= 80 -> "⚠️ ${String.format("%.0f", percentage)}% of budget used"
                    else -> "${String.format("%.0f", percentage)}% of budget used"
                }
                textSize = 11f
                setPadding(0, 4, 0, 0)
                setTextColor(
                    when {
                        actualSpent >= budgetAmount -> resources.getColor(R.color.expense_red, null)
                        percentage >= 80 -> resources.getColor(R.color.yellow_accent, null)
                        else -> resources.getColor(R.color.text_secondary, null)
                    }
                )
            }
            row.addView(statusText)
        } else if (type == "min") {
            val statusText = TextView(this).apply {
                text = if (actualSpent < budgetAmount) {
                    "⚠️ Below minimum budget by R${String.format("%.2f", budgetAmount - actualSpent)}"
                } else {
                    "✓ Meeting minimum budget requirement"
                }
                textSize = 11f
                setPadding(0, 4, 0, 0)
                setTextColor(
                    if (actualSpent < budgetAmount) {
                        resources.getColor(R.color.yellow_accent, null)
                    } else {
                        resources.getColor(R.color.income_green, null)
                    }
                )
            }
            row.addView(statusText)
        }

        return row
    }

    private fun setupSettingsButton() {
        findViewById<ImageButton>(R.id.menuButton)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.navHome)?.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navHistory)?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navReports)?.setOnClickListener { }
        findViewById<ImageButton>(R.id.navProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}
