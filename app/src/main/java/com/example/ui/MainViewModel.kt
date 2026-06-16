package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationDatabase
import com.example.data.CalculationHistory
import com.example.data.CalculationRepository
import com.example.data.ExchangeRateApi
import com.example.calculator.CalculationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculationRepository
    private val exchangeRateApi = ExchangeRateApi.create()

    sealed interface ApiStatus {
        object Idle : ApiStatus
        object Loading : ApiStatus
        data class Success(val lastUpdated: String) : ApiStatus
        data class Error(val message: String) : ApiStatus
    }

    private val _apiStatus = MutableStateFlow<ApiStatus>(ApiStatus.Idle)
    val apiStatus: StateFlow<ApiStatus> = _apiStatus.asStateFlow()

    val history: StateFlow<List<CalculationHistory>>
    val favorites: StateFlow<List<CalculationHistory>>

    // Custom offline currency rates (starts with helper defaults, custom values are editable)
    private val _currencyRates = MutableStateFlow(CalculationHelper.defaultCurrencyRates)
    val currencyRates: StateFlow<Map<String, Double>> = _currencyRates.asStateFlow()

    // Screen Theme choices ("system", "light", "dark")
    private val _themeSelection = MutableStateFlow("system")
    val themeSelection: StateFlow<String> = _themeSelection.asStateFlow()

    init {
        val database = CalculationDatabase.getDatabase(application)
        repository = CalculationRepository(database.calculationDao())
        
        history = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        favorites = repository.favorites.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Fetch feed rates on VM start
        fetchLiveRates()
    }

    // Load Live exchange rates from cloud
    fun fetchLiveRates() {
        viewModelScope.launch {
            _apiStatus.value = ApiStatus.Loading
            try {
                val response = exchangeRateApi.getLatestRates("USD")
                if (response.rates.isNotEmpty()) {
                    val updated = _currencyRates.value.toMutableMap()
                    response.rates.forEach { (code, rate) ->
                        updated[code] = rate
                    }
                    _currencyRates.value = updated
                    val timeString = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    _apiStatus.value = ApiStatus.Success(timeString)
                } else {
                    _apiStatus.value = ApiStatus.Error("API returned empty rates mapping")
                }
            } catch (e: Exception) {
                _apiStatus.value = ApiStatus.Error(e.localizedMessage ?: "Network connection error")
            }
        }
    }

    // In-memory Theme modification
    fun setTheme(theme: String) {
        _themeSelection.value = theme
    }

    // Update customizable offline rates
    fun updateCurrencyRate(currency: String, newRate: Double) {
        if (newRate > 0) {
            val updated = _currencyRates.value.toMutableMap()
            updated[currency] = newRate
            _currencyRates.value = updated
        }
    }

    // Operations for Calculation records
    fun saveCalculation(type: String, title: String, input: String, result: String, isFav: Boolean = false) {
        viewModelScope.launch {
            repository.insertCalculation(
                CalculationHistory(
                    calculatorType = type,
                    title = title,
                    calculationInput = input,
                    result = result,
                    isFavorite = isFav
                )
            )
        }
    }

    fun toggleFavorite(calculation: CalculationHistory) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(calculation.id, !calculation.isFavorite)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
