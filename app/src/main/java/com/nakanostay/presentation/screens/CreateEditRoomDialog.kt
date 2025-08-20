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
import com.nakanostay.data.models.Room
import com.nakanostay.ui.theme.*
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditRoomDialog(
    room: Room? = null,
    hotels: List<Hotel>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (hotelId: Long, roomNumber: String, roomType: String?, pricePerNight: BigDecimal, isAvailable: Boolean) -> Unit
) {
    val roomHotel = room?.let { r -> hotels.find { it.id == r.hotelId } }

    var selectedHotel by remember { mutableStateOf<Hotel?>(roomHotel) }
    var roomNumber by remember { mutableStateOf(room?.roomNumber ?: "") }
    var roomType by remember { mutableStateOf(room?.roomType ?: "") }
    var pricePerNight by remember { mutableStateOf(room?.pricePerNight?.toString() ?: "") }
    var isAvailable by remember { mutableStateOf(room?.isAvailable ?: true) }

    var hotelError by remember { mutableStateOf<String?>(null) }
    var roomNumberError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    var showHotelDropdown by remember { mutableStateOf(false) }

    val isEditing = room != null
    val title = if (isEditing) "Editar Habitación" else "Crear Nueva Habitación"

    fun validateForm(): Boolean {
        var isValid = true

        hotelError = if (selectedHotel == null) {
            isValid = false
            "Debe seleccionar un hotel"
        } else null

        roomNumberError = when {
            roomNumber.isBlank() -> {
                isValid = false
                "El número de habitación es requerido"
            }
            roomNumber.length > 10 -> {
                isValid = false
                "El número no puede exceder 10 caracteres"
            }
            else -> null
        }

        priceError = when {
            pricePerNight.isBlank() -> {
                isValid = false
                "El precio es requerido"
            }
            pricePerNight.toBigDecimalOrNull() == null -> {
                isValid = false
                "Debe ser un precio válido"
            }
            pricePerNight.toBigDecimalOrNull()!! <= BigDecimal.ZERO -> {
                isValid = false
                "El precio debe ser mayor a 0"
            }
            pricePerNight.toBigDecimalOrNull()!! > BigDecimal("99999.99") -> {
                isValid = false
                "El precio no puede exceder $99,999.99"
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

                ExposedDropdownMenuBox(
                    expanded = showHotelDropdown,
                    onExpandedChange = { showHotelDropdown = !isLoading && it }
                ) {
                    OutlinedTextField(
                        value = selectedHotel?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Hotel *") },
                        placeholder = { Text("Seleccionar hotel") },
                        leadingIcon = {
                            Icon(Icons.Default.Hotel, contentDescription = null)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = showHotelDropdown
                            )
                        },
                        isError = hotelError != null,
                        supportingText = hotelError?.let { { Text(it, color = ErrorRed) } },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = showHotelDropdown,
                        onDismissRequest = { showHotelDropdown = false }
                    ) {
                        hotels.forEach { hotel ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(hotel.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            hotel.city ?: "Sin ciudad",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceLight.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedHotel = hotel
                                    showHotelDropdown = false
                                    hotelError = null
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Hotel,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = {
                        roomNumber = it
                        roomNumberError = null
                    },
                    label = { Text("Número de Habitación *") },
                    placeholder = { Text("101") },
                    leadingIcon = {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null)
                    },
                    isError = roomNumberError != null,
                    supportingText = roomNumberError?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = roomType,
                    onValueChange = { roomType = it },
                    label = { Text("Tipo de Habitación") },
                    placeholder = { Text("Sencilla, Doble, Suite, etc.") },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pricePerNight,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() || it == '.' } && newValue.count { it == '.' } <= 1) {
                            pricePerNight = newValue
                            priceError = null
                        }
                    },
                    label = { Text("Precio por Noche (USD) *") },
                    placeholder = { Text("50.00") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                    },
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAvailable) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.Block,
                                contentDescription = null,
                                tint = if (isAvailable) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Disponibilidad",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isAvailable) "Habitación disponible para reservas" else "Habitación no disponible",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceLight.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it },
                            enabled = !isLoading,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SuccessGreen,
                                checkedTrackColor = SuccessGreen.copy(alpha = 0.5f),
                                uncheckedThumbColor = ErrorRed,
                                uncheckedTrackColor = ErrorRed.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

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
                                    selectedHotel!!.id!!,
                                    roomNumber.trim(),
                                    roomType.trim().ifBlank { null },
                                    pricePerNight.toBigDecimal(),
                                    isAvailable
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