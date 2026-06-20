package com.billcraft.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "business")
data class BusinessEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val gstin: String? = null,
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val phone: String = "",
    val email: String? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val upiId: String? = null,
    val logoUri: String? = null,
    val signatureUri: String? = null
)
