package com.billcraft.app.domain.usecase

import com.billcraft.app.data.repository.InvoiceRepository
import com.billcraft.app.domain.model.InvoiceType
import java.time.LocalDate
import javax.inject.Inject

/**
 * Generates sequential, human-readable invoice / estimate / receipt numbers.
 *
 * Format:
 *  - Invoice  → "INV-2425-0001"
 *  - Estimate → "EST-2425-0001"
 *  - Receipt  → "REC-2425-0001"
 *
 * The middle segment encodes the financial year (Apr–Mar), e.g. FY 2024-25 = "2425".
 * The numeric suffix is zero-padded to 4 digits and increments per type per FY.
 */
class InvoiceNumberGenerator @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) {

    suspend fun generate(type: InvoiceType): String {
        val prefix = when (type) {
            InvoiceType.INVOICE -> "INV"
            InvoiceType.ESTIMATE -> "EST"
            InvoiceType.RECEIPT -> "REC"
        }
        val fySegment = financialYearSegment()
        val searchPrefix = "$prefix-$fySegment-"

        val lastNumber = invoiceRepository.generateInvoiceNumber(LocalDate.now().year)
        // generateInvoiceNumber in the repository returns the prefix-based last number;
        // we compute the next sequence ourselves here using a prefix search
        val next = (invoiceRepository.getLastSequenceForPrefix(searchPrefix) ?: 0) + 1
        return "$searchPrefix${next.toString().padStart(4, '0')}"
    }

    private fun financialYearSegment(): String {
        val today = LocalDate.now()
        val startYear = if (today.monthValue >= 4) today.year else today.year - 1
        val endYear = startYear + 1
        return "${startYear.toString().takeLast(2)}${endYear.toString().takeLast(2)}"
    }
}
