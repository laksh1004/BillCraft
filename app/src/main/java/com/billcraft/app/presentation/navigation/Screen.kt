package com.billcraft.app.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object InvoiceList : Screen("invoices")
    object CreateInvoice : Screen("invoices/create")
    object InvoiceDetail : Screen("invoices/{invoiceId}") {
        fun createRoute(id: String) = "invoices/$id"
    }
    object EditInvoice : Screen("invoices/{invoiceId}/edit") {
        fun createRoute(id: String) = "invoices/$id/edit"
    }
    object CustomerList : Screen("customers")
    object CustomerDetail : Screen("customers/{customerId}") {
        fun createRoute(id: String) = "customers/$id"
    }
    object AddCustomer : Screen("customers/add")
    object RecordPayment : Screen("payment/{invoiceId}") {
        fun createRoute(id: String) = "payment/$id"
    }
    object Settings : Screen("settings")
    object BusinessProfile : Screen("business-profile")
    object CheckoutSummary : Screen("checkout/{invoiceId}") {
        fun createRoute(id: String) = "checkout/$id"
    }
}
