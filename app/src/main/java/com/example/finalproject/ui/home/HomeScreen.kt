package com.example.finalproject.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finalproject.ui.auth.AuthViewModel
import com.example.finalproject.ui.watchlist.WatchlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onOpenWatchlist: () -> Unit,
    onSignedOut: () -> Unit,
    authVm: AuthViewModel = hiltViewModel(),
    watchlistVm: WatchlistViewModel = hiltViewModel()
) {
    val items by watchlistVm.items.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("MessagingApp") },
            actions = {
                TextButton(onClick = { authVm.signOut(); onSignedOut() }) {
                    Text("Sign out")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onOpenWatchlist, modifier = Modifier.fillMaxWidth()) {
                Text("My Watchlist")
            }
            HorizontalDivider()

            if (items.isEmpty()) {
                EmptyState(onOpenWatchlist = onOpenWatchlist)
            } else {
                Text("Chat rooms:", style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.ticker }) { item ->
                        Button(
                            onClick = { onOpenChat(item.ticker) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${item.ticker} chatroom")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onOpenWatchlist: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No chat rooms yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add a stock to your watchlist to start chatting about it with other users.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onOpenWatchlist,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Go to Watchlist")
        }
    }
}