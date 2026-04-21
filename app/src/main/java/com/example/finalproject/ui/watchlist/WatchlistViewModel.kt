package com.example.finalproject.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.AuthRepository
import com.example.finalproject.data.SP500Validator
import com.example.finalproject.data.WatchlistItem
import com.example.finalproject.data.WatchlistRepository
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

data class WatchlistUiState(
    val draft: String = "",
    val adding: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repo: WatchlistRepository,
    private val authRepo: AuthRepository,
    private val validator: SP500Validator
) : ViewModel() {

    private val _ui = MutableStateFlow(WatchlistUiState())
    val ui: StateFlow<WatchlistUiState> = _ui.asStateFlow()

    private val _userId = MutableStateFlow(authRepo.currentUser?.uid)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<WatchlistItem>> = _userId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else repo.watchlistFor(uid)
        }
        .onEach { _ui.value = _ui.value.copy(isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onDraft(v: String) { _ui.value = _ui.value.copy(draft = v, error = null) }

    fun add() {
        val uid = authRepo.currentUser?.uid ?: return
        val t = _ui.value.draft.trim().uppercase()
        if (t.isBlank()) return

        if (!validator.isValid(t)) {
            _ui.value = _ui.value.copy(
                error = "$t is not a supported ticker. Try AAPL, MSFT, NVDA, GOOGL, AMZN, META, BRK.B, LLY, AVGO, or TSLA."
            )
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(adding = true, error = null, draft = "")
            runCatching { repo.add(uid, t) }
                .onFailure { e -> _ui.value = _ui.value.copy(error = e.message, draft = t) }
            _ui.value = _ui.value.copy(adding = false)
        }
    }

    fun remove(ticker: String) {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repo.remove(uid, ticker) }
                .onFailure { e -> _ui.value = _ui.value.copy(error = e.message) }
        }
    }
}