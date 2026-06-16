package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val calculationDao: CalculationDao) {
    val allHistory: Flow<List<CalculationHistory>> = calculationDao.getAllHistory()
    val favorites: Flow<List<CalculationHistory>> = calculationDao.getFavorites()

    suspend fun insertCalculation(calculation: CalculationHistory): Long {
        return calculationDao.insertCalculation(calculation)
    }

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
        calculationDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun deleteHistoryItem(id: Long) {
        calculationDao.deleteHistoryItem(id)
    }

    suspend fun clearHistory() {
        calculationDao.clearAllHistory()
    }
}
