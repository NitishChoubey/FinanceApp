package com.ebf.financeapp.data.remote



import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

@Singleton
class ApiRepository @Inject constructor(
    private val api: FinanceApiService
) {
    suspend fun getExchangeRates(): ApiResult<ExchangeRatesResponse> = safeCall {
        api.getExchangeRates()
    }

    suspend fun getFinancialTips(): ApiResult<FinancialTipsResponse> = safeCall {
        api.getFinancialTips()
    }

    suspend fun getSpendingAdvice(percent: Int): ApiResult<SpendingAdviceResponse> = safeCall {
        api.getSpendingAdvice(percent)
    }

    private suspend fun <T> safeCall(call: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(call())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}