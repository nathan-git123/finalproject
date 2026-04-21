package com.example.finalproject.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

// AI Generated.
@Singleton
class SP500Validator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val tickers: List<String> by lazy { load() }
    private val tickerSet: Set<String> by lazy { tickers.toSet() }

    fun isValid(ticker: String): Boolean =
        tickerSet.contains(ticker.trim().uppercase())

    private fun load(): List<String> {
        val json = context.assets.open("sp500.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        return buildList {
            for (i in 0 until arr.length()) add(arr.getString(i).uppercase())
        }
    }
}