package com.billcraft.app.domain.model

import java.time.LocalDate

data class Payment(
    val id: String,
    val invoiceId: String,
    val amount: Double,
    val paymentDate: LocalDate,
    val paymentMode: PaymentMode,
    val upiTransactionId: String? = null,
    val chequeNumber: String? = null,
    val bankName: String? = null,
    val notes: String? = null
) {
    val modeDisplayName: String
        get() = when (paymentMode) {
            PaymentMode.CASH -> "Cash"
            PaymentMode.UPI -> "UPI"
            PaymentMode.BANK_TRANSFER -> "Bank Transfer"
            PaymentMode.CHEQUE -> "Cheque"
            PaymentMode.CARD -> "Card"
        }
}
