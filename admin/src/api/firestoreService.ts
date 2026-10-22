import { 
  ref, 
  get, 
  push, 
  remove, 
  onValue,
  update 
} from 'firebase/database';
import { db } from './firebase';
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

// Master function to sync all content to Firebase Realtime Database
export async function syncAllAppContentToFirestore(): Promise<{
  categoriesCount: number;
  postsCount: number;
  quizzesCount: number;
  calendarCount: number;
}> {
  // Use posts from localStorage if present (to retain any posts created by the user), otherwise defaultPosts
  const postsToSync = getInitialPosts();
  const categoriesToSync = getInitialCategories();
  const quizzesToSync = getInitialQuizzes();
  const calendarToSync = getInitialCalendarEvents();

  let categoriesCount = 0;
  let postsCount = 0;
  let quizzesCount = 0;
  let calendarCount = 0;

  // 1. Sync Categories to /categories
  const categoriesMap: Record<string, Category> = {};
  for (const cat of categoriesToSync) {
    const catId = cat.id || `cat_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    categoriesMap[catId] = { ...cat, id: catId, updatedAt: Date.now() };
    categoriesCount++;
  }
  await update(ref(db, 'categories'), categoriesMap);

  // 2. Sync Posts to /posts
  const postsMap: Record<string, Post> = {};
  for (const post of postsToSync) {
    const postId = post.id || `post_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    postsMap[postId] = { ...post, id: postId, updatedAt: Date.now() };
    postsCount++;
  }
  await update(ref(db, 'posts'), postsMap);

  // 3. Sync Quizzes to /quizzes
  const quizzesMap: Record<string, QuizQuestion> = {};
  for (const quiz of quizzesToSync) {
    const quizId = quiz.id || `quiz_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    quizzesMap[quizId] = { ...quiz, id: quizId, updatedAt: Date.now() };
    quizzesCount++;
  }
  await update(ref(db, 'quizzes'), quizzesMap);

  // 4. Sync Calendar Events to /calendar_events
  const calendarMap: Record<string, IslamicCalendarPost> = {};
  for (const event of calendarToSync) {
    const eventId = event.id || `cal_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    calendarMap[eventId] = { ...event, id: eventId, updatedAt: Date.now() };
    calendarCount++;
  }
  await update(ref(db, 'calendar_events'), calendarMap);

  // Cache locally
  localStorage.setItem('noor_categories', JSON.stringify(Object.values(categoriesMap)));
  localStorage.setItem('noor_posts', JSON.stringify(Object.values(postsMap)));
  localStorage.setItem('noor_quizzes', JSON.stringify(Object.values(quizzesMap)));
  localStorage.setItem('noor_calendar_events', JSON.stringify(Object.values(calendarMap)));

  // Log audit
  try {
    await push(ref(db, 'audit_logs'), {
      action: 'SYSTEM_SYNC_ALL_CONTENT',
      details: `Synced ${postsCount} posts, ${categoriesCount} categories, ${quizzesCount} quizzes, ${calendarCount} calendar events to Realtime Database`,
      timestamp: Date.now(),
      user: 'Administrator'
    });
  } catch (e) {
    console.warn('Audit log write skipped:', e);
  }

  return { categoriesCount, postsCount, quizzesCount, calendarCount };
}

// Log audit helper
export async function logAudit(action: string, details: string, user: string = 'Admin') {
  try {
    await push(ref(db, 'audit_logs'), {
      action,
      details,
      timestamp: Date.now(),
      user
    });
  } catch (e) {
    console.warn('Audit log write error:', e);
  }
}
