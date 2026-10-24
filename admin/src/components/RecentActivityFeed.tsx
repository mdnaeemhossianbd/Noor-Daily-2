import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  collection, 
  query as firestoreQuery, 
  orderBy as firestoreOrderBy, 
  limit as firestoreLimit,
  onSnapshot 
} from 'firebase/firestore';
import { firestore } from '../api/firebase';
import { fetchRecentFirestoreActivity, FirestoreActivityItem } from '../api/firestoreService';
import { 
  Clock, 
  FileText, 
  HelpCircle, 
  Sliders, 
  FolderTree, 
  Sparkles,
  ArrowRight,
  RefreshCw,
  Database,
  ExternalLink
} from 'lucide-react';
import { AuditLog, Post } from '../types/schema';

interface RecentActivityFeedProps {
  logs?: AuditLog[];
  posts?: Post[];
}

export const RecentActivityFeed: React.FC<RecentActivityFeedProps> = ({ logs = [], posts = [] }) => {
  const [activities, setActivities] = useState<FirestoreActivityItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState<boolean>(false);
  const [dataSource, setDataSource] = useState<'firestore' | 'cache'>('firestore');

  const loadActivities = async () => {
    try {
      setRefreshing(true);
      const fsItems = await fetchRecentFirestoreActivity(5);
      if (fsItems && fsItems.length > 0) {
        setActivities(fsItems);
        setDataSource('firestore');
      } else {
        // Fallback from logs/posts if Firestore query returns empty
        fallbackFromProps();
      }
    } catch (err) {
      console.warn('Error loading Firestore activities, falling back:', err);
      fallbackFromProps();
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const fallbackFromProps = () => {
    const list: FirestoreActivityItem[] = [];
    logs.slice(0, 5).forEach((l) => {
      list.push({
        id: l.id,
        action: l.action || 'ADMIN_ACTION',
        details: l.details || 'Configuration updated',
        timestamp: l.timestamp || Date.now(),
        user: l.user || 'Administrator',
        type: l.action?.includes('POST') ? 'post' : l.action?.includes('QUIZ') ? 'quiz' : 'config',
        link: l.action?.includes('POST') ? '/posts' : l.action?.includes('QUIZ') ? '/quizzes' : '/prayer-config',
        source: 'fallback'
      });
    });
    setActivities(list.slice(0, 5));
    setDataSource('cache');
  };

  useEffect(() => {
    // 1. Initial load from Firestore
    loadActivities();

    // 2. Real-time Firestore snapshot listener for live updates
    let unsubscribe: (() => void) | undefined;
    try {
      const q = firestoreQuery(
        collection(firestore, 'audit_logs'),
        firestoreOrderBy('timestamp', 'desc'),
        firestoreLimit(5)
      );
      unsubscribe = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const freshList: FirestoreActivityItem[] = [];
          snapshot.forEach((docSnap) => {
            const data = docSnap.data();
            const action = data.action || 'CONTENT_UPDATE';
            const actionUpper = action.toUpperCase();

            let type: FirestoreActivityItem['type'] = 'system';
            let link: string | undefined = undefined;

            if (actionUpper.includes('POST')) {
              type = 'post';
              link = '/posts';
            } else if (actionUpper.includes('QUIZ')) {
              type = 'quiz';
              link = '/quizzes';
            } else if (actionUpper.includes('PRAYER') || actionUpper.includes('CONFIG') || actionUpper.includes('ADMOB')) {
              type = 'config';
              link = actionUpper.includes('PRAYER') ? '/prayer-config' : '/admob-settings';
            } else if (actionUpper.includes('CAT') || actionUpper.includes('SUB')) {
              type = 'category';
              link = '/categories';
            }

            freshList.push({
              id: docSnap.id,
              action,
              details: data.details || 'Content update recorded in Firestore',
              timestamp: Number(data.timestamp) || Date.now(),
              user: data.user || 'Administrator',
              type,
              link,
              source: 'firestore'
            });
          });

          setActivities(freshList.slice(0, 5));
          setDataSource('firestore');
          setLoading(false);
        }
      }, (err) => {
        console.warn('Firestore real-time listener notice:', err);
      });
    } catch (e) {
      console.warn('Failed to set up Firestore onSnapshot listener:', e);
    }

    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, []);

  const formatRelativeTime = (timestamp: number) => {
    const diff = Math.max(0, Date.now() - timestamp);
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'এইমাত্র (Just now)';
    if (minutes < 60) return `${minutes} মিনিট আগে`;
    if (hours < 24) return `${hours} ঘন্টা আগে`;
    if (days === 1) return 'গতকাল';
    if (days < 7) return `${days} দিন আগে`;
    return new Date(timestamp).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const getBadgeStyle = (action: string) => {
    const act = (action || '').toUpperCase();
    if (act.includes('DELETE') || act.includes('REMOVE')) {
      return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
    }
    if (act.includes('PUBLISH') || act.includes('SYNC')) {
      return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
    }
    if (act.includes('CONFIG') || act.includes('SETTING') || act.includes('PRAYER')) {
      return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
    }
    if (act.includes('QUIZ')) {
      return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
    }
    return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
  };

  const getTypeIcon = (type: FirestoreActivityItem['type']) => {
    switch (type) {
      case 'post':
        return <FileText className="w-4 h-4 text-emerald-400" />;
      case 'quiz':
        return <HelpCircle className="w-4 h-4 text-purple-400" />;
      case 'config':
        return <Sliders className="w-4 h-4 text-amber-400" />;
      case 'category':
        return <FolderTree className="w-4 h-4 text-blue-400" />;
      default:
        return <Sparkles className="w-4 h-4 text-teal-400" />;
    }
  };

  return (
    <div className="rounded-3xl bg-slate-900/90 border border-slate-800 p-6 md:p-8 shadow-2xl space-y-6">
      {/* Widget Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl text-emerald-400">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-white tracking-wide">
                Recent Activity (সাম্প্রতিক কার্যকলাপ)
              </h2>
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                Firebase Firestore
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Firebase Firestore থেকে সরাসরি সর্বশেষ ৫টি কনটেন্ট আপডেট ও অ্যাডমিন অ্যাকশন
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2">
          <button
            onClick={loadActivities}
            disabled={refreshing}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 active:scale-95 text-slate-300 hover:text-white text-xs font-medium border border-slate-700/80 transition disabled:opacity-50"
            title="Refresh latest updates from Firestore"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin text-emerald-400' : ''}`} />
            <span>{refreshing ? 'রিফ্রেশ হচ্ছে...' : 'Refresh Firestore'}</span>
          </button>

          <Link
            to="/audit-logs"
            className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-emerald-600/20 hover:bg-emerald-600 text-emerald-300 hover:text-white text-xs font-semibold border border-emerald-500/30 transition"
          >
            <span>সম্পূর্ণ লগ</span>
            <ExternalLink className="w-3 h-3" />
          </Link>
        </div>
      </div>

      {/* Loading Skeleton */}
      {loading ? (
        <div className="space-y-3 py-4">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="animate-pulse p-4 rounded-2xl bg-slate-950/40 border border-slate-800/60 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-slate-800" />
                <div className="space-y-2">
                  <div className="w-32 h-3.5 bg-slate-800 rounded" />
                  <div className="w-48 h-3 bg-slate-800/60 rounded" />
                </div>
              </div>
              <div className="w-20 h-3 bg-slate-800 rounded" />
            </div>
          ))}
        </div>
      ) : activities.length === 0 ? (
        <div className="py-12 text-center space-y-3">
          <Database className="w-8 h-8 text-slate-600 mx-auto" />
          <p className="text-sm font-semibold text-slate-400">ফায়ারবেসে কোনো অ্যাক্টিভিটি পাওয়া যায়নি</p>
          <p className="text-xs text-slate-500">কনটেন্ট তৈরি বা এডিট করলে সর্বশেষ ৫টি রেকর্ড এখানে স্বয়ংক্রিয়ভাবে ভেসে উঠবে।</p>
          <button
            onClick={loadActivities}
            className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-600 text-white rounded-xl text-xs font-semibold hover:bg-emerald-500 transition"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>চেক করুন</span>
          </button>
        </div>
      ) : (
        /* Top 5 Firestore Recent Activity Items */
        <div className="relative pl-6 space-y-3.5 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-800/80">
          {activities.slice(0, 5).map((item, index) => (
            <div key={item.id || index} className="relative group">
              {/* Timeline marker */}
              <div className="absolute -left-[1.45rem] top-3.5 w-3.5 h-3.5 rounded-full bg-slate-950 border-2 border-emerald-400 group-hover:scale-125 transition-transform shadow-md shadow-emerald-500/20" />

              <div className="p-4 rounded-2xl bg-slate-950/70 border border-slate-800/90 hover:border-emerald-500/40 hover:bg-slate-950/90 transition flex flex-col md:flex-row md:items-center justify-between gap-3 shadow-sm">
                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 shrink-0 mt-0.5">
                    {getTypeIcon(item.type)}
                  </div>
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className={`px-2 py-0.5 rounded-md text-[11px] font-semibold border ${getBadgeStyle(item.action)}`}>
                        {item.action}
                      </span>
                      <span className="text-xs text-slate-600">•</span>
                      <span className="text-xs text-slate-400 font-mono">
                        {item.user}
                      </span>
                      <span className="text-[10px] text-emerald-400/80 bg-emerald-950/40 px-1.5 py-0.2 rounded border border-emerald-800/30">
                        Firestore
                      </span>
                    </div>
                    <p className="text-sm text-slate-200 font-medium leading-snug">
                      {item.details}
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between md:justify-end gap-3 shrink-0 pt-2 md:pt-0 border-t md:border-t-0 border-slate-800/60">
                  <span className="text-xs text-slate-400 whitespace-nowrap">
                    {formatRelativeTime(item.timestamp)}
                  </span>
                  {item.link && (
                    <Link
                      to={item.link}
                      className="p-1.5 rounded-lg bg-slate-900 hover:bg-emerald-600 hover:text-white text-slate-400 transition"
                      title="অ্যাকশন ওপেন করুন"
                    >
                      <ArrowRight className="w-4 h-4" />
                    </Link>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RecentActivityFeed;
