package com.billcraft.app.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.billcraft.app.domain.model.Invoice
import com.billcraft.app.domain.model.InvoiceStatus
import com.billcraft.app.domain.util.CurrencyFormatter
import com.billcraft.app.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToInvoices: () -> Unit,
    onNavigateToCreateInvoice: () -> Unit,
    onNavigateToInvoiceDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val pendingAmount by viewModel.pendingAmount.collectAsState()
    val overdueCount by viewModel.overdueCount.collectAsState()
    val recentInvoices by viewModel.recentInvoices.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BillCraft", fontWeight = FontWeight.Bold)
                        Text("Dashboard", style = MaterialTheme.typography.bodySmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New Invoice") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onNavigateToCreateInvoice,
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // ── Summary Cards ───────────────────────────────────────────
            item {
                Text(
                    "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.TrendingUp,
                        label = "Total Revenue",
                        value = CurrencyFormatter.formatCompact(totalRevenue),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Pending,
                        label = "Pending",
                        value = CurrencyFormatter.formatCompact(pendingAmount),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (overdueCount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer)
                            Column {
                                Text(
                                    "$overdueCount Overdue Invoice${if (overdueCount > 1) "s" else ""}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Action required",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = onNavigateToInvoices) {
                                Text("View")
                            }
                        }
                    }
                }
            }

            // ── Recent Invoices ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Invoices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToInvoices) {
                        Text("View All")
                    }
                }
            }

            if (recentInvoices.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No invoices yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "Create your first invoice to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(recentInvoices) { invoice ->
                    InvoiceSummaryCard(
                        invoice = invoice,
                        onClick = { onNavigateToInvoiceDetail(invoice.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
            Text(label, style = MaterialTheme.typography.bodySmall, color = contentColor)
        }
    }
}

@Composable
private fun InvoiceSummaryCard(invoice: Invoice, onClick: () -> Unit) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold)
                Text(
                    invoice.customer?.name ?: "Unknown Customer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    invoice.invoiceDate.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(invoice.totalAmount),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Badge(containerColor = statusColor) {
                    Text(invoice.status.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
