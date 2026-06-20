package com.billcraft.app.presentation.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.billcraft.app.domain.model.Invoice
import com.billcraft.app.domain.model.InvoiceStatus
import com.billcraft.app.domain.util.CurrencyFormatter
import com.billcraft.app.presentation.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.invoices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoices") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Invoice")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search invoices...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            // Status filter chips
            val statuses = listOf(null) + InvoiceStatus.values().toList()
            ScrollableTabRow(
                selectedTabIndex = statuses.indexOf(filterStatus),
                modifier = Modifier.fillMaxWidth()
            ) {
                statuses.forEachIndexed { index, status ->
                    Tab(
                        selected = filterStatus == status,
                        onClick = { viewModel.setFilterStatus(status) },
                        text = { Text(status?.name ?: "All") }
                    )
                }
            }

            // Invoice list
            if (invoices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No invoices found", style = MaterialTheme.typography.titleMedium)
                        if (searchQuery.isBlank()) {
                            TextButton(onClick = onNavigateToCreate) {
                                Text("Create your first invoice")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(invoices) { invoice ->
                        InvoiceListItem(
                            invoice = invoice,
                            onClick = { onNavigateToDetail(invoice.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceListItem(invoice: Invoice, onClick: () -> Unit) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.PAID -> MaterialTheme.colorScheme.primary
        InvoiceStatus.OVERDUE -> MaterialTheme.colorScheme.error
        InvoiceStatus.SENT -> MaterialTheme.colorScheme.secondary
        InvoiceStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
        InvoiceStatus.DRAFT -> MaterialTheme.colorScheme.outline
        InvoiceStatus.CANCELLED -> MaterialTheme.colorScheme.outline
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold)
                Text(invoice.customer?.name ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                Text(invoice.invoiceDate.toString(), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(invoice.totalAmount),
                    fontWeight = FontWeight.SemiBold
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(invoice.status.name, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    )
                )
            }
        }
    }
}
