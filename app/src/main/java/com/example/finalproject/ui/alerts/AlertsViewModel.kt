package com.example.finalproject.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.Alert
import com.example.finalproject.data.AlertDirection
import com.example.finalproject.data.AlertsRepository
import com.example.finalproject.data.AuthRepository
import com.example.finalproject.data.MarketDataRepository
import com.example.finalproject.data.SP500Validator
import com.example.finalproject.notifications.AlertNotifier
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val adding: Boolean = false
)
// Mostly AI generated.
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertsRepo: AlertsRepository,
    private val authRepo: AuthRepository,
    private val marketRepo: MarketDataRepository,
    private val notifier: AlertNotifier,
    private val validator: SP500Validator
) : ViewModel() {

    private val _ui = MutableStateFlow(AlertsUiState())
    val ui: StateFlow<AlertsUiState> = _ui.asStateFlow()

    private val _userId = MutableStateFlow(authRepo.currentUser?.uid)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val alerts: StateFlow<List<Alert>> = _userId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else alertsRepo.alertsFor(uid)
        }
        .onEach { _ui.value = _ui.value.copy(isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun createAlert(ticker: String, direction: AlertDirection, threshold: Double) {
        val uid = authRepo.currentUser?.uid ?: return
        val t = ticker.trim().uppercase()
        if (!validator.isValid(t)) {
            _ui.value = _ui.value.copy(error = "$t is not a supported ticker.")
            return
        }
        if (threshold <= 0) {
            _ui.value = _ui.value.copy(error = "Threshold must be greater than 0.")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(adding = true, error = null)

            val alertId = runCatching { alertsRepo.add(uid, t, direction, threshold) }
                .getOrElse { e ->
                    _ui.value = _ui.value.copy(error = e.message, adding = false)
                    return@launch
                }

            // Immediate check: if condition is already satisfied, fire + mark triggered.
            runCatching {
                val price = marketRepo.fetchLatestMinutePrice(t).getOrNull()
                if (price != null && shouldFire(direction, threshold, price)) {
                    val alert = Alert(
                        id = alertId,
                        ticker = t,
                        direction = direction,
                        threshold = threshold,
                        createdAt = Timestamp.now(),
                        triggered = true
                    )
                    notifier.notifyPriceAlert(alert, price)
                    alertsRepo.markTriggered(uid, alertId)
                }
            }

            _ui.value = _ui.value.copy(adding = false)
        }
    }

    private fun shouldFire(direction: AlertDirection, threshold: Double, price: Double): Boolean = when (direction) {
        AlertDirection.ABOVE -> price > threshold
        AlertDirection.BELOW -> price < threshold // I wrote this logic.
    }

    fun deleteAlert(alertId: String) {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { alertsRepo.remove(uid, alertId) }
                .onFailure { e -> _ui.value = _ui.value.copy(error = e.message) }
        }
    }

    fun dismissError() {
        _ui.value = _ui.value.copy(error = null)
    }
}