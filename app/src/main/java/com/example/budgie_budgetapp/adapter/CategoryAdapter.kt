package com.example.budgie_budgetapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgie_budgetapp.R
import com.example.budgie_budgetapp.data.entity.Category

class CategoryAdapter(
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var categories: List<Category> = emptyList()

    fun submitList(list: List<Category>) {
        categories = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvDefault: TextView = view.findViewById(R.id.tvDefaultBadge)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditCategory)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvName.text = category.name

        val isDefault = category.userId == 0
        holder.tvDefault.visibility = if (isDefault) View.VISIBLE else View.GONE
        holder.btnEdit.isEnabled = !isDefault
        holder.btnEdit.alpha = if (isDefault) 0.3f else 1.0f
        holder.btnDelete.isEnabled = !isDefault
        holder.btnDelete.alpha = if (isDefault) 0.3f else 1.0f

        holder.btnEdit.setOnClickListener { if (!isDefault) onEdit(category) }
        holder.btnDelete.setOnClickListener { if (!isDefault) onDelete(category) }
    }

    override fun getItemCount() = categories.size
}
