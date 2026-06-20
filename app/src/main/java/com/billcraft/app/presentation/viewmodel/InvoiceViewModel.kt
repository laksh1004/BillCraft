package com.billcraft.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcraft.app.data.repository.BusinessRepository
import com.billcraft.app.data.repository.CustomerRepository
import com.billcraft.app.data.repository.InvoiceRepository
import com.billcraft.app.domain.model.*
import com.billcraft.app.domain.util.GSTCalculator
import com.billcraft.app.domain.util.InvoiceNumberGenerator
import com.billcraft.app.domain.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class InvoiceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {

    // ── Search & Filter State ────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<InvoiceStatus?>(null)
    val filterStatus: StateFlow<InvoiceStatus?> = _filterStatus.asStateFlow()

    // ── Invoices List ────────────────────────────────────────────────────
    val invoices: StateFlow<List<Invoice>> = combine(
        _searchQuery,
        _filterStatus
    ) { query, status -> Pair(query, status) }
        .flatMapLatest { (query, status) ->
            when {
                query.isNotBlank() -> invoiceRepository.searchInvoices(query)
                status != null -> invoiceRepository.getInvoicesByStatus(status)
                else -> invoiceRepository.getAllInvoices()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Selected Invoice ─────────────────────────────────────────────────
    private val _selectedInvoiceId = MutableStateFlow<String?>(null)

    val selectedInvoice: StateFlow<Invoice?> = _selectedInvoiceId
        .flatMapLatest { id ->
            if (id != null) invoiceRepository.getInvoiceById(id)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── UI State ─────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    // ── Actions ───────────────────────────────────────────────────────────

    fun loadInvoice(invoiceId: String) {
        _selectedInvoiceId.value = invoiceId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: InvoiceStatus?) {
        _filterStatus.value = status
    }

    fun createInvoice(invoice: Invoice, lineItems: List<LineItem>) {
        viewModelScope.launch {
            _uiState.value = InvoiceUiState(isLoading = true)
            try {
                val id = invoiceRepository.createInvoice(invoice, lineItems)
                _uiState.value = InvoiceUiState(success = id)
            } catch (e: Exception) {
                _uiState.value = InvoiceUiState(error = e.message ?: "Failed to create invoice")
            }
        }
    }

    fun updateInvoice(invoice: Invoice, lineItems: List<LineItem>) {
        viewModelScope.launch {
            _uiState.value = InvoiceUiState(isLoading = true)
            try {
                invoiceRepository.updateInvoice(invoice, lineItems)
                _uiState.value = InvoiceUiState(success = invoice.id)
            } catch (e: Exception) {
                _uiState.value = InvoiceUiState(error = e.message ?: "Failed to update invoice")
            }
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            _uiState.value = InvoiceUiState(isLoading = true)
            try {
                invoiceRepository.deleteInvoice(invoiceId)
                _uiState.value = InvoiceUiState(success = "deleted")
            } catch (e: Exception) {
                _uiState.value = InvoiceUiState(error = e.message ?: "Failed to delete invoice")
            }
        }
    }

    fun updateStatus(invoiceId: String, status: InvoiceStatus) {
        viewModelScope.launch {
            invoiceRepository.updateInvoiceStatus(invoiceId, status)
        }
    }

    /**
     * Generates a PDF for the given invoice and opens a share sheet.
     */
    fun generateAndSharePdf(context: Context, invoice: Invoice) {
        viewModelScope.launch {
            try {
                val pdfGenerator = PdfGenerator(context)
                val file = pdfGenerator.generateInvoicePdf(invoice)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "com.billcraft.app.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Invoice PDF"))
            } catch (e: Exception) {
                _uiState.value = InvoiceUiState(error = "Failed to generate PDF: ${e.message}")
            }
        }
    }

    /**
     * Shares invoice details via WhatsApp.
     */
    fun shareViaWhatsApp(context: Context, invoice: Invoice) {
        viewModelScope.launch {
            try {
                val message = buildWhatsAppMessage(invoice)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp")
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                context.startActivity(Intent.createChooser(intent, "Share via WhatsApp"))
            } catch (e: Exception) {
                // WhatsApp not installed – fallback to generic share
                val message = buildWhatsAppMessage(invoice)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                context.startActivity(Intent.createChooser(intent, "Share Invoice"))
            }
        }
    }

    /**
     * Shares invoice via email with PDF attachment.
     */
    fun shareViaEmail(context: Context, invoice: Invoice) {
        viewModelScope.launch {
            try {
                val pdfGenerator = PdfGenerator(context)
                val file = pdfGenerator.generateInvoicePdf(invoice)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "com.billcraft.app.fileprovider",
                    file
                )
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(invoice.customer?.email ?: ""))
                    putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber} from ${invoice.business?.name ?: "BillCraft"}")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Dear ${invoice.customer?.name ?: "Customer"},\n\nPlease find attached invoice ${invoice.invoiceNumber}.\n\nAmount Due: ₹${invoice.balanceDue}\n\nThank you for your business!\n\n${invoice.business?.name}"
                    )
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
            } catch (e: Exception) {
                _uiState.value = InvoiceUiState(error = "Failed to share via email: ${e.message}")
            }
        }
    }

    /**
     * Generates a new invoice number for the current year.
     */
    suspend fun generateInvoiceNumber(): String {
        val year = LocalDate.now().year
        return invoiceRepository.generateInvoiceNumber(year)
    }

    fun clearUiState() {
        _uiState.value = InvoiceUiState()
    }

    private fun buildWhatsAppMessage(invoice: Invoice): String = buildString {
        appendLine("*Invoice: ${invoice.invoiceNumber}*")
        appendLine("From: ${invoice.business?.name ?: "BillCraft"}")
        appendLine("Date: ${invoice.invoiceDate}")
        appendLine()
        appendLine("*Items:*")
        invoice.lineItems.forEach { item ->
            appendLine("• ${item.description} × ${item.quantity} = ₹${item.amount}")
        }
        appendLine()
        appendLine("*Total: ₹${invoice.totalAmount}*")
        if (invoice.balanceDue > 0) {
            appendLine("*Balance Due: ₹${invoice.balanceDue}*")
        }
        invoice.business?.upiId?.let { appendLine("UPI: $it") }
    }
}
