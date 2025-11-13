package com.example.empresafotos.ui.photodetail

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(navController: NavController, photoUrl: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val decodedUrl = photoUrl // The URL is already decoded by the navigation component

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isDownloading by remember { mutableStateOf(false) }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        null
    } else {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                imageBitmap?.let {
                    coroutineScope.launch {
                        isDownloading = true
                        val saved = saveImageToDownloads(context, it, "${System.currentTimeMillis()}.jpg")
                        if (saved) {
                            Toast.makeText(context, "Descarga completada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No se pudo guardar la imagen", Toast.LENGTH_LONG).show()
                        }
                        isDownloading = false
                    }
                }
            } else {
                Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var imageSize by remember { mutableStateOf(Size.Zero) }
    val scale = remember { Animatable(1f) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        coroutineScope.launch {
            val newScale = (scale.value * zoomChange).coerceIn(1f, 5f)
            scale.snapTo(newScale)

            if (newScale > 1f) {
                val newOffset = offset.value + panChange
                val maxBoundX = (imageSize.width * (newScale - 1)) / 2
                val maxBoundY = (imageSize.height * (newScale - 1)) / 2
                offset.snapTo(
                    Offset(
                        x = newOffset.x.coerceIn(-maxBoundX, maxBoundX),
                        y = newOffset.y.coerceIn(-maxBoundY, maxBoundY)
                    )
                )
            } else {
                offset.snapTo(Offset.Zero)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                )
            )
        },
        floatingActionButton = {
            if (decodedUrl != null) {
                FloatingActionButton(onClick = {
                    val bitmapToSave = imageBitmap
                    if (bitmapToSave == null) {
                        Toast.makeText(context, "La imagen aún no se ha cargado.", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }

                    val permissionStatus = permissionToRequest?.let {
                        ContextCompat.checkSelfPermission(context, it)
                    } ?: PackageManager.PERMISSION_GRANTED

                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                        coroutineScope.launch {
                            isDownloading = true
                            val saved = saveImageToDownloads(context, bitmapToSave, "${System.currentTimeMillis()}.jpg")
                            if (saved) {
                                Toast.makeText(context, "Descarga completada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "No se pudo guardar la imagen", Toast.LENGTH_LONG).show()
                            }
                            isDownloading = false
                        }
                    } else {
                        permissionToRequest?.let { launcher.launch(it) }
                    }
                }) {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Download, contentDescription = "Descargar Foto")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (decodedUrl == null) {
                Text("URL de la foto no válida.")
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(decodedUrl)
                        .allowHardware(false)
                        .crossfade(false)
                        .build(),
                    contentDescription = "Foto en detalle",
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { imageSize = it.toSize() }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    coroutineScope.launch {
                                        if (scale.value > 1f) {
                                            scale.animateTo(1f, animationSpec = tween(300))
                                            offset.animateTo(Offset.Zero, animationSpec = tween(300))
                                        } else {
                                            scale.animateTo(2.5f, animationSpec = tween(300))
                                        }
                                    }
                                }
                            )
                        }
                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value,
                            translationX = offset.value.x,
                            translationY = offset.value.y
                        )
                        .transformable(state = transformableState),
                    contentScale = ContentScale.Fit
                ) {
                    val state = painter.state
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator()
                        }
                        is AsyncImagePainter.State.Success -> {
                            imageBitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                            SubcomposeAsyncImageContent()
                        }
                        is AsyncImagePainter.State.Error -> {
                            val errorMessage = state.result.throwable.message ?: "Error desconocido"
                            LaunchedEffect(decodedUrl, errorMessage) {
                                Toast.makeText(context, "URL: $decodedUrl\nError: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                            Text("No se pudo cargar la imagen.")
                        }
                        else -> { // Empty
                            Text("No se pudo cargar la imagen.")
                        }
                    }
                }
            }
        }
    }
}

private fun saveImageToDownloads(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        var uri: Uri? = null
        return try {
            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri == null) {
                return false
            }
            resolver.openOutputStream(uri)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    return false
                }
            } ?: return false
            true
        } catch (e: Exception) {
            uri?.let { resolver.delete(it, null, null) }
            e.printStackTrace()
            false
        }
    } else {
        @Suppress("DEPRECATION")
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val imageFile = java.io.File(downloadsDir, fileName)
        return try {
            java.io.FileOutputStream(imageFile).use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    return false
                }
            }
            // Notify the media scanner
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(imageFile)
            context.sendBroadcast(mediaScanIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
