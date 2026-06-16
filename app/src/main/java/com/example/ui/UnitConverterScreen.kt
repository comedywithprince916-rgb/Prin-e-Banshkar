package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.calculator.CalculationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: MainViewModel,
    converterType: String, // "Length", "Weight", "Area", "Volume", "Temperature", "Speed", "Time"
    onBack: () -> Unit
) {
    // Determine units based on the converterType
    val units = remember(converterType) {
        when (converterType) {
            "Length" -> listOf("mm", "cm", "m", "km", "inch", "feet", "mile")
            "Weight" -> listOf("mg", "g", "kg", "ton", "pound")
            "Area" -> listOf("sq mm", "sq cm", "sq m", "sq km", "sq inch", "sq feet", "sq yard", "acre", "hectare")
            "Volume" -> listOf("ml", "l", "cubic meter", "teaspoon", "tablespoon", "cup", "fluid ounce", "pint", "quart", "gallon")
            "Temperature" -> listOf("Celsius", "Fahrenheit", "Kelvin")
            "Speed" -> listOf("m/s", "km/h", "mph", "knots")
            "Time" -> listOf("millisecond", "second", "minute", "hour", "day", "week", "month", "year")
            else -> emptyList()
        }
    }

    // Standard dropdown select states
    var unitFrom by remember(converterType) { mutableStateOf(units.firstOrNull() ?: "") }
    var unitTo by remember(converterType) { mutableStateOf(units.getOrNull(1) ?: units.firstOrNull() ?: "") }
    var amount by remember { mutableStateOf("1") }
    var isFavorite by remember { mutableStateOf(false) }

    var openDropdownFrom by remember { mutableStateOf(false) }
    var openDropdownTo by remember { mutableStateOf(false) }

    var conversionResult by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("converter_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "$converterType Converter",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { isFavorite = !isFavorite }, modifier = Modifier.testTag("converter_fav_toggle")) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Pin Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dual Dropdowns selection Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dropdown From
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { openDropdownFrom = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("converter_from_trigger")
                    ) {
                        Text("$unitFrom ▾")
                    }
                    DropdownMenu(
                        expanded = openDropdownFrom,
                        onDismissRequest = { openDropdownFrom = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unitFrom = u
                                    openDropdownFrom = false
                                }
                            )
                        }
                    }
                }

                // Swap Button
                IconButton(
                    onClick = {
                        val temp = unitFrom
                        unitFrom = unitTo
                        unitTo = temp
                    },
                    modifier = Modifier.testTag("converter_swap")
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Swap Units")
                }

                // Dropdown To
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { openDropdownTo = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("converter_to_trigger")
                    ) {
                        Text("$unitTo ▾")
                    }
                    DropdownMenu(
                        expanded = openDropdownTo,
                        onDismissRequest = { openDropdownTo = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unitTo = u
                                    openDropdownTo = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Value quantity to translate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    if (amount.isNotEmpty()) {
                        IconButton(onClick = { amount = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("converter_amount")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amtDouble = amount.toDoubleOrNull() ?: 0.0
                    if (amount.isEmpty() || amtDouble.isNaN()) {
                        Toast.makeText(context, "Input a correct numerical amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val convertedValue = try {
                        when (converterType) {
                            "Length" -> CalculationHelper.convertLength(amtDouble, unitFrom, unitTo)
                            "Weight" -> CalculationHelper.convertWeight(amtDouble, unitFrom, unitTo)
                            "Area" -> CalculationHelper.convertArea(amtDouble, unitFrom, unitTo)
                            "Volume" -> CalculationHelper.convertVolume(amtDouble, unitFrom, unitTo)
                            "Temperature" -> CalculationHelper.convertTemperature(amtDouble, unitFrom, unitTo)
                            "Speed" -> CalculationHelper.convertSpeed(amtDouble, unitFrom, unitTo)
                            "Time" -> CalculationHelper.convertTime(amtDouble, unitFrom, unitTo)
                            else -> Double.NaN
                        }
                    } catch (e: Exception) {
                        Double.NaN
                    }

                    if (convertedValue.isNaN()) {
                        Toast.makeText(context, "Conversion formula error", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    conversionResult = convertedValue

                    // Save record logs
                    viewModel.saveCalculation(
                        type = "CONVERTER_$converterType",
                        title = "$converterType Unit Conversion",
                        input = "$amount $unitFrom",
                        result = "${String.format("%.6f", convertedValue).trimEnd('0').trimEnd('.')} $unitTo",
                        isFav = isFavorite
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("converter_calculate")
            ) {
                Text("Convert Units")
            }

            conversionResult?.let { res ->
                val amtDouble = amount.toDoubleOrNull() ?: 0.0
                val formattedRes = if (res % 1 == 0.0) {
                    res.toLong().toString()
                } else {
                    String.format("%.6f", res).trimEnd('0').trimEnd('.')
                }

                ResultDashboard(
                    title = "Unit Equivalent Output",
                    resultItems = listOf(
                        "Original Measure" to "$amtDouble $unitFrom",
                        "Equivalent Measure" to "$formattedRes $unitTo",
                        "Converter Category" to converterType
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}
