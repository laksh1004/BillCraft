package com.billcraft.app.domain.util

import java.time.LocalDate

object InvoiceNumberGenerator {

    /**
     * Generates an invoice number in the format INV-YYYY-NNN.
     * The sequence is auto-incremented from the last used number.
     *
     * @param lastSequence The last used sequence number (0 if none exists).
     * @param prefix Override prefix (default "INV").
     * @param year Override year (defaults to current year).
     * @return New invoice number string, e.g. "INV-2024-001"
     */
    fun generate(
        lastSequence: Int,
        prefix: String = "INV",
        year: Int = LocalDate.now().year
    ): String {
        val nextSequence = lastSequence + 1
        return "$prefix-$year-${nextSequence.toString().padStart(3, '0')}"
    }

    /**
     * Generates an estimate number in the format EST-YYYY-NNN.
     */
    fun generateEstimate(lastSequence: Int, year: Int = LocalDate.now().year): String =
        generate(lastSequence, "EST", year)

    /**
     * Generates a receipt number in the format REC-YYYY-NNN.
     */
    fun generateReceipt(lastSequence: Int, year: Int = LocalDate.now().year): String =
        generate(lastSequence, "REC", year)

    /**
     * Extracts the sequence number from an invoice number string.
     * E.g., "INV-2024-042" → 42
     */
    fun extractSequence(invoiceNumber: String): Int {
        return try {
            invoiceNumber.substringAfterLast("-").trimStart('0').toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Returns the year prefix for a given year, e.g. 2024.
     */
    fun buildPrefix(type: String = "INV", year: Int = LocalDate.now().year): String =
        "$type-$year"
}
