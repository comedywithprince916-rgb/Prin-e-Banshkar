package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val calculatorType: String, // e.g. "BASIC", "EMI", "SIP", "BMI", "DISCOUNT", etc.
    val title: String,          // e.g. "EMI Loan Calculator"
    val calculationInput: String, // e.g. "Principal: $10,000, Interest: 8%, Tenure: 5 Years"
    val result: String,          // e.g. "EMI: $202.76 / Month, Total Interest: $2,165.84"
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) : Serializable
