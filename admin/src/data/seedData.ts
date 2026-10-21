import { Category, Post, QuizQuestion, IslamicCalendarPost } from '../types/schema';

export const defaultCategories: Category[] = [
  {
    id: "cat_quran",
    name_en: "Holy Quran & Tafsir",
    name_bn: "পবিত্র কুরআন ও তাফসির",
    name_ar: "القرآن الكريم والتفسير",
    depth: 0,
    order: 0,
    isActive: true
  },
  {
    id: "cat_namaz",
    name_en: "Namaz & Prayer Rules",
    name_bn: "নামাজ ও দোয়ার নিয়ম",
    name_ar: "أحكام الصلاة والأدعية",
    depth: 0,
    order: 1,
    isActive: true
  },
  {
    id: "cat_hadith",
    name_en: "Prophetic Hadiths",
    name_bn: "রাসূলের (সা.) হাদিস শরিফ",
    name_ar: "الأحاديث النبوية الشريفة",
    depth: 0,
    order: 2,
    isActive: true
  },
  {
    id: "cat_duas",
    name_en: "Daily Duas & Adhkar",
    name_bn: "দৈনন্দিন দোয়া ও জিকির",
    name_ar: "الأذكار والأدعية اليومية",
    depth: 0,
    order: 3,
    isActive: true
  },
  {
    id: "cat_roza",
    name_en: "Fasting & Ramadan",
    name_bn: "রোজা ও মাহে রমজান",
    name_ar: "الصيام وشهر رمضان",
    depth: 0,
    order: 4,
    isActive: true
  },
  {
    id: "cat_zakat",
    name_en: "Zakat & Charity",
    name_bn: "যাকাত ও সদকা",
    name_ar: "الزكاة والصدقات",
    depth: 0,
    order: 5,
    isActive: true
  },
  {
    id: "cat_calendar",
    name_en: "Islamic Calendar & Events",
    name_bn: "ইসলামিক ক্যালেন্ডার ও দিবস",
    name_ar: "التقويم الهجري والمناسبات",
    depth: 0,
    order: 6,
    isActive: true
  }
];

