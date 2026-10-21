import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../core/models/post_model.dart';
import '../../core/providers/locale_provider.dart';
import '../../core/providers/content_provider.dart';
import '../../core/theme/islamic_theme.dart';

class PostDetailScreen extends StatefulWidget {
  final PostModel post;

  const PostDetailScreen({super.key, required this.post});

  @override
  State<PostDetailScreen> createState() => _PostDetailScreenState();
}

class _PostDetailScreenState extends State<PostDetailScreen> {
  double _fontSize = 16.0;

  @override
  Widget build(BuildContext context) {
    final localeProvider = context.watch<LocaleProvider>();
    final contentProvider = context.watch<ContentProvider>();
    final langCode = localeProvider.locale.languageCode;
    final isBookmarked = contentProvider.isBookmarked(widget.post.id);

    final title = widget.post.localizedTitle(langCode);
    final subtitle = widget.post.localizedSubtitle(langCode);
    final body = widget.post.localizedBody(langCode);
    final isRtl = langCode == 'ar';

    return Scaffold(
      appBar: AppBar(
        actions: [
          // Text Zoom controls
          IconButton(
            icon: const Icon(Icons.text_decrease, size: 20),
            onPressed: () {
              if (_fontSize > 13) setState(() => _fontSize -= 1.5);
            },
            tooltip: 'Decrease font size',
          ),
          IconButton(
            icon: const Icon(Icons.text_increase, size: 20),
            onPressed: () {
              if (_fontSize < 24) setState(() => _fontSize += 1.5);
            },
            tooltip: 'Increase font size',
          ),
          // Bookmark Toggle
          IconButton(
            icon: Icon(
              isBookmarked ? Icons.bookmark : Icons.bookmark_border,
              color: isBookmarked ? IslamicTheme.accentGold : null,
            ),
            onPressed: () {
              contentProvider.toggleBookmark(widget.post.id);
            },
            tooltip: 'Bookmark',
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
        child: Column(
          crossAxisAlignment: isRtl ? CrossAxisAlignment.end : CrossAxisAlignment.start,
          children: [
            // Cover Image
            if (widget.post.imageUrl != null && widget.post.imageUrl!.isNotEmpty) ...[
              ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: CachedNetworkImage(
                  imageUrl: widget.post.imageUrl!,
                  width: double.infinity,
                  height: 200,
                  fit: BoxFit.cover,
                  errorWidget: (context, url, error) => const SizedBox.shrink(),
                ),
              ),
              const SizedBox(height: 16),
            ],

            // Article Title
            Text(
              title,
              style: TextStyle(
                fontSize: _fontSize + 6,
                fontWeight: FontWeight.bold,
                height: 1.35,
              ),
              textDirection: isRtl ? TextDirection.rtl : TextDirection.ltr,
            ),
            const SizedBox(height: 8),

            // Subtitle
            if (subtitle.isNotEmpty) ...[
              Text(
                subtitle,
                style: TextStyle(
                  fontSize: _fontSize,
                  color: Theme.of(context).textTheme.bodySmall?.color?.withValues(alpha: 0.8),
                  height: 1.4,
                  fontStyle: FontStyle.italic,
                ),
                textDirection: isRtl ? TextDirection.rtl : TextDirection.ltr,
              ),
              const SizedBox(height: 16),
            ],

            const Divider(height: 24),

            // Body Text
            Text(
              body,
              style: TextStyle(
                fontSize: _fontSize,
                height: isRtl ? 1.8 : 1.6,
                letterSpacing: 0.2,
              ),
              textDirection: isRtl ? TextDirection.rtl : TextDirection.ltr,
            ),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }
}
