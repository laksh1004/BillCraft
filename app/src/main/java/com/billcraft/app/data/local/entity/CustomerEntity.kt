package com.billcraft.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val gstin: String? = null,
    val phone: String = "",
    val email: String? = null,
    val address: String = "",
    val state: String = "",
    val city: String = "",
    val pincode: String = "",
    val balance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
