package com.billcraft.app.data.repository

import com.billcraft.app.data.local.dao.CustomerDao
import com.billcraft.app.data.local.entity.CustomerEntity
import com.billcraft.app.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<Customer>> =
        customerDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCustomerById(id: String): Customer? =
        customerDao.getById(id)?.toDomain()

    override fun searchCustomers(query: String): Flow<List<Customer>> =
        customerDao.searchByName(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun createCustomer(customer: Customer): String {
        val id = customer.id.ifBlank { UUID.randomUUID().toString() }
        customerDao.insert(customer.toEntity(id))
        return id
    }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.update(customer.toEntity(customer.id))
    }

    override suspend fun deleteCustomer(customerId: String) {
        customerDao.deleteById(customerId)
    }

    override suspend fun updateBalance(customerId: String, amount: Double) {
        customerDao.updateBalance(customerId, amount)
    }

    // ---- Mapping functions ----

    private fun CustomerEntity.toDomain(): Customer = Customer(
        id = id,
        name = name,
        gstin = gstin,
        phone = phone,
        email = email,
        address = address,
        state = state,
        city = city,
        pincode = pincode,
        balance = balance
    )

    private fun Customer.toEntity(entityId: String): CustomerEntity = CustomerEntity(
        id = entityId,
        name = name,
        gstin = gstin,
        phone = phone,
        email = email,
        address = address,
        state = state,
        city = city,
        pincode = pincode,
        balance = balance,
        updatedAt = System.currentTimeMillis()
    )
}
