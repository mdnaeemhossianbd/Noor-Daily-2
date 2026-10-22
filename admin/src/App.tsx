import React from 'react';
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { CategoriesManager } from './pages/CategoriesManager';
import { PostsManager } from './pages/PostsManager';
import { QuizManager } from './pages/QuizManager';
import { CalendarManager } from './pages/CalendarManager';
import { AdMobSettings } from './pages/AdMobSettings';
import { PrivacyPolicy } from './pages/PrivacyPolicy';
import { PrayerConfig } from './pages/PrayerConfig';
import { AuditLogs } from './pages/AuditLogs';

export const App: React.FC = () => {
  return (
    <HashRouter>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/privacy-policy" element={<PrivacyPolicy />} />

          {/* Protected Administrative Routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="categories" element={<CategoriesManager />} />
            <Route path="posts" element={<PostsManager />} />
            <Route path="quizzes" element={<QuizManager />} />
            <Route path="calendar" element={<CalendarManager />} />
            <Route path="prayer-config" element={<PrayerConfig />} />
            <Route path="admob-settings" element={<AdMobSettings />} />
            <Route path="audit-logs" element={<AuditLogs />} />
          </Route>

          {/* Catch-all Fallback */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </HashRouter>
  );
};

export default App;
