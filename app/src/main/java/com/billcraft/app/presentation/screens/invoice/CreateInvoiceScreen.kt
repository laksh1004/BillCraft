package com.billcraft.app.presentation.screens.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    onNavigateBack: () -> Unit
) {
    var selectedSegment by remember { mutableIntStateOf(0) }
    val segments = listOf("Invoice", "Estimate", "Receipt")
    
    var date by remember { mutableStateOf("2024-05-20") }
    var customer by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create ${segments[selectedSegment]}") }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onNavigateBack, modifier = Modifier.weight(1f).padding(8.dp)) {
                        Text("Cancel")
                    }
                    Button(onClick = { /* Save */ }, modifier = Modifier.weight(1f).padding(8.dp)) {
                        Text("Save")
                    }
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
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                segments.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = segments.size),
                        onClick = { selectedSegment = index },
                        selected = index == selectedSegment
                    ) {
                        Text(label)
                    }
                }
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = customer,
                onValueChange = { customer = it },
                label = { Text("Select Customer") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Text("Line Items", style = MaterialTheme.typography.titleMedium)
            
            Button(onClick = { /* Add Item */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Item")
            }

            HorizontalDivider()
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GST Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal")
                        Text("₹0.00")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CGST")
                        Text("₹0.00")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SGST")
                        Text("₹0.00")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text("₹0.00", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
