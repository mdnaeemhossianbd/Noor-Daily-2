import 'dart:async';
import 'dart:math' as math;
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:geolocator/geolocator.dart';
import '../models/prayer_times_model.dart';
import '../services/prayer_calculator_service.dart';

class CityPreset {
  final String name;
  final double lat;
  final double lng;

  const CityPreset({required this.name, required this.lat, required this.lng});
}

class PrayerProvider extends ChangeNotifier {
  // Preset global cities for manual selection
  static const List<CityPreset> presetCities = [
    CityPreset(name: 'Dhaka, Bangladesh', lat: 23.8103, lng: 90.4125),
    CityPreset(name: 'Chittagong, Bangladesh', lat: 22.3569, lng: 91.7832),
    CityPreset(name: 'Sylhet, Bangladesh', lat: 24.8949, lng: 91.8687),
    CityPreset(name: 'Makkah, Saudi Arabia', lat: 21.3891, lng: 39.8579),
    CityPreset(name: 'Madinah, Saudi Arabia', lat: 24.5247, lng: 39.5692),
    CityPreset(name: 'Cairo, Egypt', lat: 30.0444, lng: 31.2357),
    CityPreset(name: 'Istanbul, Turkey', lat: 41.0082, lng: 28.9784),
    CityPreset(name: 'Karachi, Pakistan', lat: 24.8607, lng: 67.0011),
    CityPreset(name: 'Jakarta, Indonesia', lat: -6.2088, lng: 106.8456),
    CityPreset(name: 'Kuala Lumpur, Malaysia', lat: 3.1390, lng: 101.6869),
    CityPreset(name: 'London, United Kingdom', lat: 51.5074, lng: -0.1278),
    CityPreset(name: 'New York, USA', lat: 40.7128, lng: -74.0060),
    CityPreset(name: 'Toronto, Canada', lat: 43.6532, lng: -79.3832),
  ];

  double _latitude = 23.8103;
  double _longitude = 90.4125;
  String _locationName = 'Dhaka, Bangladesh';
  bool _isAutoGps = false;

  String _calculationMethod = 'karachi';
  String _madhhab = 'hanafi';

  int _fajrAdjustment = 0;
  int _sunriseAdjustment = 0;
  int _dhuhrAdjustment = 0;
  int _asrAdjustment = 0;
  int _maghribAdjustment = 0;
  int _ishaAdjustment = 0;

  PrayerTimesModel? _prayerTimes;
  Timer? _countdownTimer;

  PrayerTimesModel? get prayerTimes => _prayerTimes;
  String get locationName => _locationName;
  bool get isAutoGps => _isAutoGps;
  String get calculationMethod => _calculationMethod;
  String get madhhab => _madhhab;

  PrayerProvider() {
    _loadPreferences().then((_) {
      recalculate();
      _startCountdownTicker();
    });
  }

