package com.billcraft.app.data.local.dao

import androidx.room.*
import com.billcraft.app.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getByIdFlow(id: String): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getByCustomer(customerId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM invoices WHERE status IN ('PAID', 'PARTIAL')")
    fun getTotalRevenue(): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount - amountPaid), 0) FROM invoices WHERE status IN ('SENT', 'PARTIAL', 'OVERDUE')")
    fun getTotalPending(): Flow<Double>

    @Query("SELECT COUNT(*) FROM invoices WHERE status = 'OVERDUE'")
    fun getOverdueCount(): Flow<Int>

    @Query("""
        SELECT * FROM invoices 
        WHERE invoiceNumber LIKE '%' || :query || '%' 
        ORDER BY createdAt DESC
    """)
    fun searchInvoices(query: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity)

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int

    @Query("SELECT MAX(CAST(SUBSTR(invoiceNumber, INSTR(invoiceNumber, '-', INSTR(invoiceNumber, '-') + 1) + 1) AS INTEGER)) FROM invoices WHERE invoiceNumber LIKE :prefix || '%'")
    suspend fun getLastInvoiceNumber(prefix: String): Int?
}
