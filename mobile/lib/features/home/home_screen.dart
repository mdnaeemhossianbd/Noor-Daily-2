import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/providers/locale_provider.dart';
import '../../core/providers/content_provider.dart';
import '../../core/theme/islamic_theme.dart';
import '../../core/models/post_model.dart';
import '../posts/post_detail_screen.dart';
import '../ads/banner_ad_widget.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final localeProvider = context.watch<LocaleProvider>();
    final contentProvider = context.watch<ContentProvider>();
    final langCode = localeProvider.locale.languageCode;

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: IslamicTheme.primaryEmerald.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(
                Icons.auto_stories,
                color: IslamicTheme.primaryEmerald,
                size: 20,
              ),
            ),
            const SizedBox(width: 10),
            Text(l10n.translate('app_name')),
          ],
        ),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          contentProvider.initSubscriptions();
        },
        child: contentProvider.isLoading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                children: [
                  // Daily Inspiration Banner Card
                  _buildDailyAyatCard(context, contentProvider.dailyAyatPost, langCode, l10n),
                  const SizedBox(height: 20),

                  // Section Title
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        l10n.translate('latest_posts'),
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),

                  // Posts list or empty state
                  if (contentProvider.posts.isEmpty)
                    _buildEmptyState(context, l10n)
                  else
                    ...contentProvider.posts.map((post) {
                      return _buildPostCard(context, post, langCode);
                    }),
                  
                  // In-Feed Banner Ad (Remote Controlled)
                  const BannerAdWidget(),
                  const SizedBox(height: 24),
                ],
              ),
      ),
    );
  }

  Widget _buildDailyAyatCard(
    BuildContext context,
    PostModel? post,
    String langCode,
    AppLocalizations l10n,
  ) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [IslamicTheme.primaryEmerald, Color(0xFF04331E)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: IslamicTheme.primaryEmerald.withValues(alpha: 0.25),
            blurRadius: 15,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: IslamicTheme.accentGold.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: IslamicTheme.accentGold, width: 0.5),
                ),
                child: Text(
                  l10n.translate('daily_ayat_title'),
                  style: const TextStyle(
                    color: IslamicTheme.accentGoldLight,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const Icon(Icons.star_rounded, color: IslamicTheme.accentGold, size: 20),
            ],
          ),
          const SizedBox(height: 14),
          Text(
            post != null
                ? post.localizedTitle(langCode)
                : 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ',
            style: TextStyle(
              color: Colors.white,
              fontSize: langCode == 'ar' ? 20 : 17,
              fontWeight: FontWeight.bold,
              height: 1.4,
            ),
            textDirection: langCode == 'ar' ? TextDirection.rtl : TextDirection.ltr,
          ),
          const SizedBox(height: 8),
          Text(
            post != null
                ? post.localizedSubtitle(langCode)
                : '"In the name of Allah, the Entirely Merciful, the Especially Merciful."',
            style: const TextStyle(
              color: Color(0xFFC7E2D3),
              fontSize: 13,
              height: 1.4,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }

  Widget _buildPostCard(BuildContext context, PostModel post, String langCode) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => PostDetailScreen(post: post),
            ),
          );
        },
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (post.imageUrl != null && post.imageUrl!.isNotEmpty)
                ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: CachedNetworkImage(
                    imageUrl: post.imageUrl!,
                    width: 76,
                    height: 76,
                    fit: BoxFit.cover,
                    placeholder: (context, url) => Container(
                      width: 76,
                      height: 76,
                      color: Colors.grey.withValues(alpha: 0.1),
                      child: const Icon(Icons.image, color: Colors.grey),
                    ),
                    errorWidget: (context, url, error) => Container(
                      width: 76,
                      height: 76,
                      color: Colors.grey.withValues(alpha: 0.1),
                      child: const Icon(Icons.menu_book, color: Colors.grey),
                    ),
                  ),
                )
              else
                Container(
                  width: 76,
                  height: 76,
                  decoration: BoxDecoration(
                    color: IslamicTheme.primaryEmerald.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(
                    Icons.menu_book,
                    color: IslamicTheme.primaryEmerald,
                    size: 28,
                  ),
                ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      post.localizedTitle(langCode),
                      style: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.bold,
                        height: 1.3,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      textDirection: langCode == 'ar' ? TextDirection.rtl : TextDirection.ltr,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      post.localizedSubtitle(langCode),
                      style: TextStyle(
                        fontSize: 12,
                        color: Theme.of(context).textTheme.bodySmall?.color?.withValues(alpha: 0.7),
                        height: 1.3,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context, AppLocalizations l10n) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 48, horizontal: 24),
      alignment: Alignment.center,
      child: Column(
        children: [
          Icon(Icons.menu_book_outlined, size: 48, color: Colors.grey.withValues(alpha: 0.5)),
          const SizedBox(height: 12),
          Text(
            l10n.translate('no_posts_found'),
            style: const TextStyle(fontSize: 14, color: Colors.grey),
          ),
        ],
      ),
    );
  }
}
