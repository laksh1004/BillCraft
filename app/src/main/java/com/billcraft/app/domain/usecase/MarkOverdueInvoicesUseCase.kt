package com.billcraft.app.domain.usecase

import com.billcraft.app.data.repository.InvoiceRepository
import com.billcraft.app.domain.model.InvoiceStatus
import com.billcraft.app.domain.util.PaymentStatusCalculator
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * Scans all non-closed invoices and marks them [InvoiceStatus.OVERDUE] when
 * their due date has passed and they have not been fully paid.
 *
 * Called daily by [com.billcraft.app.domain.worker.OverdueCheckWorker].
 *
 * @return List of invoice IDs that were newly marked as OVERDUE during this run.
 */
class MarkOverdueInvoicesUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) {

    suspend operator fun invoke(): List<String> {
        val today = LocalDate.now()
        val newlyOverdue = mutableListOf<String>()

        // Fetch all invoices (one-shot via first())
        val allInvoices = invoiceRepository.getAllInvoices().first()

        allInvoices.forEach { invoice ->
            // Skip statuses that are already terminal or don't need re-evaluation
            if (invoice.status == InvoiceStatus.PAID ||
                invoice.status == InvoiceStatus.CANCELLED ||
                invoice.status == InvoiceStatus.DRAFT
            ) {
                return@forEach
            }

            // Already marked overdue — no need to update again
            if (invoice.status == InvoiceStatus.OVERDUE) return@forEach

            val dueDate = invoice.dueDate ?: return@forEach

            if (today.isAfter(dueDate) && invoice.amountPaid < invoice.totalAmount) {
                invoiceRepository.updateInvoiceStatus(invoice.id, InvoiceStatus.OVERDUE)
                newlyOverdue.add(invoice.id)
            }
        }

        return newlyOverdue
    }
}
