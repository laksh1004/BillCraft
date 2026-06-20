package com.billcraft.app.domain.util

/**
 * TDS (Tax Deducted at Source) calculator for Indian businesses.
 *
 * Covers the most commonly applicable sections under the Income Tax Act, 1961.
 * Rates are as per standard provisions (not considering surcharge / cess for
 * simplicity at the invoice level; those can be added on top).
 */
object TDSCalculator {

    // -------------------------------------------------------------------------
    // Data model
    // -------------------------------------------------------------------------

    data class TDSSection(
        /** Short code displayed in the UI – e.g. "194C" */
        val code: String,
        /** Human-readable description of the payment type */
        val description: String,
        /** TDS rate (%) applicable for Individual / HUF assessee */
        val rateIndividual: Double,
        /** TDS rate (%) applicable for Company / Firm / AOP assessee */
        val rateCompany: Double,
        /** Threshold limit (₹) below which TDS is NOT deducted (0 = always deduct) */
        val thresholdAmount: Double = 0.0
    )

    data class TDSResult(
        val section: TDSSection,
        val taxableAmount: Double,
        val tdsAmount: Double,
        val netPayable: Double
    )

    // -------------------------------------------------------------------------
    // Section master list
    // -------------------------------------------------------------------------

    fun getTDSSections(): List<TDSSection> = listOf(
        TDSSection(
            code = "192",
            description = "Salary",
            rateIndividual = 0.0,  // slab-based; pass 0 as placeholder
            rateCompany = 0.0,
            thresholdAmount = 250_000.0
        ),
        TDSSection(
            code = "193",
            description = "Interest on Securities",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 5_000.0
        ),
        TDSSection(
            code = "194",
            description = "Dividend",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 5_000.0
        ),
        TDSSection(
            code = "194A",
            description = "Interest other than on Securities (Banks / NBFCs etc.)",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 40_000.0
        ),
        TDSSection(
            code = "194B",
            description = "Winnings from Lottery / Crossword Puzzle",
            rateIndividual = 30.0,
            rateCompany = 30.0,
            thresholdAmount = 10_000.0
        ),
        TDSSection(
            code = "194C",
            description = "Payment to Contractors / Sub-contractors",
            rateIndividual = 1.0,
            rateCompany = 2.0,
            thresholdAmount = 30_000.0  // single payment; aggregate ₹1 L / year
        ),
        TDSSection(
            code = "194D",
            description = "Insurance Commission",
            rateIndividual = 5.0,
            rateCompany = 10.0,
            thresholdAmount = 15_000.0
        ),
        TDSSection(
            code = "194E",
            description = "Payment to Non-Resident Sportsman / Entertainment Association",
            rateIndividual = 20.0,
            rateCompany = 20.0,
            thresholdAmount = 0.0
        ),
        TDSSection(
            code = "194G",
            description = "Commission on Sale of Lottery Tickets",
            rateIndividual = 5.0,
            rateCompany = 5.0,
            thresholdAmount = 15_000.0
        ),
        TDSSection(
            code = "194H",
            description = "Commission or Brokerage",
            rateIndividual = 5.0,
            rateCompany = 5.0,
            thresholdAmount = 15_000.0
        ),
        TDSSection(
            code = "194I",
            description = "Rent (Land, Building, Furniture & Fittings)",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 240_000.0
        ),
        TDSSection(
            code = "194IA",
            description = "Payment for Transfer of Immovable Property (other than agricultural land)",
            rateIndividual = 1.0,
            rateCompany = 1.0,
            thresholdAmount = 5_000_000.0
        ),
        TDSSection(
            code = "194IB",
            description = "Rent by Individual / HUF (not covered under 194I)",
            rateIndividual = 5.0,
            rateCompany = 5.0,
            thresholdAmount = 240_000.0
        ),
        TDSSection(
            code = "194IC",
            description = "Payment under Joint Development Agreements",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 0.0
        ),
        TDSSection(
            code = "194J",
            description = "Fees for Professional / Technical Services",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 30_000.0
        ),
        TDSSection(
            code = "194JA",
            description = "Fees for Technical Services (not being Professional Services)",
            rateIndividual = 2.0,
            rateCompany = 2.0,
            thresholdAmount = 30_000.0
        ),
        TDSSection(
            code = "194K",
            description = "Income from Mutual Fund Units",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 5_000.0
        ),
        TDSSection(
            code = "194LA",
            description = "Compensation on Acquisition of Immovable Property",
            rateIndividual = 10.0,
            rateCompany = 10.0,
            thresholdAmount = 250_000.0
        ),
        TDSSection(
            code = "194M",
            description = "Payment to Contractor / Professional by Individual / HUF (non-audit)",
            rateIndividual = 5.0,
            rateCompany = 5.0,
            thresholdAmount = 5_000_000.0
        ),
        TDSSection(
            code = "194N",
            description = "Cash Withdrawal exceeding specified limit",
            rateIndividual = 2.0,
            rateCompany = 2.0,
            thresholdAmount = 2_000_000.0
        ),
        TDSSection(
            code = "194O",
            description = "Payment by E-Commerce Operator to E-Commerce Participant",
            rateIndividual = 1.0,
            rateCompany = 1.0,
            thresholdAmount = 500_000.0
        ),
        TDSSection(
            code = "194Q",
            description = "Payment for Purchase of Goods",
            rateIndividual = 0.1,
            rateCompany = 0.1,
            thresholdAmount = 5_000_000.0
        ),
        TDSSection(
            code = "195",
            description = "Payment to Non-Residents (other than salary)",
            rateIndividual = 20.0,
            rateCompany = 40.0,
            thresholdAmount = 0.0
        ),
        TDSSection(
            code = "206C",
            description = "Tax Collected at Source – Scrap / Timber / Tendu Leaves / Liquor etc.",
            rateIndividual = 1.0,
            rateCompany = 1.0,
            thresholdAmount = 0.0
        )
    )

