package com.example.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.financetracker.R
import com.example.financetracker.util.PrefsUtil
import androidx.core.content.ContextCompat


class DashboardFragment : Fragment() {

    private lateinit var tvBudgetAmount: TextView
    private lateinit var tvBudgetPercentage: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var categoryExpenseLayout: LinearLayout
    private lateinit var tvWarningMessage: TextView  // TextView to show warning message

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvBudgetAmount = view.findViewById(R.id.tv_dashboard_budget_amount)
        tvBudgetPercentage = view.findViewById(R.id.tv_budget_percentage)
        progressBudget = view.findViewById(R.id.progress_budget)
        tvTotalIncome = view.findViewById(R.id.tv_total_income)
        tvTotalExpense = view.findViewById(R.id.tv_total_expense)
        categoryExpenseLayout = view.findViewById(R.id.category_expenses_layout)
        tvWarningMessage = view.findViewById(R.id.tv_warning_message)  // Initialize the warning message TextView

        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    fun updateDashboard() {
        val prefsUtil = PrefsUtil(requireContext())
        val selectedCurrency = prefsUtil.getCurrency()

        // Fetch the data
        val budget = prefsUtil.getBudget()
        tvBudgetAmount.text = "$selectedCurrency ${budget.toInt()}"

        val totalIncome = prefsUtil.getTotalIncome()  // Dynamically fetch real data
        val totalExpense = prefsUtil.getTotalExpense()  // Dynamically fetch real data

        tvTotalIncome.text = "$selectedCurrency ${totalIncome.toInt()}"
        tvTotalExpense.text = "$selectedCurrency ${totalExpense.toInt()}"

        val usedPercentage = if (budget > 0) (totalExpense / budget * 100).toInt() else 0
        tvBudgetPercentage.text = "$usedPercentage% used"
        progressBudget.progress = usedPercentage

        // Show warning if the budget is nearing or exceeded
        if (usedPercentage >= 90) {
            tvWarningMessage.visibility = View.VISIBLE
            tvWarningMessage.text = getString(R.string.warning_nearing_budget_limit)
            tvWarningMessage.setTextColor(resources.getColor(R.color.warning_color))
        } else {
            tvWarningMessage.visibility = View.GONE
        }

        // Fetch and display category expenses
        displayCategoryExpenses(prefsUtil)
    }

    private fun displayCategoryExpenses(prefsUtil: PrefsUtil) {
        val categoryExpenses = prefsUtil.getCategoryExpenses()

        // Clear existing views
        categoryExpenseLayout.removeAllViews()

        // Add the category expenses dynamically
        for ((category, expense) in categoryExpenses) {
            val textView = TextView(requireContext())

            // Set the text for each category and expense
            textView.text = "$category: $expense"

            // Set color based on the category or expense (you can adjust the logic here)
            val textColor = when (category) {
                "Travel" -> ContextCompat.getColor(requireContext(), R.color.category_travel)
                "Bills" -> ContextCompat.getColor(requireContext(), R.color.category_bills)
                "Food" -> ContextCompat.getColor(requireContext(), R.color.category_food)
                "Entertainment" -> ContextCompat.getColor(requireContext(), R.color.category_entertainment)
                else -> ContextCompat.getColor(requireContext(), R.color.default_category_color) // Default color
            }

            // Set the text color dynamically
            textView.setTextColor(textColor)

            // Optionally, you can set background color as well
            // textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.some_background_color))

            // Add the TextView to the layout
            categoryExpenseLayout.addView(textView)
        }
    }

}
