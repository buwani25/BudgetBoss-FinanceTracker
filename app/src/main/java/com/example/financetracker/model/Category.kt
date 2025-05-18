package com.example.financetracker.model

import com.example.financetracker.R

enum class ExpenseCategory(val displayName: String, val colorResId: Int) {
    TRAVEL("Travel", R.color.category_travel),
    BILLS("Bills", R.color.category_bills),
    FOOD("Food", R.color.category_food),
    ENTERTAINMENT("Entertainment", R.color.category_entertainment),
    OTHER("Other", R.color.category_other)
}

enum class IncomeCategory(val displayName: String, val colorResId: Int) {
    SALARY("Salary", R.color.category_salary),
    BONUS("Bonus", R.color.category_bonus),
    GIFT("Gift", R.color.category_gift),
    OTHER("Other", R.color.category_other)
}
