package com.example.budgie_budgetapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.data.entity.User
import com.example.budgie_budgetapp.utils.hashPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            registerUser(username, email, password)
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val existingEmail = withContext(Dispatchers.IO) { db.userDao().getUserByEmail(email) }
            if (existingEmail != null) {
                Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val existingUsername = withContext(Dispatchers.IO) { db.userDao().getUserByUsername(username) }
            if (existingUsername != null) {
                Toast.makeText(this@RegisterActivity, "Username already taken", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val user = User(username = username, email = email, passwordHash = hashPassword(password))
            withContext(Dispatchers.IO) { db.userDao().insert(user) }
            Toast.makeText(this@RegisterActivity, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }
}
