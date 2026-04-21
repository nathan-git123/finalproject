package com.example.finalproject.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketDataRepository @Inject constructor(
    private val api: YahooFinanceApi
) {
    /** Full 1-year snapshot for the stock detail chart. */
    suspend fun fetchSnapshot(ticker: String): Result<StockSnapshot> = runCatching {
        val response = api.getChart(ticker = ticker, range = "1y", interval = "1d")
        response.chart.error?.let { err -> error(err.description ?: "Yahoo error: ${err.code}") }
        val result = response.chart.result?.firstOrNull() ?: error("No data returned for $ticker")
        val timestamps = result.timestamp ?: emptyList()
        val closes = result.indicators.quote?.firstOrNull()?.close ?: emptyList()
        val history = timestamps.zip(closes).mapNotNull { (ts, close) -> close?.let { PricePoint(ts, it) } }
        val currentPrice = result.meta.regularMarketPrice
            ?: history.lastOrNull()?.close
            ?: error("No current price available for $ticker")
        val previousClose = result.meta.previousClose
            ?: result.meta.chartPreviousClose
            ?: currentPrice
        StockSnapshot(
            ticker = ticker.uppercase(),
            currentPrice = currentPrice,
            previousClose = previousClose,
            history = history
        )
    }

    /**
     * Fast fetch of the current price.
     * Tries the 1-minute endpoint first (intraday), falls back to 1-day (for off-hours / weekends).
     */
    suspend fun fetchLatestMinutePrice(ticker: String): Result<Double> = runCatching {
        // Try 1-min first
        val minuteResult = runCatching {
            val response = api.getChart(ticker = ticker, range = "1d", interval = "1m")
            val result = response.chart.result?.firstOrNull()
                ?: error("No minute data for $ticker")
            val closes = result.indicators.quote?.firstOrNull()?.close ?: emptyList()
            closes.lastOrNull { it != null }
                ?: result.meta.regularMarketPrice
                ?: error("No minute price for $ticker")
        }

        if (minuteResult.isSuccess) return@runCatching minuteResult.getOrThrow()

        // Fallback to 1-day endpoint's regularMarketPrice (works off-hours)
        val dailyResponse = api.getChart(ticker = ticker, range = "5d", interval = "1d")
        dailyResponse.chart.error?.let { err -> error(err.description ?: "Yahoo error: ${err.code}") }
        val dailyResult = dailyResponse.chart.result?.firstOrNull()
            ?: error("No daily data for $ticker")

        dailyResult.meta.regularMarketPrice
            ?: dailyResult.indicators.quote?.firstOrNull()?.close?.lastOrNull { it != null }
            ?: error("No price available for $ticker")
    }
}