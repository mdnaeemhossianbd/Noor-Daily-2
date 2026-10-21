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
            <Route path="prayer-config" element={<div className="text-white p-6">Prayer configuration editor</div>} />
            <Route path="admob-settings" element={<AdMobSettings />} />
            <Route path="audit-logs" element={<div className="text-white p-6">Full audit log viewer</div>} />
          </Route>

          {/* Catch-all Fallback */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </HashRouter>
  );
};

export default App;
