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

@Singleton
class AlertsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun alertsFor(userId: String): Flow<List<Alert>> = callbackFlow {
        val reg = firestore.collection("users")
            .document(userId)
            .collection("alerts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val alerts = snap?.documents?.mapNotNull { d ->
                    d.toObject(Alert::class.java)?.copy(id = d.id)
                } ?: emptyList()
                trySend(alerts)
            }
        awaitClose { reg.remove() }
    }

    suspend fun activeAlertsOnce(userId: String): List<Alert> {
        val snap = firestore.collection("users")
            .document(userId)
            .collection("alerts")
            .whereEqualTo("triggered", false)
            .get()
            .await()
        return snap.documents.mapNotNull { d ->
            d.toObject(Alert::class.java)?.copy(id = d.id)
        }
    }

    /** Creates an alert and returns its generated document ID. */
    suspend fun add(userId: String, ticker: String, direction: AlertDirection, threshold: Double): String {
        val data = mapOf(
            "ticker" to ticker.uppercase(),
            "direction" to direction.name,
            "threshold" to threshold,
            "createdAt" to FieldValue.serverTimestamp(),
            "triggered" to false
        )
        val ref = firestore.collection("users")
            .document(userId)
            .collection("alerts")
            .add(data)
            .await()
        return ref.id
    }

    suspend fun remove(userId: String, alertId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("alerts")
            .document(alertId)
            .delete()
            .await()
    }

    suspend fun markTriggered(userId: String, alertId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("alerts")
            .document(alertId)
            .update(
                mapOf(
                    "triggered" to true,
                    "triggeredAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}