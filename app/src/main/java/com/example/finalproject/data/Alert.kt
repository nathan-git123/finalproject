package com.example.finalproject.data

import com.google.firebase.Timestamp

enum class AlertDirection { ABOVE, BELOW }

data class Alert(
    val id: String = "",
    val ticker: String = "",
    val direction: AlertDirection = AlertDirection.ABOVE,
    val threshold: Double = 0.0,
    val createdAt: Timestamp? = null,
    val triggered: Boolean = false,
    val triggeredAt: Timestamp? = null
)