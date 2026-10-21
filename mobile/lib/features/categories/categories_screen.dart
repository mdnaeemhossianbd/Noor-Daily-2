import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/providers/locale_provider.dart';
import '../../core/providers/content_provider.dart';
import '../../core/models/category_model.dart';
import '../../core/theme/islamic_theme.dart';
import '../posts/post_detail_screen.dart';

class CategoriesScreen extends StatefulWidget {
  const CategoriesScreen({super.key});

  @override
  State<CategoriesScreen> createState() => _CategoriesScreenState();
}

class _CategoriesScreenState extends State<CategoriesScreen> {
  String? _selectedCategoryId;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final localeProvider = context.watch<LocaleProvider>();
    final contentProvider = context.watch<ContentProvider>();
    final langCode = localeProvider.locale.languageCode;

    final categories = contentProvider.categories;
    final selectedCategory = categories.firstWhere(
      (c) => c.id == _selectedCategoryId,
      orElse: () => categories.isNotEmpty ? categories.first : CategoryModel(
        id: '',
        nameAr: '',
        nameBn: '',
        nameEn: '',
        depth: 0,
        order: 0,
        isActive: true,
      ),
    );

    final posts = _selectedCategoryId != null && _selectedCategoryId!.isNotEmpty
        ? contentProvider.getPostsByCategory(_selectedCategoryId!)
        : contentProvider.posts;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('categories_title')),
      ),
      body: categories.isEmpty
          ? Center(
              child: Text(
                l10n.translate('no_posts_found'),
                style: const TextStyle(color: Colors.grey),
              ),
            )
          : Column(
              children: [
                // Horizontal category selector chips
                SizedBox(
                  height: 48,
                  child: ListView(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    children: [
                      Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: ChoiceChip(
                          label: Text(l10n.translate('all_categories')),
                          selected: _selectedCategoryId == null,
                          onSelected: (val) {
                            setState(() => _selectedCategoryId = null);
                          },
                          selectedColor: IslamicTheme.primaryEmerald.withValues(alpha: 0.15),
                          labelStyle: TextStyle(
                            color: _selectedCategoryId == null
                                ? IslamicTheme.primaryEmerald
                                : null,
                            fontWeight: _selectedCategoryId == null
                                ? FontWeight.bold
                                : FontWeight.normal,
                          ),
                        ),
                      ),
                      ...categories.map((cat) {
                        final isSelected = _selectedCategoryId == cat.id;
                        return Padding(
                          padding: const EdgeInsets.only(right: 8),
                          child: ChoiceChip(
                            label: Text(
                              '${'— ' * cat.depth}${cat.localizedName(langCode)}',
                            ),
                            selected: isSelected,
                            onSelected: (val) {
                              setState(() => _selectedCategoryId = val ? cat.id : null);
                            },
                            selectedColor: IslamicTheme.primaryEmerald.withValues(alpha: 0.15),
                            labelStyle: TextStyle(
                              color: isSelected ? IslamicTheme.primaryEmerald : null,
                              fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            ),
                          ),
                        );
                      }),
                    ],
                  ),
                ),
                const Divider(height: 16),

                // Post list in category
                Expanded(
                  child: posts.isEmpty
                      ? Center(
                          child: Padding(
                            padding: const EdgeInsets.all(24.0),
                            child: Text(
                              l10n.translate('no_posts_found'),
                              style: const TextStyle(color: Colors.grey),
                            ),
                          ),
                        )
                      : ListView.builder(
                          padding: const EdgeInsets.all(16),
                          itemCount: posts.length,
                          itemBuilder: (context, index) {
                            final post = posts[index];
                            return Card(
                              margin: const EdgeInsets.only(bottom: 12),
                              child: ListTile(
                                contentPadding: const EdgeInsets.all(14),
                                title: Text(
                                  post.localizedTitle(langCode),
                                  style: const TextStyle(fontWeight: FontWeight.bold),
                                  textDirection: langCode == 'ar' ? TextDirection.rtl : TextDirection.ltr,
                                ),
                                subtitle: Padding(
                                  padding: const EdgeInsets.only(top: 6),
                                  child: Text(
                                    post.localizedSubtitle(langCode),
                                    maxLines: 2,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                trailing: const Icon(Icons.arrow_forward_ios, size: 14),
                                onTap: () {
                                  Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                      builder: (_) => PostDetailScreen(post: post),
                                    ),
                                  );
                                },
                              ),
                            );
                          },
                        ),
                ),
              ],
            ),
    );
  }
}
