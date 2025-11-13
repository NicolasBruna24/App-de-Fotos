package com.example.empresafotos.ui.organizer

import androidx.lifecycle.ViewModel
import com.example.empresafotos.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private var eventsListener: ListenerRegistration? = null

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            eventsListener = db.collection("events")
                .whereEqualTo("organizerId", currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val eventList = snapshot.documents.map { doc ->
                            Event(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                status = doc.getString("status") ?: ""
                            )
                        }
                        _events.value = eventList
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the listener to prevent memory leaks
        eventsListener?.remove()
    }
}
