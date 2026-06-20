package com.billcraft.app.domain.util

import com.billcraft.app.domain.model.InvoiceStatus
import java.time.LocalDate

/**
 * Pure, stateless calculator that derives [InvoiceStatus] from financial and
 * temporal state.  Priority order (highest → lowest):
 * CANCELLED → DRAFT → PAID → OVERDUE (partial or full) → PARTIAL → SENT
 */
object PaymentStatusCalculator {

    /**
     * @param totalAmount   Grand total of the invoice.
     * @param amountPaid    Sum of all recorded payments against this invoice.
     * @param dueDate       Optional due date; overdue logic is skipped when null.
     * @param isDraft       True when the invoice has not yet been sent to the customer.
     * @param isCancelled   True when the invoice is voided / cancelled.
     */
    fun calculate(
        totalAmount: Double,
        amountPaid: Double,
        dueDate: LocalDate?,
        isDraft: Boolean = false,
        isCancelled: Boolean = false
    ): InvoiceStatus {
        return when {
            isCancelled -> InvoiceStatus.CANCELLED
            isDraft -> InvoiceStatus.DRAFT
            totalAmount <= 0.0 -> InvoiceStatus.DRAFT          // degenerate case
            amountPaid >= totalAmount -> InvoiceStatus.PAID
            amountPaid > 0.0 -> {
                // Partial payment – still overdue if past due date
                if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
                    InvoiceStatus.OVERDUE
                } else {
                    InvoiceStatus.PARTIAL
                }
            }
            // Zero payment
            dueDate != null && LocalDate.now().isAfter(dueDate) -> InvoiceStatus.OVERDUE
            else -> InvoiceStatus.SENT
        }
    }
}
