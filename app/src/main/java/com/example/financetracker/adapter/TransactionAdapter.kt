package com.example.financetracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.model.ExpenseCategory
import com.example.financetracker.model.IncomeCategory
import com.example.financetracker.model.Transaction
import com.example.financetracker.util.PrefsUtil
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val context: Context,
    private var transactions: List<Transaction>,
    private val listener: OnTransactionClickListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val prefsUtil = PrefsUtil(context)
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance(prefsUtil.getCurrency())
    }

    interface OnTransactionClickListener {
        fun onTransactionClick(transaction: Transaction)  // For edit
        fun onTransactionDelete(transaction: Transaction) // For delete
    }

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTransactionTitle: TextView = itemView.findViewById(R.id.tv_transaction_title)
        private val tvTransactionCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        private val tvTransactionDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
        private val tvTransactionAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        private val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: Button = itemView.findViewById(R.id.btn_delete)

        fun bind(transaction: Transaction) {
            tvTransactionTitle.text = transaction.title
            tvTransactionCategory.text = transaction.category
            tvTransactionDate.text = dateFormat.format(Date(transaction.date))

            val formattedAmount = if (transaction.isExpense) {
                "- ${currencyFormat.format(transaction.amount)}"
            } else {
                "+ ${currencyFormat.format(transaction.amount)}"
            }
            tvTransactionAmount.text = formattedAmount

            val textColor = if (transaction.isExpense) {
                ContextCompat.getColor(context, R.color.category_bills)
            } else {
                ContextCompat.getColor(context, R.color.category_salary)
            }
            tvTransactionAmount.setTextColor(textColor)

            val categoryColorResId = if (transaction.isExpense) {
                when (transaction.category) {
                    ExpenseCategory.TRAVEL.displayName -> R.color.category_travel
                    ExpenseCategory.BILLS.displayName -> R.color.category_bills
                    ExpenseCategory.FOOD.displayName -> R.color.category_food
                    ExpenseCategory.ENTERTAINMENT.displayName -> R.color.category_entertainment
                    else -> R.color.category_other
                }
            } else {
                when (transaction.category) {
                    IncomeCategory.SALARY.displayName -> R.color.category_salary
                    IncomeCategory.BONUS.displayName -> R.color.category_bonus
                    IncomeCategory.GIFT.displayName -> R.color.category_gift
                    else -> R.color.category_other
                }
            }
            tvTransactionCategory.setBackgroundColor(ContextCompat.getColor(context, categoryColorResId))

            btnEdit.setOnClickListener {
                listener.onTransactionClick(transaction)
            }

            btnDelete.setOnClickListener {
                listener.onTransactionDelete(transaction)
            }
        }
    }
}
