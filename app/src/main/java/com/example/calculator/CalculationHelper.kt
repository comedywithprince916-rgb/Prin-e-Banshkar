package com.example.calculator

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.pow
import kotlin.math.sqrt

object CalculationHelper {

    // 1. Basic Calculator expression evaluator
    fun evaluateBasicExpression(expression: String): Double {
        // We'll write a simple, robust parser to handle simple binary operations and expressions safely.
        // For basic calculator, typical inputs are: "3+5", "10*5", "15-3/2"
        // Since we evaluate on keypress or press '=', let's build a simple tokenizer or parser.
        return try {
            val expr = expression.replace(" ", "").replace("×", "*").replace("÷", "/")
            parseExpression(expr)
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun parseExpression(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected expression character: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor // division
                    } else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = str.substring(startPos, this.pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> Math.sin(Math.toRadians(x))
                        "cos" -> Math.cos(Math.toRadians(x))
                        "tan" -> Math.tan(Math.toRadians(x))
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor()) // power

                return x
            }
        }.parse()
    }

    // 2. Simple Interest Calculator
    fun calculateSimpleInterest(principal: Double, rate: Double, timeYears: Double): SimpleInterestResult {
        val interest = (principal * rate * timeYears) / 100.0
        val totalAmount = principal + interest
        return SimpleInterestResult(interest, totalAmount)
    }

    data class SimpleInterestResult(val interest: Double, val totalAmount: Double)

    // 3. Compound Interest Calculator
    fun calculateCompoundInterest(
        principal: Double,
        rate: Double,
        timeYears: Double,
        compoundingFrequency: Int // 1 = Annually, 2 = Semi-Annually, 4 = Quarterly, 12 = Monthly
    ): CompoundInterestResult {
        // Compound interest: A = P(1 + r/n)^(nt)
        val r = rate / 100.0
        val n = compoundingFrequency.toDouble()
        val totalAmount = principal * (1 + r / n).pow(n * timeYears)
        val interest = totalAmount - principal
        return CompoundInterestResult(interest, totalAmount)
    }

    data class CompoundInterestResult(val interest: Double, val totalAmount: Double)

    // 4. EMI Loan Calculator
    fun calculateEMI(principal: Double, annualRate: Double, months: Int): EMIResult {
        if (months <= 0) return EMIResult(0.0, 0.0, 0.0)
        if (annualRate == 0.0) {
            val emi = principal / months
            return EMIResult(emi, principal, 0.0)
        }
        val r = (annualRate / 12.0) / 100.0
        val emi = principal * r * (1 + r).pow(months) / ((1 + r).pow(months) - 1.0)
        val totalPayment = emi * months
        val totalInterest = totalPayment - principal
        return EMIResult(emi, totalPayment, totalInterest)
    }

    data class EMIResult(val emi: Double, val totalPayment: Double, val totalInterest: Double)

    // 5. Savings and Investment Calculator (Future Value with compound and standard monthly deposits)
    fun calculateSavings(
        initialDeposit: Double,
        monthlyContribution: Double,
        annualRate: Double,
        years: Double
    ): SavingsResult {
        val r = (annualRate / 12.0) / 100.0
        val totalMonths = (years * 12.0).toInt()
        var balance = initialDeposit
        var totalInvested = initialDeposit

        if (r == 0.0) {
            balance += monthlyContribution * totalMonths
            totalInvested += monthlyContribution * totalMonths
            return SavingsResult(balance, totalInvested, balance - totalInvested)
        }

        // Formula or Month-by-month compound accumulation:
        for (m in 1..totalMonths) {
            balance = (balance + monthlyContribution) * (1 + r)
            totalInvested += monthlyContribution
        }
        // Balance = deposit * (1+r)^m + contribution * (((1+r)^m -1)/r) * (1+r) if deposits at beginning
        val wealthGained = balance - totalInvested
        return SavingsResult(balance, totalInvested, wealthGained)
    }

    data class SavingsResult(val futureValue: Double, val totalInvested: Double, val wealthGained: Double)

    // 6. SIP Calculator
    fun calculateSIP(monthlyInvestment: Double, expectedReturnAnnual: Double, years: Double): SIPResult {
        val i = (expectedReturnAnnual / 12.0) / 100.0
        val n = (years * 12.0).toInt()
        if (n <= 0) return SIPResult(0.0, 0.0, 0.0)

        val totalInvested = monthlyInvestment * n
        if (i == 0.0) {
            return SIPResult(totalInvested, totalInvested, 0.0)
        }

        // SIP Formula: M = P * [ ( (1 + i)^n - 1 ) / i ] * (1 + i)
        val maturityValue = monthlyInvestment * (((1 + i).pow(n) - 1.0) / i) * (1 + i)
        val wealthGained = maturityValue - totalInvested
        return SIPResult(maturityValue, totalInvested, wealthGained)
    }

    data class SIPResult(val maturityValue: Double, val totalInvested: Double, val wealthGained: Double)

    // 7. Profit and Loss Calculator
    fun calculateProfitAndLoss(costPrice: Double, sellingPrice: Double): ProfitLossResult {
        return if (sellingPrice >= costPrice) {
            val amount = sellingPrice - costPrice
            val percent = if (costPrice > 0) (amount / costPrice) * 100.0 else 0.0
            ProfitLossResult(isProfit = true, amount = amount, percent = percent)
        } else {
            val amount = costPrice - sellingPrice
            val percent = if (costPrice > 0) (amount / costPrice) * 100.0 else 0.0
            ProfitLossResult(isProfit = false, amount = amount, percent = percent)
        }
    }

    data class ProfitLossResult(val isProfit: Boolean, val amount: Double, val percent: Double)

    // 8. Discount Calculator
    fun calculateDiscount(originalPrice: Double, discountPercent: Double, taxPercent: Double): DiscountResult {
        val discountAmount = originalPrice * (discountPercent / 100.0)
        val salesPrice = originalPrice - discountAmount
        val taxAmount = salesPrice * (taxPercent / 100.0)
        val finalPrice = salesPrice + taxAmount
        return DiscountResult(discountAmount, salesPrice, taxAmount, finalPrice)
    }

    data class DiscountResult(
        val discountAmount: Double,
        val priceAfterDiscount: Double,
        val taxAmount: Double,
        val finalPrice: Double
    )

    // 9. GST / Tax Calculator
    fun calculateGST(amount: Double, rate: Double, inclusive: Boolean): GSTResult {
        return if (inclusive) {
            // Price is GST Inclusive
            val netPrice = amount / (1.0 + (rate / 100.0))
            val gstAmount = amount - netPrice
            GSTResult(netPrice = netPrice, gstAmount = gstAmount, totalPrice = amount)
        } else {
            // Price is GST Exclusive
            val gstAmount = amount * (rate / 100.0)
            val totalPrice = amount + gstAmount
            GSTResult(netPrice = amount, gstAmount = gstAmount, totalPrice = totalPrice)
        }
    }

    data class GSTResult(val netPrice: Double, val gstAmount: Double, val totalPrice: Double)

    // 10. Currency Converter Exchange Rates Map (Offline Local Base USD)
    val defaultCurrencyRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "INR" to 83.50,
        "JPY" to 157.00,
        "CAD" to 1.37,
        "AUD" to 1.51,
        "SGD" to 1.35,
        "CNY" to 7.25,
        "CHF" to 0.89,
        "AED" to 3.67,
        "SAR" to 3.75,
        "MXN" to 18.52
    )

    fun convertCurrency(amount: Double, from: String, to: String, rates: Map<String, Double> = defaultCurrencyRates): Double {
        val rateFrom = rates[from] ?: 1.0
        val rateTo = rates[to] ?: 1.0
        val usdAmount = amount / rateFrom
        return usdAmount * rateTo
    }

    // 11. Age Calculator
    fun calculateAge(dob: LocalDate, targetDate: LocalDate = LocalDate.now()): AgeResult {
        if (targetDate.isBefore(dob)) return AgeResult(0, 0, 0, 0, 0, 0, 0, 0, 0)
        val period = Period.between(dob, targetDate)

        // Count months, days, weeks, etc.
        val totalYears = period.years
        val totalMonths = ChronoUnit.MONTHS.between(dob, targetDate)
        val totalDays = ChronoUnit.DAYS.between(dob, targetDate)
        val totalWeeks = totalDays / 7
        val totalHours = totalDays * 24
        val totalMinutes = totalHours * 60

        // Next birthday countdown
        val nextBirthdayYear = if (targetDate.monthValue > dob.monthValue || (targetDate.monthValue == dob.monthValue && targetDate.dayOfMonth >= dob.dayOfMonth)) {
            targetDate.year + 1
        } else {
            targetDate.year
        }
        val nextBirthday = dob.withYear(nextBirthdayYear)
        val daysToNextBirthday = ChronoUnit.DAYS.between(targetDate, nextBirthday)

        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalMonths = totalMonths,
            totalWeeks = totalWeeks,
            totalDays = totalDays,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            daysToNextBirthday = daysToNextBirthday
        )
    }

    data class AgeResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalMonths: Long,
        val totalWeeks: Long,
        val totalDays: Long,
        val totalHours: Long,
        val totalMinutes: Long,
        val daysToNextBirthday: Long
    )

    // 12. Height Calculator (Converts and calculates ideal body weight parameters based on heights)
    fun convertHeightAndMetrics(feet: Int, inches: Int, cm: Double): HeightResult {
        val finalCm = if (cm > 0.0) {
            cm
        } else {
            val totalInches = (feet * 12) + inches
            totalInches * 2.54
        }

        val totalInches = finalCm / 2.54
        val finalFeet = (totalInches / 12).toInt()
        val finalInches = (totalInches % 12).toInt()
        val meters = finalCm / 100.0

        // Devine Formula for Ideal Weight (for people 5 feet or taller)
        // Male: 50.0 + 2.3 kg per inch over 5 feet
        // Female: 45.5 + 2.3 kg per inch over 5 feet
        val inchesOverFiveFeet = totalInches - 60.0
        val idealWeightMaleKg = if (inchesOverFiveFeet > 0) 50.0 + 2.3 * inchesOverFiveFeet else 50.0
        val idealWeightFemaleKg = if (inchesOverFiveFeet > 0) 45.5 + 2.3 * inchesOverFiveFeet else 45.5

        return HeightResult(
            cm = finalCm,
            meters = meters,
            feet = finalFeet,
            inches = finalInches,
            idealWeightMaleKg = idealWeightMaleKg,
            idealWeightFemaleKg = idealWeightFemaleKg
        )
    }

    data class HeightResult(
        val cm: Double,
        val meters: Double,
        val feet: Int,
        val inches: Int,
        val idealWeightMaleKg: Double,
        val idealWeightFemaleKg: Double
    )

    // 13. BMI Calculator
    fun calculateBMI(heightMeters: Double, weightKg: Double): BMIResult {
        if (heightMeters <= 0) return BMIResult(0.0, "Invalid height", 0f)
        val bmi = weightKg / (heightMeters * heightMeters)

        val rating: String
        val colorRangeFraction: Float // 0f to 1f representation for color dials
        when {
            bmi < 18.5 -> {
                rating = "Underweight"
                colorRangeFraction = 0.15f
            }
            bmi < 25.0 -> {
                rating = "Normal Weight"
                colorRangeFraction = 0.4f
            }
            bmi < 30.0 -> {
                rating = "Overweight"
                colorRangeFraction = 0.65f
            }
            else -> {
                rating = "Obese"
                colorRangeFraction = 0.9f
            }
        }

        return BMIResult(bmi, rating, colorRangeFraction)
    }

    data class BMIResult(
        val score: Double,
        val rating: String,
        val progressPercent: Float // Visual feedback
    )

    // UNIT CONVERTERS
    // Sub-tables of ratios relative to a base unit
    // A. Length: base is meter
    private val lengthRatios = mapOf(
        "mm" to 0.001,
        "cm" to 0.01,
        "m" to 1.0,
        "km" to 1000.0,
        "inch" to 0.0254,
        "feet" to 0.3048,
        "mile" to 1609.344
    )

    fun convertLength(value: Double, from: String, to: String): Double {
        val baseVal = value * (lengthRatios[from] ?: 1.0)
        return baseVal / (lengthRatios[to] ?: 1.0)
    }

    // B. Weight: base is gram (g)
    private val weightRatios = mapOf(
        "mg" to 0.001,
        "g" to 1.0,
        "kg" to 1000.0,
        "ton" to 1000000.0,
        "pound" to 453.59237
    )

    fun convertWeight(value: Double, from: String, to: String): Double {
        val baseVal = value * (weightRatios[from] ?: 1.0)
        return baseVal / (weightRatios[to] ?: 1.0)
    }

    // C. Area: base is square meter (m²)
    private val areaRatios = mapOf(
        "sq mm" to 0.000001,
        "sq cm" to 0.0001,
        "sq m" to 1.0,
        "sq km" to 1000000.0,
        "sq inch" to 0.00064516,
        "sq feet" to 0.09290304,
        "sq yard" to 0.83612736,
        "acre" to 4046.85642,
        "hectare" to 10000.0
    )

    fun convertArea(value: Double, from: String, to: String): Double {
        val baseVal = value * (areaRatios[from] ?: 1.0)
        return baseVal / (areaRatios[to] ?: 1.0)
    }

    // D. Volume: base is liter (l)
    private val volumeRatios = mapOf(
        "ml" to 0.001,
        "l" to 1.0,
        "cubic meter" to 1000.0,
        "teaspoon" to 0.00492892,
        "tablespoon" to 0.0147868,
        "cup" to 0.24,
        "fluid ounce" to 0.0295735,
        "pint" to 0.473176,
        "quart" to 0.946353,
        "gallon" to 3.78541
    )

    fun convertVolume(value: Double, from: String, to: String): Double {
        val baseVal = value * (volumeRatios[from] ?: 1.0)
        return baseVal / (volumeRatios[to] ?: 1.0)
    }

    // E. Temperature: special conversions
    fun convertTemperature(value: Double, from: String, to: String): Double {
        val celsius = when (from) {
            "Celsius" -> value
            "Fahrenheit" -> (value - 32.0) * 5.0 / 9.0
            "Kelvin" -> value - 273.15
            else -> value
        }
        return when (to) {
            "Celsius" -> celsius
            "Fahrenheit" -> (celsius * 9.0 / 5.0) + 32.0
            "Kelvin" -> celsius + 273.15
            else -> celsius
        }
    }

    // F. Speed: base is m/s
    private val speedRatios = mapOf(
        "m/s" to 1.0,
        "km/h" to 0.277778,
        "mph" to 0.44704,
        "knots" to 0.514444
    )

    fun convertSpeed(value: Double, from: String, to: String): Double {
        val baseVal = value * (speedRatios[from] ?: 1.0)
        return baseVal / (speedRatios[to] ?: 1.0)
    }

    // G. Time: base is second (s)
    private val timeRatios = mapOf(
        "millisecond" to 0.001,
        "second" to 1.0,
        "minute" to 60.0,
        "hour" to 3600.0,
        "day" to 86400.0,
        "week" to 604800.0,
        "month" to 2629746.0, // Average Gregorian month
        "year" to 31556952.0 // Average Gregorian year
    )

    fun convertTime(value: Double, from: String, to: String): Double {
        val baseVal = value * (timeRatios[from] ?: 1.0)
        return baseVal / (timeRatios[to] ?: 1.0)
    }
}
