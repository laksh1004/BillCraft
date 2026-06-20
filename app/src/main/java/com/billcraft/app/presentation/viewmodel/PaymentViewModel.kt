package com.billcraft.app.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcraft.app.data.repository.PaymentRepository
import com.billcraft.app.domain.model.Invoice
import com.billcraft.app.domain.model.Payment
import com.billcraft.app.domain.model.PaymentMode
import com.billcraft.app.domain.util.UpiQrGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _paymentHistory = MutableStateFlow<List<Payment>>(emptyList())
    val paymentHistory: StateFlow<List<Payment>> = _paymentHistory.asStateFlow()

    private val _totalPaid = MutableStateFlow(0.0)
    val totalPaid: StateFlow<Double> = _totalPaid.asStateFlow()

    private val _totalInvoiced = MutableStateFlow(0.0)
    val totalInvoiced: StateFlow<Double> = _totalInvoiced.asStateFlow()

    val balanceDue: StateFlow<Double> = combine(_totalInvoiced, _totalPaid) { invoiced, paid ->
        (invoiced - paid).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // Input state
    val selectedPaymentMode = MutableStateFlow(PaymentMode.CASH)
    val paymentAmount = MutableStateFlow("")
    val upiTransactionId = MutableStateFlow("")
    val chequeNumber = MutableStateFlow("")
    val bankName = MutableStateFlow("")
    val paymentNotes = MutableStateFlow("")

    // UPI QR
    private val _upiQrBitmap = MutableStateFlow<Bitmap?>(null)
    val upiQrBitmap: StateFlow<Bitmap?> = _upiQrBitmap.asStateFlow()

    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess.asStateFlow()

    // Keep track of which invoice is loaded to avoid repeated collection
    private var currentInvoiceId: String? = null

    // -------------------------------------------------------------------------
    // Load
    // -------------------------------------------------------------------------

    fun loadPayments(invoiceId: String, invoiceTotalAmount: Double = 0.0) {
        if (currentInvoiceId == invoiceId) return
        currentInvoiceId = invoiceId
        _totalInvoiced.value = invoiceTotalAmount

        viewModelScope.launch {
            // Collect payment list
            paymentRepository.getPaymentsByInvoice(invoiceId)
                .catch { e -> _errorMessage.value = e.localizedMessage }
                .collect { payments ->
                    _paymentHistory.value = payments.sortedByDescending { it.paymentDate }
                }
        }

        viewModelScope.launch {
            paymentRepository.getTotalPaidForInvoice(invoiceId)
                .catch { e -> _errorMessage.value = e.localizedMessage }
                .collect { total -> _totalPaid.value = total }
        }
    }

    // -------------------------------------------------------------------------
    // Record payment
    // -------------------------------------------------------------------------

    fun recordPayment(invoiceId: String, invoice: Invoice) {
        val amountStr = paymentAmount.value
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _errorMessage.value = "Please enter a valid payment amount."
            return
        }
        if (amount > invoice.balanceDue + 0.001) {
            _errorMessage.value = "Payment cannot exceed balance due."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val payment = Payment(
                id = UUID.randomUUID().toString(),
                invoiceId = invoiceId,
                amount = amount,
                paymentDate = LocalDate.now(),
                paymentMode = selectedPaymentMode.value,
                upiTransactionId = upiTransactionId.value.takeIf { it.isNotBlank() },
                chequeNumber = chequeNumber.value.takeIf { it.isNotBlank() },
                bankName = bankName.value.takeIf { it.isNotBlank() },
                notes = paymentNotes.value.takeIf { it.isNotBlank() }
            )

            paymentRepository.recordPayment(payment, invoice).fold(
                onSuccess = {
                    resetInputFields()
                    _paymentSuccess.value = true
                },
                onFailure = { e ->
                    _errorMessage.value = e.localizedMessage ?: "Failed to record payment."
                }
            )
            _isLoading.value = false
        }
    }

    // -------------------------------------------------------------------------
    // UPI QR generation
    // -------------------------------------------------------------------------

    fun generateUpiQr(upiId: String, amount: Double, invoiceNumber: String) {
        viewModelScope.launch {
            _upiQrBitmap.value = null
            val bitmap = withContext(Dispatchers.Default) {
                val uri = UpiQrGenerator.generateUpiUri(
                    upiId = upiId,
                    payeeName = "BillCraft",
                    amount = amount,
                    note = "Payment for invoice $invoiceNumber"
                )
                UpiQrGenerator.generateQrBitmap(uri)
            }
            _upiQrBitmap.value = bitmap
        }
    }

    // -------------------------------------------------------------------------
    // Input event handlers
    // -------------------------------------------------------------------------

    fun onPaymentModeChanged(mode: PaymentMode) {
        selectedPaymentMode.value = mode
        // Clear QR when switching away from UPI
        if (mode != PaymentMode.UPI) _upiQrBitmap.value = null
    }

    fun onAmountChanged(amount: String) {
        paymentAmount.value = amount
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetPaymentSuccess() {
        _paymentSuccess.value = false
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun resetInputFields() {
        paymentAmount.value = ""
        upiTransactionId.value = ""
        chequeNumber.value = ""
        bankName.value = ""
        paymentNotes.value = ""
    }
}
