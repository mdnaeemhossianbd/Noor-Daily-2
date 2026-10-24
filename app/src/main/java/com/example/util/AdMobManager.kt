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
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.graphics.Typeface
import android.graphics.Color as AndroidColor

object AdMobManager {
  const val APP_ID = "ca-app-pub-6596350151078116~4467168043"
  const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-6596350151078116/5690593680"
  const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6596350151078116/9629838690"
  const val REWARDED_AD_UNIT_ID = "ca-app-pub-6596350151078116/2848807925"
  const val NATIVE_ADVANCED_AD_UNIT_ID = "ca-app-pub-6596350151078116/4193102198"
  // Production Banner Ad Unit ID supplied by the user.
  const val BANNER_AD_UNIT_ID = "ca-app-pub-6596350151078116/6135511890"
  const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

  @Volatile private var initialized = false
  private var interstitialAd: InterstitialAd? = null
  private var rewardedAd: RewardedAd? = null
  private var appOpenAd: AppOpenAd? = null
  private var appOpenLoading = false
  private var lastAppOpenShownAt = 0L
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
      loadAppOpen(context.applicationContext)
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
  fun loadAppOpen(context: Context) {
    if (appOpenLoading || appOpenAd != null) return
    appOpenLoading = true
    val unitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395921" else APP_OPEN_AD_UNIT_ID
    AppOpenAd.load(context, unitId, AdRequest.Builder().build(), object : AppOpenAd.AppOpenAdLoadCallback() {
      override fun onAdLoaded(ad: AppOpenAd) {
        appOpenAd = ad
        appOpenLoading = false
      }
      override fun onAdFailedToLoad(error: LoadAdError) {
        appOpenAd = null
        appOpenLoading = false
      }
    })
  }

  fun showAppOpenIfAvailable(activity: Activity) {
    val now = System.currentTimeMillis()
    if (now - lastAppOpenShownAt < 240_000L) return
    val ad = appOpenAd
    if (ad == null) {
      loadAppOpen(activity.applicationContext)
      return
    }
    appOpenAd = null
    lastAppOpenShownAt = now
    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() { loadAppOpen(activity.applicationContext) }
      override fun onAdFailedToShowFullScreenContent(error: AdError) { loadAppOpen(activity.applicationContext) }
    }
    ad.show(activity)
  }

}


@Composable
fun NativeAdvancedAdCard(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      val adView = NativeAdView(ctx)
      val column = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 18, 24, 18)
        setBackgroundColor(AndroidColor.rgb(245, 247, 245))
      }
      val label = TextView(ctx).apply {
        text = "ADVERTISEMENT"
        textSize = 10f
        setTextColor(AndroidColor.DKGRAY)
      }
      val headline = TextView(ctx).apply {
        textSize = 16f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(AndroidColor.BLACK)
        setPadding(0, 6, 0, 4)
      }
      val body = TextView(ctx).apply {
        textSize = 13f
        setTextColor(AndroidColor.DKGRAY)
        setPadding(0, 0, 0, 8)
      }
      val cta = TextView(ctx).apply {
        textSize = 13f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(AndroidColor.rgb(0, 105, 75))
        gravity = Gravity.END
      }
      column.addView(label)
      column.addView(headline)
      column.addView(body)
      column.addView(cta)
      adView.addView(column)
      adView.headlineView = headline
      adView.bodyView = body
      adView.callToActionView = cta
      val unitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else AdMobManager.NATIVE_ADVANCED_AD_UNIT_ID
      val loader = AdLoader.Builder(ctx, unitId)
        .forNativeAd { nativeAd: NativeAd ->
          adView.headlineView?.let { (it as TextView).text = nativeAd.headline }
          adView.bodyView?.let {
            (it as TextView).text = nativeAd.body ?: ""
            it.visibility = if (nativeAd.body.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
          }
          adView.callToActionView?.let {
            (it as TextView).text = nativeAd.callToAction ?: ""
            it.visibility = if (nativeAd.callToAction.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
          }
          adView.setNativeAd(nativeAd)
        }
        .build()
      loader.loadAd(AdRequest.Builder().build())
      adView
    }
  )
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  if (!BuildConfig.DEBUG && AdMobManager.BANNER_AD_UNIT_ID == "REPLACE_WITH_YOUR_BANNER_AD_UNIT_ID") return
  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      AdView(ctx).apply {
        setAdSize(AdSize.BANNER)
        adUnitId = if (BuildConfig.DEBUG) AdMobManager.BANNER_TEST_AD_UNIT_ID else AdMobManager.BANNER_AD_UNIT_ID
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadAd(AdRequest.Builder().build())
      }
    },
    update = { view -> view.visibility = android.view.View.VISIBLE }
  )
}
