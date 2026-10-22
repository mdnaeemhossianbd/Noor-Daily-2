export type PostStatus = 'draft' | 'published';

export interface Category {
  id?: string;
  name_ar: string;
  name_bn: string;
  name_en: string;
  parentId: string | null; // null represents a root-level category
  depth: number;           // 0 = root, 1 = sub-category, 2 = sub-sub-category, etc.
  order: number;           // display order
  isActive: boolean;       // whether published to mobile users
  createdAt?: any;
  updatedAt?: any;
}

export interface Subcategory {
  id?: string;
  categoryId: string;
  parentSubcategoryId?: string | null;
  name_bn: string;
  name_en: string;
  name_ar?: string;
  icon?: string;
  order: number;
  isActive: boolean;
  createdAt?: any;
  updatedAt?: any;
}

export interface Post {
  id?: string;
  title_en: string;
  title_bn: string;
  title_ar?: string;
  subtitle_en: string;
  subtitle_bn: string;
  subtitle_ar?: string;
  
  // Custom Islamic Post Fields specified by user
  mainArabic?: string;          // Main arabic line (মূল আরবি লাইন)
  banglaTranslation?: string;   // Bangla onubad (বাংলা অনুবাদ)
  banglaPronunciation?: string; // Arabi tar bangla uccharon (আরবি উচ্চারণ)
  mainCategory?: string;        // post er main category
  subCategory?: string;         // sub category
  author?: string;              // lekhok (লেখক)
  hadithReference?: string;     // hadish refarence (হাদিস / কুরআনিক রেফারেন্স)

  body_en?: string;
  body_bn?: string;
  body_ar?: string;
  categoryId: string;          // references Category.id
  subcategoryId?: string;      // references Subcategory.id
  imageUrl?: string;           // public download URL
  status: PostStatus;
  createdAt?: any;
  updatedAt?: any;
}

export interface QuizQuestion {
  id?: string;
  question_en: string;
  question_bn: string;
  options_en: string[];        // 4 options: A, B, C, D
  options_bn: string[];        // 4 options in Bangla
  correctOptionIndex: number;  // 0, 1, 2, or 3 (akta shothik)
  explanation_en?: string;
  explanation_bn?: string;
  hadithReference?: string;    // Hadith/Quran reference
  category: string;
  status: PostStatus;
  createdAt?: any;
  updatedAt?: any;
}

export interface IslamicCalendarPost {
  id?: string;
  title_en: string;
  title_bn: string;
  hijriDay: number;
  hijriMonth: string;
  hijriMonthIndex: number;     // 1 to 12
  significance_en: string;
  significance_bn: string;
  recommendedDeeds_en?: string;
  recommendedDeeds_bn?: string;
  status: PostStatus;
  createdAt?: any;
  updatedAt?: any;
}

export interface AppConfig {
  id?: string;
  banner_ads_enabled: boolean;
  interstitial_enabled: boolean;
  rewarded_enabled: boolean;
  interstitial_frequency_cap: number;
  interstitial_cooldown_seconds: number;
  privacy_policy_url: string;
  contact_email: string;
  updatedAt?: any;
}

export interface PrayerSettingsConfig {
  id?: string;
  defaultMethod: string;
  defaultMadhhab: string;
  manualAdjustments: {
    fajr: number;
    sunrise: number;
    dhuhr: number;
    asr: number;
    maghrib: number;
    isha: number;
  };
  updatedAt?: any;
}

export interface AuditLog {
  id?: string;
  adminEmail: string;
  adminUid: string;
  action: 'create' | 'update' | 'delete' | 'publish' | 'draft';
  targetCollection: 'categories' | 'posts' | 'quizzes' | 'calendar' | 'app_config' | 'prayer_settings';
  targetDocumentId: string;
  details: string;
  timestamp: any;
}
