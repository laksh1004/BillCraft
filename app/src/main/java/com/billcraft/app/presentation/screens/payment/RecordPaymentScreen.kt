package com.billcraft.app.presentation.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreen(
    onRecordPayment: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Cash") }
    val paymentModes = listOf("Cash", "UPI", "Bank Transfer", "Cheque", "Card")
    var modeExpanded by remember { mutableStateOf(false) }

    var upiTxnId by remember { mutableStateOf("") }
    var chequeNumber by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2024-05-20") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Payment") }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Record Payment")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Invoice Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Invoice #: INV-2024-101")
                    Text("Customer: Acme Corp")
                    Text("Total Amount: ₹15,000.00")
                    Text("Pending: ₹15,000.00", color = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount Received") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedMode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = modeExpanded,
                    onDismissRequest = { modeExpanded = false }
                ) {
                    paymentModes.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode) },
                            onClick = {
                                selectedMode = mode
                                modeExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedMode == "UPI") {
                OutlinedTextField(
                    value = upiTxnId,
                    onValueChange = { upiTxnId = it },
                    label = { Text("UPI Transaction ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (selectedMode == "Cheque") {
                OutlinedTextField(
                    value = chequeNumber,
                    onValueChange = { chequeNumber = it },
                    label = { Text("Cheque Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Payment Date") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}
