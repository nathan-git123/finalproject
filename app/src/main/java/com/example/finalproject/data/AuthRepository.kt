package com.example.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// AI-assisted.
@Singleton
class AuthRepository @Inject constructor(
    // Provided by Hilt
    private val auth: FirebaseAuth
) {
    // Checks to see if user is signed in.
    val currentUser: FirebaseUser? get() = auth.currentUser

    // Exposes auth state as a Flow so the UI reacts to sign-in/sign-out automatically.
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Signs an existing user in and returns an exception.
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Sign-in returned null user")
    }

    // Creates a new account.
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Sign-up returned null user")
    }

    // Signs the user out. authStateFlow will emit null, which sends the UI back to the auth screen.
    fun signOut() = auth.signOut()
}