    // -------------------------------------------------------------------------
    // Calculation helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the TDS section whose [TDSSection.code] matches [sectionCode],
     * or null if not found.
     */
    fun findSection(sectionCode: String): TDSSection? =
        getTDSSections().firstOrNull { it.code.equals(sectionCode, ignoreCase = true) }

    /**
     * Calculates TDS for a given [amount] and [sectionCode].
     *
     * @param amount       Gross payment amount in INR.
     * @param sectionCode  Section code string (e.g. "194C", "194J").
     * @param isCompany    True when the payee is a Company/Firm/AOP; false for Individual/HUF.
     * @return [TDSResult] containing the TDS amount and net payable, or null if section not found.
     */
    fun calculateTDS(
        amount: Double,
        sectionCode: String,
        isCompany: Boolean = false
    ): TDSResult? {
        val section = findSection(sectionCode) ?: return null
        if (amount < section.thresholdAmount) {
            return TDSResult(
                section = section,
                taxableAmount = amount,
                tdsAmount = 0.0,
                netPayable = amount
            )
        }
        val rate = if (isCompany) section.rateCompany else section.rateIndividual
        val tds = amount * rate / 100.0
        return TDSResult(
            section = section,
            taxableAmount = amount,
            tdsAmount = tds,
            netPayable = amount - tds
        )
    }

    /**
     * Convenience overload that accepts a [TDSSection] object directly.
     */
    fun calculateTDS(
        amount: Double,
        section: TDSSection,
        isCompany: Boolean = false
    ): TDSResult {
        if (amount < section.thresholdAmount) {
            return TDSResult(
                section = section,
                taxableAmount = amount,
                tdsAmount = 0.0,
                netPayable = amount
            )
        }
        val rate = if (isCompany) section.rateCompany else section.rateIndividual
        val tds = amount * rate / 100.0
        return TDSResult(
            section = section,
            taxableAmount = amount,
            tdsAmount = tds,
            netPayable = amount - tds
        )
    }
}
