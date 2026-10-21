import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/providers/locale_provider.dart';
import '../../core/theme/islamic_theme.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final localeProvider = context.watch<LocaleProvider>();
    final currentLang = localeProvider.locale.languageCode;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('settings_title')),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Section: Language Selection
          _buildSectionHeader(context, l10n.translate('language')),
          Card(
            child: Column(
              children: [
                _buildRadioTile(
                  title: 'English (US)',
                  subtitle: 'English language',
                  value: 'en',
                  groupValue: currentLang,
                  onChanged: (val) {
                    if (val != null) localeProvider.setLocale(val);
                  },
                ),
                const Divider(height: 1),
                _buildRadioTile(
                  title: 'বাংলা (Bangla)',
                  subtitle: 'বাংলা ভাষা',
                  value: 'bn',
                  groupValue: currentLang,
                  onChanged: (val) {
                    if (val != null) localeProvider.setLocale(val);
                  },
                ),
                const Divider(height: 1),
                _buildRadioTile(
                  title: 'العربية (Arabic)',
                  subtitle: 'اللغة العربية مع محاذاة النص لليمين',
                  value: 'ar',
                  groupValue: currentLang,
                  onChanged: (val) {
                    if (val != null) localeProvider.setLocale(val);
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Section: Theme Mode
          _buildSectionHeader(context, l10n.translate('theme_mode')),
          Card(
            child: Column(
              children: [
                _buildThemeTile(
                  context,
                  title: l10n.translate('system_theme'),
                  mode: ThemeMode.system,
                  currentMode: localeProvider.themeMode,
                  onTap: () => localeProvider.setThemeMode(ThemeMode.system),
                ),
                const Divider(height: 1),
                _buildThemeTile(
                  context,
                  title: l10n.translate('light_theme'),
                  mode: ThemeMode.light,
                  currentMode: localeProvider.themeMode,
                  onTap: () => localeProvider.setThemeMode(ThemeMode.light),
                ),
                const Divider(height: 1),
                _buildThemeTile(
                  context,
                  title: l10n.translate('dark_theme'),
                  mode: ThemeMode.dark,
                  currentMode: localeProvider.themeMode,
                  onTap: () => localeProvider.setThemeMode(ThemeMode.dark),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Section: About App
          _buildSectionHeader(context, l10n.translate('about_app')),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          color: IslamicTheme.primaryEmerald.withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Icon(Icons.mosque, color: IslamicTheme.primaryEmerald),
                      ),
                      const SizedBox(width: 12),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            l10n.translate('app_name'),
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                          ),
                          const Text('Version 1.0.0', style: TextStyle(color: Colors.grey, fontSize: 12)),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    l10n.translate('about_description'),
                    style: const TextStyle(fontSize: 13, height: 1.4, color: Colors.grey),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Text(
        title,
        style: TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.bold,
          color: Theme.of(context).colorScheme.primary,
        ),
      ),
    );
  }

  Widget _buildRadioTile({
    required String title,
    required String subtitle,
    required String value,
    required String groupValue,
    required ValueChanged<String?> onChanged,
  }) {
    return RadioListTile<String>(
      title: Text(title, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
      subtitle: Text(subtitle, style: const TextStyle(fontSize: 12, color: Colors.grey)),
      value: value,
      groupValue: groupValue,
      onChanged: onChanged,
      activeColor: IslamicTheme.primaryEmerald,
    );
  }

  Widget _buildThemeTile(
    BuildContext context, {
    required String title,
    required ThemeMode mode,
    required ThemeMode currentMode,
    required VoidCallback onTap,
  }) {
    final isSelected = mode == currentMode;
    return ListTile(
      title: Text(title, style: const TextStyle(fontSize: 14)),
      trailing: isSelected
          ? const Icon(Icons.check_circle, color: IslamicTheme.primaryEmerald)
          : null,
      onTap: onTap,
    );
  }
}
