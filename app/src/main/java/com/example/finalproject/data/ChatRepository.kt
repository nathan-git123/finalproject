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
// AI assisted when stuck (about half).
@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun messagesFor(ticker: String): Flow<List<Message>> = callbackFlow {
        val reg = firestore.collection("chats")
            .document(ticker)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val msgs = snap?.documents?.mapNotNull { d ->
                    d.toObject(Message::class.java)?.copy(id = d.id)
                } ?: emptyList()
                trySend(msgs)
            }
        awaitClose { reg.remove() }
    }

    suspend fun send(ticker: String, senderId: String, senderEmail: String, text: String) {
        val data = mapOf(
            "senderId" to senderId,
            "senderEmail" to senderEmail,
            "text" to text.trim(),
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("chats")
            .document(ticker)
            .collection("messages")
            .add(data)
            .await()
    }
}
