package com.billcraft.app.presentation.screens.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onAddCustomerClick: () -> Unit,
    onCustomerClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCustomerClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search customers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search results
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(5) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCustomerClick() }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Customer Name $index", style = MaterialTheme.typography.titleMedium)
                            Text("GSTIN: 27AAAAA0000A1Z$index", style = MaterialTheme.typography.bodyMedium)
                            Text("Balance: ₹1000.00", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
