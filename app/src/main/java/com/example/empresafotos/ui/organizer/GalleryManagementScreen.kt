package com.example.empresafotos.ui.organizer

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GalleryManagementScreen(navController: NavController, eventId: String?) {
    var showQrDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var photoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedPhotoUrls by remember { mutableStateOf(setOf<String>()) }
    val coroutineScope = rememberCoroutineScope()

    if (eventId == null) {
        // Handle error state
        return
    }

    DisposableEffect(eventId) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("events").document(eventId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val urlsRaw = snapshot.get("photoUrls")
                    if (urlsRaw is List<*>) {
                        photoUrls = urlsRaw.mapNotNull { it as? String }
                    }
                }
            }
        onDispose {
            listener.remove()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isLoading = true
            // TODO: Handle image upload in a ViewModel
            val storage = FirebaseStorage.getInstance()
            val db = FirebaseFirestore.getInstance()
            val imageUrls = mutableListOf<String>()

            uris.forEach { uri ->
                val storageRef = storage.reference.child("events/$eventId/${uri.lastPathSegment}")
                storageRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    storageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        imageUrls.add(downloadUri.toString())
                        if (imageUrls.size == uris.size) {
                            db.collection("events").document(eventId)
                                .update("photoUrls", FieldValue.arrayUnion(*imageUrls.toTypedArray()))
                                .addOnCompleteListener { 
                                    isLoading = false 
                                }
                        }
                    }
                }
            }
        }
    }
    
    fun deleteSelectedPhotos() {
        coroutineScope.launch {
            isLoading = true
            try {
                val storage = FirebaseStorage.getInstance()
                val db = FirebaseFirestore.getInstance()

                // Delete from Storage
                selectedPhotoUrls.forEach { url ->
                    try {
                        storage.getReferenceFromUrl(url).delete().await()
                    } catch (e: Exception) {
                        Log.e("DeletePhoto", "Failed to delete photo from Storage: $url", e)
                    }
                }

                // Delete from Firestore
                db.collection("events").document(eventId)
                    .update("photoUrls", FieldValue.arrayRemove(*selectedPhotoUrls.toTypedArray()))
                    .await()

            } catch (e: Exception) {
                Log.e("DeletePhoto", "Error deleting photos", e)
            } finally {
                selectedPhotoUrls = emptySet()
                isSelectionMode = false
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSelectionMode) "${selectedPhotoUrls.size} seleccionadas" else "Gestionar Galería") },
                navigationIcon = {
                    if (!isSelectionMode) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedPhotoUrls = emptySet()
                        }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancelar Selección")
                        }
                        IconButton(onClick = ::deleteSelectedPhotos, enabled = selectedPhotoUrls.isNotEmpty()) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!isSelectionMode) {
                 Button(onClick = { imagePickerLauncher.launch("image/*") }, enabled = !isLoading) {
                    Text("Subir Fotos")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showQrDialog = true }) {
                    Text("Generar Código QR")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
           

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 128.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(photoUrls) { url ->
                        val isSelected = selectedPhotoUrls.contains(url)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedPhotoUrls = if (isSelected) {
                                                selectedPhotoUrls - url
                                            } else {
                                                selectedPhotoUrls + url
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        isSelectionMode = true
                                        selectedPhotoUrls = selectedPhotoUrls + url
                                    }
                                )
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Foto de la galería",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 4.dp, 
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQrDialog) {
        QrDisplayDialog(eventId = eventId) {
            showQrDialog = false
        }
    }
}

@Composable
fun QrDisplayDialog(eventId: String?, onDismiss: () -> Unit) {
    val qrBitmap = remember(eventId) {
        if (eventId.isNullOrEmpty()) null else generateQrCode(eventId)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.size(250.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "ID del Evento: $eventId")
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun generateQrCode(content: String): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return bmp
}

@Preview(showBackground = true)
@Composable
fun GalleryManagementScreenPreview() {
    GalleryManagementScreen(navController = rememberNavController(), eventId = "sample-event-id")
}
