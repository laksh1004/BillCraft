package com.billcraft.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.billcraft.app.presentation.screens.onboarding.BusinessSetupScreen
import com.billcraft.app.presentation.screens.customer.AddEditCustomerScreen
import com.billcraft.app.presentation.screens.customer.CustomerDetailScreen
import com.billcraft.app.presentation.screens.customer.CustomerListScreen
import com.billcraft.app.presentation.ui.dashboard.DashboardScreen
import com.billcraft.app.presentation.screens.invoice.CreateInvoiceScreen
import com.billcraft.app.presentation.ui.invoice.InvoiceDetailScreen
import com.billcraft.app.presentation.ui.invoice.InvoiceListScreen
import com.billcraft.app.presentation.screens.onboarding.WelcomeScreen
import com.billcraft.app.presentation.screens.payment.RecordPaymentScreen
import com.billcraft.app.presentation.screens.settings.SettingsScreen
import com.billcraft.app.presentation.screens.payment.CheckoutSummaryScreen

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

private val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Filled.Dashboard, Screen.Dashboard),
    BottomNavItem("Invoices", Icons.Filled.Receipt, Screen.InvoiceList),
    BottomNavItem("Customers", Icons.Filled.People, Screen.CustomerList),
    BottomNavItem("Settings", Icons.Filled.Settings, Screen.Settings)
)

private val bottomNavRoutes = setOf(
    Screen.Dashboard.route,
    Screen.InvoiceList.route,
    Screen.CustomerList.route,
    Screen.Settings.route
)

@Composable
fun BillCraftApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        BillCraftNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BillCraftNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onContinue = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToInvoices = { navController.navigate(Screen.InvoiceList.route) },
                onNavigateToCreateInvoice = { navController.navigate(Screen.CreateInvoice.route) },
                onNavigateToInvoiceDetail = { id ->
                    navController.navigate(Screen.InvoiceDetail.createRoute(id))
                }
            )
        }

        composable(Screen.InvoiceList.route) {
            InvoiceListScreen(
                onNavigateToCreate = { navController.navigate(Screen.CreateInvoice.route) },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.InvoiceDetail.createRoute(id))
                }
            )
        }

        composable(Screen.CreateInvoice.route) {
            CreateInvoiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
            InvoiceDetailScreen(
                invoiceId = invoiceId,
                onBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditInvoice.createRoute(id))
                },
                onNavigateToPayment = { id ->
                    navController.navigate(Screen.RecordPayment.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditInvoice.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { 
            CreateInvoiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerList.route) {
            CustomerListScreen(
                onAddCustomerClick = { navController.navigate(Screen.AddCustomer.route) },
                onCustomerClick = { 
                    navController.navigate(Screen.CustomerDetail.createRoute("dummy"))
                }
            )
        }

        composable(Screen.AddCustomer.route) {
            AddEditCustomerScreen(
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { 
            CustomerDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RecordPayment.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { 
            RecordPaymentScreen(
                onRecordPayment = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.BusinessProfile.route) {
            BusinessSetupScreen(
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CheckoutSummary.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
            CheckoutSummaryScreen(
                invoiceId = invoiceId,
                navController = navController
            )
        }
    }
}
