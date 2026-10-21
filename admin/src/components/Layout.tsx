import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, 
  FolderTree, 
  FileText, 
  Clock, 
  Sliders, 
  History, 
  LogOut, 
  Menu, 
  X,
  Compass,
  HelpCircle,
  Calendar,
  Sparkles
} from 'lucide-react';

export const Layout: React.FC = () => {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const navItems = [
    { label: 'ড্যাশবোর্ড (Dashboard)', path: '/dashboard', icon: LayoutDashboard },
    { label: 'ইসলামিক পোস্ট (Posts)', path: '/posts', icon: FileText },
    { label: 'ক্যাটাগরি সমূহ (Categories)', path: '/categories', icon: FolderTree },
    { label: 'ইসলামিক কুইজ (Quizzes)', path: '/quizzes', icon: HelpCircle },
    { label: 'ইসলামিক ক্যালেন্ডার (Calendar)', path: '/calendar', icon: Calendar },
    { label: 'নামাজের সময় কনফিগ (Prayer Config)', path: '/prayer-config', icon: Clock },
    { label: 'বিজ্ঞাপন ও AdMob সেটিংস', path: '/admob-settings', icon: Sliders },
    { label: 'অডিট লগ (Audit Logs)', path: '/audit-logs', icon: History },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col md:flex-row">
      
      {/* Mobile Top App Bar */}
      <div className="md:hidden flex items-center justify-between p-4 bg-slate-900 border-b border-slate-800 sticky top-0 z-50">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30">
            <Compass className="w-5 h-5" />
          </div>
          <span className="font-bold text-emerald-100 text-lg">নূর ডেইলি অ্যাডমিন</span>
        </div>
        <button 
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="p-2 text-slate-400 hover:text-white rounded-lg focus:outline-none"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Sidebar Navigation (Desktop & Mobile Drawer) */}
      <aside 
        className={`fixed md:static inset-y-0 left-0 z-40 w-64 bg-slate-900 border-r border-slate-800 flex flex-col transition-transform duration-300 ease-in-out md:translate-x-0 ${
          mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="p-6 hidden md:flex items-center gap-3 border-b border-slate-800/80">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center border border-emerald-500/30 shadow-inner">
            <Compass className="w-6 h-6" />
          </div>
          <div>
            <h1 className="font-bold text-emerald-100 text-lg leading-tight flex items-center gap-1.5">
              <span>নূর ডেইলি</span>
              <Sparkles className="w-4 h-4 text-emerald-400" />
            </h1>
            <p className="text-xs text-emerald-400/80 font-medium">অ্যাডমিন কন্ট্রোল প্যানেল</p>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="flex-1 p-4 space-y-1.5 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => setMobileMenuOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition duration-150 ${
                    isActive
                      ? 'bg-emerald-600/20 text-emerald-300 border border-emerald-500/30'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                  }`
                }
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* User Profile & Logout */}
        <div className="p-4 border-t border-slate-800/80 bg-slate-900/50">
          <div className="flex items-center gap-3 mb-3 px-2">
            <div className="w-8 h-8 rounded-full bg-emerald-600 flex items-center justify-center text-white text-xs font-bold uppercase">
              {currentUser?.email ? currentUser.email.substring(0, 2) : 'AD'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-slate-200 truncate">{currentUser?.email || 'অ্যাডমিনিস্ট্রেটর'}</p>
              <p className="text-[10px] text-emerald-400 font-medium">সুপার অ্যাডমিন</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 bg-slate-800/80 hover:bg-red-500/10 text-slate-400 hover:text-red-400 border border-slate-700/50 hover:border-red-500/30 rounded-xl text-xs font-medium transition duration-150"
          >
            <LogOut className="w-4 h-4" />
            <span>লগআউট করুন (Log Out)</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        <div className="flex-1 p-4 md:p-8 max-w-7xl w-full mx-auto">
          <Outlet />
        </div>
      </main>

    </div>
  );
};
export default Layout;
