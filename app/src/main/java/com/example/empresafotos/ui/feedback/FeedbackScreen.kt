package com.example.empresafotos.ui.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController, eventId: String?) {
    var feedback by remember { mutableStateOf("") }
    var eventName by remember { mutableStateOf("Cargando nombre del evento...") }
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(eventId) {
        if (eventId != null) {
            try {
                val document = db.collection("events").document(eventId).get().await()
                eventName = document.getString("name") ?: "Evento Desconocido"
            } catch (e: Exception) {
                eventName = "Evento Desconocido"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dejar Feedback") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Evento: $eventName")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("Escribe tu feedback aquí") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                if (feedback.isNotBlank() && currentUser != null && eventId != null) {
                    val feedbackData = hashMapOf(
                        "comment" to feedback,
                        "userEmail" to (currentUser.email ?: "Correo no disponible"),
                        "eventName" to eventName,
                        "eventId" to eventId
                    )
                    db.collection("feedback")
                        .add(feedbackData)
                        .addOnSuccessListener { 
                            navController.popBackStack()
                        }
                }
            }) {
                Text("Enviar Feedback")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    FeedbackScreen(navController = rememberNavController(), eventId = "sample-event-id")
}
