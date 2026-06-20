package com.billcraft.app.data.repository

import com.billcraft.app.domain.model.Invoice
import com.billcraft.app.domain.model.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Contract for persisting and querying [Payment] records.
 *
 * All suspend functions are safe to call from any coroutine scope.
 * Flow-returning functions are cold streams observed from the UI layer.
 */
interface PaymentRepository {

    /**
     * Records a new payment and updates the parent invoice's [Invoice.amountPaid]
     * and [Invoice.status] accordingly.
     */
    suspend fun recordPayment(payment: Payment, invoice: Invoice): Result<Unit>

    /** Returns all payments for [invoiceId], ordered newest-first, as a live stream. */
    fun getPaymentsByInvoice(invoiceId: String): Flow<List<Payment>>

    /** Live total of all payment amounts recorded against [invoiceId]. */
    fun getTotalPaidForInvoice(invoiceId: String): Flow<Double>

    /** Permanently removes a single payment record.  Invoice totals are recalculated. */
    suspend fun deletePayment(paymentId: String, invoice: Invoice): Result<Unit>
}
