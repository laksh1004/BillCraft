package com.billcraft.app.domain.usecase

import com.billcraft.app.domain.model.GSTLineItem
import com.billcraft.app.domain.model.GSTSummary
import com.billcraft.app.domain.model.LineItem
import javax.inject.Inject

/**
 * Pure use-case that computes GST totals for a list of [LineItem]s.
 *
 * Rules:
 * - Intra-state supply → CGST + SGST each at half the GST rate.
 * - Inter-state supply → IGST at the full GST rate.
 * - Line item taxable amount = (qty × price) × (1 − discount% / 100)
 * - Items are grouped by GST rate to produce per-slab breakdown rows.
 */
class CalculateInvoiceTotalsUseCase @Inject constructor() {

    /**
     * @param lineItems   The list of line items on the invoice.
     * @param isInterState True if the supply crosses state lines (IGST applicable).
     */
    operator fun invoke(lineItems: List<LineItem>, isInterState: Boolean): GSTSummary {

        // 1. Group items by GST rate
        val grouped: Map<Double, List<LineItem>> = lineItems.groupBy { it.gstRate }

        var overallSubtotal = 0.0
        var totalCGST = 0.0
        var totalSGST = 0.0
        var totalIGST = 0.0

        // 2. Build per-rate breakdown
        val gstBreakdown = grouped.map { (rate, items) ->
            // Taxable amount = sum of (qty × price × (1 − discount/100)) for each item
            val taxableAmount = items.sumOf { item ->
                val base = item.quantity * item.pricePerUnit
                val discount = base * item.discountPercent / 100.0
                base - discount
            }

            overallSubtotal += taxableAmount

            val (cgst, sgst, igst) = if (isInterState) {
                // IGST = full rate applied to taxable amount
                val igstAmt = taxableAmount * rate / 100.0
                totalIGST += igstAmt
                Triple(0.0, 0.0, igstAmt)
            } else {
                // CGST + SGST each at half rate
                val halfRate = rate / 2.0
                val cgstAmt = taxableAmount * halfRate / 100.0
                val sgstAmt = taxableAmount * halfRate / 100.0
                totalCGST += cgstAmt
                totalSGST += sgstAmt
                Triple(cgstAmt, sgstAmt, 0.0)
            }

            GSTLineItem(
                rate = rate,
                taxableAmount = taxableAmount,
                cgst = cgst,
                sgst = sgst,
                igst = igst
            )
        }.sortedBy { it.rate }

        val totalGST = totalCGST + totalSGST + totalIGST
        val grandTotal = overallSubtotal + totalGST

        return GSTSummary(
            subtotal = overallSubtotal,
            gstBreakdown = gstBreakdown,
            totalCGST = totalCGST,
            totalSGST = totalSGST,
            totalIGST = totalIGST,
            totalGST = totalGST,
            grandTotal = grandTotal,
            isInterState = isInterState
        )
    }
}
