package com.billcraft.app.domain.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val indianLocale = Locale("en", "IN")
    private val indianFormat: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(indianLocale)
    }

    /**
     * Formats a Double to Indian Rupee format: ₹1,23,456.00
     */
    fun format(amount: Double): String {
        return indianFormat.format(amount)
    }

    /**
     * Formats without currency symbol: 1,23,456.00
     */
    fun formatWithoutSymbol(amount: Double): String {
        val nf = NumberFormat.getNumberInstance(indianLocale)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return nf.format(amount)
    }

    /**
     * Formats compact: ₹1.2L, ₹45K etc.
     */
    fun formatCompact(amount: Double): String {
        return when {
            amount >= 10_00_000 -> "₹${String.format("%.1f", amount / 10_00_000)}Cr"
            amount >= 1_00_000 -> "₹${String.format("%.1f", amount / 1_00_000)}L"
            amount >= 1_000 -> "₹${String.format("%.1f", amount / 1_000)}K"
            else -> format(amount)
        }
    }

    /**
     * Converts a numeric amount to Indian words (for invoice totals).
     * E.g., 12345.50 → "Twelve Thousand Three Hundred Forty-Five and Fifty Paise Only"
     */
    fun toWords(amount: Double): String {
        val rupees = amount.toLong()
        val paise = ((amount - rupees) * 100).toLong()

        val rupeesWords = numberToWords(rupees)
        return if (paise > 0) {
            "$rupeesWords Rupees and ${numberToWords(paise)} Paise Only"
        } else {
            "$rupeesWords Rupees Only"
        }
    }

    private val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    private fun numberToWords(n: Long): String {
        if (n == 0L) return "Zero"
        if (n < 0) return "Minus ${numberToWords(-n)}"

        return buildString {
            var num = n
            if (num >= 1_00_00_00_000L) {
                append("${numberToWords(num / 1_00_00_00_000L)} Arab ")
                num %= 1_00_00_00_000L
            }
            if (num >= 1_00_00_000L) {
                append("${numberToWords(num / 1_00_00_000L)} Crore ")
                num %= 1_00_00_000L
            }
            if (num >= 1_00_000L) {
                append("${numberToWords(num / 1_00_000L)} Lakh ")
                num %= 1_00_000L
            }
            if (num >= 1_000L) {
                append("${numberToWords(num / 1_000L)} Thousand ")
                num %= 1_000L
            }
            if (num >= 100L) {
                append("${ones[(num / 100).toInt()]} Hundred ")
                num %= 100L
            }
            if (num >= 20L) {
                append("${tens[(num / 10).toInt()]} ")
                num %= 10L
            }
            if (num > 0L) {
                append(ones[num.toInt()])
            }
        }.trim()
    }
}
