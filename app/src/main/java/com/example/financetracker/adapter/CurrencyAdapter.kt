package com.example.financetracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.financetracker.R

class CurrencyAdapter(
    context: Context,
    private val currencyCodes: Array<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, currencyCodes) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_item, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getDisplayName(currencyCodes[position])

        //  Set text color for selected item
        textView.setTextColor(context.resources.getColor(R.color.white))




        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getDisplayName(currencyCodes[position])




        return view
    }

    private fun getDisplayName(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "US Dollar (USD)"
            "EUR" -> "Euro (EUR)"
            "GBP" -> "British Pound (GBP)"
            "JPY" -> "Japanese Yen (JPY)"
            "CAD" -> "Canadian Dollar (CAD)"
            "AUD" -> "Australian Dollar (AUD)"
            "INR" -> "Indian Rupee (INR)"
            "LKR" -> context.getString(R.string.currency_lkr)
            else -> currencyCode
        }
    }
}
