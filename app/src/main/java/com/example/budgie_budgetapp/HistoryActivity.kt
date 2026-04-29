package com.example.budgie_budgetapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
import com.example.budgie_budgetapp.utils.PREFS_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var transactionContainer: LinearLayout
    private lateinit var dayChip: TextView
    private lateinit var weekChip: TextView
    private lateinit var monthChip: TextView
    private lateinit var tvDateRange: TextView
    private lateinit var etSearch: EditText

    private var currentFilter = "Day"
    private var currentSearchQuery = ""
    private var allTransactions: List<Transaction> = emptyList()
    private var userId = 0
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_USER_ID, 0)

        initializeViews()
        setupFilters()
        setupSearch()
        setupNavigation()
        setupSettingsButton()
        loadTransactions()
    }

    private fun initializeViews() {
        transactionContainer = findViewById(R.id.transactionContainer)
        dayChip = findViewById(R.id.dayChip)
        weekChip = findViewById(R.id.weekChip)
        monthChip = findViewById(R.id.monthChip)
        tvDateRange = findViewById(R.id.tvDateRange)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                applySearchFilter()
            }
        })
    }

    private fun applySearchFilter() {
        val filtered = if (currentSearchQuery.isEmpty()) {
            allTransactions
        } else {
            val query = currentSearchQuery.lowercase()
            allTransactions.filter { t ->
                t.categoryName.lowercase().contains(query) ||
                t.description.lowercase().contains(query) ||
                String.format("%.2f", t.amount).contains(query)
            }
        }
        displayTransactions(filtered)
    }

    private fun setupFilters() {
        dayChip.setOnClickListener {
            currentFilter = "Day"
            updateChipSelection()
            loadTransactions()
        }
        weekChip.setOnClickListener {
            currentFilter = "Week"
            updateChipSelection()
            loadTransactions()
        }
        monthChip.setOnClickListener {
            currentFilter = "Month"
            updateChipSelection()
            loadTransactions()
        }
    }

    private fun updateChipSelection() {
        val chips = listOf(dayChip, weekChip, monthChip)
        chips.forEach { chip ->
            if (chip.text == currentFilter) {
                chip.setBackgroundResource(R.drawable.chip_selected)
                chip.setTextColor(resources.getColor(R.color.black, null))
            } else {
                chip.setBackgroundResource(R.drawable.chip_unselected)
                chip.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
    }

    private fun loadTransactions() {
        val (startDate, endDate) = DateRangeHelper.getDateRange(currentFilter)
        tvDateRange.text = DateRangeHelper.formatDateRange(startDate, endDate)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@HistoryActivity)
            allTransactions = withContext(Dispatchers.IO) {
                db.transactionDao().getTransactionsBetweenDates(userId, startDate, endDate)
            }
            applySearchFilter()
        }
    }

    private fun displayTransactions(transactions: List<Transaction>) {
        transactionContainer.removeAllViews()

        if (transactions.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No transactions for this period"
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 14f
                setPadding(16, 32, 16, 32)
                gravity = android.view.Gravity.CENTER
            }
            transactionContainer.addView(emptyText)
            return
        }

        // Group transactions by date
        val grouped = transactions.groupBy {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date)
        }

        for ((dateKey, dayTransactions) in grouped) {
            // Add date header
            val dateHeader = TextView(this).apply {
                text = formatDateHeader(dayTransactions.first().date)
                setTextColor(resources.getColor(R.color.yellow_accent, null))
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 24, 0, 12)
            }
            transactionContainer.addView(dateHeader)

            // Add transactions for this date
            dayTransactions.forEach { transaction ->
                val transactionView = createTransactionView(transaction)
                transactionContainer.addView(transactionView)
                transactionContainer.addView(createDivider())
            }
        }
    }

    private fun formatDateHeader(date: Date): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val transactionCal = Calendar.getInstance().apply { time = date }

        return when {
            transactionCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    transactionCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            transactionCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    transactionCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
            else -> dateFormat.format(date)
        }
    }

    private fun createTransactionView(transaction: Transaction): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Left side - name and time (made clickable to show details)
        val leftLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { showTransactionDetail(transaction) }
        }

        val nameText = TextView(this).apply {
            text = transaction.categoryName
            setTextColor(resources.getColor(R.color.text_primary, null))
            textSize = 16f
        }
        leftLayout.addView(nameText)

        val descText = TextView(this).apply {
            text = transaction.description.take(30)
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 11f
        }
        leftLayout.addView(descText)

        layout.addView(leftLayout)

        // Right side - amount
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
        layout.addView(amountText)

        // Info Icon Button
        val infoButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_info_details)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                showTransactionDetail(transaction)
            }
        }
        layout.addView(infoButton)

        // Delete Icon Button
        val deleteButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setColorFilter(resources.getColor(R.color.expense_red, null))
            setOnClickListener {
                confirmDeleteTransaction(transaction)
            }
        }
        layout.addView(deleteButton)

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

    private fun showTransactionDetail(transaction: Transaction) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(transaction.categoryName)

        val info = buildString {
            append("Amount: ${if (transaction.type == "expense") "-" else "+"}R${String.format("%.2f", transaction.amount)}\n")
            append("Date: ${dateFormat.format(transaction.date)}\n")
            append("Description: ${transaction.description}\n")
            if (transaction.startTime != null) append("Start: ${transaction.startTime}\n")
            if (transaction.endTime != null) append("End: ${transaction.endTime}\n")
        }

        builder.setMessage(info)

        if (transaction.photoPath != null) {
            builder.setPositiveButton("View Photo") { _, _ ->
                showPhotoDialog(transaction.photoPath)
            }
        }

        builder.setNegativeButton("Close", null)
        builder.show()
    }

    private fun confirmDeleteTransaction(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Delete ${transaction.categoryName} transaction for R${String.format("%.2f", transaction.amount)}?\nThis cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@HistoryActivity)

                // Delete the photo file if it exists
                if (transaction.photoPath != null) {
                    val photoFile = File(transaction.photoPath)
                    if (photoFile.exists()) {
                        photoFile.delete()
                    }
                }

                // Delete transaction from database
                withContext(Dispatchers.IO) {
                    db.transactionDao().delete(transaction)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistoryActivity, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    loadTransactions() // Refresh the list
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistoryActivity, "Error deleting transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPhotoDialog(photoPath: String?) {
        if (photoPath == null) {
            Toast.makeText(this, "No photo available", Toast.LENGTH_SHORT).show()
            return
        }

        val photoFile = File(photoPath)
        if (!photoFile.exists()) {
            Toast.makeText(this, "Photo file not found", Toast.LENGTH_SHORT).show()
            return
        }

        val imageView = android.widget.ImageView(this).apply {
            setImageURI(Uri.fromFile(photoFile))
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Transaction Photo")
            .setView(imageView)
            .setPositiveButton("Close", null)
            .show()
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
        findViewById<ImageButton>(R.id.navHistory)?.setOnClickListener { }
        findViewById<ImageButton>(R.id.navReports)?.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}
