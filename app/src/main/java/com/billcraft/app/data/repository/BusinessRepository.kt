package com.billcraft.app.data.repository

import com.billcraft.app.domain.model.Business
import kotlinx.coroutines.flow.Flow

interface BusinessRepository {
    fun getAllBusinesses(): Flow<List<Business>>
    fun getFirstBusiness(): Flow<Business?>
    suspend fun getBusinessById(id: String): Business?
    suspend fun createBusiness(business: Business): String
    suspend fun updateBusiness(business: Business)
    suspend fun deleteBusiness(businessId: String)
    suspend fun businessExists(): Boolean
}
