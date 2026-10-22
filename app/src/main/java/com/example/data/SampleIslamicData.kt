package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.CityLocation
import com.example.model.IslamicCategory
import com.example.model.IslamicPost
import com.example.model.IslamicSubcategory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SampleIslamicData {

  val defaultCategories = listOf(
    IslamicCategory(
      id = "cat_namaz",
      nameEn = "Namaz & Prayer Rules",
      nameBn = "নামাজ ও দোয়ার নিয়ম",
      nameAr = "أحكام الصلاة والأدعية",
      depth = 0,
      iconName = "prayer",
      subcategoryCount = 8,
      postCount = 10
    ),
    IslamicCategory(
      id = "cat_roza",
      nameEn = "Fasting & Ramadan",
      nameBn = "রোজা ও মাহে রমজান",
      nameAr = "الصيام وشهر رمضان",
      depth = 0,
      iconName = "moon",
      subcategoryCount = 4,
      postCount = 5
    ),
    IslamicCategory(
      id = "cat_quran",
      nameEn = "Holy Quran & Tafsir",
      nameBn = "পবিত্র কুরআন ও তাফসির",
      nameAr = "القرآن الكريم والتفسير",
      depth = 0,
      iconName = "quran",
      subcategoryCount = 4,
      postCount = 5
    ),
    IslamicCategory(
      id = "cat_hadith",
      nameEn = "Prophetic Hadiths",
      nameBn = "রাসূলের (সা.) হাদিস শরিফ",
      nameAr = "الأحاديث النبوية الشريفة",
      depth = 0,
      iconName = "hadith",
      subcategoryCount = 3,
      postCount = 4
    ),
    IslamicCategory(
      id = "cat_duas",
      nameEn = "Daily Duas & Adhkar",
      nameBn = "দৈনন্দিন দোয়া ও জিকির",
      nameAr = "الأذكار والأدعية اليومية",
      depth = 0,
      iconName = "dua",
      subcategoryCount = 4,
      postCount = 5
    ),
    IslamicCategory(
      id = "cat_zakat",
      nameEn = "Zakat & Charity",
      nameBn = "যাকাত ও সদকা",
      nameAr = "الزكاة والصدقات",
      depth = 0,
      iconName = "charity",
      subcategoryCount = 2,
      postCount = 3
    ),
  )

  val defaultSubcategories = listOf(
    // Subcategories for cat_namaz (নামাজ ও দোয়ার নিয়ম)
    IslamicSubcategory(
      id = "sub_fajr",
      categoryId = "cat_namaz",
      nameEn = "Fajr Prayer (Dawn)",
      nameBn = "ফজরের নামাজ",
      nameAr = "صلاة الفجر",
      iconName = "wb_twilight",
      order = 1,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_zuhr",
      categoryId = "cat_namaz",
      nameEn = "Zuhr Prayer (Noon)",
      nameBn = "যোহরের নামাজ",
      nameAr = "صلاة الظهر",
      iconName = "wb_sunny",
      order = 2,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_asr",
      categoryId = "cat_namaz",
      nameEn = "Asr Prayer (Afternoon)",
      nameBn = "আসরের নামাজ",
      nameAr = "صلاة العصر",
      iconName = "brightness_6",
      order = 3,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_maghrib",
      categoryId = "cat_namaz",
      nameEn = "Maghrib Prayer (Sunset)",
      nameBn = "মাগরিবের নামাজ",
      nameAr = "صلاة المغرب",
      iconName = "bedtime",
      order = 4,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_isha",
      categoryId = "cat_namaz",
      nameEn = "Isha & Witr Prayer (Night)",
      nameBn = "এশার নামাজ ও বিতর",
      nameAr = "صلاة العشاء والوتر",
      iconName = "nightlight_round",
      order = 5,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_tahajjud",
      categoryId = "cat_namaz",
      nameEn = "Tahajjud & Nafl Prayers",
      nameBn = "তাহাজ্জুদ ও নফল নামাজ",
      nameAr = "صلاة التهجد والنوافل",
      iconName = "stars",
      order = 6,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_jummah_eid",
      categoryId = "cat_namaz",
      nameEn = "Jummah & Eid Prayers",
      nameBn = "জুমুআ ও দুই ঈদের নামাজ",
      nameAr = "صلاة الجمعة والعيدين",
      iconName = "mosque",
      order = 7,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_janaza_qaza",
      categoryId = "cat_namaz",
      nameEn = "Janaza & Qaza Rules",
      nameBn = "জানাজা ও কাজা নামাজের নিয়ম",
      nameAr = "صلاة الجنازة والقضاء",
      iconName = "access_time",
      order = 8,
      postCount = 1
    ),

    // Subcategories for cat_roza (রোজা ও মাহে রমজান)
    IslamicSubcategory(
      id = "sub_roza_niyat",
      categoryId = "cat_roza",
      nameEn = "Fasting Intention & Rules",
      nameBn = "রোজার নিয়ত ও বিধান",
      nameAr = "نية الصيام وأحكامه",
      iconName = "favorite",
      order = 1,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_sehri_iftar",
      categoryId = "cat_roza",
      nameEn = "Sehri & Iftar Duas & Time",
      nameBn = "সেহরি ও ইফতারের দোয়া ও নিয়ম",
      nameAr = "أدعية السحور والإفطار",
      iconName = "restaurant",
      order = 2,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_tarabi",
      categoryId = "cat_roza",
      nameEn = "Tarabi Prayer & Duas",
      nameBn = "তারাবীহ নামাজের নিয়ম ও দোয়া",
      nameAr = "صلاة التراويح وأدعيتها",
      iconName = "nights_stay",
      order = 3,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_itikaf",
      categoryId = "cat_roza",
      nameEn = "Itikaf & Shab-e-Qadr",
      nameBn = "ইতিকাফ ও লাইলাতুল কদর",
      nameAr = "الاعتكاف وليلة القدر",
      iconName = "brightness_high",
      order = 4,
      postCount = 1
    ),

    // Subcategories for cat_quran (পবিত্র কুরআন ও তাফসির)
    IslamicSubcategory(
      id = "sub_surah_baqarah",
      categoryId = "cat_quran",
      nameEn = "Surah Al-Baqarah",
      nameBn = "সূরা আল-বাকারা",
      nameAr = "سورة البقرة",
      iconName = "menu_book",
      order = 1,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_surah_yasin",
      categoryId = "cat_quran",
      nameEn = "Surah Yasin & Benefits",
      nameBn = "সূরা ইয়াসীন ও ফজিলত",
      nameAr = "سورة يس وفضلها",
      iconName = "auto_stories",
      order = 2,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_ayat_kursi",
      categoryId = "cat_quran",
      nameEn = "Ayatul Kursi & Protection",
      nameBn = "আয়াতুল কুরসী ও শেষ ৩ আয়াত",
      nameAr = "آية الكرسي والمعوذات",
      iconName = "shield",
      order = 3,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_quran_recitation",
      categoryId = "cat_quran",
      nameEn = "Quran Recitation & Tajweed",
      nameBn = "কুরআন তেলাওয়াত ও তাজবীদ",
      nameAr = "تلاوة القرآن وأحكام التجويد",
      iconName = "record_voice_over",
      order = 4,
      postCount = 1
    ),

    // Subcategories for cat_hadith (রাসূলের হাদিস শরিফ)
    IslamicSubcategory(
      id = "sub_iman_purity",
      categoryId = "cat_hadith",
      nameEn = "Faith & Purity (Taharah)",
      nameBn = "ঈমান ও পবিত্রতা (তাহরাত)",
      nameAr = "الإيمان والطهارة",
      iconName = "water_drop",
      order = 1,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_akhlaq",
      categoryId = "cat_hadith",
      nameEn = "Manners & Good Deeds",
      nameBn = "উত্তম চরিত্র ও সুন্দর আমল",
      nameAr = "حسن الخلق والآداب",
      iconName = "sentiment_satisfied_alt",
      order = 2,
      postCount = 1
    ),
    IslamicSubcategory(
      id = "sub_bukhari_core",
      categoryId = "cat_hadith",
      nameEn = "Selected Sahih Hadiths",
      nameBn = "সহিহ হাদিস সংকলন",
      nameAr = "مختارات من صحيح الحديث",
      iconName = "bookmark",
      order = 3,
      postCount = 1
    ),

    // Subcategories for cat_duas (দৈনন্দিন দোয়া ও জিকির)
    IslamicSubcategory(
      id = "sub_morning_evening",
      categoryId = "cat_duas",
      nameEn = "Morning & Evening Adhkar",
      nameBn = "সকাল-সন্ধ্যার মাসনূন জিকির",
      nameAr = "أذكار الصباح والمساء",
      iconName = "wb_sunny",
      order = 1,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_daily_life",
      categoryId = "cat_duas",
      nameEn = "Daily Life Masnoon Duas",
      nameBn = "দৈনন্দিন জীবনের মাসনূন দোয়া",
      nameAr = "أدعية الحياة اليومية",
      iconName = "pan_tool",
      order = 2,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_forgiveness",
      categoryId = "cat_duas",
      nameEn = "Repentance & Inner Peace",
      nameBn = "তওবা, ক্ষমা ও মনের প্রশান্তি",
      nameAr = "التوبة والاستغفار وراحة القلب",
      iconName = "favorite_border",
      order = 3,
      postCount = 1
    ),

    // Subcategories for cat_zakat (যাকাত ও সদকা)
    IslamicSubcategory(
      id = "sub_zakat_calc",
      categoryId = "cat_zakat",
      nameEn = "Zakat Rules & Nisab",
      nameBn = "যাকাতের নিসাব ও হিসাব",
      nameAr = "نصاب الزكاة وحسابها",
      iconName = "calculate",
      order = 1,
      postCount = 2
    ),
    IslamicSubcategory(
      id = "sub_sadqah",
      categoryId = "cat_zakat",
      nameEn = "Sadaqah & Charity Virtues",
      nameBn = "সদকা ও দানের ফযিলত",
      nameAr = "فضل الصدقة والإنفاق",
      iconName = "volunteer_activism",
      order = 2,
      postCount = 1
    )
  )

  var categories by mutableStateOf<List<IslamicCategory>>(defaultCategories)
  var subcategories by mutableStateOf<List<IslamicSubcategory>>(defaultSubcategories)

  val defaultPosts = listOf(
    // 1. Ayat of the day in Quran -> Surah Al-Baqarah
    IslamicPost(
      id = "post_ayat_today",
      categoryId = "cat_quran",
      subcategoryId = "sub_surah_baqarah",
      categoryNameEn = "Holy Quran & Tafsir",
      categoryNameBn = "পবিত্র কুরআন ও তাফসির",
      categoryNameAr = "القرآن الكريم",
      subcategoryNameEn = "Surah Al-Baqarah",
      subcategoryNameBn = "সূরা আল-বাকারা",
      subcategoryNameAr = "سورة البقرة",
      titleEn = "Ayat of the Day: Surah Al-Baqarah (2:286)",
      titleBn = "আজকের আয়াত: সূরা আল-বাকারা (২:২৮৬)",
      titleAr = "آية اليوم: سورة البقرة (٢٨٦)",
      subtitleEn = "Allah does not charge a soul except with that within its capacity.",
      subtitleBn = "আল্লাহ কোনো ব্যক্তিকে তার সাধ্যের বাইরে কোনো দায়িত্ব চাপিয়ে দেন না।",
      subtitleAr = "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
      bodyEn = """
        بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
        
        "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ"
        
        "Allah does not burden a soul beyond that it can bear. It will have [the consequence of] what [good] it has gained, and it will bear [the consequence of] what [evil] it has earned."
        
        Tafsir Note:
        This verse brings profound tranquility to the believer's heart. It affirms that whatever trials, hardships, or duties you face today, Allah knows your inner strength and will grant you ease after difficulty.
      """.trimIndent(),
      bodyBn = """
        বিসমিল্লাহির রাহমানির রাহিম
        
        "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ"
        
        অনুবাদ:
        "আল্লাহ কাউকে তার সাধ্যাতীত কোনো কাজের ভার দেন না। সে যা ভালো কাজ করে তার প্রতিফল তারই জন্য এবং সে যা মন্দ কাজ করে তার প্রতিফলও তারই উপর বর্তায়।" (সূরা আল-বাকারা: ২৮৬)
        
        শিক্ষা ও তাৎপর্য:
        এই আয়াত মুমিনের অন্তরে অফুরন্ত আশার সঞ্চার করে। জীবনে কোনো বিপদে নিরাশ হওয়া উচিত নয়, কারণ আল্লাহ তায়ালা কোনো বান্দাকে এমন কোনো কঠিন পরীক্ষার সম্মুখীন করেন না যা অতিক্রম করার ক্ষমতা তার নেই।
      """.trimIndent(),
      bodyAr = """
        بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
        
        ﴿لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ ۗ رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا﴾
        
        سورة البقرة - الآية ٢٨٦
        
        تفسير موجز:
        هذه الآية الكريمة قاعدة عظيمة في رفع الحرج واللطف بالعباد، فالله عز وجل رحيم بعباده لا يأمرهم بما يعجزون عنه، ولا يبتليهم بما لا يطيقون.
      """.trimIndent(),
      source = "Surah Al-Baqarah (2:286)",
      readTimeMinutes = 2,
      isAyatOfTheDay = true,
    ),

    // 2. Hadith in Hadith -> Faith & Purity
    IslamicPost(
      id = "post_daily_hadith",
      categoryId = "cat_hadith",
      subcategoryId = "sub_iman_purity",
      categoryNameEn = "Prophetic Hadiths",
      categoryNameBn = "রাসূলের (সা.) হাদিস শরিফ",
      categoryNameAr = "الحديث النبوي",
      subcategoryNameEn = "Faith & Purity (Taharah)",
      subcategoryNameBn = "ঈমান ও পবিত্রতা (তাহরাত)",
      subcategoryNameAr = "الإيمان والطهارة",
      titleEn = "Daily Hadith: The Best of Deeds",
      titleBn = "দৈনিক হাদিস: সর্বোত্তম ও প্রিয় আমল",
      titleAr = "حديث اليوم: أحب الأعمال إلى الله",
      subtitleEn = "The Prophet (ﷺ) was asked: 'Which deed is the dearest to Allah?'",
      subtitleBn = "রাসূলুল্লাহকে (সা.) জিজ্ঞেস করা হলো: 'আল্লাহর কাছে সবচেয়ে প্রিয় আমল কোনটি?'",
      subtitleAr = "عن عبد الله بن مسعود رضي الله عنه قال: سألت النبي صلى الله عليه وسلم أي العمل أحب إلى الله؟",
      bodyEn = """
        Abdullah ibn Mas'ud reported:
        I asked the Messenger of Allah (ﷺ): "Which deed is the dearest to Allah?"
        He replied: "To perform prayer at its proper earliest time."
        I asked: "What next?"
        He said: "To be good and dutiful to your parents."
        I asked: "What next?"
        He said: "Jihad in the cause of Allah."
        [Sahih al-Bukhari: 527, Sahih Muslim: 85]
      """.trimIndent(),
      bodyBn = """
        হজরত আব্দুল্লাহ ইবনে মাসউদ (রা.) থেকে বর্ণিত:
        তিনি বলেন, আমি রাসুলুল্লাহকে (সা.) জিজ্ঞাসা করলাম, 'আল্লাহর কাছে সবচেয়ে প্রিয় আমল কোনটি?'
        তিনি বললেন: 'ওয়াক্তমতো সময়মতো নামাজ আদায় করা।'
        আমি বললাম, 'এরপর কোনটি?'
        তিনি বললেন: 'পিতামাতার সাথে সদ্ব্যবহার ও সেবা করা।'
        আমি বললাম, 'এরপর কোনটি?'
        তিনি বললেন: 'আল্লাহর রাস্তায় জিহাদ বা সর্বোচ্চ প্রচেষ্টা করা।'
        [সহিহ বুখারি: ৫২৭, সহিহ মুসলিম: ৮৫]
      """.trimIndent(),
      bodyAr = """
        عَنْ عَبْدِ اللَّهِ بْنِ مَسْعُودٍ رَضِيَ اللَّهُ عَنْهُ قَالَ:
        سَأَلْتُ النَّبِيَّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: أَيُّ الْعَمَلِ أَحَبُّ إِلَى اللَّهِ؟
        قَالَ: «الصَّلَاةُ عَلَى وَقْتِهَا»
        قَالَ: ثُمَّ أَيٌّ؟ قَالَ: «ثُمَّ بِرُّ الْوَالِدَيْنِ»
        [رواه البخاري ومسلم]
      """.trimIndent(),
      source = "Sahih al-Bukhari 527, Muslim 85",
      readTimeMinutes = 2,
      isDailyHadith = true,
    ),

    // 3. Fajr Namaz -> Fajr Rules & Method
    IslamicPost(
      id = "post_fajr_rules",
      categoryId = "cat_namaz",
      subcategoryId = "sub_fajr",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      subcategoryNameEn = "Fajr Prayer (Dawn)",
      subcategoryNameBn = "ফজরের নামাজ",
      subcategoryNameAr = "صلاة الفجر",
      titleEn = "Fajr Prayer: Niyat, Sunnah & Fard Rules",
      titleBn = "ফজরের নামাজের নিয়ত ও পড়ার সঠিক নিয়ম",
      titleAr = "كيفية صلاة الفجر وسنتها وفضلها",
      subtitleEn = "Complete guide to praying 2 Rak'ahs Sunnah and 2 Rak'ahs Fard of Fajr.",
      subtitleBn = "ফজরের ২ রাকাত সুন্নত ও ২ রাকাত ফরজ আদায়ের পূর্ণাঙ্গ পদ্ধতি।",
      subtitleAr = "تفصيل ركعتي الفجر وسنة الفجر القبلية المؤكدة.",
      bodyEn = """
        Fajr consists of total 4 Rak'ahs:
        1. 2 Rak'ahs Sunnah Mu'akkadah (prayed first, highly emphasized by the Prophet).
        2. 2 Rak'ahs Fard (obligatory, recited out loud by the Imam in congregation).
        
        The 2 Rak'ahs Sunnah:
        The Prophet (ﷺ) said: "The two Rak'ahs of Fajr are better than this world and everything in it." [Sahih Muslim]
        
        Sunnah Recitation:
        In the first Rak'ah recite Surah Al-Kafirun, and in the second Rak'ah recite Surah Al-Ikhlas after Surah Al-Fatihah.
      """.trimIndent(),
      bodyBn = """
        ফজরের নামাজে মোট ৪ রাকাত:
        ১. প্রথমে ২ রাকাত সুন্নত (সুন্নাতে মুয়াক্কাদা)।
        ২. এরপর ২ রাকাত ফরজ।
        
        ফজরের সুন্নতের গুরুত্ব:
        রাসূলুল্লাহ (সা.) বলেছেন: "ফজরের দুই রাকাত সুন্নত দুনিয়া ও তার মধ্যকার সবকিছুর চেয়েও উত্তম।" [সহিহ মুসলিম]
        
        পড়ার নিয়ম:
        - প্রথমে দাঁড়িয়ে অন্তরে ২ রাকাত সুন্নতের নিয়ত করে 'আল্লাহু আকবার' বলে হাত বাঁধবেন।
        - সানা, আউজুবিল্লাহ, বিসমিল্লাহ পড়ে সূরা ফাতিহা ও অন্য সূরা মেলাবেন।
        - রুকু ও দুই সেজদা করে দ্বিতীয় রাকাতও একইভাবে পড়ে তাশাহহুদ, দরূদ ও দোয়া মাসূরা পড়ে সালাম ফেরাবেন।
        - এরপর একইভাবে ২ রাকাত ফরজ নামাজ আদায় করবেন।
      """.trimIndent(),
      bodyAr = """
        صلاة الفجر أربع ركعات: ركعتان سنة مؤكدة قبلية، وركعتان فريضة جهرية.
        قال النبي ﷺ: «ركعتا الفجر خير من الدنيا وما فيها» [رواه مسلم].
      """.trimIndent(),
      source = "Sahih Muslim, Abu Dawud",
      readTimeMinutes = 3,
    ),

    // 4. Fajr Virtue
    IslamicPost(
      id = "post_fajr_virtue",
      categoryId = "cat_namaz",
      subcategoryId = "sub_fajr",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      subcategoryNameEn = "Fajr Prayer (Dawn)",
      subcategoryNameBn = "ফজরের নামাজ",
      subcategoryNameAr = "صلاة الفجر",
      titleEn = "The Shield of Dawn: Fajr in Congregation",
      titleBn = "ফজর নামাজের অপরিসীম ফজিলত ও জামাত",
      titleAr = "فضل صلاة الفجر وبركة البكور",
      subtitleEn = "Whoever prays the dawn prayer is under the protection of Allah.",
      subtitleBn = "যে ব্যক্তি ফজরের নামাজ আদায় করল, সে আল্লাহর জিম্মাদারীতে চলে গেল।",
      subtitleAr = "من صلى الصبح فهو في ذمة الله.",
      bodyEn = """
        The Messenger of Allah (ﷺ) said:
        "Whoever prays the dawn prayer in congregation, it is as if he had prayed the whole night." [Sahih Muslim]
        "Whoever prays the morning prayer is under the protection of Allah."
      """.trimIndent(),
      bodyBn = """
        রাসূলুল্লাহ (সা.) বলেছেন:
        "যে ব্যক্তি জামাতের সাথে ফজরের নামাজ আদায় করে, সে যেন সারারাত জেগে নামাজ আদায় করল।" [সহিহ মুসলিম]
        তিনি আরও বলেছেন:
        "যে ব্যক্তি ফজরের নামাজ পড়ল, সে আল্লাহর নিরাপত্তা ও দায়িত্বে চলে গেল।"
      """.trimIndent(),
      bodyAr = """
        قال صلى الله عليه وسلم:
        «من صلى الصبح في جماعة فكأنما صلى الليل كله» [رواه مسلم].
      """.trimIndent(),
      source = "Sahih Muslim 656",
      readTimeMinutes = 2,
    ),

    // 5. Zuhr Namaz
    IslamicPost(
      id = "post_zuhr_rules",
      categoryId = "cat_namaz",
      subcategoryId = "sub_zuhr",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      subcategoryNameEn = "Zuhr Prayer (Noon)",
      subcategoryNameBn = "যোহরের নামাজ",
      subcategoryNameAr = "صلاة الظهر",
      titleEn = "Zuhr Prayer: Rakat Count & Method",
      titleBn = "যোহরের নামাজের বিস্তারিত নিয়ম ও রাকাত সংখ্যা",
      titleAr = "أحكام وركعات صلاة الظهر",
      subtitleEn = "Rules for 4 Sunnah, 4 Fard, 2 Sunnah, and 2 Nafl.",
      subtitleBn = "যোহরের মোট ১২ রাকাত নামাজের সঠিক বিন্যাস ও নিয়ম।",
      subtitleAr = "تفصيل ركعات الظهر وسننها الرواتب المؤكدة.",
      bodyEn = """
        Zuhr consists of:
        - 4 Rak'ahs Sunnah Mu'akkadah before Fard
        - 4 Rak'ahs Fard
        - 2 Rak'ahs Sunnah Mu'akkadah after Fard
        - 2 Rak'ahs Nafl (optional recommended)
      """.trimIndent(),
      bodyBn = """
        যোহরের নামাজে মোট ১২ রাকাত পড়া উত্তম:
        ১. ৪ রাকাত সুন্নতে মুয়াক্কাদা (ফরজের আগে)।
        ২. ৪ রাকাত ফরজ।
        ৩. ২ রাকাত সুন্নতে মুয়াক্কাদা (ফরজের পরে)।
        ৪. ২ রাকাত নফল।
        
        ফরজের পূর্বের ৪ রাকাত অত্যন্ত গুরুত্বপূর্ণ, যা স্বয়ং রাসুলুল্লাহ (সা.) কখনো ছাড়তেন না।
      """.trimIndent(),
      bodyAr = """
        صلاة الظهر: أربع ركعات سنة قبلية، ثم أربع ركعات فرض سري، ثم ركعتان سنة بعدية وركعتان نفل.
      """.trimIndent(),
      source = "Tirmidhi, Bukhari",
      readTimeMinutes = 3,
    ),

    // 6. Asr Namaz
    IslamicPost(
      id = "post_asr_rules",
      categoryId = "cat_namaz",
      subcategoryId = "sub_asr",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      subcategoryNameEn = "Asr Prayer (Afternoon)",
      subcategoryNameBn = "আসরের নামাজ",
      subcategoryNameAr = "صلاة العصر",
      titleEn = "Asr Prayer: The Middle Prayer & Great Warning",
      titleBn = "আসরের নামাজের গুরুত্ব ও পড়ার নিয়ম",
      titleAr = "المحافظة على صلاة العصر (الصلاة الوسطى)",
      subtitleEn = "Guard strictly your habit of praying Asr before sunset.",
      subtitleBn = "কুরআনে বর্ণিত 'সালাতুল উসতা' বা মধ্যবর্তী নামাজের গুরুত্ব।",
      subtitleAr = "حذر النبي ﷺ من تضييع صلاة العصر ووصف فضلها العظيم.",
      bodyEn = """
        Asr consists of 4 Rak'ahs Fard. (4 Rak'ahs Sunnah Ghayr Mu'akkadah before Fard is highly rewarded).
        Allah says in the Quran: "Guard strictly the prayers, especially the middle prayer (Asr)." [Surah Al-Baqarah: 238]
      """.trimIndent(),
      bodyBn = """
        আসরের নামাজে ৪ রাকাত ফরজ। ফরজের পূর্বে ৪ রাকাত সুন্নতে গায়রে মুয়াক্কাদা পড়া অত্যন্ত সওয়াবের কাজ।
        আল্লাহ তায়ালা পবিত্র কুরআনে বলেন: "তোমরা নামাজের প্রতি যত্নবান হও, বিশেষ করে মধ্যবর্তী নামাজের (আসর)।" (সূরা আল-বাকারা: ২৩৮)
      """.trimIndent(),
      bodyAr = """
        صلاة العصر أربع ركعات فريضة، ويسن قبلها أربع ركعات.
      """.trimIndent(),
      source = "Surah Al-Baqarah 238",
      readTimeMinutes = 2,
    ),

    // 7. Maghrib Namaz
    IslamicPost(
      id = "post_maghrib_rules",
      categoryId = "cat_namaz",
      subcategoryId = "sub_maghrib",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      subcategoryNameEn = "Maghrib Prayer (Sunset)",
      subcategoryNameBn = "মাগরিবের নামাজ",
      subcategoryNameAr = "صلاة المغرب",
      titleEn = "Maghrib Prayer: Rules & Awwabin Prayer",
      titleBn = "মাগরিবের নামাজের নিয়ম ও আউওয়াবীন নামাজ",
      titleAr = "أحكام صلاة المغرب وصلاة الأوابين",
      subtitleEn = "3 Rak'ahs Fard, 2 Rak'ahs Sunnah, and 6 Rak'ahs Awwabin.",
      subtitleBn = "মাগরিবের নামাজের পর আউওয়াবীন পড়ার ফযিলত ও সঠিক নিয়ম।",
      subtitleAr = "تفصيل ركعات المغرب وفضل قيام ما بعد المغرب.",
      bodyEn = """
        Maghrib consists of:
        - 3 Rak'ahs Fard
        - 2 Rak'ahs Sunnah Mu'akkadah
        - 2 to 6 Rak'ahs Awwabin (optional Nafl after Sunnah)
      """.trimIndent(),
      bodyBn = """
        মাগরিবের নামাজে মোট ৭ রাকাত:
        ১. ৩ রাকাত ফরজ (প্রথম দুই রাকাতে ইমাম উচ্চস্বরে কেরাত পড়েন)।
        ২. ২ রাকাত সুন্নতে মুয়াক্কাদা।
        ৩. ২ রাকাত নফল।
        
        মাগরিবের পর ৬ রাকাত আউওয়াবীন নামাজ পড়া মুস্তাহাব। হাদিসে বর্ণিত আছে, যে ব্যক্তি মাগরিবের পর আউওয়াবীন পড়বে, আল্লাহ তার গুনাহ ক্ষমা করে দেবেন।
      """.trimIndent(),
      bodyAr = """
        صلاة المغرب ثلاث ركعات فريضة جهرية، ثم ركعتان سنة مؤكدة، ويستحب صلاة الأوابين بعدها.
      """.trimIndent(),
      source = "Sahih al-Bukhari, Tirmidhi",
      readTimeMinutes = 3,
    ),

    // 8. Isha & Witr
    IslamicPost(
      id = "post_isha_witr",
      categoryId = "cat_namaz",
      subcategoryId = "sub_isha",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      subcategoryNameEn = "Isha & Witr Prayer (Night)",
      subcategoryNameBn = "এশার নামাজ ও বিতর",
      subcategoryNameAr = "صلاة العشاء والوتر",
      titleEn = "Isha & Witr Prayer: Complete Method & Dua Qunoot",
      titleBn = "এশার নামাজ ও বিতর নামাজের দোয়া কুনুত",
      titleAr = "كيفية صلاة العشاء والوتر ودعاء القنوت",
      subtitleEn = "Step-by-step rules for 4 Fard, 2 Sunnah, and 3 Rak'ahs Witr with Dua Qunoot.",
      subtitleBn = "বিতর নামাজ পড়ার নিয়ম এবং দোয়া কুনুতের বাংলা অর্থ ও উচ্চারণ।",
      subtitleAr = "بيان ركعات العشاء وصلاة الوتر وقنوت الوتر المأثور.",
      bodyEn = """
        Isha consists of:
        - 4 Rak'ahs Fard
        - 2 Rak'ahs Sunnah Mu'akkadah
        - 2 Rak'ahs Nafl
        - 3 Rak'ahs Witr Wajib
        
        In the 3rd Rak'ah of Witr, after Surah Al-Fatihah and another Surah, say 'Allahu Akbar' raising both hands, fold hands again, and recite Dua Qunoot.
      """.trimIndent(),
      bodyBn = """
        এশার নামাজে মোট ১৭ রাকাত পর্যন্ত পড়া যায়:
        - ৪ রাকাত সুন্নত (গায়রে মুয়াক্কাদা)
        - ৪ রাকাত ফরজ
        - ২ রাকাত সুন্নত (সুন্নাতে মুয়াক্কাদা)
        - ২ রাকাত নফল
        - ৩ রাকাত বিতর (ওয়াজিব)
        - ২ রাকাত নফল
        
        বিতরের ৩য় রাকাতে সূরা ফাতিহা ও অন্য সূরা মেলানোর পর 'আল্লাহু আকবার' বলে কান পর্যন্ত হাত তুলে আবার হাত বাঁধবেন এবং দোয়া কুনুত পাঠ করবেন:
        'আল্লাহুম্মা ইন্না নাস্তাঈনুকা ওয়া নাস্তাগফিরুকা...'
      """.trimIndent(),
      bodyAr = """
        صلاة العشاء: أربع ركعات فرض جهرية، وركعتان سنة مؤكدة، وثلاث ركعات وتر واجبة مع دعاء القنوت.
      """.trimIndent(),
      source = "Abu Dawud, Nasai",
      readTimeMinutes = 3,
    ),

    // 9. Tahajjud in Namaz -> Tahajjud
    IslamicPost(
      id = "post_tahajjud",
      categoryId = "cat_namaz",
      subcategoryId = "sub_tahajjud",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      subcategoryNameEn = "Tahajjud & Nafl Prayers",
      subcategoryNameBn = "তাহাজ্জুদ ও নফল নামাজ",
      subcategoryNameAr = "صلاة التهجد والنوافل",
      titleEn = "The Hidden Power of Tahajjud Prayer",
      titleBn = "তাহাজ্জুদ নামাজের নিয়ম ও অলৌকিক ফজিলত",
      titleAr = "أسرار وفضل قيام الليل وصلاة التهجد",
      subtitleEn = "How to wake up and pray the night prayer that changes destiny.",
      subtitleBn = "রাতের নির্জনতায় রবের সামনে দাঁড়ানোর ফজিলত ও পদ্ধতি।",
      subtitleAr = "شرف المؤمن قيام الليل، ودعوة في السحر لا ترد بإذن الله.",
      bodyEn = """
        The Prophet (ﷺ) said: "The best prayer after the obligatory prayers is the night prayer." [Sahih Muslim]
        Tahajjud is prayed in sets of 2 Rak'ahs during the last third of the night before Fajr.
      """.trimIndent(),
      bodyBn = """
        তাহাজ্জুদ নামাজ অত্যন্ত বরকতময় এবং নফল ইবাদতের মধ্যে সর্বোত্তম।
        রাসূলুল্লাহ (সা.) বলেছেন: "ফরজ নামাজের পর সর্বোত্তম নামাজ হলো রাতের নামাজ (তাহাজ্জুদ)।" [সহিহ মুসলিম]
        এশার নামাজের পর রাতে কিছুটা ঘুমিয়ে মধ্যরাতের পর থেকে ফজরের পূর্ব পর্যন্ত সময় তাহাজ্জুদের জন্য উত্তম।
      """.trimIndent(),
      bodyAr = """
        قال رسول الله صلى الله عليه وسلم: «أفضل الصلاة بعد الفريضة صلاة الليل» [رواه مسلم].
      """.trimIndent(),
      source = "Sahih Muslim, Abu Dawud",
      readTimeMinutes = 3,
    ),

    // 10. Roza -> Roza Niyat
    IslamicPost(
      id = "post_roza_niyat",
      categoryId = "cat_roza",
      subcategoryId = "sub_roza_niyat",
      categoryNameEn = "Fasting & Ramadan",
      categoryNameBn = "রোজা ও মাহে রমজান",
      categoryNameAr = "الصيام وشهر رمضان",
      subcategoryNameEn = "Fasting Intention & Rules",
      subcategoryNameBn = "রোজার নিয়ত ও বিধান",
      subcategoryNameAr = "نية الصيام وأحكامه",
      titleEn = "Intention for Fasting (Roza Niyat) & What Breaks the Fast",
      titleBn = "রোজার সঠিক নিয়ত ও রোজা ভঙ্গের কারণসমূহ",
      titleAr = "نية صيام رمضان ومبطلات الصوم",
      subtitleEn = "Arabic and Bengali intention for fasting and things that invalidate the fast.",
      subtitleBn = "রোজার নিয়ত কীভাবে করবেন এবং যেসব কারণে রোজা ভেঙে যায়।",
      subtitleAr = "كيفية عقد النية والأمور التي تفطر الصائم.",
      bodyEn = """
        Intention (Niyyah) is primarily in the heart: intending sincerely to fast for the sake of Allah from dawn until sunset.
        
        Bengali / Arabic verbal intention:
        "নাওয়াইতু আন আসুমা গাদাম মিন শাহরি রমাদানাল মুবারাক..."
        "O Allah, I intend to observe the fast of Ramadan today for Your sake."
      """.trimIndent(),
      bodyBn = """
        রোজার নিয়তের মূল স্থান হলো অন্তর। মুখে উচ্চারণ করা জরুরি নয়, তবে মুখে বলা উত্তম:
        "নাওয়াইতু আন আসুমা গাদাম মিন শাহরি রমাদানাল মুবারাকি ফারদাল্লাকা ইয়া আল্লাহু ফাতাকাব্বাল মিন্নী ইন্নাকা আন্তাস সামিউল আলীম।"
        
        অর্থ:
        "হে আল্লাহ! আমি আগামীকাল তোমার সন্তুষ্টির উদ্দেশ্যে মাহে রমজানের ফরজ রোজা রাখার নিয়ত করলাম। তুমি আমার এই রোজা কবুল করো, নিশ্চয়ই তুমি সর্বশ্রোতা ও সর্বজ্ঞ।"
        
        যেসব কারণে রোজা ভেঙে যায়:
        ১. ইচ্ছাকৃত পানাহার বা ধূমপান করা।
        ২. স্ত্রী সহবাস করা।
        ৩. ইচ্ছাকৃত মুখ ভরে বমি করা ইত্যাদি।
      """.trimIndent(),
      bodyAr = """
        النية محلها القلب: أن ينوي الصائم الإمساك عن المفطرات من طلوع الفجر إلى غروب الشمس تقرباً إلى الله.
      """.trimIndent(),
      source = "Al-Fiqh al-Islami",
      readTimeMinutes = 3,
    ),

    // 11. Roza -> Sehri & Iftar Duas
    IslamicPost(
      id = "post_sehri_iftar",
      categoryId = "cat_roza",
      subcategoryId = "sub_sehri_iftar",
      categoryNameEn = "Fasting & Ramadan",
      categoryNameBn = "রোজা ও মাহে রমজান",
      categoryNameAr = "الصيام وشهر رمضان",
      subcategoryNameEn = "Sehri & Iftar Duas & Time",
      subcategoryNameBn = "সেহরি ও ইফতারের দোয়া ও নিয়ম",
      subcategoryNameAr = "أدعية السحور والإفطار",
      titleEn = "Duas for Sehri and Iftar with Sunnah Etiquettes",
      titleBn = "সেহরি ও ইফতারের দোয়া এবং সুন্নতি আদব",
      titleAr = "أدعية الإفطار والسحور وآدابها النبوية",
      subtitleEn = "Authentic supplications to recite at the time of breaking fast.",
      subtitleBn = "ইফতারের সময় দোয়া কবুল হওয়ার সুসংবাদ ও নবীজির (সা.) শেখানো দোয়া।",
      subtitleAr = "«ذهب الظمأ وابتلت العروق وثبت الأجر إن شاء الله».",
      bodyEn = """
        Iftar Dua (Sunnah):
        "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ"
        "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills." [Abu Dawud]
      """.trimIndent(),
      bodyBn = """
        ইফতারের সুন্নতি দোয়া:
        "জাহাবাজ জামাউ, ওয়াবতাল্লাতিল উরুকু, ওয়া সাবাতাল আজরু ইনশাআল্লাহ।"
        
        অর্থ:
        "পিপাসা দূর হলো, শিরা-উপশিরা সিক্ত হলো এবং আল্লাহর ইচ্ছায় সওয়াব নিশ্চিত হলো।" [আবু দাউদ: ২৩৫৭]
        
        আরেকটি প্রচলিত দোয়া:
        "আল্লাহুম্মা লাকা সুমতু ওয়া আলা রিজক্বিকা আফত্বারতু।"
        
        রাসূলুল্লাহ (সা.) বলেছেন: "রোজাদারের জন্য ইফতারের সময় এমন একটি দোয়া রয়েছে যা ফিরিয়ে দেওয়া হয় না (অবশ্যই কবুল হয়)।" [ইবনে মাজাহ]
      """.trimIndent(),
      bodyAr = """
        كان النبي ﷺ إذا أفطر قال: «ذهب الظمأ وابتلت العروق وثبت الأجر إن شاء الله» [رواه أبو داود].
      """.trimIndent(),
      source = "Sunan Abi Dawud 2357, Ibn Majah",
      readTimeMinutes = 2,
    ),

    // 12. Roza -> Tarabi
    IslamicPost(
      id = "post_tarabi_rules",
      categoryId = "cat_roza",
      subcategoryId = "sub_tarabi",
      categoryNameEn = "Fasting & Ramadan",
      categoryNameBn = "রোজা ও মাহে রমজান",
      categoryNameAr = "الصيام وشهر رمضان",
      subcategoryNameEn = "Tarabi Prayer & Duas",
      subcategoryNameBn = "তারাবীহ নামাজের নিয়ম ও দোয়া",
      subcategoryNameAr = "صلاة التراويح وأدعيتها",
      titleEn = "Tarabi Prayer: 20 Rak'ahs, Method & Duas",
      titleBn = "তারাবীহ নামাজের নিয়ম, ২০ রাকাত ও চার রাকাত পর পর পড়ার দোয়া",
      titleAr = "كيفية صلاة التراويح وأدعيتها المأثورة",
      subtitleEn = "Complete guide to praying Tarabi in congregation and at home.",
      subtitleBn = "রমজান মাসে তারাবীহ নামাজের নিয়ম ও বিশ্রামের দোয়া।",
      subtitleAr = "فضل قيام رمضان وصلاة التراويح جماعة في المسجد.",
      bodyEn = """
        The Prophet (ﷺ) said: "Whoever prays during the nights of Ramadan with faith and seeking reward, all his previous sins will be forgiven." [Sahih al-Bukhari]
      """.trimIndent(),
      bodyBn = """
        তারাবীহ নামাজ সুন্নাতে মুয়াক্কাদা। পুরুষদের জন্য জামাতের সাথে মসজিদে আদায় করা সুন্নতে মুয়াক্কাদা কেফায়া।
        
        চার রাকাত পর পর পড়ার তাসবীহ:
        "সুবহানা জিল মুলকি ওয়াল মালাকুত, সুবহানা জিল ইজ্জাতি ওয়াল আজমাতি ওয়াল হাইবাতি ওয়াল কুদরাতি ওয়াল কিবরিয়ায়ি ওয়াল জাবারুত..."
        
        তারাবীহর মোনাজাত:
        "আল্লাহুম্মা ইন্না নাসআলুকাল জান্নাতা ওয়া নাউজু বিকা মিনান নার..."
      """.trimIndent(),
      bodyAr = """
        قال رسول الله ﷺ: «من قام رمضان إيماناً واحتساباً غفر له ما تقدم من ذنبه» [رواه البخاري ومسلم].
      """.trimIndent(),
      source = "Sahih al-Bukhari 37",
      readTimeMinutes = 3,
    ),

    // 13. Duas -> Morning Adhkar
    IslamicPost(
      id = "post_morning_adhkar",
      categoryId = "cat_duas",
      subcategoryId = "sub_morning_evening",
      categoryNameEn = "Daily Duas & Adhkar",
      categoryNameBn = "দৈনন্দিন দোয়া ও জিকির",
      categoryNameAr = "الأذكار اليومية",
      subcategoryNameEn = "Morning & Evening Adhkar",
      subcategoryNameBn = "সকাল-সন্ধ্যার মাসনূন জিকির",
      subcategoryNameAr = "أذكار الصباح والمساء",
      titleEn = "Morning Adhkar: Fortress of the Believer",
      titleBn = "সকাল-সন্ধ্যার গুরুত্বপূর্ণ মাসনূন জিকির",
      titleAr = "أذكار الصباح والمساء حصن المسلم",
      subtitleEn = "Essential prophetic supplications for daily peace and protection.",
      subtitleBn = "প্রতিদিন সকালে ও সন্ধ্যায় পড়ার বিশেষ দোয়া ও আমল।",
      subtitleAr = "أدعية مأثورة تحفظك من كل سوء بفضل الله ورحمته.",
      bodyEn = """
        Sayyid al-Istighfar:
        "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ..."
        Whoever recites this with firm faith in the morning and dies before the evening will be among the people of Paradise. [Sahih al-Bukhari]
      """.trimIndent(),
      bodyBn = """
        সাইয়্যেদুল ইস্তিগফার (ক্ষমা প্রার্থনার শ্রেষ্ঠ দোয়া):
        "আল্লাহুম্মা আন্তা রাব্বি, লা ইলাহা ইল্লা আন্তা, খালাকতানি ওয়া আনা আবদুকা, ওয়া আনা আলা আহদিকা ওয়া ওয়াদিকা মাস্তাতাতু, আউজু বিকা মিন শাররি মা সানাতু, আবুউ লাকা বিনি'মাতিকা আলাইয়্যা, ওয়া আবুউ বিজাম্বি ফাগফিরলি, ফাইন্নাহু লা ইয়াগফিরুজ জুনুবা ইল্লা আন্তা।"
        
        রাসূলুল্লাহ (সা.) বলেছেন: যে ব্যক্তি বিশ্বাসের সাথে সকালে এটি পড়বে এবং সন্ধ্যার পূর্বে মারা যাবে, সে জান্নাতি হবে। [সহিহ বুখারি]
      """.trimIndent(),
      bodyAr = """
        سيد الاستغفار: «اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ...» [رواه البخاري].
      """.trimIndent(),
      source = "Hisn al-Muslim, Sahih al-Bukhari",
      readTimeMinutes = 2,
    ),

    // 14. Duas -> Daily life
    IslamicPost(
      id = "post_daily_house_duas",
      categoryId = "cat_duas",
      subcategoryId = "sub_daily_life",
      categoryNameEn = "Daily Duas & Adhkar",
      categoryNameBn = "দৈনন্দিন দোয়া ও জিকির",
      categoryNameAr = "الأذكار اليومية",
      subcategoryNameEn = "Daily Life Masnoon Duas",
      subcategoryNameBn = "দৈনন্দিন জীবনের মাসনূন দোয়া",
      subcategoryNameAr = "أدعية الحياة اليومية",
      titleEn = "Duas for Leaving & Entering Home",
      titleBn = "ঘর থেকে বের হওয়া ও ঘরে প্রবেশের দোয়া",
      titleAr = "دعاء الخروج من المنزل والدخول إليه",
      subtitleEn = "In the name of Allah, I trust in Allah; there is no power except with Allah.",
      subtitleBn = "ঘর থেকে বের হওয়ার সময় আল্লাহর নিরাপত্তা লাভের দোয়া।",
      subtitleAr = "«بسم الله توكلت على الله ولا حول ولا قوة إلا بالله».",
      bodyEn = """
        When leaving home:
        "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ"
        "In the name of Allah, I place my trust in Allah. There is no might nor power except with Allah."
      """.trimIndent(),
      bodyBn = """
        ঘর থেকে বের হওয়ার দোয়া:
        "বিসমিল্লাহি তাওয়াক্কালতু আলাল্লাহ, ওয়ালা হাওলা ওয়ালা কুওয়াতা ইল্লা বিল্লাহ।"
        
        অর্থ:
        "আল্লাহর নামে বের হচ্ছি, আল্লাহর ওপর ভরসা করলাম। আল্লাহর সাহায্য ছাড়া গুনাহ থেকে বাঁচার বা নেক কাজ করার কোনো শক্তি নেই।" [তিরমিজি: ৩৪২৬]
        
        ঘরে প্রবেশের দোয়া:
        "বিসমিল্লাহি ওয়ালাজনা, ওয়া বিসমিল্লাহি খারাজনা, ওয়া আলাল্লাহি রাব্বিনা তাওয়াক্কালনা।"
      """.trimIndent(),
      bodyAr = """
        دعاء الخروج: «بسم الله توكلت على الله، لا حول ولا قوة إلا بالله» [رواه الترمذي].
      """.trimIndent(),
      source = "Jami at-Tirmidhi 3426",
      readTimeMinutes = 2,
    ),

    // 15. Zakat -> Nisab
    IslamicPost(
      id = "post_zakat_nisab",
      categoryId = "cat_zakat",
      subcategoryId = "sub_zakat_calc",
      categoryNameEn = "Zakat & Charity",
      categoryNameBn = "যাকাত ও সদকা",
      categoryNameAr = "الزكاة والصدقات",
      subcategoryNameEn = "Zakat Rules & Nisab",
      subcategoryNameBn = "যাকাতের নিসাব ও হিসাব",
      subcategoryNameAr = "نصاب الزكاة وحسابها",
      titleEn = "Zakat Nisab & Calculation Rules",
      titleBn = "যাকাতের নিসাব কত? স্বর্ণ, রূপা ও নগদ টাকার সহজ হিসাব",
      titleAr = "نصاب زكاة الذهب والفضة والمال النقدي",
      subtitleEn = "How to calculate 2.5% Zakat on gold, silver, savings, and investments.",
      subtitleBn = "কার উপর যাকাত ফরজ এবং কত টাকা থাকলে যাকাত দিতে হবে।",
      subtitleAr = "شروط وجوب الزكاة ومقدار الـ ٢.٥٪ على المدخرات الحولية.",
      bodyEn = """
        Zakat is obligatory on every sane, adult Muslim who possesses wealth equal to or exceeding the Nisab for one lunar year.
        - Gold Nisab: 7.5 Tolas (approx 87.48 grams)
        - Silver Nisab: 52.5 Tolas (approx 612.36 grams)
        Rate: 2.5% of total eligible surplus wealth.
      """.trimIndent(),
      bodyBn = """
        যাকাতের নিসাব:
        কারো কাছে প্রয়োজনীয় খরচের অতিরিক্ত সাড়ে সাত ভরি (৮৭.৪৮ গ্রাম) স্বর্ণ অথবা সাড়ে বায়ান্ন ভরি (৬১২.৩৬ গ্রাম) রূপা অথবা সমমূল্যের নগদ টাকা/ব্যবসায়িক সম্পদ এক বছরকাল থাকলে তার ওপর যাকাত ফরজ।
        
        হার: মোট নিসাব পরিমাণ সম্পদের ২.৫% বা চল্লিশ ভাগের এক ভাগ গরিব-মিসকিনকে দিয়ে দিতে হয়।
      """.trimIndent(),
      bodyAr = """
        تجب الزكاة بنسبة ٢.٥٪ إذا بلغ المال نصاب الفضة أو الذهب وحال عليه الحول الهجري كاملاً.
      """.trimIndent(),
      source = "Sahih al-Bukhari, Fiqh as-Sunnah",
      readTimeMinutes = 3,
    )
  )

  var posts by mutableStateOf<List<IslamicPost>>(defaultPosts)

  val cities = listOf(
    CityLocation(
      nameEn = "Dhaka",
      nameBn = "ঢাকা, বাংলাদেশ",
      nameAr = "دكا، بنغلاديش",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 23.8103,
      longitude = 90.4125,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:52 AM",
      sunriseTime = "06:08 AM",
      dhuhrTime = "11:58 AM",
      asrTimeHanafi = "04:08 PM",
      asrTimeShafi = "03:22 PM",
      maghribTime = "05:48 PM",
      ishaTime = "07:04 PM",
    ),
    CityLocation(
      nameEn = "Chittagong",
      nameBn = "চট্টগ্রাম, বাংলাদেশ",
      nameAr = "شيتاغونغ",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 22.3569,
      longitude = 91.7832,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:48 AM",
      sunriseTime = "06:04 AM",
      dhuhrTime = "11:54 AM",
      asrTimeHanafi = "04:04 PM",
      asrTimeShafi = "03:18 PM",
      maghribTime = "05:44 PM",
      ishaTime = "07:00 PM",
    ),
    CityLocation(
      nameEn = "Sylhet",
      nameBn = "সিলেট, বাংলাদেশ",
      nameAr = "سلهت",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 24.8949,
      longitude = 91.8687,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:45 AM",
      sunriseTime = "06:02 AM",
      dhuhrTime = "11:52 AM",
      asrTimeHanafi = "04:02 PM",
      asrTimeShafi = "03:16 PM",
      maghribTime = "05:42 PM",
      ishaTime = "06:58 PM",
    ),
    CityLocation(
      nameEn = "Rajshahi",
      nameBn = "রাজশাহী, বাংলাদেশ",
      nameAr = "راجشاهي",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 24.3636,
      longitude = 88.6241,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:58 AM",
      sunriseTime = "06:14 AM",
      dhuhrTime = "12:04 PM",
      asrTimeHanafi = "04:14 PM",
      asrTimeShafi = "03:28 PM",
      maghribTime = "05:54 PM",
      ishaTime = "07:10 PM",
    ),
    CityLocation(
      nameEn = "Khulna",
      nameBn = "খুলনা, বাংলাদেশ",
      nameAr = "خولنا",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 22.8456,
      longitude = 89.5403,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:55 AM",
      sunriseTime = "06:11 AM",
      dhuhrTime = "12:01 PM",
      asrTimeHanafi = "04:11 PM",
      asrTimeShafi = "03:25 PM",
      maghribTime = "05:51 PM",
      ishaTime = "07:07 PM",
    ),
    CityLocation(
      nameEn = "Barishal",
      nameBn = "বরিশাল, বাংলাদেশ",
      nameAr = "باريسال",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 22.7010,
      longitude = 90.3535,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:53 AM",
      sunriseTime = "06:09 AM",
      dhuhrTime = "11:59 AM",
      asrTimeHanafi = "04:09 PM",
      asrTimeShafi = "03:23 PM",
      maghribTime = "05:49 PM",
      ishaTime = "07:05 PM",
    ),
    CityLocation(
      nameEn = "Rangpur",
      nameBn = "রংপুর, বাংলাদেশ",
      nameAr = "رانجبور",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 25.7439,
      longitude = 89.2752,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:56 AM",
      sunriseTime = "06:13 AM",
      dhuhrTime = "12:03 PM",
      asrTimeHanafi = "04:12 PM",
      asrTimeShafi = "03:26 PM",
      maghribTime = "05:53 PM",
      ishaTime = "07:09 PM",
    ),
    CityLocation(
      nameEn = "Cox's Bazar",
      nameBn = "কক্সবাজার, বাংলাদেশ",
      nameAr = "كوكس بازار",
      country = "Bangladesh",
      countryBn = "বাংলাদেশ",
      latitude = 21.4272,
      longitude = 92.0058,
      timeZoneId = "Asia/Dhaka",
      fajrTime = "04:46 AM",
      sunriseTime = "06:01 AM",
      dhuhrTime = "11:51 AM",
      asrTimeHanafi = "04:01 PM",
      asrTimeShafi = "03:15 PM",
      maghribTime = "05:41 PM",
      ishaTime = "06:56 PM",
    ),
    // Middle East & International Cities
    CityLocation(
      nameEn = "Makkah",
      nameBn = "মক্কা মুকাররমা, সৌদি আরব",
      nameAr = "مكة المكرمة، السعودية",
      country = "Saudi Arabia",
      countryBn = "সৌদি আরব",
      latitude = 21.4225,
      longitude = 39.8262,
      timeZoneId = "Asia/Riyadh",
      fajrTime = "05:04 AM",
      sunriseTime = "06:22 AM",
      dhuhrTime = "12:15 PM",
      asrTimeHanafi = "04:22 PM",
      asrTimeShafi = "03:38 PM",
      maghribTime = "06:08 PM",
      ishaTime = "07:38 PM",
    ),
    CityLocation(
      nameEn = "Madinah",
      nameBn = "মদিনা মুনাওয়ারা, সৌদি আরব",
      nameAr = "المدينة المنورة، السعودية",
      country = "Saudi Arabia",
      countryBn = "সৌদি আরব",
      latitude = 24.5247,
      longitude = 39.5692,
      timeZoneId = "Asia/Riyadh",
      fajrTime = "05:07 AM",
      sunriseTime = "06:26 AM",
      dhuhrTime = "12:16 PM",
      asrTimeHanafi = "04:20 PM",
      asrTimeShafi = "03:36 PM",
      maghribTime = "06:06 PM",
      ishaTime = "07:36 PM",
    ),
    CityLocation(
      nameEn = "Riyadh",
      nameBn = "রিয়াদ, সৌদি আরব",
      nameAr = "الرياض، السعودية",
      country = "Saudi Arabia",
      countryBn = "সৌদি আরব",
      latitude = 24.7136,
      longitude = 46.6753,
      timeZoneId = "Asia/Riyadh",
      fajrTime = "04:52 AM",
      sunriseTime = "06:10 AM",
      dhuhrTime = "11:58 AM",
      asrTimeHanafi = "04:06 PM",
      asrTimeShafi = "03:20 PM",
      maghribTime = "05:46 PM",
      ishaTime = "07:16 PM",
    ),
    CityLocation(
      nameEn = "Dubai",
      nameBn = "দুবাই, সংযুক্ত আরব আমিরাত",
      nameAr = "دبي، الإمارات",
      country = "UAE",
      countryBn = "সংযুক্ত আরব আমিরাত",
      latitude = 25.2048,
      longitude = 55.2708,
      timeZoneId = "Asia/Dubai",
      fajrTime = "05:15 AM",
      sunriseTime = "06:32 AM",
      dhuhrTime = "12:25 PM",
      asrTimeHanafi = "04:32 PM",
      asrTimeShafi = "03:48 PM",
      maghribTime = "06:18 PM",
      ishaTime = "07:42 PM",
    ),
    CityLocation(
      nameEn = "Doha",
      nameBn = "দোহা, কাতার",
      nameAr = "الدوحة، قطر",
      country = "Qatar",
      countryBn = "কাতার",
      latitude = 25.2854,
      longitude = 51.5310,
      timeZoneId = "Asia/Qatar",
      fajrTime = "04:42 AM",
      sunriseTime = "06:00 AM",
      dhuhrTime = "11:48 AM",
      asrTimeHanafi = "03:55 PM",
      asrTimeShafi = "03:12 PM",
      maghribTime = "05:36 PM",
      ishaTime = "07:02 PM",
    ),
    CityLocation(
      nameEn = "London",
      nameBn = "লন্ডন, যুক্তরাজ্য",
      nameAr = "لندن، المملكة المتحدة",
      country = "United Kingdom",
      countryBn = "যুক্তরাজ্য",
      latitude = 51.5074,
      longitude = -0.1278,
      timeZoneId = "Europe/London",
      fajrTime = "05:40 AM",
      sunriseTime = "07:22 AM",
      dhuhrTime = "12:50 PM",
      asrTimeHanafi = "04:12 PM",
      asrTimeShafi = "03:28 PM",
      maghribTime = "06:18 PM",
      ishaTime = "07:54 PM",
    ),
    CityLocation(
      nameEn = "New York",
      nameBn = "নিউইয়র্ক, যুক্তরাষ্ট্র",
      nameAr = "نيويورك، أمريكا",
      country = "USA",
      countryBn = "যুক্তরাষ্ট্র",
      latitude = 40.7128,
      longitude = -74.0060,
      timeZoneId = "America/New_York",
      fajrTime = "05:52 AM",
      sunriseTime = "07:12 AM",
      dhuhrTime = "12:48 PM",
      asrTimeHanafi = "04:25 PM",
      asrTimeShafi = "03:42 PM",
      maghribTime = "06:24 PM",
      ishaTime = "07:44 PM",
    ),
  )

  private val httpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(10, TimeUnit.SECONDS)
      .build()
  }

  private const val BASE_RTDB_URL = "https://gen-lang-client-0048418625-default-rtdb.asia-southeast1.firebasedatabase.app"

  fun syncFromFirebase() {
    try {
      // 1. Fetch Categories from Realtime Database
      val catsReq = Request.Builder().url("$BASE_RTDB_URL/categories.json").build()
      httpClient.newCall(catsReq).execute().use { response ->
        if (response.isSuccessful) {
          val catBodyStr = response.body?.string()
          if (!catBodyStr.isNullOrBlank() && catBodyStr != "null") {
            val (fetchedCats, extractedSubs) = parseCategoriesAndSubcategories(catBodyStr)
            if (fetchedCats.isNotEmpty()) {
              categories = fetchedCats
            }
            if (extractedSubs.isNotEmpty()) {
              // Merge with existing subcategories
              val currentMap = subcategories.associateBy { it.id }.toMutableMap()
              for (sub in extractedSubs) {
                currentMap[sub.id] = sub
              }
              subcategories = currentMap.values.toList()
            }
          }
        }
      }

      // 2. Fetch Subcategories directly from Realtime Database
      val subcatsReq = Request.Builder().url("$BASE_RTDB_URL/subcategories.json").build()
      httpClient.newCall(subcatsReq).execute().use { response ->
        if (response.isSuccessful) {
          val subBodyStr = response.body?.string()
          if (!subBodyStr.isNullOrBlank() && subBodyStr != "null") {
            val fetchedSubs = parseSubcategoriesJson(subBodyStr)
            if (fetchedSubs.isNotEmpty()) {
              val currentMap = subcategories.associateBy { it.id }.toMutableMap()
              for (sub in fetchedSubs) {
                currentMap[sub.id] = sub
              }
              subcategories = currentMap.values.toList()
            }
          }
        }
      }

      // 3. Fetch Posts from Realtime Database
      val postsReq = Request.Builder().url("$BASE_RTDB_URL/posts.json").build()
      httpClient.newCall(postsReq).execute().use { response ->
        if (response.isSuccessful) {
          val bodyStr = response.body?.string()
          if (!bodyStr.isNullOrBlank() && bodyStr != "null") {
            val fetchedPosts = parsePostsJson(bodyStr)
            if (fetchedPosts.isNotEmpty()) {
              posts = fetchedPosts
            }
          }
        }
      }

      // 4. Update counts
      updateCounts()
    } catch (e: Exception) {
      android.util.Log.w("SampleIslamicData", "Realtime sync notice: ${e.message}")
    }
  }

  private fun updateCounts() {
    val currentPosts = posts
    val currentSubs = subcategories

    categories = categories.map { cat ->
      val countPosts = currentPosts.count { it.categoryId == cat.id }
      val countSubs = currentSubs.count { it.categoryId == cat.id }
      cat.copy(postCount = countPosts, subcategoryCount = countSubs)
    }

    subcategories = currentSubs.map { sub ->
      val count = currentPosts.count { it.subcategoryId == sub.id || (it.categoryId == sub.categoryId && it.subcategoryNameBn == sub.nameBn) }
      val childCount = currentSubs.count { it.parentSubcategoryId == sub.id }
      sub.copy(postCount = count, childCount = childCount)
    }
  }

  private fun parseCategoriesAndSubcategories(jsonStr: String): Pair<List<IslamicCategory>, List<IslamicSubcategory>> {
    val catList = mutableListOf<IslamicCategory>()
    val subList = mutableListOf<IslamicSubcategory>()

    try {
      val trimmed = jsonStr.trim()
      if (trimmed.startsWith("{")) {
        val obj = JSONObject(trimmed)
        val keys = obj.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          val itemObj = obj.optJSONObject(key) ?: continue
          processCategoryJsonItem(key, itemObj, catList, subList)
        }
      } else if (trimmed.startsWith("[")) {
        val arr = JSONArray(trimmed)
        for (i in 0 until arr.length()) {
          val itemObj = arr.optJSONObject(i) ?: continue
          processCategoryJsonItem("cat_$i", itemObj, catList, subList)
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("SampleIslamicData", "parseCategoriesAndSubcategories error: ${e.message}")
    }

    return Pair(
      if (catList.isNotEmpty()) catList.sortedBy { it.nameBn } else defaultCategories,
      subList
    )
  }

  private fun processCategoryJsonItem(
    defaultId: String,
    obj: JSONObject,
    catList: MutableList<IslamicCategory>,
    subList: MutableList<IslamicSubcategory>
  ) {
    if (!obj.optBoolean("isActive", true)) return
    val id = obj.optString("id", defaultId)
    val nameBn = obj.optString("name_bn", obj.optString("nameBn", ""))
    val nameEn = obj.optString("name_en", obj.optString("nameEn", ""))
    val nameAr = obj.optString("name_ar", obj.optString("nameAr", ""))
    val depth = obj.optInt("depth", 0)
    val parentId = obj.optString("parentId", "")
    val icon = obj.optString("icon", "book")

    if (depth > 0 || (parentId.isNotEmpty() && parentId != "root" && parentId != "null")) {
      // It's a subcategory
      subList.add(
        IslamicSubcategory(
          id = id,
          categoryId = if (parentId.isNotEmpty()) parentId else "cat_namaz",
          parentSubcategoryId = if (depth > 1) parentId else null,
          nameEn = nameEn.ifEmpty { nameBn },
          nameBn = nameBn.ifEmpty { nameEn },
          nameAr = nameAr,
          iconName = icon,
          order = obj.optInt("order", 0)
        )
      )
    } else {
      // Root category
      catList.add(
        IslamicCategory(
          id = id,
          nameEn = nameEn.ifEmpty { nameBn },
          nameBn = nameBn.ifEmpty { nameEn },
          nameAr = nameAr,
          depth = 0,
          iconName = icon
        )
      )
    }
  }

  private fun parseSubcategoriesJson(jsonStr: String): List<IslamicSubcategory> {
    val list = mutableListOf<IslamicSubcategory>()
    try {
      val trimmed = jsonStr.trim()
      if (trimmed.startsWith("{")) {
        val obj = JSONObject(trimmed)
        val keys = obj.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          val subObj = obj.optJSONObject(key) ?: continue
          parseSingleSubcategory(key, subObj)?.let { list.add(it) }
        }
      } else if (trimmed.startsWith("[")) {
        val arr = JSONArray(trimmed)
        for (i in 0 until arr.length()) {
          val subObj = arr.optJSONObject(i) ?: continue
          parseSingleSubcategory("sub_$i", subObj)?.let { list.add(it) }
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("SampleIslamicData", "parseSubcategoriesJson error: ${e.message}")
    }
    return list
  }

  private fun parseSingleSubcategory(defaultId: String, obj: JSONObject): IslamicSubcategory? {
    if (!obj.optBoolean("isActive", true)) return null
    val id = obj.optString("id", defaultId)
    val categoryId = obj.optString("categoryId", obj.optString("parentId", "cat_namaz"))
    val parentSubId = obj.optString("parentSubcategoryId", obj.optString("parentSubId", "")).ifEmpty { null }
    val nameBn = obj.optString("name_bn", obj.optString("nameBn", ""))
    val nameEn = obj.optString("name_en", obj.optString("nameEn", ""))
    val nameAr = obj.optString("name_ar", obj.optString("nameAr", ""))
    val icon = obj.optString("icon", "folder")

    return IslamicSubcategory(
      id = id,
      categoryId = categoryId,
      parentSubcategoryId = parentSubId,
      nameEn = nameEn.ifEmpty { nameBn },
      nameBn = nameBn.ifEmpty { nameEn },
      nameAr = nameAr,
      iconName = icon,
      order = obj.optInt("order", 0)
    )
  }

  private fun parsePostsJson(jsonStr: String): List<IslamicPost> {
    val list = mutableListOf<IslamicPost>()
    try {
      val trimmed = jsonStr.trim()
      if (trimmed.startsWith("{")) {
        val obj = JSONObject(trimmed)
        val keys = obj.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          val postObj = obj.optJSONObject(key) ?: continue
          val parsed = parseSinglePost(key, postObj)
          if (parsed != null) list.add(parsed)
        }
      } else if (trimmed.startsWith("[")) {
        val arr = JSONArray(trimmed)
        for (i in 0 until arr.length()) {
          val postObj = arr.optJSONObject(i) ?: continue
          val parsed = parseSinglePost("post_$i", postObj)
          if (parsed != null) list.add(parsed)
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("SampleIslamicData", "parsePostsJson failed: ${e.message}")
    }
    return if (list.isNotEmpty()) list.sortedByDescending { it.isAyatOfTheDay } else defaultPosts
  }

  private fun parseSinglePost(defaultId: String, obj: JSONObject): IslamicPost? {
    val status = obj.optString("status", "published").trim().lowercase()
    if (status != "published" && status != "publish" && status != "public" && status.isNotEmpty()) {
      return null
    }
    val id = obj.optString("id", defaultId)
    val catId = obj.optString("categoryId", "cat_namaz")
    var subId = obj.optString("subcategoryId", obj.optString("subCategory", "")).trim()
    if (subId.isEmpty() || subId == "null") {
      // Find matching subcategory if possible
      val subCatName = obj.optString("subCategory", "")
      val matched = subcategories.firstOrNull { it.categoryId == catId && (it.nameBn == subCatName || it.nameEn == subCatName) }
      subId = matched?.id ?: (subcategories.firstOrNull { it.categoryId == catId }?.id ?: "sub_fajr")
    }

    val titleBn = obj.optString("title_bn", obj.optString("titleBn", ""))
    val titleEn = obj.optString("title_en", obj.optString("titleEn", ""))
    val titleAr = obj.optString("title_ar", obj.optString("titleAr", ""))
    val subBn = obj.optString("subtitle_bn", obj.optString("subtitleBn", ""))
    val subEn = obj.optString("subtitle_en", obj.optString("subtitleEn", ""))
    val subAr = obj.optString("subtitle_ar", "")
    val bodyBn = obj.optString("body_bn", obj.optString("banglaTranslation", ""))
    val bodyEn = obj.optString("body_en", "")
    val bodyAr = obj.optString("mainArabic", "")
    val source = obj.optString("hadithReference", obj.optString("author", "নূর ডেইলি"))
    val catNameBn = obj.optString("mainCategory", "ইসলামিক নির্দেশনা")
    val subCatName = obj.optString("subCategory", "")

    val isAyat = id == "post_ayat_today" || obj.optBoolean("isAyatOfTheDay", false)
    val isHadith = id == "post_hadith_today" || obj.optBoolean("isDailyHadith", false)

    return IslamicPost(
      id = id,
      categoryId = catId,
      subcategoryId = subId,
      categoryNameEn = obj.optString("mainCategory", "Islamic Guidance"),
      categoryNameBn = catNameBn,
      categoryNameAr = "الإسلامية",
      subcategoryNameBn = subCatName,
      subcategoryNameEn = subCatName,
      titleEn = titleEn.ifEmpty { titleBn },
      titleBn = titleBn.ifEmpty { titleEn },
      titleAr = titleAr,
      subtitleEn = subEn.ifEmpty { subBn },
      subtitleBn = subBn.ifEmpty { subEn },
      subtitleAr = subAr,
      bodyEn = bodyEn.ifEmpty { bodyBn },
      bodyBn = bodyBn.ifEmpty { bodyEn },
      bodyAr = bodyAr,
      source = source,
      readTimeMinutes = obj.optInt("readTimeMinutes", 3),
      isAyatOfTheDay = isAyat,
      isDailyHadith = isHadith
    )
  }
}
