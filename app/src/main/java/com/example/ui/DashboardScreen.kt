package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationHistory

data class CalcItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Finance", "Math", "Converter", "Market"
    val icon: ImageVector,
    val colors: List<Color>,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    favorites: List<CalculationHistory>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val themeMode by viewModel.themeSelection.collectAsState()

    // Definition of Calculators and Converters
    val calculators = remember {
        listOf(
            // FINANCE
            CalcItem(
                id = "sip",
                title = "SIP Calculator",
                description = "Maturity & wealth estimates on mutual fund investments",
                category = "Finance",
                icon = Icons.Default.TrendingUp,
                colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)),
                route = "sip"
            ),
            CalcItem(
                id = "emi",
                title = "EMI Loan Calculator",
                description = "Calculate regular home, car, or personal loan EMIs",
                category = "Finance",
                icon = Icons.Default.AccountBalance,
                colors = listOf(Color(0xFF1565C0), Color(0xFF1976D2)),
                route = "emi"
            ),
            CalcItem(
                id = "compound_interest",
                title = "Compound Interest",
                description = "Track growth of investments compounded periodically",
                category = "Finance",
                icon = Icons.Default.ShowChart,
                colors = listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA)),
                route = "compound_interest"
            ),
            CalcItem(
                id = "simple_interest",
                title = "Simple Interest",
                description = "Calculate interest yield on principal values quickly",
                category = "Finance",
                icon = Icons.Default.Timeline,
                colors = listOf(Color(0xFFAD1457), Color(0xFFD81B60)),
                route = "simple_interest"
            ),
            CalcItem(
                id = "savings",
                title = "Savings & Investment",
                description = "Map future wealth with scheduled monthly additives",
                category = "Finance",
                icon = Icons.Default.Savings,
                colors = listOf(Color(0xFF00796B), Color(0xFF009688)),
                route = "savings"
            ),
            CalcItem(
                id = "profit_loss",
                title = "Profit & Loss",
                description = "Instant margins and ratios for retail & business ops",
                category = "Finance",
                icon = Icons.Default.Store,
                colors = listOf(Color(0xFFEF6C00), Color(0xFFF57C00)),
                route = "profit_loss"
            ),
            CalcItem(
                id = "discount",
                title = "Discount Calculator",
                description = "Calculates marked values and active discount reductions",
                category = "Finance",
                icon = Icons.Default.Percent,
                colors = listOf(Color(0xFFC2185B), Color(0xFFE91E63)),
                route = "discount"
            ),
            CalcItem(
                id = "gst",
                title = "GST / Tax Calculator",
                description = "Inclusive or exclusive tax valuations in seconds",
                category = "Finance",
                icon = Icons.Default.ReceiptLong,
                colors = listOf(Color(0xFF283593), Color(0xFF3F51B5)),
                route = "gst"
            ),

            // MATH
            CalcItem(
                id = "basic",
                title = "Basic Calculator",
                description = "Standard math, square roots, percentages & power ops",
                category = "Math",
                icon = Icons.Default.Calculate,
                colors = listOf(Color(0xFF37474F), Color(0xFF455A64)),
                route = "basic"
            ),
            CalcItem(
                id = "age",
                title = "Age Calculator",
                description = "Find exact birthdays, months, and countdown clocks",
                category = "Math",
                icon = Icons.Default.CalendarToday,
                colors = listOf(Color(0xFF00838F), Color(0xFF00ACC1)),
                route = "age"
            ),
            CalcItem(
                id = "height",
                title = "Height Converter",
                description = "Feet, inches, cm conversions and ideal weight metrics",
                category = "Math",
                icon = Icons.Default.SquareFoot,
                colors = listOf(Color(0xFFE65100), Color(0xFFF57C00)),
                route = "height"
            ),
            CalcItem(
                id = "bmi",
                title = "BMI Calculator",
                description = "Diagnose body mass ratios and corresponding weight tier",
                category = "Math",
                icon = Icons.Default.MonitorWeight,
                colors = listOf(Color(0xFF2E7D32), Color(0xFF558B2F)),
                route = "bmi"
            ),

            // MARKETS (CURRENCY)
            CalcItem(
                id = "currency",
                title = "Currency Converter",
                description = "Convert global values with fully customizable offline rates",
                category = "Market",
                icon = Icons.Default.AttachMoney,
                colors = listOf(Color(0xFF0277BD), Color(0xFF039BE5)),
                route = "currency"
            ),

            // UNIT CONVERTERS
            CalcItem(
                id = "len_converter",
                title = "Length Converter",
                description = "Millimeters, cm, meters, kilometers, feet, and miles",
                category = "Converter",
                icon = Icons.Default.Straighten,
                colors = listOf(Color(0xFF43A047), Color(0xFF4CAF50)),
                route = "unit_converter/Length"
            ),
            CalcItem(
                id = "weight_converter",
                title = "Weight Converter",
                description = "Milligrams, grams, kilograms, tons, and pounds",
                category = "Converter",
                icon = Icons.Default.Scale,
                colors = listOf(Color(0xFF8E24AA), Color(0xFFAB47BC)),
                route = "unit_converter/Weight"
            ),
            CalcItem(
                id = "area_converter",
                title = "Area Converter",
                description = "Converts meters squared, square yards, acres, & hectares",
                category = "Converter",
                icon = Icons.Default.Crop,
                colors = listOf(Color(0xFFD81B60), Color(0xFFEC407A)),
                route = "unit_converter/Area"
            ),
            CalcItem(
                id = "volume_converter",
                title = "Volume Converter",
                description = "Litres, milliliters, cups, fluid ounces, and gallons",
                category = "Converter",
                icon = Icons.Default.LocalHospital,
                colors = listOf(Color(0xFF00863F), Color(0xFF2E7D32)),
                route = "unit_converter/Volume"
            ),
            CalcItem(
                id = "temp_converter",
                title = "Temperature Converter",
                description = "Conversions between Celsius, Fahrenheit, and Kelvin",
                category = "Converter",
                icon = Icons.Default.Thermostat,
                colors = listOf(Color(0xFFE53935), Color(0xFFEF5350)),
                route = "unit_converter/Temperature"
            ),
            CalcItem(
                id = "speed_converter",
                title = "Speed Converter",
                description = "Converts m/s, km/h, mph, and nautical knots",
                category = "Converter",
                icon = Icons.Default.Speed,
                colors = listOf(Color(0xFF5E35B1), Color(0xFF7E57C2)),
                route = "unit_converter/Speed"
            ),
            CalcItem(
                id = "time_converter",
                title = "Time Converter",
                description = "Milliseconds, minutes, hours, weeks, and average years",
                category = "Converter",
                icon = Icons.Default.AccessTime,
                colors = listOf(Color(0xFF00ACC1), Color(0xFF26C6DA)),
                route = "unit_converter/Time"
            )
        )
    }

    // Filter calculations
    val filteredCalculators = remember(searchQuery, selectedCategory) {
        calculators.filter {
            val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || it.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "%",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                )
                            )
                        }
                        Column {
                            Text(
                                text = "SmartCalc",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "Finance & Smart Utilities",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate("history") },
                        modifier = Modifier.testTag("dashboard_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculation History"
                        )
                    }
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("dashboard_theme_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                "dark" -> Icons.Default.DarkMode
                                "light" -> Icons.Default.LightMode
                                else -> Icons.Default.SettingsSuggest
                            },
                            contentDescription = "Switch Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar & Filter Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search calculators or units...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Tabs Rows
                val categories = listOf("All", "Finance", "Math", "Market", "Converter")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        val displayLabel = when (cat) {
                            "Math" -> "Smart Math"
                            "Converter" -> "Converters"
                            "Market" -> "Markets"
                            else -> cat
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(displayLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("category_chip_$cat")
                        )
                    }
                }
            }

            // Real Dynamic "Recent Calculation" layout matching standard High Density Design Template
            if (history.isNotEmpty() && searchQuery.isEmpty()) {
                val recentCalc = history.first()
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable {
                                val match = calculators.firstOrNull { it.id == recentCalc.calculatorType.lowercase() }
                                if (match != null) {
                                    onNavigate(match.route)
                                } else if (recentCalc.calculatorType.startsWith("CONVERTER_")) {
                                    val subType = recentCalc.calculatorType.removePrefix("CONVERTER_")
                                    onNavigate("unit_converter/$subType")
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "RECENT CALCULATION",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "HISTORY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recentCalc.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = recentCalc.result,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val matchedItem = calculators.firstOrNull { it.id == recentCalc.calculatorType.lowercase() }
                            val (catBg, catText) = getCategoryColors(recentCalc.calculatorType, matchedItem?.category ?: "Finance")

                            Surface(
                                color = catBg,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = matchedItem?.category ?: "Finance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = catText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = favorites.isNotEmpty() && searchQuery.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Saved Favorites",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(favorites) { fav ->
                            ElevatedCard(
                                onClick = {
                                    val match = calculators.firstOrNull { it.id == fav.calculatorType.lowercase() }
                                    if (match != null) {
                                        onNavigate(match.route)
                                    } else if (fav.calculatorType.startsWith("CONVERTER_")) {
                                        val subType = fav.calculatorType.removePrefix("CONVERTER_")
                                        onNavigate("unit_converter/$subType")
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(110.dp)
                                    .testTag("favorite_card_${fav.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val matchingIcon = calculators.firstOrNull { it.id == fav.calculatorType.lowercase() }?.icon ?: Icons.Default.Star
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = matchingIcon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            tint = Color.Red,
                                            contentDescription = "Pinned",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = fav.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = fav.result,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calculators / Converters Grid
            if (filteredCalculators.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No calculator match found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try searching are for other keywords like 'Interest', 'Weight' or 'BMI'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 145.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(filteredCalculators, key = { it.id }) { item ->
                        DashboardCard(item = item, onClick = { onNavigate(item.route) })
                    }
                }
            }
        }
    }

    // Theme Selector modal
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("App Theme Mode") },
            text = {
                Column {
                    val modes = listOf("system" to "System Default", "light" to "Light Mode", "dark" to "Dark Mode")
                    modes.forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(key)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = themeMode == key,
                                onClick = {
                                    viewModel.setTheme(key)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// Map high density colors based on calculator context and id
fun getCategoryColors(id: String, category: String): Pair<Color, Color> {
    val cleanId = id.lowercase()
    return when {
        cleanId == "emi" -> Color(0xFFD3E3FD) to Color(0xFF001C38)
        cleanId == "sip" -> Color(0xFFE8DEF8) to Color(0xFF21005D)
        cleanId == "currency" -> Color(0xFFC7E7BE) to Color(0xFF072711)
        cleanId == "bmi" -> Color(0xFFE1F5FE) to Color(0xFF01579B)
        cleanId == "age" -> Color(0xFFF3E5F5) to Color(0xFF4A148C)
        cleanId == "discount" -> Color(0xFFE0F2F1) to Color(0xFF004D40)
        cleanId.contains("weight") -> Color(0xFFFEEFC3) to Color(0xFF412B00)
        cleanId.contains("len") || cleanId.contains("height") -> Color(0xFFF8D7DA) to Color(0xFF721C24)
        cleanId.contains("temp") -> Color(0xFFE2F0D9) to Color(0xFF385723)
        cleanId.contains("speed") -> Color(0xFFFFF2CC) to Color(0xFF7F6000)
        category == "Finance" -> Color(0xFFD1E1FF) to Color(0xFF001C38)
        category == "Market" -> Color(0xFFC7E7BE) to Color(0xFF072711)
        category == "Converter" -> Color(0xFFFFF2CC) to Color(0xFF7F6000)
        category == "Math" -> Color(0xFFE8DEF8) to Color(0xFF21005D)
        else -> Color(0xFFE1E2E9) to Color(0xFF44474E)
    }
}

@Composable
fun DashboardCard(item: CalcItem, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick)
            .testTag("calc_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val (badgeBg, badgeText) = getCategoryColors(item.id, item.category)
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(badgeBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = badgeText,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
