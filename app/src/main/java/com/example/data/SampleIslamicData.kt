package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.CityLocation
import com.example.model.IslamicCategory
import com.example.model.IslamicPost
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SampleIslamicData {

  val defaultCategories = listOf(
    IslamicCategory(
      id = "cat_quran",
      nameEn = "Holy Quran & Tafsir",
      nameBn = "পবিত্র কুরআন ও তাফসির",
      nameAr = "القرآن الكريم والتفسير",
      depth = 0,
      iconName = "quran",
      postCount = 4
    ),
    IslamicCategory(
      id = "cat_namaz",
      nameEn = "Namaz & Prayer Rules",
      nameBn = "নামাজ ও দোয়ার নিয়ম",
      nameAr = "أحكام الصلاة والأدعية",
      depth = 0,
      iconName = "prayer",
      postCount = 5
    ),
    IslamicCategory(
      id = "cat_hadith",
      nameEn = "Prophetic Hadiths",
      nameBn = "রাসূলের (সা.) হাদিস শরিফ",
      nameAr = "الأحاديث النبوية الشريفة",
      depth = 0,
      iconName = "hadith",
      postCount = 3
    ),
    IslamicCategory(
      id = "cat_duas",
      nameEn = "Daily Duas & Adhkar",
      nameBn = "দৈনন্দিন দোয়া ও জিকির",
      nameAr = "الأذكار والأدعية اليومية",
      depth = 0,
      iconName = "dua",
      postCount = 3
    ),
    IslamicCategory(
      id = "cat_roza",
      nameEn = "Fasting & Ramadan",
      nameBn = "রোজা ও মাহে রমজান",
      nameAr = "الصيام وشهر رمضان",
      depth = 0,
      iconName = "moon",
      postCount = 2
    ),
    IslamicCategory(
      id = "cat_zakat",
      nameEn = "Zakat & Charity",
      nameBn = "যাকাত ও সদকা",
      nameAr = "الزكاة والصدقات",
      depth = 0,
      iconName = "charity",
      postCount = 2
    ),
  )

  var categories by mutableStateOf<List<IslamicCategory>>(defaultCategories)

  val defaultPosts = listOf(
    IslamicPost(
      id = "post_ayat_today",
      categoryId = "cat_quran",
      categoryNameEn = "Holy Quran & Tafsir",
      categoryNameBn = "পবিত্র কুরআন ও তাফসির",
      categoryNameAr = "القرآن الكريم",
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
        هذه الآية الكريمة قاعدة عظيمة في رفع الحرج واللطف بالعباد، فالله عز وجل رحيم بعباده لا يأمرهم بما يعجزون عنه، ولا يبتليهم بما لا يطيقون. وفيها تعليم للأمة أن تضرع إلى ربها بالعفو والمغفرة.
      """.trimIndent(),
      source = "Surah Al-Baqarah (2:286)",
      readTimeMinutes = 2,
      isAyatOfTheDay = true,
    ),
    IslamicPost(
      id = "post_daily_hadith",
      categoryId = "cat_hadith",
      categoryNameEn = "Prophetic Hadiths",
      categoryNameBn = "রাসূলের (সা.) হাদিস শরিফ",
      categoryNameAr = "الحديث النبوي",
      titleEn = "Daily Hadith: The Best of Deeds",
      titleBn = "দৈনিক হাদিস: সর্বোত্তম ও প্রিয় আমল",
      titleAr = "حديث اليوم: أحب الأعمال إلى الله",
      subtitleEn = "The Prophet (ﷺ) was asked: 'Which deed is the dearest to Allah?'",
      subtitleBn = "রাসূলুল্লাহকে (সা.) জিজ্ঞেস করা হলো: 'আল্লাহর কাছে সবচেয়ে প্রিয় আমল কোনটি?'",
      subtitleAr = "عن عبد الله بن مسعود رضي الله عنه قال: سألت النبي صلى الله عليه وسلم أي العمل أحب إلى الله؟",
      bodyEn = """
        Abdullah ibn Mas'ud (may Allah be pleased with him) reported:
        I asked the Messenger of Allah (ﷺ): "Which deed is the dearest to Allah?"
        He replied: "To perform prayer at its proper earliest time."
        I asked: "What next?"
        He said: "To be good and dutiful to your parents."
        I asked: "What next?"
        He said: "Jihad (striving) in the cause of Allah."
        
        [Sahih al-Bukhari: 527, Sahih Muslim: 85]
        
        Key Lesson:
        Guard your five daily prayers and establish them on time, as this forms the primary anchor of peace in a Muslim's daily life.
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
        
        শিক্ষা:
        নামাজ যথাসময়ে আদায় করা এবং পিতা-মাতার প্রতি দায়িত্ব পালন করা আল্লাহর নৈকট্য লাভের অন্যতম শ্রেষ্ঠ সোপান।
      """.trimIndent(),
      bodyAr = """
        عَنْ عَبْدِ اللَّهِ بْنِ مَسْعُودٍ رَضِيَ اللَّهُ عَنْهُ قَالَ:
        سَأَلْتُ النَّبِيَّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: أَيُّ الْعَمَلِ أَحَبُّ إِلَى اللَّهِ؟
        قَالَ: «الصَّلَاةُ عَلَى وَقْتِهَا»
        قَالَ: ثُمَّ أَيٌّ؟ قَالَ: «ثُمَّ بِرُّ الْوَالِدَيْنِ»
        قَالَ: ثُمَّ أَيٌّ؟ قَالَ: «الْجِهَادُ فِي سَبِيلِ اللَّهِ»
        
        [رواه البخاري ومسلم]
        
        العبرة النبوية:
        الحرص على أداء الصلوات المكتوبة في أول أوقاتها من أعظم علامات الصدق والإيمان والتسليم لله تعالى.
      """.trimIndent(),
      source = "Sahih al-Bukhari 527, Muslim 85",
      readTimeMinutes = 2,
      isDailyHadith = true,
    ),
    IslamicPost(
      id = "post_tahajjud",
      categoryId = "cat_namaz",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      titleEn = "The Hidden Power of Tahajjud Prayer",
      titleBn = "তাহাজ্জুদ নামাজের নিয়ম ও অলৌকিক ফজিলত",
      titleAr = "أسرار وفضل قيام الليل وصلاة التهجد",
      subtitleEn = "How to wake up and pray the night prayer that changes destiny.",
      subtitleBn = "রাতের নির্জনতায় রবের সামনে দাঁড়ানোর ফজিলত ও পদ্ধতি।",
      subtitleAr = "شرف المؤمن قيام الليل، ودعوة في السحر لا ترد بإذن الله.",
      bodyEn = """
        Tahajjud (the night prayer) is a Sunnah Mu'akkadah and one of the highest spiritual stations for a Muslim.
        
        The Prophet (ﷺ) said:
        "The best prayer after the obligatory prayers is the night prayer." [Sahih Muslim]
        
        Time of Tahajjud:
        After praying Isha and sleeping for a portion of the night. The best time is during the last third of the night before Fajr dawn.
        
        How to Pray:
        1. Make intention (Niyyah) in your heart.
        2. Pray in sets of 2 Rak'ahs (minimum 2 Rak'ahs, standard is 8 Rak'ahs).
        3. Conclude with Witr prayer if you have not already prayed Witr after Isha.
        4. Pour out your heart in sincere Du'a in Sujood.
      """.trimIndent(),
      bodyBn = """
        তাহাজ্জুদ নামাজ অত্যন্ত বরকতময় এবং নফল ইবাদতের মধ্যে সর্বোত্তম।
        
        রাসূলুল্লাহ (সা.) বলেছেন:
        "ফরজ নামাজের পর সর্বোত্তম নামাজ হলো রাতের নামাজ (তাহাজ্জুদ)।" [সহিহ মুসলিম]
        
        তাহাজ্জুদের ওয়াক্ত:
        এশার নামাজের পর রাতে কিছুটা সময় ঘুমিয়ে মধ্যরাতের পর থেকে ফজরের আজানের পূর্ব পর্যন্ত তাহাজ্জুদের সময় থাকে। তবে রাতের শেষ তৃতীয়াংশ হলো সর্বোত্তম সময়।
        
        নামাজের নিয়ম:
        ১. অন্তরে তাহাজ্জুদের নিয়ত করবেন।
        ২. দুই রাকাত করে করে নফল নামাজের মতো আদায় করবেন। সর্বনিম্ন ২ রাকাত, সর্বোচ্চ ৮ বা ১২ রাকাত পড়া যায়।
        ৩. এশার পর বিতর না পড়ে থাকলে তাহাজ্জুদের শেষে বিতর আদায় করবেন।
        ৪. সেজদায় এবং নামাজের পর চোখের পানি ফেলে আল্লাহর কাছে দোয়া করবেন।
      """.trimIndent(),
      bodyAr = """
        قيام الليل وصلاة التهجد هي دأب الصالحين وتجارة المؤمنين مع رب العالمين.
        
        قال رسول الله صلى الله عليه وسلم:
        «أفضل الصلاة بعد الفريضة صلاة الليل» [رواه مسلم].
        
        وقتها وكيفيتها:
        يبدأ وقتها من بعد صلاة العشاء إلى طلوع الفجر، وأفضله الثلث الأخير من الليل حين ينزل ربنا تبارك وتعالى نزولاً يليق بجلاله إلى السماء الدنيا فيقول: هل من داعٍ فأستجيب له؟
        تصلى ركعتين ركعتين، ويختم المسلم صلاته بركعة الوتر.
      """.trimIndent(),
      source = "Sahih Muslim, Abu Dawud",
      readTimeMinutes = 3,
    ),
    IslamicPost(
      id = "post_fajr_virtue",
      categoryId = "cat_namaz",
      categoryNameEn = "Namaz & Prayer Rules",
      categoryNameBn = "নামাজ ও দোয়ার নিয়ম",
      categoryNameAr = "أحكام الصلاة",
      titleEn = "The Shield of Dawn: Fajr in Congregation",
      titleBn = "ফজর নামাজের অপরিসীম ফজিলত ও জামাত",
      titleAr = "فضل صلاة الفجر وبركة البكور",
      subtitleEn = "Whoever prays the dawn prayer is under the protection of Allah.",
      subtitleBn = "যে ব্যক্তি ফজরের নামাজ আদায় করল, সে আল্লাহর জিম্মাদারীতে চলে গেল।",
      subtitleAr = "من صلى الصبح فهو في ذمة الله فلا يطلبنكم الله من ذمته بشيء.",
      bodyEn = """
        Fajr is the foundation of a Muslim's day. The Messenger of Allah (ﷺ) said:
        "Whoever prays the dawn prayer in congregation, it is as if he had prayed the whole night." [Sahih Muslim]
        
        Furthermore, he said:
        "Whoever prays the morning prayer is under the protection of Allah."
        
        Practical tips to wake up:
        1. Sleep early with Sunnah Adhkar.
        2. Set two alarms placed away from your bed.
        3. Make genuine intention to meet your Creator at dawn.
      """.trimIndent(),
      bodyBn = """
        দিনের শুরুতেই আল্লাহর হুকুম পালনের মাধ্যমে নিজের দিনটিকে বরকতময় করে তোলার সর্বোত্তম হাতিয়ার হলো ফজরের নামাজ।
        
        রাসূলুল্লাহ (সা.) বলেছেন:
        "যে ব্যক্তি জামাতের সাথে ফজরের নামাজ আদায় করে, সে যেন সারারাত জেগে নামাজ আদায় করল।" [সহিহ মুসলিম]
        
        তিনি আরও বলেছেন:
        "যে ব্যক্তি ফজরের নামাজ পড়ল, সে আল্লাহর নিরাপত্তা ও হেফাজতে চলে গেল।"
        
        ফজরে জাগার সহজ উপায়:
        ১. দ্রুত ঘুমানোর অভ্যাস গড়ে তোলা।
        ২. বিছানা থেকে দূরে অ্যালার্ম রাখা।
        ৩. রাতে ঘুমানোর পূর্বে সুন্নতি দোয়া পাঠ করা।
      """.trimIndent(),
      bodyAr = """
        صلاة الفجر هي ميزان الإيمان، وبداية يوم المؤمن بنور وبركة.
        
        قال صلى الله عليه وسلم:
        «من صلى العشاء في جماعة فكأنما قام نصف الليل، ومن صلى الصبح في جماعة فكأنما صلى الليل كله» [رواه مسلم].
        
        وقال عليه الصلاة والسلام:
        «من صلى الصبح فهو في ذمة الله».
      """.trimIndent(),
      source = "Sahih Muslim 656",
      readTimeMinutes = 3,
    ),
    IslamicPost(
      id = "post_morning_adhkar",
      categoryId = "cat_duas",
      categoryNameEn = "Daily Duas & Adhkar",
      categoryNameBn = "দৈনন্দিন দোয়া ও জিকির",
      categoryNameAr = "الأذكار اليومية",
      titleEn = "Morning Adhkar: Fortress of the Believer",
      titleBn = "সকাল-সন্ধ্যার গুরুত্বপূর্ণ মাসনূন জিকির",
      titleAr = "أذكار الصباح والمساء حصن المسلم",
      subtitleEn = "Essential prophetic supplications for daily peace and protection.",
      subtitleBn = "প্রতিদিন সকালে ও সন্ধ্যায় পড়ার বিশেষ দোয়া ও আমল।",
      subtitleAr = "أدعية مأثورة تحفظك من كل سوء بفضل الله ورحمته.",
      bodyEn = """
        Sayyid al-Istighfar (The Master Supplication for Forgiveness):
        
        "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ"
        
        Whoever recites this with firm faith in the morning and dies before the evening will be among the people of Paradise. [Sahih al-Bukhari]
      """.trimIndent(),
      bodyBn = """
        সাইয়্যেদুল ইস্তিগফার (ক্ষমা প্রার্থনার শ্রেষ্ঠ দোয়া):
        
        "আল্লাহুম্মা আন্তা রাব্বি, লা ইলাহা ইল্লা আন্তা, খালাকতানি ওয়া আনা আবদুকা, ওয়া আনা আলা আহদিকা ওয়া ওয়াদিকা মাস্তাতাতু, আউজু বিকা মিন শাররি মা সানাতু, আবুউ লাকা বিনি'মাতিকা আলাইয়্যা, ওয়া আবুউ বিজাম্বি ফাগফিরলি, ফাইন্নাহু লা ইয়াগফিরুজ জুনুবা ইল্লা আন্তা।"
        
        অর্থ:
        "হে আল্লাহ! আপনিই আমার প্রতিপালক। আপনি ছাড়া কোনো সত্য ইলাহ নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা..."
        
        রাসূলুল্লাহ (সা.) বলেছেন: যে ব্যক্তি বিশ্বাসের সাথে সকালে এটি পড়বে এবং সন্ধ্যার পূর্বে মারা যাবে, সে জান্নাতি হবে। [সহিহ বুখারি]
      """.trimIndent(),
      bodyAr = """
        سيد الاستغفار:
        
        «اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ»
        
        من قالها موقناً بها حين يمسي فمات من ليلته دخل الجنة، ومن قالها موقناً بها حين يصبح فمات من يومه دخل الجنة. [رواه البخاري]
      """.trimIndent(),
      source = "Hisn al-Muslim, Sahih al-Bukhari",
      readTimeMinutes = 2,
    ),
  )

  var posts by mutableStateOf<List<IslamicPost>>(defaultPosts)

  val cities = listOf(
    CityLocation(
      nameEn = "Dhaka",
      nameBn = "ঢাকা, বাংলাদেশ",
      nameAr = "دكا، بنغلاديش",
      country = "Bangladesh",
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
      fajrTime = "04:48 AM",
      sunriseTime = "06:04 AM",
      dhuhrTime = "11:54 AM",
      asrTimeHanafi = "04:04 PM",
      asrTimeShafi = "03:18 PM",
      maghribTime = "05:44 PM",
      ishaTime = "07:00 PM",
    ),
    CityLocation(
      nameEn = "Makkah",
      nameBn = "মক্কা মুকাররমা",
      nameAr = "مكة المكرمة",
      country = "Saudi Arabia",
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
      nameBn = "মদিনা মুনাওয়ারা",
      nameAr = "المدينة المنورة",
      country = "Saudi Arabia",
      fajrTime = "05:07 AM",
      sunriseTime = "06:26 AM",
      dhuhrTime = "12:16 PM",
      asrTimeHanafi = "04:20 PM",
      asrTimeShafi = "03:36 PM",
      maghribTime = "06:06 PM",
      ishaTime = "07:36 PM",
    ),
    CityLocation(
      nameEn = "Cairo",
      nameBn = "কায়রো, মিশর",
      nameAr = "القاهرة، مصر",
      country = "Egypt",
      fajrTime = "04:42 AM",
      sunriseTime = "06:08 AM",
      dhuhrTime = "11:50 AM",
      asrTimeHanafi = "03:52 PM",
      asrTimeShafi = "03:08 PM",
      maghribTime = "05:32 PM",
      ishaTime = "06:50 PM",
    ),
    CityLocation(
      nameEn = "London",
      nameBn = "লন্ডন, যুক্তরাজ্য",
      nameAr = "لندن، المملكة المتحدة",
      country = "United Kingdom",
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
      // 1. Fetch Posts from Realtime Database
      val postsReq = Request.Builder()
        .url("$BASE_RTDB_URL/posts.json")
        .build()
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

      // 2. Fetch Categories from Realtime Database
      val catsReq = Request.Builder()
        .url("$BASE_RTDB_URL/categories.json")
        .build()
      httpClient.newCall(catsReq).execute().use { response ->
        if (response.isSuccessful) {
          val catBodyStr = response.body?.string()
          if (!catBodyStr.isNullOrBlank() && catBodyStr != "null") {
            val fetchedCats = parseCategoriesJson(catBodyStr)
            if (fetchedCats.isNotEmpty()) {
              categories = fetchedCats
            }
          }
        }
      }
    } catch (e: Exception) {
      android.util.Log.w("SampleIslamicData", "Realtime sync notice: ${e.message}")
    }
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
    val catId = obj.optString("categoryId", "cat_quran")
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

    val isAyat = id == "post_ayat_today" || obj.optBoolean("isAyatOfTheDay", false)
    val isHadith = id == "post_hadith_today" || obj.optBoolean("isDailyHadith", false)

    return IslamicPost(
      id = id,
      categoryId = catId,
      categoryNameEn = obj.optString("mainCategory", "Islamic Guidance"),
      categoryNameBn = catNameBn,
      categoryNameAr = "الإسلامية",
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

  private fun parseCategoriesJson(jsonStr: String): List<IslamicCategory> {
    val list = mutableListOf<IslamicCategory>()
    try {
      val trimmed = jsonStr.trim()
      if (trimmed.startsWith("{")) {
        val obj = JSONObject(trimmed)
        val keys = obj.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          val catObj = obj.optJSONObject(key) ?: continue
          val parsed = parseSingleCategory(key, catObj)
          if (parsed != null) list.add(parsed)
        }
      } else if (trimmed.startsWith("[")) {
        val arr = JSONArray(trimmed)
        for (i in 0 until arr.length()) {
          val catObj = arr.optJSONObject(i) ?: continue
          val parsed = parseSingleCategory("cat_$i", catObj)
          if (parsed != null) list.add(parsed)
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("SampleIslamicData", "parseCategoriesJson failed: ${e.message}")
    }
    return if (list.isNotEmpty()) list.sortedBy { it.depth } else defaultCategories
  }

  private fun parseSingleCategory(defaultId: String, obj: JSONObject): IslamicCategory? {
    if (!obj.optBoolean("isActive", true)) return null
    val id = obj.optString("id", defaultId)
    val nameBn = obj.optString("name_bn", obj.optString("nameBn", ""))
    val nameEn = obj.optString("name_en", obj.optString("nameEn", ""))
    val nameAr = obj.optString("name_ar", obj.optString("nameAr", ""))
    val depth = obj.optInt("depth", 0)
    val icon = obj.optString("icon", "book")

    return IslamicCategory(
      id = id,
      nameEn = nameEn.ifEmpty { nameBn },
      nameBn = nameBn.ifEmpty { nameEn },
      nameAr = nameAr,
      depth = depth,
      iconName = icon,
      postCount = posts.count { it.categoryId == id }
    )
  }
}
