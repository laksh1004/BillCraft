package com.billcraft.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.billcraft.app.data.local.dao.*
import com.billcraft.app.data.local.entity.*

@Database(
    entities = [
        BusinessEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        LineItemEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BillCraftDatabase : RoomDatabase() {

    abstract fun businessDao(): BusinessDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun lineItemDao(): LineItemDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: BillCraftDatabase? = null

        fun getDatabase(context: Context): BillCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillCraftDatabase::class.java,
                    "billcraft_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
