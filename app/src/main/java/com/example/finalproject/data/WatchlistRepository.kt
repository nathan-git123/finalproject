package com.example.finalproject.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
// AI assisted
@Singleton
class WatchlistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun watchlistFor(userId: String): Flow<List<WatchlistItem>> = callbackFlow {
        val reg = firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull {
                    it.toObject(WatchlistItem::class.java)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { reg.remove() }
    }

    suspend fun add(userId: String, ticker: String) {
        val t = ticker.trim().uppercase()
        if (t.isBlank()) return
        val data = mapOf(
            "ticker" to t,
            "addedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(t)
            .set(data)
            .await()
    }

    suspend fun remove(userId: String, ticker: String) {
        firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(ticker.uppercase())
            .delete()
            .await()
    }
}
