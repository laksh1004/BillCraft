package com.billcraft.app.domain.model

import java.time.LocalDate

enum class InvoiceType { INVOICE, ESTIMATE, RECEIPT }

enum class InvoiceStatus { DRAFT, SENT, PAID, PARTIAL, OVERDUE, CANCELLED }

enum class PaymentMode { CASH, UPI, BANK_TRANSFER, CHEQUE, CARD }

data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val type: InvoiceType,
    val customer: Customer?,
    val business: Business?,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate?,
    val status: InvoiceStatus,
    val lineItems: List<LineItem>,
    val subtotal: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double,
    val amountPaid: Double,
    val notes: String?,
    val termsAndConditions: String?,
    val isInterState: Boolean
) {
    val balanceDue: Double get() = totalAmount - amountPaid

    val isOverdue: Boolean
        get() = dueDate != null &&
                LocalDate.now().isAfter(dueDate) &&
                status != InvoiceStatus.PAID &&
                status != InvoiceStatus.CANCELLED

    val isPaid: Boolean get() = status == InvoiceStatus.PAID || amountPaid >= totalAmount

    val paymentProgress: Float
        get() = if (totalAmount > 0) (amountPaid / totalAmount).toFloat().coerceIn(0f, 1f) else 0f
}
