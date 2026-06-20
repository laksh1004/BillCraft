package com.billcraft.app.domain.usecase

import com.billcraft.app.data.repository.InvoiceRepository
import com.billcraft.app.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Transitions an invoice from [InvoiceStatus.DRAFT] → [InvoiceStatus.SENT].
 *
 * Preconditions checked before updating:
 * - The invoice must exist.
 * - It must currently be in DRAFT status.
 * - It must contain at least one line item.
 */
class FinalizeInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) {

    suspend operator fun invoke(invoiceId: String): Result<Unit> {
        return try {
            // Fetch current state (first emission from the flow)
            val invoice = invoiceRepository.getInvoiceById(invoiceId).first()
                ?: return Result.failure(NoSuchElementException("Invoice $invoiceId not found."))

            when {
                invoice.status != InvoiceStatus.DRAFT ->
                    return Result.failure(
                        IllegalStateException(
                            "Invoice is already in status '${invoice.status}'; only DRAFT invoices can be sent."
                        )
                    )

                invoice.lineItems.isEmpty() ->
                    return Result.failure(
                        IllegalStateException("Cannot send an invoice with no line items.")
                    )
            }

            invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
