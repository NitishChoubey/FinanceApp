package com.ebf.financeapp.data.remote



import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody


class MockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url     = request.url.toString()

        // Simulate network delay
        Thread.sleep(600)

        val mockJson = when {
            url.contains("exchange-rates") -> exchangeRatesJson()
            url.contains("financial-tips") -> financialTipsJson()
            url.contains("spending-advice")-> spendingAdviceJson(url)
            else -> """{"error":"endpoint not found"}"""
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(mockJson.toResponseBody("application/json".toMediaType()))
            .addHeader("content-type", "application/json")
            .build()
    }

    private fun exchangeRatesJson() = """
    {
      "base": "INR",
      "timestamp": ${System.currentTimeMillis() / 1000},
      "rates": {
        "USD": 0.012,
        "EUR": 0.011,
        "GBP": 0.0094,
        "JPY": 1.78,
        "AED": 0.044,
        "SGD": 0.016,
        "CAD": 0.016,
        "AUD": 0.018
      }
    }
    """.trimIndent()

    private fun financialTipsJson() = """
    {
      "tips": [
        {
          "id": 1,
          "category": "savings",
          "title": "The 50/30/20 Rule",
          "body": "Allocate 50%% of income to needs, 30%% to wants, and 20%% to savings. It's the simplest budgeting framework that actually works.",
          "icon": "savings",
          "color": "#1D9E75"
        },
        {
          "id": 2,
          "category": "spending",
          "title": "24-Hour Rule",
          "body": "Before any non-essential purchase above ₹2,000, wait 24 hours. Impulse buying drops by 60%% with this one habit.",
          "icon": "timer",
          "color": "#378ADD"
        },
        {
          "id": 3,
          "category": "investment",
          "title": "Start a SIP Today",
          "body": "A ₹5,000/month SIP at 12%% annual return becomes ₹50 lakhs in 20 years. Time in the market beats timing the market.",
          "icon": "trending_up",
          "color": "#8B5CF6"
        },
        {
          "id": 4,
          "category": "budgeting",
          "title": "Track Every Rupee",
          "body": "People who track expenses spend 15-20%% less than those who don't. Awareness itself changes behaviour.",
          "icon": "receipt_long",
          "color": "#EF9F27"
        },
        {
          "id": 5,
          "category": "emergency",
          "title": "Build Your Safety Net",
          "body": "Aim for 3-6 months of expenses in a liquid fund. This is your financial immune system against life's surprises.",
          "icon": "security",
          "color": "#E24B4A"
        }
      ]
    }
    """.trimIndent()

    private fun spendingAdviceJson(url: String): String {
        // Parse spending percent from URL query param
        val pct = url.substringAfter("percent=").substringBefore("&").toIntOrNull() ?: 50
        val advice = when {
            pct > 90 -> "You've spent over 90% of your budget. Consider pausing non-essential purchases for the rest of the month."
            pct > 75 -> "You're at $pct% of your monthly budget. You're on track — stay mindful of discretionary spending."
            pct > 50 -> "You've used $pct% of your budget at this point in the month. Great discipline so far!"
            else     -> "Excellent! Only $pct% of your budget used. You're well ahead of schedule to hit your savings goal."
        }
        return """{"percent":$pct,"advice":"$advice","status":"${if (pct > 90) "danger" else if (pct > 75) "warning" else "good"}"}"""
    }
}