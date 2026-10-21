import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/notification_service.dart';
import '../models/prayer_times_model.dart';

class NotificationProvider extends ChangeNotifier {
  final NotificationService _service = NotificationService();

  bool _isNotificationsEnabled = true;
  bool _isSoundEnabled = true;

  bool _fajrAlert = true;
  bool _dhuhrAlert = true;
  bool _asrAlert = true;
  bool _maghribAlert = true;
  bool _ishaAlert = true;

  bool get isNotificationsEnabled => _isNotificationsEnabled;
  bool get isSoundEnabled => _isSoundEnabled;
  bool get fajrAlert => _fajrAlert;
  bool get dhuhrAlert => _dhuhrAlert;
  bool get asrAlert => _asrAlert;
  bool get maghribAlert => _maghribAlert;
  bool get ishaAlert => _ishaAlert;

  NotificationProvider() {
    _init();
  }

  Future<void> _init() async {
    await _service.init();
    await _loadPreferences();
  }

  Future<void> _loadPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    _isNotificationsEnabled = prefs.getBool('notif_global_enabled') ?? true;
    _isSoundEnabled = prefs.getBool('notif_sound_enabled') ?? true;

    _fajrAlert = prefs.getBool('notif_fajr') ?? true;
    _dhuhrAlert = prefs.getBool('notif_dhuhr') ?? true;
    _asrAlert = prefs.getBool('notif_asr') ?? true;
    _maghribAlert = prefs.getBool('notif_maghrib') ?? true;
    _ishaAlert = prefs.getBool('notif_isha') ?? true;

    notifyListeners();
  }

  Future<void> setGlobalNotifications(bool enabled, PrayerTimesModel? prayerTimes) async {
    _isNotificationsEnabled = enabled;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('notif_global_enabled', enabled);

    if (enabled) {
      await _service.requestPermissions();
      if (prayerTimes != null) {
        await updateSchedule(prayerTimes);
      }
    } else {
      await _service.cancelAllPrayerNotifications();
    }
    notifyListeners();
  }

  Future<void> setSoundEnabled(bool enabled, PrayerTimesModel? prayerTimes) async {
    _isSoundEnabled = enabled;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('notif_sound_enabled', enabled);
    if (prayerTimes != null) await updateSchedule(prayerTimes);
    notifyListeners();
  }

  Future<void> setPrayerAlert(String prayer, bool enabled, PrayerTimesModel? prayerTimes) async {
    final prefs = await SharedPreferences.getInstance();
    switch (prayer.toLowerCase()) {
      case 'fajr':
        _fajrAlert = enabled;
        await prefs.setBool('notif_fajr', enabled);
        break;
      case 'dhuhr':
        _dhuhrAlert = enabled;
        await prefs.setBool('notif_dhuhr', enabled);
        break;
      case 'asr':
        _asrAlert = enabled;
        await prefs.setBool('notif_asr', enabled);
        break;
      case 'maghrib':
        _maghribAlert = enabled;
        await prefs.setBool('notif_maghrib', enabled);
        break;
      case 'isha':
        _ishaAlert = enabled;
        await prefs.setBool('notif_isha', enabled);
        break;
    }

    if (prayerTimes != null) await updateSchedule(prayerTimes);
    notifyListeners();
  }

  Future<void> updateSchedule(PrayerTimesModel prayerTimes) async {
    if (!_isNotificationsEnabled) {
      await _service.cancelAllPrayerNotifications();
      return;
    }

    await _service.schedulePrayerNotifications(
      prayerTimes: prayerTimes,
      isSoundEnabled: _isSoundEnabled,
      fajrEnabled: _fajrAlert,
      dhuhrEnabled: _dhuhrAlert,
      asrEnabled: _asrAlert,
      maghribEnabled: _maghribAlert,
      ishaEnabled: _ishaAlert,
    );
  }

  Future<void> sendTestNotification() async {
    await _service.showTestNotification();
  }
}
