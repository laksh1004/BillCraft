package com.billcraft.app.presentation.ui.invoice

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.billcraft.app.domain.model.InvoiceStatus
import com.billcraft.app.domain.util.CurrencyFormatter
import com.billcraft.app.domain.util.GSTCalculator
import com.billcraft.app.presentation.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    onBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToPayment: (String) -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoice by viewModel.selectedInvoice.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(invoice?.invoiceNumber ?: "Invoice Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    invoice?.let { inv ->
                        IconButton(onClick = { onNavigateToEdit(inv.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.generateAndSharePdf(context, inv) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share PDF")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (invoice == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val inv = invoice!!
        val gstSummary = remember(inv) { GSTCalculator.calculateGST(inv.lineItems, inv.isInterState) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge)
                                Text(inv.type.name, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(inv.status.name) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Date: ${inv.invoiceDate}")
                        inv.dueDate?.let { Text("Due: $it") }
                    }
                }
            }

            // Customer info
            item {
                inv.customer?.let { customer ->
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bill To", style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(customer.name, fontWeight = FontWeight.SemiBold)
                            customer.gstin?.let { Text("GSTIN: $it") }
                            customer.phone.let { if (it.isNotBlank()) Text("Ph: $it") }
                            customer.email?.let { Text(it) }
                        }
                    }
                }
            }

            // Line Items
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Items", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        inv.lineItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.description, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${item.quantity} ${item.unit} × ₹${item.pricePerUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    if (item.discountPercent > 0) {
                                        Text("Discount: ${item.discountPercent}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary)
                                    }
                                    Text("GST: ${item.gstRate}%", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(CurrencyFormatter.format(item.amount), fontWeight = FontWeight.SemiBold)
                            }
                            if (index < inv.lineItems.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }

            // GST Summary
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tax Summary", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        TotalRow("Subtotal", CurrencyFormatter.format(gstSummary.subtotal))
                        if (inv.isInterState) {
                            TotalRow("IGST", CurrencyFormatter.format(gstSummary.totalIGST))
                        } else {
                            TotalRow("CGST", CurrencyFormatter.format(gstSummary.totalCGST))
                            TotalRow("SGST", CurrencyFormatter.format(gstSummary.totalSGST))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        TotalRow("Grand Total", CurrencyFormatter.format(gstSummary.grandTotal), bold = true)
                        if (inv.amountPaid > 0) {
                            TotalRow("Amount Paid", CurrencyFormatter.format(inv.amountPaid))
                            TotalRow("Balance Due", CurrencyFormatter.format(inv.balanceDue), bold = true)
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (inv.status != InvoiceStatus.PAID && inv.status != InvoiceStatus.CANCELLED) {
                        Button(
                            onClick = { onNavigateToPayment(inv.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Payment")
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.shareViaWhatsApp(context, inv) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp")
                    }
                }
            }

            // Notes
            inv.notes?.let { notes ->
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notes)
                        }
                    }
                }
            }

            // Terms
            inv.termsAndConditions?.let { terms ->
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Terms & Conditions", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(terms)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
