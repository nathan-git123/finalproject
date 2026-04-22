package com.example.finalproject.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finalproject.data.Message
import com.example.finalproject.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Locale
// AI generated
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    ticker: String,
    onBack: () -> Unit,
    vm: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(ticker) { vm.bindTicker(ticker) }

    val state by vm.ui.collectAsState()
    val messages by vm.messages.collectAsState()
    val meUid = vm.currentUserId
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$ticker chatroom") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            InputBar(
                value = state.draft,
                enabled = !state.sending,
                onValueChange = vm::onDraft,
                onSend = vm::send
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            when {
                state.isLoading -> LoadingIndicator(label = "Loading messages\u2026")
                messages.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No messages yet. Be the first to post.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(msg = msg, isMine = msg.senderId == meUid)
                        }
                    }
                }
            }
        }
    }
}

private fun usernameFromEmail(email: String): String {
    if (email.isBlank()) return "unknown"
    val at = email.indexOf('@')
    return if (at > 0) email.substring(0, at) else email
}

@Composable
private fun MessageBubble(msg: Message, isMine: Boolean) {
    val bg = if (isMine) MaterialTheme.colorScheme.primary
             else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isMine) MaterialTheme.colorScheme.onPrimary
             else MaterialTheme.colorScheme.onSurfaceVariant
    val align = if (isMine) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        if (!isMine) {
            Text(
                usernameFromEmail(msg.senderEmail),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(msg.text, color = fg)
        }
        msg.timestamp?.toDate()?.let { date ->
            val fmt = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
            Text(
                fmt.format(date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun InputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                maxLines = 4,
                enabled = enabled
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}