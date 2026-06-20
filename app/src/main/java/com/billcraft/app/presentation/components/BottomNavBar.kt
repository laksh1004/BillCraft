package com.billcraft.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class NavRoute(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    INVOICES("Invoices", Icons.Default.Receipt),
    CUSTOMERS("Customers", Icons.Default.People),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    currentRoute: NavRoute,
    onRouteSelected: (NavRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavRoute.entries.forEach { route ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onRouteSelected(route) },
                icon = {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.label
                    )
                },
                label = { Text(text = route.label) }
            )
        }
    }
}
