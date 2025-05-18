package com.example.financetracker.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.adapter.TransactionAdapter
import com.example.financetracker.model.ExpenseCategory
import com.example.financetracker.model.IncomeCategory
import com.example.financetracker.model.Transaction
import com.example.financetracker.util.PrefsUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import com.example.financetracker.util.NotificationUtil


class RecordsFragment : Fragment(), TransactionAdapter.OnTransactionClickListener {

    private lateinit var prefsUtil: PrefsUtil
    private lateinit var transactionAdapter: TransactionAdapter
    private val calendar = Calendar.getInstance()
    private var selectedDate = Date()

    private lateinit var recyclerTransactions: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton

    // Dialog Views
    private lateinit var rbIncome: RadioButton
    private lateinit var rbExpense: RadioButton
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvDialogTitle: TextView
    private lateinit var etTransactionTitle: EditText
    private lateinit var etTransactionAmount: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        recyclerTransactions = view.findViewById(R.id.recycler_transactions)
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsUtil = PrefsUtil(requireContext())

        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(requireContext(), mutableListOf(), this)
        recyclerTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
        loadTransactions()
    }

    private fun loadTransactions() {
        val transactions = prefsUtil.getTransactions().sortedByDescending { it.date }
        transactionAdapter.updateTransactions(transactions)
    }

    private fun setupFab() {
        fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog(transaction: Transaction? = null) {
        val dialogBinding = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        val isEdit = transaction != null

        tvDialogTitle = dialogBinding.findViewById(R.id.tv_dialog_title)
        etTransactionTitle = dialogBinding.findViewById(R.id.et_transaction_title)
        etTransactionAmount = dialogBinding.findViewById(R.id.et_transaction_amount)
        rbIncome = dialogBinding.findViewById(R.id.rb_income)
        rbExpense = dialogBinding.findViewById(R.id.rb_expense)
        val btnSelectDate: Button = dialogBinding.findViewById(R.id.btn_select_date)
        spinnerCategory = dialogBinding.findViewById(R.id.spinner_category)
        val btnCancel: Button = dialogBinding.findViewById(R.id.btn_cancel)
        val btnSave: Button = dialogBinding.findViewById(R.id.btn_save)

        tvDialogTitle.text = getString(if (isEdit) R.string.edit_record else R.string.add_record)

        if (isEdit) {
            etTransactionTitle.setText(transaction!!.title)
            etTransactionAmount.setText(transaction.amount.toString())
            rbIncome.isChecked = !transaction.isExpense
            rbExpense.isChecked = transaction.isExpense
            selectedDate = Date(transaction.date)
        }

        updateDateButtonText(btnSelectDate)
        btnSelectDate.setOnClickListener {
            showDatePicker(btnSelectDate)
        }

        setupCategorySpinner(isEdit, transaction)

        dialogBinding.findViewById<RadioGroup>(R.id.rg_transaction_type).setOnCheckedChangeListener { _, _ ->
            setupCategorySpinner(isEdit, transaction)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                saveTransaction(transaction?.id, dialog)
            }
        }

        dialog.show()
    }

    private fun setupCategorySpinner(isEdit: Boolean, transaction: Transaction?) {
        val isExpense = rbExpense.isChecked
        val categories = if (isExpense) {
            ExpenseCategory.values().map { it.displayName }
        } else {
            IncomeCategory.values().map { it.displayName }
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        spinnerCategory.adapter = adapter

        if (isEdit && transaction != null) {
            val position = categories.indexOf(transaction.category)
            if (position != -1) {
                spinnerCategory.setSelection(position)
            }
        }
    }

    private fun showDatePicker(btnSelectDate: Button) {
        calendar.time = selectedDate

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = calendar.time
                updateDateButtonText(btnSelectDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun updateDateButtonText(btnSelectDate: Button) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        btnSelectDate.text = dateFormat.format(selectedDate)
    }
        //validations
        private fun validateInput(): Boolean {
            val title = etTransactionTitle.text.toString().trim()
            val amountStr = etTransactionAmount.text.toString().trim()

            // Title validation: Check if it contains only letters and spaces
            val titleRegex = "^[a-zA-Z ]*$".toRegex()
            if (title.isEmpty()) {
                etTransactionTitle.error = "Title is required"
                return false
            } else if (!title.matches(titleRegex)) {
                etTransactionTitle.error = "Title can only contain letters and spaces"
                return false
            }

            // Amount validation
            if (amountStr.isEmpty()) {
                etTransactionAmount.error = "Amount is required"
                return false
            }

            try {
                val amount = amountStr.toDouble()
                if (amount <= 0) {
                    etTransactionAmount.error = "Amount must be greater than zero"
                    return false
                }
            } catch (e: NumberFormatException) {
                etTransactionAmount.error = "Invalid amount"
                return false
            }

            return true
        }


    private fun saveTransaction(existingId: String?, dialog: AlertDialog) {
        val title = etTransactionTitle.text.toString().trim()
        val amount = etTransactionAmount.text.toString().toDouble()
        val isExpense = rbExpense.isChecked
        val category = spinnerCategory.selectedItem.toString()

        val transaction = Transaction(
            id = existingId ?: UUID.randomUUID().toString(),
            title = title,
            amount = amount,
            category = category,
            isExpense = isExpense,
            date = selectedDate.time
        )

        // Save the transaction
        if (existingId == null) {
            prefsUtil.saveTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction added", Toast.LENGTH_SHORT).show()
        } else {
            prefsUtil.updateTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction updated", Toast.LENGTH_SHORT).show()
        }

        // Load the updated transactions list
        loadTransactions()

        // After saving or updating the transaction, check if the budget is exceeded or approaching
        val currentSpending = prefsUtil.getCurrentSpending()  // Get the current spending from PrefsUtil
        val budget = prefsUtil.getBudget()  // Get the monthly budget from PrefsUtil

        // Show budget warning or exceeded notification
        if (currentSpending > budget) {
            NotificationUtil(requireContext()).showBudgetExceededNotification()
        } else if (currentSpending > (budget * 0.9)) {  // 90% of the budget
            NotificationUtil(requireContext()).showBudgetWarningNotification()
        }

        // Dismiss the dialog
        dialog.dismiss()
    }


    override fun onTransactionClick(transaction: Transaction) {
        // Open the dialog to edit the transaction
        showAddTransactionDialog(transaction)
    }

    override fun onTransactionDelete(transaction: Transaction) {
        // Show AlertDialog to confirm delete
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete \"${transaction.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                prefsUtil.deleteTransaction(transaction.id)
                loadTransactions()
                Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
