import React, { useState, useEffect } from 'react';
import { 
  ref, 
  set, 
  remove, 
  onValue 
} from 'firebase/database';
import { db } from '../api/firebase';
import { Category } from '../types/schema';
import { useAuth } from '../context/AuthContext';
import { 
  FolderPlus, 
  FolderTree, 
  Edit3, 
  Trash2, 
  Search, 
  ChevronRight, 
  Check, 
  X,
  AlertCircle,
  RefreshCw,
  Sparkles
} from 'lucide-react';
import { defaultCategories } from '../data/seedData';
import { getInitialCategories, logAudit } from '../api/firestoreService';

export const CategoriesManager: React.FC = () => {
  const { currentUser } = useAuth();
  const [categories, setCategories] = useState<Category[]>(getInitialCategories());
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [syncToast, setSyncToast] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Modal / Form state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [nameEn, setNameEn] = useState('');
  const [nameAr, setNameAr] = useState('');
  const [nameBn, setNameBn] = useState('');
  const [parentId, setParentId] = useState<string>('root');
  const [order, setOrder] = useState<number>(0);
  const [isActive, setIsActive] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const catsRef = ref(db, 'categories');
    const unsub = onValue(catsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const list: Category[] = Object.values(data);
        list.sort((a, b) => a.depth - b.depth || a.order - b.order);
        setCategories(list);
        localStorage.setItem('noor_categories', JSON.stringify(list));
      }
    }, (err) => {
      console.warn('RTDB categories listener note:', err);
    });

    return () => unsub();
  }, []);

  const syncDefaultCategories = async (manual = true) => {
    try {
      if (manual) setSyncing(true);
      const catMap: Record<string, Category> = {};
      const catsToSeed = categories.length > 0 ? categories : defaultCategories;
      for (const cat of catsToSeed) {
        const catId = cat.id || `cat_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        catMap[catId] = { ...cat, id: catId, updatedAt: Date.now() };
      }
      await set(ref(db, 'categories'), catMap);
      setCategories(Object.values(catMap));
      localStorage.setItem('noor_categories', JSON.stringify(Object.values(catMap)));

      if (manual) {
        setSyncToast('সকল ইসলামিক ক্যাটাগরি ফায়ারবেস ক্লাউডে সফলভাবে সিঙ্ক হয়েছে!');
        setTimeout(() => setSyncToast(null), 4000);
      }
    } catch (err: any) {
      console.error('Failed to sync categories:', err);
      if (manual) alert('Error: ' + err.message);
    } finally {
      if (manual) setSyncing(false);
    }
  };

  const openCreateModal = () => {
    setEditingCategory(null);
    setNameEn('');
    setNameAr('');
    setNameBn('');
    setParentId('root');
    setOrder(categories.length);
    setIsActive(true);
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const openEditModal = (cat: Category) => {
    setEditingCategory(cat);
    setNameEn(cat.name_en || '');
    setNameAr(cat.name_ar || '');
    setNameBn(cat.name_bn || '');
    setParentId(cat.parentId || 'root');
    setOrder(cat.order || 0);
    setIsActive(cat.isActive ?? true);
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!nameBn.trim()) {
      setErrorMessage('অনুগ্রহ করে ক্যাটাগরির বাংলা নাম লিখুন।');
      return;
    }

    setSubmitting(true);
    try {
      const parent = parentId === 'root' ? null : categories.find(c => c.id === parentId);
      const computedDepth = parent ? parent.depth + 1 : 0;
      const catId = editingCategory?.id || `cat_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;

      const payload: Category = {
        id: catId,
        name_bn: nameBn.trim(),
        name_en: nameEn.trim() || nameBn.trim(), // Auto fallback
        name_ar: nameAr.trim() || '',
        parentId: parent ? parent.id! : null,
        depth: computedDepth,
        order: Number(order) || 0,
        isActive: isActive,
        updatedAt: Date.now(),
      };

      // 1. Instantly update local state & localStorage
      setCategories(prev => {
        const exists = prev.some(c => c.id === catId);
        const updated = exists 
          ? prev.map(c => c.id === catId ? payload : c)
          : [...prev, payload];
        localStorage.setItem('noor_categories', JSON.stringify(updated));
        return updated;
      });

      setIsModalOpen(false);
      setSubmitting(false);

      // 2. Write to Firebase Realtime Database with explicit feedback
      try {
        await set(ref(db, `categories/${catId}`), payload);
        setSyncToast(editingCategory ? 'ক্যাটাগরি সফলভাবে ক্লাউড ডাটাবেসে আপডেট হয়েছে!' : 'নতুন ক্যাটাগরি সফলভাবে ডাটাবেসে তৈরি হয়েছে!');
        setTimeout(() => setSyncToast(null), 4000);

        logAudit(
          editingCategory ? 'CATEGORY_UPDATED' : 'CATEGORY_CREATED',
          `Category "${payload.name_en || payload.name_bn}" saved`
        ).catch(() => {});
      } catch (dbErr: any) {
        console.error('Category write error:', dbErr);
        if (dbErr.code === 'PERMISSION_DENIED' || dbErr.message?.includes('PERMISSION_DENIED')) {
          setErrorMessage('ক্যাটাগরি লোকালি সেভ হয়েছে, কিন্তু Firebase Rules-এর কারণে ক্লাউডে রাইট হয়নি (PERMISSION_DENIED)। অনুগ্রহ করে Firebase Console-এ রুলস আপডেট করুন।');
        } else {
          setErrorMessage('ডাটাবেসে সেভ করতে সমস্যা হয়েছে: ' + (dbErr.message || 'Error'));
        }
      }
    } catch (err: any) {
      console.error('Failed to save category:', err);
      setErrorMessage('Failed to save: ' + (err.message || 'Error'));
      setSubmitting(false);
    }
  };

  const handleDelete = async (categoryId: string, categoryName: string) => {
    const hasChildren = categories.some(c => c.parentId === categoryId);
    if (hasChildren) {
      alert('Cannot delete this category because it contains sub-categories. Remove or reassign them first.');
      return;
    }

    if (!window.confirm(`Are you sure you want to delete category "${categoryName}"?`)) return;

    try {
      await remove(ref(db, `categories/${categoryId}`));

      setCategories(prev => {
        const filtered = prev.filter(c => c.id !== categoryId);
        localStorage.setItem('noor_categories', JSON.stringify(filtered));
        return filtered;
      });

      await logAudit('CATEGORY_DELETED', `Category "${categoryName}" deleted`);
    } catch (err: any) {
      console.error('Failed to delete category:', err);
      alert('Delete failed: ' + err.message);
    }
  };

  const handleToggleActive = async (cat: Category) => {
    const updatedStatus = !cat.isActive;
    try {
      await set(ref(db, `categories/${cat.id}/isActive`), updatedStatus);
      await set(ref(db, `categories/${cat.id}/updatedAt`), Date.now());

      setCategories(prev => {
        const updated = prev.map(c => c.id === cat.id ? { ...c, isActive: updatedStatus } : c);
        localStorage.setItem('noor_categories', JSON.stringify(updated));
        return updated;
      });
    } catch (err: any) {
      console.error('Failed to update status:', err);
      alert('Update failed: ' + err.message);
    }
  };

  const filteredCategories = categories.filter(c => 
    c.name_en.toLowerCase().includes(searchQuery.toLowerCase()) ||
    c.name_bn.toLowerCase().includes(searchQuery.toLowerCase()) ||
    c.name_ar.includes(searchQuery)
  );

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      {syncToast && (
        <div className="p-4 bg-emerald-500/20 border border-emerald-500/50 rounded-2xl flex items-center gap-3 text-emerald-200 text-sm shadow-xl">
          <Sparkles className="w-5 h-5 text-emerald-400 shrink-0" />
          <p className="flex-1 font-medium">{syncToast}</p>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 bg-slate-900 border border-slate-800 rounded-2xl">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <FolderTree className="w-5 h-5 text-emerald-400" />
            <h1 className="text-xl sm:text-2xl font-bold text-white">Categories Management</h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-400">
            Total {categories.length} categories available for mobile app navigation
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={() => syncDefaultCategories(true)}
            disabled={syncing}
            className="flex items-center gap-2 px-3.5 py-2.5 bg-slate-800 hover:bg-slate-700 text-emerald-300 rounded-xl text-sm font-medium border border-slate-700 transition disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${syncing ? 'animate-spin' : ''}`} />
            <span>{syncing ? 'সিঙ্ক হচ্ছে...' : 'সিঙ্ক করুন (Sync Categories)'}</span>
          </button>
          <button
            onClick={openCreateModal}
            className="flex items-center gap-2 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition shadow-lg shadow-emerald-900/30"
          >
            <FolderPlus className="w-4 h-4" />
            <span>Add New Category</span>
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
          placeholder="Search categories by English, Bangla, or Arabic name..."
          className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
        />
      </div>

      {/* Categories List */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden divide-y divide-slate-800/80">
        {filteredCategories.map((cat) => {
          const parent = categories.find(c => c.id === cat.parentId);
          return (
            <div key={cat.id} className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-800/30 transition">
              <div className="flex items-start sm:items-center gap-3">
                <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 border ${
                  cat.depth === 0 
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
                    : 'bg-slate-800 text-slate-400 border-slate-700 ml-4'
                }`}>
                  <FolderTree className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-bold text-white text-base">{cat.name_bn || cat.name_en}</span>
                    <span className="text-xs text-slate-400">({cat.name_en})</span>
                    {cat.name_ar && (
                      <span className="text-xs font-arabic text-emerald-300/80 bg-emerald-950/40 px-2 py-0.5 rounded border border-emerald-800/30" dir="rtl">
                        {cat.name_ar}
                      </span>
                    )}
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold border ${
                      cat.isActive 
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
                        : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                    }`}>
                      {cat.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                  {parent && (
                    <p className="text-xs text-slate-500 mt-0.5 flex items-center gap-1">
                      <span>Under:</span> <strong className="text-slate-400">{parent.name_en}</strong>
                    </p>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-2 self-end sm:self-auto">
                <button
                  onClick={() => handleToggleActive(cat)}
                  className="px-2.5 py-1 text-xs font-medium rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
                >
                  {cat.isActive ? 'Deactivate' : 'Activate'}
                </button>
                <button
                  onClick={() => openEditModal(cat)}
                  className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition"
                >
                  <Edit3 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => handleDelete(cat.id!, cat.name_en || cat.name_bn)}
                  className="p-2 text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 rounded-lg transition"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Create / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between">
              <h2 className="text-base font-bold text-white">
                {editingCategory ? 'Edit Category' : 'Create New Category'}
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
                <label className="block text-xs font-semibold text-slate-200 mb-1.5 flex items-center justify-between">
                  <span>ক্যাটাগরির বাংলা নাম <span className="text-emerald-400">*</span></span>
                  <span className="text-[11px] text-emerald-400">ইংরেজি স্বয়ংক্রিয় হবে</span>
                </label>
                <input
                  type="text"
                  value={nameBn}
                  onChange={(e) => setNameBn(e.target.value)}
                  placeholder="যেমন: পবিত্র কুরআন ও তাফসির"
                  required
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1.5">
                  ইংরেজি নাম (ঐচ্ছিক - ফাঁকা রাখলে স্বয়ংক্রিয়ভাবে বাংলা নাম ব্যবহার হবে)
                </label>
                <input
                  type="text"
                  value={nameEn}
                  onChange={(e) => setNameEn(e.target.value)}
                  placeholder="যেমন: Holy Quran & Tafsir"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">আরবি নাম (ঐচ্ছিক)</label>
                <input
                  type="text"
                  value={nameAr}
                  onChange={(e) => setNameAr(e.target.value)}
                  dir="rtl"
                  placeholder="القرآن الكريم والتفسير"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-emerald-300 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">প্যারেন্ট বিভাগ (ঐচ্ছিক)</label>
                  <select
                    value={parentId}
                    onChange={(e) => setParentId(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"
                  >
                    <option value="root">মূল বিভাগ (Top Level)</option>
                    {categories.filter(c => c.id !== editingCategory?.id).map((c) => (
                      <option key={c.id} value={c.id}>{c.name_bn || c.name_en}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">ক্রম নম্বর (Display Order)</label>
                  <input
                    type="number"
                    value={order}
                    onChange={(e) => setOrder(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"
                  />
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs font-semibold hover:bg-slate-700 transition"
                >
                  বাতিল করুন
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-emerald-600 text-white rounded-xl text-xs font-semibold shadow-lg shadow-emerald-900/30 hover:bg-emerald-500 transition disabled:opacity-50"
                >
                  {submitting ? 'সংরক্ষণ হচ্ছে...' : editingCategory ? 'ক্যাটাগরি আপডেট করুন' : 'ক্যাটাগরি সংরক্ষণ করুন'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
