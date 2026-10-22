import React, { useEffect, useState } from 'react';
import { onValue, ref, set } from 'firebase/database';
import { AlertCircle, CheckCircle2, Clock3, Save } from 'lucide-react';
import { db } from '../api/firebase';
import { logAudit } from '../api/firestoreService';
import { useAuth } from '../context/AuthContext';

type PrayerMethod =
  | 'Umm al-Qura'
  | 'Muslim World League'
  | 'Egyptian'
  | 'Karachi'
  | 'ISNA';

type Adjustments = {
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

const FIELD_LABELS: Array<{ key: keyof Adjustments; label: string }> = [
  { key: 'fajr', label: 'Fajr' },
  { key: 'sunrise', label: 'Sunrise' },
  { key: 'dhuhr', label: 'Dhuhr' },
  { key: 'asr', label: 'Asr' },
  { key: 'maghrib', label: 'Maghrib' },
  { key: 'isha', label: 'Isha' },
];

export const PrayerConfig: React.FC = () => {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [defaultMethod, setDefaultMethod] = useState<PrayerMethod>('Umm al-Qura');
  const [defaultMadhhab, setDefaultMadhhab] = useState<'Hanafi' | 'Shafi'>('Hanafi');
  const [manualAdjustments, setManualAdjustments] = useState<Adjustments>(DEFAULT_ADJUSTMENTS);
  const [updatedAt, setUpdatedAt] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
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
            setManualAdjustments({
              fajr: Number(data.manualAdjustments.fajr ?? 0),
              sunrise: Number(data.manualAdjustments.sunrise ?? 0),
              dhuhr: Number(data.manualAdjustments.dhuhr ?? 0),
              asr: Number(data.manualAdjustments.asr ?? 0),
              maghrib: Number(data.manualAdjustments.maghrib ?? 0),
              isha: Number(data.manualAdjustments.isha ?? 0),
            });
          }
          setUpdatedAt(typeof data.updatedAt === 'number' ? data.updatedAt : null);
        }
        setLoading(false);
        setErrorMessage(null);
      },
      (error) => {
        console.error('Failed to load prayer settings from /prayer_settings:', error);
        setErrorMessage(
          error.code === 'PERMISSION_DENIED'
            ? 'Firebase permission denied while reading /prayer_settings. Check the existing authenticated admin read rules.'
            : `Could not load prayer settings: ${error.message}`
        );
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  const updateAdjustment = (key: keyof Adjustments, value: string) => {
    const parsed = Number(value);
    setManualAdjustments((previous) => ({
      ...previous,
      [key]: Number.isFinite(parsed) ? Math.max(-30, Math.min(30, parsed)) : 0,
    }));
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
      updatedBy: currentUser?.email || 'admin',
    };

    try {
      await set(ref(db, 'prayer_settings'), payload);
      setUpdatedAt(payload.updatedAt);
      await logAudit(
        'PRAYER_SETTINGS_UPDATED',
        'Prayer calculation method, madhhab, and manual minute adjustments saved',
        currentUser?.email || 'Admin'
      );
      setSuccessMessage('Prayer settings saved successfully.');
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      console.error('Failed to save /prayer_settings:', error);
      setErrorMessage(
        message.toLowerCase().includes('permission')
          ? 'Firebase permission denied while saving /prayer_settings. Check the existing authenticated admin write rules; do not make the database public.'
          : `Could not save prayer settings: ${message}`
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <header className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <div className="flex items-center gap-3">
          <Clock3 className="h-6 w-6 text-emerald-400" />
          <div>
            <h1 className="text-xl font-bold text-white sm:text-2xl">Prayer Time Configuration</h1>
            <p className="mt-1 text-sm text-slate-400">
              Configure the default calculation method, madhhab, and manual minute adjustments.
            </p>
          </div>
        </div>
      </header>

      {errorMessage && (
        <div role="alert" className="flex gap-3 rounded-xl border border-rose-500/30 bg-rose-500/10 p-4 text-sm text-rose-200">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}
      {successMessage && (
        <div role="status" className="flex gap-3 rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-4 text-sm text-emerald-200">
          <CheckCircle2 className="h-5 w-5 shrink-0" />
          <span>{successMessage}</span>
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">
        <section className="space-y-5 rounded-2xl border border-slate-800 bg-slate-900 p-6">
          <div>
            <label htmlFor="prayer-method" className="mb-2 block text-sm font-semibold text-slate-200">
              Calculation method
            </label>
            <select
              id="prayer-method"
              value={defaultMethod}
              onChange={(event) => setDefaultMethod(event.target.value as PrayerMethod)}
              disabled={loading || saving}
              className="w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-sm text-white outline-none focus:border-emerald-500"
            >
              <option>Umm al-Qura</option>
              <option>Muslim World League</option>
              <option>Egyptian</option>
              <option>Karachi</option>
              <option>ISNA</option>
            </select>
            <p className="mt-2 text-xs text-slate-500">
              This value is stored in Firebase. Confirm the mobile app reads this setting before expecting it to change calculated prayer times.
            </p>
          </div>

          <div>
            <span className="mb-2 block text-sm font-semibold text-slate-200">Asr calculation (Madhhab)</span>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              {(['Hanafi', 'Shafi'] as const).map((madhhab) => (
                <label key={madhhab} className="flex cursor-pointer items-center gap-3 rounded-xl border border-slate-700 bg-slate-950 p-4 text-sm text-slate-200">
                  <input
                    type="radio"
                    name="madhhab"
                    value={madhhab}
                    checked={defaultMadhhab === madhhab}
                    onChange={() => setDefaultMadhhab(madhhab)}
                    disabled={loading || saving}
                    className="accent-emerald-500"
                  />
                  {madhhab}
                </label>
              ))}
            </div>
          </div>
        </section>

        <section className="space-y-4 rounded-2xl border border-slate-800 bg-slate-900 p-6">
          <div>
            <h2 className="text-base font-semibold text-white">Manual adjustments (minutes)</h2>
            <p className="mt-1 text-xs text-slate-400">Use values from -30 to +30 minutes. Zero means no adjustment.</p>
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {FIELD_LABELS.map(({ key, label }) => (
              <div key={key}>
                <label htmlFor={`adjust-${key}`} className="mb-1.5 block text-sm text-slate-300">{label}</label>
                <input
                  id={`adjust-${key}`}
                  type="number"
                  min={-30}
                  max={30}
                  step={1}
                  value={manualAdjustments[key]}
                  onChange={(event) => updateAdjustment(key, event.target.value)}
                  disabled={loading || saving}
                  className="w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-sm text-white outline-none focus:border-emerald-500"
                />
              </div>
            ))}
          </div>
        </section>

        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-xs text-slate-500">
            {updatedAt ? `Last saved: ${new Date(updatedAt).toLocaleString()}` : 'No saved configuration found. Default values are shown.'}
          </p>
          <button
            type="submit"
            disabled={loading || saving}
            className="inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Save className="h-4 w-4" />
            {saving ? 'Saving…' : 'Save Configuration'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default PrayerConfig;