export const defaultPosts: Post[] = [
  {
    id: "post_ayat_today",
    categoryId: "cat_quran",
    mainCategory: "Holy Quran",
    subCategory: "Surah Al-Baqarah",
    title_en: "Ayat of the Day: Surah Al-Baqarah (2:286)",
    title_bn: "আজকের আয়াত: সূরা আল-বাকারা (২:২৮৬)",
    subtitle_en: "Allah does not burden a soul beyond that it can bear.",
    subtitle_bn: "আল্লাহ কোনো ব্যক্তিকে তার সাধ্যের বাইরে কোনো দায়িত্ব চাপিয়ে দেন না।",
    mainArabic: "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ",
    banglaTranslation: "আল্লাহ কাউকে তার সাধ্যাতীত কোনো কাজের ভার দেন না। সে যা ভালো কাজ করে তার প্রতিফল তারই জন্য এবং সে যা মন্দ কাজ করে তার প্রতিফলও তারই উপর বর্তায়।",
    banglaPronunciation: "লা ইউকাল্লিফুল্লাহু নাফসান ইল্লা উস'আহা, লাহা মা কাসাবাত ওয়া আলাইহা মাকতাসাবাত।",
    author: "আল-কুরআনুল কারীম",
    hadithReference: "সূরা আল-বাকারা: আয়াত ২৮৬ (পবিত্র কুরআন)",
    body_en: "Allah does not burden a soul beyond that it can bear. It will have what it has earned, and it will bear what it has incurred.",
    body_bn: "আল্লাহ কাউকে তার সাধ্যাতীত কোনো কাজের ভার দেন না। সে যা ভালো কাজ করে তার প্রতিফল তারই জন্য এবং সে যা মন্দ কাজ করে তার প্রতিফলও তারই উপর বর্তায়। (সূরা আল-বাকারা: ২৮৬)",
    status: "published",
    createdAt: Date.now() - 86400000 * 5,
    updatedAt: Date.now()
  },
  {
    id: "post_hadith_today",
    categoryId: "cat_hadith",
    mainCategory: "Prophetic Hadiths",
    subCategory: "Purity & Taharah",
    title_en: "Daily Hadith: Purity is Half of Faith",
    title_bn: "দৈনিক হাদিস: পবিত্রতা ঈমানের অর্ধেক",
    subtitle_en: "Cleanliness and internal purity elevate the believer.",
    subtitle_bn: "পবিত্রতা ঈমানের অঙ্গ এবং অন্তরের প্রশান্তির মাধ্যম।",
    mainArabic: "الطهور شطر الإيمان والحمد لله تملأ الميزان",
    banglaTranslation: "পবিত্রতা ঈমানের অর্ধেক, আর 'আলহামদুলিল্লাহ' নেকির পাল্লাকে পূর্ণ করে দেয়।",
    banglaPronunciation: "আত্ব-তুহুরু শাত্ব্রুল ঈমান, ওয়াল হামদুলিল্লাহি তামলাউল মীযান।",
    author: "হযরত আবু মালিক আল-আশ'আরী (রা.)",
    hadithReference: "সহীহ মুসলিম: হাদিস ২২৩",
    body_en: "Abu Malik al-Ash'ari reported: The Messenger of Allah, peace and blessings be upon him, said: Purity is half of faith.",
    body_bn: "হযরত আবু মালিক আল-আশয়ারী (রা.) থেকে বর্ণিত, রাসূলুল্লাহ (সা.) বলেছেন: পবিত্রতা ঈমানের অর্ধেক। (সহীহ মুসলিম: ২২৩)",
    status: "published",
    createdAt: Date.now() - 86400000 * 4,
    updatedAt: Date.now()
  },
  {
    id: "post_tahajjud",
    categoryId: "cat_namaz",
    mainCategory: "Namaz & Prayer Rules",
    subCategory: "Nafl Prayers",
    title_en: "Virtues of Tahajjud Prayer (Qiyam al-Layl)",
    title_bn: "তাহাজ্জুদ নামাজের নিয়ম ও অপার ফজিলত",
    subtitle_en: "The best prayer after the obligatory prayers.",
    subtitle_bn: "ফরজ নামাজের পর সর্বোত্তম নামাজ হলো রাতের তাহাজ্জুদ।",
    mainArabic: "أَفْضَلُ الصَّلَاةِ بَعْدَ الصَّلَاةِ الْمَكْتُوبَةِ الصَّلَاةُ فِي جَوْفِ اللَّيْلِ",
    banglaTranslation: "ফরজ নামাজের পর সবচেয়ে মর্যাদাপূর্ণ নামাজ হলো রাতের শেষ ভাগের তাহাজ্জুদ নামাজ।",
    banglaPronunciation: "আফদ্বালুস সালাতি বা'দাস সালাতিল মাকতূবাতি সালাতু ফী জাউফিল লাইল।",
    author: "হযরত আবু হুরায়রা (রা.)",
    hadithReference: "সহীহ মুসলিম: হাদিস ১১৬৩",
    body_en: "The Messenger of Allah said: The best prayer after the obligatory ones is prayer in the depths of the night.",
    body_bn: "হযরত আবু হুরায়রা (রা.) বলেন, রাসূলুল্লাহ (সা.) বলেছেন: 'ফরজ নামাজের পর আল্লাহর নিকট সর্বাধিক মর্যাদাপূর্ণ নামাজ হলো রাতের তাহাজ্জুদ।' (সহীহ মুসলিম)",
    status: "published",
    createdAt: Date.now() - 86400000 * 3,
    updatedAt: Date.now()
  },
  {
    id: "post_ayat_kursi",
    categoryId: "cat_quran",
    mainCategory: "Holy Quran",
    subCategory: "Greatest Verses",
    title_en: "Ayat al-Kursi: The Greatest Verse of the Quran",
    title_bn: "আয়াতুল কুরসি: কুরআনের সর্বশ্রেষ্ঠ আয়াত ও ফজিলত",
    subtitle_en: "Protection from all harm and elevation of rank.",
    subtitle_bn: "প্রতিটি ফরজ নামাজের পর পাঠে জান্নাত প্রাপ্তির সুসংবাদ।",
    mainArabic: "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
    banglaTranslation: "আল্লাহ, তিনি ছাড়া কোনো সত্য উপাস্য নেই, তিনি চিরঞ্জীব, সর্বসত্তার ধারক। তন্দ্রা বা ঘুম তাকে স্পর্শ করে না।",
    banglaPronunciation: "আল্লাহু লা ইলাহা ইল্লা হুওয়াল হাইয়্যুল ক্বাইয়্যূম, লা তা'খুযুহু সিনাতুঁও ওয়ালা নাউম।",
    author: "হযরত উবাই ইবনে কা'ব (রা.)",
    hadithReference: "সূরা আল-বাকারা: ২৫৫ | সহীহ মুসলিম: ৮১০",
    body_en: "Whoever recites Ayat al-Kursi after every obligatory prayer, nothing prevents him from entering Paradise except death.",
    body_bn: "রাসূলুল্লাহ (সা.) বলেছেন: 'যে ব্যক্তি প্রত্যেক ফরজ নামাজের পর আয়াতুল কুরসি পাঠ করবে, তার জান্নাতে প্রবেশের পথে মৃত্যু ছাড়া আর কোনো বাধা থাকবে না।' (সুনানে নাসাঈ)",
    status: "published",
    createdAt: Date.now() - 86400000 * 2,
    updatedAt: Date.now()
  },
  {
    id: "post_morning_adhkar",
    categoryId: "cat_duas",
    mainCategory: "Daily Duas & Adhkar",
    subCategory: "Morning Remembrance",
    title_en: "Essential Morning and Evening Supplications",
    title_bn: "সকাল-সন্ধ্যার অত্যাবশ্যকীয় মাসনূন জিকির ও দোয়া",
    subtitle_en: "Spiritual armor protecting the believer throughout the day.",
    subtitle_bn: "দিন ও রাতের সকল অনিষ্ট ও বিপদাপদ থেকে রক্ষার ঢাল।",
    mainArabic: "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
    banglaTranslation: "আমরা প্রভাতে উপনীত হয়েছি এবং সমস্ত রাজত্ব আল্লাহর জন্য প্রভাত করেছে। সকল প্রশংসা আল্লাহর, আল্লাহ ছাড়া কোনো ইলাহ নেই, তিনি এক, তাঁর কোনো অংশীদার নেই।",
    banglaPronunciation: "আসবাহনা ওয়া আসবাহাল মুলকু লিল্লাহ, ওয়াল হামদুলিল্লাহ, লা ইলাহা ইল্লাল্লাহু ওয়াহদাহু লা শারীকা লাহ।",
    author: "হযরত আবদুল্লাহ ইবনে মাসউদ (রা.)",
    hadithReference: "সহীহ মুসলিম: হাদিস ২৭২৩",
    body_en: "Reciting morning and evening adhkar protects against anxiety, evil eye, and worldly calamities.",
    body_bn: "সকাল ও সন্ধ্যায় নিয়মিত মাসনূন জিকির পাঠ করলে আল্লাহ তায়ালা বান্দাকে সকল বিপদাপদ ও শয়তানের অনিষ্ট থেকে নিরাপদ রাখেন।",
    status: "published",
    createdAt: Date.now() - 86400000,
    updatedAt: Date.now()
  },
  {
    id: "post_ramadan_prep",
    categoryId: "cat_roza",
    mainCategory: "Fasting & Ramadan",
    subCategory: "Virtues of Sawm",
    title_en: "Preparing for the Blessed Month of Ramadan",
    title_bn: "পবিত্র মাহে রমজানের প্রস্তুতি ও রোজা রাখার ফজিলত",
    subtitle_en: "The month of mercy, forgiveness, and salvation.",
    subtitle_bn: "রহমত, মাগফিরাত ও নাজাতের মহান মাসের প্রস্তুতি।",
    mainArabic: "مَنْ صَامَ رَمَضَانَ إِيمَانًا وَاحْتِسَابًا غُفِرَ لَهُ مَا تَقَدَّمَ مِنْ ذَنْبِهِ",
    banglaTranslation: "যে ব্যক্তি ঈমান ও সওয়াব অর্জনের দৃঢ় প্রত্যয়ে রমজানের রোজা রাখবে, তার পূর্ববর্তী সকল গুনাহ ক্ষমা করে দেওয়া হবে।",
    banglaPronunciation: "মান সামা রামাদ্বানা ঈমানাঁও ওয়াহতিসাবান গুফিরা লাহু মা তাক্বাদ্দামা মিন যামবিহ।",
    author: "হযরত আবু হুরায়রা (রা.)",
    hadithReference: "সহীহ বুখারী: ৩৮, সহীহ মুসলিম: ৭৬০",
    body_en: "Whoever fasts during Ramadan with faith and seeking reward, his past sins will be forgiven.",
    body_bn: "রাসূলুল্লাহ (সা.) বলেছেন: 'যে ব্যক্তি ঈমান ও সওয়াবের আশায় রমজানের রোজা রাখবে, তার পূর্বের সকল গুনাহ ক্ষমা করে দেওয়া হবে।' (সহীহ বুখারী: ৩৮)",
    status: "published",
    createdAt: Date.now(),
    updatedAt: Date.now()
  }
];

