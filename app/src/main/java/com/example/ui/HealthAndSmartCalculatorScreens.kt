package com.example.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculationHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

// 1. AGE CALCULATOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var targetDate by remember { mutableStateOf(LocalDate.now()) }
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.AgeResult?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val datePickerFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    fun showDatePicker(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
        
        val picker = DatePickerDialog(
            context,
            { _: DatePicker, y: Int, m: Int, d: Int ->
                onDateSelected(LocalDate.of(y, m + 1, d))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        )
        picker.show()
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
                title = "Age Calculator",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Birth Date Click Button
            Text("Date of Birth", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showDatePicker(dob ?: LocalDate.of(2000, 1, 1)) { dob = it } },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("dob_picker_trigger")
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dob?.format(datePickerFormatter) ?: "Select Birth Date")
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Date Click Button
            Text("Target Age Date", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showDatePicker(targetDate) { targetDate = it } },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("target_picker_trigger")
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(targetDate.format(datePickerFormatter))
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val dobVal = dob
                        if (dobVal == null) {
                            Toast.makeText(context, "Please select Date of Birth first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (targetDate.isBefore(dobVal)) {
                            Toast.makeText(context, "Target date cannot be before Date of Birth", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = CalculationHelper.calculateAge(dobVal, targetDate)
                        result = res

                        viewModel.saveCalculation(
                            type = "AGE",
                            title = "Age Calculator",
                            input = "Born: ${dobVal.format(datePickerFormatter)}, Target: ${targetDate.format(datePickerFormatter)}",
                            result = "Age: ${res.years} Years, ${res.months} Months, ${res.days} Days",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("age_calculate")
                ) {
                    Text("Calculate Age")
                }

                OutlinedButton(
                    onClick = {
                        dob = null
                        targetDate = LocalDate.now()
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                Spacer(modifier = Modifier.height(20.dp))
                
                // Giant Highlight Main Age Card
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "AGE LIFESPAN STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${res.years} Years, ${res.months} Months, ${res.days} Days",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Next Birthday in: ${res.daysToNextBirthday} Days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                ResultDashboard(
                    title = "Lifespan Metrics Cumulative Summary",
                    resultItems = listOf(
                        "Total lifespan in Months" to "${res.totalMonths} months",
                        "Total lifespan in Weeks" to "${res.totalWeeks} weeks",
                        "Total lifespan in Days" to "${res.totalDays} days",
                        "Total lifespan in Hours" to "${res.totalHours} hours",
                        "Total lifespan in Minutes" to "${res.totalMinutes} minutes"
                    ),
                    onCopy = {},
                    onShare = {}
                )
            }
        }
    }
}

// 2. HEIGHT SCREEN (HEIGHT UNIT AND IDEAL METRIC CONVERTER)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeightScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var checkMetricHeight by remember { mutableStateOf(false) } // false = feet/inches, true = cm
    var feet by remember { mutableStateOf("") }
    var inches by remember { mutableStateOf("") }
    var cmValue by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.HeightResult?>(null) }
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
                title = "Height & Weight Analytics",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = if (checkMetricHeight) 1 else 0) {
                Tab(selected = !checkMetricHeight, onClick = { checkMetricHeight = false }, text = { Text("Feet / Inches") })
                Tab(selected = checkMetricHeight, onClick = { checkMetricHeight = true }, text = { Text("Metric (cm)") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!checkMetricHeight) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = feet,
                        onValueChange = { feet = it },
                        label = { Text("Feet (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("height_feet")
                    )
                    OutlinedTextField(
                        value = inches,
                        onValueChange = { inches = it },
                        label = { Text("Inches (in)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("height_inches")
                    )
                }
            } else {
                OutlinedTextField(
                    value = cmValue,
                    onValueChange = { cmValue = it },
                    label = { Text("Height in Centimeters (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("height_cm")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val fNum = feet.toIntOrNull() ?: 0
                        val iNum = inches.toIntOrNull() ?: 0
                        val cNum = cmValue.toDoubleOrNull() ?: 0.0

                        if (!checkMetricHeight && fNum <= 0 && iNum <= 0) {
                            Toast.makeText(context, "Ensure heights are positive integers", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (checkMetricHeight && cNum <= 0) {
                            Toast.makeText(context, "Metric value must be positive double weight", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val res = CalculationHelper.convertHeightAndMetrics(
                            feet = if (checkMetricHeight) 0 else fNum,
                            inches = if (checkMetricHeight) 0 else iNum,
                            cm = if (checkMetricHeight) cNum else 0.0
                        )
                        result = res

                        val inputLabel = if (checkMetricHeight) "$cNum cm" else "$fNum ft $iNum in"

                        viewModel.saveCalculation(
                            type = "HEIGHT",
                            title = "Height Converter",
                            input = "Stored Height: $inputLabel",
                            result = "Equivalent: ${String.format("%.1f", res.cm)} cm | ${res.feet} ft ${res.inches} in",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("height_calculate")
                ) {
                    Text("Translate Height")
                }

                OutlinedButton(
                    onClick = {
                        feet = ""
                        inches = ""
                        cmValue = ""
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                ResultDashboard(
                    title = "Height Equivalent Dimensions",
                    resultItems = listOf(
                        "Centimeters Equivalent" to "${String.format("%.2f", res.cm)} cm",
                        "Meters Equivalent" to "${String.format("%.3f", res.meters)} meters",
                        "Imperial Equivalent" to "${res.feet} feet, ${res.inches} inches",
                        "Total equivalent Inches" to "${String.format("%.1f", res.cm / 2.54)} inches"
                    ),
                    onCopy = {},
                    onShare = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Medical ideal weight metrics based on height
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Clinical Devine Ideal Weights for height",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Male Ideal Weight")
                            Text(
                                "${String.format("%.1f", res.idealWeightMaleKg)} kg (${String.format("%.1f", res.idealWeightMaleKg * 2.20462)} lbs)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Female Ideal Weight")
                            Text(
                                "${String.format("%.1f", res.idealWeightFemaleKg)} kg (${String.format("%.1f", res.idealWeightFemaleKg * 2.20462)} lbs)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. BMI CALCULATOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMIScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var weightUnitIsKg by remember { mutableStateOf(true) } // true = kg, false = lbs
    var weightString by remember { mutableStateOf("") }
    var heightString by remember { mutableStateOf("") } // height in cm
    var isFavorite by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf<CalculationHelper.BMIResult?>(null) }
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
                title = "BMI Calculator",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = heightString,
                onValueChange = { heightString = it },
                label = { Text("Height in Centimeters (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("bmi_height")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = weightString,
                    onValueChange = { weightString = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("bmi_weight")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(selected = weightUnitIsKg, onClick = { weightUnitIsKg = true })
                    Text("kg")
                    Spacer(modifier = Modifier.width(12.dp))
                    RadioButton(selected = !weightUnitIsKg, onClick = { weightUnitIsKg = false })
                    Text("lbs")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val hcm = heightString.toDoubleOrNull() ?: 0.0
                        val w = weightString.toDoubleOrNull() ?: 0.0

                        if (hcm <= 0 || w <= 0) {
                            Toast.makeText(context, "Input valid double dimensions", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val finalWeightKg = if (weightUnitIsKg) w else w * 0.45359237
                        val heightMeters = hcm / 100.0
                        val res = CalculationHelper.calculateBMI(heightMeters, finalWeightKg)
                        result = res

                        viewModel.saveCalculation(
                            type = "BMI",
                            title = "BMI Calculator",
                            input = "Height: $hcm cm, Weight: $w ${if (weightUnitIsKg) "kg" else "lbs"}",
                            result = "BMI: ${String.format("%.2f", res.score)} (${res.rating})",
                            isFav = isFavorite
                        )
                    },
                    modifier = Modifier.weight(1.5f).testTag("bmi_calculate")
                ) {
                    Text("Calculate BMI")
                }

                OutlinedButton(
                    onClick = {
                        weightString = ""
                        heightString = ""
                        result = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            result?.let { res ->
                Spacer(modifier = Modifier.height(20.dp))

                // Gauge meter drawing
                val ratingColor = when (res.rating) {
                    "Underweight" -> Color(0xFF03A9F4)
                    "Normal Weight" -> Color(0xFF4CAF50)
                    "Overweight" -> Color(0xFFFF9800)
                    else -> Color(0xFFE53935)
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ratingColor.copy(alpha = 0.06f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "BODY MASS INDEX DIAGNOSIS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ratingColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1f", res.score),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = ratingColor
                        )
                        Text(
                            text = res.rating,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ratingColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom canvas color dial gauge meter
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            
                            // Draw 4 segments of BMI
                            val partW = canvasWidth / 4f
                            val h = 10f
                            
                            // Draw Underweight (blue)
                            drawRect(Color(0xFF03A9F4), size = size.copy(width = partW, height = h))
                            // Normal (green)
                            drawRect(
                                Color(0xFF4CAF50),
                                topLeft = Offset(partW, 0f),
                                size = size.copy(width = partW, height = h)
                            )
                            // Overweight (orange)
                            drawRect(
                                Color(0xFFFF9800),
                                topLeft = Offset(partW * 2f, 0f),
                                size = size.copy(width = partW, height = h)
                            )
                            // Obese (red)
                            drawRect(
                                Color(0xFFE53935),
                                topLeft = Offset(partW * 3f, 0f),
                                size = size.copy(width = partW, height = h)
                            )

                            // Pointer
                            val pointerPos = canvasWidth * res.progressPercent
                            drawCircle(
                                color = ratingColor,
                                radius = 7.dp.toPx(),
                                center = Offset(pointerPos, h / 2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Normal BMI range lies securely between 18.5 and 24.9.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
