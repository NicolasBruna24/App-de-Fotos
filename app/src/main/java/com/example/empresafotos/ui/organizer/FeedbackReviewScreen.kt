package com.example.empresafotos.ui.organizer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.empresafotos.data.model.Feedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackReviewScreen(navController: NavController, viewModel: FeedbackReviewViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFeedbacks by remember { mutableStateOf(setOf<String>()) }
    val isInSelectionMode = selectedFeedbacks.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isInSelectionMode) "${selectedFeedbacks.size} seleccionados" else "Revisión de Feedback") },
                navigationIcon = {
                    if (isInSelectionMode) {
                        IconButton(onClick = { selectedFeedbacks = emptySet() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar Selección")
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (isInSelectionMode) {
                        IconButton(onClick = {
                            viewModel.deleteSelectedFeedbacks(selectedFeedbacks)
                            selectedFeedbacks = emptySet() // Clear selection immediately
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar Feedback")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is FeedbackUiState.Loading -> CircularProgressIndicator()
                is FeedbackUiState.Success -> {
                    if (state.feedbacks.isEmpty()) {
                        Text("No hay feedback disponible por el momento.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                            items(state.feedbacks, key = { it.id }) { feedback ->
                                FeedbackItem(
                                    feedback = feedback,
                                    isSelected = selectedFeedbacks.contains(feedback.id),
                                    isInSelectionMode = isInSelectionMode,
                                    onToggleSelection = {
                                        selectedFeedbacks = if (selectedFeedbacks.contains(feedback.id)) {
                                            selectedFeedbacks - feedback.id
                                        } else {
                                            selectedFeedbacks + feedback.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is FeedbackUiState.Error -> Text(state.message)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedbackItem(feedback: Feedback, isSelected: Boolean, isInSelectionMode: Boolean, onToggleSelection: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = {
                    if (isInSelectionMode) {
                        onToggleSelection()
                    }
                },
                onLongClick = onToggleSelection
            )
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = feedback.userEmail, fontWeight = FontWeight.Bold)
            Text(text = "Evento: ${feedback.eventName}")
            Text(text = feedback.comment)
        }
    }
}
