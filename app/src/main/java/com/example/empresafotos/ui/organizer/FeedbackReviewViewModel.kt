package com.example.empresafotos.ui.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.empresafotos.data.model.Feedback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Sealed class to represent the UI state
sealed class FeedbackUiState {
    object Loading : FeedbackUiState()
    data class Success(val feedbacks: List<Feedback>) : FeedbackUiState()
    data class Error(val message: String) : FeedbackUiState()
}

class FeedbackReviewViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Loading)
    val uiState: StateFlow<FeedbackUiState> = _uiState

    init {
        fetchFeedbacks()
    }

    fun fetchFeedbacks() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = FeedbackUiState.Error("No has iniciado sesión.")
                return@launch
            }

            _uiState.value = FeedbackUiState.Loading
            try {
                val eventsSnapshot = db.collection("events")
                    .whereEqualTo("organizerId", currentUser.uid)
                    .get()
                    .await()

                val eventIds = eventsSnapshot.documents.map { it.id }
                if (eventIds.isEmpty()) {
                    _uiState.value = FeedbackUiState.Success(emptyList())
                    return@launch
                }

                val feedbackSnapshot = db.collection("feedback").whereIn("eventId", eventIds).get().await()
                val feedbacks = feedbackSnapshot.documents.map { doc ->
                    Feedback(
                        id = doc.id,
                        eventName = doc.getString("eventName") ?: "Evento Desconocido",
                        comment = doc.getString("comment") ?: "Sin comentario",
                        userEmail = doc.getString("userEmail") ?: "Correo no disponible"
                    )
                }
                _uiState.value = FeedbackUiState.Success(feedbacks)
            } catch (e: Exception) {
                _uiState.value = FeedbackUiState.Error("Error al cargar el feedback: ${e.message}")
            }
        }
    }

    fun deleteSelectedFeedbacks(feedbackIds: Set<String>) {
        viewModelScope.launch {
            if (feedbackIds.isEmpty()) return@launch

            try {
                val batch = db.batch()
                feedbackIds.forEach { feedbackId ->
                    batch.delete(db.collection("feedback").document(feedbackId))
                }
                batch.commit().await()
                // Refresh the list after deletion
                fetchFeedbacks()
            } catch (e: Exception) {
                // Optionally, handle the error state on the UI
            }
        }
    }
}
