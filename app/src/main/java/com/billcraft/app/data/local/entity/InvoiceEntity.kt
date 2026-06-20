package com.billcraft.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val invoiceNumber: String,
    val type: String = "INVOICE", // INVOICE, ESTIMATE, RECEIPT
    val customerId: String,
    val businessId: String,
    val invoiceDate: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val status: String = "DRAFT", // DRAFT, SENT, PAID, PARTIAL, OVERDUE, CANCELLED
    val subtotal: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val notes: String? = null,
    val termsAndConditions: String? = null,
    val isInterState: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
