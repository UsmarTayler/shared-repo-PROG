package com.example.budgie_budgetapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.utils.KEY_EMAIL
import com.example.budgie_budgetapp.utils.KEY_USER_ID
import com.example.budgie_budgetapp.utils.KEY_USERNAME
import com.example.budgie_budgetapp.utils.PREFS_NAME
import com.example.budgie_budgetapp.utils.hashPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Skip login if already logged in
        if (prefs.getInt(KEY_USER_ID, -1) != -1) {
            goToDashboard()
            return
        }

        val etUsernameOrEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGuest = findViewById<Button>(R.id.btnGuest)

        btnLogin.setOnClickListener {
            val usernameOrEmail = etUsernameOrEmail.text.toString().trim()
            val password = etPassword.text.toString()
            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(usernameOrEmail, password)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnGuest.setOnClickListener {
            goToDashboard()
        }
    }

    private fun loginUser(usernameOrEmail: String, password: String) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            // Try username first, then fall back to email lookup
            val user = withContext(Dispatchers.IO) {
                db.userDao().getUserByUsername(usernameOrEmail)
                    ?: db.userDao().getUserByEmail(usernameOrEmail)
            }
            if (user != null && user.passwordHash == hashPassword(password)) {
                prefs.edit()
                    .putInt(KEY_USER_ID, user.id)
                    .putString(KEY_USERNAME, user.username)
                    .putString(KEY_EMAIL, user.email)
                    .apply()
                goToDashboard()
            } else {
                Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
