package com.billcraft.app.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class SettingItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val settingsList = listOf(
        SettingItem("Business Profile", Icons.Default.Person) {},
        SettingItem("Invoice Settings", Icons.Default.List) {},
        SettingItem("Appearance", Icons.Default.Edit) {},
        SettingItem("Notifications", Icons.Default.Notifications) {},
        SettingItem("Backup & Restore", Icons.Default.Refresh) {},
        SettingItem("Rate BillCraft", Icons.Default.Star) {},
        SettingItem("Share BillCraft", Icons.Default.Share) {},
        SettingItem("Privacy Policy", Icons.Default.Lock) {},
        SettingItem("About", Icons.Default.Info) {}
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(settingsList) { item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        leadingContent = { Icon(item.icon, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = item.onClick)
                    )
                    HorizontalDivider()
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BillCraft v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
