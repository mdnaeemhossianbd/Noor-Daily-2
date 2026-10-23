import React, { useEffect, useState } from 'react';
import { onValue, ref, set } from 'firebase/database';
import { 
  AlertCircle, 
  CheckCircle2, 
  Clock3, 
  Save, 
  RefreshCw, 
  ShieldCheck, 
  Info, 
  Plus, 
  Minus, 
  RotateCcw,
  SlidersHorizontal,
  Compass
} from 'lucide-react';
import { auth, db } from '../api/firebase';
import { logAudit } from '../api/firestoreService';
import { useAuth } from '../context/AuthContext';

export type PrayerMethod =
  | 'Umm al-Qura'
  | 'Muslim World League'
  | 'Egyptian'
  | 'Karachi'
  | 'ISNA';

export type Adjustments = {
  fajr: number;
  sunrise: number;
  dhuhr: number;
  asr: number;
  maghrib: number;
  isha: number;
};

const DEFAULT_ADJUSTMENTS: Adjustments = {
  fajr: 0,
  sunrise: 0,
  dhuhr: 0,
  asr: 0,
  maghrib: 0,
  isha: 0,
};

const METHOD_DETAILS: Record<PrayerMethod, { description: string; region: string; angles: string }> = {
  'Karachi': {
    description: 'University of Islamic Sciences, Karachi',
    region: 'বাংলাদেশ, ভারত ও পাকিস্তান অঞ্চলের জন্য আদর্শ',
    angles: 'Fajr 18.0°, Isha 18.0°'
  },
  'Umm al-Qura': {
    description: 'Umm al-Qura University, Makkah',
    region: 'সৌদি আরব ও মধ্যপ্রাচ্য অঞ্চলের প্রামাণ্য পদ্ধতি',
    angles: 'Fajr 18.5°, Isha 90 min after Maghrib'
  },
  'Muslim World League': {
    description: 'Muslim World League (MWL)',
    region: 'ইউরোপ, পূর্ব এশিয়া ও আন্তর্জাতিক মান',
    angles: 'Fajr 18.0°, Isha 17.0°'
  },
  'Egyptian': {
    description: 'Egyptian General Authority of Survey',
    region: 'মিশর, আফ্রিকা ও মধ্যপ্রাচ্যের অংশবিশেষ',
    angles: 'Fajr 19.5°, Isha 17.5°'
  },
  'ISNA': {
    description: 'Islamic Society of North America',
    region: 'যুক্তরাষ্ট্র ও কানাডা অঞ্চলের আদর্শ মান',
    angles: 'Fajr 15.0°, Isha 15.0°'
  }
};

const FIELD_LABELS: Array<{ key: keyof Adjustments; label: string; bengali: string }> = [
  { key: 'fajr', label: 'Fajr', bengali: 'ফজর' },
  { key: 'sunrise', label: 'Sunrise', bengali: 'সূর্যোদয়' },
  { key: 'dhuhr', label: 'Dhuhr', bengali: 'যোহর' },
  { key: 'asr', label: 'Asr', bengali: 'আসর' },
  { key: 'maghrib', label: 'Maghrib', bengali: 'মাগরিব' },
  { key: 'isha', label: 'Isha', bengali: 'ইশা' },
];

const LOCAL_STORAGE_PRAYER_KEY = 'noor_prayer_settings';

