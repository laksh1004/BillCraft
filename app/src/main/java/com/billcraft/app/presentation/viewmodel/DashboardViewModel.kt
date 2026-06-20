package com.billcraft.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcraft.app.data.repository.InvoiceRepository
import com.billcraft.app.domain.model.Invoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    val totalRevenue: StateFlow<Double> = invoiceRepository
        .getTotalRevenue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val pendingAmount: StateFlow<Double> = invoiceRepository
        .getTotalPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val overdueCount: StateFlow<Int> = invoiceRepository
        .getOverdueCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val recentInvoices: StateFlow<List<Invoice>> = invoiceRepository
        .getRecentInvoices(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Derived metric: total invoices count
    val totalInvoices: StateFlow<Int> = invoiceRepository
        .getAllInvoices()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
