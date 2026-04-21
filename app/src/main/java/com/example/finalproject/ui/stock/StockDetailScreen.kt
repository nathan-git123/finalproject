package com.example.finalproject.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finalproject.data.StockSnapshot
import com.example.finalproject.ui.components.LoadingIndicator
import com.example.finalproject.ui.components.NeonPink
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    ticker: String,
    onBack: () -> Unit,
    onSetAlert: (String) -> Unit,
    vm: StockDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(ticker) { vm.bindTicker(ticker) }
    val state by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ticker) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onSetAlert(ticker) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Set alert")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> LoadingIndicator(label = "Loading $ticker\u2026")
                state.error != null -> ErrorView(message = state.error!!, onRetry = vm::retry)
                state.snapshot != null -> SnapshotView(state.snapshot!!)
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Couldn\u2019t load stock data", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun SnapshotView(snap: StockSnapshot) {
    val isUp = snap.dayChange >= 0
    val changeColor = if (isUp) Color(0xFF1B8E3E) else MaterialTheme.colorScheme.error
    val sign = if (isUp) "+" else ""

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("$${"%.2f".format(snap.currentPrice)}", style = MaterialTheme.typography.displaySmall)
        Text(
            "$sign${"%.2f".format(snap.dayChange)} ($sign${"%.2f".format(snap.dayChangePct)}%) today",
            color = changeColor,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "1-year price history",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChartView(snap = snap, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun ChartView(snap: StockSnapshot, modifier: Modifier = Modifier) {
    val pinkArgb = NeonPink.toArgb()
    val axisArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                axisRight.isEnabled = false
                axisLeft.textColor = axisArgb
                axisLeft.setDrawGridLines(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = axisArgb
                xAxis.setDrawGridLines(false)
                xAxis.setLabelCount(4, true)
                xAxis.valueFormatter = object : ValueFormatter() {
                    private val fmt = SimpleDateFormat("MMM ''yy", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return fmt.format(Date(value.toLong() * 1000L))
                    }
                }
            }
        },
        update = { chart ->
            val entries = snap.history.map { Entry(it.timestampSec.toFloat(), it.close.toFloat()) }
            val dataSet = LineDataSet(entries, snap.ticker).apply {
                color = pinkArgb
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = pinkArgb
                fillAlpha = 40
                mode = LineDataSet.Mode.LINEAR
                highLightColor = pinkArgb
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}