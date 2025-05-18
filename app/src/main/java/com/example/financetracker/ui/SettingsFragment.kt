package com.example.financetracker.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Spinner
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.financetracker.R
import com.example.financetracker.adapter.CurrencyAdapter
import com.example.financetracker.util.FileUtil
import com.example.financetracker.util.PrefsUtil
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import com.example.financetracker.MainActivity


class SettingsFragment : Fragment() {

    private lateinit var prefsUtil: PrefsUtil
    private lateinit var fileUtil: FileUtil
    private lateinit var currencyFormat: NumberFormat

    private lateinit var tvCurrentBudget: TextView
    private lateinit var btnChangeBudget: Button
    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnApplyCurrency: Button
    private lateinit var btnExport: Button
    private lateinit var btnImport: Button

    private val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "INR", "LKR")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize views using findViewById
        tvCurrentBudget = view.findViewById(R.id.et_budget_amount)
        btnChangeBudget = view.findViewById(R.id.btn_change_budget)
        spinnerCurrency = view.findViewById(R.id.spinner_currency)
        btnApplyCurrency = view.findViewById(R.id.btn_apply_currency)
        btnExport = view.findViewById(R.id.btn_export)
        btnImport = view.findViewById(R.id.btn_import)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsUtil = PrefsUtil(requireContext())
        fileUtil = FileUtil(requireContext())

        setupCurrencyFormat()
        setupBudgetSection()
        setupCurrencySpinner()
        setupBackupButtons()
    }

    override fun onResume() {
        super.onResume()
        updateBudgetDisplay()
    }

    private fun setupCurrencyFormat() {
        val currencyCode = prefsUtil.getCurrency()
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        currencyFormat.currency = Currency.getInstance(currencyCode)
    }

    private fun setupBudgetSection() {
        updateBudgetDisplay()

        btnChangeBudget.setOnClickListener {
            showBudgetDialog()
        }
    }

    private fun updateBudgetDisplay() {
        val budget = prefsUtil.getBudget() // Access prefsUtil here
        tvCurrentBudget.text = currencyFormat.format(budget)
    }


    private fun showBudgetDialog() {
        val dialogBinding = LayoutInflater.from(context).inflate(R.layout.dialog_budget, null)
        val etBudgetAmount: EditText = dialogBinding.findViewById(R.id.et_budget_amount)
        etBudgetAmount.setText(prefsUtil.getBudget().toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding)
            .create()

        val btnCancelBudget: Button = dialogBinding.findViewById(R.id.btn_cancel_budget)
        val btnSaveBudget: Button = dialogBinding.findViewById(R.id.btn_save_budget)

        btnCancelBudget.setOnClickListener {
            dialog.dismiss()
        }

        btnSaveBudget.setOnClickListener {
            val budgetStr = etBudgetAmount.text.toString().trim()

            if (budgetStr.isEmpty()) {
                etBudgetAmount.error = "Budget amount is required"
                return@setOnClickListener
            }

            try {
                val budget = budgetStr.toDouble()
                if (budget <= 0) {
                    etBudgetAmount.error = "Budget must be greater than zero"
                    return@setOnClickListener
                }

                prefsUtil.setBudget(budget)
                updateBudgetDisplay()
                Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } catch (e: NumberFormatException) {
                etBudgetAmount.error = "Invalid budget amount"
            }
        }

        dialog.show()
    }

    private fun setupCurrencySpinner() {
        val adapter = CurrencyAdapter(requireContext(), currencies)

        spinnerCurrency.adapter = adapter

        // Set current currency
        val currentCurrency = prefsUtil.getCurrency()
        val position = currencies.indexOf(currentCurrency)
        if (position != -1) {
            spinnerCurrency.setSelection(position)
        }

        btnApplyCurrency.setOnClickListener {
            val selectedPosition = spinnerCurrency.selectedItemPosition
            val selectedCurrency = currencies[selectedPosition]
            prefsUtil.setCurrency(selectedCurrency)

            // Make sure the dashboard reflects the currency change
            (requireActivity() as? MainActivity)?.supportFragmentManager?.fragments?.forEach {
                if (it is DashboardFragment) {
                    it.updateDashboard() // Make sure you have this method in your DashboardFragment
                }
            }

            setupCurrencyFormat()
            updateBudgetDisplay()
            Toast.makeText(requireContext(), "Currency updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBackupButtons() {
        btnExport.setOnClickListener {
            exportData()
        }

        btnImport.setOnClickListener {
            showBackupFilesList()
        }
    }

    private fun exportData() {
        val jsonData = prefsUtil.exportDataToJson()
        val filePath = fileUtil.exportData(jsonData)

        if (filePath != null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.export_success),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.export_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showBackupFilesList() {
        val backupFiles = fileUtil.getBackupFiles()

        if (backupFiles.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No backup files found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val fileNames = backupFiles.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Backup File")
            .setItems(fileNames) { _, which ->
                importData(backupFiles[which].absolutePath)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun importData(filePath: String) {
        val jsonData = fileUtil.importData(filePath)

        if (jsonData != null && prefsUtil.importDataFromJson(jsonData)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.import_success),
                Toast.LENGTH_SHORT
            ).show()
            updateBudgetDisplay()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.import_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // New method to delete a transaction and update the budget
    fun deleteTransaction(transactionId: String) {
        prefsUtil.deleteTransaction(transactionId)
        updateBudgetDisplay()  // Refresh the displayed budget after deletion
        Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
    }
}
