package com.example.finalproject.ui.stock
// AI Generated
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.MarketDataRepository
import com.example.finalproject.data.StockSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockDetailUiState(
    val isLoading: Boolean = true,
    val snapshot: StockSnapshot? = null,
    val error: String? = null
)

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val repo: MarketDataRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(StockDetailUiState())
    val ui: StateFlow<StockDetailUiState> = _ui.asStateFlow()

    private var boundTicker: String? = null

    fun bindTicker(ticker: String) {
        if (boundTicker == ticker) return
        boundTicker = ticker
        load(ticker)
    }

    fun retry() {
        boundTicker?.let { load(it) }
    }

    private fun load(ticker: String) {
        viewModelScope.launch {
            _ui.value = StockDetailUiState(isLoading = true)
            val res = repo.fetchSnapshot(ticker)
            _ui.value = res.fold(
                onSuccess = { StockDetailUiState(isLoading = false, snapshot = it) },
                onFailure = { StockDetailUiState(isLoading = false, error = it.message ?: "Failed to load") }
            )
        }
    }
}