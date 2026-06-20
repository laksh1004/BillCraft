package com.billcraft.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val TAG = "InterstitialAdManager"

/**
 * Manages loading and display of AdMob interstitial ads for BillCraft.
 *
 * Lifecycle:
 *  1. Instantiate once (e.g. inside a ViewModel or Activity).
 *  2. Call [onInvoiceCreated] each time the user saves an invoice.
 *  3. The manager automatically shows an interstitial every
 *     [AdConstants.AD_TRIGGER_INTERVAL] invoice-creation events.
 *  4. After every impression the manager pre-fetches the next ad.
 *
 * Thread safety: All AdMob calls must happen on the main thread.
 * This class does not marshal to the main thread itself — the caller is
 * responsible for calling its methods from the UI thread.
 *
 * @param context Any [Context]; the Application context is preferred to
 *                avoid Activity leaks.
 */
class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var invoiceCreatedCount = 0
    private var isLoading = false
    private var retryCount = 0
    private var lastShownTimeMs = 0L

    init {
        loadAd()
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Call this every time the user successfully creates an invoice.
     * The manager will show an interstitial when the trigger interval is hit.
     *
     * @param activity The currently-visible [Activity] (required by AdMob for
     *                 full-screen content).
     */
    fun onInvoiceCreated(activity: Activity) {
        invoiceCreatedCount++
        Log.d(TAG, "Invoice created. Count=$invoiceCreatedCount")

        if (invoiceCreatedCount % AdConstants.AD_TRIGGER_INTERVAL == 0) {
            showAd(activity) {
                // Pre-fetch the next ad immediately after the current one is dismissed.
                retryCount = 0
                loadAd()
            }
        }
    }

    /**
     * Attempts to show the pre-loaded interstitial.
     *
     * If no ad is ready (still loading or failed to load), the function returns
     * silently and [onAdDismissed] is invoked immediately so the caller's flow
     * is not blocked.
     *
     * A minimum interval of [AdConstants.MIN_INTERSTITIAL_INTERVAL_MS] is
     * enforced between consecutive impressions to maintain a good UX.
     *
     * @param activity      The currently-visible [Activity].
     * @param onAdDismissed Callback invoked when the ad closes (or immediately
     *                      if no ad was shown).
     */
    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastShownTimeMs

        if (timeSinceLast < AdConstants.MIN_INTERSTITIAL_INTERVAL_MS) {
            Log.d(TAG, "Skipping ad — minimum interval not yet elapsed.")
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.w(TAG, "No interstitial ad loaded yet; skipping.")
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed.")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Failed to show interstitial: ${error.message}")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial shown.")
                lastShownTimeMs = System.currentTimeMillis()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Interstitial impression recorded.")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Interstitial clicked.")
            }
        }

        ad.show(activity)
    }

    // -------------------------------------------------------------------------
    // Internal — ad loading
    // -------------------------------------------------------------------------

    /**
     * Pre-fetches an interstitial ad from the AdMob network.
     * Retries up to [AdConstants.MAX_RETRY_ATTEMPTS] times on failure.
     */
    fun loadAd() {
        if (isLoading || interstitialAd != null) return

        isLoading = true
        Log.d(TAG, "Loading interstitial ad… (attempt ${retryCount + 1})")

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AdConstants.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                    interstitialAd = ad
                    isLoading = false
                    retryCount = 0
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial load failed: ${loadAdError.message}")
                    interstitialAd = null
                    isLoading = false

                    if (retryCount < AdConstants.MAX_RETRY_ATTEMPTS) {
                        retryCount++
                        Log.d(TAG, "Retrying ad load ($retryCount/${AdConstants.MAX_RETRY_ATTEMPTS})…")
                        loadAd()
                    } else {
                        Log.w(TAG, "Max retry attempts reached. Giving up for this session.")
                    }
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // Helpers / state accessors
    // -------------------------------------------------------------------------

    /** Returns true if an ad is currently pre-loaded and ready to display. */
    val isAdReady: Boolean
        get() = interstitialAd != null

    /** Resets the invoice counter (e.g. after a fresh install or session). */
    fun resetCounter() {
        invoiceCreatedCount = 0
    }
}
