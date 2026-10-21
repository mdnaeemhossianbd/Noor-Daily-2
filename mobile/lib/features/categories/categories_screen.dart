import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/providers/locale_provider.dart';
import '../../core/providers/content_provider.dart';
import '../../core/models/category_model.dart';
import '../../core/theme/islamic_theme.dart';
import '../posts/post_detail_screen.dart';

/// Displays one level of the category tree per screen.
/// Main categories never share a list with their subcategories.
class CategoriesScreen extends StatelessWidget {
  const CategoriesScreen({super.key, this.parentCategory});

  final CategoryModel? parentCategory;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final localeProvider = context.watch<LocaleProvider>();
    final contentProvider = context.watch<ContentProvider>();
    final langCode = localeProvider.locale.languageCode;
    final categories = contentProvider.categories;

    final visibleCategories = categories.where((category) {
      if (parentCategory == null) {
        final parentId = category.parentId?.trim();
        return parentId == null || parentId.isEmpty || parentId == 'null';
      }
      return category.parentId == parentCategory!.id;
    }).toList()
      ..sort((a, b) => a.order.compareTo(b.order));

    final posts = parentCategory == null || visibleCategories.isNotEmpty
        ? const []
        : contentProvider.getPostsByCategory(parentCategory!.id);

    return Scaffold(
      appBar: AppBar(
        leading: parentCategory == null
            ? null
            : IconButton(
                tooltip: MaterialLocalizations.of(context).backButtonTooltip,
                icon: const Icon(Icons.arrow_back),
                onPressed: () => Navigator.of(context).pop(),
              ),
        title: Text(
          parentCategory?.localizedName(langCode) ??
              l10n.translate('categories_title'),
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
      ),
      body: contentProvider.isLoading && categories.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : categories.isEmpty
              ? _emptyState(l10n.translate('no_posts_found'))
              : visibleCategories.isNotEmpty
                  ? ListView.separated(
                      padding: const EdgeInsets.all(16),
                      itemCount: visibleCategories.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 8),
                      itemBuilder: (context, index) {
                        final category = visibleCategories[index];
                        final childCount = categories
                            .where((item) => item.parentId == category.id)
                            .length;
                        return Card(
                          clipBehavior: Clip.antiAlias,
                          child: ListTile(
                            contentPadding: const EdgeInsets.symmetric(
                              horizontal: 16,
                              vertical: 8,
                            ),
                            leading: Container(
                              width: 44,
                              height: 44,
                              decoration: BoxDecoration(
                                color: IslamicTheme.primaryEmerald
                                    .withValues(alpha: 0.12),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Icon(
                                childCount == 0
                                    ? Icons.folder_outlined
                                    : Icons.folder_open_outlined,
                                color: IslamicTheme.primaryEmerald,
                              ),
                            ),
                            title: Text(
                              category.localizedName(langCode),
                              style: const TextStyle(
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                            subtitle: Text(
                              childCount > 0
                                  ? 'সাব-ক্যাটাগরি দেখতে চাপ দিন'
                                  : 'পোস্ট দেখতে চাপ দিন',
                            ),
                            trailing: const Icon(Icons.chevron_right),
                            onTap: () => Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => CategoriesScreen(
                                  parentCategory: category,
                                ),
                              ),
                            ),
                          ),
                        );
                      },
                    )
                  : posts.isEmpty
                      ? _emptyState(l10n.translate('no_posts_found'))
                      : ListView.separated(
                          padding: const EdgeInsets.all(16),
                          itemCount: posts.length,
                          separatorBuilder: (_, __) => const SizedBox(height: 8),
                          itemBuilder: (context, index) {
                            final post = posts[index];
                            return Card(
                              child: ListTile(
                                contentPadding: const EdgeInsets.all(14),
                                title: Text(
                                  post.localizedTitle(langCode),
                                  style: const TextStyle(
                                    fontWeight: FontWeight.bold,
                                  ),
                                  textDirection: langCode == 'ar'
                                      ? TextDirection.rtl
                                      : TextDirection.ltr,
                                ),
                                subtitle: Padding(
                                  padding: const EdgeInsets.only(top: 6),
                                  child: Text(
                                    post.localizedSubtitle(langCode),
                                    maxLines: 2,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                trailing: const Icon(
                                  Icons.arrow_forward_ios,
                                  size: 14,
                                ),
                                onTap: () => Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (_) => PostDetailScreen(post: post),
                                  ),
                                ),
                              ),
                            );
                          },
                        ),
    );
  }

  Widget _emptyState(String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Text(
          message,
          textAlign: TextAlign.center,
          style: const TextStyle(color: Colors.grey),
        ),
      ),
    );
  }
}
