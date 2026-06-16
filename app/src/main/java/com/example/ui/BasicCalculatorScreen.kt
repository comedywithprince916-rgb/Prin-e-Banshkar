package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicCalculatorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var displayExpression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val onKeyPress: (String) -> Unit = { key ->
        when (key) {
            "C" -> {
                displayExpression = ""
                resultText = ""
            }
            "⌫" -> {
                if (displayExpression.isNotEmpty()) {
                    displayExpression = displayExpression.dropLast(1)
                }
            }
            "=" -> {
                if (displayExpression.isNotEmpty()) {
                    val evaluated = CalculationHelper.evaluateBasicExpression(displayExpression)
                    if (evaluated.isNaN()) {
                        resultText = "Error"
                    } else {
                        // Format double cleanly (hide .0 decimal if integer)
                        val formattedResult = if (evaluated % 1 == 0.0) {
                            evaluated.toLong().toString()
                        } else {
                            String.format("%.6f", evaluated).trimEnd('0').trimEnd('.')
                        }
                        resultText = formattedResult
                        
                        // Save to Room DB history
                        viewModel.saveCalculation(
                            type = "BASIC",
                            title = "Basic Calculator",
                            input = displayExpression,
                            result = formattedResult
                        )
                    }
                }
            }
            "√" -> {
                displayExpression += "sqrt("
            }
            "x^y" -> {
                displayExpression += "^"
            }
            "%" -> {
                // If ending with numeric, apply percentage divide
                displayExpression += "/100"
            }
            else -> {
                displayExpression += key
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basic Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("basic_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (resultText.isNotEmpty() && resultText != "Error") {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Basic Calculation", "$displayExpression = $resultText")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied expression & result!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = resultText.isNotEmpty()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Calculator Screen Display Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = displayExpression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("basic_expr_display")
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("basic_result_display")
                    )
                }
            }

            // Keyboard grid representation
            val buttons = listOf(
                listOf("C", "√", "x^y", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "⌫", "=")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2.5f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { char ->
                            val isOperator = char in listOf("÷", "×", "-", "+", "=", "C", "⌫", "√", "x^y")
                            val isSpecial = char == "=" || char == "C"
                            
                            val btnColor = when {
                                char == "=" -> MaterialTheme.colorScheme.primary
                                isSpecial -> MaterialTheme.colorScheme.errorContainer
                                isOperator -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            
                            val textColor = when {
                                char == "=" -> MaterialTheme.colorScheme.onPrimary
                                isSpecial -> MaterialTheme.colorScheme.onErrorContainer
                                isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(btnColor)
                                    .clickable { onKeyPress(if (char == "×") "*" else if (char == "÷") "/" else char) }
                                    .testTag("basic_key_$char"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
