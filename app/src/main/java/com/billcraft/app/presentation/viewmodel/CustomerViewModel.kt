package com.billcraft.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcraft.app.data.repository.CustomerRepository
import com.billcraft.app.domain.model.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CustomerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) customerRepository.getAllCustomers()
            else customerRepository.searchCustomers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadCustomer(customerId: String) {
        viewModelScope.launch {
            _selectedCustomer.value = customerRepository.getCustomerById(customerId)
        }
    }

    fun createCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = CustomerUiState(isLoading = true)
            try {
                customerRepository.createCustomer(customer.copy(id = UUID.randomUUID().toString()))
                _uiState.value = CustomerUiState(success = true)
            } catch (e: Exception) {
                _uiState.value = CustomerUiState(error = e.message ?: "Failed to create customer")
            }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = CustomerUiState(isLoading = true)
            try {
                customerRepository.updateCustomer(customer)
                _uiState.value = CustomerUiState(success = true)
            } catch (e: Exception) {
                _uiState.value = CustomerUiState(error = e.message ?: "Failed to update customer")
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            _uiState.value = CustomerUiState(isLoading = true)
            try {
                customerRepository.deleteCustomer(customerId)
                _uiState.value = CustomerUiState(success = true)
            } catch (e: Exception) {
                _uiState.value = CustomerUiState(error = e.message ?: "Failed to delete customer")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = CustomerUiState()
    }
}
