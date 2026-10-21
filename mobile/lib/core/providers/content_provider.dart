
import 'dart:async';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/category_model.dart';
import '../models/post_model.dart';

class ContentProvider extends ChangeNotifier {
  static const String _databaseUrl =
      'https://gen-lang-client-0048418625-default-rtdb.asia-southeast1.firebasedatabase.app';

  late final FirebaseDatabase _database;

  List<CategoryModel> _categories = [];
  List<PostModel> _posts = [];
  List<String> _bookmarkedPostIds = [];

  bool _isLoading = true;
  String? _errorMessage;

  StreamSubscription<DatabaseEvent>? _postsSubscription;
  StreamSubscription<DatabaseEvent>? _categoriesSubscription;

  List<CategoryModel> get categories => _categories;
  List<PostModel> get posts => _posts;
  List<String> get bookmarkedPostIds => _bookmarkedPostIds;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  ContentProvider() {
    _database = FirebaseDatabase.instanceFor(
      app: Firebase.app(),
      databaseURL: _databaseUrl,
    );

    _loadBookmarks();
    initSubscriptions();
  }

  Map<String, dynamic> _toStringMap(Object? value) {
    if (value is Map) {
      return value.map(
        (key, value) => MapEntry(key.toString(), value),
      );
    }
    return <String, dynamic>{};
  }

  List<CategoryModel> _sortCategoriesHierarchically(
    List<CategoryModel> items,
  ) {
    final byId = <String, CategoryModel>{
      for (final item in items) item.id: item,
    };
    int compare(CategoryModel a, CategoryModel b) {
      final orderComparison = a.order.compareTo(b.order);
      if (orderComparison != 0) return orderComparison;
      final nameComparison = a.nameBn.compareTo(b.nameBn);
      if (nameComparison != 0) return nameComparison;
      return a.id.compareTo(b.id);
    }

    final roots = <CategoryModel>[];
    final childrenByParent = <String, List<CategoryModel>>{};
    for (final item in items) {
      final parentId = item.parentId;
      if (parentId == null || !byId.containsKey(parentId)) {
        roots.add(item);
      } else {
        (childrenByParent[parentId] ??= <CategoryModel>[]).add(item);
      }
    }

    roots.sort(compare);
    for (final siblings in childrenByParent.values) {
      siblings.sort(compare);
    }

    final result = <CategoryModel>[];
    final visited = <String>{};
    void visit(CategoryModel item) {
      if (!visited.add(item.id)) return;
      result.add(item);
      for (final child in childrenByParent[item.id] ?? const <CategoryModel>[]) {
        visit(child);
      }
    }

    for (final root in roots) {
      visit(root);
    }
    // Keep orphaned or malformed records visible rather than silently dropping them.
    final remaining = List<CategoryModel>.from(items)..sort(compare);
    for (final item in remaining) {
      visit(item);
    }
    return result;
  }

  void initSubscriptions() {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    _categoriesSubscription?.cancel();
    _postsSubscription?.cancel();

    _categoriesSubscription = _database
        .ref('categories')
        .onValue
        .listen(
      (event) {
        final raw = event.snapshot.value;
        final result = <CategoryModel>[];

        if (raw is Map) {
          raw.forEach((key, value) {
            if (value is Map) {
              final data = _toStringMap(value);
              final category = CategoryModel.fromRealtimeDatabase(
                key.toString(),
                data,
              );

              if (category.isActive) {
                result.add(category);
              }
            }
          });
        }

        _categories = _sortCategoriesHierarchically(result);
        notifyListeners();
      },
      onError: (Object error) {
        debugPrint('Realtime Database categories error: $error');
        _errorMessage = 'Could not load categories.';
        notifyListeners();
      },
    );

    _postsSubscription = _database
        .ref('posts')
        .onValue
        .listen(
      (event) {
        final raw = event.snapshot.value;
        final result = <PostModel>[];

        if (raw is Map) {
          raw.forEach((key, value) {
            if (value is Map) {
              final data = _toStringMap(value);
              final post = PostModel.fromRealtimeDatabase(
                (data['id'] ?? key).toString(),
                data,
              );
              final status = post.status.trim().toLowerCase();
              if (status == 'published' || status == 'publish' || status == 'public') {
                result.add(post);
              }
            }
          });
        } else if (raw is List) {
          for (var index = 0; index < raw.length; index++) {
            final value = raw[index];
            if (value is Map) {
              final data = _toStringMap(value);
              final post = PostModel.fromRealtimeDatabase(
                (data['id'] ?? 'post_$index').toString(),
                data,
              );
              final status = post.status.trim().toLowerCase();
              if (status == 'published' || status == 'publish' || status == 'public') {
                result.add(post);
              }
            }
          }
        }
        debugPrint('Noor Daily: received ${result.length} published posts from Firebase.');

        result.sort((a, b) {
          final timeA = a.createdAt ??
              DateTime.fromMillisecondsSinceEpoch(0);
          final timeB = b.createdAt ??
              DateTime.fromMillisecondsSinceEpoch(0);
          return timeB.compareTo(timeA);
        });

        _posts = result;
        _isLoading = false;
        _errorMessage = null;
        notifyListeners();
      },
      onError: (Object error) {
        debugPrint('Realtime Database posts error: $error');
        _isLoading = false;
        _errorMessage =
            'Could not load content. Please check internet connection.';
        notifyListeners();
      },
    );
  }

  Future<void> _loadBookmarks() async {
    final prefs = await SharedPreferences.getInstance();
    _bookmarkedPostIds = prefs.getStringList('saved_post_ids') ?? [];
    notifyListeners();
  }

  Future<void> toggleBookmark(String postId) async {
    final prefs = await SharedPreferences.getInstance();

    if (_bookmarkedPostIds.contains(postId)) {
      _bookmarkedPostIds.remove(postId);
    } else {
      _bookmarkedPostIds.add(postId);
    }

    await prefs.setStringList(
      'saved_post_ids',
      _bookmarkedPostIds,
    );
    notifyListeners();
  }

  bool isBookmarked(String postId) =>
      _bookmarkedPostIds.contains(postId);

  List<PostModel> get savedPosts => _posts
      .where((post) => _bookmarkedPostIds.contains(post.id))
      .toList();

  List<PostModel> getPostsByCategory(String categoryId) =>
      _posts.where((post) => post.categoryId == categoryId).toList();

  PostModel? get dailyAyatPost =>
      _posts.isEmpty ? null : _posts.first;

  @override
  void dispose() {
    _postsSubscription?.cancel();
    _categoriesSubscription?.cancel();
    super.dispose();
  }
}