export const defaultQuizzes: QuizQuestion[] = [
  {
    id: "quiz_1",
    question_en: "Which Surah in the Holy Quran is known as the 'Heart of the Quran'?",
    question_bn: "পবিত্র কুরআনের কোন সূরাকে 'কুরআনের হৃৎপিণ্ড' বা 'দিল' বলা হয়?",
    options_en: ["Surah Al-Baqarah", "Surah Ya-Sin", "Surah Ar-Rahman", "Surah Al-Mulk"],
    options_bn: ["সূরা আল-বাকারা", "সূরা ইয়াসীন", "সূরা আর-রহমান", "সূরা আল-মুলক"],
    correctOptionIndex: 1, // Option B (সূরা ইয়াসীন)
    explanation_en: "Prophet Muhammad (PBUH) said: 'Indeed, everything has a heart, and the heart of the Quran is Surah Ya-Sin.' (Sunan al-Tirmidhi 2887)",
    explanation_bn: "রাসূলুল্লাহ (সা.) বলেছেন: 'নিশ্চয় প্রত্যেক বস্তুর একটি হৃৎপিণ্ড আছে, আর কুরআনের হৃৎপিণ্ড হলো সূরা ইয়াসীন।' (সুনানে তিরমিযী: ২৮৮৭)",
    hadithReference: "সুনানে তিরমিযী: ২৮৮৭",
    category: "Quran",
    status: "published",
    createdAt: Date.now() - 86400000 * 3,
    updatedAt: Date.now()
  },
  {
    id: "quiz_2",
    question_en: "How many obligatory (Fard) daily prayers are ordained in Islam?",
    question_bn: "ইসলামে প্রতিদিন কয় ওয়াক্ত ফরজ নামাজ নির্ধারিত?",
    options_en: ["3 times", "4 times", "5 times", "7 times"],
    options_bn: ["৩ ওয়াক্ত", "৪ ওয়াক্ত", "৫ ওয়াক্ত", "৭ ওয়াক্ত"],
    correctOptionIndex: 2, // Option C (৫ ওয়াক্ত)
    explanation_en: "During the miraculous journey of Miraj, Allah obligated 5 daily prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) upon the Muslim Ummah with the reward of 50.",
    explanation_bn: "মিরাজের রজনীতে মহান আল্লাহ উম্মতে মুহাম্মাদীর উপর প্রতিদিন ৫ ওয়াক্ত নামাজ (ফজর, যোহর, আসর, মাগরিব ও এশা) ফরজ করেছেন।",
    hadithReference: "সহীহ বুখারী: ৩৪৯",
    category: "Salah",
    status: "published",
    createdAt: Date.now() - 86400000 * 2,
    updatedAt: Date.now()
  },
  {
    id: "quiz_3",
    question_en: "What was the very first word revealed to Prophet Muhammad (PBUH) in Cave Hira?",
    question_bn: "হেরা গুহায় মহানবী (সা.)-এর প্রতি নাজিলকৃত সর্বপ্রথম শব্দটি কী ছিল?",
    options_en: ["Qul (Say)", "Iqra (Read/Recite)", "Al-Hamd (Praise)", "Bismillah"],
    options_bn: ["ক্বুল (বলুন)", "ইক্বরা (পড়ুন)", "আল-হামদ (প্রশংসা)", "বিসমিল্লাহ"],
    correctOptionIndex: 1, // Option B (ইক্বরা)
    explanation_en: "The first revealed word was 'Iqra' (Read! In the Name of your Lord Who created) in Surah Al-Alaq, brought by Angel Jibreel (AS).",
    explanation_bn: "হযরত জিবরাঈল (আ.)-এর মাধ্যমে সূরা আলাকের প্রথম ৫টি আয়াত নাজিল হয় এবং এর প্রথম শব্দ ছিল 'ইক্বরা' (পড়ুন আপনার রবের নামে যিনি সৃষ্টি করেছেন)।",
    hadithReference: "সূরা আল-আলাক: আয়াত ১",
    category: "Seerah",
    status: "published",
    createdAt: Date.now() - 86400000,
    updatedAt: Date.now()
  },
  {
    id: "quiz_4",
    question_en: "In which Islamic month is the obligatory fasting (Sawm) observed?",
    question_bn: "কোন আরবি হিজরি মাসে মুসলমানদের জন্য ফরজ রোজা পালন করা হয়?",
    options_en: ["Muharram", "Rajab", "Sha'ban", "Ramadan"],
    options_bn: ["মুহররম", "রজব", "শা'বান", "রমজান"],
    correctOptionIndex: 3, // Option D (রমজান)
    explanation_en: "Allah says in Surah Al-Baqarah (2:185): 'The month of Ramadan in which was revealed the Quran, guidance for people.'",
    explanation_bn: "সূরা আল-বাকারার ১৮৫ নম্বর আয়াতে বলা হয়েছে: 'রমজান মাসই হলো সেই মাস যাতে মানুষের হেদায়াতস্বরূপ কুরআন নাজিল হয়েছে।'",
    hadithReference: "সূরা আল-বাকারা: ১৮৫",
    category: "Ramadan",
    status: "published",
    createdAt: Date.now(),
    updatedAt: Date.now()
  }
];

