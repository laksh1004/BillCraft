package com.billcraft.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMode: String = "CASH", // CASH, UPI, BANK_TRANSFER, CHEQUE, CARD
    val upiTransactionId: String? = null,
    val chequeNumber: String? = null,
    val bankName: String? = null,
    val notes: String? = null
)
