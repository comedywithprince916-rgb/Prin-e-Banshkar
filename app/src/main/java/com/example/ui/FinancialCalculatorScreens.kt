package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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

// HELPER: Core UI form elements for consistency
@Composable
fun FinanceHeader(
    title: String,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("finance_back")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("finance_fav_toggle")) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Pin Favorite",
                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ResultDashboard(
    title: String,
    resultItems: List<Pair<String, String>>,
    onCopy: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    val defaultCopy = {
        val text = "$title:\n" + resultItems.joinToString("\n") { "${it.first}: ${it.second}" }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(title, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$title copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    val defaultShare = {
        val text = "🧮 $title\n\n" + resultItems.joinToString("\n") { "▫️ ${it.first}: ${it.second}" } + "\n\nCalculated with Smart Calculator"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Results"))
    }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .testTag("finance_result_dashboard")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            resultItems.forEach { (label, value) ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onCopy ?: defaultCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
                IconButton(onClick = onShare ?: defaultShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    }
}

// 1. SIMPLE INTEREST SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleInterestScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var timeUnit by remember { mutableStateOf("Years") } // "Years" or "Months"
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.SimpleInterestResult?>(null) }
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
            FinanceHeader(
                title = "Simple Interest",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = principal,
                onValueChange = { principal = it },
                label = { Text("Principal Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("si_principal")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Interest Rate (% per annum)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("si_rate")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Duration") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("si_time")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(selected = timeUnit == "Years", onClick = { timeUnit = "Years" })
                    Text("Years")
                    Spacer(modifier = Modifier.width(6.dp))
                    RadioButton(selected = timeUnit == "Months", onClick = { timeUnit = "Months" })
                    Text("Months")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val p = principal.toDoubleOrNull() ?: 0.0
                        val r = rate.toDoubleOrNull() ?: 0.0
                        val t = time.toDoubleOrNull() ?: 0.0
                        if (p <= 0 || r <= 0 || t <= 0) {
                            Toast.makeText(context, "Please configure valid numeric inputs", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val timeInYears = if (timeUnit == "Months") t / 12.0 else t
                        val res = CalculationHelper.calculateSimpleInterest(p, r, timeInYears)
                        result = res

                        // Save Log to DB
                        viewModel.saveCalculation(
                            type = "SIMPLE_INTEREST",
                            title = "Simple Interest Calculator",
                            input = "Principal: $$p, Rate: $r%, Tenure: $t $timeUnit",
                            result = "Interest earned: $${String.format("%.2f", res.interest)}, Total Accumulation: $${String.format("%.2f", res.totalAmount)}",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("si_calculate")
                ) {
                    Text("Calculate")
                }

                OutlinedButton(
                    onClick = {
                        principal = ""
                        rate = ""
                        time = ""
                        timeUnit = "Years"
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                ResultDashboard(
                    title = "Interest Yield Results",
                    resultItems = listOf(
                        "Principal Amount" to "$${String.format("%.2f", principal.toDoubleOrNull() ?: 0.0)}",
                        "Secured Interest" to "$${String.format("%.2f", res.interest)}",
                        "Total Cumulative Value" to "$${String.format("%.2f", res.totalAmount)}"
                    )
                )
            }
        }
    }
}

// 2. COMPOUND INTEREST SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundInterestScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(12) } // 12 = monthly, 4 = quarterly, 1 = annually
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.CompoundInterestResult?>(null) }
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
            FinanceHeader(
                title = "Compound Interest",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = principal,
                onValueChange = { principal = it },
                label = { Text("Initial Investment ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("ci_principal")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Annual Growth Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("ci_rate")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Period Term (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("ci_time")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Compounding Intervals", style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(12 to "Monthly", 4 to "Quarterly", 1 to "Annually").forEach { (freq, name) ->
                    val selected = frequency == freq
                    ElevatedAssistChip(
                        onClick = { frequency = freq },
                        label = { Text(name) },
                        colors = if (selected) {
                            AssistChipDefaults.elevatedAssistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            AssistChipDefaults.elevatedAssistChipColors()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val p = principal.toDoubleOrNull() ?: 0.0
                        val r = rate.toDoubleOrNull() ?: 0.0
                        val t = time.toDoubleOrNull() ?: 0.0
                        if (p <= 0 || r <= 0 || t <= 0) {
                            Toast.makeText(context, "Please enter correct positive variables", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = CalculationHelper.calculateCompoundInterest(p, r, t, frequency)
                        result = res

                        val compName = when (frequency) {
                            12 -> "Monthly"
                            4 -> "Quarterly"
                            else -> "Annually"
                        }

                        viewModel.saveCalculation(
                            type = "COMPOUND_INTEREST",
                            title = "Compound Interest Calculator",
                            input = "Sum: $$p, Rate: $r%, Years: $t, compounded: $compName",
                            result = "Interest accumulated: $${String.format("%.2f", res.interest)}, Total Sum: $${String.format("%.2f", res.totalAmount)}",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("ci_calculate")
                ) {
                    Text("Calculate")
                }

                OutlinedButton(
                    onClick = {
                        principal = ""
                        rate = ""
                        time = ""
                        frequency = 12
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                val freqLabel = when (frequency) {
                    12 -> "Monthly compounding"
                    4 -> "Quarterly compounding"
                    else -> "Annual compounding"
                }

                ResultDashboard(
                    title = "Compound Outcomes",
                    resultItems = listOf(
                        "Initial Saving" to "$${String.format("%.2f", principal.toDoubleOrNull() ?: 0.0)}",
                        "Compounding Cycle" to freqLabel,
                        "Accrued Compound Yield" to "$${String.format("%.2f", res.interest)}",
                        "Total Accumulated Sum" to "$${String.format("%.2f", res.totalAmount)}"
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}

// 3. EMI LOAN CALCULATOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMILoanScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var tenureUnit by remember { mutableStateOf("Years") } // "Years" or "Months"
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.EMIResult?>(null) }
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
            FinanceHeader(
                title = "EMI Loan Calculator",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = principal,
                onValueChange = { principal = it },
                label = { Text("Loan Principal Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("emi_principal")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Annual Interest Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("emi_rate")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tenure,
                    onValueChange = { tenure = it },
                    label = { Text("Tenure") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("emi_tenure")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(selected = tenureUnit == "Years", onClick = { tenureUnit = "Years" })
                    Text("Years")
                    Spacer(modifier = Modifier.width(6.dp))
                    RadioButton(selected = tenureUnit == "Months", onClick = { tenureUnit = "Months" })
                    Text("Months")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val p = principal.toDoubleOrNull() ?: 0.0
                        val r = rate.toDoubleOrNull() ?: 0.0
                        val t = tenure.toDoubleOrNull() ?: 0.0
                        if (p <= 0 || r < 0 || t <= 0) {
                            Toast.makeText(context, "Please write correct positive numeric metrics", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val months = if (tenureUnit == "Years") (t * 12).toInt() else t.toInt()
                        val res = CalculationHelper.calculateEMI(p, r, months)
                        result = res

                        viewModel.saveCalculation(
                            type = "EMI",
                            title = "EMI Loan Calculator",
                            input = "Loan Amount: $$p, Interest: $r%, Period: $t $tenureUnit",
                            result = "Monthly EMI: $${String.format("%.2f", res.emi)}, Total Payment: $${String.format("%.2f", res.totalPayment)}",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("emi_calculate")
                ) {
                    Text("Calculate")
                }

                OutlinedButton(
                    onClick = {
                        principal = ""
                        rate = ""
                        tenure = ""
                        tenureUnit = "Years"
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                ResultDashboard(
                    title = "Monthly Loan Burden",
                    resultItems = listOf(
                        "Monthly Payment (EMI)" to "$${String.format("%.2f", res.emi)}",
                        "Principal Borrowed" to "$${String.format("%.2f", principal.toDoubleOrNull() ?: 0.0)}",
                        "Total Interest Charged" to "$${String.format("%.2f", res.totalInterest)}",
                        "Total Accumulated Cost" to "$${String.format("%.2f", res.totalPayment)}"
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}

// 4. SAVINGS & INVESTMENT FUTURE VALUE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsInvestmentScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var initialSum by remember { mutableStateOf("") }
    var regularSavings by remember { mutableStateOf("") }
    var growthRate by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.SavingsResult?>(null) }
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
            FinanceHeader(
                title = "Savings & Investments",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = initialSum,
                onValueChange = { initialSum = it },
                label = { Text("Initial Sum / Principal ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("savings_initial")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = regularSavings,
                onValueChange = { regularSavings = it },
                label = { Text("Regular Monthly Additions ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("savings_regular")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = growthRate,
                onValueChange = { growthRate = it },
                label = { Text("Expected Yield % (Annualized)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("savings_rate")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = years,
                onValueChange = { years = it },
                label = { Text("Investment Horizon (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("savings_years")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val initVal = initialSum.toDoubleOrNull() ?: 0.0
                        val regVal = regularSavings.toDoubleOrNull() ?: 0.0
                        val rateVal = growthRate.toDoubleOrNull() ?: 0.0
                        val yearsVal = years.toDoubleOrNull() ?: 0.0
                        if (yearsVal <= 0 || rateVal < 0) {
                            Toast.makeText(context, "Please enter valid numeric variables", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = CalculationHelper.calculateSavings(initVal, regVal, rateVal, yearsVal)
                        result = res

                        viewModel.saveCalculation(
                            type = "SAVINGS",
                            title = "Savings and Investment Calculator",
                            input = "Initial: $$initVal, Monthly: $$regVal, Yield: $rateVal%, Term: $yearsVal Years",
                            result = "Future value: $${String.format("%.2f", res.futureValue)}, Invested: $${String.format("%.2f", res.totalInvested)}",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("savings_calculate")
                ) {
                    Text("Calculate Future Wealth")
                }

                OutlinedButton(
                    onClick = {
                        initialSum = ""
                        regularSavings = ""
                        growthRate = ""
                        years = ""
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                ResultDashboard(
                    title = "Future Capital Estimate",
                    resultItems = listOf(
                        "Future Capital Value" to "$${String.format("%.2f", res.futureValue)}",
                        "Your Total Out-of-pocket" to "$${String.format("%.2f", res.totalInvested)}",
                        "Net Interest Acquired" to "$${String.format("%.2f", res.wealthGained)}"
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}

// 5. SIP CALCULATOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SIPScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var monthlyInvestment by remember { mutableStateOf("") }
    var expectedReturn by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.SIPResult?>(null) }
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
            FinanceHeader(
                title = "SIP Calculator",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = monthlyInvestment,
                onValueChange = { monthlyInvestment = it },
                label = { Text("Monthly SIP Contribution ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("sip_monthly")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = expectedReturn,
                onValueChange = { expectedReturn = it },
                label = { Text("Expected Annual Return (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("sip_rate")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = years,
                onValueChange = { years = it },
                label = { Text("Time Period (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("sip_years")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val mInv = monthlyInvestment.toDoubleOrNull() ?: 0.0
                        val rate = expectedReturn.toDoubleOrNull() ?: 0.0
                        val time = years.toDoubleOrNull() ?: 0.0
                        if (mInv <= 0 || rate < 0 || time <= 0) {
                            Toast.makeText(context, "Please input clean positive values", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = CalculationHelper.calculateSIP(mInv, rate, time)
                        result = res

                        viewModel.saveCalculation(
                            type = "SIP",
                            title = "SIP Calculator",
                            input = "SIP: $$mInv/mo, Rate: $rate%, Period: $time Years",
                            result = "Maturity: $${String.format("%.2f", res.maturityValue)}, Gained: $${String.format("%.2f", res.wealthGained)}",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("sip_calculate")
                ) {
                    Text("Calculate Yield")
                }

                OutlinedButton(
                    onClick = {
                        monthlyInvestment = ""
                        expectedReturn = ""
                        years = ""
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                ResultDashboard(
                    title = "SIP Investment Breakdown",
                    resultItems = listOf(
                        "Estimated Maturity Value" to "$${String.format("%.2f", res.maturityValue)}",
                        "Total Cash Capitalized" to "$${String.format("%.2f", res.totalInvested)}",
                        "Capital Returns Gained" to "$${String.format("%.2f", res.wealthGained)}"
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}

// 6. PROFIT & LOSS CALCULATOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitLossScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.ProfitLossResult?>(null) }
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
            FinanceHeader(
                title = "Profit & Loss Margin",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                label = { Text("Base Cost Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("pl_cost")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = sellingPrice,
                onValueChange = { sellingPrice = it },
                label = { Text("Selling Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("pl_selling")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val cp = costPrice.toDoubleOrNull() ?: 0.0
                        val sp = sellingPrice.toDoubleOrNull() ?: 0.0
                        if (cp <= 0 || sp < 0) {
                            Toast.makeText(context, "Please enter correct pricing inputs", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = CalculationHelper.calculateProfitAndLoss(cp, sp)
                        result = res

                        val tag = if (res.isProfit) "PROFIT" else "LOSS"

                        viewModel.saveCalculation(
                            type = "PROFIT_LOSS",
                            title = "Profit & Loss Calculator",
                            input = "Cost: $$cp, Retail: $$sp",
                            result = "Result: $tag, Diff Amount: $${String.format("%.2f", res.amount)} (${String.format("%.2f", res.percent)}%)",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("pl_calculate")
                ) {
                    Text("Compute Margins")
                }

                OutlinedButton(
                    onClick = {
                        costPrice = ""
                        sellingPrice = ""
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                val outcomeType = if (res.isProfit) "Profit Secured" else "Net Loss Incurred"
                val outcomeColor = if (res.isProfit) Color(0xFF2E7D32) else Color(0xFFC62828)

                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = outcomeColor.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Financial Margin",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = outcomeColor
                            )
                            Box(
                                modifier = Modifier
                                    .background(outcomeColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (res.isProfit) "PROFIT" else "LOSS",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(outcomeType, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "$${String.format("%.2f", res.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = outcomeColor
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Margin Ratio %", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${String.format("%.2f", res.percent)}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = outcomeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. DISCOUNT CALCULATOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var rawPrice by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var taxPercent by remember { mutableStateOf("0") }
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.DiscountResult?>(null) }
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
            FinanceHeader(
                title = "Discount Calculator",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = rawPrice,
                onValueChange = { rawPrice = it },
                label = { Text("Original Marked Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("disc_price")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = discountPercent,
                onValueChange = { discountPercent = it },
                label = { Text("Discount Deductions (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("disc_percent")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = taxPercent,
                onValueChange = { taxPercent = it },
                label = { Text("Optional Tax Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("disc_tax")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val rp = rawPrice.toDoubleOrNull() ?: 0.0
                        val dp = discountPercent.toDoubleOrNull() ?: 0.0
                        val tp = taxPercent.toDoubleOrNull() ?: 0.0
                        if (rp <= 0 || dp < 0 || dp > 100 || tp < 0) {
                            Toast.makeText(context, "Configure correct markdown ratios (discount 0-100%)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = CalculationHelper.calculateDiscount(rp, dp, tp)
                        result = res

                        viewModel.saveCalculation(
                            type = "DISCOUNT",
                            title = "Discount Calculator",
                            input = "Price: $$rp, Discount: $dp%, Sales Tax: $tp%",
                            result = "Discounted final cost: $${String.format("%.2f", res.finalPrice)}, You cut budget by: $${String.format("%.2f", res.discountAmount)}",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("disc_calculate")
                ) {
                    Text("Apply Discounts")
                }

                OutlinedButton(
                    onClick = {
                        rawPrice = ""
                        discountPercent = ""
                        taxPercent = "0"
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                ResultDashboard(
                    title = "Shopping Reductions",
                    resultItems = listOf(
                        "Tag Sticker Price" to "$${String.format("%.2f", rawPrice.toDoubleOrNull() ?: 0.0)}",
                        "Cash Budget Saved" to "$${String.format("%.2f", res.discountAmount)}",
                        "Pre-Tax Reduced Cost" to "$${String.format("%.2f", res.priceAfterDiscount)}",
                        "Tax Added Amount" to "$${String.format("%.2f", res.taxAmount)}",
                        "Final Retail Price" to "$${String.format("%.2f", res.finalPrice)}"
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}

// 8. GST / TAX CALCULATOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GSTScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var baseAmount by remember { mutableStateOf("") }
    var gstRate by remember { mutableStateOf("") }
    var inclusiveGst by remember { mutableStateOf(false) } // false = exclusive (additive), true = inclusive
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.GSTResult?>(null) }
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
            FinanceHeader(
                title = "GST / Tax Calculator",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = baseAmount,
                onValueChange = { baseAmount = it },
                label = { Text("Corporate Base Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("gst_amount")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gstRate,
                onValueChange = { gstRate = it },
                label = { Text("Tax Rate / VAT / GST (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("gst_rate")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("GST Condition Rule", style = MaterialTheme.typography.titleSmall)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(selected = !inclusiveGst, onClick = { inclusiveGst = false }, modifier = Modifier.testTag("gst_exclusive_radio"))
                Text("Add GST (Exclusive)")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = inclusiveGst, onClick = { inclusiveGst = true }, modifier = Modifier.testTag("gst_inclusive_radio"))
                Text("Remove GST (Inclusive)")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val base = baseAmount.toDoubleOrNull() ?: 0.0
                        val rate = gstRate.toDoubleOrNull() ?: 18.0
                        if (base <= 0 || rate < 0) {
                            Toast.makeText(context, "Provide correct standard double values", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = CalculationHelper.calculateGST(base, rate, inclusiveGst)
                        result = res

                        val tag = if (inclusiveGst) "Inclusive" else "Exclusive"

                        viewModel.saveCalculation(
                            type = "GST",
                            title = "GST / Tax Calculator",
                            input = "Base: $$base, Rate: $rate%, Formula: $tag",
                            result = "Net cost: $${String.format("%.2f", res.netPrice)}, Tax Charge: $${String.format("%.2f", res.gstAmount)}, Total amount: $${String.format("%.2f", res.totalPrice)}",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("gst_calculate")
                ) {
                    Text("Apply Tax Formula")
                }

                OutlinedButton(
                    onClick = {
                        baseAmount = ""
                        gstRate = ""
                        inclusiveGst = false
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                val formulaLabel = if (inclusiveGst) "GST Inclusive deductions" else "GST Exclusive additions"
                ResultDashboard(
                    title = "Tax Audit Calculations",
                    resultItems = listOf(
                        "Net Taxable Cost" to "$${String.format("%.2f", res.netPrice)}",
                        "Tax Collected (GST)" to "$${String.format("%.2f", res.gstAmount)}",
                        "Final Gross Amount" to "$${String.format("%.2f", res.totalPrice)}",
                        "Calculated Paradigm" to formulaLabel
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}
