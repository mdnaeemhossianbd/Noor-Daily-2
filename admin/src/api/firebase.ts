import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getStorage } from 'firebase/storage';
import { getDatabase } from 'firebase/database';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
 apiKey: "AIzaSyA0L1PfFrDxgYLUROcZYTiY-P0IStGhF9g",
  authDomain: "gen-lang-client-0048418625.firebaseapp.com",
  databaseURL:
    'https://gen-lang-client-0048418625-default-rtdb.asia-southeast1.firebasedatabase.app',
  projectId: "gen-lang-client-0048418625",
  storageBucket: "gen-lang-client-0048418625.firebasestorage.app",
  messagingSenderId: "585883427862",
  appId: "1:585883427862:web:cbe771340a23a318ac0137"
};

// Initialize Firebase safely preventing duplicate instances
const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const storage = getStorage(app);

// Realtime Database instance pointing to asia-southeast1
export const db = getDatabase(app);

// Cloud Firestore instance
export const firestore = getFirestore(app);

export default app;
