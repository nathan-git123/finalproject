package com.example.finalproject.data

import com.google.firebase.Timestamp
data class WatchlistItem(
    val ticker: String = "",
    val addedAt: Timestamp? = null
)
