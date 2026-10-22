import React, { useState, useEffect } from 'react';
import { 
  ref, 
  set, 
  update,
  remove, 
  onValue 
} from 'firebase/database';
import { db } from '../api/firebase';
import { Category, Subcategory } from '../types/schema';
import { useAuth } from '../context/AuthContext';
import { 
  FolderPlus, 
  FolderTree, 
  Edit3, 
  Trash2, 
  Search, 
  Check, 
  X,
  AlertCircle,
  RefreshCw,
  Sparkles,
  Layers,
  ArrowRight
} from 'lucide-react';
import { defaultCategories, defaultSubcategories } from '../data/seedData';
import { getInitialCategories, getInitialSubcategories, logAudit } from '../api/firestoreService';

export const CategoriesManager: React.FC = () => {
  const { currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState<'categories' | 'subcategories'>('categories');
  const [categories, setCategories] = useState<Category[]>(getInitialCategories());
  const [subcategories, setSubcategories] = useState<Subcategory[]>(getInitialSubcategories());
  const [syncing, setSyncing] = useState(false);
  const [syncToast, setSyncToast] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('all');
  
  // Category Modal state
  const [isCatModalOpen, setIsCatModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [catNameEn, setCatNameEn] = useState('');
  const [catNameAr, setCatNameAr] = useState('');
  const [catNameBn, setCatNameBn] = useState('');
  const [catOrder, setCatOrder] = useState<number>(0);
  const [catIsActive, setCatIsActive] = useState<boolean>(true);

  // Subcategory Modal state
  const [isSubModalOpen, setIsSubModalOpen] = useState(false);
  const [editingSubcategory, setEditingSubcategory] = useState<Subcategory | null>(null);
  const [subCategoryId, setSubCategoryId] = useState('');
  const [subParentSubId, setSubParentSubId] = useState<string>('');
  const [subNameEn, setSubNameEn] = useState('');
  const [subNameAr, setSubNameAr] = useState('');
  const [subNameBn, setSubNameBn] = useState('');
  const [subOrder, setSubOrder] = useState<number>(0);
  const [subIsActive, setSubIsActive] = useState<boolean>(true);

  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // RTDB Listeners for categories and subcategories
  useEffect(() => {
    // 1. Categories
    const catsRef = ref(db, 'categories');
    const unsubCats = onValue(catsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const list: Category[] = Object.values(data);
        list.sort((a, b) => (a.order || 0) - (b.order || 0));
        setCategories(list);
        localStorage.setItem('noor_categories', JSON.stringify(list));
      }
    });

    // 2. Subcategories
    const subcatsRef = ref(db, 'subcategories');
    const unsubSubs = onValue(subcatsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const list: Subcategory[] = Object.values(data);
        list.sort((a, b) => (a.order || 0) - (b.order || 0));
        setSubcategories(list);
        localStorage.setItem('noor_subcategories', JSON.stringify(list));
      }
    });

    return () => {
      unsubCats();
      unsubSubs();
    };
  }, []);

  const syncDefaults = async () => {
    try {
      setSyncing(true);
      // Seed categories
      const catMap: Record<string, Category> = {};
      const catsToSeed = categories.length > 0 ? categories : defaultCategories;
      for (const cat of catsToSeed) {
        const catId = cat.id || `cat_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
        catMap[catId] = { ...cat, id: catId, updatedAt: Date.now() };
      }
      await update(ref(db, 'categories'), catMap);
      setCategories(Object.values(catMap));

      // Seed subcategories
      const subMap: Record<string, Subcategory> = {};
      const subsToSeed = subcategories.length > 0 ? subcategories : defaultSubcategories;
      for (const sub of subsToSeed) {
        const subId = sub.id || `sub_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
        subMap[subId] = { ...sub, id: subId, updatedAt: Date.now() };
      }
      await update(ref(db, 'subcategories'), subMap);
      setSubcategories(Object.values(subMap));

      setSyncToast('সকল ইসলামিক প্রধান ক্যাটাগরি ও উপ-বিভাগ সফলভাবে ফায়ারবেস ক্লাউডে সিঙ্ক হয়েছে!');
      setTimeout(() => setSyncToast(null), 4000);
    } catch (err: any) {
      console.error('Failed to sync default categories/subcategories:', err);
      const message = String(err?.message || err);
      if (message.includes('PERMISSION_DENIED') || message.toLowerCase().includes('permission denied')) {
        alert(
          'Firebase permission denied. Admin login email must be habitsk50@gmail.com and the deployed Realtime Database Rules must allow this authenticated email to write /categories and /subcategories. The rules file in GitHub does not automatically update Firebase Console rules.'
        );
      } else {
        alert('ডিফল্ট সিঙ্ক ব্যর্থ হয়েছে: ' + message);
      }
    } finally {
      setSyncing(false);
    }
  };

  // CATEGORY ACTIONS
  const openCreateCategory = () => {
    setEditingCategory(null);
    setCatNameEn('');
    setCatNameAr('');
    setCatNameBn('');
    setCatOrder(categories.length);
    setCatIsActive(true);
    setErrorMessage(null);
    setIsCatModalOpen(true);
  };

  const openEditCategory = (cat: Category) => {
    setEditingCategory(cat);
    setCatNameEn(cat.name_en || '');
    setCatNameAr(cat.name_ar || '');
    setCatNameBn(cat.name_bn || '');
    setCatOrder(cat.order || 0);
    setCatIsActive(cat.isActive ?? true);
    setErrorMessage(null);
    setIsCatModalOpen(true);
  };

  const handleSaveCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    if (!catNameBn.trim()) {
      setErrorMessage('অনুগ্রহ করে ক্যাটাগরির বাংলা নাম লিখুন।');
      return;
    }
    setSubmitting(true);
    try {
      const catId = editingCategory?.id || `cat_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
      const payload: Category = {
        id: catId,
        name_bn: catNameBn.trim(),
        name_en: catNameEn.trim() || catNameBn.trim(),
        name_ar: catNameAr.trim() || '',
        parentId: null,
        depth: 0,
        order: Number(catOrder) || 0,
        isActive: catIsActive,
        updatedAt: Date.now()
      };
      await set(ref(db, `categories/${catId}`), payload);
      setCategories(prev => {
        const exists = prev.some(c => c.id === catId);
        const updated = exists ? prev.map(c => c.id === catId ? payload : c) : [...prev, payload];
        localStorage.setItem('noor_categories', JSON.stringify(updated));
        return updated;
      });
      setIsCatModalOpen(false);
      setSyncToast(editingCategory ? 'ক্যাটাগরি আপডেট হয়েছে!' : 'নতুন মূল ক্যাটাগরি তৈরি হয়েছে!');
      setTimeout(() => setSyncToast(null), 3500);
      logAudit(editingCategory ? 'CATEGORY_UPDATED' : 'CATEGORY_CREATED', `Category "${payload.name_bn}" saved`);
    } catch (err: any) {
      setErrorMessage(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteCategory = async (categoryId: string, categoryName: string) => {
    const hasSubs = subcategories.some(s => s.categoryId === categoryId);
    if (hasSubs) {
      alert(`এই ক্যাটাগরিতে উপ-বিভাগ রয়েছে। প্রথমে এর অধীনস্থ উপ-বিভাগগুলো মুছুন বা স্থানান্তর করুন।`);
      return;
    }
    if (!window.confirm(`আপনি কি "${categoryName}" ক্যাটাগরিটি মুছে ফেলতে চান?`)) return;
    try {
      await remove(ref(db, `categories/${categoryId}`));
      setCategories(prev => {
        const filtered = prev.filter(c => c.id !== categoryId);
        localStorage.setItem('noor_categories', JSON.stringify(filtered));
        return filtered;
      });
      logAudit('CATEGORY_DELETED', `Category "${categoryName}" deleted`);
    } catch (err: any) {
      alert('Delete failed: ' + err.message);
    }
  };

  // SUBCATEGORY ACTIONS
  const openCreateSubcategory = (catId?: string) => {
    setEditingSubcategory(null);
    setSubCategoryId(catId || (categories[0]?.id || ''));
    setSubParentSubId('');
    setSubNameEn('');
    setSubNameAr('');
    setSubNameBn('');
    setSubOrder(subcategories.length);
    setSubIsActive(true);
    setErrorMessage(null);
    setIsSubModalOpen(true);
  };

  const openEditSubcategory = (sub: Subcategory) => {
    setEditingSubcategory(sub);
    setSubCategoryId(sub.categoryId);
    setSubParentSubId(sub.parentSubcategoryId || '');
    setSubNameEn(sub.name_en || '');
    setSubNameAr(sub.name_ar || '');
    setSubNameBn(sub.name_bn || '');
    setSubOrder(sub.order || 0);
    setSubIsActive(sub.isActive ?? true);
    setErrorMessage(null);
    setIsSubModalOpen(true);
  };

  const handleSaveSubcategory = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    if (!subCategoryId) {
      setErrorMessage('অনুগ্রহ করে মূল ক্যাটাগরি নির্বাচন করুন।');
      return;
    }
    if (!subNameBn.trim()) {
      setErrorMessage('অনুগ্রহ করে উপ-বিভাগের বাংলা নাম লিখুন।');
      return;
    }
    setSubmitting(true);
    try {
      const subId = editingSubcategory?.id || `sub_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
      const payload: Subcategory = {
        id: subId,
        categoryId: subCategoryId,
        parentSubcategoryId: subParentSubId.trim() ? subParentSubId.trim() : null,
        name_bn: subNameBn.trim(),
        name_en: subNameEn.trim() || subNameBn.trim(),
        name_ar: subNameAr.trim() || '',
        order: Number(subOrder) || 0,
        isActive: subIsActive,
        updatedAt: Date.now()
      };
      await set(ref(db, `subcategories/${subId}`), payload);
      setSubcategories(prev => {
        const exists = prev.some(s => s.id === subId);
        const updated = exists ? prev.map(s => s.id === subId ? payload : s) : [...prev, payload];
        localStorage.setItem('noor_subcategories', JSON.stringify(updated));
        return updated;
      });
      setIsSubModalOpen(false);
      setSyncToast(editingSubcategory ? 'উপ-বিভাগ সফলভাবে আপডেট হয়েছে!' : 'নতুন উপ-বিভাগ সফলভাবে তৈরি হয়েছে!');
      setTimeout(() => setSyncToast(null), 3500);
      logAudit(editingSubcategory ? 'SUBCATEGORY_UPDATED' : 'SUBCATEGORY_CREATED', `Subcategory "${payload.name_bn}" saved`);
    } catch (err: any) {
      setErrorMessage(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteSubcategory = async (subId: string, subName: string) => {
    if (!window.confirm(`আপনি কি "${subName}" উপ-বিভাগটি মুছে ফেলতে চান?`)) return;
    try {
      await remove(ref(db, `subcategories/${subId}`));
      setSubcategories(prev => {
        const filtered = prev.filter(s => s.id !== subId);
        localStorage.setItem('noor_subcategories', JSON.stringify(filtered));
        return filtered;
      });
      logAudit('SUBCATEGORY_DELETED', `Subcategory "${subName}" deleted`);
    } catch (err: any) {
      alert('Delete failed: ' + err.message);
    }
  };

  const filteredCategories = categories.filter(c => 
    c.name_en.toLowerCase().includes(searchQuery.toLowerCase()) ||
    c.name_bn.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const filteredSubcategories = subcategories.filter(s => {
    const matchesCat = selectedCategoryFilter === 'all' || s.categoryId === selectedCategoryFilter;
    const matchesSearch = !searchQuery.trim() || 
      s.name_en.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.name_bn.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCat && matchesSearch;
  });

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
            <h1 className="text-xl sm:text-2xl font-bold text-white">ক্যাটাগরি ও উপ-বিভাগ ব্যবস্থাপনা</h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-400">
            লেভেল ১ (মূল ক্যাটাগরি) ও লেভেল ২ (উপ-বিভাগ) পরিচালনা করুন
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={syncDefaults}
            disabled={syncing}
            className="flex items-center gap-2 px-3.5 py-2.5 bg-slate-800 hover:bg-slate-700 text-emerald-300 rounded-xl text-sm font-medium border border-slate-700 transition disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${syncing ? 'animate-spin' : ''}`} />
            {syncing ? 'সিঙ্ক হচ্ছে...' : 'ডিফল্ট সিঙ্ক'}
          </button>

          {activeTab === 'categories' ? (
            <button
              onClick={openCreateCategory}
              className="flex items-center gap-2 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold shadow-lg shadow-emerald-900/30 transition"
            >
              <FolderPlus className="w-4 h-4" />
              নতুন মূল ক্যাটাগরি
            </button>
          ) : (
            <button
              onClick={() => openCreateSubcategory()}
              className="flex items-center gap-2 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold shadow-lg shadow-emerald-900/30 transition"
            >
              <Layers className="w-4 h-4" />
              নতুন উপ-বিভাগ
            </button>
          )}
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-2">
        <button
          onClick={() => setActiveTab('categories')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition ${
            activeTab === 'categories'
              ? 'bg-emerald-600 text-white shadow-md'
              : 'text-slate-400 hover:text-white hover:bg-slate-800'
          }`}
        >
          <FolderTree className="w-4 h-4" />
          মূল ক্যাটাগরি ({categories.length})
        </button>
        <button
          onClick={() => setActiveTab('subcategories')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition ${
            activeTab === 'subcategories'
              ? 'bg-emerald-600 text-white shadow-md'
              : 'text-slate-400 hover:text-white hover:bg-slate-800'
          }`}
        >
          <Layers className="w-4 h-4" />
          উপ-বিভাগসমূহ ({subcategories.length})
        </button>
      </div>

      {/* Filters Bar */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder={activeTab === 'categories' ? "মূল ক্যাটাগরি অনুসন্ধান করুন..." : "উপ-বিভাগ অনুসন্ধান করুন..."}
            className="w-full bg-slate-900 border border-slate-800 text-slate-200 pl-10 pr-4 py-2.5 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
          />
        </div>

        {activeTab === 'subcategories' && (
          <select
            value={selectedCategoryFilter}
            onChange={(e) => setSelectedCategoryFilter(e.target.value)}
            className="bg-slate-900 border border-slate-800 text-slate-200 px-4 py-2.5 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
          >
            <option value="all">সকল মূল ক্যাটাগরি</option>
            {categories.map(c => (
              <option key={c.id} value={c.id}>{c.name_bn} ({c.name_en})</option>
            ))}
          </select>
        )}
      </div>

      {/* TAB 1: MAIN CATEGORIES VIEW */}
      {activeTab === 'categories' && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-xs tracking-wider border-b border-slate-800">
                <tr>
                  <th className="py-3.5 px-4">অর্ডার</th>
                  <th className="py-3.5 px-4">বাংলা নাম</th>
                  <th className="py-3.5 px-4">ইংরেজি নাম</th>
                  <th className="py-3.5 px-4">উপ-বিভাগ সংখ্যা</th>
                  <th className="py-3.5 px-4">স্ট্যাটাস</th>
                  <th className="py-3.5 px-4 text-right">অ্যাকশন</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {filteredCategories.map((cat) => {
                  const subsCount = subcategories.filter(s => s.categoryId === cat.id).length;
                  return (
                    <tr key={cat.id} className="hover:bg-slate-800/40 transition">
                      <td className="py-3 px-4 font-mono text-slate-400">{cat.order}</td>
                      <td className="py-3 px-4 font-semibold text-white">{cat.name_bn}</td>
                      <td className="py-3 px-4 text-slate-300">{cat.name_en}</td>
                      <td className="py-3 px-4">
                        <button
                          onClick={() => {
                            setSelectedCategoryFilter(cat.id!);
                            setActiveTab('subcategories');
                          }}
                          className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 rounded-lg text-xs font-medium transition"
                        >
                          <Layers className="w-3.5 h-3.5" />
                          {subsCount} টি উপ-বিভাগ
                          <ArrowRight className="w-3 h-3" />
                        </button>
                      </td>
                      <td className="py-3 px-4">
                        <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-semibold ${
                          cat.isActive ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'
                        }`}>
                          {cat.isActive ? 'সক্রিয়' : 'নিষ্ক্রিয়'}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => openEditCategory(cat)}
                            className="p-1.5 text-slate-400 hover:text-emerald-400 transition"
                            title="এডিট"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteCategory(cat.id!, cat.name_bn)}
                            className="p-1.5 text-slate-400 hover:text-red-400 transition"
                            title="মুছুন"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: SUBCATEGORIES VIEW */}
      {activeTab === 'subcategories' && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-xs tracking-wider border-b border-slate-800">
                <tr>
                  <th className="py-3.5 px-4">অর্ডার</th>
                  <th className="py-3.5 px-4">উপ-বিভাগের নাম (বাংলা)</th>
                  <th className="py-3.5 px-4">ইংরেজি নাম</th>
                  <th className="py-3.5 px-4">মূল ক্যাটাগরি</th>
                  <th className="py-3.5 px-4">স্ট্যাটাস</th>
                  <th className="py-3.5 px-4 text-right">অ্যাকশন</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {filteredSubcategories.map((sub) => {
                  const parentCat = categories.find(c => c.id === sub.categoryId);
                  return (
                    <tr key={sub.id} className="hover:bg-slate-800/40 transition">
                      <td className="py-3 px-4 font-mono text-slate-400">{sub.order}</td>
                      <td className="py-3 px-4 font-semibold text-white">
                        {sub.name_bn}
                        {sub.parentSubcategoryId && (
                          <span className="block text-xs text-emerald-400/80 font-normal">নেস্টেড উপ-বিভাগ</span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-slate-300">{sub.name_en}</td>
                      <td className="py-3 px-4">
                        <span className="px-2 py-1 bg-slate-800 rounded-lg text-xs text-slate-300">
                          {parentCat ? parentCat.name_bn : sub.categoryId}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-semibold ${
                          sub.isActive ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'
                        }`}>
                          {sub.isActive ? 'সক্রিয়' : 'নিষ্ক্রিয়'}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => openEditSubcategory(sub)}
                            className="p-1.5 text-slate-400 hover:text-emerald-400 transition"
                            title="এডিট"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteSubcategory(sub.id!, sub.name_bn)}
                            className="p-1.5 text-slate-400 hover:text-red-400 transition"
                            title="মুছুন"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* CATEGORY MODAL */}
      {isCatModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg p-6 space-y-5 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h2 className="text-lg font-bold text-white">
                {editingCategory ? 'মূল ক্যাটাগরি এডিট করুন' : 'নতুন মূল ক্যাটাগরি তৈরি'}
              </h2>
              <button onClick={() => setIsCatModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {errorMessage && (
              <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-xs">
                {errorMessage}
              </div>
            )}

            <form onSubmit={handleSaveCategory} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">বাংলা নাম (আবশ্যক)</label>
                <input
                  type="text"
                  required
                  value={catNameBn}
                  onChange={(e) => setCatNameBn(e.target.value)}
                  placeholder="যেমন: নামাজ ও দোয়ার নিয়ম"
                  className="w-full bg-slate-800 border border-slate-700 text-white px-3.5 py-2.5 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">ইংরেজি নাম</label>
                <input
                  type="text"
                  value={catNameEn}
                  onChange={(e) => setCatNameEn(e.target.value)}
                  placeholder="e.g. Namaz & Prayer Rules"
                  className="w-full bg-slate-800 border border-slate-700 text-white px-3.5 py-2.5 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">অর্ডার / ক্রম</label>
                  <input
                    type="number"
                    value={catOrder}
                    onChange={(e) => setCatOrder(Number(e.target.value))}
                    className="w-full bg-slate-800 border border-slate-700 text-white px-3.5 py-2 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div className="flex items-center gap-2 pt-6">
                  <input
                    type="checkbox"
                    id="catActive"
                    checked={catIsActive}
                    onChange={(e) => setCatIsActive(e.target.checked)}
                    className="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 bg-slate-800 border-slate-700"
                  />
                  <label htmlFor="catActive" className="text-sm text-slate-200">অ্যাপে প্রদর্শন করুন</label>
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsCatModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition disabled:opacity-50"
                >
                  {submitting ? 'সংরক্ষণ হচ্ছে...' : 'সংরক্ষণ করুন'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* SUBCATEGORY MODAL */}
      {isSubModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg p-6 space-y-5 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h2 className="text-lg font-bold text-white">
                {editingSubcategory ? 'উপ-বিভাগ এডিট করুন' : 'নতুন উপ-বিভাগ তৈরি'}
              </h2>
              <button onClick={() => setIsSubModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {errorMessage && (
              <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-xs">
                {errorMessage}
              </div>
            )}

            <form onSubmit={handleSaveSubcategory} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">মূল ক্যাটাগরি (আবশ্যক)</label>
                <select
                  required
                  value={subCategoryId}
                  onChange={(e) => setSubCategoryId(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 text-white px-3.5 py-2.5 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
                >
                  <option value="">ক্যাটাগরি নির্বাচন করুন</option>
                  {categories.map(c => (
                    <option key={c.id} value={c.id}>{c.name_bn} ({c.name_en})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">উপ-বিভাগের বাংলা নাম (আবশ্যক)</label>
                <input
                  type="text"
                  required
                  value={subNameBn}
                  onChange={(e) => setSubNameBn(e.target.value)}
                  placeholder="যেমন: ফজরের নামাজ, সেহরি ও ইফতার"
                  className="w-full bg-slate-800 border border-slate-700 text-white px-3.5 py-2.5 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">উপ-বিভাগের ইংরেজি নাম</label>
                <input
                  type="text"
                  value={subNameEn}
                  onChange={(e) => setSubNameEn(e.target.value)}
                  placeholder="e.g. Fajr Prayer, Sehri & Iftar"
                  className="w-full bg-slate-800 border border-slate-700 text-white px-3.5 py-2.5 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">অর্ডার / ক্রম</label>
                  <input
                    type="number"
                    value={subOrder}
                    onChange={(e) => setSubOrder(Number(e.target.value))}
                    className="w-full bg-slate-800 border border-slate-700 text-white px-3.5 py-2 rounded-xl text-sm focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div className="flex items-center gap-2 pt-6">
                  <input
                    type="checkbox"
                    id="subActive"
                    checked={subIsActive}
                    onChange={(e) => setSubIsActive(e.target.checked)}
                    className="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 bg-slate-800 border-slate-700"
                  />
                  <label htmlFor="subActive" className="text-sm text-slate-200">অ্যাপে প্রদর্শন করুন</label>
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsSubModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition disabled:opacity-50"
                >
                  {submitting ? 'সংরক্ষণ হচ্ছে...' : 'সংরক্ষণ করুন'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
