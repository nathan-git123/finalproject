package com.example.finalproject.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(ticker: String, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("$ticker chatroom") })
    }) { padding ->
        Box(Modifier.padding(padding).padding(24.dp)) {
            Text("Chat goes here")
        }
    }
}