  void _startCountdownTicker() {
    _countdownTimer?.cancel();
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      notifyListeners();
    });
  }

  Future<void> _loadPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    _latitude = prefs.getDouble('prayer_lat') ?? 23.8103;
    _longitude = prefs.getDouble('prayer_lng') ?? 90.4125;
    _locationName = prefs.getString('prayer_location_name') ?? 'Dhaka, Bangladesh';
    _isAutoGps = prefs.getBool('prayer_auto_gps') ?? false;
    _calculationMethod = prefs.getString('prayer_calc_method') ?? 'karachi';
    _madhhab = prefs.getString('prayer_madhhab') ?? 'hanafi';

    _fajrAdjustment = prefs.getInt('prayer_adj_fajr') ?? 0;
    _sunriseAdjustment = prefs.getInt('prayer_adj_sunrise') ?? 0;
    _dhuhrAdjustment = prefs.getInt('prayer_adj_dhuhr') ?? 0;
    _asrAdjustment = prefs.getInt('prayer_adj_asr') ?? 0;
    _maghribAdjustment = prefs.getInt('prayer_adj_maghrib') ?? 0;
    _ishaAdjustment = prefs.getInt('prayer_adj_isha') ?? 0;
  }

  void recalculate() {
    _prayerTimes = PrayerCalculatorService.calculatePrayerTimes(
      latitude: _latitude,
      longitude: _longitude,
      date: DateTime.now(),
      method: _calculationMethod,
      madhhab: _madhhab,
      fajrAdj: _fajrAdjustment,
      sunriseAdj: _sunriseAdjustment,
      dhuhrAdj: _dhuhrAdjustment,
      asrAdj: _asrAdjustment,
      maghribAdj: _maghribAdjustment,
      ishaAdj: _ishaAdjustment,
      locationName: _locationName,
    );
    notifyListeners();
  }

  Future<void> setManualCity(CityPreset city) async {
    _latitude = city.lat;
    _longitude = city.lng;
    _locationName = city.name;
    _isAutoGps = false;

    final prefs = await SharedPreferences.getInstance();
    await prefs.setDouble('prayer_lat', _latitude);
    await prefs.setDouble('prayer_lng', _longitude);
    await prefs.setString('prayer_location_name', _locationName);
    await prefs.setBool('prayer_auto_gps', false);

    recalculate();
  }

  Future<bool> requestGpsLocation() async {
    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) return false;

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) return false;
      }

      if (permission == LocationPermission.deniedForever) return false;

      Position pos = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(accuracy: LocationAccuracy.low),
      );

      _latitude = pos.latitude;
      _longitude = pos.longitude;
      _locationName = _resolveLocationName(pos.latitude, pos.longitude);
      _isAutoGps = true;

      final prefs = await SharedPreferences.getInstance();
      await prefs.setDouble('prayer_lat', _latitude);
      await prefs.setDouble('prayer_lng', _longitude);
      await prefs.setString('prayer_location_name', _locationName);
      await prefs.setBool('prayer_auto_gps', true);

      recalculate();
      return true;
    } catch (e) {
      debugPrint('GPS detection failed: $e');
      return false;
    }
  }

  Future<void> setCalculationMethod(String method) async {
    _calculationMethod = method;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('prayer_calc_method', method);
    recalculate();
  }

  Future<void> setMadhhab(String madhhab) async {
    _madhhab = madhhab;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('prayer_madhhab', madhhab);
    recalculate();
  }

  Future<void> setAdjustment(String prayer, int minutes) async {
    final prefs = await SharedPreferences.getInstance();
    switch (prayer.toLowerCase()) {
      case 'fajr':
        _fajrAdjustment = minutes;
        await prefs.setInt('prayer_adj_fajr', minutes);
        break;
      case 'dhuhr':
        _dhuhrAdjustment = minutes;
        await prefs.setInt('prayer_adj_dhuhr', minutes);
        break;
      case 'asr':
        _asrAdjustment = minutes;
        await prefs.setInt('prayer_adj_asr', minutes);
        break;
      case 'maghrib':
        _maghribAdjustment = minutes;
        await prefs.setInt('prayer_adj_maghrib', minutes);
        break;
      case 'isha':
        _ishaAdjustment = minutes;
        await prefs.setInt('prayer_adj_isha', minutes);
        break;
    }
    recalculate();
  }

  String _resolveLocationName(double lat, double lng) {
    double minDistance = double.infinity;
    String closestName = 'Dhaka, Bangladesh';

    for (final city in presetCities) {
      final dLat = (city.lat - lat) * (math.pi / 180.0);
      final dLng = (city.lng - lng) * (math.pi / 180.0);
      final a = math.sin(dLat / 2) * math.sin(dLat / 2) +
          math.cos(lat * (math.pi / 180.0)) *
              math.cos(city.lat * (math.pi / 180.0)) *
              math.sin(dLng / 2) *
              math.sin(dLng / 2);
      final c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a));
      final distance = 6371.0 * c;
      if (distance < minDistance) {
        minDistance = distance;
        closestName = city.name;
      }
    }
    return closestName;
  }

  @override
  void dispose() {
    _countdownTimer?.cancel();
    super.dispose();
  }
}
