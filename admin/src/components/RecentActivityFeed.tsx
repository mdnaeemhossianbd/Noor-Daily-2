import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { 
  Clock, 
  FileText, 
  HelpCircle, 
  Sliders, 
  FolderTree, 
  ShieldCheck, 
  CheckCircle2, 
  Edit3, 
  Trash2, 
  Sparkles,
  ArrowRight,
  Filter
} from 'lucide-react';
import { AuditLog, Post } from '../types/schema';

export interface ActivityItem {
  id: string;
  type: 'post' | 'quiz' | 'config' | 'category' | 'system';
  action: string;
  title: string;
  description: string;
  author: string;
  timestamp: number;
  link?: string;
  badgeColor: 'emerald' | 'blue' | 'amber' | 'purple' | 'rose';
}

interface RecentActivityFeedProps {
  logs: AuditLog[];
  posts: Post[];
}

export const RecentActivityFeed: React.FC<RecentActivityFeedProps> = ({ logs, posts }) => {
  const [activeFilter, setActiveFilter] = useState<'all' | 'post' | 'quiz' | 'config'>('all');

  // Synthesize unified activities from audit logs and latest post changes
  const combinedActivities: ActivityItem[] = [];

  // 1. Audit logs
  logs.forEach((log) => {
    let type: ActivityItem['type'] = 'system';
    let badgeColor: ActivityItem['badgeColor'] = 'emerald';
    let link: string | undefined = undefined;

    const actionUpper = (log.action || '').toUpperCase();
    if (actionUpper.includes('POST')) {
      type = 'post';
      badgeColor = actionUpper.includes('DELETE') ? 'rose' : actionUpper.includes('UPDATE') ? 'blue' : 'emerald';
      link = '/posts';
    } else if (actionUpper.includes('QUIZ')) {
      type = 'quiz';
      badgeColor = 'purple';
      link = '/quizzes';
    } else if (actionUpper.includes('PRAYER') || actionUpper.includes('CONFIG') || actionUpper.includes('ADMOB')) {
      type = 'config';
      badgeColor = 'amber';
      link = actionUpper.includes('PRAYER') ? '/prayer-config' : '/admob-settings';
    } else if (actionUpper.includes('CAT') || actionUpper.includes('SUB')) {
      type = 'category';
      badgeColor = 'blue';
      link = '/categories';
    }

    combinedActivities.push({
      id: log.id || `log_${log.timestamp}_${Math.random()}`,
      type,
      action: log.action,
      title: log.action.replace(/_/g, ' '),
      description: log.details || 'System configuration change recorded.',
      author: log.user || 'Administrator',
      timestamp: log.timestamp || Date.now(),
      link,
      badgeColor,
    });
  });

  // 2. Synthesize latest post activities if they are not already in audit logs
  posts.slice(0, 10).forEach((p) => {
    const postTime = p.updatedAt || p.createdAt || (Date.now() - 3600000);
    // Only add if not duplicates
    const alreadyLogged = combinedActivities.some(
      (a) => Math.abs(a.timestamp - postTime) < 2000 && a.description.includes(p.titleBn || p.titleEn || '')
    );
    if (!alreadyLogged) {
      combinedActivities.push({
        id: `post_act_${p.id}`,
        type: 'post',
        action: p.status === 'published' ? 'POST_PUBLISHED' : 'POST_DRAFTED',
        title: p.status === 'published' ? 'পোস্ট প্রকাশিত হয়েছে' : 'পোস্ট সংরক্ষিত (খসড়া)',
        description: `"${p.titleBn || p.titleEn}" (${p.categoryName || 'ইসলামিক বিষয়'})`,
        author: p.author || 'habitsk50@gmail.com',
        timestamp: postTime,
        link: '/posts',
        badgeColor: p.status === 'published' ? 'emerald' : 'blue',
      });
    }
  });

  // Sort descending by timestamp
  combinedActivities.sort((a, b) => b.timestamp - a.timestamp);

  // Filter
  const filtered = activeFilter === 'all'
    ? combinedActivities.slice(0, 15)
    : combinedActivities.filter((a) => a.type === activeFilter).slice(0, 15);

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

  const getBadgeStyle = (color: ActivityItem['badgeColor']) => {
    switch (color) {
      case 'emerald':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'blue':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'amber':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'purple':
        return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
      case 'rose':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
    }
  };

  const getTypeIcon = (type: ActivityItem['type']) => {
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
      {/* Header & Filter Pills */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl text-emerald-400">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white tracking-wide">
              সাম্প্রতিক কার্যকলাপ ও অডিট ফিড (Recent Activity)
            </h2>
            <p className="text-xs text-slate-400">
              পোস্ট তৈরি ও এডিট, কুইজ আপডেট এবং নামাজের সময়সূচি কনফিগারেশন লগ
            </p>
          </div>
        </div>

        {/* Filter buttons */}
        <div className="flex flex-wrap items-center gap-1.5 p-1 bg-slate-950 rounded-xl border border-slate-800 text-xs">
          <button
            onClick={() => setActiveFilter('all')}
            className={`px-3 py-1.5 rounded-lg font-medium transition ${
              activeFilter === 'all'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white hover:bg-slate-900'
            }`}
          >
            সব ({combinedActivities.length})
          </button>
          <button
            onClick={() => setActiveFilter('post')}
            className={`px-3 py-1.5 rounded-lg font-medium transition ${
              activeFilter === 'post'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white hover:bg-slate-900'
            }`}
          >
            পোস্ট
          </button>
          <button
            onClick={() => setActiveFilter('quiz')}
            className={`px-3 py-1.5 rounded-lg font-medium transition ${
              activeFilter === 'quiz'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white hover:bg-slate-900'
            }`}
          >
            কুইজ
          </button>
          <button
            onClick={() => setActiveFilter('config')}
            className={`px-3 py-1.5 rounded-lg font-medium transition ${
              activeFilter === 'config'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white hover:bg-slate-900'
            }`}
          >
            কনফিগারেশন
          </button>
        </div>
      </div>

      {/* Activity Timeline List */}
      {filtered.length === 0 ? (
        <div className="py-12 text-center space-y-3">
          <Clock className="w-8 h-8 text-slate-600 mx-auto" />
          <p className="text-sm font-semibold text-slate-400">কোনো কার্যকলাপ পাওয়া যায়নি</p>
          <p className="text-xs text-slate-500">নতুন কোনো পোস্ট বা সেটিংস আপডেট করলে এখানে তা সরাসরি প্রদর্শিত হবে।</p>
        </div>
      ) : (
        <div className="relative pl-6 space-y-4 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-800">
          {filtered.map((item) => (
            <div key={item.id} className="relative group">
              {/* Timeline marker */}
              <div className="absolute -left-[1.45rem] top-2 w-3.5 h-3.5 rounded-full bg-slate-950 border-2 border-emerald-400 group-hover:scale-125 transition-transform" />

              <div className="p-4 rounded-2xl bg-slate-950/70 border border-slate-800/90 hover:border-slate-700 transition flex flex-col md:flex-row md:items-center justify-between gap-3">
                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 shrink-0">
                    {getTypeIcon(item.type)}
                  </div>
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className={`px-2 py-0.5 rounded-md text-[11px] font-semibold border ${getBadgeStyle(item.badgeColor)}`}>
                        {item.action}
                      </span>
                      <span className="text-xs text-slate-500">•</span>
                      <span className="text-xs text-slate-400 font-mono">
                        {item.author}
                      </span>
                    </div>
                    <p className="text-sm text-slate-200 font-medium">
                      {item.description}
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
                      title="বিস্তারিত দেখুন"
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
