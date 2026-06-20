package com.billcraft.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.billcraft.app.domain.model.Payment
import com.billcraft.app.domain.model.PaymentMode
import com.billcraft.app.presentation.viewmodel.PaymentViewModel
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// Colour tokens
// ─────────────────────────────────────────────────────────────────────────────
private val BackgroundDark = Color(0xFF111827)
private val SurfaceCard    = Color(0xFF1F2937)
private val AccentIndigo   = Color(0xFF6366F1)
private val GreenSuccess   = Color(0xFF22C55E)
private val AmberWarn      = Color(0xFFF59E0B)
private val TextPrimary    = Color(0xFFF9FAFB)
private val TextSecondary  = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    invoiceId: String,
    invoiceNumber: String,
    totalInvoiced: Double,
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    val payments     by viewModel.paymentHistory.collectAsState()
    val totalPaid    by viewModel.totalPaid.collectAsState()
    val balanceDue   by viewModel.balanceDue.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.loadPayments(invoiceId, totalInvoiced)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Payment History",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            invoiceNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentIndigo
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Navigate to RecordPayment */ },
                containerColor = AccentIndigo,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Payment", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Summary Header ─────────────────────────────────────────────
            item {
                SummaryHeader(
                    totalInvoiced = totalInvoiced,
                    totalPaid = totalPaid,
                    balanceDue = balanceDue
                )
            }

            // ── Section label ──────────────────────────────────────────────
            item {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // ── Loading / empty state ──────────────────────────────────────
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentIndigo, modifier = Modifier.size(36.dp))
                    }
                }
            } else if (payments.isEmpty()) {
                item { EmptyPaymentState() }
            }

            // ── Payment entries with running balance ───────────────────────
            var runningBalance = totalInvoiced
            itemsIndexed(payments, key = { _, p -> p.id }) { _, payment ->
                runningBalance -= payment.amount
                PaymentEntryCard(
                    payment = payment,
                    runningBalance = runningBalance,
                    dateFormatter = dateFormatter
                )
            }

            // Bottom spacer for FAB
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryHeader(
    totalInvoiced: Double,
    totalPaid: Double,
    balanceDue: Double
) {
    val paymentProgress = if (totalInvoiced > 0) (totalPaid / totalInvoiced).toFloat().coerceIn(0f, 1f) else 0f

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Three stat boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    label = "Invoiced",
                    value = "₹${String.format("%.2f", totalInvoiced)}",
                    valueColor = TextPrimary
                )
                VerticalDivider(color = Color.White.copy(alpha = 0.1f))
                StatBox(
                    label = "Paid",
                    value = "₹${String.format("%.2f", totalPaid)}",
                    valueColor = GreenSuccess
                )
                VerticalDivider(color = Color.White.copy(alpha = 0.1f))
                StatBox(
                    label = "Balance",
                    value = "₹${String.format("%.2f", balanceDue)}",
                    valueColor = if (balanceDue > 0) AmberWarn else GreenSuccess
                )
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            Text(
                text = "${(paymentProgress * 100).toInt()}% Paid",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { paymentProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GreenSuccess,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun RowScope.StatBox(label: String, value: String, valueColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun VerticalDivider(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(40.dp)
            .background(color)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment Entry Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaymentEntryCard(
    payment: Payment,
    runningBalance: Double,
    dateFormatter: DateTimeFormatter
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentIndigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = payment.paymentMode.emoji(),
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Date + mode + ID
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.paymentDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = payment.modeDisplayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                payment.upiTransactionId?.let { txId ->
                    Text(
                        text = "UPI: $txId",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentIndigo
                    )
                }
                payment.chequeNumber?.let { chq ->
                    Text(
                        text = "Cheque: $chq",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentIndigo
                    )
                }
                payment.notes?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Amount + running balance
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "−₹${String.format("%.2f", payment.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreenSuccess
                )
                Text(
                    text = "Bal: ₹${String.format("%.2f", runningBalance.coerceAtLeast(0.0))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (runningBalance <= 0) GreenSuccess else AmberWarn
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyPaymentState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("💰", fontSize = 56.sp)
        Text(
            text = "No payments recorded",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Tap the + button to record a payment against this invoice.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Extension: payment mode emoji
// ─────────────────────────────────────────────────────────────────────────────

private fun PaymentMode.emoji(): String = when (this) {
    PaymentMode.CASH          -> "💵"
    PaymentMode.UPI           -> "📱"
    PaymentMode.BANK_TRANSFER -> "🏦"
    PaymentMode.CHEQUE        -> "📝"
    PaymentMode.CARD          -> "💳"
}
