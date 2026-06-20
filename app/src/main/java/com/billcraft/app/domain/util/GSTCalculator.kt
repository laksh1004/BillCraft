package com.billcraft.app.domain.util

import com.billcraft.app.domain.model.GSTLineItem
import com.billcraft.app.domain.model.GSTSummary
import com.billcraft.app.domain.model.LineItem
import kotlin.math.roundToInt

object GSTCalculator {

    /**
     * Calculates full GST summary for a list of line items.
     * Groups by GST rate and computes CGST/SGST (intra-state) or IGST (inter-state).
     */
    fun calculateGST(lineItems: List<LineItem>, isInterState: Boolean): GSTSummary {
        // Group line items by GST rate
        val grouped = lineItems.groupBy { it.gstRate }

        val gstBreakdown = grouped.map { (rate, items) ->
            val taxableAmount = items.sumOf { calculateTaxableAmount(it) }
            if (isInterState) {
                val igst = roundToTwoDecimal(taxableAmount * rate / 100.0)
                GSTLineItem(
                    rate = rate,
                    taxableAmount = roundToTwoDecimal(taxableAmount),
                    cgst = 0.0,
                    sgst = 0.0,
                    igst = igst
                )
            } else {
                val halfRate = rate / 2.0
                val cgst = roundToTwoDecimal(taxableAmount * halfRate / 100.0)
                val sgst = roundToTwoDecimal(taxableAmount * halfRate / 100.0)
                GSTLineItem(
                    rate = rate,
                    taxableAmount = roundToTwoDecimal(taxableAmount),
                    cgst = cgst,
                    sgst = sgst,
                    igst = 0.0
                )
            }
        }.sortedBy { it.rate }

        val subtotal = lineItems.sumOf { calculateTaxableAmount(it) }
        val totalCGST = gstBreakdown.sumOf { it.cgst }
        val totalSGST = gstBreakdown.sumOf { it.sgst }
        val totalIGST = gstBreakdown.sumOf { it.igst }
        val totalGST = totalCGST + totalSGST + totalIGST
        val grandTotal = subtotal + totalGST

        return GSTSummary(
            subtotal = roundToTwoDecimal(subtotal),
            gstBreakdown = gstBreakdown,
            totalCGST = roundToTwoDecimal(totalCGST),
            totalSGST = roundToTwoDecimal(totalSGST),
            totalIGST = roundToTwoDecimal(totalIGST),
            totalGST = roundToTwoDecimal(totalGST),
            grandTotal = roundToTwoDecimal(grandTotal),
            isInterState = isInterState
        )
    }

    /**
     * Calculates taxable amount for a single line item after discount.
     */
    private fun calculateTaxableAmount(item: LineItem): Double {
        val gross = item.quantity * item.pricePerUnit
        val discountAmount = gross * (item.discountPercent / 100.0)
        return gross - discountAmount
    }

    /**
     * Calculates complete line item amount including GST.
     */
    fun calculateLineItemAmount(
        quantity: Double,
        pricePerUnit: Double,
        discountPercent: Double,
        gstRate: Double
    ): Double {
        val gross = quantity * pricePerUnit
        val discountAmount = gross * (discountPercent / 100.0)
        val taxable = gross - discountAmount
        val gstAmount = taxable * (gstRate / 100.0)
        return roundToTwoDecimal(taxable + gstAmount)
    }

    /**
     * Validates a GSTIN number using the official format.
     * Format: 2-digit state code + 10-char PAN + 1 entity num + Z + 1 checksum
     */
    fun validateGSTIN(gstin: String): Boolean {
        if (gstin.isBlank()) return false
        val pattern = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
        return pattern.matches(gstin.trim().uppercase())
    }

    /**
     * Extracts the 2-digit state code from a GSTIN.
     */
    fun getStateCodeFromGSTIN(gstin: String): String {
        return if (gstin.length >= 2) gstin.substring(0, 2) else ""
    }

    /**
     * Determines if a transaction is inter-state by comparing state codes in GSTINs.
     * Returns true if different states → IGST applies.
     * Returns false if same state → CGST + SGST apply.
     */
    fun isInterStateTransaction(businessGSTIN: String?, customerGSTIN: String?): Boolean {
        if (businessGSTIN.isNullOrBlank() || customerGSTIN.isNullOrBlank()) return false
        val businessState = getStateCodeFromGSTIN(businessGSTIN.uppercase())
        val customerState = getStateCodeFromGSTIN(customerGSTIN.uppercase())
        return businessState != customerState
    }

    /**
     * Indian GST state codes lookup.
     */
    val stateCodeMap: Map<String, String> = mapOf(
        "01" to "Jammu & Kashmir", "02" to "Himachal Pradesh", "03" to "Punjab",
        "04" to "Chandigarh", "05" to "Uttarakhand", "06" to "Haryana",
        "07" to "Delhi", "08" to "Rajasthan", "09" to "Uttar Pradesh",
        "10" to "Bihar", "11" to "Sikkim", "12" to "Arunachal Pradesh",
        "13" to "Nagaland", "14" to "Manipur", "15" to "Mizoram",
        "16" to "Tripura", "17" to "Meghalaya", "18" to "Assam",
        "19" to "West Bengal", "20" to "Jharkhand", "21" to "Odisha",
        "22" to "Chhattisgarh", "23" to "Madhya Pradesh", "24" to "Gujarat",
        "26" to "Dadra & Nagar Haveli and Daman & Diu", "27" to "Maharashtra",
        "28" to "Andhra Pradesh", "29" to "Karnataka", "30" to "Goa",
        "31" to "Lakshadweep", "32" to "Kerala", "33" to "Tamil Nadu",
        "34" to "Puducherry", "35" to "Andaman & Nicobar Islands",
        "36" to "Telangana", "37" to "Andhra Pradesh (New)"
    )

    fun getStateName(gstin: String): String {
        val code = getStateCodeFromGSTIN(gstin)
        return stateCodeMap[code] ?: "Unknown State"
    }

    private fun roundToTwoDecimal(value: Double): Double =
        (value * 100.0).roundToInt() / 100.0
}
