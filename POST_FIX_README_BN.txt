NOOR DAILY - POST LOADING PATCH

এই ZIP-এ পোস্টের ফিল্ডের নামের পার্থক্য সামলানোর জন্য ২টি Flutter ফাইল আপডেট করা হয়েছে।

পরিবর্তিত ফাইল:
1) mobile/lib/core/models/post_model.dart
2) mobile/lib/core/providers/content_provider.dart

ব্যবহার (Windows):
1. পুরোনো প্রজেক্টের একটি কপি/ব্যাকআপ রাখুন।
2. এই ZIP Extract করুন।
3. Extract করা ফোল্ডারের mobile/lib/core/models/post_model.dart ফাইলটি আপনার প্রজেক্টের একই পথের ফাইল দিয়ে Replace করুন।
4. একইভাবে mobile/lib/core/providers/content_provider.dart Replace করুন।
5. Android Studio-তে Terminal খুলে mobile ফোল্ডারে গিয়ে `flutter pub get` এবং তারপর `flutter run` চালান।

গুরুত্বপূর্ণ: এই প্যাচ কোডের ডেটা-ফিল্ড সামঞ্জস্য ঠিক করে। Firebase Rules/প্রজেক্ট অ্যাক্সেসে সমস্যা থাকলে সেটি আলাদাভাবে ঠিক করতে হবে। ZIP-এর বাকি ফাইল অপরিবর্তিত।
