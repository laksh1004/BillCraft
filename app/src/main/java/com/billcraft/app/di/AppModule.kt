package com.billcraft.app.di
import android.content.Context
import androidx.room.Room
import com.billcraft.app.data.local.BillCraftDatabase
import com.billcraft.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BillCraftDatabase {
        return Room.databaseBuilder(ctx, BillCraftDatabase::class.java, "billcraft_db").build()
    }

    @Provides fun provideBusinessDao(db: BillCraftDatabase) = db.businessDao()
    @Provides fun provideCustomerDao(db: BillCraftDatabase) = db.customerDao()
    @Provides fun provideInvoiceDao(db: BillCraftDatabase) = db.invoiceDao()
    @Provides fun provideLineItemDao(db: BillCraftDatabase) = db.lineItemDao()
    @Provides fun providePaymentDao(db: BillCraftDatabase) = db.paymentDao()

    @Provides @Singleton fun provideInvoiceRepository(impl: InvoiceRepositoryImpl): InvoiceRepository = impl
    @Provides @Singleton fun provideCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository = impl
    @Provides @Singleton fun provideBusinessRepository(impl: BusinessRepositoryImpl): BusinessRepository = impl
    @Provides @Singleton fun providePaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository = impl
}
