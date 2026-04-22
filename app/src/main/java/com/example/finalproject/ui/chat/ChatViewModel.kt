package com.example.finalproject.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.AuthRepository
import com.example.finalproject.data.ChatRepository
import com.example.finalproject.data.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
// AI generated
data class ChatUiState(
    val draft: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatUiState())
    val ui: StateFlow<ChatUiState> = _ui.asStateFlow()

    private val _ticker = MutableStateFlow("")

    val messages: StateFlow<List<Message>> =
        kotlinx.coroutines.flow.flow {
            _ticker.collect { t ->
                if (t.isNotBlank()) {
                    chatRepo.messagesFor(t).collect { emit(it) }
                }
            }
        }
        .onEach { _ui.value = _ui.value.copy(isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun bindTicker(ticker: String) {
        if (_ticker.value != ticker) {
            _ui.value = _ui.value.copy(isLoading = true)
            _ticker.value = ticker
        }
    }

    fun onDraft(v: String) { _ui.value = _ui.value.copy(draft = v, error = null) }

    fun send() {
        val s = _ui.value
        val text = s.draft.trim()
        val ticker = _ticker.value
        val user = authRepo.currentUser
        if (text.isBlank() || ticker.isBlank() || user == null) return

        viewModelScope.launch {
            _ui.value = s.copy(sending = true, error = null, draft = "")
            runCatching {
                chatRepo.send(
                    ticker = ticker,
                    senderId = user.uid,
                    senderEmail = user.email.orEmpty(),
                    text = text
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(error = e.message, draft = text)
            }
            _ui.value = _ui.value.copy(sending = false)
        }
    }

    val currentUserId: String? get() = authRepo.currentUser?.uid
}