package com.billcraft.app.data.repository

import com.billcraft.app.data.local.dao.BusinessDao
import com.billcraft.app.data.local.entity.BusinessEntity
import com.billcraft.app.domain.model.Business
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessRepositoryImpl @Inject constructor(
    private val businessDao: BusinessDao
) : BusinessRepository {

    override fun getAllBusinesses(): Flow<List<Business>> =
        businessDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getFirstBusiness(): Flow<Business?> =
        businessDao.getFirst().map { it?.toDomain() }

    override suspend fun getBusinessById(id: String): Business? =
        businessDao.getById(id)?.toDomain()

    override suspend fun createBusiness(business: Business): String {
        val id = business.id.ifBlank { UUID.randomUUID().toString() }
        businessDao.insert(business.toEntity(id))
        return id
    }

    override suspend fun updateBusiness(business: Business) {
        businessDao.update(business.toEntity(business.id))
    }

    override suspend fun deleteBusiness(businessId: String) {
        val entity = businessDao.getById(businessId) ?: return
        businessDao.delete(entity)
    }

    override suspend fun businessExists(): Boolean =
        businessDao.count() > 0

    // ---- Mapping functions ----

    private fun BusinessEntity.toDomain(): Business = Business(
        id = id,
        name = name,
        gstin = gstin,
        address = address,
        city = city,
        state = state,
        pincode = pincode,
        phone = phone,
        email = email,
        bankName = bankName,
        accountNumber = accountNumber,
        ifscCode = ifscCode,
        upiId = upiId,
        logoUri = logoUri,
        signatureUri = signatureUri
    )

    private fun Business.toEntity(entityId: String): BusinessEntity = BusinessEntity(
        id = entityId,
        name = name,
        gstin = gstin,
        address = address,
        city = city,
        state = state,
        pincode = pincode,
        phone = phone,
        email = email,
        bankName = bankName,
        accountNumber = accountNumber,
        ifscCode = ifscCode,
        upiId = upiId,
        logoUri = logoUri,
        signatureUri = signatureUri
    )
}
