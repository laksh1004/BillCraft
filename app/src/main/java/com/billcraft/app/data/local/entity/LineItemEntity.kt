package com.billcraft.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "line_items")
data class LineItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val description: String,
    val hsnCode: String? = null,
    val quantity: Double = 1.0,
    val unit: String = "Nos",
    val pricePerUnit: Double = 0.0,
    val discountPercent: Double = 0.0,
    val gstRate: Double = 0.0,
    val amount: Double = 0.0,
    val position: Int = 0
)
