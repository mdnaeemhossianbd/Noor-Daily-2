import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ref, onValue, query, limitToLast } from 'firebase/database';
import { db } from '../api/firebase';
import { 
  FileText, FolderTree, CheckCircle, Clock, PlusCircle, ExternalLink, ShieldAlert, TrendingUp, RefreshCw, Sparkles, HelpCircle, Calendar
} from 'lucide-react';
import { AuditLog, Post, Category } from '../types/schema';
import { 
  getInitialPosts, 
  getInitialCategories, 
  syncAllAppContentToFirestore 
} from '../api/firestoreService';
import { IslamicAnalyticsWidget } from '../components/IslamicAnalyticsWidget';

export const Dashboard: React.FC = () => {
  const initialPosts = getInitialPosts();
  const initialCategories = getInitialCategories();

  const [postsList, setPostsList] = useState<Post[]>(initialPosts);
  const [categoriesList, setCategoriesList] = useState<Category[]>(initialCategories);

  const [stats, setStats] = useState({ 
    totalPosts: initialPosts.length, 
    publishedPosts: initialPosts.filter(p => p.status === 'published').length, 
    draftPosts: initialPosts.filter(p => p.status === 'draft').length, 
    totalCategories: initialCategories.length 
  });
  const [recentLogs, setRecentLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [syncToast, setSyncToast] = useState<string | null>(null);

  useEffect(() => {
    // 1. Listen to posts in Realtime Database
    const postsRef = ref(db, 'posts');
    const unsubPosts = onValue(postsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        let total = 0, published = 0, draft = 0;
        const fetchedPosts: Post[] = Object.entries(data).map(([id, val]: [string, any]) => ({
          id,
          ...val
        }));
        total = fetchedPosts.length;
        fetchedPosts.forEach(post => {
          if (post.status === 'published') published++;
          else draft++;
        });
        setStats(prev => ({ ...prev, totalPosts: total, publishedPosts: published, draftPosts: draft }));
        setPostsList(fetchedPosts);
      }
    }, (err) => {
      console.warn('RTDB posts listener note:', err);
    });

    // 2. Listen to categories in Realtime Database
    const catsRef = ref(db, 'categories');
    const unsubCats = onValue(catsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const fetchedCats: Category[] = Object.entries(data).map(([id, val]: [string, any]) => ({
          id,
          ...val
        }));
        setStats(prev => ({ ...prev, totalCategories: Object.keys(data).length }));
        setCategoriesList(fetchedCats);
      }
    }, (err) => {
      console.warn('RTDB categories listener note:', err);
    });

    // 3. Listen to audit logs
    const logsRef = query(ref(db, 'audit_logs'), limitToLast(5));
    const unsubLogs = onValue(logsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const logsList: AuditLog[] = Object.entries(data).map(([id, val]: [string, any]) => ({
          id,
          ...val
        }));
        logsList.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0));
        setRecentLogs(logsList);
      }
    }, (err) => {
      console.warn('RTDB logs listener note:', err);
    });

    return () => { unsubPosts(); unsubCats(); unsubLogs(); };
  }, []);

  const handleSyncAll = async () => {
    try {
      setSyncing(true);
      const res = await syncAllAppContentToFirestore();
      setSyncToast(`সাফল্যের সাথে ${res.postsCount} টি পোস্ট, ${res.categoriesCount} টি ক্যাটাগরি, ${res.quizzesCount} টি কুইজ এবং ${res.calendarCount} টি ক্যালেন্ডার ইভেন্ট ক্লাউড ফায়ারবেসে সিঙ্ক হয়েছে!`);
      setTimeout(() => setSyncToast(null), 5000);
    } catch (err: any) {
      console.error('Failed to sync content:', err);
      setSyncToast('সিঙ্ক করার সময় সমস্যা হয়েছে: ' + (err.message || 'Error'));
    } finally {
      setSyncing(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {syncToast && (
        <div className="p-4 bg-emerald-500/20 border border-emerald-500/50 rounded-2xl flex items-center gap-3 text-emerald-200 text-sm shadow-xl">
          <Sparkles className="w-5 h-5 text-amber-300 flex-shrink-0" />
          <span>{syncToast}</span>
        </div>
      )}

      {/* Hero Welcome & Cloud Sync Master Action */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-emerald-900 via-teal-900 to-slate-900 border border-emerald-500/30 p-8 shadow-2xl">
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/20 text-emerald-300 text-xs font-semibold border border-emerald-400/30">
              <Sparkles className="w-3.5 h-3.5 text-amber-300" />
              <span>Noor Daily Master Cloud Control</span>
            </div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">
              নূর ডেইলি অ্যাডমিন ড্যাশবোর্ড
            </h1>
            <p className="text-emerald-200/80 text-sm max-w-2xl">
              অ্যাপের সকল ইসলামিক কনটেন্ট, কুরআন ও হাদিস পোস্ট, ক্যাটাগরি এবং নামাজ সংক্রান্ত তথ্য সরাসরি ফায়ারবেস রিয়েলটাইম ডাটাবেসে নিয়ন্ত্রণ করুন।
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              onClick={handleSyncAll}
              disabled={syncing}
              className="flex items-center gap-2 px-5 py-3 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 active:scale-95 text-slate-950 font-bold rounded-xl text-xs sm:text-sm transition shadow-lg shadow-amber-900/40 disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${syncing ? 'animate-spin' : ''}`} />
              <span>{syncing ? 'সিঙ্ক হচ্ছে...' : 'Sync All App Content to Cloud'}</span>
            </button>

            <Link
              to="/posts"
              className="flex items-center gap-2 px-5 py-3 bg-emerald-600 hover:bg-emerald-500 active:scale-95 text-white font-semibold rounded-xl text-xs sm:text-sm transition shadow-lg shadow-emerald-950/50"
            >
              <PlusCircle className="w-4 h-4" />
              <span>New Post</span>
            </Link>
          </div>
        </div>

        {/* Decorative background circle */}
        <div className="absolute -right-10 -bottom-10 w-64 h-64 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="p-6 rounded-2xl bg-slate-900/90 border border-slate-800 shadow-lg flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">মোট পোস্ট</p>
            <p className="text-3xl font-black text-white mt-2">{stats.totalPosts}</p>
            <p className="text-xs text-emerald-400 mt-1 flex items-center gap-1">
              <TrendingUp className="w-3.5 h-3.5" />
              <span>সকল ক্যাটাগরি অন্তর্ভুক্ত</span>
            </p>
          </div>
          <div className="p-4 rounded-2xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <FileText className="w-6 h-6" />
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/90 border border-slate-800 shadow-lg flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">প্রকাশিত (Published)</p>
            <p className="text-3xl font-black text-emerald-400 mt-2">{stats.publishedPosts}</p>
            <p className="text-xs text-slate-400 mt-1">অ্যাপে সরাসরি দৃশ্যমান</p>
          </div>
          <div className="p-4 rounded-2xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <CheckCircle className="w-6 h-6" />
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/90 border border-slate-800 shadow-lg flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">খসড়া (Draft)</p>
            <p className="text-3xl font-black text-amber-400 mt-2">{stats.draftPosts}</p>
            <p className="text-xs text-slate-400 mt-1">অপ্রকাশিত অবস্থায় সংরক্ষিত</p>
          </div>
          <div className="p-4 rounded-2xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <Clock className="w-6 h-6" />
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/90 border border-slate-800 shadow-lg flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">ক্যাটাগরি</p>
            <p className="text-3xl font-black text-teal-400 mt-2">{stats.totalCategories}</p>
            <p className="text-xs text-slate-400 mt-1">বিষয়ভিত্তিক বিভাগ</p>
          </div>
          <div className="p-4 rounded-2xl bg-teal-500/10 text-teal-400 border border-teal-500/20">
            <FolderTree className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Islamic Analytics & Recharts Visualization: Prayer Times Distribution & User Engagement Metrics */}
      <IslamicAnalyticsWidget posts={postsList} categories={categoriesList} />

      {/* Navigation Quick Links */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <Link
          to="/posts"
          className="group p-6 rounded-2xl bg-slate-900/70 border border-slate-800 hover:border-emerald-500/40 transition shadow-lg space-y-3"
        >
          <div className="flex items-center justify-between">
            <div className="p-3 bg-emerald-500/10 text-emerald-400 rounded-xl">
              <FileText className="w-5 h-5" />
            </div>
            <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-emerald-400 transition" />
          </div>
          <h3 className="text-lg font-bold text-white group-hover:text-emerald-300 transition">
            পোস্ট ম্যানেজমেন্ট
          </h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            কুরআন, হাদিস, দৈনন্দিন আমল, দোয়ার বাংলা অর্থ, আরবি উচ্চারণ ও হাদিস রেফারেন্স সহ পোস্ট যোগ এবং এডিট করুন।
          </p>
        </Link>

        <Link
          to="/categories"
          className="group p-6 rounded-2xl bg-slate-900/70 border border-slate-800 hover:border-emerald-500/40 transition shadow-lg space-y-3"
        >
          <div className="flex items-center justify-between">
            <div className="p-3 bg-teal-500/10 text-teal-400 rounded-xl">
              <FolderTree className="w-5 h-5" />
            </div>
            <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-teal-400 transition" />
          </div>
          <h3 className="text-lg font-bold text-white group-hover:text-teal-300 transition">
            ক্যাটাগরি কন্ট্রোল
          </h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            প্রধান ক্যাটাগরি ও সাব-ক্যাটাগরি তৈরি, সক্রিয়/নিষ্ক্রিয় ও ক্রমবিন্যাস (Hierarchy & Ordering) নিয়ন্ত্রণ করুন।
          </p>
        </Link>

        <Link
          to="/quizzes"
          className="group p-6 rounded-2xl bg-slate-900/70 border border-slate-800 hover:border-emerald-500/40 transition shadow-lg space-y-3"
        >
          <div className="flex items-center justify-between">
            <div className="p-3 bg-amber-500/10 text-amber-400 rounded-xl">
              <HelpCircle className="w-5 h-5" />
            </div>
            <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-amber-400 transition" />
          </div>
          <h3 className="text-lg font-bold text-white group-hover:text-amber-300 transition">
            ইসলামিক কুইজ ব্যাংক
          </h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            ৪টি অপশন ভিত্তিক ইসলামিক সাধারণ জ্ঞান ও কুইজ প্রশ্ন তৈরি করুন।
          </p>
        </Link>
      </div>

      {/* Recent Audit Logs */}
      <div className="rounded-2xl bg-slate-900/80 border border-slate-800 p-6 shadow-xl space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Clock className="w-5 h-5 text-emerald-400" />
            <h2 className="text-base font-bold text-white">সাম্প্রতিক অ্যাডমিন কার্যকলাপ (Activity Audit)</h2>
          </div>
        </div>

        {recentLogs.length === 0 ? (
          <p className="text-xs text-slate-500 italic py-4">এখনো কোনো কার্যকলাপ রেকর্ড হয়নি।</p>
        ) : (
          <div className="divide-y divide-slate-800/80">
            {recentLogs.map((log) => (
              <div key={log.id} className="py-3 flex items-center justify-between text-xs">
                <div>
                  <span className="font-semibold text-emerald-300">{log.action}: </span>
                  <span className="text-slate-300">{log.details}</span>
                </div>
                <div className="text-slate-500 text-[11px] whitespace-nowrap ml-4">
                  {new Date(log.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
