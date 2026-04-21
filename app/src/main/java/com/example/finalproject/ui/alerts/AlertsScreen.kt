package com.example.finalproject.ui.alerts

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finalproject.data.Alert
import com.example.finalproject.data.AlertDirection
import com.example.finalproject.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onBack: () -> Unit,
    presetTicker: String? = null,
    vm: AlertsViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val alerts by vm.alerts.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(presetTicker != null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* ignore result */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add alert")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Checking prices every minute while signed in.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }
            when {
                state.isLoading -> LoadingIndicator(label = "Loading alerts\u2026")
                alerts.isEmpty() -> EmptyAlertsState()
                else -> AlertsList(alerts = alerts, onDelete = vm::deleteAlert)
            }
        }
    }

    if (showAddDialog) {
        AddAlertDialog(
            presetTicker = presetTicker,
            onDismiss = { showAddDialog = false },
            onCreate = { ticker, dir, threshold ->
                vm.createAlert(ticker, dir, threshold)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyAlertsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text("No alerts set", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap + to create one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AlertsList(alerts: List<Alert>, onDelete: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(alerts, key = { it.id }) { alert ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(alert.ticker, style = MaterialTheme.typography.titleMedium)
                        val verb = if (alert.direction == AlertDirection.ABOVE) "above" else "below"
                        Text(
                            "$verb $${"%.2f".format(alert.threshold)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (alert.triggered) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Triggered",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(alert.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAlertDialog(
    presetTicker: String?,
    onDismiss: () -> Unit,
    onCreate: (String, AlertDirection, Double) -> Unit
) {
    var ticker by rememberSaveable { mutableStateOf(presetTicker.orEmpty()) }
    var direction by rememberSaveable { mutableStateOf(AlertDirection.ABOVE) }
    var thresholdText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create alert") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Ticker") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    enabled = presetTicker == null
                )
                Text("Notify me when price is:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = direction == AlertDirection.ABOVE,
                        onClick = { direction = AlertDirection.ABOVE },
                        label = { Text("Above") }
                    )
                    FilterChip(
                        selected = direction == AlertDirection.BELOW,
                        onClick = { direction = AlertDirection.BELOW },
                        label = { Text("Below") }
                    )
                }
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Threshold (USD)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val v = thresholdText.toDoubleOrNull() ?: return@TextButton
                    onCreate(ticker, direction, v)
                },
                enabled = ticker.isNotBlank() && thresholdText.toDoubleOrNull() != null
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}