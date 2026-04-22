package com.example.finalproject.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.data.Alert
import com.example.finalproject.data.AlertDirection
import com.example.finalproject.data.AlertsRepository
import com.example.finalproject.data.AuthRepository
import com.example.finalproject.data.MarketDataRepository
import com.example.finalproject.notifications.AlertNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
// Mostly AI generated.
@AndroidEntryPoint
class PriceAlertService : Service() {

    @Inject lateinit var authRepo: AuthRepository
    @Inject lateinit var alertsRepo: AlertsRepository
    @Inject lateinit var marketRepo: MarketDataRepository
    @Inject lateinit var notifier: AlertNotifier

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loopJob: Job? = null

    companion object {
        const val SERVICE_CHANNEL_ID = "price_alert_service"
        const val SERVICE_CHANNEL_NAME = "Price alert checker"
        const val SERVICE_NOTIFICATION_ID = 1001
        const val POLL_INTERVAL_MS = 60_000L

        fun start(context: Context) {
            val intent = Intent(context, PriceAlertService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PriceAlertService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createServiceChannel()
        startForeground(SERVICE_NOTIFICATION_ID, buildServiceNotification())
        loopJob = scope.launch { runLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        loopJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun runLoop() {
        while (scope.isActive) {
            runCatching { checkAlerts() }
            delay(POLL_INTERVAL_MS)
        }
    }

    private suspend fun checkAlerts() {
        val userId = authRepo.currentUser?.uid ?: return stopSelf()
        val alerts = runCatching { alertsRepo.activeAlertsOnce(userId) }.getOrElse { return }
        if (alerts.isEmpty()) return stopSelf()

        val byTicker = alerts.groupBy { it.ticker }
        for ((ticker, alertsForTicker) in byTicker) {
            val price = marketRepo.fetchLatestMinutePrice(ticker).getOrNull() ?: continue
            for (alert in alertsForTicker) {
                if (shouldFire(alert, price)) {
                    notifier.notifyPriceAlert(alert, price)
                    runCatching { alertsRepo.markTriggered(userId, alert.id) }
                }
            }
        }
    }

    private fun shouldFire(alert: Alert, price: Double): Boolean = when (alert.direction) {
        AlertDirection.ABOVE -> price >= alert.threshold
        AlertDirection.BELOW -> price <= alert.threshold
    }

    private fun createServiceChannel() {
        val channel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            SERVICE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Runs in the background to check your price alerts"
            setShowBadge(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun buildServiceNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MessagingApp alerts are active")
            .setContentText("Checking prices every minute")
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}