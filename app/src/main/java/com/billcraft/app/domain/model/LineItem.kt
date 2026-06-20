package com.billcraft.app.domain.model

data class LineItem(
    val id: String,
    val invoiceId: String,
    val description: String,
    val hsnCode: String? = null,
    val quantity: Double,
    val unit: String = "Nos",
    val pricePerUnit: Double,
    val discountPercent: Double = 0.0,
    val gstRate: Double,
    val amount: Double,
    val position: Int = 0
) {
    val discountAmount: Double get() = (pricePerUnit * quantity * discountPercent) / 100.0
    val taxableAmount: Double get() = (pricePerUnit * quantity) - discountAmount
    val gstAmount: Double get() = taxableAmount * gstRate / 100.0
    val totalAmount: Double get() = taxableAmount + gstAmount
}
