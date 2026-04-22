package com.example.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// AI assisted
@Singleton
class AuthRepository @Inject constructor(
    // Provided by Hilt
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Sign-in returned null user")
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Sign-up returned null user")
    }

    fun signOut() = auth.signOut()
}