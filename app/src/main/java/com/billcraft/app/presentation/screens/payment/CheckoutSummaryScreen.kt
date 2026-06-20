package com.billcraft.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.billcraft.app.domain.model.*
import com.billcraft.app.domain.util.AmountToWords
import com.billcraft.app.presentation.navigation.Screen
import com.billcraft.app.presentation.viewmodel.InvoiceViewModel
import com.billcraft.app.presentation.viewmodel.PaymentViewModel
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// Colour tokens (kept local to avoid theme coupling)
// ─────────────────────────────────────────────────────────────────────────────
private val GreenPaid   = Color(0xFF22C55E)
private val AmberOwed   = Color(0xFFF59E0B)
private val RedOverdue  = Color(0xFFEF4444)
private val SurfaceDark = Color(0xFF1E2640)
private val AccentBlue  = Color(0xFF6366F1)
private val CardBg      = Color(0xFF252D44)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutSummaryScreen(
    invoiceId: String,
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel(),
    invoiceViewModel: InvoiceViewModel = hiltViewModel()
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    // ── Observe state ────────────────────────────────────────────────────────
    val invoice by invoiceViewModel.selectedInvoice.collectAsState()
    val totalPaid by viewModel.totalPaid.collectAsState()
    val balanceDue by viewModel.balanceDue.collectAsState()

    // Load invoice + payments when the screen enters composition
    LaunchedEffect(invoiceId) {
        invoiceViewModel.loadInvoice(invoiceId)
        invoice?.let { viewModel.loadPayments(invoiceId, it.totalAmount) }
    }

    // When invoice becomes available, initialise payment amounts
    LaunchedEffect(invoice) {
        invoice?.let { viewModel.loadPayments(invoiceId, it.totalAmount) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Invoice Summary",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = SurfaceDark
    ) { padding ->

        if (invoice == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
            return@Scaffold
        }

        val inv = invoice!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── 1. Invoice Header ──────────────────────────────────────────
            item {
                InvoiceHeaderCard(inv = inv, dateFormatter = dateFormatter)
            }

            // ── 2. Line Items ──────────────────────────────────────────────
            item {
                SectionTitle("Line Items")
            }
            items(inv.lineItems, key = { it.id }) { item ->
                LineItemRow(item)
            }

            // ── 3. GST Breakdown ──────────────────────────────────────────
            item {
                SectionTitle("GST Breakdown")
                GstBreakdownTable(inv = inv)
            }

            // ── 4. Total Amount ───────────────────────────────────────────
            item {
                TotalsCard(
                    subtotal = inv.subtotal,
                    totalGST = inv.cgstAmount + inv.sgstAmount + inv.igstAmount,
                    grandTotal = inv.totalAmount,
                    totalPaid = totalPaid,
                    balanceDue = balanceDue
                )
            }

            // ── 5. Amount in words ────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = AmountToWords.convert(inv.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFADB5BD),
                        modifier = Modifier.padding(12.dp),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // ── 6. Action Buttons ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Record Payment button
                    Button(
                        onClick = {
                            navController.navigate(Screen.RecordPayment.createRoute(invoiceId))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Record Payment", fontWeight = FontWeight.SemiBold)
                    }

                    // Share Invoice button
                    OutlinedButton(
                        onClick = { /* TODO: trigger share/PDF intent */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccentBlue),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null,
                            modifier = Modifier.size(18.dp), tint = AccentBlue)
                        Spacer(Modifier.width(6.dp))
                        Text("Share Invoice", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InvoiceHeaderCard(inv: Invoice, dateFormatter: DateTimeFormatter) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = inv.invoiceNumber,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = inv.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBlue
                    )
                }
                StatusBadge(inv.status)
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.08f))
            Spacer(Modifier.height(12.dp))

            InfoRow("Customer", inv.customer?.displayName ?: "—")
            InfoRow("Invoice Date", inv.invoiceDate.format(dateFormatter))
            inv.dueDate?.let { InfoRow("Due Date", it.format(dateFormatter)) }
        }
    }
}

@Composable
private fun StatusBadge(status: InvoiceStatus) {
    val (bg, label) = when (status) {
        InvoiceStatus.PAID      -> Color(0xFF14532D) to "PAID"
        InvoiceStatus.PARTIAL   -> Color(0xFF78350F) to "PARTIAL"
        InvoiceStatus.OVERDUE   -> Color(0xFF7F1D1D) to "OVERDUE"
        InvoiceStatus.SENT      -> Color(0xFF1E3A5F) to "SENT"
        InvoiceStatus.DRAFT     -> Color(0xFF374151) to "DRAFT"
        InvoiceStatus.CANCELLED -> Color(0xFF3F3F46) to "CANCELLED"
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF))
        Text(value, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium, color = Color.White,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF9CA3AF),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun LineItemRow(item: LineItem) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.description, color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
                item.hsnCode?.let {
                    Text("HSN: $it", style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF))
                }
                Text(
                    text = "${item.quantity} ${item.unit} × ₹${String.format("%.2f", item.pricePerUnit)}" +
                           if (item.discountPercent > 0) " (${item.discountPercent}% off)" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", item.taxableAmount)}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "GST ${item.gstRate.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentBlue
                )
            }
        }
    }
}

