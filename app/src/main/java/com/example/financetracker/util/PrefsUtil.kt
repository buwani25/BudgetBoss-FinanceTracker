package com.example.financetracker.util

import android.content.Context
import android.content.SharedPreferences
import com.example.financetracker.model.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class PrefsUtil(context: Context) {
    companion object {
        private const val PREFS_NAME = "FinanceTrackerPrefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_BUDGET = "budget"
        private const val KEY_CURRENCY = "currency"
        private const val DEFAULT_BUDGET = 5000.0
        private const val DEFAULT_CURRENCY = "USD"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Budget methods
    fun setBudget(budget: Double) {
        prefs.edit().putFloat(KEY_BUDGET, budget.toFloat()).apply()
    }

    fun getBudget(): Double {
        return prefs.getFloat(KEY_BUDGET, DEFAULT_BUDGET.toFloat()).toDouble()
    }

    // Currency methods
    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    // Get the current total spending (expenses for the current month)
    fun getCurrentSpending(): Double {
        return getTotalExpense()
    }


    // Transaction methods
    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getTransactions().toMutableList()
        val transactionToRemove = transactions.find { it.id == transactionId }
        if (transactionToRemove != null) {
            transactions.remove(transactionToRemove)
            val newBudget = transactions.sumOf { it.amount }
            setBudget(newBudget)
            saveTransactions(transactions)
        }
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val jsonArray = JSONArray()
        for (transaction in transactions) {
            val obj = JSONObject()
            obj.put("id", transaction.id)
            obj.put("title", transaction.title)
            obj.put("amount", transaction.amount)
            obj.put("category", transaction.category)
            obj.put("date", transaction.date)
            obj.put("isExpense", transaction.isExpense)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_TRANSACTIONS, jsonArray.toString()).apply()
    }

    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val jsonArray = JSONArray(json)
        val transactions = mutableListOf<Transaction>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val transaction = Transaction(
                id = obj.getString("id"),
                title = obj.getString("title"),
                amount = obj.getDouble("amount"),
                category = obj.getString("category"),
                date = obj.getLong("date"),
                isExpense = obj.getBoolean("isExpense")
            )
            transactions.add(transaction)
        }
        return transactions
    }

    // Helper methods for analytics
    fun getCurrentMonthTransactions(): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        return getTransactions().filter { transaction ->
            val transactionCalendar = Calendar.getInstance().apply {
                timeInMillis = transaction.date
            }
            transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                    transactionCalendar.get(Calendar.YEAR) == currentYear
        }
    }

    fun getTotalIncome(): Double {
        return getCurrentMonthTransactions()
            .filter { !it.isExpense }
            .sumOf { it.amount }
    }

    fun getTotalExpense(): Double {
        return getCurrentMonthTransactions()
            .filter { it.isExpense }
            .sumOf { it.amount }
    }

    // Fetch category expenses for the current month
    fun getCategoryExpenses(): Map<String, Double> {
        return getCurrentMonthTransactions()
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    fun getBudgetUsagePercentage(): Float {
        val totalExpense = getTotalExpense()
        val budget = getBudget()
        return (totalExpense / budget * 100).toFloat()
    }

    // Export and import data methods (not changed)
    fun exportDataToJson(): String {
        val data = JSONObject()
        data.put("budget", getBudget())
        data.put("currency", getCurrency())

        val transactionsArray = JSONArray()
        for (transaction in getTransactions()) {
            val obj = JSONObject()
            obj.put("id", transaction.id)
            obj.put("title", transaction.title)
            obj.put("amount", transaction.amount)
            obj.put("category", transaction.category)
            obj.put("date", transaction.date)
            obj.put("isExpense", transaction.isExpense)
            transactionsArray.put(obj)
        }
        data.put("transactions", transactionsArray)

        return data.toString()
    }

    fun importDataFromJson(json: String): Boolean {
        return try {
            val data = JSONObject(json)

            // Import budget
            val budget = data.optDouble("budget", DEFAULT_BUDGET)
            setBudget(budget)

            // Import currency
            val currency = data.optString("currency", DEFAULT_CURRENCY)
            setCurrency(currency)

            // Import transactions
            val transactionsArray = data.getJSONArray("transactions")
            val transactions = mutableListOf<Transaction>()
            for (i in 0 until transactionsArray.length()) {
                val obj = transactionsArray.getJSONObject(i)
                val transaction = Transaction(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    amount = obj.getDouble("amount"),
                    category = obj.getString("category"),
                    date = obj.getLong("date"),
                    isExpense = obj.getBoolean("isExpense")
                )
                transactions.add(transaction)
            }
            saveTransactions(transactions)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
