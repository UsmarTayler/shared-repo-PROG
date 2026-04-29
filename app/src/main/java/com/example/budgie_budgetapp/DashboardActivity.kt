package com.example.budgie_budgetapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.data.entity.Transaction
import com.example.budgie_budgetapp.utils.DateRangeHelper
import com.example.budgie_budgetapp.utils.KEY_USER_ID
import com.example.budgie_budgetapp.utils.KEY_USERNAME
import com.example.budgie_budgetapp.utils.PREFS_NAME
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private val TAG = "DashboardActivity"
    private lateinit var recentTransactionsContainer: LinearLayout
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private var userId = 0
    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        Log.d(TAG, "DashboardActivity created")

        initializeViews()
        loadUserGreeting()
        loadCategoriesFromDatabase()
        loadFinancialSummary()
        loadRecentTransactions()
        setupCategoriesButton()
        setupNavigation()
        setupSettingsButton()
        setupAddButton()
        setupRefreshButton()
    }

    private fun initializeViews() {
        recentTransactionsContainer = findViewById(R.id.recentTransactionsContainer)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
    }

    private fun loadUserGreeting() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, null)
        userId = prefs.getInt(KEY_USER_ID, -1)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvInitials = findViewById<TextView>(R.id.tvUserInitials)

        if (username != null && userId != -1) {
            val initials = username.take(2).uppercase()
            tvInitials.text = initials
            tvGreeting.text = "Hello, $username!"
            Log.d(TAG, "Loaded greeting for user: $username (id=$userId)")
        } else {
            tvInitials.text = "G"
            tvGreeting.text = "Browsing as Guest"
            userId = 0
            Log.d(TAG, "No logged-in user, showing guest greeting")
        }
    }

    private fun loadFinancialSummary() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@DashboardActivity)

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfMonth = calendar.time
                val endOfMonth = Date()

                val totalIncome = withContext(Dispatchers.IO) {
                    db.transactionDao().getTotalByType(userId, "income", startOfMonth, endOfMonth) ?: 0.0
                }

                val totalExpenses = withContext(Dispatchers.IO) {
                    db.transactionDao().getTotalByType(userId, "expense", startOfMonth, endOfMonth) ?: 0.0
                }

                val balance = totalIncome - totalExpenses

                withContext(Dispatchers.Main) {
                    tvTotalBalance.text = "R${String.format("%.2f", balance)}"
                    tvTotalIncome.text = "R${String.format("%.2f", totalIncome)}"
                    tvTotalExpenses.text = "R${String.format("%.2f", totalExpenses)}"

                    if (balance >= 0) {
                        tvTotalBalance.setTextColor(resources.getColor(R.color.income_green, null))
                    } else {
                        tvTotalBalance.setTextColor(resources.getColor(R.color.expense_red, null))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading financial summary: ${e.message}")
            }
        }
    }

    private fun loadRecentTransactions() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@DashboardActivity)

                val recentTransactions = withContext(Dispatchers.IO) {
                    db.transactionDao().getRecentTransactions(userId, 5)
                }

                withContext(Dispatchers.Main) {
                    displayRecentTransactions(recentTransactions)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent transactions: ${e.message}")
            }
        }
    }

    private fun displayRecentTransactions(transactions: List<Transaction>) {
        recentTransactionsContainer.removeAllViews()

        if (transactions.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No transactions yet.\nTap the + button to add one!"
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_secondary, null))
                gravity = Gravity.CENTER
                setPadding(16, 32, 16, 32)
            }
            recentTransactionsContainer.addView(emptyText)
            return
        }

        transactions.forEach { transaction ->
            val transactionView = createTransactionRow(transaction)
            recentTransactionsContainer.addView(transactionView)
            recentTransactionsContainer.addView(createDivider())
        }

        val viewAllButton = createViewAllButton()
        recentTransactionsContainer.addView(viewAllButton)
    }

    private fun createTransactionRow(transaction: Transaction): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { showTransactionDetails(transaction) }
        }

        val leftLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val categoryText = TextView(this).apply {
            text = transaction.categoryName
            setTextColor(resources.getColor(R.color.text_primary, null))
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        leftLayout.addView(categoryText)

        val descText = TextView(this).apply {
            text = if (transaction.description.length > 25)
                transaction.description.take(25) + "..."
            else
                transaction.description
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 12f
        }
        leftLayout.addView(descText)

        layout.addView(leftLayout)

        val rightLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }

        val amountText = TextView(this).apply {
            val prefix = if (transaction.type == "expense") "-R" else "+R"
            text = "$prefix${String.format("%.2f", transaction.amount)}"
            setTextColor(resources.getColor(
                if (transaction.type == "expense") R.color.expense_red else R.color.income_green,
                null
            ))
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        rightLayout.addView(amountText)

        val dateText = TextView(this).apply {
            text = dateFormat.format(transaction.date)
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 11f
        }
        rightLayout.addView(dateText)

        layout.addView(rightLayout)

        return layout
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(resources.getColor(R.color.light_gray, null))
        }
    }

    private fun createViewAllButton(): TextView {
        return TextView(this).apply {
            text = "View All Transactions ›"
            textSize = 14f
            setTextColor(resources.getColor(R.color.yellow_accent, null))
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            setOnClickListener {
                startActivity(Intent(this@DashboardActivity, HistoryActivity::class.java))
            }
        }
    }

    private fun showTransactionDetails(transaction: Transaction) {
        val dateFormatFull = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val details = buildString {
            append("Category: ${transaction.categoryName}\n")
            append("Amount: ${if (transaction.type == "expense") "-" else "+"}R${String.format("%.2f", transaction.amount)}\n")
            append("Date: ${dateFormatFull.format(transaction.date)}\n")
            append("Description: ${transaction.description}\n")
            if (transaction.startTime != null) append("Start: ${transaction.startTime}\n")
            if (transaction.endTime != null) append("End: ${transaction.endTime}\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Transaction Details")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun loadCategoriesFromDatabase() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getInt(KEY_USER_ID, 0)
        val db = AppDatabase.getDatabase(this)
        val container = findViewById<LinearLayout>(R.id.categoriesContainer)

        db.categoryDao().getCategoriesForUser(userId).observe(this) { categories ->
            container.removeAllViews()

            categories.forEach { category ->
                val chip = TextView(this).apply {
                    text = category.name
                    textSize = 13f
                    setTextColor(resources.getColor(R.color.black, null))
                    setBackgroundResource(R.drawable.chip_selected)
                    gravity = Gravity.CENTER
                    val pad = resources.getDimensionPixelSize(R.dimen.chip_padding)
                    setPadding(pad * 2, pad, pad * 2, pad)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.marginEnd = resources.getDimensionPixelSize(R.dimen.chip_margin)
                    layoutParams = params
                }
                container.addView(chip)
            }
        }
    }

    private fun setupCategoriesButton() {
        findViewById<TextView>(R.id.btnManageCategories).setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
    }

    private fun setupSettingsButton() {
        findViewById<ImageButton>(R.id.menuButton)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupAddButton() {
        findViewById<FloatingActionButton>(R.id.addExpenseButton)?.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
        }
    }

    private fun setupRefreshButton() {
        val refreshButton = findViewById<ImageButton>(R.id.refreshButton)
        refreshButton?.setOnClickListener {
            Log.d(TAG, "Refresh button clicked")
            refreshAllData()
        }
    }

    private fun refreshAllData() {
        // Show feedback
        Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show()

        // Animate the refresh button
        val refreshButton = findViewById<ImageButton>(R.id.refreshButton)
        refreshButton?.animate()?.rotationBy(360f)?.setDuration(500)?.start()

        // Reload all data
        loadUserGreeting()
        loadCategoriesFromDatabase()
        loadFinancialSummary()
        loadRecentTransactions()
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.navHome)?.setOnClickListener {
            Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.navHistory)?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<ImageButton>(R.id.navReports)?.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.navProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
