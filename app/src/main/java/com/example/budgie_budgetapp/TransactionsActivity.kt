package com.example.budgie_budgetapp

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgie_budgetapp.data.AppDatabase
import com.example.budgie_budgetapp.data.entity.Category
import com.example.budgie_budgetapp.data.entity.Transaction
import com.example.budgie_budgetapp.utils.KEY_USER_ID
import com.example.budgie_budgetapp.utils.PREFS_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TransactionsActivity : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var dateText: TextView
    private lateinit var startTimeText: TextView
    private lateinit var endTimeText: TextView
    private lateinit var descriptionInput: EditText
    private lateinit var photoButton: Button
    private lateinit var photoPreview: ImageView
    private lateinit var selectedPhotoUri: Uri

    private var transactionType = "expense" // "expense" or "income"
    private var selectedDate: Date = Date()
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var categories: MutableList<String> = mutableListOf()
    private var categoryIds: MutableList<Int> = mutableListOf()
    private var userId: Int = 0
    private var photoPath: String? = null

    // Permission launchers
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openCamera() else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { savePhotoToInternalStorage(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedPhotoUri?.let { savePhotoToInternalStorage(it) }
        } else {
            Toast.makeText(this, "Photo capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_USER_ID, 0)

        initializeViews()
        loadCategories()
        setupTypeTabs()
        setupDatePicker()
        setupTimePickers()
        setupPhotoButton()
        setupSaveButton()
        setupBackButton()
    }

    private fun initializeViews() {
        amountInput = findViewById(R.id.amountInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        dateText = findViewById(R.id.dateText)
        startTimeText = findViewById(R.id.startTimeText)
        endTimeText = findViewById(R.id.endTimeText)
        descriptionInput = findViewById(R.id.noteInput)
        photoButton = findViewById(R.id.photoButton)
        photoPreview = findViewById(R.id.photoPreview)
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@TransactionsActivity)
            val categoryList = withContext(Dispatchers.IO) {
                db.categoryDao().getCategoriesSync(userId)
            }
            categories = categoryList.map { it.name }.toMutableList()
            categoryIds = categoryList.map { it.id }.toMutableList()

            // Add a special "➕ Add New Category" option at the end
            categories.add("➕ Add New Category")
            categoryIds.add(-1) // Use -1 as sentinel value for new category

            val adapter = object : ArrayAdapter<String>(
                this@TransactionsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            ) {
                override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                    val view = super.getDropDownView(position, convertView, parent)
                    if (position == categories.size - 1) {
                        // Style the "Add New Category" option differently
                        (view as TextView).setTextColor(resources.getColor(R.color.yellow_accent, null))
                        view.setTypeface(view.typeface, android.graphics.Typeface.BOLD)
                    }
                    return view
                }

                override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                    val view = super.getView(position, convertView, parent)
                    if (position == categories.size - 1) {
                        (view as TextView).setTextColor(resources.getColor(R.color.yellow_accent, null))
                    }
                    return view
                }
            }

            categorySpinner.adapter = adapter

            // Handle spinner item selection
            categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                    if (position == categories.size - 1) {
                        // User selected "Add New Category"
                        showAddCategoryDialog()
                        // Reset spinner to previous selection
                        categorySpinner.setSelection(0)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        tvTitle.text = "Add New Category"

        AlertDialog.Builder(this)
            .setTitle("Create Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = etName.text.toString().trim()
                if (categoryName.isEmpty()) {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(this@TransactionsActivity)

                    // Check if category already exists
                    val existingCategories = withContext(Dispatchers.IO) {
                        db.categoryDao().getCategoriesSync(userId)
                    }

                    if (existingCategories.any { it.name.equals(categoryName, ignoreCase = true) }) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TransactionsActivity, "Category already exists", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    // Create new category
                    val newCategory = Category(name = categoryName, userId = userId)
                    val newId = withContext(Dispatchers.IO) {
                        db.categoryDao().insert(newCategory)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TransactionsActivity, "Category '$categoryName' added", Toast.LENGTH_SHORT).show()
                        // Reload categories
                        loadCategories()
                        // Auto-select the new category
                        val newPosition = categories.indexOfFirst { it == categoryName }
                        if (newPosition != -1) {
                            categorySpinner.setSelection(newPosition)
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupTypeTabs() {
        val expenseTab = findViewById<TextView>(R.id.expenseTab)
        val incomeTab = findViewById<TextView>(R.id.incomeTab)

        expenseTab.setOnClickListener {
            transactionType = "expense"
            expenseTab.setBackgroundResource(R.drawable.tab_selected)
            expenseTab.setTextColor(resources.getColor(android.R.color.black, null))
            incomeTab.setBackgroundResource(R.drawable.tab_unselected)
            incomeTab.setTextColor(resources.getColor(R.color.text_primary, null))
        }

        incomeTab.setOnClickListener {
            transactionType = "income"
            incomeTab.setBackgroundResource(R.drawable.tab_selected)
            incomeTab.setTextColor(resources.getColor(android.R.color.black, null))
            expenseTab.setBackgroundResource(R.drawable.tab_unselected)
            expenseTab.setTextColor(resources.getColor(R.color.text_primary, null))
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        dateText.text = dateFormat.format(selectedDate)
        dateText.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    dateText.text = dateFormat.format(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePickers() {
        startTimeText.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedStartTime = String.format("%02d:%02d", hourOfDay, minute)
                    startTimeText.text = "Start: ${selectedStartTime}"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        endTimeText.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedEndTime = String.format("%02d:%02d", hourOfDay, minute)
                    endTimeText.text = "End: ${selectedEndTime}"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupPhotoButton() {
        photoButton.setOnClickListener {
            showPhotoOptionsDialog()
        }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> {
                        photoPath = null
                        photoPreview.setImageURI(null)
                        photoPreview.visibility = ImageView.GONE
                        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Budgie")
            }
        }
        selectedPhotoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return
        cameraLauncher.launch(selectedPhotoUri)
    }

    private fun savePhotoToInternalStorage(uri: Uri) {
        try {
            val fileName = "expense_photo_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            photoPath = file.absolutePath
            photoPreview.setImageURI(uri)
            photoPreview.visibility = ImageView.VISIBLE
            Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSaveButton() {
        val saveButton = findViewById<ImageButton>(R.id.saveButton)
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val amount = amountInput.text.toString().trim()
        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return false
        }
        if (amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return false
        }
        if (categorySpinner.selectedItemPosition < 0 || categorySpinner.selectedItemPosition >= categories.size - 1) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
            return false
        }
        if (descriptionInput.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveTransaction() {
        val amount = amountInput.text.toString().trim().toDouble()
        val selectedPosition = categorySpinner.selectedItemPosition
        val categoryName = categories[selectedPosition]
        val categoryId = categoryIds[selectedPosition]
        val description = descriptionInput.text.toString().trim()

        val transaction = Transaction(
            amount = amount,
            categoryId = categoryId,
            categoryName = categoryName,
            type = transactionType,
            description = description,
            date = selectedDate,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            photoPath = photoPath,
            userId = userId
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@TransactionsActivity)
            withContext(Dispatchers.IO) {
                db.transactionDao().insert(transaction)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@TransactionsActivity, "Transaction saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}
