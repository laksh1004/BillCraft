package com.billcraft.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcraft.app.data.repository.BusinessRepository
import com.billcraft.app.domain.model.Business
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BusinessUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isSetupComplete: Boolean = false
)

@HiltViewModel
class BusinessViewModel @Inject constructor(
    private val businessRepository: BusinessRepository
) : ViewModel() {

    val business: StateFlow<Business?> = businessRepository
        .getFirstBusiness()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState: StateFlow<BusinessUiState> = _uiState.asStateFlow()

    init {
        // Check if business setup is complete
        viewModelScope.launch {
            val exists = businessRepository.businessExists()
            _uiState.update { it.copy(isSetupComplete = exists) }
        }
    }

    fun saveBusiness(business: Business) {
        viewModelScope.launch {
            _uiState.value = BusinessUiState(isLoading = true)
            try {
                val exists = businessRepository.businessExists()
                if (exists) {
                    businessRepository.updateBusiness(business)
                } else {
                    val id = business.id.ifBlank { UUID.randomUUID().toString() }
                    businessRepository.createBusiness(business.copy(id = id))
                }
                _uiState.value = BusinessUiState(success = true, isSetupComplete = true)
            } catch (e: Exception) {
                _uiState.value = BusinessUiState(error = e.message ?: "Failed to save business")
            }
        }
    }

    fun updateBusiness(business: Business) {
        viewModelScope.launch {
            _uiState.value = BusinessUiState(isLoading = true)
            try {
                businessRepository.updateBusiness(business)
                _uiState.value = BusinessUiState(success = true, isSetupComplete = true)
            } catch (e: Exception) {
                _uiState.value = BusinessUiState(error = e.message ?: "Failed to update business")
            }
        }
    }

    fun clearUiState() {
        _uiState.update { it.copy(error = null, success = false) }
    }
}
