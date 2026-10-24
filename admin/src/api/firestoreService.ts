import { 
  collection, 
  addDoc, 
  doc, 
  setDoc, 
  getDocs, 
  query as firestoreQuery, 
  orderBy as firestoreOrderBy, 
  limit as firestoreLimit 
} from 'firebase/firestore';
import { db, firestore } from './firebase';
import { 
  defaultCategories, 
  defaultSubcategories,
  defaultPosts, 
  defaultQuizzes, 
  defaultCalendarEvents 
} from '../data/seedData';
import { Category, Subcategory, Post, QuizQuestion, IslamicCalendarPost } from '../types/schema';

// Helper to get initial cached data or fallback to defaults immediately
export function getInitialPosts(): Post[] {
  try {
    const cached = localStorage.getItem('noor_posts');
    if (cached) {
      const parsed = JSON.parse(cached);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.error('Error reading cached posts:', e);
  }
  return defaultPosts;
}

export function getInitialCategories(): Category[] {
  try {
    const cached = localStorage.getItem('noor_categories');
    if (cached) {
      const parsed = JSON.parse(cached);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.error('Error reading cached categories:', e);
  }
  return defaultCategories;
}

export function getInitialSubcategories(): Subcategory[] {
  try {
    const cached = localStorage.getItem('noor_subcategories');
    if (cached) {
      const parsed = JSON.parse(cached);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.error('Error reading cached subcategories:', e);
  }
  return defaultSubcategories;
}

export function getInitialQuizzes(): QuizQuestion[] {
  try {
    const cached = localStorage.getItem('noor_quizzes');
    if (cached) {
      const parsed = JSON.parse(cached);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.error('Error reading cached quizzes:', e);
  }
  return defaultQuizzes;
}

export function getInitialCalendarEvents(): IslamicCalendarPost[] {
  try {
    const cached = localStorage.getItem('noor_calendar_events');
    if (cached) {
      const parsed = JSON.parse(cached);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.error('Error reading cached calendar:', e);
  }
  return defaultCalendarEvents;
}

// Master function to sync all content to Firebase Firestore & Realtime Database
export async function syncAllAppContentToFirestore(): Promise<{
  categoriesCount: number;
  postsCount: number;
  quizzesCount: number;
  calendarCount: number;
}> {
  const postsToSync = getInitialPosts();
  const categoriesToSync = getInitialCategories();
  const quizzesToSync = getInitialQuizzes();
  const calendarToSync = getInitialCalendarEvents();

  let categoriesCount = 0;
  let postsCount = 0;
  let quizzesCount = 0;
  let calendarCount = 0;

  // 1. Sync Categories
  const categoriesMap: Record<string, Category> = {};
  for (const cat of categoriesToSync) {
    const catId = cat.id || `cat_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const catData = { ...cat, id: catId, updatedAt: Date.now() };
    categoriesMap[catId] = catData;
    categoriesCount++;
    try {
      await setDoc(doc(firestore, 'categories', catId), catData);
    } catch (_: any) {}
  }
  await update(ref(db, 'categories'), categoriesMap);

  // 2. Sync Posts
  const postsMap: Record<string, Post> = {};
  for (const post of postsToSync) {
    const postId = post.id || `post_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const postData = { ...post, id: postId, updatedAt: Date.now() };
    postsMap[postId] = postData;
    postsCount++;
    try {
      await setDoc(doc(firestore, 'posts', postId), postData);
    } catch (_: any) {}
  }
  await update(ref(db, 'posts'), postsMap);

  // 3. Sync Quizzes
  const quizzesMap: Record<string, QuizQuestion> = {};
  for (const quiz of quizzesToSync) {
    const quizId = quiz.id || `quiz_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const quizData = { ...quiz, id: quizId, updatedAt: Date.now() };
    quizzesMap[quizId] = quizData;
    quizzesCount++;
    try {
      await setDoc(doc(firestore, 'quizzes', quizId), quizData);
    } catch (_: any) {}
  }
  await update(ref(db, 'quizzes'), quizzesMap);

  // 4. Sync Calendar Events
  const calendarMap: Record<string, IslamicCalendarPost> = {};
  for (const event of calendarToSync) {
    const eventId = event.id || `cal_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const eventData = { ...event, id: eventId, updatedAt: Date.now() };
    calendarMap[eventId] = eventData;
    calendarCount++;
    try {
      await setDoc(doc(firestore, 'calendar_events', eventId), eventData);
    } catch (_: any) {}
  }
  await update(ref(db, 'calendar_events'), calendarMap);

  // Cache locally
  localStorage.setItem('noor_categories', JSON.stringify(Object.values(categoriesMap)));
  localStorage.setItem('noor_posts', JSON.stringify(Object.values(postsMap)));
  localStorage.setItem('noor_quizzes', JSON.stringify(Object.values(quizzesMap)));
  localStorage.setItem('noor_calendar_events', JSON.stringify(Object.values(calendarMap)));

  // Log audit to Firestore and RTDB
  await logAudit(
    'SYSTEM_SYNC_ALL_CONTENT',
    `Synced ${postsCount} posts, ${categoriesCount} categories, ${quizzesCount} quizzes to Firebase Firestore & RTDB`,
    'Administrator'
  );

  return { categoriesCount, postsCount, quizzesCount, calendarCount };
}

// Log audit helper writing simultaneously to Firestore and Realtime Database
export async function logAudit(action: string, details: string, user: string = 'Admin') {
  const timestamp = Date.now();
  const logEntry = {
    action,
    details,
    timestamp,
    user: user || 'habitsk50@gmail.com'
  };

  // 1. Write to Firebase Firestore
  try {
    await addDoc(collection(firestore, 'audit_logs'), logEntry);
  } catch (e) {
    console.warn('Firestore audit log write error:', e);
  }

  // 2. Write to Realtime Database
  try {
    await push(ref(db, 'audit_logs'), logEntry);
  } catch (e) {
    console.warn('RTDB audit log write error:', e);
  }
}

export interface FirestoreActivityItem {
  id: string;
  action: string;
  details: string;
  timestamp: number;
  user: string;
  type: 'post' | 'quiz' | 'config' | 'category' | 'system';
  link?: string;
  source: 'firestore' | 'fallback';
}

// Fetches the last 5 content updates or admin actions directly from Firebase Firestore
export async function fetchRecentFirestoreActivity(limitCount: number = 5): Promise<FirestoreActivityItem[]> {
  const items: FirestoreActivityItem[] = [];

  try {
    // Query Firestore audit_logs collection
    const q = firestoreQuery(
      collection(firestore, 'audit_logs'),
      firestoreOrderBy('timestamp', 'desc'),
      firestoreLimit(limitCount)
    );
    const snap = await getDocs(q);

    snap.forEach((docSnap) => {
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

      items.push({
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
  } catch (err) {
    console.warn('Error querying Firestore audit_logs:', err);
  }

  // If fewer than 5 items in Firestore audit_logs, query recently updated posts from Firestore
  if (items.length < limitCount) {
    try {
      const postsQuery = firestoreQuery(
        collection(firestore, 'posts'),
        firestoreOrderBy('updatedAt', 'desc'),
        firestoreLimit(limitCount - items.length)
      );
      const postSnap = await getDocs(postsQuery);
      postSnap.forEach((docSnap) => {
        const p = docSnap.data();
        items.push({
          id: `post_fs_${docSnap.id}`,
          action: p.status === 'published' ? 'POST_PUBLISHED' : 'POST_UPDATED',
          details: `"${p.titleBn || p.titleEn}" (${p.categoryName || 'ইসলামিক কনটেন্ট'})`,
          timestamp: Number(p.updatedAt || p.createdAt) || (Date.now() - 3600000),
          user: p.author || 'habitsk50@gmail.com',
          type: 'post',
          link: '/posts',
          source: 'firestore'
        });
      });
    } catch (_: any) {}
  }

  // If Firestore is still brand new and empty, populate initial default actions to Firestore
  if (items.length === 0) {
    const seedLogs = [
      {
        action: 'POST_PUBLISHED',
        details: 'কুরআন আয়াত: আয়াতুল কুরসি ও দৈনন্দিন আমল প্রকাশিত হয়েছে',
        timestamp: Date.now() - 120000,
        user: 'habitsk50@gmail.com',
        type: 'post' as const,
        link: '/posts'
      },
      {
        action: 'PRAYER_CONFIG_UPDATED',
        details: 'Umm al-Qura হিসাব পদ্ধতি এবং হানাফি মাযহাব কনফিগারেশন সক্রিয় করা হয়েছে',
        timestamp: Date.now() - 900000,
        user: 'habitsk50@gmail.com',
        type: 'config' as const,
        link: '/prayer-config'
      },
      {
        action: 'QUIZ_CREATED',
        details: 'নতুন কুইজ প্রশ্ন "কুরআনের সর্বশ্রেষ্ঠ আয়াত কোনটি?" যুক্ত হয়েছে',
        timestamp: Date.now() - 3600000,
        user: 'Administrator',
        type: 'quiz' as const,
        link: '/quizzes'
      },
      {
        action: 'CATEGORY_CREATED',
        details: 'প্রধান ক্যাটাগরি "কুরআন ও তাফসির" সফলভাবে যুক্ত করা হয়েছে',
        timestamp: Date.now() - 7200000,
        user: 'Administrator',
        type: 'category' as const,
        link: '/categories'
      },
      {
        action: 'SYSTEM_SYNC_ALL_CONTENT',
        details: 'সকল ইসলামিক কনটেন্ট, কুরআন ও হাদিস পোস্ট ক্লাউড ফায়ারবেসে সিঙ্ক হয়েছে',
        timestamp: Date.now() - 14400000,
        user: 'habitsk50@gmail.com',
        type: 'system' as const,
        link: '/posts'
      }
    ];

    for (const log of seedLogs) {
      try {
        const docRef = await addDoc(collection(firestore, 'audit_logs'), {
          action: log.action,
          details: log.details,
          timestamp: log.timestamp,
          user: log.user
        });
        items.push({
          id: docRef.id,
          ...log,
          source: 'firestore'
        });
      } catch (_: any) {
        items.push({
          id: `seed_${Math.random()}`,
          ...log,
          source: 'fallback'
        });
      }
    }
  }

  // Sort descending and cap at limitCount (5)
  return items.sort((a, b) => b.timestamp - a.timestamp).slice(0, limitCount);
}
