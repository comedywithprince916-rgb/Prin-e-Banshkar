package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      // Modern instantiation of MainViewModel
      val mainViewModel: MainViewModel = viewModel()
      
      val themeSelection by mainViewModel.themeSelection.collectAsState()
      val history by mainViewModel.history.collectAsState()
      val favorites by mainViewModel.favorites.collectAsState()

      MyApplicationTheme(themeSelection = themeSelection) {
        val navController = rememberNavController()

        NavHost(
          navController = navController,
          startDestination = "dashboard",
          modifier = Modifier.fillMaxSize()
        ) {
          composable("dashboard") {
            DashboardScreen(
              viewModel = mainViewModel,
              onNavigate = { route -> navController.navigate(route) },
              favorites = favorites
            )
          }

          composable("history") {
            HistoryScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() },
              historyList = history,
              favoritesList = favorites
            )
          }

          // CORE CALCULATORS
          composable("basic") {
            BasicCalculatorScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("simple_interest") {
            SimpleInterestScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("compound_interest") {
            CompoundInterestScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("emi") {
            EMILoanScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("savings") {
            SavingsInvestmentScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("sip") {
            SIPScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("profit_loss") {
            ProfitLossScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("discount") {
            DiscountScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("gst") {
            GSTScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("currency") {
            CurrencyConverterScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("age") {
            AgeCalculatorScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("height") {
            HeightScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable("bmi") {
            BMIScreen(
              viewModel = mainViewModel,
              onBack = { navController.popBackStack() }
            )
          }

          // POLYMORPHIC UNIT CONVERTER
          composable(
            route = "unit_converter/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
          ) { backStackEntry ->
            val converterType = backStackEntry.arguments?.getString("type") ?: "Length"
            UnitConverterScreen(
              viewModel = mainViewModel,
              converterType = converterType,
              onBack = { navController.popBackStack() }
            )
          }
        }
      }
    }
  }
}
