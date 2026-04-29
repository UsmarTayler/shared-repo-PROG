package com.example.budgie_budgetapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgie_budgetapp.R
import com.example.budgie_budgetapp.data.entity.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = transactions[position]
        holder.tvCategory.text = tx.categoryName
        holder.tvDescription.text = if (tx.description.isBlank()) tx.categoryName else tx.description
        holder.tvDate.text = dateFormat.format(tx.date)

        val ctx = holder.itemView.context
        if (tx.type == "income") {
            holder.tvAmount.text = "+R%.2f".format(tx.amount)
            holder.tvAmount.setTextColor(ctx.getColor(R.color.income_green))
        } else {
            holder.tvAmount.text = "-R%.2f".format(tx.amount)
            holder.tvAmount.setTextColor(ctx.getColor(R.color.expense_red))
        }
    }

    override fun getItemCount() = transactions.size

    fun updateData(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}
