import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:firebase_core/firebase_core.dart';
import 'core/theme/islamic_theme.dart';
import 'core/localization/app_localizations.dart';
import 'core/providers/locale_provider.dart';
import 'core/providers/content_provider.dart';
import 'core/providers/prayer_provider.dart';
import 'core/providers/notification_provider.dart';
import 'shared/main_scaffold.dart';

import 'core/services/remote_config_service.dart';
import 'core/services/admob_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Safe Firebase initialization
  try {
    await Firebase.initializeApp();
    await RemoteConfigService().init();
    await AdMobService().init();
  } catch (e) {
    debugPrint('Firebase/AdMob init notice: $e');
  }

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => LocaleProvider()),
        ChangeNotifierProvider(create: (_) => ContentProvider()),
        ChangeNotifierProvider(create: (_) => PrayerProvider()),
        ChangeNotifierProvider(create: (_) => NotificationProvider()),
      ],
      child: const NoorDailyApp(),
    ),
  );
}

class NoorDailyApp extends StatelessWidget {
  const NoorDailyApp({super.key});

  @override
  Widget build(BuildContext context) {
    final localeProvider = context.watch<LocaleProvider>();

    return MaterialApp(
      title: 'Noor Daily',
      debugShowCheckedModeBanner: false,
      theme: IslamicTheme.lightTheme,
      darkTheme: IslamicTheme.darkTheme,
      themeMode: localeProvider.themeMode,
      locale: localeProvider.locale,
      supportedLocales: const [
        Locale('en', ''),
        Locale('bn', ''),
      ],
      localizationsDelegates: const [
        AppLocalizationsDelegate(),
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      home: const MainScaffold(),
    );
  }
}
