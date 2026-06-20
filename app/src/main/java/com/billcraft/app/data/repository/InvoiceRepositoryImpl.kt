package com.billcraft.app.data.repository

import com.billcraft.app.data.local.dao.CustomerDao
import com.billcraft.app.data.local.dao.BusinessDao
import com.billcraft.app.data.local.dao.InvoiceDao
import com.billcraft.app.data.local.dao.LineItemDao
import com.billcraft.app.data.local.entity.InvoiceEntity
import com.billcraft.app.data.local.entity.LineItemEntity
import com.billcraft.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val lineItemDao: LineItemDao,
    private val customerDao: CustomerDao,
    private val businessDao: BusinessDao
) : InvoiceRepository {

    override fun getAllInvoices(): Flow<List<Invoice>> =
        invoiceDao.getAll().map { entities ->
            entities.map { entity -> enrichInvoice(entity) }
        }

    override fun getInvoiceById(id: String): Flow<Invoice?> =
        invoiceDao.getByIdFlow(id).map { entity ->
            entity?.let { enrichInvoice(it) }
        }

    override fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>> =
        invoiceDao.getByStatus(status.name).map { entities ->
            entities.map { enrichInvoice(it) }
        }

    override fun getInvoicesByCustomer(customerId: String): Flow<List<Invoice>> =
        invoiceDao.getByCustomer(customerId).map { entities ->
            entities.map { enrichInvoice(it) }
        }

    override fun getTotalRevenue(): Flow<Double> = invoiceDao.getTotalRevenue()

    override fun getTotalPending(): Flow<Double> = invoiceDao.getTotalPending()

    override fun getOverdueCount(): Flow<Int> = invoiceDao.getOverdueCount()

    override fun searchInvoices(query: String): Flow<List<Invoice>> =
        invoiceDao.searchInvoices(query).map { entities ->
            entities.map { enrichInvoice(it) }
        }

    override fun getRecentInvoices(limit: Int): Flow<List<Invoice>> =
        invoiceDao.getRecent(limit).map { entities ->
            entities.map { enrichInvoice(it) }
        }

    override suspend fun createInvoice(invoice: Invoice, lineItems: List<LineItem>): String {
        val id = invoice.id.ifBlank { UUID.randomUUID().toString() }
        invoiceDao.insert(invoice.toEntity(id))
        lineItemDao.deleteByInvoiceId(id)
        lineItemDao.insertAll(lineItems.mapIndexed { index, item ->
            item.toEntity(invoiceId = id, position = index)
        })
        return id
    }

    override suspend fun updateInvoice(invoice: Invoice, lineItems: List<LineItem>) {
        invoiceDao.update(invoice.toEntity(invoice.id))
        lineItemDao.deleteByInvoiceId(invoice.id)
        lineItemDao.insertAll(lineItems.mapIndexed { index, item ->
            item.toEntity(invoiceId = invoice.id, position = index)
        })
    }

    override suspend fun deleteInvoice(invoiceId: String) {
        lineItemDao.deleteByInvoiceId(invoiceId)
        invoiceDao.deleteById(invoiceId)
    }

    override suspend fun updateInvoiceStatus(invoiceId: String, status: InvoiceStatus) {
        val entity = invoiceDao.getById(invoiceId) ?: return
        invoiceDao.update(entity.copy(status = status.name, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun generateInvoiceNumber(year: Int): String {
        val prefix = "INV-$year"
        val lastNumber = invoiceDao.getLastInvoiceNumber(prefix) ?: 0
        return "$prefix-${String.format("%03d", lastNumber + 1)}"
    }

    override suspend fun getLastSequenceForPrefix(prefix: String): Int? {
        return invoiceDao.getLastInvoiceNumber(prefix)
    }

    // ---- Helper to enrich an InvoiceEntity with customer, business, lineItems ----

    private suspend fun enrichInvoice(entity: InvoiceEntity): Invoice {
        val customer = customerDao.getById(entity.customerId)?.let {
            Customer(
                id = it.id, name = it.name, gstin = it.gstin,
                phone = it.phone, email = it.email, address = it.address,
                state = it.state, city = it.city, pincode = it.pincode, balance = it.balance
            )
        }
        val business = businessDao.getById(entity.businessId)?.let {
            Business(
                id = it.id, name = it.name, gstin = it.gstin,
                address = it.address, city = it.city, state = it.state,
                pincode = it.pincode, phone = it.phone, email = it.email,
                bankName = it.bankName, accountNumber = it.accountNumber,
                ifscCode = it.ifscCode, upiId = it.upiId,
                logoUri = it.logoUri, signatureUri = it.signatureUri
            )
        }
        val lineItems = lineItemDao.getByInvoiceIdOnce(entity.id).map { li ->
            LineItem(
                id = li.id, invoiceId = li.invoiceId, description = li.description,
                hsnCode = li.hsnCode, quantity = li.quantity, unit = li.unit,
                pricePerUnit = li.pricePerUnit, discountPercent = li.discountPercent,
                gstRate = li.gstRate, amount = li.amount, position = li.position
            )
        }
        return entity.toDomain(customer, business, lineItems)
    }

    // ---- Mapping functions ----

    private fun InvoiceEntity.toDomain(
        customer: Customer?,
        business: Business?,
        lineItems: List<LineItem>
    ): Invoice = Invoice(
        id = id,
        invoiceNumber = invoiceNumber,
        type = InvoiceType.valueOf(type),
        customer = customer,
        business = business,
        invoiceDate = epochToLocalDate(invoiceDate),
        dueDate = dueDate?.let { epochToLocalDate(it) },
        status = InvoiceStatus.valueOf(status),
        lineItems = lineItems,
        subtotal = subtotal,
        cgstAmount = cgstAmount,
        sgstAmount = sgstAmount,
        igstAmount = igstAmount,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        notes = notes,
        termsAndConditions = termsAndConditions,
        isInterState = isInterState
    )

    private fun Invoice.toEntity(entityId: String): InvoiceEntity = InvoiceEntity(
        id = entityId,
        invoiceNumber = invoiceNumber,
        type = type.name,
        customerId = customer?.id ?: "",
        businessId = business?.id ?: "",
        invoiceDate = localDateToEpoch(invoiceDate),
        dueDate = dueDate?.let { localDateToEpoch(it) },
        status = status.name,
        subtotal = subtotal,
        cgstAmount = cgstAmount,
        sgstAmount = sgstAmount,
        igstAmount = igstAmount,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        notes = notes,
        termsAndConditions = termsAndConditions,
        isInterState = isInterState,
        updatedAt = System.currentTimeMillis()
    )

    private fun LineItem.toEntity(invoiceId: String, position: Int): LineItemEntity =
        LineItemEntity(
            id = id.ifBlank { UUID.randomUUID().toString() },
            invoiceId = invoiceId,
            description = description,
            hsnCode = hsnCode,
            quantity = quantity,
            unit = unit,
            pricePerUnit = pricePerUnit,
            discountPercent = discountPercent,
            gstRate = gstRate,
            amount = amount,
            position = position
        )

    private fun epochToLocalDate(epoch: Long): LocalDate =
        Instant.ofEpochMilli(epoch).atZone(ZoneOffset.UTC).toLocalDate()

    private fun localDateToEpoch(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
