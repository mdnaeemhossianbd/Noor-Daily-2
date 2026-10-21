import React, { useEffect, useState } from 'react';
import { Navigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck } from 'lucide-react';

export const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { currentUser, isAdmin, loading } = useAuth();
  const [showFallbackLink, setShowFallbackLink] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setShowFallbackLink(true);
    }, 1500);
    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-emerald-950 text-white p-4">
        <div className="flex flex-col items-center gap-4 text-center max-w-sm">
          <div className="w-12 h-12 border-4 border-emerald-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-emerald-200 font-medium text-sm">Verifying admin credentials...</p>
          {showFallbackLink && (
            <Link
              to="/login"
              className="mt-2 text-xs text-emerald-400 hover:text-emerald-300 underline font-semibold transition"
            >
              Taking too long? Click here to open Login Page
            </Link>
          )}
        </div>
      </div>
    );
  }

  if (!currentUser || !isAdmin) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
