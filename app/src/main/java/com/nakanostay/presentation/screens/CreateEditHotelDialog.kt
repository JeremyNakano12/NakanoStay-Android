package com.nakanostay.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nakanostay.data.models.Hotel
import com.nakanostay.ui.theme.*

@Composable
fun CreateEditHotelDialog(
    hotel: Hotel? = null,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, address: String, city: String, stars: Int, email: String) -> Unit
) {
    var name by remember { mutableStateOf(hotel?.name ?: "") }
    var address by remember { mutableStateOf(hotel?.address ?: "") }
    var city by remember { mutableStateOf(hotel?.city ?: "") }
    var stars by remember { mutableStateOf(hotel?.stars?.toString() ?: "") }
    var email by remember { mutableStateOf(hotel?.email ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var starsError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val isEditing = hotel != null
    val title = if (isEditing) "Editar Hotel" else "Crear Nuevo Hotel"

    fun validateForm(): Boolean {
        var isValid = true

        nameError = when {
            name.isBlank() -> {
                isValid = false
                "El nombre es requerido"
            }
            name.length < 2 -> {
                isValid = false
                "El nombre debe tener al menos 2 caracteres"
            }
            name.length > 100 -> {
                isValid = false
                "El nombre no puede exceder 100 caracteres"
            }
            else -> null
        }

        addressError = when {
            address.isBlank() -> {
                isValid = false
                "La dirección es requerida"
            }
            address.length < 5 -> {
                isValid = false
                "La dirección debe tener al menos 5 caracteres"
            }
            address.length > 200 -> {
                isValid = false
                "La dirección no puede exceder 200 caracteres"
            }
            else -> null
        }

        cityError = when {
            city.isBlank() -> {
                isValid = false
                "La ciudad es requerida"
            }
            city.length < 2 -> {
                isValid = false
                "La ciudad debe tener al menos 2 caracteres"
            }
            city.length > 50 -> {
                isValid = false
                "La ciudad no puede exceder 50 caracteres"
            }
            else -> null
        }

        starsError = when {
            stars.isBlank() -> {
                isValid = false
                "Las estrellas son requeridas"
            }
            stars.toIntOrNull() == null -> {
                isValid = false
                "Debe ser un número válido"
            }
            stars.toIntOrNull()!! < 1 -> {
                isValid = false
                "Mínimo 1 estrella"
            }
            stars.toIntOrNull()!! > 5 -> {
                isValid = false
                "Máximo 5 estrellas"
            }
            else -> null
        }

        emailError = when {
            email.isBlank() -> {
                isValid = false
                "El email es requerido"
            }
            !email.contains("@") || !email.contains(".") -> {
                isValid = false
                "Formato de email inválido"
            }
            email.length > 100 -> {
                isValid = false
                "El email no puede exceder 100 caracteres"
            }
            else -> null
        }

        return isValid
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    if (!isLoading) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Nombre del Hotel *") },
                    placeholder = { Text("Hotel Plaza Mayor") },
                    leadingIcon = {
                        Icon(Icons.Default.Hotel, contentDescription = null)
                    },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        addressError = null
                    },
                    label = { Text("Dirección *") },
                    placeholder = { Text("Av. Principal 123") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    isError = addressError != null,
                    supportingText = addressError?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        city = it
                        cityError = null
                    },
                    label = { Text("Ciudad *") },
                    placeholder = { Text("Quito") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationCity, contentDescription = null)
                    },
                    isError = cityError != null,
                    supportingText = cityError?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = stars,
                    onValueChange = { newValue ->
                        // Only allow digits
                        if (newValue.all { it.isDigit() } && newValue.length <= 1) {
                            stars = newValue
                            starsError = null
                        }
                    },
                    label = { Text("Estrellas (1-5) *") },
                    placeholder = { Text("5") },
                    leadingIcon = {
                        Icon(Icons.Default.Star, contentDescription = null)
                    },
                    isError = starsError != null,
                    supportingText = starsError?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text("Email de Contacto *") },
                    placeholder = { Text("info@hotelplaza.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (validateForm()) {
                                onSave(
                                    name.trim(),
                                    address.trim(),
                                    city.trim(),
                                    stars.toInt(),
                                    email.trim()
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isEditing) "Actualizar" else "Crear")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "* Campos requeridos",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceLight.copy(alpha = 0.6f)
                )
            }
        }
    }
}