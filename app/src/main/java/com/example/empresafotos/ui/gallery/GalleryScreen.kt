package com.example.empresafotos.ui.gallery

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(navController: NavController, qrCodeValue: String?) {
    var photoUrls by remember { mutableStateOf<List<String>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    // Redirect to login if not authenticated
    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo(0) // Clear back stack
            }
        }
    }


    if (qrCodeValue.isNullOrBlank() || qrCodeValue.contains("/")) {
        errorMessage = "Código QR no válido"
    } else {
        DisposableEffect(qrCodeValue) {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("events").document(qrCodeValue)
            val listener = docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "Error al cargar la galería."
                    photoUrls = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val urls = snapshot.get("photoUrls") as? List<String>
                    photoUrls = urls ?: emptyList()
                } else {
                    errorMessage = "Evento no encontrado."
                    photoUrls = emptyList()
                }
            }
            onDispose {
                listener.remove()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galería") },
                actions = {
                    IconButton(onClick = { navController.navigate("feedback/$qrCodeValue") }) {
                        Icon(Icons.Filled.Email, contentDescription = "Dejar Feedback")
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                errorMessage != null -> {
                    Text(errorMessage!!)
                }
                photoUrls == null -> {
                    CircularProgressIndicator()
                }
                photoUrls!!.isEmpty() -> {
                    Text("No hay fotos en esta galería.")
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photoUrls!!) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Foto de la galería",
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable {
                                        val encodedUrl = Uri.encode(url)
                                        navController.navigate("photoDetail/$encodedUrl")
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryScreenPreview() {
    GalleryScreen(navController = rememberNavController(), qrCodeValue = "sample-qr-code")
}
