package com.example.budgie_budgetapp

import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import android.widget.ImageButton // Add this import

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Back Arrow Button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close this activity and go back
        }

        // Currency Spinner
        val currencySpinner = findViewById<Spinner>(R.id.currencySpinner)
        val currencies = arrayOf("ZAR", "USD", "EUR", "GBP")
        currencySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)

        // Country Spinner
        val countrySpinner = findViewById<Spinner>(R.id.countrySpinner)
        val countries = arrayOf("South Africa", "United States", "United Kingdom", "Germany")
        countrySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)

        // Language Spinner
        val languageSpinner = findViewById<Spinner>(R.id.languageSpinner)
        val languages = arrayOf("English", "Afrikaans", "French", "German")
        languageSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)

        // Light Mode Toggle
        val lightModeSwitch = findViewById<Switch>(R.id.lightModeSwitch)
        lightModeSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                Toast.makeText(this, "Light mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Light mode disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}