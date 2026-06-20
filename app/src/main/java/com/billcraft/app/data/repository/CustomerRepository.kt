package com.billcraft.app.data.repository

import com.billcraft.app.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: String): Customer?
    fun searchCustomers(query: String): Flow<List<Customer>>
    suspend fun createCustomer(customer: Customer): String
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customerId: String)
    suspend fun updateBalance(customerId: String, amount: Double)
}
