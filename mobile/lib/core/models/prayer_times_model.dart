class PrayerTimesModel {
  final DateTime fajr;
  final DateTime sunrise;
  final DateTime dhuhr;
  final DateTime asr;
  final DateTime maghrib;
  final DateTime isha;
  final String locationName;
  final String calculationMethodName;
  final String madhhabName;

  PrayerTimesModel({
    required this.fajr,
    required this.sunrise,
    required this.dhuhr,
    required this.asr,
    required this.maghrib,
    required this.isha,
    required this.locationName,
    required this.calculationMethodName,
    required this.madhhabName,
  });

  // Determine which prayer is coming up next
  Map<String, dynamic> getNextPrayerInfo() {
    final now = DateTime.now();

    if (now.isBefore(fajr)) {
      return {'name': 'Fajr', 'time': fajr, 'remaining': fajr.difference(now)};
    } else if (now.isBefore(sunrise)) {
      return {'name': 'Sunrise', 'time': sunrise, 'remaining': sunrise.difference(now)};
    } else if (now.isBefore(dhuhr)) {
      return {'name': 'Dhuhr', 'time': dhuhr, 'remaining': dhuhr.difference(now)};
    } else if (now.isBefore(asr)) {
      return {'name': 'Asr', 'time': asr, 'remaining': asr.difference(now)};
    } else if (now.isBefore(maghrib)) {
      return {'name': 'Maghrib', 'time': maghrib, 'remaining': maghrib.difference(now)};
    } else if (now.isBefore(isha)) {
      return {'name': 'Isha', 'time': isha, 'remaining': isha.difference(now)};
    } else {
      // Tomorrow's Fajr
      final tomorrowFajr = fajr.add(const Duration(days: 1));
      return {'name': 'Fajr', 'time': tomorrowFajr, 'remaining': tomorrowFajr.difference(now)};
    }
  }

  // Determine current active prayer period
  String getCurrentPrayerName() {
    final now = DateTime.now();
    if (now.isAfter(fajr) && now.isBefore(sunrise)) return 'Fajr';
    if (now.isAfter(sunrise) && now.isBefore(dhuhr)) return 'Sunrise';
    if (now.isAfter(dhuhr) && now.isBefore(asr)) return 'Dhuhr';
    if (now.isAfter(asr) && now.isBefore(maghrib)) return 'Asr';
    if (now.isAfter(maghrib) && now.isBefore(isha)) return 'Maghrib';
    return 'Isha';
  }
}
