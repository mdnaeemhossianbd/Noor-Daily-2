import React, { createContext, useContext, useEffect, useState } from 'react';
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword,
  signInWithPopup,
  GoogleAuthProvider,
  signOut, 
  onAuthStateChanged
} from 'firebase/auth';
import { auth } from '../api/firebase';

export interface AdminUser {
  uid: string;
  email: string | null;
  displayName?: string | null;
  isLocalSession?: boolean;
}

interface AuthContextType {
  currentUser: AdminUser | null;
  isAdmin: boolean;
  loading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  registerAdmin: (email: string, pass: string) => Promise<void>;
  loginWithGoogle: () => Promise<void>;
  logout: () => Promise<void>;
  authError: string | null;
}

const LOCAL_STORAGE_KEY = 'noor_daily_admin_auth';

// Strict Admin Access Control:
// Only authorized administrator emails are permitted. All unauthorized Gmail accounts are rejected!
export const AUTHORIZED_ADMIN_EMAILS = [
  'habitsk50@gmail.com',
  'admin@noordaily.com'
];

export function isEmailAuthorized(email: string | null | undefined): boolean {
  if (!email) return false;
  return AUTHORIZED_ADMIN_EMAILS.includes(email.trim().toLowerCase());
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Firebase Auth is the source of truth; cached localStorage data is never treated as proof of login.
  const [currentUser, setCurrentUser] = useState<AdminUser | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);

  const [authError, setAuthError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    let unsubscribe = () => {};
    try {
      unsubscribe = onAuthStateChanged(auth, async (user) => {
        if (!isMounted) return;

        if (user) {
          // Check if this Google / Email user is authorized
          if (!isEmailAuthorized(user.email)) {
            console.warn('Unauthorized user blocked:', user.email);
            try {
              await signOut(auth);
            } catch (_) {}
            setCurrentUser(null);
            setIsAdmin(false);
            localStorage.removeItem(LOCAL_STORAGE_KEY);
            setAuthError(`অননুমোদিত প্রবেশাধিকার: (${user.email}) এই অ্যাকাউন্টটি অনুমোদিত নয়। শুধুমাত্র অ্যাডমিন (habitsk50@gmail.com) লগইন করতে পারবেন।`);
            setLoading(false);
            return;
          }

          const adminUser: AdminUser = {
            uid: user.uid,
            email: user.email,
            displayName: user.displayName || 'Administrator',
            isLocalSession: false
          };

          setIsAdmin(true);
          setCurrentUser(adminUser);
          localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(adminUser));
          setAuthError(null);
        } else {
          setCurrentUser(null);
          setIsAdmin(false);
          localStorage.removeItem(LOCAL_STORAGE_KEY);
        }
        setLoading(false);
      });
    } catch (e) {
      console.warn('Firebase onAuthStateChanged error:', e);
      setLoading(false);
    }

    return () => {
      isMounted = false;
      unsubscribe();
    };
  }, []);

  // Email/Password Sign-In
  const login = async (email: string, pass: string) => {
    setAuthError(null);
    if (!isEmailAuthorized(email)) {
      const msg = `অননুমোদিত ইমেইল: (${email}) এই অ্যাকাউন্টটি অ্যাডমিন প্যানেলে অনুমোদিত নয়। শুধুমাত্র habitsk50@gmail.com অনুমোদিত।`;
      setAuthError(msg);
      throw new Error(msg);
    }

    try {
      const cred = await signInWithEmailAndPassword(auth, email, pass);
      const adminUser: AdminUser = {
        uid: cred.user.uid,
        email: cred.user.email,
        displayName: cred.user.displayName || 'Administrator',
        isLocalSession: false
      };
      setCurrentUser(adminUser);
      setIsAdmin(true);
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(adminUser));
    } catch (err: any) {
      console.error('Login error in AuthContext:', err);
      setAuthError(err.message || 'Authentication failed');
      throw err;
    }
  };

  // Register New Admin Account
  const registerAdmin = async (email: string, pass: string) => {
    setAuthError(null);
    if (!isEmailAuthorized(email)) {
      const msg = `অননুমোদিত ইমেইল: শুধুমাত্র অনুমোদিত অ্যাডমিন ইমেইল (habitsk50@gmail.com) দিয়ে অ্যাকাউন্ট খোলা যাবে।`;
      setAuthError(msg);
      throw new Error(msg);
    }

    try {
      const cred = await createUserWithEmailAndPassword(auth, email, pass);
      const adminUser: AdminUser = {
        uid: cred.user.uid,
        email: cred.user.email,
        displayName: 'Administrator',
        isLocalSession: false
      };
      setCurrentUser(adminUser);
      setIsAdmin(true);
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(adminUser));
    } catch (err: any) {
      console.error('Registration error in AuthContext:', err);
      setAuthError(err.message || 'Registration failed');
      throw err;
    }
  };

  // Google Sign-In with strict unauthorized Gmail rejection!
  const loginWithGoogle = async () => {
    setAuthError(null);
    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: 'select_account' });
      const cred = await signInWithPopup(auth, provider);
      
      const userEmail = cred.user.email?.toLowerCase();
      // Strict Check: ONLY habitsk50@gmail.com or @noordaily.com allowed!
      if (!isEmailAuthorized(userEmail)) {
        await signOut(auth);
        setCurrentUser(null);
        setIsAdmin(false);
        localStorage.removeItem(LOCAL_STORAGE_KEY);
        const errorMsg = `অননুমোদিত প্রবেশাধিকার: (${cred.user.email}) এই জিমেইল অ্যাকাউন্টটি অ্যাডমিন হিসেবে অনুমোদিত নয়। শুধুমাত্র অনুমোদিত অ্যাডমিন (habitsk50@gmail.com) প্রবেশ করতে পারবেন।`;
        setAuthError(errorMsg);
        throw new Error(errorMsg);
      }

      const adminUser: AdminUser = {
        uid: cred.user.uid,
        email: cred.user.email,
        displayName: cred.user.displayName || 'Google Administrator',
        isLocalSession: false
      };
      setCurrentUser(adminUser);
      setIsAdmin(true);
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(adminUser));
    } catch (err: any) {
      console.error('Google Sign In error:', err);
      setAuthError(err.message || 'Google Sign-In failed');
      throw err;
    }
  };

  // Sign Out
  const logout = async () => {
    try {
      await signOut(auth);
    } catch (error) {
      console.warn('SignOut warning:', error);
    } finally {
      setCurrentUser(null);
      setIsAdmin(false);
      localStorage.removeItem(LOCAL_STORAGE_KEY);
    }
  };

  return (
    <AuthContext.Provider value={{ 
      currentUser, 
      isAdmin, 
      loading, 
      login, 
      registerAdmin, 
      loginWithGoogle, 
      logout, 
      authError 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
