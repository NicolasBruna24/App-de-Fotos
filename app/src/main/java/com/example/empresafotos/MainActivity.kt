package com.example.empresafotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.empresafotos.ui.feedback.FeedbackScreen
import com.example.empresafotos.ui.gallery.GalleryScreen
import com.example.empresafotos.ui.login.LoginScreen
import com.example.empresafotos.ui.organizer.CreateEventScreen
import com.example.empresafotos.ui.organizer.DashboardScreen
import com.example.empresafotos.ui.organizer.FeedbackReviewScreen
import com.example.empresafotos.ui.organizer.GalleryManagementScreen
import com.example.empresafotos.ui.photodetail.PhotoDetailScreen
import com.example.empresafotos.ui.qr.QrScannerScreen
import com.example.empresafotos.ui.register.RegisterScreen
import com.example.empresafotos.ui.theme.EmpresaFotosTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmpresaFotosTheme {
                AppNavigation()
            }
        }
    }
}



@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("qrScanner") {
            QrScannerScreen(navController = navController)
        }
        composable(
            "gallery/{qrCodeValue}",
            arguments = listOf(navArgument("qrCodeValue") { type = NavType.StringType })
        ) {
            val qrCodeValue = it.arguments?.getString("qrCodeValue")
            GalleryScreen(navController = navController, qrCodeValue = qrCodeValue)
        }
        composable(
            "photoDetail/{photoUrl}",
            arguments = listOf(navArgument("photoUrl") { type = NavType.StringType })
        ) {
            val photoUrl = it.arguments?.getString("photoUrl")
            PhotoDetailScreen(navController = navController, photoUrl = photoUrl)
        }
        composable(
            "feedback/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            val eventId = it.arguments?.getString("eventId")
            FeedbackScreen(navController = navController, eventId = eventId)
        }

        // Organizer routes
        composable("organizerDashboard") {
            DashboardScreen(navController = navController)
        }
        composable("createEvent") {
            CreateEventScreen(navController = navController)
        }
        composable(
            "galleryManagement/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            val eventId = it.arguments?.getString("eventId")
            GalleryManagementScreen(navController = navController, eventId = eventId)
        }
        composable("feedbackReview") {
            FeedbackReviewScreen(navController = navController)
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            try {
                val document = db.collection("users").document(currentUser.uid).get().await()
                val role = document.getString("role")
                val destination = if (role == "organizer") "organizerDashboard" else "qrScanner"
                navController.navigate(destination) {
                    popUpTo("splash") { inclusive = true }
                }
            } catch (e: Exception) {
                // Assume client role if document doesn't exist or fails to load
                navController.navigate("qrScanner") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text("Cargando...")
    }
}
