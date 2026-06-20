package com.billcraft.app.data.repository

import com.billcraft.app.data.local.dao.InvoiceDao
import com.billcraft.app.data.local.dao.PaymentDao
import com.billcraft.app.data.local.entity.PaymentEntity
import com.billcraft.app.domain.model.Invoice
import com.billcraft.app.domain.model.Payment
import com.billcraft.app.domain.model.PaymentMode
import com.billcraft.app.domain.util.PaymentStatusCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao,
    private val invoiceDao: InvoiceDao
) : PaymentRepository {

    // -------------------------------------------------------------------------
    // Record payment
    // -------------------------------------------------------------------------

    override suspend fun recordPayment(payment: Payment, invoice: Invoice): Result<Unit> {
        return try {
            // 1. Insert payment entity
            paymentDao.insert(payment.toEntity())

            // 2. Recalculate total paid
            val newAmountPaid = paymentDao.getTotalPaidForInvoice(invoice.id)

            // 3. Derive new status
            val newStatus = PaymentStatusCalculator.calculate(
                totalAmount = invoice.totalAmount,
                amountPaid = newAmountPaid,
                dueDate = invoice.dueDate,
                isDraft = false,
                isCancelled = false
            )

            // 4. Update invoice entity
            val existingEntity = invoiceDao.getById(invoice.id)
                ?: return Result.failure(NoSuchElementException("Invoice ${invoice.id} not found"))

            invoiceDao.update(
                existingEntity.copy(
                    amountPaid = newAmountPaid,
                    status = newStatus.name,
                    updatedAt = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Query payments
    // -------------------------------------------------------------------------

    override fun getPaymentsByInvoice(invoiceId: String): Flow<List<Payment>> =
        paymentDao.getByInvoiceId(invoiceId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getTotalPaidForInvoice(invoiceId: String): Flow<Double> =
        paymentDao.getTotalPaidForInvoiceFlow(invoiceId)

    // -------------------------------------------------------------------------
    // Delete payment
    // -------------------------------------------------------------------------

    override suspend fun deletePayment(paymentId: String, invoice: Invoice): Result<Unit> {
        return try {
            paymentDao.deleteById(paymentId)

            // Recalculate after deletion
            val newAmountPaid = paymentDao.getTotalPaidForInvoice(invoice.id)
            val newStatus = PaymentStatusCalculator.calculate(
                totalAmount = invoice.totalAmount,
                amountPaid = newAmountPaid,
                dueDate = invoice.dueDate,
                isDraft = false,
                isCancelled = false
            )

            val existingEntity = invoiceDao.getById(invoice.id)
                ?: return Result.failure(NoSuchElementException("Invoice ${invoice.id} not found"))

            invoiceDao.update(
                existingEntity.copy(
                    amountPaid = newAmountPaid,
                    status = newStatus.name,
                    updatedAt = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private fun Payment.toEntity(): PaymentEntity = PaymentEntity(
        id = id,
        invoiceId = invoiceId,
        amount = amount,
        paymentDate = paymentDate.toEpochMillis(),
        paymentMode = paymentMode.name,
        upiTransactionId = upiTransactionId,
        chequeNumber = chequeNumber,
        bankName = bankName,
        notes = notes
    )

    private fun PaymentEntity.toDomain(): Payment = Payment(
        id = id,
        invoiceId = invoiceId,
        amount = amount,
        paymentDate = paymentDate.toLocalDate(),
        paymentMode = PaymentMode.valueOf(paymentMode),
        upiTransactionId = upiTransactionId,
        chequeNumber = chequeNumber,
        bankName = bankName,
        notes = notes
    )

    private fun LocalDate.toEpochMillis(): Long =
        atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
}
