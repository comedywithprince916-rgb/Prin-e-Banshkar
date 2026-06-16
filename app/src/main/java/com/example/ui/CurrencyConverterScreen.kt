package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("1") }
    var currencyFrom by remember { mutableStateOf("USD") }
    var currencyTo by remember { mutableStateOf("EUR") }
    var isFavorite by remember { mutableStateOf(false) }

    // Rates state flow from MainViewModel (highly dynamic & custom-modifiable offline!)
    val rates by viewModel.currencyRates.collectAsState()
    val rateKeys = remember(rates) { rates.keys.toList() }

    // Dropdown UI states
    var openDropdownFrom by remember { mutableStateOf(false) }
    var openDropdownTo by remember { mutableStateOf(false) }

    // Dynamic Edit Rates UI state
    var editingCurrency by remember { mutableStateOf("EUR") }
    var editedRateText by remember { mutableStateOf("") }

    var conversionResult by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(rates) {
        editedRateText = rates[editingCurrency]?.toString() ?: ""
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            FinanceHeader(
                title = "Currency Converter",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val apiStatus by viewModel.apiStatus.collectAsState()

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = when (apiStatus) {
                                is MainViewModel.ApiStatus.Success -> Color(0xFFC7E7BE)
                                is MainViewModel.ApiStatus.Error -> Color(0xFFF8D7DA)
                                is MainViewModel.ApiStatus.Loading -> Color(0xFFD3E3FD)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when (apiStatus) {
                                    is MainViewModel.ApiStatus.Loading -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF001C38)
                                        )
                                    }
                                    is MainViewModel.ApiStatus.Success -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Success",
                                            tint = Color(0xFF072711),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    is MainViewModel.ApiStatus.Error -> {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Error",
                                            tint = Color(0xFF721C24),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Live",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Live Exchange Rates API",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = when (val s = apiStatus) {
                                    is MainViewModel.ApiStatus.Loading -> "Updating rate values..."
                                    is MainViewModel.ApiStatus.Success -> "Updated: ${s.lastUpdated}"
                                    is MainViewModel.ApiStatus.Error -> "Offline: ${s.message}"
                                    else -> "Ready for real-time rates"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.fetchLiveRates() },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("refresh_live_rates")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Conversion Inputs Row
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
                        modifier = Modifier.fillMaxWidth().testTag("currency_from_trigger")
                    ) {
                        Text("From: $currencyFrom ▾")
                    }
                    DropdownMenu(
                        expanded = openDropdownFrom,
                        onDismissRequest = { openDropdownFrom = false }
                    ) {
                        rateKeys.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    currencyFrom = code
                                    openDropdownFrom = false
                                }
                            )
                        }
                    }
                }

                // Swap Button
                IconButton(
                    onClick = {
                        val temp = currencyFrom
                        currencyFrom = currencyTo
                        currencyTo = temp
                    },
                    modifier = Modifier.testTag("currency_swap")
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Swap Currencies")
                }

                // Dropdown To
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { openDropdownTo = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("currency_to_trigger")
                    ) {
                        Text("To: $currencyTo ▾")
                    }
                    DropdownMenu(
                        expanded = openDropdownTo,
                        onDismissRequest = { openDropdownTo = false }
                    ) {
                        rateKeys.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    currencyTo = code
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
                label = { Text("Transfer Value Base Sum") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("currency_amount")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amtDouble = amount.toDoubleOrNull() ?: 0.0
                    if (amtDouble <= 0) {
                        Toast.makeText(context, "Input a correct base transfer amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val convertedValue = CalculationHelper.convertCurrency(amtDouble, currencyFrom, currencyTo, rates)
                    conversionResult = convertedValue

                    viewModel.saveCalculation(
                        type = "CURRENCY",
                        title = "Currency Conversion",
                        input = "$amtDouble $currencyFrom to $currencyTo",
                        result = "${String.format("%.4f", convertedValue)} $currencyTo (Rate From: ${rates[currencyFrom]}, Rate To: ${rates[currencyTo]})",
                        isFav = isFavorite
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("currency_convert")
            ) {
                Text("Convert Offline")
            }

            conversionResult?.let { res ->
                val rateText = remember(rates, currencyFrom, currencyTo) {
                    val usdRatio1 = rates[currencyFrom] ?: 1.0
                    val usdRatio2 = rates[currencyTo] ?: 1.0
                    "1 $currencyFrom = ${String.format("%.4f", usdRatio2 / usdRatio1)} $currencyTo"
                }

                ResultDashboard(
                    title = "Conversion Ledger Result",
                    resultItems = listOf(
                        "Exchanged Capital" to "${String.format("%.2f", amount.toDoubleOrNull() ?: 1.0)} $currencyFrom",
                        "Acquired Capital" to "${String.format("%.2f", res)} $currencyTo",
                        "Exchange Rate Matrix" to rateText
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // CUSTOM OFFLINE RATES AUDITOR
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Edit Offline Conversion Matrix (Base 1 USD)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "You have full offline control over conversion weights. Adjust custom values compared to 1.0 USD below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Choose code to edit
                        var codeDropdownOpen by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { codeDropdownOpen = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("$editingCurrency ▾")
                            }
                            DropdownMenu(
                                expanded = codeDropdownOpen,
                                onDismissRequest = { codeDropdownOpen = false }
                            ) {
                                rates.filter { it.key != "USD" }.keys.forEach { code ->
                                    DropdownMenuItem(
                                        text = { Text(code) },
                                        onClick = {
                                            editingCurrency = code
                                            editedRateText = rates[code]?.toString() ?: ""
                                            codeDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        // Rate Input
                        OutlinedTextField(
                            value = editedRateText,
                            onValueChange = { editedRateText = it },
                            placeholder = { Text("Rate e.g. 1.34") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.5f)
                        )

                        // Save Button
                        Button(
                            onClick = {
                                val rateNum = editedRateText.toDoubleOrNull() ?: 0.0
                                if (rateNum <= 0) {
                                    Toast.makeText(context, "Rate weight must be positive double ratio", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.updateCurrencyRate(editingCurrency, rateNum)
                                Toast.makeText(context, "Offline rate for $editingCurrency updated!", Toast.LENGTH_SHORT).show()
                                
                                // Re-trigger conversion calculations instantly if target currencies match
                                val amtDouble = amount.toDoubleOrNull() ?: 0.0
                                if (amtDouble > 0 && (currencyFrom == editingCurrency || currencyTo == editingCurrency)) {
                                    conversionResult = CalculationHelper.convertCurrency(
                                        amtDouble, currencyFrom, currencyTo,
                                        rates.toMutableMap().apply { put(editingCurrency, rateNum) }
                                    )
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.weight(1.2f).testTag("save_rate_weight")
                        ) {
                            Text("Save", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Current State Weights: USD: 1.0000 | EUR: ${String.format("%.4f", rates["EUR"])} | GBP: ${String.format("%.4f", rates["GBP"])} | INR: ${String.format("%.4f", rates["INR"])}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
