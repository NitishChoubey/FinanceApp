package com.ebf.financeapp.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    // Currency symbol map
    private val symbolMap = mapOf(
        "INR" to "₹", "USD" to "$", "EUR" to "€",
        "GBP" to "£", "JPY" to "¥", "AED" to "د.إ",
        "SGD" to "S$", "CAD" to "CA$"
    )

    // Call this once from MainActivity after reading settings
    private var currentCurrency: String = "INR"

    fun setCurrency(code: String) {
        currentCurrency = code
    }

    private fun symbol() = symbolMap[currentCurrency] ?: currentCurrency

    fun format(amount: Double): String =
        "${symbol()}${"%.2f".format(amount)}"

    fun formatCompact(amount: Double): String = when {
        amount >= 1_00_000 -> "${symbol()}${"%.1f".format(amount / 1_00_000)}L"
        amount >= 1_000   -> "${symbol()}${"%.1f".format(amount / 1_000)}K"
        else               -> format(amount)
    }

    fun formatWithSign(amount: Double, isIncome: Boolean): String {
        val prefix = if (isIncome) "+" else "-"
        return "$prefix${format(amount)}"
    }
}