export const defaultCalendarEvents: IslamicCalendarPost[] = [
  {
    id: "cal_ashura",
    title_en: "Day of Ashura (10th Muharram)",
    title_bn: "পবিত্র আশুরা (১০ই মহররম)",
    hijriDay: 10,
    hijriMonth: "Muharram",
    hijriMonthIndex: 1,
    significance_en: "The day Prophet Musa (AS) and Bani Israel were saved from Pharaoh. Prophet Muhammad (PBUH) fasted this day and recommended fasting the 9th and 10th.",
    significance_bn: "ঐতিহাসিক মহা দিবস যেদিন মুসা (আ.) ও বনী ইসরাঈলকে ফেরাউনের কবল থেকে রক্ষা করা হয়। রাসূল (সা.) এ দিনে রোজা রেখেছেন এবং ৯ ও ১০ই মহররম রোজা রাখার তাগিদ দিয়েছেন।",
    recommendedDeeds_en: "Fasting on the 9th and 10th of Muharram, making sincere Tawbah, giving charity.",
    recommendedDeeds_bn: "৯ ও ১০ই মহররম নফল রোজা রাখা, তওবা-ইস্তিগফার এবং বেশি বেশি সদকা করা।",
    status: "published"
  },
  {
    id: "cal_ramadan",
    title_en: "First Day of Blessed Ramadan",
    title_bn: "পবিত্র মাহে রমজানের শুরু (১লা রমজান)",
    hijriDay: 1,
    hijriMonth: "Ramadan",
    hijriMonthIndex: 9,
    significance_en: "Commencement of the holy month of fasting, self-purification, and revelation of the Holy Quran.",
    significance_bn: "রহমত, মাগফিরাত ও নাজাতের মাস। এ মাসেই মানবতার মুক্তির দিশারী কুরআনুল কারীম নাজিল হয়।",
    recommendedDeeds_en: "Fasting, Taraweeh prayers, Tilawat of Quran, Zakat and Sadaqah.",
    recommendedDeeds_bn: "সিয়াম সাধনা, তারাবীহ নামাজ, কুরআন তিলাওয়াত এবং বেশি বেশি দান-সদকা।",
    status: "published"
  },
  {
    id: "cal_laylatul_qadr",
    title_en: "Laylatul Qadr (The Night of Decree)",
    title_bn: "পবিত্র শবে কদর (লাইলাতুল কদর - ২৭শে রমজান রাত)",
    hijriDay: 27,
    hijriMonth: "Ramadan",
    hijriMonthIndex: 9,
    significance_en: "A night better than a thousand months. Sins are forgiven and immense reward is bestowed upon worshipping servants.",
    significance_bn: "হাজার মাসের চেয়েও উত্তম এক বরকতময় রাত। এ রাতে ইবাদত করলে হাজার মাসের চেয়ে বেশি সওয়াব লাভ হয়।",
    recommendedDeeds_en: "Qiyam al-Layl, reciting 'Allahumma innaka afuwwun tuhibbul afwa fa'fu anni', Dua and Quran recitation.",
    recommendedDeeds_bn: "রাত্রি জেগে নফল নামাজ, শবে কদরের বিশেষ দোয়া পাঠ, ক্ষমা প্রার্থনা ও জিকির।",
    status: "published"
  },
  {
    id: "cal_eid_fitr",
    title_en: "Eid-ul-Fitr (1st Shawwal)",
    title_bn: "পবিত্র ঈদুল ফিতর (১লা শাওয়াল)",
    hijriDay: 1,
    hijriMonth: "Shawwal",
    hijriMonthIndex: 10,
    significance_en: "The joyful festival gifted by Allah celebrating the completion of Ramadan's fasting.",
    significance_bn: "রমজানের সফল সিয়াম সাধনা শেষে মহান আল্লাহ প্রদত্ত আনন্দের দিন ও মহা মিলনমেলা।",
    recommendedDeeds_en: "Paying Sadaqat-ul-Fitr before Eid Salah, offering Eid prayer, embracing brotherhood.",
    recommendedDeeds_bn: "নামাজের পূর্বে ফিতরা আদায়, তাকবীর পাঠ, ঈদগাহে নামাজ আদায় ও পারস্পরিক শুভেচ্ছা বিনিময়।",
    status: "published"
  },
  {
    id: "cal_eid_adha",
    title_en: "Eid-ul-Adha (10th Dhul Hijjah)",
    title_bn: "পবিত্র ঈদুল আজহা ও কোরবানি (১০ই জিলহজ)",
    hijriDay: 10,
    hijriMonth: "Dhul Hijjah",
    hijriMonthIndex: 12,
    significance_en: "Commemoration of the sublime sacrifice and devotion of Prophet Ibrahim (AS) and Ismail (AS).",
    significance_bn: "হযরত ইব্রাহিম (আ.) ও ইসমাইল (আ.)-এর ত্যাগ ও তৌহিদের মহান স্মারক।",
    recommendedDeeds_en: "Eid prayer, Qurbani (sacrifice) in the name of Allah, sharing meat with the poor.",
    recommendedDeeds_bn: "ঈদের নামাজ, আল্লাহর সন্তুষ্টির উদ্দেশ্যে কোরবানি এবং অসহায় ও আত্মীয়দের মাঝে গোশত বণ্টন।",
    status: "published"
  }
];
