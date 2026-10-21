import 'package:adhan/adhan.dart';
import '../models/prayer_times_model.dart';

class PrayerCalculatorService {
  static PrayerTimesModel calculatePrayerTimes({
    required double latitude,
    required double longitude,
    required DateTime date,
    String method = 'karachi',
    String madhhab = 'hanafi',
    int fajrAdj = 0,
    int sunriseAdj = 0,
    int dhuhrAdj = 0,
    int asrAdj = 0,
    int maghribAdj = 0,
    int ishaAdj = 0,
    String locationName = 'Current Location',
  }) {
    final coordinates = Coordinates(latitude, longitude);
    final dateComponents = DateComponents.from(date);

    // Determine Calculation Method
    CalculationParameters params;
    String methodName;
    switch (method.toLowerCase()) {
      case 'mwl':
      case 'muslim_world_league':
        params = CalculationMethod.muslim_world_league.getParameters();
        methodName = 'Muslim World League';
        break;
      case 'makkah':
      case 'umm_al_qura':
        params = CalculationMethod.umm_al_qura.getParameters();
        methodName = 'Umm Al-Qura (Makkah)';
        break;
      case 'egypt':
      case 'egyptian':
        params = CalculationMethod.egyptian.getParameters();
        methodName = 'Egyptian General Authority';
        break;
      case 'north_america':
      case 'isna':
        params = CalculationMethod.north_america.getParameters();
        methodName = 'ISNA (North America)';
        break;
      case 'karachi':
      default:
        params = CalculationMethod.karachi.getParameters();
        methodName = 'University of Islamic Sciences, Karachi';
        break;
    }

    // Determine Madhhab (Shadow ratio for Asr)
    String madhhabDisplayName;
    if (madhhab.toLowerCase() == 'hanafi') {
      params.madhab = Madhab.hanafi;
      madhhabDisplayName = 'Hanafi';
    } else {
      params.madhab = Madhab.shafi;
      madhhabDisplayName = 'Shafi / Maliki / Hanbali';
    }

    // Apply manual minute adjustments
    params.adjustments.fajr = fajrAdj;
    params.adjustments.sunrise = sunriseAdj;
    params.adjustments.dhuhr = dhuhrAdj;
    params.adjustments.asr = asrAdj;
    params.adjustments.maghrib = maghribAdj;
    params.adjustments.isha = ishaAdj;

    final adhanTimes = PrayerTimes(coordinates, dateComponents, params);

    return PrayerTimesModel(
      fajr: adhanTimes.fajr,
      sunrise: adhanTimes.sunrise,
      dhuhr: adhanTimes.dhuhr,
      asr: adhanTimes.asr,
      maghrib: adhanTimes.maghrib,
      isha: adhanTimes.isha,
      locationName: locationName,
      calculationMethodName: methodName,
      madhhabName: madhhabDisplayName,
    );
  }
}
