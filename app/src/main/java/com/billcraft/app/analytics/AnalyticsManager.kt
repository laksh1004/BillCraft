package com.billcraft.app.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Centralized Firebase Analytics manager for BillCraft.
 * Call [init] once from Application.onCreate() before logging any events.
 */
object AnalyticsManager {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    fun init(context: Context) {
        firebaseAnalytics = Firebase.analytics
    }

    // -------------------------------------------------------------------------
    // Onboarding Events
    // -------------------------------------------------------------------------

    /** Fired when the user first opens the Welcome / onboarding screen. */
    fun logOnboardingStarted() {
        log("onboarding_started")
    }

    /**
     * Fired when the user completes onboarding and lands on the dashboard.
     * We deliberately avoid logging the raw business name for privacy;
     * only its length is sent.
     */
    fun logOnboardingCompleted(businessName: String) {
        log(
            "onboarding_completed",
            Bundle().apply {
                putString("business_name_length", businessName.length.toString())
            }
        )
    }

    /**
     * Fired when the business-profile setup step is finished.
     *
     * @param hasGSTIN whether the user entered a GSTIN during setup.
     */
    fun logBusinessSetupCompleted(hasGSTIN: Boolean) {
        log(
            "business_setup_completed",
            Bundle().apply {
                putBoolean("has_gstin", hasGSTIN)
            }
        )
    }

    // -------------------------------------------------------------------------
    // Invoice Events
    // -------------------------------------------------------------------------

    /**
     * Fired every time a new invoice / estimate / receipt is saved.
     *
     * @param type        "invoice" | "estimate" | "receipt"
     * @param amount      grand total in INR
     * @param itemCount   number of line items
     */
    fun logInvoiceCreated(type: String, amount: Double, itemCount: Int) {
        log(
            "invoice_created",
            Bundle().apply {
                putString("document_type", type)
                putDouble("amount_inr", amount)
                putInt("item_count", itemCount)
            }
        )
    }

    /**
     * Fired when the user shares an invoice via any channel.
     *
     * @param shareMethod "whatsapp" | "email" | "pdf" | "other"
     */
    fun logInvoiceShared(shareMethod: String) {
        log(
            "invoice_shared",
            Bundle().apply {
                putString("share_method", shareMethod)
            }
        )
    }

    /**
     * Fired when a payment is recorded against an invoice.
     *
     * @param amount      amount paid in INR
     * @param paymentMode "cash" | "upi" | "bank" | "cheque" | "card"
     */
    fun logInvoicePaid(amount: Double, paymentMode: String) {
        log(
            "invoice_paid",
            Bundle().apply {
                putDouble("amount_inr", amount)
                putString("payment_mode", paymentMode)
            }
        )
    }

    /** Fired when the user opens an invoice detail / preview screen. */
    fun logInvoiceViewed() {
        log("invoice_viewed")
    }

    // -------------------------------------------------------------------------
    // Customer Events
    // -------------------------------------------------------------------------

    /** Fired when a new customer record is saved. */
    fun logCustomerAdded() {
        log("customer_added")
    }

    // -------------------------------------------------------------------------
    // Document Creation Events
    // -------------------------------------------------------------------------

    /** Fired when an estimate / quotation is created. */
    fun logEstimateCreated() {
        log("estimate_created")
    }

    /** Fired when a receipt / payment receipt document is created. */
    fun logReceiptCreated() {
        log("receipt_created")
    }

    // -------------------------------------------------------------------------
    // PDF & QR Events
    // -------------------------------------------------------------------------

    /** Fired when a PDF is generated (download or share). */
    fun logPdfGenerated() {
        log("pdf_generated")
    }

    /** Fired when the user opens the UPI QR code view on an invoice. */
    fun logQrCodeViewed() {
        log("qr_code_viewed")
    }

    // -------------------------------------------------------------------------
    // Search Events
    // -------------------------------------------------------------------------

    /**
     * Fired when the user performs a search.
     * Only the length of the query is logged, not the raw text.
     *
     * @param query the search string entered by the user.
     */
    fun logSearchPerformed(query: String) {
        log(
            "search_performed",
            Bundle().apply {
                putInt("query_length", query.trim().length)
            }
        )
    }

    // -------------------------------------------------------------------------
    // User Properties
    // -------------------------------------------------------------------------

    /**
     * Sets the user's state (Indian state abbreviation, e.g. "MH", "KA").
     * Used to segment revenue and usage by geography.
     */
    fun setUserState(state: String) {
        firebaseAnalytics?.setUserProperty("user_state", state)
    }

    /**
     * Marks whether the user's business has a GSTIN registered.
     */
    fun setHasGSTIN(hasGSTIN: Boolean) {
        firebaseAnalytics?.setUserProperty("has_gstin", hasGSTIN.toString())
    }

    /**
     * Sets the broad business type for segmentation.
     *
     * @param type "shop" | "freelancer" | "contractor" | "trader" | "other"
     */
    fun setBusinessType(type: String) {
        firebaseAnalytics?.setUserProperty("business_type", type)
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun log(eventName: String, params: Bundle? = null) {
        firebaseAnalytics?.logEvent(eventName, params)
    }
}