@Composable
private fun GstBreakdownTable(inv: Invoice) {
    val isInterState = inv.isInterState
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row
            Row(modifier = Modifier.fillMaxWidth()) {
                TableCell("GST %", weight = 1f, isHeader = true)
                TableCell("Taxable", weight = 2f, isHeader = true, textAlign = TextAlign.End)
                if (isInterState) {
                    TableCell("IGST", weight = 2f, isHeader = true, textAlign = TextAlign.End)
                } else {
                    TableCell("CGST", weight = 2f, isHeader = true, textAlign = TextAlign.End)
                    TableCell("SGST", weight = 2f, isHeader = true, textAlign = TextAlign.End)
                }
                TableCell("Total", weight = 2f, isHeader = true, textAlign = TextAlign.End)
            }

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

            // Build breakdown from line items grouped by rate
            val grouped = inv.lineItems.groupBy { it.gstRate }
            grouped.entries.sortedBy { it.key }.forEach { (rate, items) ->
                val taxable = items.sumOf { it.taxableAmount }
                val gstTotal = items.sumOf { it.gstAmount }
                val halfGst = gstTotal / 2
                Row(modifier = Modifier.fillMaxWidth()) {
                    TableCell("${rate.toInt()}%", weight = 1f)
                    TableCell("₹${String.format("%.2f", taxable)}", weight = 2f, textAlign = TextAlign.End)
                    if (isInterState) {
                        TableCell("₹${String.format("%.2f", gstTotal)}", weight = 2f, textAlign = TextAlign.End)
                    } else {
                        TableCell("₹${String.format("%.2f", halfGst)}", weight = 2f, textAlign = TextAlign.End)
                        TableCell("₹${String.format("%.2f", halfGst)}", weight = 2f, textAlign = TextAlign.End)
                    }
                    TableCell("₹${String.format("%.2f", gstTotal)}", weight = 2f, textAlign = TextAlign.End)
                }
            }

            Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 6.dp))

            // Totals row
            val totalGst = inv.cgstAmount + inv.sgstAmount + inv.igstAmount
            Row(modifier = Modifier.fillMaxWidth()) {
                TableCell("Total", weight = 1f, isHeader = true)
                TableCell("₹${String.format("%.2f", inv.subtotal)}", weight = 2f,
                    isHeader = true, textAlign = TextAlign.End)
                if (isInterState) {
                    TableCell("₹${String.format("%.2f", inv.igstAmount)}", weight = 2f,
                        isHeader = true, textAlign = TextAlign.End)
                } else {
                    TableCell("₹${String.format("%.2f", inv.cgstAmount)}", weight = 2f,
                        isHeader = true, textAlign = TextAlign.End)
                    TableCell("₹${String.format("%.2f", inv.sgstAmount)}", weight = 2f,
                        isHeader = true, textAlign = TextAlign.End)
                }
                TableCell("₹${String.format("%.2f", totalGst)}", weight = 2f,
                    isHeader = true, textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) Color.White else Color(0xFFD1D5DB),
        textAlign = textAlign
    )
}

@Composable
private fun TotalsCard(
    subtotal: Double,
    totalGST: Double,
    grandTotal: Double,
    totalPaid: Double,
    balanceDue: Double
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TotalRow("Subtotal", "₹${String.format("%.2f", subtotal)}", Color(0xFFD1D5DB))
            TotalRow("Total GST", "₹${String.format("%.2f", totalGST)}", Color(0xFFD1D5DB))

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

            TotalRow(
                label = "Grand Total",
                value = "₹${String.format("%.2f", grandTotal)}",
                valueColor = Color.White,
                isBold = true,
                fontSize = 18.sp
            )

            if (totalPaid > 0.0) {
                Spacer(Modifier.height(4.dp))
                TotalRow(
                    label = "Amount Paid",
                    value = "−₹${String.format("%.2f", totalPaid)}",
                    valueColor = GreenPaid,
                    isBold = true
                )
            }

            if (balanceDue > 0.0) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (balanceDue == grandTotal) RedOverdue.copy(alpha = 0.15f)
                    else AmberOwed.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TotalRow(
                        label = "Balance Due",
                        value = "₹${String.format("%.2f", balanceDue)}",
                        valueColor = if (balanceDue == grandTotal) RedOverdue else AmberOwed,
                        isBold = true,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalRow(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize),
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}
