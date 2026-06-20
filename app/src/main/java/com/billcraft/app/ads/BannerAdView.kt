package com.billcraft.app.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Composable wrapper around AdMob's [AdView].
 *
 * Usage:
 * ```kotlin
 * // At the bottom of a scaffold / column:
 * BannerAdView(modifier = Modifier.fillMaxWidth())
 * ```
 *
 * The view automatically loads an ad as soon as it is attached to the window.
 * Pass [adUnitId] explicitly in production (swap out the default test ID).
 *
 * @param adUnitId  AdMob ad unit ID.  Defaults to the Google test ID.
 * @param adSize    AdMob [AdSize].  Defaults to [AdSize.BANNER] (320×50 dp).
 * @param modifier  Standard Compose modifier applied to the wrapping [AndroidView].
 * @param onAdLoaded     Optional callback when the ad loads successfully.
 * @param onAdFailedToLoad Optional callback when the ad fails to load.
 */
@Composable
fun BannerAdView(
    adUnitId: String = AdConstants.BANNER_AD_UNIT_ID,
    adSize: AdSize = AdSize.BANNER,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((LoadAdError) -> Unit)? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize)
                this.adUnitId = adUnitId

                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        onAdLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        onAdFailedToLoad?.invoke(error)
                    }
                }

                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // If the composable is recomposed with a different adUnitId,
            // reload the ad with the new unit.
            if (adView.adUnitId != adUnitId) {
                adView.adUnitId = adUnitId
                adView.loadAd(AdRequest.Builder().build())
            }
        }
    )
}
