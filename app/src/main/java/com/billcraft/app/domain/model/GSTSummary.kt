package com.billcraft.app.domain.model

data class GSTSummary(
    val subtotal: Double,
    val gstBreakdown: List<GSTLineItem>,
    val totalCGST: Double,
    val totalSGST: Double,
    val totalIGST: Double,
    val totalGST: Double,
    val grandTotal: Double,
    val isInterState: Boolean
)

data class GSTLineItem(
    val rate: Double,
    val taxableAmount: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double
)
