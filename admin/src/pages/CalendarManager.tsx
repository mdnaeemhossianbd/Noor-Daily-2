import React, { useState, useEffect } from 'react';
import { 
  ref, 
  set, 
  remove, 
  onValue 
} from 'firebase/database';
import { db } from '../api/firebase';
import { IslamicCalendarPost, PostStatus } from '../types/schema';
import { useAuth } from '../context/AuthContext';
import { 
  Calendar, PlusCircle, Edit3, Trash2, Search, CheckCircle, Clock, X, AlertCircle, RefreshCw, Star, Sparkles
} from 'lucide-react';
import { defaultCalendarEvents } from '../data/seedData';
import { getInitialCalendarEvents, logAudit } from '../api/firestoreService';

export const CalendarManager: React.FC = () => {
  const { currentUser } = useAuth();
  const [events, setEvents] = useState<IslamicCalendarPost[]>(getInitialCalendarEvents());
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [syncSuccessMsg, setSyncSuccessMsg] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<IslamicCalendarPost | null>(null);

  // Form states
  const [titleBn, setTitleBn] = useState('');
  const [titleEn, setTitleEn] = useState('');
  const [hijriDay, setHijriDay] = useState<number>(1);
  const [hijriMonth, setHijriMonth] = useState('Ramadan');
  const [significanceBn, setSignificanceBn] = useState('');
  const [significanceEn, setSignificanceEn] = useState('');
  const [recommendedDeedsBn, setRecommendedDeedsBn] = useState('');
  const [recommendedDeedsEn, setRecommendedDeedsEn] = useState('');
  const [status, setStatus] = useState<PostStatus>('published');
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const hijriMonthsList = [
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
    "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
    "Ramadan", "Shawwal", "Dhul Qi'dah", "Dhul Hijjah"
  ];

  useEffect(() => {
    const calRef = ref(db, 'calendar_events');
    const unsub = onValue(calRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const list: IslamicCalendarPost[] = Object.values(data);
        list.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
        setEvents(list);
        localStorage.setItem('noor_calendar_events', JSON.stringify(list));
      }
    }, (err) => {
      console.warn('RTDB calendar events listener note:', err);
    });

    return () => unsub();
  }, []);

  const syncDefaultCalendar = async (manual = true) => {
    try {
      if (manual) setSyncing(true);
      const calMap: Record<string, IslamicCalendarPost> = {};
      const eventsToSeed = events.length > 0 ? events : defaultCalendarEvents;
      for (const ev of eventsToSeed) {
        const eventId = ev.id || `cal_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        calMap[eventId] = { ...ev, id: eventId, updatedAt: Date.now() };
      }
      await set(ref(db, 'calendar_events'), calMap);
      setEvents(Object.values(calMap));
      localStorage.setItem('noor_calendar_events', JSON.stringify(Object.values(calMap)));

      if (manual) {
        setSyncSuccessMsg('সকল ইসলামিক ক্যালেন্ডার ইভেন্ট ক্লাউড ফায়ারবেসে সফলভাবে সিঙ্ক হয়েছে!');
        setTimeout(() => setSyncSuccessMsg(null), 4000);
      }
    } catch (err: any) {
      console.error('Failed to sync calendar:', err);
      if (manual) setErrorMessage('Failed to sync: ' + err.message);
    } finally {
      if (manual) setSyncing(false);
    }
  };

  const openCreateModal = () => {
    setEditingEvent(null);
    setTitleBn('');
    setTitleEn('');
    setHijriDay(1);
    setHijriMonth('Ramadan');
    setSignificanceBn('');
    setSignificanceEn('');
    setRecommendedDeedsBn('');
    setRecommendedDeedsEn('');
    setStatus('published');
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const openEditModal = (ev: IslamicCalendarPost) => {
    setEditingEvent(ev);
    setTitleBn(ev.title_bn || '');
    setTitleEn(ev.title_en || '');
    setHijriDay(ev.hijriDay || 1);
    setHijriMonth(ev.hijriMonth || 'Ramadan');
    setSignificanceBn(ev.significance_bn || '');
    setSignificanceEn(ev.significance_en || '');
    setRecommendedDeedsBn(ev.recommendedDeeds_bn || '');
    setRecommendedDeedsEn(ev.recommendedDeeds_en || '');
    setStatus(ev.status);
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!titleBn.trim() && !titleEn.trim()) {
      setErrorMessage('অনুগ্রহ করে ক্যালেন্ডার ইভেন্টের শিরোনাম দিন।');
      return;
    }

    setSubmitting(true);
    try {
      const monthIdx = hijriMonthsList.indexOf(hijriMonth) + 1;
      const eventId = editingEvent?.id || `cal_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
      const payload: IslamicCalendarPost = {
        id: eventId,
        title_bn: titleBn.trim() || titleEn.trim(),
        title_en: titleEn.trim() || titleBn.trim(),
        hijriDay: Number(hijriDay) || 1,
        hijriMonth: hijriMonth,
        hijriMonthIndex: monthIdx > 0 ? monthIdx : 1,
        significance_bn: significanceBn.trim(),
        significance_en: significanceEn.trim(),
        recommendedDeeds_bn: recommendedDeedsBn.trim(),
        recommendedDeeds_en: recommendedDeedsEn.trim(),
        status: status,
        createdAt: editingEvent?.createdAt || Date.now(),
        updatedAt: Date.now()
      };

      // 1. Instantly update local state & localStorage
      setEvents(prev => {
        const exists = prev.some(ev => ev.id === eventId);
        const updated = exists 
          ? prev.map(ev => ev.id === eventId ? payload : ev)
          : [...prev, payload];
        localStorage.setItem('noor_calendar_events', JSON.stringify(updated));
        return updated;
      });

      setIsModalOpen(false);
      setSubmitting(false);
      setSyncSuccessMsg(editingEvent ? 'ক্যালেন্ডার ইভেন্ট আপডেট হয়েছে!' : 'নতুন ক্যালেন্ডার ইভেন্ট তৈরি হয়েছে!');
      setTimeout(() => setSyncSuccessMsg(null), 3000);

      // 2. Sync to Realtime Database in background
      set(ref(db, `calendar_events/${eventId}`), payload).catch(err => {
        console.warn('Calendar sync note:', err);
      });

      logAudit(
        editingEvent ? 'CALENDAR_EVENT_UPDATED' : 'CALENDAR_EVENT_CREATED',
        `Calendar event "${payload.title_bn || payload.title_en}" saved`
      ).catch(() => {});
    } catch (err: any) {
      console.error('Error saving calendar event:', err);
      setErrorMessage('Failed to save event: ' + (err.message || 'Error'));
      setSubmitting(false);
    }
  };

  const handleDelete = async (eventId: string, title: string) => {
    if (!window.confirm(`Are you sure you want to delete event "${title}"?`)) return;

    try {
      await remove(ref(db, `calendar_events/${eventId}`));

      setEvents(prev => {
        const filtered = prev.filter(ev => ev.id !== eventId);
        localStorage.setItem('noor_calendar_events', JSON.stringify(filtered));
        return filtered;
      });

      await logAudit('CALENDAR_EVENT_DELETED', `Calendar event "${title}" deleted`);
    } catch (err: any) {
      console.error('Failed to delete event:', err);
      alert('Delete failed: ' + err.message);
    }
  };

  const filteredEvents = events.filter(ev => 
    ev.title_bn.toLowerCase().includes(searchQuery.toLowerCase()) ||
    ev.title_en.toLowerCase().includes(searchQuery.toLowerCase()) ||
    ev.hijriMonth.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      {syncSuccessMsg && (
        <div className="p-4 bg-emerald-500/20 border border-emerald-500/50 rounded-2xl flex items-center gap-3 text-emerald-200 text-sm shadow-xl">
          <Sparkles className="w-5 h-5 text-emerald-400 shrink-0" />
          <p className="flex-1 font-medium">{syncSuccessMsg}</p>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 bg-slate-900 border border-slate-800 rounded-2xl">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Calendar className="w-5 h-5 text-emerald-400" />
            <h1 className="text-xl sm:text-2xl font-bold text-white">Islamic Calendar & Events Manager</h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-400">
            Publish special Islamic dates, historical significance, and recommended deeds
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={() => syncDefaultCalendar(true)}
            disabled={syncing}
            className="flex items-center gap-2 px-3.5 py-2.5 bg-slate-800 hover:bg-slate-700 text-emerald-300 rounded-xl text-sm font-medium border border-slate-700 transition disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${syncing ? 'animate-spin' : ''}`} />
            <span>{syncing ? 'সিঙ্ক হচ্ছে...' : 'সিঙ্ক করুন (Sync Calendar)'}</span>
          </button>
          <button
            onClick={openCreateModal}
            className="flex items-center gap-2 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition shadow-lg shadow-emerald-900/30"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Add Calendar Event</span>
          </button>
        </div>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search calendar events by title, month, or significance..."
          className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
        />
      </div>

      {/* Event Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredEvents.map((ev) => (
          <div key={ev.id} className="p-5 bg-slate-900 border border-slate-800 rounded-2xl space-y-3">
            <div className="flex items-start justify-between gap-3">
              <div className="space-y-1">
                <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  {ev.hijriDay} {ev.hijriMonth}
                </span>
                <h3 className="text-base font-bold text-white pt-1">
                  {ev.title_bn || ev.title_en}
                </h3>
                {ev.title_en && ev.title_bn && (
                  <p className="text-xs text-slate-400">{ev.title_en}</p>
                )}
              </div>

              <div className="flex items-center gap-1.5 shrink-0">
                <button
                  onClick={() => openEditModal(ev)}
                  className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition"
                >
                  <Edit3 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => handleDelete(ev.id!, ev.title_bn || ev.title_en)}
                  className="p-2 text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 rounded-lg transition"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>

            {ev.significance_bn && (
              <p className="text-xs text-slate-300 leading-relaxed">
                <strong className="text-emerald-400">তাৎপর্য: </strong>
                {ev.significance_bn}
              </p>
            )}

            {ev.recommendedDeeds_bn && (
              <div className="p-2.5 bg-slate-950/80 border border-slate-800 rounded-xl text-xs text-amber-200/90 flex items-start gap-2">
                <Star className="w-3.5 h-3.5 text-amber-400 shrink-0 mt-0.5" />
                <div>
                  <strong>আমল: </strong>
                  <span>{ev.recommendedDeeds_bn}</span>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm overflow-y-auto">
          <div className="w-full max-w-lg my-8 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between">
              <h2 className="text-base font-bold text-white">
                {editingEvent ? 'Edit Calendar Event' : 'Add New Calendar Event'}
              </h2>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-6 space-y-4">
              {errorMessage && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-300 text-xs">
                  {errorMessage}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">ইভেন্টের নাম (বাংলায়) *</label>
                <input
                  type="text"
                  value={titleBn}
                  onChange={(e) => setTitleBn(e.target.value)}
                  placeholder="যেমন: পবিত্র আশুরা (১০ই মহররম)"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Event Name in English</label>
                <input
                  type="text"
                  value={titleEn}
                  onChange={(e) => setTitleEn(e.target.value)}
                  placeholder="e.g. Day of Ashura (10th Muharram)"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Hijri Day (১-৩০)</label>
                  <input
                    type="number"
                    min={1}
                    max={30}
                    value={hijriDay}
                    onChange={(e) => setHijriDay(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Hijri Month</label>
                  <select
                    value={hijriMonth}
                    onChange={(e) => setHijriMonth(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"
                  >
                    {hijriMonthsList.map((m) => (
                      <option key={m} value={m}>{m}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">ঐতিহাসিক তাৎপর্য ও গুরুত্ব</label>
                <textarea
                  value={significanceBn}
                  onChange={(e) => setSignificanceBn(e.target.value)}
                  rows={2}
                  placeholder="এই দিবসের ঐতিহাসিক তাৎপর্য ও ফজিলত..."
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">করণীয় নফল আমল ও দোয়া</label>
                <textarea
                  value={recommendedDeedsBn}
                  onChange={(e) => setRecommendedDeedsBn(e.target.value)}
                  rows={2}
                  placeholder="যেমন: নফল রোজা রাখা, বেশি বেশি সদকা করা..."
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-emerald-600 text-white rounded-xl text-xs font-semibold shadow-lg shadow-emerald-900/30"
                >
                  {submitting ? 'Saving...' : 'Save Event'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
