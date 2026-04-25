package com.example.budgie_budgetapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgie_budgetapp.adapter.CategoryAdapter
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.data.entity.Category
import com.example.budgie_budgetapp.utils.KEY_USER_ID
import com.example.budgie_budgetapp.utils.PREFS_NAME
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesActivity : AppCompatActivity() {

    private lateinit var adapter: CategoryAdapter
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_USER_ID, 0)

        val db = AppDatabase.getDatabase(this)

        adapter = CategoryAdapter(
            onEdit = { category -> showCategoryDialog(category) },
            onDelete = { category -> confirmDelete(category) }
        )

        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter
        rvCategories.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        db.categoryDao().getCategoriesForUser(userId).observe(this) { categories ->
            adapter.submitList(categories)
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            showCategoryDialog(null)
        }
    }

    private fun showCategoryDialog(existing: Category?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle)

        if (existing != null) {
            tvTitle.text = "Edit Category"
            etName.setText(existing.name)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(if (existing == null) "Add" else "Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val db = AppDatabase.getDatabase(this)
                lifecycleScope.launch {
                    if (existing == null) {
                        val category = Category(name = name, userId = userId)
                        withContext(Dispatchers.IO) { db.categoryDao().insert(category) }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CategoriesActivity, "Category added", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val updated = existing.copy(name = name)
                        withContext(Dispatchers.IO) { db.categoryDao().update(updated) }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CategoriesActivity, "Category updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete \"${category.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val db = AppDatabase.getDatabase(this)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { db.categoryDao().delete(category) }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategoriesActivity, "Category deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
