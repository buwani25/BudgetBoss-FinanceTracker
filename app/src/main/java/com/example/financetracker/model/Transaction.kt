package com.example.financetracker.model

import java.io.Serializable
import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val date: Long = Date().time
) : Serializable
