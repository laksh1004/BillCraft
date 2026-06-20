package com.billcraft.app.data.repository

import com.billcraft.app.domain.model.Invoice
import com.billcraft.app.domain.model.InvoiceStatus
import com.billcraft.app.domain.model.LineItem
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getAllInvoices(): Flow<List<Invoice>>
    fun getInvoiceById(id: String): Flow<Invoice?>
    fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>>
    fun getInvoicesByCustomer(customerId: String): Flow<List<Invoice>>
    fun getTotalRevenue(): Flow<Double>
    fun getTotalPending(): Flow<Double>
    fun getOverdueCount(): Flow<Int>
    fun searchInvoices(query: String): Flow<List<Invoice>>
    fun getRecentInvoices(limit: Int = 10): Flow<List<Invoice>>
    suspend fun createInvoice(invoice: Invoice, lineItems: List<LineItem>): String
    suspend fun updateInvoice(invoice: Invoice, lineItems: List<LineItem>)
    suspend fun deleteInvoice(invoiceId: String)
    suspend fun updateInvoiceStatus(invoiceId: String, status: InvoiceStatus)
    suspend fun generateInvoiceNumber(year: Int): String
    /** Returns the highest numeric suffix found for [prefix], or null if none exist. */
    suspend fun getLastSequenceForPrefix(prefix: String): Int?
}
