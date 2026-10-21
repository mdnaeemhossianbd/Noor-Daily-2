import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/providers/prayer_provider.dart';
import '../../core/providers/locale_provider.dart';
import '../../core/theme/islamic_theme.dart';

import 'widgets/notification_settings_sheet.dart';

class PrayerTimesScreen extends StatelessWidget {
  const PrayerTimesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final prayerProvider = context.watch<PrayerProvider>();
    final prayerTimes = prayerProvider.prayerTimes;

    if (prayerTimes == null) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    final nextPrayerInfo = prayerTimes.getNextPrayerInfo();
    final nextPrayerName = nextPrayerInfo['name'] as String;
    final remainingDuration = nextPrayerInfo['remaining'] as Duration;
    final currentActivePrayer = prayerTimes.getCurrentPrayerName();

    final hours = remainingDuration.inHours.toString().padLeft(2, '0');
    final minutes = (remainingDuration.inMinutes % 60).toString().padLeft(2, '0');
    final seconds = (remainingDuration.inSeconds % 60).toString().padLeft(2, '0');

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('nav_prayer_times')),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_active_outlined),
            onPressed: () {
              showModalBottomSheet(
                context: context,
                isScrollControlled: true,
                shape: const RoundedRectangleBorder(
                  borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
                ),
                builder: (_) => const NotificationSettingsSheet(),
              );
            },
            tooltip: 'Notification Reminders',
          ),
          IconButton(
            icon: const Icon(Icons.tune),
            onPressed: () => _showPrayerSettingsModal(context, prayerProvider),
            tooltip: 'Calculation Settings',
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        children: [
          // Location bar with change button
          InkWell(
            onTap: () => _showLocationSelectorModal(context, prayerProvider),
            borderRadius: BorderRadius.circular(12),
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: Theme.of(context).cardTheme.color,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(
                  color: Theme.of(context).dividerColor.withValues(alpha: 0.1),
                ),
              ),
              child: Row(
                children: [
                  Icon(
                    prayerProvider.isAutoGps ? Icons.my_location : Icons.location_on,
                    color: IslamicTheme.primaryEmerald,
                    size: 18,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      prayerProvider.locationName,
                      style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  const Text(
                    'Change',
                    style: TextStyle(
                      color: IslamicTheme.primaryEmerald,
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Next Prayer Hero Card with Countdown Ticker
          Container(
            padding: const EdgeInsets.all(22),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFF074328), Color(0xFF032213)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(24),
              boxShadow: [
                BoxShadow(
                  color: IslamicTheme.primaryEmerald.withValues(alpha: 0.3),
                  blurRadius: 18,
                  offset: const Offset(0, 8),
                ),
              ],
            ),
            child: Column(
              children: [
                Text(
                  '${l10n.translate('next_prayer')}: $nextPrayerName',
                  style: const TextStyle(
                    color: IslamicTheme.accentGoldLight,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  '$hours:$minutes:$seconds',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 40,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 2,
                    fontFeatures: [FontFeature.tabularFigures()],
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  'Current Prayer: $currentActivePrayer',
                  style: const TextStyle(color: Color(0xFFC7E2D3), fontSize: 12),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // 6 Daily Prayer Cards
          _buildPrayerRow(context, 'Fajr', prayerTimes.fajr, currentActivePrayer == 'Fajr', Icons.wb_twilight),
          _buildPrayerRow(context, 'Sunrise', prayerTimes.sunrise, currentActivePrayer == 'Sunrise', Icons.wb_sunny_outlined),
          _buildPrayerRow(context, 'Dhuhr', prayerTimes.dhuhr, currentActivePrayer == 'Dhuhr', Icons.wb_sunny),
          _buildPrayerRow(context, 'Asr', prayerTimes.asr, currentActivePrayer == 'Asr', Icons.brightness_5),
          _buildPrayerRow(context, 'Maghrib', prayerTimes.maghrib, currentActivePrayer == 'Maghrib', Icons.nights_stay_outlined),
          _buildPrayerRow(context, 'Isha', prayerTimes.isha, currentActivePrayer == 'Isha', Icons.nights_stay),

          const SizedBox(height: 16),

          // Method & Madhhab info footer card
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: IslamicTheme.primaryEmerald.withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Method: ${prayerTimes.calculationMethodName}',
                  style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 4),
                Text(
                  'Juristic: ${prayerTimes.madhhabName}',
                  style: const TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _buildPrayerRow(
    BuildContext context,
    String name,
    DateTime time,
    bool isActive,
    IconData icon,
  ) {
    final formattedTime = DateFormat('hh:mm a').format(time);

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: isActive
            ? IslamicTheme.primaryEmerald.withValues(alpha: 0.12)
            : Theme.of(context).cardTheme.color,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: isActive
              ? IslamicTheme.primaryEmerald
              : Theme.of(context).dividerColor.withValues(alpha: 0.1),
          width: isActive ? 1.5 : 1,
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Icon(
                icon,
                size: 20,
                color: isActive ? IslamicTheme.primaryEmerald : Colors.grey,
              ),
              const SizedBox(width: 12),
              Text(
                name,
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: isActive ? FontWeight.bold : FontWeight.w600,
                  color: isActive ? IslamicTheme.primaryEmerald : null,
                ),
              ),
            ],
          ),
          Text(
            formattedTime,
            style: TextStyle(
              fontSize: 15,
              fontWeight: isActive ? FontWeight.bold : FontWeight.w500,
              color: isActive ? IslamicTheme.primaryEmerald : null,
            ),
          ),
        ],
      ),
    );
  }

  void _showLocationSelectorModal(BuildContext context, PrayerProvider provider) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) {
        return DraggableScrollableSheet(
          expand: false,
          initialChildSize: 0.65,
          maxChildSize: 0.9,
          builder: (_, controller) {
            return Column(
              children: [
                const SizedBox(height: 12),
                Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Colors.grey.withValues(alpha: 0.4),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Select Location',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                      TextButton.icon(
                        icon: const Icon(Icons.gps_fixed, size: 16),
                        label: const Text('Use GPS'),
                        onPressed: () async {
                          Navigator.pop(ctx);
                          final ok = await provider.requestGpsLocation();
                          if (!ok && context.mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Could not access device GPS. Please grant permission.')),
                            );
                          }
                        },
                      ),
                    ],
                  ),
                ),
                const Divider(height: 1),
                Expanded(
                  child: ListView.builder(
                    controller: controller,
                    itemCount: PrayerProvider.presetCities.length,
                    itemBuilder: (c, idx) {
                      final city = PrayerProvider.presetCities[idx];
                      final isSelected = provider.locationName == city.name;
                      return ListTile(
                        leading: const Icon(Icons.location_city),
                        title: Text(city.name),
                        trailing: isSelected
                            ? const Icon(Icons.check_circle, color: IslamicTheme.primaryEmerald)
                            : null,
                        onTap: () {
                          provider.setManualCity(city);
                          Navigator.pop(ctx);
                        },
                      );
                    },
                  ),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _showPrayerSettingsModal(BuildContext context, PrayerProvider provider) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) {
        return Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Prayer Settings',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),

              // Madhhab Selection
              const Text('Juristic Method (Asr time)', style: TextStyle(fontSize: 13, color: Colors.grey)),
              const SizedBox(height: 6),
              SegmentedButton<String>(
                segments: const [
                  ButtonSegment(value: 'hanafi', label: Text('Hanafi')),
                  ButtonSegment(value: 'shafi', label: Text('Shafi / Standard')),
                ],
                selected: {provider.madhhab},
                onSelectionChanged: (val) {
                  provider.setMadhhab(val.first);
                  Navigator.pop(ctx);
                },
              ),
              const SizedBox(height: 16),

              // Calculation Method Selection
              const Text('Calculation Method', style: TextStyle(fontSize: 13, color: Colors.grey)),
              const SizedBox(height: 6),
              DropdownButtonFormField<String>(
                value: provider.calculationMethod,
                decoration: InputDecoration(
                  contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                ),
                items: const [
                  DropdownMenuItem(value: 'karachi', child: Text('Karachi (Univ. Islamic Sciences)')),
                  DropdownMenuItem(value: 'mwl', child: Text('Muslim World League')),
                  DropdownMenuItem(value: 'makkah', child: Text('Umm Al-Qura (Makkah)')),
                  DropdownMenuItem(value: 'egypt', child: Text('Egyptian General Authority')),
                  DropdownMenuItem(value: 'isna', child: Text('ISNA (North America)')),
                ],
                onChanged: (val) {
                  if (val != null) {
                    provider.setCalculationMethod(val);
                    Navigator.pop(ctx);
                  }
                },
              ),
              const SizedBox(height: 16),
            ],
          ),
        );
      },
    );
  }
}