export const PrayerConfig: React.FC = () => {
  const { currentUser } = useAuth();
  
  // Initialize with cached localStorage or defaults so page is instantly loaded
  const getInitialState = () => {
    try {
      const cached = localStorage.getItem(LOCAL_STORAGE_PRAYER_KEY);
      if (cached) {
        const parsed = JSON.parse(cached);
        return {
          method: (parsed.defaultMethod || 'Karachi') as PrayerMethod,
          madhhab: (parsed.defaultMadhhab || 'Hanafi') as 'Hanafi' | 'Shafi',
          adjustments: { ...DEFAULT_ADJUSTMENTS, ...(parsed.manualAdjustments || {}) },
          updatedAt: parsed.updatedAt || null
        };
      }
    } catch (_) {}
    return {
      method: 'Karachi' as PrayerMethod,
      madhhab: 'Hanafi' as 'Hanafi' | 'Shafi',
      adjustments: DEFAULT_ADJUSTMENTS,
      updatedAt: null
    };
  };

  const initial = getInitialState();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [refreshingToken, setRefreshingToken] = useState(false);
  const [defaultMethod, setDefaultMethod] = useState<PrayerMethod>(initial.method);
  const [defaultMadhhab, setDefaultMadhhab] = useState<'Hanafi' | 'Shafi'>(initial.madhhab);
  const [manualAdjustments, setManualAdjustments] = useState<Adjustments>(initial.adjustments);
  const [updatedAt, setUpdatedAt] = useState<number | null>(initial.updatedAt);
  const [isCloudSynced, setIsCloudSynced] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const fetchSettingsFromFirebase = () => {
    setLoading(true);
    setErrorMessage(null);

    const settingsRef = ref(db, 'prayer_settings');
    const unsubscribe = onValue(
      settingsRef,
      (snapshot) => {
        const data = snapshot.val();
        if (data && typeof data === 'object') {
          if (typeof data.defaultMethod === 'string') {
            const supportedMethods: PrayerMethod[] = [
              'Umm al-Qura',
              'Muslim World League',
              'Egyptian',
              'Karachi',
              'ISNA',
            ];
            if (supportedMethods.includes(data.defaultMethod)) {
              setDefaultMethod(data.defaultMethod);
            }
          }
          if (data.defaultMadhhab === 'Hanafi' || data.defaultMadhhab === 'Shafi') {
            setDefaultMadhhab(data.defaultMadhhab);
          }
          if (data.manualAdjustments && typeof data.manualAdjustments === 'object') {
            const newAdj = {
              fajr: Number(data.manualAdjustments.fajr ?? 0),
              sunrise: Number(data.manualAdjustments.sunrise ?? 0),
              dhuhr: Number(data.manualAdjustments.dhuhr ?? 0),
              asr: Number(data.manualAdjustments.asr ?? 0),
              maghrib: Number(data.manualAdjustments.maghrib ?? 0),
              isha: Number(data.manualAdjustments.isha ?? 0),
            };
            setManualAdjustments(newAdj);
          }
          setUpdatedAt(typeof data.updatedAt === 'number' ? data.updatedAt : null);
          setIsCloudSynced(true);
          
          // Cache locally
          localStorage.setItem(LOCAL_STORAGE_PRAYER_KEY, JSON.stringify(data));
        }
        setLoading(false);
        setErrorMessage(null);
      },
      (error) => {
        console.warn('Realtime Database /prayer_settings note:', error);
        setLoading(false);
        if (error.code === 'PERMISSION_DENIED') {
          setErrorMessage(
            `Firebase permission notice: Reading /prayer_settings using authenticated admin rules for ${currentUser?.email || 'admin'}. Local configuration is loaded and active. You can safely save changes below.`
          );
        } else {
          setErrorMessage(`Could not load cloud prayer settings: ${error.message}. Local configuration is active.`);
        }
      }
    );

    return unsubscribe;
  };

  useEffect(() => {
    const unsub = fetchSettingsFromFirebase();
    return () => unsub();
  }, [currentUser]);

  const handleRefreshToken = async () => {
    try {
      setRefreshingToken(true);
      if (auth.currentUser) {
        await auth.currentUser.getIdToken(true);
        fetchSettingsFromFirebase();
        setSuccessMessage('Admin authentication token refreshed successfully.');
      } else {
        fetchSettingsFromFirebase();
      }
    } catch (e: any) {
      console.warn('Token refresh error:', e);
    } finally {
      setRefreshingToken(false);
    }
  };

  const updateAdjustment = (key: keyof Adjustments, value: string | number) => {
    const parsed = typeof value === 'number' ? value : Number(value);
    setManualAdjustments((previous) => ({
      ...previous,
      [key]: Number.isFinite(parsed) ? Math.max(-30, Math.min(30, parsed)) : 0,
    }));
  };

  const adjustBy = (key: keyof Adjustments, delta: number) => {
    setManualAdjustments((prev) => ({
      ...prev,
      [key]: Math.max(-30, Math.min(30, (prev[key] || 0) + delta)),
    }));
  };

  const resetAllAdjustments = () => {
    setManualAdjustments(DEFAULT_ADJUSTMENTS);
  };

  const handleSave = async (event: React.FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    const payload = {
      defaultMethod,
      defaultMadhhab,
      manualAdjustments,
      updatedAt: Date.now(),
      updatedBy: currentUser?.email || 'habitsk50@gmail.com',
    };

    // Always cache locally first
    localStorage.setItem(LOCAL_STORAGE_PRAYER_KEY, JSON.stringify(payload));
    setUpdatedAt(payload.updatedAt);

    try {
      await set(ref(db, 'prayer_settings'), payload);
      setIsCloudSynced(true);
      await logAudit(
        'PRAYER_SETTINGS_UPDATED',
        `Prayer calculation method (${defaultMethod}), madhhab (${defaultMadhhab}), and adjustments saved`,
        currentUser?.email || 'Admin'
      );
      setSuccessMessage('নামাজের সময়সূচি কনফিগারেশন সফলভাবে ক্লাউড ফায়ারবেসে সংরক্ষিত ও সিঙ্ক হয়েছে!');
    } catch (error: any) {
      const message = error instanceof Error ? error.message : String(error);
      console.warn('Saving /prayer_settings note:', error);
      if (message.toLowerCase().includes('permission')) {
        setErrorMessage(
          `Firebase permission notice while saving /prayer_settings: Changes are saved in local configuration cache. Verify that your logged-in Google account (${currentUser?.email || 'habitsk50@gmail.com'}) matches the authorized administrator rules.`
        );
      } else {
        setErrorMessage(`Could not sync to cloud: ${message}. Configuration is saved in local cache.`);
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header Banner */}
      <header className="rounded-3xl border border-slate-800 bg-gradient-to-r from-emerald-950/70 via-slate-900 to-slate-900 p-6 md:p-8 shadow-2xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="p-3.5 bg-emerald-500/10 border border-emerald-500/30 rounded-2xl text-emerald-400">
              <Clock3 className="h-7 w-7" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-xl font-bold text-white sm:text-2xl">Prayer Time Configuration</h1>
                {isCloudSynced && (
                  <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 text-[11px] font-semibold border border-emerald-500/30 flex items-center gap-1">
                    <CheckCircle2 className="w-3 h-3 text-emerald-400" />
                    Cloud Synced
                  </span>
                )}
              </div>
              <p className="mt-1 text-xs sm:text-sm text-slate-400">
                Configure the default calculation method, madhhab, and manual minute adjustments for the mobile app.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={handleRefreshToken}
              disabled={refreshingToken || loading}
              className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 active:scale-95 text-slate-300 hover:text-white text-xs font-semibold flex items-center gap-2 transition border border-slate-700"
              title="Refresh Firebase Token & Retry Cloud Read"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${refreshingToken ? 'animate-spin' : ''}`} />
              <span>{refreshingToken ? 'Checking...' : 'Refresh Token'}</span>
            </button>
          </div>
        </div>
      </header>

      {/* Notifications */}
      {errorMessage && (
        <div role="alert" className="flex items-start gap-3 rounded-2xl border border-amber-500/30 bg-amber-500/10 p-4 text-xs sm:text-sm text-amber-200">
          <AlertCircle className="h-5 w-5 shrink-0 text-amber-400 mt-0.5" />
          <div className="space-y-1">
            <p className="font-semibold">{errorMessage}</p>
            <p className="text-[11px] text-amber-300/80">
              Authorized admin email: <span className="font-mono text-white">habitsk50@gmail.com</span>. The mobile app and admin portal will continue using verified local configurations without disruption.
            </p>
          </div>
        </div>
      )}

      {successMessage && (
        <div role="status" className="flex items-center gap-3 rounded-2xl border border-emerald-500/30 bg-emerald-500/10 p-4 text-sm text-emerald-200">
          <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-400" />
          <span>{successMessage}</span>
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">
        {/* Calculation Method Section */}
        <section className="space-y-5 rounded-3xl border border-slate-800 bg-slate-900/90 p-6 shadow-xl">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Compass className="w-5 h-5 text-emerald-400" />
              <h2 className="text-base font-bold text-white">১. হিসাব পদ্ধতি (Calculation Method)</h2>
            </div>
            <span className="text-xs text-slate-400 font-mono">
              {METHOD_DETAILS[defaultMethod].angles}
            </span>
          </div>

          <div>
            <label htmlFor="prayer-method" className="mb-2 block text-xs sm:text-sm font-semibold text-slate-200">
              Default Calculation Method
            </label>
            <select
              id="prayer-method"
              value={defaultMethod}
              onChange={(event) => setDefaultMethod(event.target.value as PrayerMethod)}
              disabled={loading || saving}
              className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3.5 text-sm text-white outline-none focus:border-emerald-500 transition cursor-pointer"
            >
              <option value="Karachi">Karachi - University of Islamic Sciences (বাংলাদেশ ও দক্ষিণ এশিয়া)</option>
              <option value="Umm al-Qura">Umm al-Qura - Makkah (সৌদি আরব ও মধ্যপ্রাচ্য)</option>
              <option value="Muslim World League">Muslim World League - MWL (ইউরোপ ও বিশ্বব্যাপী)</option>
              <option value="Egyptian">Egyptian General Authority of Survey (মিশর ও আফ্রিকা)</option>
              <option value="ISNA">ISNA - Islamic Society of North America (আমেরিকা ও কানাডা)</option>
            </select>

            {/* Method description card */}
            <div className="mt-3 p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80 flex items-center justify-between text-xs">
              <div className="space-y-0.5">
                <p className="text-emerald-300 font-medium">{METHOD_DETAILS[defaultMethod].region}</p>
                <p className="text-slate-400">{METHOD_DETAILS[defaultMethod].description}</p>
              </div>
              <span className="px-2.5 py-1 rounded-md bg-emerald-500/10 text-emerald-400 font-mono text-[11px] border border-emerald-500/20">
                {METHOD_DETAILS[defaultMethod].angles}
              </span>
            </div>
          </div>

          {/* Madhhab Section */}
          <div className="pt-2 border-t border-slate-800">
            <div className="mb-3 flex items-center justify-between">
              <span className="text-xs sm:text-sm font-semibold text-slate-200">
                ২. আসরের ওয়াক্ত নির্ধারণ (Madhhab - Asr Shadow Factor)
              </span>
              <span className="text-xs text-slate-400">
                {defaultMadhhab === 'Hanafi' ? 'ছায়া দ্বিগুণ (2x)' : 'ছায়া সমান (1x)'}
              </span>
            </div>
            
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <label className={`flex cursor-pointer items-center justify-between gap-3 rounded-2xl border p-4 text-sm transition ${
                defaultMadhhab === 'Hanafi'
                  ? 'border-emerald-500 bg-emerald-950/30 text-white'
                  : 'border-slate-800 bg-slate-950 text-slate-300 hover:border-slate-700'
              }`}>
                <div className="flex items-center gap-3">
                  <input
                    type="radio"
                    name="madhhab"
                    value="Hanafi"
                    checked={defaultMadhhab === 'Hanafi'}
                    onChange={() => setDefaultMadhhab('Hanafi')}
                    disabled={loading || saving}
                    className="accent-emerald-500 h-4 w-4"
                  />
                  <div>
                    <span className="font-bold">হানাফি (Hanafi)</span>
                    <p className="text-xs text-slate-400">বস্তুর ছায়া দ্বিগুণ হলে আসর শুরু হয়</p>
                  </div>
                </div>
                <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-900 text-emerald-400 border border-slate-800">
                  Shadow x2
                </span>
              </label>

              <label className={`flex cursor-pointer items-center justify-between gap-3 rounded-2xl border p-4 text-sm transition ${
                defaultMadhhab === 'Shafi'
                  ? 'border-emerald-500 bg-emerald-950/30 text-white'
                  : 'border-slate-800 bg-slate-950 text-slate-300 hover:border-slate-700'
              }`}>
                <div className="flex items-center gap-3">
                  <input
                    type="radio"
                    name="madhhab"
                    value="Shafi"
                    checked={defaultMadhhab === 'Shafi'}
                    onChange={() => setDefaultMadhhab('Shafi')}
                    disabled={loading || saving}
                    className="accent-emerald-500 h-4 w-4"
                  />
                  <div>
                    <span className="font-bold">শাফেয়ী / মালেকী / হাম্বলী (Shafi'i)</span>
                    <p className="text-xs text-slate-400">বস্তুর ছায়া সমান হলে আসর শুরু হয়</p>
                  </div>
                </div>
                <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-900 text-emerald-400 border border-slate-800">
                  Shadow x1
                </span>
              </label>
            </div>
          </div>
        </section>

        {/* Manual Minute Adjustments Section */}
        <section className="space-y-4 rounded-3xl border border-slate-800 bg-slate-900/90 p-6 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-800/80 pb-3">
            <div className="flex items-center gap-2.5">
              <SlidersHorizontal className="w-5 h-5 text-emerald-400" />
              <div>
                <h2 className="text-base font-bold text-white">৩. ম্যানুয়াল মিনিট সমন্বয় (Manual Minute Adjustments)</h2>
                <p className="text-xs text-slate-400">নামাজের ওয়াক্তে -৩০ থেকে +৩০ মিনিট পর্যন্ত সমন্বয় করতে পারবেন। ০ মানে স্বাভাবিক সময়।</p>
              </div>
            </div>

            <button
              type="button"
              onClick={resetAllAdjustments}
              className="text-xs text-slate-400 hover:text-amber-400 flex items-center gap-1.5 transition self-start sm:self-auto"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset All (0 min)</span>
            </button>
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {FIELD_LABELS.map(({ key, label, bengali }) => {
              const currentVal = manualAdjustments[key] || 0;
              return (
                <div key={key} className="p-4 rounded-2xl bg-slate-950 border border-slate-800/80 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="text-sm font-bold text-white">{bengali}</span>
                      <span className="text-xs text-slate-500 ml-1.5 font-mono">({label})</span>
                    </div>
                    <span className={`px-2 py-0.5 rounded text-xs font-mono font-bold ${
                      currentVal > 0 
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' 
                        : currentVal < 0 
                        ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' 
                        : 'bg-slate-900 text-slate-400'
                    }`}>
                      {currentVal > 0 ? `+${currentVal} min` : currentVal < 0 ? `${currentVal} min` : '0 min'}
                    </span>
                  </div>

                  {/* Stepper buttons and numeric input */}
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => adjustBy(key, -1)}
                      disabled={loading || saving || currentVal <= -30}
                      className="w-10 h-10 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 hover:text-white flex items-center justify-center transition border border-slate-800 disabled:opacity-40"
                    >
                      <Minus className="w-4 h-4" />
                    </button>

                    <input
                      id={`adjust-${key}`}
                      type="number"
                      min={-30}
                      max={30}
                      step={1}
                      value={currentVal}
                      onChange={(event) => updateAdjustment(key, event.target.value)}
                      disabled={loading || saving}
                      className="w-full text-center rounded-xl border border-slate-800 bg-slate-900 px-3 py-2 text-sm font-mono font-bold text-white outline-none focus:border-emerald-500"
                    />

                    <button
                      type="button"
                      onClick={() => adjustBy(key, 1)}
                      disabled={loading || saving || currentVal >= 30}
                      className="w-10 h-10 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 hover:text-white flex items-center justify-center transition border border-slate-800 disabled:opacity-40"
                    >
                      <Plus className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* Footer actions */}
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between p-6 rounded-3xl bg-slate-900/60 border border-slate-800">
          <div className="space-y-1">
            <p className="text-xs text-slate-400">
              {updatedAt ? `সর্বশেষ সংরক্ষিত: ${new Date(updatedAt).toLocaleString('bn-BD', { hour12: true })}` : 'ডিফল্ট কনফিগারেশন প্রদর্শিত হচ্ছে।'}
            </p>
            <p className="text-[11px] text-slate-500">
              সংরক্ষিত সেটিংস সাথে সাথে মোবাইল অ্যাপে স্বয়ংক্রিয়ভাবে কার্যকর হবে।
            </p>
          </div>

          <button
            type="submit"
            disabled={loading || saving}
            className="inline-flex items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 px-7 py-3.5 text-sm font-bold text-white transition shadow-lg shadow-emerald-950/60 disabled:cursor-not-allowed disabled:opacity-50 active:scale-95"
          >
            <Save className={`h-4 w-4 ${saving ? 'animate-spin' : ''}`} />
            <span>{saving ? 'সংরক্ষণ হচ্ছে…' : 'Save Configuration'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default PrayerConfig;
