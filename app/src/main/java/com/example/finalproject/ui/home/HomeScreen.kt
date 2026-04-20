package com.example.finalproject.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finalproject.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onSignedOut: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("MessagingApp") },
            actions = {
                TextButton(onClick = { vm.signOut(); onSignedOut() }) {
                    Text("Sign out")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Chat rooms:", style = MaterialTheme.typography.titleMedium)
            listOf("AAPL", "TSLA", "MSFT", "GOOG").forEach { t ->
                Button(onClick = { onOpenChat(t) }, modifier = Modifier.fillMaxWidth()) {
                    Text("$t chatroom")
                }
            }
        }
    }
}