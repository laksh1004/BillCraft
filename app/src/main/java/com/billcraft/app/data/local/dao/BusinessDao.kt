package com.billcraft.app.data.local.dao

import androidx.room.*
import com.billcraft.app.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {

    @Query("SELECT * FROM business")
    fun getAll(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM business WHERE id = :id")
    suspend fun getById(id: String): BusinessEntity?

    @Query("SELECT * FROM business LIMIT 1")
    fun getFirst(): Flow<BusinessEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(business: BusinessEntity)

    @Update
    suspend fun update(business: BusinessEntity)

    @Delete
    suspend fun delete(business: BusinessEntity)

    @Query("DELETE FROM business WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM business")
    suspend fun count(): Int
}
