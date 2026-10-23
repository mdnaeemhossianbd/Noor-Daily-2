package com.example.util

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
  const val APP_ID = "ca-app-pub-6596350151078116~4467168043"
  const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-6596350151078116/5690593680"
  const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6596350151078116/9629838690"
  const val REWARDED_AD_UNIT_ID = "ca-app-pub-6596350151078116/2848807925"
  const val NATIVE_ADVANCED_AD_UNIT_ID = "ca-app-pub-6596350151078116/4193102198"
  // No production Banner unit ID was supplied; use Google's test unit until one is created.
  const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

  @Volatile private var initialized = false
  private var interstitialAd: InterstitialAd? = null
  private var rewardedAd: RewardedAd? = null
  private var lastInterstitialShownAt = 0L
  private var interstitialsShownThisSession = 0

  fun initialize(context: Context) {
    if (initialized) return
    synchronized(this) {
      if (initialized) return
      MobileAds.initialize(context.applicationContext)
      initialized = true
      loadInterstitial(context.applicationContext)
      loadRewarded(context.applicationContext)
    }
  }

  fun loadInterstitial(context: Context) {
    val unitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else INTERSTITIAL_AD_UNIT_ID
    InterstitialAd.load(context, unitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
      override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
      override fun onAdFailedToLoad(error: LoadAdError) { interstitialAd = null }
    })
  }

  fun showInterstitialIfEligible(activity: Activity) {
    val now = System.currentTimeMillis()
    if (interstitialsShownThisSession >= 3 || now - lastInterstitialShownAt < 180_000L) return
    val ad = interstitialAd ?: run { loadInterstitial(activity.applicationContext); return }
    interstitialAd = null
    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() { loadInterstitial(activity.applicationContext) }
      override fun onAdFailedToShowFullScreenContent(error: AdError) { loadInterstitial(activity.applicationContext) }
    }
    lastInterstitialShownAt = now
    interstitialsShownThisSession++
    ad.show(activity)
  }

  fun loadRewarded(context: Context) {
    val unitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else REWARDED_AD_UNIT_ID
    RewardedAd.load(context, unitId, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
      override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
      override fun onAdFailedToLoad(error: LoadAdError) { rewardedAd = null }
    })
  }

  fun showRewarded(activity: Activity, onRewardEarned: () -> Unit) {
    val ad = rewardedAd ?: run { loadRewarded(activity.applicationContext); return }
    rewardedAd = null
    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() { loadRewarded(activity.applicationContext) }
      override fun onAdFailedToShowFullScreenContent(error: AdError) { loadRewarded(activity.applicationContext) }
    }
    ad.show(activity) { onRewardEarned() }
  }
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      AdView(ctx).apply {
        setAdSize(AdSize.BANNER)
        adUnitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else AdMobManager.BANNER_TEST_AD_UNIT_ID
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadAd(AdRequest.Builder().build())
      }
    },
    update = { view -> view.visibility = android.view.View.VISIBLE }
  )
}
