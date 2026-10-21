import 'package:flutter/foundation.dart';
import 'package:firebase_remote_config/firebase_remote_config.dart';

class RemoteConfigService {
  static final RemoteConfigService _instance = RemoteConfigService._internal();
  factory RemoteConfigService() => _instance;
  RemoteConfigService._internal();

  final FirebaseRemoteConfig _remoteConfig = FirebaseRemoteConfig.instance;

  // AdMob parameters with safe defaults (disabled by default during development)
  bool get bannerAdsEnabled => _remoteConfig.getBool('banner_ads_enabled');
  bool get interstitialEnabled => _remoteConfig.getBool('interstitial_enabled');
  bool get rewardedEnabled => _remoteConfig.getBool('rewarded_enabled');
  int get interstitialFrequencyCap => _remoteConfig.getInt('interstitial_frequency_cap');
  int get interstitialCooldownSeconds => _remoteConfig.getInt('interstitial_cooldown_seconds');

  Future<void> init() async {
    try {
      await _remoteConfig.setConfigSettings(
        RemoteConfigSettings(
          fetchTimeout: const Duration(seconds: 15),
          minimumFetchInterval: kDebugMode ? Duration.zero : const Duration(hours: 1),
        ),
      );

      // Safe defaults
      await _remoteConfig.setDefaults(<String, dynamic>{
        'banner_ads_enabled': false,
        'interstitial_enabled': false,
        'rewarded_enabled': false,
        'interstitial_frequency_cap': 3,
        'interstitial_cooldown_seconds': 180,
      });

      await _remoteConfig.fetchAndActivate();
      debugPrint('Remote Config loaded: banners=${bannerAdsEnabled}, interstitials=${interstitialEnabled}');
    } catch (e) {
      debugPrint('Remote Config fetch error (using defaults): $e');
    }
  }
}
