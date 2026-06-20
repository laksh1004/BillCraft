package com.billcraft.app.domain.model

data class Customer(
    val id: String,
    val name: String,
    val gstin: String? = null,
    val phone: String = "",
    val email: String? = null,
    val address: String = "",
    val state: String = "",
    val city: String = "",
    val pincode: String = "",
    val balance: Double = 0.0
) {
    val displayName: String get() = if (name.isNotBlank()) name else phone
    val hasGSTIN: Boolean get() = !gstin.isNullOrBlank()
}
