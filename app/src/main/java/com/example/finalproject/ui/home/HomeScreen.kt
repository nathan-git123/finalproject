package com.example.finalproject.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finalproject.ui.auth.AuthViewModel
import com.example.finalproject.ui.components.LoadingIndicator
import com.example.finalproject.ui.watchlist.WatchlistViewModel

// AI generated
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onOpenWatchlist: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenStock: (String) -> Unit,
    onSignedOut: () -> Unit,
    authVm: AuthViewModel = hiltViewModel(),
    watchlistVm: WatchlistViewModel = hiltViewModel(),
    homeVm: HomeViewModel = hiltViewModel()
) {
    val items by watchlistVm.items.collectAsState()
    val state by watchlistVm.ui.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    val matches = remember(query, homeVm.allTickers) {
        if (query.isBlank()) emptyList()
        else homeVm.allTickers.filter { it.contains(query.trim(), ignoreCase = true) }
    }

    Scaffold(topBar = { // I edited this part.
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
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.uppercase() },
                label = { Text("Search S&P 500 Top 10") }, // My line
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { // I wrote the two buttons below this.
                // It's important that you know which account you're signed into.
                OutlinedButton(onClick = onOpenWatchlist, modifier = Modifier.weight(1f)) {
                    Text("${authVm.userEmail}'s Watchlist")
                }
                OutlinedButton(onClick = onOpenAlerts, modifier = Modifier.weight(1f)) {
                    Text("${authVm.userEmail}'s Alerts")
                }
            }
            HorizontalDivider()

            if (query.isNotBlank()) {
                SearchResults(matches = matches, onTap = onOpenStock)
            } else {
                when {
                    state.isLoading -> LoadingIndicator()
                    items.isEmpty() -> EmptyState(onOpenWatchlist = onOpenWatchlist)
                    else -> {
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
    }
}

@Composable
private fun SearchResults(matches: List<String>, onTap: (String) -> Unit) {
    if (matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No matches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(matches, key = { it }) { t ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable { onTap(t) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t, style = MaterialTheme.typography.titleMedium)
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
        Text("No chat rooms yet", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Add a stock to your watchlist to start chatting about it with other users.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenWatchlist, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Go to Watchlist")
        }
    }
}