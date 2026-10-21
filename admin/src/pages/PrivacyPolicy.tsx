import React, { useState } from 'react';
import { Compass, ShieldCheck, MapPin, Bell, Database, Lock, Mail, ArrowLeft, Globe } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const PrivacyPolicy: React.FC = () => {
  const navigate = useNavigate();
  const [lang, setLang] = useState<'bn' | 'en'>('bn');

  return (
    <div className="min-h-screen bg-slate-950 text-slate-200 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        {/* Navigation & Actions */}
        <div className="flex items-center justify-between mb-8 pb-4 border-b border-slate-800">
          <button
            onClick={() => navigate('/login')}
            className="flex items-center gap-2 text-sm text-slate-400 hover:text-emerald-400 transition"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>{lang === 'bn' ? 'লগইন পেজে ফিরুন' : 'Back to Login'}</span>
          </button>

          <div className="flex items-center gap-2">
            <Globe className="w-4 h-4 text-emerald-400" />
            <button
              onClick={() => setLang('bn')}
              className={`px-3 py-1 rounded-lg text-xs font-semibold transition ${
                lang === 'bn'
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'bg-slate-900 text-slate-400 hover:text-white'
              }`}
            >
              বাংলা
            </button>
            <button
              onClick={() => setLang('en')}
              className={`px-3 py-1 rounded-lg text-xs font-semibold transition ${
                lang === 'en'
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'bg-slate-900 text-slate-400 hover:text-white'
              }`}
            >
              English
            </button>
          </div>
        </div>

        {/* Card Header */}
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-10 shadow-2xl mb-8">
          <div className="flex items-center gap-4 mb-4">
            <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400 flex-shrink-0">
              <Compass className="w-8 h-8" />
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                {lang === 'bn' ? 'নূর ডেইলি গোপনীয়তা নীতি' : 'Privacy Policy for Noor Daily'}
              </h1>
              <p className="text-xs sm:text-sm text-emerald-400 font-medium mt-1">
                {lang === 'bn'
                  ? 'সর্বশেষ হালনাগাদ: অক্টোবর ২০২৬ • গুগল প্লে পলিসি ও ব্যবহারকারী সুরক্ষা নির্দেশিকা'
                  : 'Last Updated: October 2026 • Google Play Policy & User Safety Guidelines'}
              </p>
            </div>
          </div>

          <p className="text-sm text-slate-300 leading-relaxed mt-4">
            {lang === 'bn'
              ? 'নূর ডেইলি (Noor Daily - নূরুল ইয়ামিয়া) অ্যাপে আপনাকে স্বাগতম। আপনার ব্যক্তিগত গোপনীয়তা রক্ষা করা আমাদের অন্যতম প্রধান দায়িত্ব। এই গোপনীয়তা নীতিতে পরিষ্কারভাবে তুলে ধরা হয়েছে কীভাবে নূর ডেইলি ব্যবহারকারীর তথ্য সংরক্ষণ বা পরিচালনা করে।'
              : 'Welcome to Noor Daily. Safeguarding your privacy and personal information is our highest priority. This Privacy Policy transparently explains how Noor Daily handles user information, device permissions, and security.'}
          </p>
        </div>

        {/* Content Sections */}
        <div className="space-y-6">
          {/* Section 1: Location Policy */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6 sm:p-8">
            <div className="flex items-center gap-3 mb-4 text-emerald-400">
              <MapPin className="w-6 h-6 flex-shrink-0" />
              <h2 className="text-lg font-bold text-white">
                {lang === 'bn' ? '১. জিপিএস লোকেশন ও অবস্থান তথ্য (Location Policy)' : '1. GPS Location & Real-Time Calculation'}
              </h2>
            </div>
            <div className="text-sm text-slate-300 space-y-3 leading-relaxed">
              <p>
                {lang === 'bn'
                  ? '• নামাজের সঠিক সময়সূচি ও দিকনির্দেশনা (কেবলা) নির্ভুলভাবে নির্ধারণ করার জন্য আপনার ডিভাইসের জিপিএস বা আনুমানিক নেটওয়ার্ক লোকেশনের অনুমতি প্রয়োজন হতে পারে।'
                  : '• To calculate astronomically precise solar prayer times (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha) and determine Qibla orientation, Noor Daily utilizes on-device GPS coordinates.'}
              </p>
              <p className="bg-emerald-950/40 border border-emerald-500/20 rounded-xl p-4 text-emerald-200">
                {lang === 'bn'
                  ? 'গুরুত্বপূর্ণ ঘোষণা: আপনার জিপিএস স্থানাঙ্ক বা ভৌগোলিক অবস্থান আমাদের কোনো সার্ভারে কখনো আপলোড, সংরক্ষণ বা তৃতীয় কোনো পক্ষের সাথে বিক্রয় করা হয় না। সকল হিসাব-নিকাশ সম্পূর্ণ অফলাইনে ও অন-ডিভাইসে সম্পন্ন হয়।'
                  : 'Zero-Upload Guarantee: Your geographic coordinates are never uploaded to our servers, sold, tracked, or shared with third parties. All astronomical equations run entirely on-device locally.'}
              </p>
            </div>
          </div>

          {/* Section 2: Notifications */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6 sm:p-8">
            <div className="flex items-center gap-3 mb-4 text-emerald-400">
              <Bell className="w-6 h-6 flex-shrink-0" />
              <h2 className="text-lg font-bold text-white">
                {lang === 'bn' ? '২. আযান ও নোটিফিকেশন অ্যালার্ট (Prayer Alerts)' : '2. Azan & Local Reminder Notifications'}
              </h2>
            </div>
            <p className="text-sm text-slate-300 leading-relaxed">
              {lang === 'bn'
                ? 'ওয়াক্ত শুরু হওয়ার সাথে সাথে ব্যবহারকারীকে অবগত করতে স্থানীয় নোটিফিকেশন ও সুমধুর ২০-সেকেন্ড আযান অ্যালার্ট বাজানো হয়। এই অ্যালার্টগুলো ডিভাইসের লোকাল অ্যালার্ম শিডিউলার দ্বারা চালিত হয় এবং ব্যবহারকারী চাইলে যেকোনো সময় সেটিংস থেকে তা বন্ধ বা পরিবর্তন করতে পারেন।'
                : 'Local notifications and a 20-second Azan audio reminder alert users at prayer times. These run entirely via Android / iOS local scheduling managers and can be silenced or toggled at any time in Settings.'}
            </p>
          </div>

          {/* Section 3: Data Storage */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6 sm:p-8">
            <div className="flex items-center gap-3 mb-4 text-emerald-400">
              <Database className="w-6 h-6 flex-shrink-0" />
              <h2 className="text-lg font-bold text-white">
                {lang === 'bn' ? '৩. লোকাল ডাটা ও বুকমার্ক (Local Bookmarks)' : '3. Local Storage & Bookmarks'}
              </h2>
            </div>
            <p className="text-sm text-slate-300 leading-relaxed">
              {lang === 'bn'
                ? 'আপনার পছন্দের কুরআন আয়াত, হাদিস বা সংরক্ষিত আর্টিকেলগুলো শুধুমাত্র আপনার ডিভাইসের লোকাল স্টোরেজে (SharedPreferences / Room) সংরক্ষিত থাকে। কোনো ব্যক্তিগত অ্যাকাউন্ট তৈরি বা লগইন ছাড়াই সাধারণ ব্যবহারকারীরা অ্যাপের সকল ফিচার সম্পূর্ণ বিনামূল্যে উপভোগ করতে পারেন।'
                : 'Bookmarked Quranic verses, Hadith collections, and articles are stored solely on your local device storage. Regular users can access all Islamic content without needing an account, login, or submitting personal data.'}
            </p>
          </div>

          {/* Section 4: Advertising (Google AdMob) */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6 sm:p-8">
            <div className="flex items-center gap-3 mb-4 text-emerald-400">
              <ShieldCheck className="w-6 h-6 flex-shrink-0" />
              <h2 className="text-lg font-bold text-white">
                {lang === 'bn' ? '৪. বিজ্ঞাপন নীতি (Google AdMob)' : '4. Advertising Policy (Google AdMob)'}
              </h2>
            </div>
            <p className="text-sm text-slate-300 leading-relaxed">
              {lang === 'bn'
                ? 'অ্যাপের রক্ষণাবেক্ষণ ও সার্ভার খরচের জন্য গুগল অ্যাডমব (Google AdMob) ব্যানার ও ইন্টারস্টিশিয়াল বিজ্ঞাপন প্রদর্শন করতে পারে। গুগল অ্যাডমব ব্যবহারকারীর কোনো ব্যক্তিগত সংবেদনশীল তথ্য সংগ্রহ করে না। বিজ্ঞাপন প্রদর্শনে গুগলের নিজস্ব গোপনীয়তা নীতি অনুসৃত হয়।'
                : 'To maintain server hosting and ongoing development, non-intrusive ads via Google AdMob may be displayed. AdMob complies with strict Google Play Developer policies and does not collect personally identifiable private data.'}
            </p>
          </div>

          {/* Section 5: Children's Privacy */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6 sm:p-8">
            <div className="flex items-center gap-3 mb-4 text-emerald-400">
              <Lock className="w-6 h-6 flex-shrink-0" />
              <h2 className="text-lg font-bold text-white">
                {lang === 'bn' ? '৫. শিশুদের গোপনীয়তা (Children’s Privacy)' : '5. Children’s Privacy Protection'}
              </h2>
            </div>
            <p className="text-sm text-slate-300 leading-relaxed">
              {lang === 'bn'
                ? 'নূর ডেইলি ১৩ বছরের কম বয়সী কোনো শিশুর ব্যক্তিগত তথ্য সংগ্রহ করে না। আমাদের অ্যাপের সকল বিষয়বস্তু সার্বজনীন এবং পরিবার-বান্ধব ইসলামিক জ্ঞানভিত্তিক।'
                : 'Noor Daily does not knowingly collect personally identifiable information from children under the age of 13. All content is family-friendly, educational Islamic spiritual guidance.'}
            </p>
          </div>

          {/* Section 6: Contact Information */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6 sm:p-8">
            <div className="flex items-center gap-3 mb-4 text-emerald-400">
              <Mail className="w-6 h-6 flex-shrink-0" />
              <h2 className="text-lg font-bold text-white">
                {lang === 'bn' ? '৬. যোগাযোগ ও সহায়তা (Contact Us)' : '6. Contact & Support'}
              </h2>
            </div>
            <div className="text-sm text-slate-300 leading-relaxed space-y-2">
              <p>
                {lang === 'bn'
                  ? 'গোপনীয়তা নীতি সম্পর্কে আপনার কোনো প্রশ্ন, পরামর্শ বা অভিযোগ থাকলে আমাদের সাথে সরাসরি যোগাযোগ করুন:'
                  : 'If you have any questions or inquiries about this Privacy Policy, please contact our administrative team:'}
              </p>
              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl space-y-1">
                <p className="font-semibold text-white">Noor Daily Developer & Admin Support</p>
                <p className="text-emerald-400">Email: habitsk50@gmail.com</p>
                <p className="text-slate-400 text-xs">Alternative: support@noordaily.com</p>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center mt-10 pt-6 border-t border-slate-800 text-xs text-slate-500">
          <p>© 2026 Noor Daily (نور اليومي). All Rights Reserved.</p>
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicy;
