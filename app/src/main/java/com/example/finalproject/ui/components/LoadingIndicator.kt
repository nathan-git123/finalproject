package com.example.finalproject.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator // mine
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// AI did almost everything here except the colors.
val NeonPink = Color(0xFFFF00C8)

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    label: String? = "Loading\u2026"
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = NeonPink) // mine
        if (!label.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}