package com.ebf.financeapp.data.remote



import retrofit2.http.GET
import retrofit2.http.Query

interface FinanceApiService {

    @GET("exchange-rates")
    suspend fun getExchangeRates(): ExchangeRatesResponse

    @GET("financial-tips")
    suspend fun getFinancialTips(): FinancialTipsResponse

    @GET("spending-advice")
    suspend fun getSpendingAdvice(
        @Query("percent") percent: Int
    ): SpendingAdviceResponse
}