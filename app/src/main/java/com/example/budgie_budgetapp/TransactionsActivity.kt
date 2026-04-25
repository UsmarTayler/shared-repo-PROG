package com.example.budgie_budgetapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TransactionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        // Back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close activity and go back
        }

        // Save button
        val saveButton = findViewById<ImageButton>(R.id.saveButton)
        saveButton.setOnClickListener {
            Toast.makeText(this, "Transaction saved (demo)", Toast.LENGTH_SHORT).show()
            finish() // Close activity and go back
        }

        // Expense/Income tabs (simple visual toggle)
        val expenseTab = findViewById<TextView>(R.id.expenseTab)
        val incomeTab = findViewById<TextView>(R.id.incomeTab)

        expenseTab.setOnClickListener {
            expenseTab.setBackgroundResource(R.drawable.tab_selected)
            expenseTab.setTextColor(resources.getColor(android.R.color.black))
            incomeTab.setBackgroundResource(R.drawable.tab_unselected)
            incomeTab.setTextColor(resources.getColor(R.color.text_primary))
        }

        incomeTab.setOnClickListener {
            incomeTab.setBackgroundResource(R.drawable.tab_selected)
            incomeTab.setTextColor(resources.getColor(android.R.color.black))
            expenseTab.setBackgroundResource(R.drawable.tab_unselected)
            expenseTab.setTextColor(resources.getColor(R.color.text_primary))
        }

        // Date picker (placeholder - just shows toast for now)
        val dateText = findViewById<TextView>(R.id.dateText)
        dateText.setOnClickListener {
            Toast.makeText(this, "Date picker would open here", Toast.LENGTH_SHORT).show()
        }
    }
}