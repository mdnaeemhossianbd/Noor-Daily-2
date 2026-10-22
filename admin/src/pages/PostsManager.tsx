import React, { useState, useEffect, useMemo } from 'react';
import { 
  ref, 
  set, 
  remove, 
  onValue 
} from 'firebase/database';
import { ref as storageRef, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../api/firebase';
import { Post, Category, Subcategory, PostStatus } from '../types/schema';
import { useAuth } from '../context/AuthContext';
import { 
  FilePlus, FileText, Edit3, Trash2, Search, CheckCircle, Clock, X, Image as ImageIcon, AlertCircle, RefreshCw, BookOpen, Quote, Sparkles, Eye, Check, Layers
} from 'lucide-react';
import { defaultPosts, defaultCategories, defaultSubcategories } from '../data/seedData';
import { getInitialPosts, getInitialCategories, getInitialSubcategories, logAudit } from '../api/firestoreService';

// Auto-generates clean English titles & metadata from Bangla inputs
function generateAutoEnglish(bnText: string, fallback: string = 'Daily Islamic Post'): string {
  if (!bnText || !bnText.trim()) return fallback;
  if (/[a-zA-Z]/.test(bnText)) return bnText.trim();

  const dictionary: Record<string, string> = {
    'ফজর': 'Fajr',
    'যোহর': 'Dhuhr',
    'আসর': 'Asr',
    'মাগরিব': 'Maghrib',
    'এশা': 'Isha',
    'নামাজ': 'Prayer (Salah)',
    'রোজা': 'Fasting (Sawm)',
    'হজ': 'Hajj',
    'যাকাত': 'Zakat',
    'কুরআন': 'Holy Quran',
    'হাদিস': 'Hadith',
    'দোয়া': 'Dua (Supplication)',
    'জিকির': 'Dhikr',
    'তাহাজ্জুদ': 'Tahajjud',
    'ঈমান': 'Iman (Faith)',
    'রমজান': 'Ramadan',
    'সুন্নাহ': 'Sunnah',
    'দরূদ': 'Durood',
    'আজকের আয়াত': 'Ayat of the Day',
    'আজকের হাদিস': 'Hadith of the Day'
  };

  for (const [bnWord, enWord] of Object.entries(dictionary)) {
    if (bnText.includes(bnWord)) {
      return `${enWord} - ${bnText.substring(0, 30)}`;
    }
  }

  return `Noor Daily: ${bnText.substring(0, 45)}`;
}

export const PostsManager: React.FC = () => {
  const { currentUser } = useAuth();
  const [posts, setPosts] = useState<Post[]>(getInitialPosts());
  const [categories, setCategories] = useState<Category[]>(getInitialCategories());
  const [subcategories, setSubcategories] = useState<Subcategory[]>(getInitialSubcategories());
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [syncSuccessMsg, setSyncSuccessMsg] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'published' | 'draft'>('all');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');
  const [subcategoryFilter, setSubcategoryFilter] = useState<string>('all');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPost, setEditingPost] = useState<Post | null>(null);

  // Form Fields - All in Bengali
  const [titleBn, setTitleBn] = useState('');
  const [subtitleBn, setSubtitleBn] = useState('');
  const [mainArabic, setMainArabic] = useState('');
  const [banglaTranslation, setBanglaTranslation] = useState('');
  const [banglaPronunciation, setBanglaPronunciation] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [mainCategory, setMainCategory] = useState('');
  const [subcategoryId, setSubcategoryId] = useState('');
  const [subCategory, setSubCategory] = useState('');
  const [author, setAuthor] = useState('');
  const [hadithReference, setHadithReference] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [status, setStatus] = useState<PostStatus>('published');

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isCustomSubCategory, setIsCustomSubCategory] = useState(false);

  // Dynamic Sub-Categories List for selected Category
  const activeSubcategories = useMemo(() => {
    return subcategories.filter(s => s.categoryId === categoryId && s.isActive !== false);
  }, [subcategories, categoryId]);

  const availableSubCategories = useMemo(() => {
    const list: string[] = [];

    // 1. Defined subcategories for this category
    activeSubcategories.forEach(s => {
      const name = (s.name_bn || s.name_en || '').trim();
      if (name && !list.includes(name)) list.push(name);
    });

    // 2. Previously used subcategories in posts under this category
    posts
      .filter(p => p.categoryId === categoryId || p.mainCategory === mainCategory)
      .forEach(p => {
        const sub = (p.subCategory || p.subcategoryNameBn || '').trim();
        if (sub && !list.includes(sub)) list.push(sub);
      });

    return list;
  }, [activeSubcategories, categoryId, mainCategory, posts]);

  useEffect(() => {
    // 1. Subscribe to Categories
    const catsRef = ref(db, 'categories');
    const unsubCats = onValue(catsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const catList: Category[] = Object.values(data);
        catList.sort((a, b) => (a.order || 0) - (b.order || 0));
        setCategories(catList);
        localStorage.setItem('noor_categories', JSON.stringify(catList));
      }
    }, (err) => {
      console.warn('RTDB categories listener:', err);
    });

    // 2. Subscribe to Subcategories
    const subsRef = ref(db, 'subcategories');
    const unsubSubs = onValue(subsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const subList: Subcategory[] = Object.values(data);
        subList.sort((a, b) => (a.order || 0) - (b.order || 0));
        setSubcategories(subList);
        localStorage.setItem('noor_subcategories', JSON.stringify(subList));
      }
    }, (err) => {
      console.warn('RTDB subcategories listener:', err);
    });

    // 3. Subscribe to Posts
    const postsRef = ref(db, 'posts');
    const unsubPosts = onValue(postsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const postList: Post[] = Object.values(data);
        postList.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
        setPosts(postList);
        localStorage.setItem('noor_posts', JSON.stringify(postList));
      }
    }, (err) => {
      console.warn('RTDB posts listener:', err);
    });

    return () => { unsubCats(); unsubSubs(); unsubPosts(); };
  }, []);

  const syncAppDefaultPosts = async (manual = true) => {
    try {
      if (manual) setSyncing(true);
      
      const catMap: Record<string, Category> = {};
      for (const cat of defaultCategories) {
        const catId = cat.id || `cat_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        catMap[catId] = { ...cat, id: catId, updatedAt: Date.now() };
      }
      await set(ref(db, 'categories'), catMap);
      setCategories(Object.values(catMap));
      localStorage.setItem('noor_categories', JSON.stringify(Object.values(catMap)));

      const subMap: Record<string, Subcategory> = {};
      for (const sub of defaultSubcategories) {
        const subId = sub.id || `sub_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        subMap[subId] = { ...sub, id: subId, updatedAt: Date.now() };
      }
      await set(ref(db, 'subcategories'), subMap);
      setSubcategories(Object.values(subMap));
      localStorage.setItem('noor_subcategories', JSON.stringify(Object.values(subMap)));

      const postMap: Record<string, Post> = {};
      const postsToSeed = posts.length > 0 ? posts : defaultPosts;
      for (const post of postsToSeed) {
        const postId = post.id || `post_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        postMap[postId] = { ...post, id: postId, updatedAt: Date.now() };
      }
      await set(ref(db, 'posts'), postMap);
      setPosts(Object.values(postMap));
      localStorage.setItem('noor_posts', JSON.stringify(Object.values(postMap)));

      if (manual) {
        setSyncSuccessMsg('অ্যাপের সকল ক্যাটাগরি, সাব-ক্যাটাগরি ও পোস্ট সফলভাবে ক্লাউড ডাটাবেসে সিঙ্ক করা হয়েছে!');
        setTimeout(() => setSyncSuccessMsg(null), 5000);
      }
    } catch (err: any) {
      console.error('Error syncing posts:', err);
      if (manual) setErrorMessage('সিঙ্ক করতে সমস্যা হয়েছে: ' + err.message);
    } finally {
      if (manual) setSyncing(false);
    }
  };

  const openCreateModal = () => {
    setEditingPost(null);
    setTitleBn('');
    setSubtitleBn('');
    setMainArabic('');
    setBanglaTranslation('');
    setBanglaPronunciation('');
    const initCatId = categories[0]?.id || 'cat_namaz';
    const initCat = categories.find(c => c.id === initCatId);
    setCategoryId(initCatId);
    setMainCategory(initCat?.name_bn || initCat?.name_en || 'নামাজ ও দোয়ার নিয়ম');
    
    const matchingSubs = subcategories.filter(s => s.categoryId === initCatId);
    if (matchingSubs.length > 0) {
      setSubcategoryId(matchingSubs[0].id);
      setSubCategory(matchingSubs[0].name_bn || matchingSubs[0].name_en || '');
    } else {
      setSubcategoryId('');
      setSubCategory('');
    }
    
    setIsCustomSubCategory(false);
    setAuthor('');
    setHadithReference('');
    setImageUrl('');
    setStatus('published');
    setSelectedFile(null);
    setUploadProgress(null);
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const openEditModal = (post: Post) => {
    setEditingPost(post);
    setTitleBn(post.title_bn || post.title_en || '');
    setSubtitleBn(post.subtitle_bn || post.subtitle_en || '');
    setMainArabic(post.mainArabic || '');
    setBanglaTranslation(post.banglaTranslation || post.body_bn || post.body_en || '');
    setBanglaPronunciation(post.banglaPronunciation || '');
    const currentCatId = post.categoryId || categories[0]?.id || '';
    setCategoryId(currentCatId);
    setMainCategory(post.mainCategory || categories.find(c => c.id === currentCatId)?.name_bn || '');

    // Resolve subcategory ID
    let subId = post.subcategoryId || '';
    if (!subId && (post.subCategory || post.subcategoryNameBn)) {
      const targetName = post.subCategory || post.subcategoryNameBn;
      const found = subcategories.find(s => 
        s.categoryId === currentCatId && 
        (s.name_bn === targetName || s.name_en === targetName)
      );
      if (found) subId = found.id;
    }
    setSubcategoryId(subId);
    setSubCategory(post.subCategory || post.subcategoryNameBn || '');
    setIsCustomSubCategory(false);
    setAuthor(post.author || '');
    setHadithReference(post.hadithReference || '');
    setImageUrl(post.imageUrl || '');
    setStatus(post.status);
    setSelectedFile(null);
    setUploadProgress(null);
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!titleBn.trim()) {
      setErrorMessage('অনুগ্রহ করে পোস্টের বাংলা শিরোনাম (Title) লিখুন।');
      return;
    }

    setSubmitting(true);

    try {
      let finalImageUrl = imageUrl || '';

      // Optional image upload with safety timeout & base64 fallback
      if (selectedFile) {
        try {
          const fileDataUrlPromise = new Promise<string>((resolve) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result as string);
            reader.onerror = () => resolve('');
            reader.readAsDataURL(selectedFile);
          });

          const fileExt = selectedFile.name.split('.').pop() || 'jpg';
          const fileRef = storageRef(storage, `posts/${Date.now()}_${Math.random().toString(36).substring(7)}.${fileExt}`);
          const uploadTask = uploadBytesResumable(fileRef, selectedFile);

          const storageUploadPromise = new Promise<string>((resolve, reject) => {
            uploadTask.on(
              'state_changed',
              (snapshot) => {
                const progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
                setUploadProgress(Math.round(progress));
              },
              (error) => reject(error),
              async () => {
                const downloadUrl = await getDownloadURL(uploadTask.snapshot.ref);
                resolve(downloadUrl);
              }
            );
          });

          finalImageUrl = await Promise.race([
            storageUploadPromise,
            new Promise<string>((resolve) => setTimeout(async () => {
              const dataUrl = await fileDataUrlPromise;
              resolve(dataUrl);
            }, 2500))
          ]);
        } catch (uploadErr) {
          console.warn('Storage upload error, using local file preview:', uploadErr);
          const reader = new FileReader();
          reader.onload = () => { finalImageUrl = (reader.result as string) || ''; };
          reader.readAsDataURL(selectedFile);
        }
      }

      const postId = editingPost?.id || `post_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
      
      // Auto English Generation
      const autoEnTitle = generateAutoEnglish(titleBn, 'Daily Islamic Post');
      const autoEnSubtitle = generateAutoEnglish(subtitleBn || titleBn, 'Noor Daily Guidance');

      const catObj = categories.find(c => c.id === categoryId);
      const subObj = subcategories.find(s => s.id === subcategoryId);
      const finalSubNameBn = subObj?.name_bn || (isCustomSubCategory ? subCategory.trim() : (editingPost?.subcategoryNameBn || subCategory.trim()));
      const finalSubNameEn = subObj?.name_en || (isCustomSubCategory ? generateAutoEnglish(subCategory.trim(), 'Islamic Guidance') : (editingPost?.subcategoryNameEn || ''));
      const finalSubNameAr = subObj?.name_ar || (editingPost?.subcategoryNameAr || '');

      const payload: Post = {
        id: postId,
        title_bn: titleBn.trim(),
        title_en: autoEnTitle, // Auto generated English title
        subtitle_bn: subtitleBn.trim() || '',
        subtitle_en: autoEnSubtitle, // Auto generated English subtitle
        mainArabic: mainArabic.trim() || '',
        banglaTranslation: banglaTranslation.trim() || '',
        banglaPronunciation: banglaPronunciation.trim() || '',
        categoryId: categoryId || categories[0]?.id || 'cat_namaz',
        mainCategory: catObj?.name_bn || catObj?.name_en || mainCategory.trim() || 'নামাজ ও দোয়ার নিয়ম',
        subcategoryId: subcategoryId || (subObj?.id || ''),
        subcategoryNameBn: finalSubNameBn,
        subcategoryNameEn: finalSubNameEn,
        subcategoryNameAr: finalSubNameAr,
        subCategory: finalSubNameBn || subCategory.trim() || '',
        author: author.trim() || '',
        hadithReference: hadithReference.trim() || '',
        body_bn: banglaTranslation.trim() || subtitleBn.trim() || titleBn.trim(),
        body_en: banglaTranslation.trim() || autoEnSubtitle,
        imageUrl: finalImageUrl || '',
        status: status || 'published',
        createdAt: editingPost?.createdAt || Date.now(),
        updatedAt: Date.now()
      };

      // Save to the cloud first. Do not show a false success or close the form on failure.
      try {
        await set(ref(db, `posts/${postId}`), payload);

        setPosts(prev => {
          const exists = prev.some(p => p.id === postId);
          const updated = exists
            ? prev.map(p => p.id === postId ? payload : p)
            : [payload, ...prev];
          localStorage.setItem('noor_posts', JSON.stringify(updated));
          return updated;
        });

        setIsModalOpen(false);
        setSyncSuccessMsg(editingPost ? 'পোস্টটি সফলভাবে ক্লাউড ডাটাবেসে আপডেট হয়েছে!' : 'নতুন পোস্ট সফলভাবে Firebase ক্লাউড ডাটাবেসে সেভ ও পাবলিশ হয়েছে!');
        setTimeout(() => setSyncSuccessMsg(null), 5000);

        logAudit(
          editingPost ? 'POST_UPDATED' : 'POST_CREATED',
          `পোস্ট "${payload.title_bn}" সফলভাবে ${editingPost ? 'আপডেট' : 'তৈরি'} হয়েছে`
        ).catch(() => {});
      } catch (dbErr: any) {
        console.error('Database write error:', dbErr);
        if (dbErr.code === 'PERMISSION_DENIED' || dbErr.message?.includes('PERMISSION_DENIED')) {
          setErrorMessage('Firebase Rules অনুমতি দেয়নি (PERMISSION_DENIED)। পোস্টটি ক্লাউডে সেভ হয়নি। Firebase Console → Realtime Database → Rules-এ habitsk50@gmail.com-এর admin write rule যোগ করে Publish করুন।');
        } else {
          setErrorMessage('ডাটাবেসে সেভ করতে সমস্যা হয়েছে: ' + (dbErr.message || 'ত্রুটি'));
        }
      } finally {
        setSubmitting(false);
      }
    } catch (err: any) {
      console.error('Error saving post:', err);
      setErrorMessage('পোস্ট সেভ করতে ব্যর্থ: ' + (err.message || 'ত্রুটি'));
      setSubmitting(false);
    }
  };

  const handleDelete = async (postId: string, postTitle: string) => {
    if (!window.confirm(`আপনি কি নিশ্চিত যে "${postTitle}" পোস্টটি মুছে ফেলতে চান?`)) return;

    try {
      await remove(ref(db, `posts/${postId}`));

      setPosts(prev => {
        const filtered = prev.filter(p => p.id !== postId);
        localStorage.setItem('noor_posts', JSON.stringify(filtered));
        return filtered;
      });

      await logAudit('POST_DELETED', `পোস্ট "${postTitle}" মুছে ফেলা হয়েছে`);
      setSyncSuccessMsg(`"${postTitle}" পোস্টটি মুছে ফেলা হয়েছে`);
      setTimeout(() => setSyncSuccessMsg(null), 4000);
    } catch (err: any) {
      console.error('Error deleting post:', err);
      alert('পোস্ট মুছতে ব্যর্থ হয়েছে: ' + err.message);
    }
  };

  const handleToggleStatus = async (post: Post) => {
    const newStatus: PostStatus = post.status === 'published' ? 'draft' : 'published';
    try {
      await set(ref(db, `posts/${post.id}/status`), newStatus);
      await set(ref(db, `posts/${post.id}/updatedAt`), Date.now());

      setPosts(prev => {
        const updated = prev.map(p => p.id === post.id ? { ...p, status: newStatus } : p);
        localStorage.setItem('noor_posts', JSON.stringify(updated));
        return updated;
      });
    } catch (err: any) {
      console.error('Error updating status:', err);
      alert('স্ট্যাটাস আপডেট ব্যর্থ: ' + err.message);
    }
  };

  const filteredPosts = posts.filter(post => {
    const q = searchQuery.toLowerCase();
    const matchesSearch = 
      (post.title_bn && post.title_bn.toLowerCase().includes(q)) ||
      (post.subtitle_bn && post.subtitle_bn.toLowerCase().includes(q)) ||
      (post.mainArabic && post.mainArabic.includes(searchQuery)) ||
      (post.banglaTranslation && post.banglaTranslation.toLowerCase().includes(q)) ||
      (post.banglaPronunciation && post.banglaPronunciation.toLowerCase().includes(q)) ||
      (post.author && post.author.toLowerCase().includes(q)) ||
      (post.hadithReference && post.hadithReference.toLowerCase().includes(q));

    const matchesStatus = statusFilter === 'all' || post.status === statusFilter;
    const matchesCategory = categoryFilter === 'all' || post.categoryId === categoryFilter || post.mainCategory === categoryFilter;
    const matchesSubcategory = subcategoryFilter === 'all' || post.subcategoryId === subcategoryFilter || post.subCategory === subcategoryFilter;

    return matchesSearch && matchesStatus && matchesCategory && matchesSubcategory;
  });

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      {/* Toast Notification */}
      {syncSuccessMsg && (
        <div className="p-4 bg-emerald-500/20 border border-emerald-500/50 rounded-2xl flex items-center gap-3 text-emerald-200 text-sm shadow-xl">
          <Sparkles className="w-5 h-5 text-emerald-400 shrink-0" />
          <p className="flex-1 font-medium">{syncSuccessMsg}</p>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 bg-slate-900 border border-slate-800 rounded-2xl">
        <div>
          <div className="flex items-center gap-2.5 mb-1.5">
            <BookOpen className="w-6 h-6 text-emerald-400" />
            <h1 className="text-xl sm:text-2xl font-bold text-white">ইসলামিক পোস্ট ও কন্টেন্ট ম্যানেজমেন্ট</h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-400">
            মোট {posts.length}টি ইসলামিক পোস্ট অ্যাপ ও ক্লাউড ডাটাবেসে সংরক্ষিত আছে
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={() => syncAppDefaultPosts(true)}
            disabled={syncing}
            className="flex items-center gap-2 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-emerald-300 rounded-xl text-sm font-medium border border-slate-700 transition disabled:opacity-50"
            title="Force sync default app posts to cloud"
          >
            <RefreshCw className={`w-4 h-4 ${syncing ? 'animate-spin' : ''}`} />
            <span>{syncing ? 'সিঙ্ক হচ্ছে...' : 'সব পোস্ট ক্লাউডে সিঙ্ক করুন'}</span>
          </button>
          <button
            onClick={openCreateModal}
            className="flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition shadow-lg shadow-emerald-900/30"
          >
            <FilePlus className="w-4 h-4" />
            <span>+ নতুন পোস্ট তৈরি করুন</span>
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="p-4 bg-slate-900/60 border border-slate-800/80 rounded-2xl flex flex-col md:flex-row items-stretch md:items-center gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="পোস্টের শিরোনাম, আরবি আয়াত, বাংলা অনুবাদ, উচ্চারণ, হাদিস রেফারেন্স দিয়ে খুঁজুন..."
            className="w-full pl-10 pr-4 py-2 bg-slate-950/80 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as any)}
            className="px-3.5 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="all">সকল অবস্থা ({posts.length})</option>
            <option value="published">প্রকাশিত / লাইভ ({posts.filter(p => p.status === 'published').length})</option>
            <option value="draft">খসড়া / ড্রাফট ({posts.filter(p => p.status === 'draft').length})</option>
          </select>

          <select
            value={categoryFilter}
            onChange={(e) => {
              setCategoryFilter(e.target.value);
              setSubcategoryFilter('all');
            }}
            className="px-3.5 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="all">সকল বিভাগ (ক্যাটাগরি)</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name_bn || c.name_en}</option>
            ))}
          </select>

          {categoryFilter !== 'all' && (
            <select
              value={subcategoryFilter}
              onChange={(e) => setSubcategoryFilter(e.target.value)}
              className="px-3.5 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-300 focus:outline-none focus:border-emerald-500"
            >
              <option value="all">সকল উপ-বিভাগ</option>
              {subcategories.filter(s => s.categoryId === categoryFilter).map((s) => (
                <option key={s.id} value={s.id}>{s.name_bn || s.name_en}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {/* Posts List */}
      {filteredPosts.length === 0 ? (
        <div className="p-12 text-center bg-slate-900/40 border border-slate-800/60 rounded-2xl">
          <FileText className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <p className="text-base font-semibold text-slate-300">কোনো পোস্ট পাওয়া যায়নি</p>
          <p className="text-xs text-slate-500 mt-1 mb-4">নতুন পোস্ট তৈরি করতে "+ নতুন পোস্ট তৈরি করুন" বাটনে ক্লিক করুন।</p>
          <button
            onClick={() => syncAppDefaultPosts(true)}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold rounded-xl inline-flex items-center gap-2"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>অ্যাপের ডিফল্ট পোস্ট লোড ও সিঙ্ক করুন</span>
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredPosts.map((post) => {
            const cat = categories.find(c => c.id === post.categoryId);
            const sub = subcategories.find(s => s.id === post.subcategoryId);
            const subName = sub?.name_bn || post.subcategoryNameBn || post.subCategory;
            return (
              <div 
                key={post.id} 
                className="p-5 bg-slate-900 border border-slate-800/80 hover:border-slate-700 rounded-2xl transition shadow-sm space-y-3"
              >
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
                  <div className="space-y-1.5 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold flex items-center gap-1 ${
                        post.status === 'published' 
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' 
                          : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                      }`}>
                        {post.status === 'published' ? <CheckCircle className="w-3 h-3" /> : <Clock className="w-3 h-3" />}
                        <span>{post.status === 'published' ? 'প্রকাশিত' : 'খসড়া'}</span>
                      </span>

                      <span className="px-2.5 py-0.5 bg-slate-800 text-slate-300 rounded-full text-xs font-medium border border-slate-700">
                        বিভাগ: {cat?.name_bn || cat?.name_en || post.mainCategory || 'ইসলামিক'}
                      </span>

                      {subName && (
                        <span className="px-2.5 py-0.5 bg-teal-950/60 text-teal-300 rounded-full text-xs font-medium border border-teal-800/40 flex items-center gap-1">
                          <Layers className="w-3 h-3 text-teal-400" />
                          <span>উপ-বিভাগ: {subName}</span>
                        </span>
                      )}

                      {post.hadithReference && (
                        <span className="px-2.5 py-0.5 bg-emerald-950/60 text-emerald-300 rounded-full text-xs font-medium border border-emerald-800/40">
                          {post.hadithReference}
                        </span>
                      )}

                      {post.author && (
                        <span className="text-xs text-slate-400 font-medium">
                          • {post.author}
                        </span>
                      )}
                    </div>

                    <h3 className="text-base sm:text-lg font-bold text-white">
                      {post.title_bn || post.title_en}
                    </h3>

                    {post.subtitle_bn && (
                      <p className="text-xs text-slate-400 font-medium line-clamp-1">
                        {post.subtitle_bn}
                      </p>
                    )}
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2 shrink-0">
                    <button
                      onClick={() => handleToggleStatus(post)}
                      className={`px-3 py-1.5 rounded-xl text-xs font-semibold border transition ${
                        post.status === 'published'
                          ? 'bg-slate-800 hover:bg-slate-700 text-slate-300 border-slate-700'
                          : 'bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-300 border-emerald-500/30'
                      }`}
                    >
                      {post.status === 'published' ? 'খসড়া করুন' : 'প্রকাশ করুন'}
                    </button>
                    <button
                      onClick={() => openEditModal(post)}
                      className="p-2 bg-slate-800 hover:bg-slate-700 text-emerald-400 rounded-xl transition border border-slate-700"
                      title="সম্পাদনা করুন"
                    >
                      <Edit3 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(post.id, post.title_bn || post.title_en)}
                      className="p-2 bg-slate-800 hover:bg-rose-500/20 text-rose-400 rounded-xl transition border border-slate-700 hover:border-rose-500/30"
                      title="মুছে ফেলুন"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                {/* Arabic, Pronunciation & Translation Box */}
                {(post.mainArabic || post.banglaPronunciation || post.banglaTranslation) && (
                  <div className="p-4 bg-slate-950/70 border border-slate-800/80 rounded-xl space-y-2.5">
                    {post.mainArabic && (
                      <p className="text-lg text-emerald-300 font-arabic text-right leading-loose" dir="rtl">
                        {post.mainArabic}
                      </p>
                    )}
                    {post.banglaPronunciation && (
                      <p className="text-xs text-amber-300/90 font-medium italic">
                        উচ্চারণ: {post.banglaPronunciation}
                      </p>
                    )}
                    {post.banglaTranslation && (
                      <div className="pt-2 border-t border-slate-800/60">
                        <p className="text-xs text-slate-300 leading-relaxed whitespace-pre-line">
                          {post.banglaTranslation}
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* 100% Bangla Post Creation & Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm overflow-y-auto">
          <div className="w-full max-w-3xl my-8 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/80 sticky top-0 z-10">
              <h2 className="text-lg font-bold text-white flex items-center gap-2">
                <Quote className="w-5 h-5 text-emerald-400" />
                <span>{editingPost ? 'ইসলামিক পোস্ট সম্পাদনা করুন' : 'নতুন ইসলামিক পোস্ট তৈরি করুন'}</span>
              </h2>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-6 space-y-5 overflow-y-auto flex-1">
              {errorMessage && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-xl flex items-center gap-2 text-rose-300 text-xs">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{errorMessage}</span>
                </div>
              )}

              {/* 1. Bangla Title */}
              <div>
                <label className="block text-xs font-semibold text-slate-200 mb-1.5 flex items-center justify-between">
                  <span>পোস্টের শিরোনাম (বাংলায় লিখুন) <span className="text-emerald-400">*</span></span>
                  <span className="text-[11px] text-emerald-400 font-normal">ইংরেজি স্বয়ংক্রিয়ভাবে তৈরি হবে</span>
                </label>
                <input
                  type="text"
                  value={titleBn}
                  onChange={(e) => setTitleBn(e.target.value)}
                  placeholder="যেমন: আজকের আয়াত: সূরা আল-বাকারা (২:২৮৬)"
                  required
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              {/* 2. Bangla Subtitle */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  উপ-শিরোনাম বা সারসংক্ষেপ (ঐচ্ছিক)
                </label>
                <input
                  type="text"
                  value={subtitleBn}
                  onChange={(e) => setSubtitleBn(e.target.value)}
                  placeholder="যেমন: আল্লাহ কোনো ব্যক্তির উপর সাধ্যাতীত বোঝা চাপান না।"
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              {/* 3. Main Arabic Line */}
              <div>
                <label className="block text-xs font-semibold text-emerald-400 mb-1.5 flex items-center justify-between">
                  <span>মূল আরবি আয়াত বা হাদিস (ঐচ্ছিক)</span>
                  <span className="text-[11px] text-slate-400">আরবি হরফে লিখুন</span>
                </label>
                <textarea
                  value={mainArabic}
                  onChange={(e) => setMainArabic(e.target.value)}
                  dir="rtl"
                  rows={2}
                  placeholder="لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ"
                  className="w-full px-4 py-2.5 bg-slate-950 border border-emerald-900/50 rounded-xl text-lg text-emerald-200 focus:outline-none focus:border-emerald-500 leading-relaxed"
                />
              </div>

              {/* 4. Bangla Pronunciation */}
              <div>
                <label className="block text-xs font-semibold text-amber-400 mb-1.5">
                  আরবি শব্দের সহজ বাংলা উচ্চারণ (ঐচ্ছিক)
                </label>
                <input
                  type="text"
                  value={banglaPronunciation}
                  onChange={(e) => setBanglaPronunciation(e.target.value)}
                  placeholder="যেমন: লা ইউকাল্লিফুল্লাহু নাফসান ইল্লা উস'আহা, লাহা মা কাসাবাত..."
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-amber-100 focus:outline-none focus:border-emerald-500"
                />
              </div>

              {/* 5. Bangla Translation & Full Details */}
              <div>
                <label className="block text-xs font-semibold text-slate-200 mb-1.5">
                  বাংলা অনুবাদ ও বিস্তারিত ব্যাখ্যা/তাফসির <span className="text-emerald-400">*</span>
                </label>
                <textarea
                  value={banglaTranslation}
                  onChange={(e) => setBanglaTranslation(e.target.value)}
                  rows={4}
                  placeholder="বাংলা অনুবাদ ও ফজিলত বা তাফসির বিস্তারিত লিখুন..."
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-200 focus:outline-none focus:border-emerald-500 leading-relaxed"
                />
              </div>

              {/* 6. Categories & Subcategories */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    মূল বিভাগ (Main Category) <span className="text-emerald-400">*</span>
                  </label>
                  <select
                    value={categoryId}
                    onChange={(e) => {
                      const newCatId = e.target.value;
                      setCategoryId(newCatId);
                      const cat = categories.find(c => c.id === newCatId);
                      if (cat) setMainCategory(cat.name_bn || cat.name_en);
                      const matchingSubs = subcategories.filter(s => s.categoryId === newCatId && s.isActive !== false);
                      if (matchingSubs.length > 0) {
                        setSubcategoryId(matchingSubs[0].id);
                        setSubCategory(matchingSubs[0].name_bn || matchingSubs[0].name_en);
                      } else {
                        setSubcategoryId('');
                        setSubCategory('');
                      }
                    }}
                    className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name_bn || c.name_en}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <label className="block text-xs font-semibold text-slate-300">
                      উপ-বিভাগ (Subcategory) <span className="text-emerald-400">*</span>
                    </label>
                    <button
                      type="button"
                      onClick={() => setIsCustomSubCategory(!isCustomSubCategory)}
                      className="text-[11px] text-emerald-400 hover:text-emerald-300 underline font-normal transition"
                    >
                      {isCustomSubCategory ? 'তালিকা থেকে পছন্দ করুন' : '+ নতুন লিখুন'}
                    </button>
                  </div>
                  {isCustomSubCategory ? (
                    <input
                      type="text"
                      value={subCategory}
                      onChange={(e) => setSubCategory(e.target.value)}
                      placeholder="যেমন: ফজরের নামাজ বা রোজার বিধান"
                      className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                    />
                  ) : (
                    <select
                      value={subcategoryId}
                      onChange={(e) => {
                        if (e.target.value === '__custom__') {
                          setIsCustomSubCategory(true);
                          setSubcategoryId('');
                          setSubCategory('');
                        } else {
                          const selectedId = e.target.value;
                          setSubcategoryId(selectedId);
                          const subObj = subcategories.find(s => s.id === selectedId);
                          if (subObj) {
                            setSubCategory(subObj.name_bn || subObj.name_en);
                          }
                        }
                      }}
                      className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500 cursor-pointer"
                    >
                      <option value="">-- উপ-বিভাগ নির্বাচন করুন --</option>
                      {activeSubcategories.map((sub) => (
                        <option key={sub.id} value={sub.id}>
                          {sub.name_bn || sub.name_en} {sub.name_ar ? `(${sub.name_ar})` : ''}
                        </option>
                      ))}
                      <option value="__custom__">+ অন্য নতুন উপ-বিভাগ লিখুন...</option>
                    </select>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    লেখক / বর্ণনাকারী (ঐচ্ছিক)
                  </label>
                  <input
                    type="text"
                    value={author}
                    onChange={(e) => setAuthor(e.target.value)}
                    placeholder="যেমন: আবু হুরায়রা (রা.) / ইমাম বুখারী"
                    className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    হাদিস বা আয়াতের রেফারেন্স (ঐচ্ছিক)
                  </label>
                  <input
                    type="text"
                    value={hadithReference}
                    onChange={(e) => setHadithReference(e.target.value)}
                    placeholder="যেমন: সহীহ বুখারী: ১৮৫২ / সূরা বাকারা: ২৫৫"
                    className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    প্রকাশের অবস্থা (Status)
                  </label>
                  <select
                    value={status}
                    onChange={(e) => setStatus(e.target.value as PostStatus)}
                    className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                  >
                    <option value="published">প্রকাশিত (সরাসরি অ্যাপে দৃশ্যমান হবে)</option>
                    <option value="draft">খসড়া (অ্যাপে অপ্রকাশিত থাকবে)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    ছবি আপলোড (ঐচ্ছিক)
                  </label>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => setSelectedFile(e.target.files ? e.target.files[0] : null)}
                    className="block w-full text-xs text-slate-400 file:mr-3 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-emerald-600/20 file:text-emerald-400 hover:file:bg-emerald-600/30 cursor-pointer"
                  />
                  {uploadProgress !== null && (
                    <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden mt-1.5">
                      <div className="bg-emerald-500 h-full transition-all" style={{ width: `${uploadProgress}%` }}></div>
                    </div>
                  )}
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-3 sticky bottom-0 bg-slate-900 pb-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-semibold transition"
                >
                  বাতিল করুন
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition shadow-lg shadow-emerald-900/30 disabled:opacity-50"
                >
                  {submitting ? 'সংরক্ষণ হচ্ছে...' : editingPost ? 'পোস্ট আপডেট করুন' : 'পোস্ট সংরক্ষণ ও প্রকাশ করুন'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default PostsManager;
