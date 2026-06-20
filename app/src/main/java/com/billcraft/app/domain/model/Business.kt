package com.billcraft.app.domain.model

data class Business(
    val id: String,
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
) {
    val fullAddress: String
        get() = listOf(address, city, state, pincode)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    val hasBankDetails: Boolean
        get() = !bankName.isNullOrBlank() && !accountNumber.isNullOrBlank() && !ifscCode.isNullOrBlank()

    val hasUpi: Boolean get() = !upiId.isNullOrBlank()

    val stateCode: String?
        get() = gstin?.take(2)
}
