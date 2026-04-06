package com.ebf.financeapp.data.remote



import com.google.gson.annotations.SerializedName

data class ExchangeRatesResponse(
    val base: String,
    val timestamp: Long,
    val rates: Map<String, Double>
)

data class FinancialTipsResponse(
    val tips: List<FinancialTip>
)

data class FinancialTip(
    val id: Int,
    val category: String,
    val title: String,
    val body: String,
    val icon: String,
    val color: String
)

data class SpendingAdviceResponse(
    val percent: Int,
    val advice: String,
    val status: String  // "good" | "warning" | "danger"
)