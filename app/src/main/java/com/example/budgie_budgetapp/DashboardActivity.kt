package com.example.budgie_budgetapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.utils.KEY_USER_ID
import com.example.budgie_budgetapp.utils.KEY_USERNAME
import com.example.budgie_budgetapp.utils.PREFS_NAME
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Main dashboard screen displayed after login or guest access.
 * Loads categories from Room DB and displays user greeting.
 */
class DashboardActivity : AppCompatActivity() {

    private val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        Log.d(TAG, "DashboardActivity created")

        loadUserGreeting()
        loadCategoriesFromDatabase()
        setupCategoriesButton()
        setupNavigation()
        setupSettingsButton()
        setupAddButton()
    }

    /**
     * Reads the logged-in username from SharedPreferences and updates the greeting TextView.
     * If no user is logged in (guest mode), a generic greeting is shown.
     */
    private fun loadUserGreeting() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, null)
        val userId = prefs.getInt(KEY_USER_ID, -1)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvInitials = findViewById<TextView>(R.id.tvUserInitials)

        if (username != null && userId != -1) {
            // Show the first two letters of the username as initials
            val initials = username.take(2).uppercase()
            tvInitials.text = initials
            tvGreeting.text = "Hello, $username!"
            Log.d(TAG, "Loaded greeting for user: $username (id=$userId)")
        } else {
            tvInitials.text = "G"
            tvGreeting.text = "Browsing as Guest"
            Log.d(TAG, "No logged-in user, showing guest greeting")
        }
    }

    /**
     * Observes the categories LiveData from Room DB for the current user.
     * Categories with userId=0 are global defaults; user-specific ones have the user's id.
     * Dynamically adds category chips to the horizontal scroll container.
     */
    private fun loadCategoriesFromDatabase() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getInt(KEY_USER_ID, 0)
        val db = AppDatabase.getDatabase(this)
        val container = findViewById<LinearLayout>(R.id.categoriesContainer)

        Log.d(TAG, "Loading categories for userId=$userId")

        // Observe LiveData so the chips update automatically on category changes
        db.categoryDao().getCategoriesForUser(userId).observe(this) { categories ->
            container.removeAllViews()
            Log.d(TAG, "Categories loaded: ${categories.size}")

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
        val btnManage = findViewById<TextView>(R.id.btnManageCategories)
        btnManage.setOnClickListener {
            Log.d(TAG, "Navigating to CategoriesActivity")
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
    }

    private fun setupSettingsButton() {
        try {
            val settingsButton = findViewById<ImageButton>(R.id.menuButton)
            settingsButton?.setOnClickListener {
                Log.d(TAG, "Settings button clicked")
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening settings: ${e.message}")
        }
    }

    private fun setupAddButton() {
        try {
            val addExpenseButton = findViewById<FloatingActionButton>(R.id.addExpenseButton)
            addExpenseButton?.setOnClickListener {
                Log.d(TAG, "Add expense button clicked")
                val intent = Intent(this, TransactionsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up add button: ${e.message}")
        }
    }

    private fun setupNavigation() {
        try {
            val home = findViewById<ImageButton>(R.id.navHome)
            val history = findViewById<ImageButton>(R.id.navHistory)
            val reports = findViewById<ImageButton>(R.id.navReports)
            val profile = findViewById<ImageButton>(R.id.navProfile)

            home?.setOnClickListener {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
            }
            history?.setOnClickListener {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            reports?.setOnClickListener {
                startActivity(Intent(this, ReportsActivity::class.java))
            }
            profile?.setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in navigation: ${e.message}")
        }
    }
}
