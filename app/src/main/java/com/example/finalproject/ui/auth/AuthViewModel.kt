package com.example.finalproject.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Mostly AI-generated

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpMode: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()


    val isSignedIn: StateFlow<Boolean> = repo.authStateFlow()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, repo.currentUser != null)


    fun onEmail(v: String) { _ui.value = _ui.value.copy(email = v, error = null) }
    fun onPassword(v: String) { _ui.value = _ui.value.copy(password = v, error = null) }
    fun toggleMode() { _ui.value = _ui.value.copy(isSignUpMode = !_ui.value.isSignUpMode, error = null) }


    fun submit() {
        val s = _ui.value

        if (s.email.isBlank() || s.password.length < 6) {
            _ui.value = s.copy(error = "Enter a valid email and 6+ char password")
            return
        }

        viewModelScope.launch {
            _ui.value = s.copy(isLoading = true, error = null)

            val res = coroutineScope {
                val authCall = async {
                    if (s.isSignUpMode) repo.signUp(s.email.trim(), s.password)
                    else repo.signIn(s.email.trim(), s.password)
                }
                val minDelay = async { delay(3000) }
                minDelay.await()
                authCall.await()
            }

            _ui.value = _ui.value.copy(
                isLoading = false,
                error = res.exceptionOrNull()?.message
            )
        }
    }

    fun signOut() = repo.signOut()
    val userEmail: String get() = repo.currentUser?.email.orEmpty() // I wrote this.
}