import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/providers/notification_provider.dart';
import '../../../core/providers/prayer_provider.dart';
import '../../../core/theme/islamic_theme.dart';

class NotificationSettingsSheet extends StatelessWidget {
  const NotificationSettingsSheet({super.key});

  @override
  Widget build(BuildContext context) {
    final notifProvider = context.watch<NotificationProvider>();
    final prayerProvider = context.watch<PrayerProvider>();
    final prayerTimes = prayerProvider.prayerTimes;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Row(
                children: [
                  Icon(Icons.notifications_active, color: IslamicTheme.primaryEmerald),
                  SizedBox(width: 10),
                  Text(
                    'Prayer Reminders',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Global Master Switch
          SwitchListTile(
            contentPadding: EdgeInsets.zero,
            title: const Text('Enable Prayer Reminders', style: TextStyle(fontWeight: FontWeight.bold)),
            subtitle: const Text('Schedule automatic alerts at Azan times', style: TextStyle(fontSize: 12)),
            value: notifProvider.isNotificationsEnabled,
            activeColor: IslamicTheme.primaryEmerald,
            onChanged: (val) {
              notifProvider.setGlobalNotifications(val, prayerTimes);
            },
          ),
          const Divider(height: 1),

          // Sound Switch
          SwitchListTile(
            contentPadding: EdgeInsets.zero,
            title: const Text('Sound Alerts', style: TextStyle(fontWeight: FontWeight.w600)),
            subtitle: const Text('Play chime/azan sound with notification', style: TextStyle(fontSize: 12)),
            value: notifProvider.isSoundEnabled,
            activeColor: IslamicTheme.primaryEmerald,
            onChanged: notifProvider.isNotificationsEnabled
                ? (val) => notifProvider.setSoundEnabled(val, prayerTimes)
                : null,
          ),
          const Divider(height: 16),

          const Text(
            'Select Prayers to Notify',
            style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: Colors.grey),
          ),
          const SizedBox(height: 8),

          // 5 Individual Prayer Switches
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _buildFilterChip('Fajr', notifProvider.fajrAlert, (val) {
                notifProvider.setPrayerAlert('Fajr', val, prayerTimes);
              }, notifProvider.isNotificationsEnabled),
              _buildFilterChip('Dhuhr', notifProvider.dhuhrAlert, (val) {
                notifProvider.setPrayerAlert('Dhuhr', val, prayerTimes);
              }, notifProvider.isNotificationsEnabled),
              _buildFilterChip('Asr', notifProvider.asrAlert, (val) {
                notifProvider.setPrayerAlert('Asr', val, prayerTimes);
              }, notifProvider.isNotificationsEnabled),
              _buildFilterChip('Maghrib', notifProvider.maghribAlert, (val) {
                notifProvider.setPrayerAlert('Maghrib', val, prayerTimes);
              }, notifProvider.isNotificationsEnabled),
              _buildFilterChip('Isha', notifProvider.ishaAlert, (val) {
                notifProvider.setPrayerAlert('Isha', val, prayerTimes);
              }, notifProvider.isNotificationsEnabled),
            ],
          ),
          const SizedBox(height: 20),

          // Instant Test Alert Button
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              icon: const Icon(Icons.send_rounded, size: 16),
              label: const Text('Send Instant Test Notification'),
              style: OutlinedButton.styleFrom(
                foregroundColor: IslamicTheme.primaryEmerald,
                side: const BorderSide(color: IslamicTheme.primaryEmerald),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
              onPressed: () async {
                await notifProvider.sendTestNotification();
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Test notification sent! Check your notification tray.')),
                  );
                }
              },
            ),
          ),
          const SizedBox(height: 12),

          // Android Battery Optimization notice
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.amber.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.amber.withValues(alpha: 0.3)),
            ),
            child: const Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(Icons.info_outline, color: Colors.amber, size: 18),
                SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'To ensure reminders always sound on time when your phone screen is off, disable Battery Optimization for Noor Daily in Android Settings.',
                    style: TextStyle(fontSize: 11, height: 1.35, color: Colors.amber),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildFilterChip(String label, bool isSelected, ValueChanged<bool> onSelected, bool enabled) {
    return FilterChip(
      label: Text(label),
      selected: isSelected && enabled,
      onSelected: enabled ? onSelected : null,
      selectedColor: IslamicTheme.primaryEmerald.withValues(alpha: 0.2),
      checkmarkColor: IslamicTheme.primaryEmerald,
      labelStyle: TextStyle(
        color: isSelected && enabled ? IslamicTheme.primaryEmerald : null,
        fontWeight: isSelected && enabled ? FontWeight.bold : FontWeight.normal,
      ),
    );
  }
}
