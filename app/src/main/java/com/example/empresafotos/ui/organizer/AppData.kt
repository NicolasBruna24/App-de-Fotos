package com.example.empresafotos.ui.organizer

import androidx.compose.runtime.mutableStateListOf
import com.example.empresafotos.data.model.Event
import com.example.empresafotos.data.model.Feedback

object AppData {
    val events = mutableStateListOf(
        Event("1", "Boda de Juan y María", "Activo"),
        Event("2", "Cumpleaños de Ana", "Activo"),
        Event("3", "Conferencia de Tecnología", "Pasado")
    )

    val feedback = mutableStateListOf(
        Feedback(id = "1", eventName = "Boda de Juan y María", comment = "¡Las fotos quedaron increíbles! Muchas gracias.", userEmail = "usuario1@example.com"),
        Feedback(id = "2", eventName = "Cumpleaños de Ana", comment = "El fotógrafo fue muy amable y profesional.", userEmail = "usuario2@example.com"),
        Feedback(id = "3", eventName = "Boda de Juan y María", comment = "Me hubiese gustado tener más fotos grupales.", userEmail = "usuario3@example.com")
    )
}
