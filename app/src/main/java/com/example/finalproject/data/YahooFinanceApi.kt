package com.example.finalproject.data

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YahooFinanceApi {
    @GET("v8/finance/chart/{ticker}")
    suspend fun getChart(
        @Path("ticker") ticker: String,
        @Query("range") range: String = "1y",
        @Query("interval") interval: String = "1d"
    ): YahooChartResponse
}

data class YahooChartResponse(val chart: YahooChart)
data class YahooChart(val result: List<YahooChartResult>?, val error: YahooError?)
data class YahooError(val code: String?, val description: String?)
data class YahooChartResult(
    val meta: YahooMeta,
    val timestamp: List<Long>?,
    val indicators: YahooIndicators
)
data class YahooMeta(
    @Json(name = "regularMarketPrice") val regularMarketPrice: Double?,
    @Json(name = "chartPreviousClose") val chartPreviousClose: Double?,
    @Json(name = "previousClose") val previousClose: Double?,
    val symbol: String?
)
data class YahooIndicators(val quote: List<YahooQuote>?)
data class YahooQuote(val close: List<Double?>?)