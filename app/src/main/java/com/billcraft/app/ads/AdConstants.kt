package com.billcraft.app.ads

/**
 * Centralised AdMob constants for BillCraft.
 *
 * ⚠️  IMPORTANT: The IDs listed under "Test Ad Unit IDs" are Google's official
 *     test IDs. They are safe during development and will never charge real
 *     advertisers or generate real revenue.
 *
 *     Before publishing to the Play Store:
 *       1. Create your AdMob account at https://admob.google.com
 *       2. Register BillCraft as an app inside AdMob.
 *       3. Create one Ad Unit per type (Banner, Interstitial, Rewarded).
 *       4. Replace the production ID placeholders below with your real IDs.
 *       5. Also update the AdMob App ID in AndroidManifest.xml.
 */
object AdConstants {

    // -------------------------------------------------------------------------
    // Test Ad Unit IDs  — safe for development / CI
    // -------------------------------------------------------------------------

    /** Google-provided test banner ad unit. */
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /** Google-provided test interstitial ad unit. */
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    /** Google-provided test rewarded ad unit. */
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // -------------------------------------------------------------------------
    // Production Ad Unit IDs  — uncomment and populate before release
    // -------------------------------------------------------------------------

    // const val BANNER_AD_UNIT_ID       = "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_UNIT_ID"
    // const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_INTERSTITIAL_UNIT_ID"
    // const val REWARDED_AD_UNIT_ID     = "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_REWARDED_UNIT_ID"

    // -------------------------------------------------------------------------
    // Ad Behaviour Configuration
    // -------------------------------------------------------------------------

    /**
     * Number of invoice-creation events between consecutive interstitial ads.
     * E.g. a value of 5 means an interstitial is shown after every 5th invoice.
     * Increase this value if user-feedback indicates the ads feel intrusive.
     */
    const val AD_TRIGGER_INTERVAL = 5

    /**
     * Maximum number of times the SDK will attempt to reload a failed ad
     * before giving up for that session.
     */
    const val MAX_RETRY_ATTEMPTS = 3

    /**
     * Minimum delay (in milliseconds) between two successive interstitial
     * impressions to avoid a poor user experience.
     */
    const val MIN_INTERSTITIAL_INTERVAL_MS = 60_000L // 1 minute
}
