import 'package:flutter/foundation.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'remote_config_service.dart';

class AdMobService {
  static final AdMobService _instance = AdMobService._internal();
  factory AdMobService() => _instance;
  AdMobService._internal();

  final RemoteConfigService _remoteConfig = RemoteConfigService();

  // Official Google AdMob Test Ad Unit IDs
  static const String testBannerId = 'ca-app-pub-3940256099942544/6300978111';
  static const String testInterstitialId = 'ca-app-pub-6596350151078116/9629838690';

  InterstitialAd? _interstitialAd;
  bool _isInterstitialLoading = false;
  DateTime? _lastInterstitialShownTime;
  int _articlesViewedSinceLastAd = 0;

  Future<void> init() async {
    await MobileAds.instance.initialize();
    _loadInterstitialAd();
  }

  void _loadInterstitialAd() {
    if (!_remoteConfig.interstitialEnabled || _isInterstitialLoading) return;

    _isInterstitialLoading = true;
    InterstitialAd.load(
      adUnitId: testInterstitialId,
      request: const AdRequest(),
      adLoadCallback: InterstitialAdLoadCallback(
        onAdLoaded: (ad) {
          _interstitialAd = ad;
          _isInterstitialLoading = false;
          _setupInterstitialCallbacks(ad);
        },
        onAdFailedToLoad: (error) {
          debugPrint('Interstitial failed to load: $error');
          _interstitialAd = null;
          _isInterstitialLoading = false;
        },
      ),
    );
  }

  void _setupInterstitialCallbacks(InterstitialAd ad) {
    ad.fullScreenContentCallback = FullScreenContentCallback(
      onAdDismissedFullScreenContent: (ad) {
        ad.dispose();
        _interstitialAd = null;
        _loadInterstitialAd();
      },
      onAdFailedToShowFullScreenContent: (ad, error) {
        debugPrint('Failed to show interstitial: $error');
        ad.dispose();
        _interstitialAd = null;
        _loadInterstitialAd();
      },
    );
  }

  // Frequency-capped and cooldown-guarded interstitial display
  void showInterstitialIfEligible({required bool isSacredScreen}) {
    // 1. Strict policy: Never show ads on sacred screens (prayer times, Quran reader)
    if (isSacredScreen) return;

    // 2. Remote Config kill-switch check
    if (!_remoteConfig.interstitialEnabled) return;

    _articlesViewedSinceLastAd++;

    // 3. Frequency cap check
    final frequencyCap = _remoteConfig.interstitialFrequencyCap;
    if (_articlesViewedSinceLastAd < frequencyCap) return;

    // 4. Cooldown seconds check
    if (_lastInterstitialShownTime != null) {
      final elapsed = DateTime.now().difference(_lastInterstitialShownTime!).inSeconds;
      if (elapsed < _remoteConfig.interstitialCooldownSeconds) return;
    }

    // 5. Show ad if ready
    if (_interstitialAd != null) {
      _interstitialAd!.show();
      _lastInterstitialShownTime = DateTime.now();
      _articlesViewedSinceLastAd = 0;
    } else {
      _loadInterstitialAd();
    }
  }

  // Create and load a BannerAd
  BannerAd createBannerAd({
    required Function() onLoaded,
    required Function(LoadAdError) onFailed,
  }) {
    return BannerAd(
      adUnitId: testBannerId,
      size: AdSize.banner,
      request: const AdRequest(),
      listener: BannerAdListener(
        onAdLoaded: (_) => onLoaded(),
        onAdFailedToLoad: (_, error) {
          debugPrint('Banner load failure: $error');
          onFailed(error);
        },
      ),
    )..load();
  }
}
