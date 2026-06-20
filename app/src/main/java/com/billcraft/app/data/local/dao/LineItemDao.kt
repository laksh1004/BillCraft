package com.billcraft.app.data.local.dao

import androidx.room.*
import com.billcraft.app.data.local.entity.LineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineItemDao {

    @Query("SELECT * FROM line_items WHERE invoiceId = :invoiceId ORDER BY position ASC")
    fun getByInvoiceId(invoiceId: String): Flow<List<LineItemEntity>>

    @Query("SELECT * FROM line_items WHERE invoiceId = :invoiceId ORDER BY position ASC")
    suspend fun getByInvoiceIdOnce(invoiceId: String): List<LineItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lineItem: LineItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lineItems: List<LineItemEntity>)

    @Update
    suspend fun update(lineItem: LineItemEntity)

    @Delete
    suspend fun delete(lineItem: LineItemEntity)

    @Query("DELETE FROM line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: String)

    @Query("DELETE FROM line_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
