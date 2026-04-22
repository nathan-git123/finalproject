package com.example.finalproject.data
// AI Generated
/** One price point on the chart: x=timestamp in seconds, y=close price. */
data class PricePoint(
    val timestampSec: Long,
    val close: Double
)

/** Current quote + recent history for a ticker. */
data class StockSnapshot(
    val ticker: String,
    val currentPrice: Double,
    val previousClose: Double,
    val history: List<PricePoint>
) {
    val dayChange: Double get() = currentPrice - previousClose
    val dayChangePct: Double get() = if (previousClose == 0.0) 0.0 else (dayChange / previousClose) * 100.0
}