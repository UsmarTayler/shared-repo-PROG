package com.example.budgie_budgetapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ReportsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        setupNavigation()
        setupSettingsButton()
    }

    private fun setupSettingsButton() {
        val settingsButton = findViewById<ImageButton>(R.id.menuButton)

        settingsButton?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupNavigation() {
        val home = findViewById<ImageButton>(R.id.navHome)
        val history = findViewById<ImageButton>(R.id.navHistory)
        val reports = findViewById<ImageButton>(R.id.navReports)
        val profile = findViewById<ImageButton>(R.id.navProfile)

        home?.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        history?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }

        reports?.setOnClickListener {
            // Already on Reports
        }

        profile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}