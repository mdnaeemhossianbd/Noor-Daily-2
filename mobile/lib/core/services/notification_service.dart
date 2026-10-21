import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/data/latest_all.dart' as tz;
import 'package:timezone/timezone.dart' as tz;
import '../models/prayer_times_model.dart';

class NotificationService {
  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  final FlutterLocalNotificationsPlugin _notifications = FlutterLocalNotificationsPlugin();

  static const String channelId = 'noor_daily_prayer_alerts';
  static const String channelName = 'Prayer Reminders';
  static const String channelDescription = 'Timely notifications and Azan alerts for daily Islamic prayers';

  Future<void> init() async {
    tz.initializeTimeZones();

    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    const initSettings = InitializationSettings(android: androidSettings);

    await _notifications.initialize(
      initSettings,
      onDidReceiveNotificationResponse: (details) {
        debugPrint('Notification clicked with payload: ${details.payload}');
      },
    );

    // Create High-Importance Android Notification Channel
    final androidChannel = AndroidNotificationChannel(
      channelId,
      channelName,
      description: channelDescription,
      importance: Importance.max,
      playSound: true,
      enableVibration: true,
    );

    await _notifications
        .resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>()
        ?.createNotificationChannel(androidChannel);
  }

  Future<bool> requestPermissions() async {
    final android = _notifications.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
    if (android != null) {
      final notifGranted = await android.requestNotificationsPermission() ?? false;
      final exactAlarmGranted = await android.requestExactAlarmsPermission() ?? false;
      return notifGranted && exactAlarmGranted;
    }
    return true;
  }

  Future<void> schedulePrayerNotifications({
    required PrayerTimesModel prayerTimes,
    required bool isSoundEnabled,
    required bool fajrEnabled,
    required bool dhuhrEnabled,
    required bool asrEnabled,
    required bool maghribEnabled,
    required bool ishaEnabled,
  }) async {
    // 1. Cancel all previous prayer alarms to prevent duplicates
    await cancelAllPrayerNotifications();

    final prayersToSchedule = [
      {'id': 101, 'name': 'Fajr', 'time': prayerTimes.fajr, 'enabled': fajrEnabled},
      {'id': 102, 'name': 'Dhuhr', 'time': prayerTimes.dhuhr, 'enabled': dhuhrEnabled},
      {'id': 103, 'name': 'Asr', 'time': prayerTimes.asr, 'enabled': asrEnabled},
      {'id': 104, 'name': 'Maghrib', 'time': prayerTimes.maghrib, 'enabled': maghribEnabled},
      {'id': 105, 'name': 'Isha', 'time': prayerTimes.isha, 'enabled': ishaEnabled},
    ];

    final now = DateTime.now();

    for (final item in prayersToSchedule) {
      final isEnabled = item['enabled'] as bool;
      final prayerTime = item['time'] as DateTime;
      final prayerName = item['name'] as String;
      final notifId = item['id'] as int;

      if (!isEnabled) continue;

      // Only schedule if prayer time is in the future today
      if (prayerTime.isAfter(now)) {
        await _scheduleSingleExactAlarm(
          id: notifId,
          title: '$prayerName Prayer Time',
          body: 'It is time for $prayerName prayer. "Prayer is better than sleep."',
          scheduledTime: prayerTime,
          isSoundEnabled: isSoundEnabled,
        );
      }
    }
  }

  Future<void> _scheduleSingleExactAlarm({
    required int id,
    required String title,
    required String body,
    required DateTime scheduledTime,
    required bool isSoundEnabled,
  }) async {
    final tzDateTime = tz.TZDateTime.from(scheduledTime, tz.local);

    final androidDetails = AndroidNotificationDetails(
      channelId,
      channelName,
      channelDescription: channelDescription,
      importance: Importance.max,
      priority: Priority.high,
      playSound: isSoundEnabled,
      enableVibration: true,
      category: AndroidNotificationCategory.alarm,
      fullScreenIntent: false,
    );

    final details = NotificationDetails(android: androidDetails);

    try {
      await _notifications.zonedSchedule(
        id,
        title,
        body,
        tzDateTime,
        details,
        androidScheduleMode: AndroidScheduleMode.exactAllowWhileIdle,
        uiLocalNotificationDateInterpretation: UILocalNotificationDateInterpretation.absoluteTime,
      );
      debugPrint('Scheduled alarm for $title at $tzDateTime (ID: $id)');
    } catch (e) {
      debugPrint('Failed to schedule exact alarm: $e');
    }
  }

  Future<void> cancelAllPrayerNotifications() async {
    for (int id in [101, 102, 103, 104, 105]) {
      await _notifications.cancel(id);
    }
  }

  // Trigger an instant preview test notification
  Future<void> showTestNotification() async {
    const androidDetails = AndroidNotificationDetails(
      channelId,
      channelName,
      channelDescription: channelDescription,
      importance: Importance.max,
      priority: Priority.high,
      playSound: true,
      enableVibration: true,
    );

    const details = NotificationDetails(android: androidDetails);

    await _notifications.show(
      999,
      'Noor Daily Test Alert',
      'Prayer reminder notification system is active and functioning properly.',
      details,
    );
  }
}
