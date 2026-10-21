import React, { useState, useEffect } from 'react';
import { 
  ref, 
  set, 
  remove, 
  onValue 
} from 'firebase/database';
import { db } from '../api/firebase';
import { QuizQuestion, PostStatus } from '../types/schema';
import { useAuth } from '../context/AuthContext';
import { 
  HelpCircle, PlusCircle, Edit3, Trash2, Search, CheckCircle, Clock, X, AlertCircle, RefreshCw, CheckCircle2, Award, Sparkles
} from 'lucide-react';
import { defaultQuizzes } from '../data/seedData';
import { getInitialQuizzes, logAudit } from '../api/firestoreService';

export const QuizManager: React.FC = () => {
  const { currentUser } = useAuth();
  const [quizzes, setQuizzes] = useState<QuizQuestion[]>(getInitialQuizzes());
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [syncSuccessMsg, setSyncSuccessMsg] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'published' | 'draft'>('all');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingQuiz, setEditingQuiz] = useState<QuizQuestion | null>(null);

  // Form states: 1 question + 4 options + 1 correct answer (akta shothik)
  const [questionBn, setQuestionBn] = useState('');
  const [questionEn, setQuestionEn] = useState('');
  const [optionsBn, setOptionsBn] = useState<string[]>(['', '', '', '']);
  const [optionsEn, setOptionsEn] = useState<string[]>(['', '', '', '']);
  const [correctOptionIndex, setCorrectOptionIndex] = useState<number>(0);
  const [explanationBn, setExplanationBn] = useState('');
  const [explanationEn, setExplanationEn] = useState('');
  const [hadithReference, setHadithReference] = useState('');
  const [category, setCategory] = useState('Quran');
  const [status, setStatus] = useState<PostStatus>('published');
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const qRef = ref(db, 'quizzes');
    const unsub = onValue(qRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const list: QuizQuestion[] = Object.values(data);
        list.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
        setQuizzes(list);
        localStorage.setItem('noor_quizzes', JSON.stringify(list));
      }
    }, (err) => {
      console.warn('RTDB quizzes listener note:', err);
    });

    return () => unsub();
  }, []);

  const syncDefaultQuizzes = async (manual = true) => {
    try {
      if (manual) setSyncing(true);
      const qMap: Record<string, QuizQuestion> = {};
      const quizzesToSeed = quizzes.length > 0 ? quizzes : defaultQuizzes;
      for (const q of quizzesToSeed) {
        const quizId = q.id || `quiz_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        qMap[quizId] = { ...q, id: quizId, updatedAt: Date.now() };
      }
      await set(ref(db, 'quizzes'), qMap);
      setQuizzes(Object.values(qMap));
      localStorage.setItem('noor_quizzes', JSON.stringify(Object.values(qMap)));

      if (manual) {
        setSyncSuccessMsg('সকল ইসলামিক কুইজ ক্লাউড ফায়ারবেসে সফলভাবে সিঙ্ক হয়েছে!');
        setTimeout(() => setSyncSuccessMsg(null), 4000);
      }
    } catch (err: any) {
      console.error('Failed to sync quizzes:', err);
      if (manual) setErrorMessage('Failed to sync: ' + err.message);
    } finally {
      if (manual) setSyncing(false);
    }
  };

  const openCreateModal = () => {
    setEditingQuiz(null);
    setQuestionBn('');
    setQuestionEn('');
    setOptionsBn(['', '', '', '']);
    setOptionsEn(['', '', '', '']);
    setCorrectOptionIndex(0);
    setExplanationBn('');
    setExplanationEn('');
    setHadithReference('');
    setCategory('Quran');
    setStatus('published');
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const openEditModal = (q: QuizQuestion) => {
    setEditingQuiz(q);
    setQuestionBn(q.question_bn || '');
    setQuestionEn(q.question_en || '');
    setOptionsBn(q.options_bn && q.options_bn.length === 4 ? [...q.options_bn] : ['', '', '', '']);
    setOptionsEn(q.options_en && q.options_en.length === 4 ? [...q.options_en] : ['', '', '', '']);
    setCorrectOptionIndex(q.correctOptionIndex || 0);
    setExplanationBn(q.explanation_bn || '');
    setExplanationEn(q.explanation_en || '');
    setHadithReference(q.hadithReference || '');
    setCategory(q.category || 'Quran');
    setStatus(q.status);
    setErrorMessage(null);
    setIsModalOpen(true);
  };

  const handleOptionBnChange = (index: number, val: string) => {
    const copy = [...optionsBn];
    copy[index] = val;
    setOptionsBn(copy);
  };

  const handleOptionEnChange = (index: number, val: string) => {
    const copy = [...optionsEn];
    copy[index] = val;
    setOptionsEn(copy);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!questionBn.trim() && !questionEn.trim()) {
      setErrorMessage('অনুগ্রহ করে কুইজের প্রশ্ন লিখুন।');
      return;
    }

    const hasAllOptions = optionsBn.every(opt => opt.trim().length > 0) || optionsEn.every(opt => opt.trim().length > 0);
    if (!hasAllOptions) {
      setErrorMessage('অনুগ্রহ করে ৪টি বিকল্প অপশনই পূরণ করুন (A, B, C, D)।');
      return;
    }

    setSubmitting(true);
    try {
      const quizId = editingQuiz?.id || `quiz_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
      const payload: QuizQuestion = {
        id: quizId,
        question_bn: questionBn.trim() || questionEn.trim(),
        question_en: questionEn.trim() || questionBn.trim(),
        options_bn: optionsBn.map((opt, i) => opt.trim() || optionsEn[i].trim()),
        options_en: optionsEn.map((opt, i) => opt.trim() || optionsBn[i].trim()),
        correctOptionIndex: correctOptionIndex,
        explanation_bn: explanationBn.trim(),
        explanation_en: explanationEn.trim(),
        hadithReference: hadithReference.trim(),
        category: category,
        status: status,
        createdAt: editingQuiz?.createdAt || Date.now(),
        updatedAt: Date.now()
      };

      // 1. Instantly update local state & localStorage
      setQuizzes(prev => {
        const exists = prev.some(q => q.id === quizId);
        const updated = exists 
          ? prev.map(q => q.id === quizId ? payload : q)
          : [payload, ...prev];
        localStorage.setItem('noor_quizzes', JSON.stringify(updated));
        return updated;
      });

      setIsModalOpen(false);
      setSubmitting(false);
      setSyncSuccessMsg(editingQuiz ? 'কুইজটি সফলভাবে আপডেট হয়েছে!' : 'নতুন কুইজ তৈরি হয়েছে!');
      setTimeout(() => setSyncSuccessMsg(null), 3000);

      // 2. Sync to Realtime Database in background
      set(ref(db, `quizzes/${quizId}`), payload).catch(err => {
        console.warn('Quiz sync note:', err);
      });

      logAudit(
        editingQuiz ? 'QUIZ_UPDATED' : 'QUIZ_CREATED',
        `Quiz question "${payload.question_bn || payload.question_en}" saved`
      ).catch(() => {});
    } catch (err: any) {
      console.error('Error saving quiz:', err);
      setErrorMessage('Failed to save quiz: ' + (err.message || 'Error'));
      setSubmitting(false);
    }
  };

  const handleDelete = async (quizId: string, qText: string) => {
    if (!window.confirm(`Are you sure you want to delete this quiz question?`)) return;

    try {
      await remove(ref(db, `quizzes/${quizId}`));

      setQuizzes(prev => {
        const filtered = prev.filter(q => q.id !== quizId);
        localStorage.setItem('noor_quizzes', JSON.stringify(filtered));
        return filtered;
      });

      await logAudit('QUIZ_DELETED', `Quiz question "${qText}" deleted`);
    } catch (err: any) {
      console.error('Failed to delete quiz:', err);
      alert('Delete failed: ' + err.message);
    }
  };

  const filteredQuizzes = quizzes.filter(q => {
    const matchesSearch = 
      (q.question_bn && q.question_bn.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (q.question_en && q.question_en.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (q.category && q.category.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesStatus = statusFilter === 'all' || q.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const optionLabels = ['A', 'B', 'C', 'D'];

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
            <HelpCircle className="w-5 h-5 text-emerald-400" />
            <h1 className="text-xl sm:text-2xl font-bold text-white">Islamic Quiz Management</h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-400">
            Create interactive Islamic quizzes: 1 Question + 4 Options with 1 correct answer
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={() => syncDefaultQuizzes(true)}
            disabled={syncing}
            className="flex items-center gap-2 px-3.5 py-2.5 bg-slate-800 hover:bg-slate-700 text-emerald-300 rounded-xl text-sm font-medium border border-slate-700 transition disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${syncing ? 'animate-spin' : ''}`} />
            <span>{syncing ? 'সিঙ্ক হচ্ছে...' : 'সিঙ্ক করুন (Sync Quizzes)'}</span>
          </button>
          <button
            onClick={openCreateModal}
            className="flex items-center gap-2 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-sm font-semibold transition shadow-lg shadow-emerald-900/30"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Create New Quiz</span>
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
            placeholder="Search quizzes by question or topic..."
            className="w-full pl-10 pr-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as any)}
          className="px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-300 focus:outline-none focus:border-emerald-500"
        >
          <option value="all">All ({quizzes.length})</option>
          <option value="published">Published</option>
          <option value="draft">Draft</option>
        </select>
      </div>

      {/* Quiz List */}
      <div className="space-y-4">
        {filteredQuizzes.map((q, idx) => (
          <div key={q.id} className="p-5 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="w-6 h-6 rounded-full bg-emerald-500/20 text-emerald-400 font-bold text-xs flex items-center justify-center border border-emerald-500/30">
                    {idx + 1}
                  </span>
                  <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                    {q.category}
                  </span>
                  {q.hadithReference && (
                    <span className="text-xs text-emerald-400/90 bg-emerald-950/40 px-2 py-0.5 rounded border border-emerald-800/30">
                      {q.hadithReference}
                    </span>
                  )}
                </div>
                <h3 className="text-base font-bold text-white pt-1">
                  {q.question_bn || q.question_en}
                </h3>
                {q.question_en && q.question_bn && (
                  <p className="text-xs text-slate-400">{q.question_en}</p>
                )}
              </div>

              <div className="flex items-center gap-2 self-end sm:self-auto">
                <button
                  onClick={() => openEditModal(q)}
                  className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition"
                >
                  <Edit3 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => handleDelete(q.id!, q.question_bn || q.question_en)}
                  className="p-2 text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 rounded-lg transition"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* 4 Options Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              {q.options_bn?.map((opt, i) => {
                const isCorrect = i === q.correctOptionIndex;
                return (
                  <div
                    key={i}
                    className={`p-3 rounded-xl border flex items-center justify-between text-xs transition ${
                      isCorrect
                        ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-200 font-medium'
                        : 'bg-slate-950/60 border-slate-800 text-slate-300'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <span className={`w-5 h-5 rounded-full flex items-center justify-center font-bold text-[10px] ${
                        isCorrect ? 'bg-emerald-500 text-slate-950' : 'bg-slate-800 text-slate-400'
                      }`}>
                        {optionLabels[i]}
                      </span>
                      <span>{opt}</span>
                    </div>
                    {isCorrect && (
                      <span className="text-[11px] font-bold text-emerald-400 flex items-center gap-1">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        <span>সঠিক উত্তর</span>
                      </span>
                    )}
                  </div>
                );
              })}
            </div>

            {/* Explanation */}
            {q.explanation_bn && (
              <div className="p-3 bg-slate-950/80 border border-slate-800/80 rounded-xl text-xs text-slate-400">
                <strong className="text-emerald-400">ব্যাখ্যা: </strong>
                <span>{q.explanation_bn}</span>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm overflow-y-auto">
          <div className="w-full max-w-2xl my-8 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/80 sticky top-0 z-10">
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <Award className="w-5 h-5 text-emerald-400" />
                <span>{editingQuiz ? 'Edit Islamic Quiz' : 'Create Islamic Quiz'}</span>
              </h2>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-6 space-y-4 overflow-y-auto flex-1">
              {errorMessage && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-300 text-xs">
                  {errorMessage}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  কুইজের প্রশ্ন (বাংলায়) *
                </label>
                <input
                  type="text"
                  value={questionBn}
                  onChange={(e) => setQuestionBn(e.target.value)}
                  placeholder="যেমন: পবিত্র কুরআনের কোন সূরাকে 'কুরআনের হৃৎপিণ্ড' বলা হয়?"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Question in English
                </label>
                <input
                  type="text"
                  value={questionEn}
                  onChange={(e) => setQuestionEn(e.target.value)}
                  placeholder="e.g. Which Surah is known as the Heart of the Quran?"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              {/* 4 Options */}
              <div className="space-y-3 pt-2 border-t border-slate-800">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-bold text-emerald-400">
                    ৪টি অপশন লিখুন এবং একটি সঠিক উত্তর সিলেক্ট করুন:
                  </label>
                  <span className="text-[11px] text-slate-400">১টি সঠিক উত্তর (Akta shothik)</span>
                </div>

                {optionLabels.map((label, idx) => (
                  <div key={idx} className="p-3 bg-slate-950/70 border border-slate-800 rounded-xl space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-slate-300">অপশন {label}</span>
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="radio"
                          name="correctOption"
                          checked={correctOptionIndex === idx}
                          onChange={() => setCorrectOptionIndex(idx)}
                          className="text-emerald-500 focus:ring-emerald-500"
                        />
                        <span className={`text-xs font-bold ${correctOptionIndex === idx ? 'text-emerald-400' : 'text-slate-500'}`}>
                          {correctOptionIndex === idx ? '✓ এটি সঠিক উত্তর' : 'সঠিক হিসেবে বাছুন'}
                        </span>
                      </label>
                    </div>

                    <input
                      type="text"
                      value={optionsBn[idx]}
                      onChange={(e) => handleOptionBnChange(idx, e.target.value)}
                      placeholder={`অপশন ${label} (বাংলায়)`}
                      className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-lg text-xs text-white focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                ))}
              </div>

              {/* Explanation & Hadith Reference */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2 border-t border-slate-800">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    সঠিক উত্তরের ব্যাখ্যা (Explanation)
                  </label>
                  <textarea
                    value={explanationBn}
                    onChange={(e) => setExplanationBn(e.target.value)}
                    rows={2}
                    placeholder="উত্তরের সংক্ষিপ্ত ব্যাখ্যা বা তাৎপর্য..."
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    হাদিস / কুরআনিক রেফারেন্স
                  </label>
                  <input
                    type="text"
                    value={hadithReference}
                    onChange={(e) => setHadithReference(e.target.value)}
                    placeholder="যেমন: সুনানে তিরমিযী: ২৮৮৭"
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Category</label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"
                  >
                    <option value="Quran">Quran (পবিত্র কুরআন)</option>
                    <option value="Hadith">Hadith (হাদিস)</option>
                    <option value="Salah">Salah (নামাজ)</option>
                    <option value="Seerah">Seerah (নবীচরিত)</option>
                    <option value="Ramadan">Ramadan (রোজা)</option>
                    <option value="History">Islamic History (ইতিহাস)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Status</label>
                  <select
                    value={status}
                    onChange={(e) => setStatus(e.target.value as PostStatus)}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"
                  >
                    <option value="published">Published (সরাসরি কুইজে আসবে)</option>
                    <option value="draft">Draft</option>
                  </select>
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-3 sticky bottom-0 bg-slate-900 pb-2">
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
                  {submitting ? 'Saving...' : 'Save Quiz Question'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
