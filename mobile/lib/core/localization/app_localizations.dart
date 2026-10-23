import 'package:flutter/material.dart';

class AppLocalizations {
  final Locale locale;

  AppLocalizations(this.locale);

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations) ??
        AppLocalizations(const Locale('en'));
  }

  static const _localizedValues = <String, Map<String, String>>{
    'en': {
      'app_name': 'Noor Daily',
      'nav_home': 'Home',
      'nav_categories': 'Categories',
      'nav_prayer_times': 'Prayer Times',
      'nav_saved': 'Saved',
      'nav_settings': 'Settings',
      'daily_ayat_title': 'Ayat of the Day',
      'daily_hadith_title': 'Daily Hadith',
      'read_more': 'Read Article',
      'latest_posts': 'Latest Islamic Guidance',
      'no_posts_found': 'No articles available yet.',
      'next_prayer': 'Next Prayer',
      'categories_title': 'Explore Topics',
      'all_categories': 'All Categories',
      'saved_posts_title': 'Bookmarked Articles',
      'no_saved_posts': 'You have not saved any articles yet.',
      'settings_title': 'Preferences',
      'language': 'Language',
      'theme_mode': 'Theme',
      'light_theme': 'Light Theme',
      'dark_theme': 'Dark Theme',
      'system_theme': 'System Default',
      'about_app': 'About Noor Daily',
      'about_description': 'A comprehensive Islamic dynamic application providing authentic Quranic teachings, Hadith, daily prayer times, and community guidance.',
      'pull_to_refresh': 'Pull down to refresh',
      'retry': 'Retry',
      'search_articles': 'Search Islamic content...',
    },
    'bn': {
      'app_name': 'নূর ডেইলি',
      'nav_home': 'হোম',
      'nav_categories': 'ক্যাটাগরি',
      'nav_prayer_times': 'নামাজের সময়',
      'nav_saved': 'সংরক্ষিত',
      'nav_settings': 'সেটিংস',
      'daily_ayat_title': 'আজকের আয়াত',
      'daily_hadith_title': 'দৈনিক হাদিস',
      'read_more': 'বিস্তারিত পড়ুন',
      'latest_posts': 'সর্বশেষ ইসলামিক আলোচনা',
      'no_posts_found': 'কোনো পোস্ট পাওয়া যায়নি।',
      'next_prayer': 'পরবর্তী নামাজ',
      'categories_title': 'বিষয়ভিত্তিক ক্যাটাগরি',
      'all_categories': 'সব ক্যাটাগরি',
      'saved_posts_title': 'সংরক্ষিত পোস্টসমূহ',
      'no_saved_posts': 'আপনি কোনো পোস্ট সংরক্ষণ করেননি।',
      'settings_title': 'পছন্দসমূহ',
      'language': 'ভাষা পরিবর্তন',
      'theme_mode': 'থিম মোড',
      'light_theme': 'লাইট থিম',
      'dark_theme': 'ডার্ক থিম',
      'system_theme': 'সিস্টেম ডিফল্ট',
      'about_app': 'নূর ডেইলি সম্পর্কে',
      'about_description': 'একটি পূর্ণাঙ্গ ইসলামিক মোবাইল অ্যাপ্লিকেশন যা কুরআন, হাদিস ও সঠিক নামাজের সময়সূচি প্রদান করে।',
      'pull_to_refresh': 'নতুন কনটেন্টের জন্য নিচে টানুন',
      'retry': 'আবার চেষ্টা করুন',
      'search_articles': 'ইসলামিক কনটেন্ট অনুসন্ধান...',
    },
  };

  String translate(String key) {
    final lang = locale.languageCode;
    return _localizedValues[lang]?[key] ?? _localizedValues['en']?[key] ?? key;
  }

  bool get isRtl => false;
}

class AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  const AppLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) => ['en', 'bn'].contains(locale.languageCode);

  @override
  Future<AppLocalizations> load(Locale locale) async {
    return AppLocalizations(locale);
  }

  @override
  bool shouldReload(AppLocalizationsDelegate old) => false;
}
