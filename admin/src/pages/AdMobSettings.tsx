import React, { useState, useEffect } from 'react';
import { ref, onValue, set } from 'firebase/database';
import { db } from '../api/firebase';
import { useAuth } from '../context/AuthContext';
import { Sliders, ShieldCheck, AlertCircle, Save, CheckCircle2, Info } from 'lucide-react';
import { logAudit } from '../api/firestoreService';

export const AdMobSettings: React.FC = () => {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Monetization & Remote Config state
  const [bannerEnabled, setBannerEnabled] = useState(false);
  const [interstitialEnabled, setInterstitialEnabled] = useState(false);
  const [rewardedEnabled, setRewardedEnabled] = useState(false);
  const [frequencyCap, setFrequencyCap] = useState(3);
  const [cooldownSeconds, setCooldownSeconds] = useState(180);
  const [privacyPolicyUrl, setPrivacyPolicyUrl] = useState('https://noordaily.com/privacy-policy');

  useEffect(() => {
    const configRef = ref(db, 'app_config/monetization');
    const unsub = onValue(configRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        setBannerEnabled(data.banner_ads_enabled ?? false);
        setInterstitialEnabled(data.interstitial_enabled ?? false);
        setRewardedEnabled(data.rewarded_enabled ?? false);
        setFrequencyCap(data.interstitial_frequency_cap ?? 3);
        setCooldownSeconds(data.interstitial_cooldown_seconds ?? 180);
        setPrivacyPolicyUrl(data.privacy_policy_url ?? 'https://noordaily.com/privacy-policy');
      }
    }, (err) => {
      console.warn('RTDB monetization snapshot listener note:', err);
    });

    return () => unsub();
  }, []);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSavedSuccess(false);
    setSaving(true);

    try {
      const payload = {
        banner_ads_enabled: bannerEnabled,
        interstitial_enabled: interstitialEnabled,
        rewarded_enabled: rewardedEnabled,
        interstitial_frequency_cap: Number(frequencyCap),
        interstitial_cooldown_seconds: Number(cooldownSeconds),
        privacy_policy_url: privacyPolicyUrl.trim(),
        updatedAt: Date.now(),
        updatedBy: currentUser?.email || 'admin',
      };

      await set(ref(db, 'app_config/monetization'), payload);
      await logAudit('SETTINGS_UPDATED', 'Monetization & Remote Config saved');

      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 4000);
    } catch (err: any) {
      console.error('Error saving settings:', err);
      setErrorMessage('Failed to save settings: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl">
        <div className="flex items-center gap-2 mb-1">
          <Sliders className="w-5 h-5 text-emerald-400" />
          <h1 className="text-xl sm:text-2xl font-bold text-white">App Configuration & AdMob Controls</h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-400">
          Remotely enable or disable ads and tune display frequencies in the live mobile app without releasing updates
        </p>
      </div>

      {savedSuccess && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl flex items-center gap-3 text-emerald-300 text-sm">
          <CheckCircle2 className="w-5 h-5 shrink-0" />
          <span>Settings saved to Firebase Realtime Database.</span>
        </div>
      )}

      {errorMessage && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/20 rounded-2xl flex items-center gap-3 text-rose-300 text-sm">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">
        <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-5">
          <h2 className="text-base font-semibold text-white">Ad Placement Toggles</h2>

          <div className="space-y-4">
            <label className="flex items-center justify-between p-3.5 bg-slate-950/60 border border-slate-800/80 rounded-xl cursor-pointer">
              <div>
                <span className="text-sm font-medium text-white block">Banner Ads</span>
                <span className="text-xs text-slate-400">Shown at bottom of Islamic feed screens</span>
              </div>
              <input
                type="checkbox"
                checked={bannerEnabled}
                onChange={(e) => setBannerEnabled(e.target.checked)}
                className="w-5 h-5 rounded text-emerald-500 focus:ring-emerald-500"
              />
            </label>

            <label className="flex items-center justify-between p-3.5 bg-slate-950/60 border border-slate-800/80 rounded-xl cursor-pointer">
              <div>
                <span className="text-sm font-medium text-white block">Interstitial Ads</span>
                <span className="text-xs text-slate-400">Full-screen ads during transition between categories or reading sessions</span>
              </div>
              <input
                type="checkbox"
                checked={interstitialEnabled}
                onChange={(e) => setInterstitialEnabled(e.target.checked)}
                className="w-5 h-5 rounded text-emerald-500 focus:ring-emerald-500"
              />
            </label>

            <label className="flex items-center justify-between p-3.5 bg-slate-950/60 border border-slate-800/80 rounded-xl cursor-pointer">
              <div>
                <span className="text-sm font-medium text-white block">Rewarded Video Ads</span>
                <span className="text-xs text-slate-400">Optional user-initiated ads for unlocking premium audio recitations</span>
              </div>
              <input
                type="checkbox"
                checked={rewardedEnabled}
                onChange={(e) => setRewardedEnabled(e.target.checked)}
                className="w-5 h-5 rounded text-emerald-500 focus:ring-emerald-500"
              />
            </label>
          </div>
        </div>

        <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
          <h2 className="text-base font-semibold text-white">Frequency Caps & Policy</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Interstitial Frequency Cap</label>
              <input
                type="number"
                min={1}
                max={10}
                value={frequencyCap}
                onChange={(e) => setFrequencyCap(Number(e.target.value))}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Cooldown (seconds)</label>
              <input
                type="number"
                min={30}
                max={600}
                value={cooldownSeconds}
                onChange={(e) => setCooldownSeconds(Number(e.target.value))}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300">Privacy Policy URL (গোপনীয়তা নীতি লিঙ্ক)</label>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setPrivacyPolicyUrl(`${window.location.origin}/privacy-policy`)}
                  className="text-[11px] text-emerald-400 hover:text-emerald-300 underline"
                >
                  Use Current Origin
                </button>
                <button
                  type="button"
                  onClick={() => window.open(privacyPolicyUrl || '/privacy-policy', '_blank')}
                  className="text-[11px] text-cyan-400 hover:text-cyan-300 underline"
                >
                  Test Link ↗
                </button>
              </div>
            </div>
            <input
              type="url"
              value={privacyPolicyUrl}
              onChange={(e) => setPrivacyPolicyUrl(e.target.value)}
              placeholder="https://your-project.vercel.app/privacy-policy"
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
            />
            <p className="text-[11px] text-slate-400 mt-1.5">
              Vercel এ আপনার প্রজেক্টের লাইভ লিঙ্ক দিন (যেমন: <code className="text-emerald-400">https://your-project.vercel.app/privacy-policy</code> বা <code className="text-emerald-400">/privacy-policy.html</code>)
            </p>
          </div>
        </div>

        <div className="flex justify-end">
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 px-6 py-3 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition shadow-lg shadow-emerald-900/30 disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            <span>{saving ? 'Publishing...' : 'Save & Publish Remote Config'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
