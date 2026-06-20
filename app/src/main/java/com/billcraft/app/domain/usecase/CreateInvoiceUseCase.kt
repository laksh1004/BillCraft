package com.billcraft.app.domain.usecase

import com.billcraft.app.data.repository.InvoiceRepository
import com.billcraft.app.domain.model.*
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Params
// ---------------------------------------------------------------------------

data class CreateInvoiceParams(
    val type: InvoiceType,
    val customerId: String,
    val businessId: String,
    val lineItems: List<LineItem>,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate?,
    val notes: String?,
    val termsAndConditions: String?,
    val isInterState: Boolean,
    val customer: Customer? = null,
    val business: Business? = null
)

// ---------------------------------------------------------------------------
// Use Case
// ---------------------------------------------------------------------------

/**
 * Orchestrates the full invoice-creation flow:
 * 1. Validates that at least one line item is present.
 * 2. Calculates GST totals via [CalculateInvoiceTotalsUseCase].
 * 3. Generates a sequential invoice number via [InvoiceNumberGenerator].
 * 4. Builds the [Invoice] domain object.
 * 5. Persists it through [InvoiceRepository].
 * 6. Returns the new invoice ID wrapped in [Result].
 */
class CreateInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val calculateTotals: CalculateInvoiceTotalsUseCase,
    private val invoiceNumberGenerator: InvoiceNumberGenerator
) {

    suspend operator fun invoke(params: CreateInvoiceParams): Result<String> {
        return try {
            // 1. Validate
            if (params.lineItems.isEmpty()) {
                return Result.failure(IllegalArgumentException("Invoice must have at least one line item."))
            }

            // 2. Calculate GST totals
            val gstSummary = calculateTotals(params.lineItems, params.isInterState)

            // 3. Generate invoice number (uses current year)
            val invoiceNumber = invoiceNumberGenerator.generate(params.type)

            // 4. Build Invoice domain object
            val invoiceId = UUID.randomUUID().toString()
            val invoice = Invoice(
                id = invoiceId,
                invoiceNumber = invoiceNumber,
                type = params.type,
                customer = params.customer,
                business = params.business,
                invoiceDate = params.invoiceDate,
                dueDate = params.dueDate,
                status = InvoiceStatus.DRAFT,
                lineItems = params.lineItems,
                subtotal = gstSummary.subtotal,
                cgstAmount = gstSummary.totalCGST,
                sgstAmount = gstSummary.totalSGST,
                igstAmount = gstSummary.totalIGST,
                totalAmount = gstSummary.grandTotal,
                amountPaid = 0.0,
                notes = params.notes,
                termsAndConditions = params.termsAndConditions,
                isInterState = params.isInterState
            )

            // 5. Persist
            invoiceRepository.createInvoice(invoice, params.lineItems)

            // 6. Return ID
            Result.success(invoiceId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
