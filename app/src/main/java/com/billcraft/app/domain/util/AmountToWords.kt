package com.billcraft.app.domain.util

object AmountToWords {

    private val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    /**
     * Converts a Double amount representing Indian Rupees into a human-readable English string.
     * Example: 12345.50 → "Rupees Twelve Thousand Three Hundred Forty Five and Fifty Paise Only"
     */
    fun convert(amount: Double): String {
        if (amount < 0) return "Invalid Amount"
        if (amount == 0.0) return "Rupees Zero Only"

        val rupees = amount.toLong()
        // Round paise to avoid floating-point imprecision (e.g. 0.1 + 0.2 issues)
        val paise = Math.round((amount - rupees) * 100)

        var result = "Rupees ${convertNumber(rupees)}"
        if (paise > 0) {
            result += " and ${convertNumber(paise)} Paise"
        }
        result += " Only"
        return result
    }

    /**
     * Converts a non-negative Long into its English word representation,
     * following the Indian numbering system (crore, lakh, thousand, hundred).
     */
    private fun convertNumber(n: Long): String {
        if (n == 0L) return "Zero"
        if (n < 0) return "Minus ${convertNumber(-n)}"

        val parts = mutableListOf<String>()

        // Crores: 10^7
        val crore = n / 10_000_000L
        var remainder = n % 10_000_000L

        // Lakhs: 10^5 (within the crore remainder)
        val lakh = remainder / 100_000L
        remainder %= 100_000L

        // Thousands: 10^3
        val thousand = remainder / 1_000L
        remainder %= 1_000L

        // Hundreds
        val hundred = remainder / 100L
        remainder %= 100L

        if (crore > 0) {
            parts.add("${convertBelow100(crore)} Crore")
        }
        if (lakh > 0) {
            parts.add("${convertBelow100(lakh)} Lakh")
        }
        if (thousand > 0) {
            parts.add("${convertBelow100(thousand)} Thousand")
        }
        if (hundred > 0) {
            parts.add("${ones[hundred.toInt()]} Hundred")
        }
        if (remainder > 0) {
            parts.add(convertBelow100(remainder))
        }

        return parts.joinToString(" ")
    }

    /**
     * Converts numbers 1–99 to words. Returns empty string for 0.
     */
    private fun convertBelow100(n: Long): String {
        return when {
            n == 0L -> ""
            n < 20L -> ones[n.toInt()]
            else -> {
                val tenPart = tens[(n / 10).toInt()]
                val onePart = if (n % 10 > 0) " ${ones[(n % 10).toInt()]}" else ""
                "$tenPart$onePart"
            }
        }
    }
}
