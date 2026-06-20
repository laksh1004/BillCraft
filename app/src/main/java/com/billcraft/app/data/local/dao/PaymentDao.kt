package com.billcraft.app.data.local.dao

import androidx.room.*
import com.billcraft.app.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY paymentDate DESC")
    fun getByInvoiceId(invoiceId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY paymentDate DESC")
    suspend fun getByInvoiceIdOnce(invoiceId: String): List<PaymentEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE invoiceId = :invoiceId")
    suspend fun getTotalPaidForInvoice(invoiceId: String): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE invoiceId = :invoiceId")
    fun getTotalPaidForInvoiceFlow(invoiceId: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity)

    @Update
    suspend fun update(payment: PaymentEntity)

    @Delete
    suspend fun delete(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM payments WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: String)
}
