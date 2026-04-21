package com.example.finalproject.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketDataRepository @Inject constructor(
    private val api: YahooFinanceApi
) {
    suspend fun fetchSnapshot(ticker: String): Result<StockSnapshot> = runCatching {
        val response = api.getChart(ticker = ticker, range = "1y", interval = "1d")

        response.chart.error?.let { err ->
            error(err.description ?: "Yahoo error: ${err.code}")
        }

        val result = response.chart.result?.firstOrNull()
            ?: error("No data returned for $ticker")

        val timestamps = result.timestamp ?: emptyList()
        val closes = result.indicators.quote?.firstOrNull()?.close ?: emptyList()

        // Pair up timestamps and closes, dropping nulls (non-trading hours, halts, etc.)
        val history = timestamps.zip(closes)
            .mapNotNull { (ts, close) -> close?.let { PricePoint(ts, it) } }

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
}