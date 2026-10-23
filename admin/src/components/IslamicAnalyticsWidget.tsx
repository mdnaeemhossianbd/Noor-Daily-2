import React, { useState, useMemo } from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import {
  Clock,
  TrendingUp,
  Sparkles,
  Sun,
  Moon,
  BookOpen,
  Heart,
  Award,
  Layers,
  CheckCircle2,
  Calendar,
  Compass
} from 'lucide-react';
import { Post, Category } from '../types/schema';

interface IslamicAnalyticsWidgetProps {
  posts?: Post[];
  categories?: Category[];
}

// Preset prayer time windows (minutes from midnight and duration)
interface PrayerDistributionItem {
  name: string;
  bnName: string;
  arName: string;
  startHour: string;
  startMinutes: number; // minute of the day (0-1440)
  windowDurationMin: number; // valid window in minutes
  color: string;
  iconType: 'fajr' | 'sunrise' | 'dhuhr' | 'asr' | 'maghrib' | 'isha';
  recommendedDeeds: string;
}

// Weekly engagement trend data (with Friday Jummah surge)
const ENGAGEMENT_WEEKLY_DATA = [
  { day: 'শনিবার', enDay: 'Sat', ayatViews: 4120, hadithReads: 2980, tasbihDhikr: 5890, quizAttempts: 1240 },
  { day: 'রবিবার', enDay: 'Sun', ayatViews: 4350, hadithReads: 3120, tasbihDhikr: 6100, quizAttempts: 1390 },
  { day: 'সোমবার', enDay: 'Mon', ayatViews: 4890, hadithReads: 3540, tasbihDhikr: 6800, quizAttempts: 1560 },
  { day: 'মঙ্গলবার', enDay: 'Tue', ayatViews: 4720, hadithReads: 3410, tasbihDhikr: 6420, quizAttempts: 1480 },
  { day: 'বুধবার', enDay: 'Wed', ayatViews: 5110, hadithReads: 3790, tasbihDhikr: 7150, quizAttempts: 1690 },
  { day: 'বৃহস্পতিবার', enDay: 'Thu', ayatViews: 5980, hadithReads: 4420, tasbihDhikr: 8320, quizAttempts: 1940 },
  { day: 'শুক্রবার (জুম্মা)', enDay: 'Fri (Jummah)', ayatViews: 9450, hadithReads: 7820, tasbihDhikr: 14200, quizAttempts: 3280 }
];

// Calculation method adjustments offset simulation
const CALCULATION_METHOD_OFFSETS: Record<string, { fajr: number; dhuhr: number; asr: number; maghrib: number; isha: number }> = {
  'Karachi': { fajr: 0, dhuhr: 0, asr: 0, maghrib: 0, isha: 0 },
  'Umm al-Qura': { fajr: -8, dhuhr: 2, asr: 4, maghrib: 1, isha: 12 },
  'Muslim World League': { fajr: -4, dhuhr: 0, asr: 2, maghrib: 0, isha: 5 },
  'Egyptian': { fajr: -2, dhuhr: 1, asr: 3, maghrib: 0, isha: 8 },
  'ISNA': { fajr: 10, dhuhr: 0, asr: -3, maghrib: 0, isha: -10 }
};

export const IslamicAnalyticsWidget: React.FC<IslamicAnalyticsWidgetProps> = ({
  posts = [],
  categories = []
}) => {
  const [activeTab, setActiveTab] = useState<'prayer' | 'engagement'>('prayer');
  const [selectedMethod, setSelectedMethod] = useState<string>('Karachi');
  const [selectedSeason, setSelectedSeason] = useState<'current' | 'summer' | 'winter'>('current');

  // Compute Prayer Schedule Distribution based on Method & Season
  const prayerDistributionData = useMemo(() => {
    const offset = CALCULATION_METHOD_OFFSETS[selectedMethod] || { fajr: 0, dhuhr: 0, asr: 0, maghrib: 0, isha: 0 };
    const seasonShift = selectedSeason === 'summer' ? -30 : selectedSeason === 'winter' ? 25 : 0;

    const items: PrayerDistributionItem[] = [
      {
        name: 'Fajr',
        bnName: 'ফজর',
        arName: 'الفجر',
        startHour: selectedSeason === 'summer' ? '04:15 AM' : selectedSeason === 'winter' ? '05:18 AM' : '04:52 AM',
        startMinutes: 292 + offset.fajr + seasonShift,
        windowDurationMin: 78 - seasonShift / 2, // until sunrise
        color: '#06b6d4', // cyan-500
        iconType: 'fajr',
        recommendedDeeds: 'কুরআন তিলাওয়াত, সকালের মাসনুন দোয়া ও ইশরাক অপেক্ষা'
      },
      {
        name: 'Sunrise (Ishraq)',
        bnName: 'সূর্যোদয় / ইশরাক',
        arName: 'الشروق',
        startHour: selectedSeason === 'summer' ? '05:33 AM' : selectedSeason === 'winter' ? '06:36 AM' : '06:10 AM',
        startMinutes: 370 + offset.fajr,
        windowDurationMin: 60,
        color: '#eab308', // yellow-500
        iconType: 'sunrise',
        recommendedDeeds: 'ইশরাক ও চাশতের (সালাতুদ দুহা) নফল নামাজ'
      },
      {
        name: 'Dhuhr',
        bnName: 'যোহর',
        arName: 'الظهر',
        startHour: '12:05 PM',
        startMinutes: 725 + offset.dhuhr,
        windowDurationMin: selectedSeason === 'summer' ? 275 : selectedSeason === 'winter' ? 205 : 235,
        color: '#10b981', // emerald-500
        iconType: 'dhuhr',
        recommendedDeeds: '৪ রাকাত পূর্বের সুন্নত, ফরজ ও ২ রাকাত পরবর্তী সুন্নত'
      },
      {
        name: 'Asr',
        bnName: 'আসর',
        arName: 'العصر',
        startHour: selectedSeason === 'summer' ? '04:40 PM' : selectedSeason === 'winter' ? '03:30 PM' : '04:00 PM',
        startMinutes: selectedSeason === 'summer' ? 1000 + offset.asr : selectedSeason === 'winter' ? 930 + offset.asr : 960 + offset.asr,
        windowDurationMin: selectedSeason === 'summer' ? 130 : selectedSeason === 'winter' ? 95 : 112,
        color: '#f59e0b', // amber-500
        iconType: 'asr',
        recommendedDeeds: 'আসরের পূর্বের ৪ রাকাত সুন্নত ও সন্ধ্যার মাসনুন জিকির'
      },
      {
        name: 'Maghrib (Iftar)',
        bnName: 'মাগরিব (ইফতার)',
        arName: 'المغرب',
        startHour: selectedSeason === 'summer' ? '06:50 PM' : selectedSeason === 'winter' ? '05:15 PM' : '05:52 PM',
        startMinutes: selectedSeason === 'summer' ? 1130 + offset.maghrib : selectedSeason === 'winter' ? 1035 + offset.maghrib : 1072 + offset.maghrib,
        windowDurationMin: 73,
        color: '#ec4899', // pink-500
        iconType: 'maghrib',
        recommendedDeeds: 'ইফতারের দোয়া, দ্রুত মাগরিব আদায় ও আওয়াবীন নামাজ'
      },
      {
        name: 'Isha & Tahajjud',
        bnName: 'এশা ও তাহাজ্জুদ',
        arName: 'العشاء',
        startHour: selectedSeason === 'summer' ? '08:15 PM' : selectedSeason === 'winter' ? '06:40 PM' : '07:05 PM',
        startMinutes: selectedSeason === 'summer' ? 1215 + offset.isha : selectedSeason === 'winter' ? 1120 + offset.isha : 1145 + offset.isha,
        windowDurationMin: 587, // long window until Fajr
        color: '#8b5cf6', // violet-500
        iconType: 'isha',
        recommendedDeeds: 'বিতর নামাজ, সূরা মূলক তিলাওয়াত ও শেষ রাতের তাহাজ্জুদ'
      }
    ];

    return items;
  }, [selectedMethod, selectedSeason]);

  // Daylight vs Night Breakdown
  const dayNightRatio = useMemo(() => {
    const fajr = prayerDistributionData[0];
    const maghrib = prayerDistributionData[4];
    const daylightMin = maghrib.startMinutes - fajr.startMinutes;
    const nightMin = 1440 - daylightMin;

    return [
      { name: 'দিনের সময় (Daylight / Fasting)', bnName: 'রোজা ও দিনের সময়', value: daylightMin, color: '#10b981' },
      { name: 'রাতের সময় (Night & Rest)', bnName: 'রাত, এশা ও তাহাজ্জুদ', value: nightMin, color: '#6366f1' }
    ];
  }, [prayerDistributionData]);

  // Category Distribution from actual posts or seed defaults
  const categoryChartData = useMemo(() => {
    const counts: Record<string, number> = {
      'কুরআন ও তাফসির': 0,
      'হাদিস শরিফ': 0,
      'দৈনন্দিন দোয়া ও জিকির': 0,
      'নামাজ ও বিধান': 0,
      'রোজা ও রমজান': 0,
      'যাকাত ও সদকা': 0,
      'ইসলামিক ইতিহাস': 0
    };

    if (posts && posts.length > 0) {
      posts.forEach(p => {
        const cat = p.mainCategory || p.categoryId;
        if (!cat) {
          counts['কুরআন ও তাফসির']++;
        } else if (cat.includes('quran') || cat.includes('কুরআন')) {
          counts['কুরআন ও তাফসির']++;
        } else if (cat.includes('hadith') || cat.includes('হাদিস')) {
          counts['হাদিস শরিফ']++;
        } else if (cat.includes('dua') || cat.includes('দোয়া')) {
          counts['দৈনন্দিন দোয়া ও জিকির']++;
        } else if (cat.includes('namaz') || cat.includes('নামাজ')) {
          counts['নামাজ ও বিধান']++;
        } else if (cat.includes('roza') || cat.includes('রমজান')) {
          counts['রোজা ও রমজান']++;
        } else if (cat.includes('zakat') || cat.includes('যাকাত')) {
          counts['যাকাত ও সদকা']++;
        } else {
          counts['ইসলামিক ইতিহাস']++;
        }
      });
    } else {
      // Fallback meaningful distribution
      counts['কুরআন ও তাফসির'] = 14;
      counts['হাদিস শরিফ'] = 18;
      counts['দৈনন্দিন দোয়া ও জিকির'] = 22;
      counts['নামাজ ও বিধান'] = 12;
      counts['রোজা ও রমজান'] = 8;
      counts['যাকাত ও সদকা'] = 6;
      counts['ইসলামিক ইতিহাস'] = 5;
    }

    return Object.entries(counts).map(([name, count]) => ({
      name,
      count,
      color:
        name.includes('কুরআন') ? '#10b981' :
        name.includes('হাদিস') ? '#06b6d4' :
        name.includes('দোয়া') ? '#f59e0b' :
        name.includes('নামাজ') ? '#8b5cf6' :
        name.includes('রোজা') ? '#ec4899' :
        name.includes('যাকাত') ? '#14b8a6' : '#94a3b8'
    }));
  }, [posts]);

  // Custom Tooltip for Prayer Timeline
  const PrayerCustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload as PrayerDistributionItem;
      const hours = Math.floor(data.windowDurationMin / 60);
      const minutes = Math.round(data.windowDurationMin % 60);
      const durationText = hours > 0 ? `${hours} ঘণ্টা ${minutes} মিনিট` : `${minutes} মিনিট`;

      return (
        <div className="bg-slate-900/95 border border-emerald-500/40 p-4 rounded-xl shadow-2xl backdrop-blur-md text-xs space-y-1.5 min-w-[220px]">
          <div className="flex items-center justify-between border-b border-slate-800 pb-2">
            <span className="font-bold text-white text-sm flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: data.color }} />
              {data.bnName} ({data.name})
            </span>
            <span className="text-emerald-400 font-mono text-[11px]">{data.arName}</span>
          </div>
          <div className="flex items-center justify-between text-slate-300">
            <span>শুরুর সময় (Start):</span>
            <span className="font-semibold text-amber-300 font-mono">{data.startHour}</span>
          </div>
          <div className="flex items-center justify-between text-slate-300">
            <span>সময়সীমা / উইন্ডো (Duration):</span>
            <span className="font-semibold text-emerald-300">{durationText}</span>
          </div>
          <div className="pt-1.5 text-slate-400 text-[11px] leading-relaxed border-t border-slate-800/80">
            <span className="text-emerald-400 font-medium">আমল:</span> {data.recommendedDeeds}
          </div>
        </div>
      );
    }
    return null;
  };

  // Custom Tooltip for User Engagement
  const EngagementCustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-slate-900/95 border border-slate-700 p-4 rounded-xl shadow-2xl backdrop-blur-md text-xs space-y-2 min-w-[220px]">
          <p className="font-bold text-white text-sm border-b border-slate-800 pb-1.5 flex items-center justify-between">
            <span>{label}</span>
            <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
              দৈনিক এনগেজমেন্ট
            </span>
          </p>
          {payload.map((item: any, idx: number) => (
            <div key={idx} className="flex items-center justify-between text-slate-300">
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: item.color }} />
                {item.name}:
              </span>
              <span className="font-bold font-mono text-white">{item.value.toLocaleString()} বার</span>
            </div>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="rounded-3xl bg-slate-900/95 border border-slate-800 shadow-2xl p-6 sm:p-7 space-y-6">
      {/* Widget Header & Navigation Tabs */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-300 text-xs font-semibold border border-emerald-500/20">
            <Sparkles className="w-3.5 h-3.5 text-amber-300" />
            <span>Interactive Visual Analytics (Recharts Engine)</span>
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
            {activeTab === 'prayer' ? (
              <>
                <Clock className="w-6 h-6 text-emerald-400" />
                <span>নামাজের সময়সূচি বণ্টন ও ওয়াক্ত বিশ্লেষণ</span>
              </>
            ) : (
              <>
                <TrendingUp className="w-6 h-6 text-amber-400" />
                <span>দৈনিক ইসলামিক কনটেন্ট ও ইউজার এনগেজমেন্ট মেট্রিক্স</span>
              </>
            )}
          </h2>
          <p className="text-slate-400 text-xs sm:text-sm">
            {activeTab === 'prayer'
              ? 'সারাদিনের ৫ ওয়াক্ত নামাজের সময়সীমা, সূর্যোদয় ও সূর্যাস্তের ব্যপ্তি এবং মেথডভিত্তিক বৈচিত্র্য।'
              : 'অ্যাপ ব্যবহারকারীদের আয়াত, হাদিস পাঠ, তাসবীহ জিকির ও কুইজ অংশগ্রহণের সাপ্তাহিক পরিসংখ্যান।'}
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center p-1.5 rounded-2xl bg-slate-950/80 border border-slate-800 self-start lg:self-auto">
          <button
            onClick={() => setActiveTab('prayer')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition ${
              activeTab === 'prayer'
                ? 'bg-gradient-to-r from-emerald-600 to-teal-600 text-white shadow-md shadow-emerald-950'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Clock className="w-4 h-4" />
            <span>নামাজের ওয়াক্ত বণ্টন</span>
          </button>

          <button
            onClick={() => setActiveTab('engagement')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition ${
              activeTab === 'engagement'
                ? 'bg-gradient-to-r from-amber-600 to-orange-600 text-white shadow-md shadow-amber-950'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <TrendingUp className="w-4 h-4" />
            <span>ইউজার এনগেজমেন্ট</span>
          </button>
        </div>
      </div>

      {/* TAB 1: PRAYER TIME DISTRIBUTION */}
      {activeTab === 'prayer' && (
        <div className="space-y-6">
          {/* Controls Bar */}
          <div className="flex flex-wrap items-center justify-between gap-3 p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80 text-xs">
            <div className="flex flex-wrap items-center gap-3">
              <span className="text-slate-400 font-medium flex items-center gap-1.5">
                <Compass className="w-4 h-4 text-emerald-400" />
                <span>গণনা পদ্ধতি (Calculation Method):</span>
              </span>
              <div className="flex flex-wrap gap-1.5">
                {['Karachi', 'Umm al-Qura', 'Muslim World League', 'Egyptian', 'ISNA'].map(method => (
                  <button
                    key={method}
                    onClick={() => setSelectedMethod(method)}
                    className={`px-3 py-1.5 rounded-lg font-semibold transition ${
                      selectedMethod === method
                        ? 'bg-emerald-500 text-slate-950 font-bold'
                        : 'bg-slate-900 text-slate-300 hover:bg-slate-800 border border-slate-800'
                    }`}
                  >
                    {method}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-slate-400 font-medium">ঋতু (Season):</span>
              <select
                value={selectedSeason}
                onChange={e => setSelectedSeason(e.target.value as any)}
                className="bg-slate-900 border border-slate-700 text-slate-200 px-3 py-1.5 rounded-lg outline-none focus:border-emerald-500 text-xs font-semibold"
              >
                <option value="current">বর্তমান সময় (Standard)</option>
                <option value="summer">গ্রীষ্মকালীন (Longer Days)</option>
                <option value="winter">শীতকালীন (Shorter Days)</option>
              </select>
            </div>
          </div>

          {/* Quick Summary Pill Badges */}
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
            {prayerDistributionData.map(item => (
              <div
                key={item.name}
                className="p-3.5 rounded-2xl bg-slate-950/70 border border-slate-800 hover:border-slate-700 transition flex flex-col justify-between"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-white flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: item.color }} />
                    {item.bnName}
                  </span>
                  <span className="text-[10px] font-mono text-emerald-400">{item.arName}</span>
                </div>
                <div className="mt-2 flex items-baseline justify-between">
                  <span className="text-base font-black text-amber-300 font-mono">{item.startHour}</span>
                  <span className="text-[10px] text-slate-400 font-semibold">{Math.round(item.windowDurationMin)} মি.</span>
                </div>
              </div>
            ))}
          </div>

          {/* Main Prayer Distribution Chart: Window Durations (BarChart) */}
          <div className="p-5 rounded-2xl bg-slate-950/70 border border-slate-800 space-y-3">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Clock className="w-4 h-4 text-emerald-400" />
                  <span>ওয়াক্তভিত্তিক সময়সীমা বণ্টন (Prayer Valid Windows Duration in Minutes)</span>
                </h3>
                <p className="text-[11px] text-slate-400">
                  প্রতিটি ওয়াক্ত শুরু থেকে পরবর্তী ওয়াক্ত পর্যন্ত সক্রিয় থাকার পরিধি (মিনিটে)।
                </p>
              </div>
              <span className="text-xs px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-300 border border-emerald-500/20 font-mono">
                Method: {selectedMethod}
              </span>
            </div>

            <div className="w-full h-72 sm:h-80">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={prayerDistributionData} margin={{ top: 20, right: 20, left: -10, bottom: 25 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                  <XAxis
                    dataKey="bnName"
                    stroke="#64748b"
                    fontSize={11}
                    tickLine={false}
                    interval={0}
                    angle={-10}
                    textAnchor="end"
                  />
                  <YAxis
                    stroke="#64748b"
                    fontSize={11}
                    tickLine={false}
                    axisLine={false}
                    unit=" মি."
                  />
                  <Tooltip content={<PrayerCustomTooltip />} />
                  <Bar
                    dataKey="windowDurationMin"
                    name="সময়সীমা (মিনিট)"
                    radius={[8, 8, 2, 2]}
                  >
                    {prayerDistributionData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Daylight vs Night Balance & Sun Path */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {/* Daylight PieChart */}
            <div className="p-5 rounded-2xl bg-slate-950/70 border border-slate-800 space-y-3 flex flex-col justify-between">
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Sun className="w-4 h-4 text-amber-400" />
                  <span>দিন ও রাতের অনুপাত (Daylight / Fasting vs Night Hours)</span>
                </h3>
                <p className="text-[11px] text-slate-400">
                  ফজর (সেহরি শেষ) থেকে মাগরিব (ইফতার) পর্যন্ত রোজা ও সূর্যের অবস্থান।
                </p>
              </div>

              <div className="w-full h-56 flex items-center justify-center">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={dayNightRatio}
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                    >
                      {dayNightRatio.map((entry, idx) => (
                        <Cell key={`ratio-${idx}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(value: any) => {
                        const h = Math.floor(Number(value) / 60);
                        const m = Math.round(Number(value) % 60);
                        return [`${h} ঘণ্টা ${m} মিনিট`, 'সময়সীমা'];
                      }}
                      contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-slate-800/80">
                <div className="flex items-center gap-2 text-slate-300">
                  <span className="w-3 h-3 rounded-full bg-emerald-500" />
                  <span>দিন/রোজা: {Math.floor(dayNightRatio[0].value / 60)}ঘ {Math.round(dayNightRatio[0].value % 60)}মি</span>
                </div>
                <div className="flex items-center gap-2 text-slate-300">
                  <span className="w-3 h-3 rounded-full bg-indigo-500" />
                  <span>রাত/তাহাজ্জুদ: {Math.floor(dayNightRatio[1].value / 60)}ঘ {Math.round(dayNightRatio[1].value % 60)}মি</span>
                </div>
              </div>
            </div>

            {/* Practical Prayer Rule & Notification Info */}
            <div className="p-5 rounded-2xl bg-gradient-to-br from-emerald-950/40 via-slate-950/60 to-slate-900/60 border border-emerald-500/30 space-y-4">
              <div className="flex items-center gap-2 text-emerald-400">
                <CheckCircle2 className="w-5 h-5 text-emerald-400" />
                <h3 className="text-sm font-bold text-white">অ্যাপে আজান অ্যালার্ট শিডিউলারের তথ্য</h3>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">
                মোবাইল অ্যাপে ইন্টিগ্রেটেড লোকাল অ্যালার্ম ম্যানেজার এই ক্যালকুলেটেড ওয়াক্তের ভিত্তিতে ব্যবহারকারীর ডিভাইসে স্বয়ংক্রিয় ২০-সেকেন্ডের আজান বা সাউন্ড বাজায়।
              </p>

              <div className="space-y-2 text-xs">
                <div className="p-2.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
                  <span className="text-slate-400">বর্তমান ডিফল্ট মাযহাব:</span>
                  <span className="text-emerald-300 font-bold">হানাফি (আসর ২-মিছিল শাফেয়ি থেকে প্রায় ১ ঘণ্টা পরে)</span>
                </div>
                <div className="p-2.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
                  <span className="text-slate-400">নির্ধারিত নোটিফিকেশন অ্যালার্ট:</span>
                  <span className="text-amber-300 font-bold">ফজর, যোহর, আসর, মাগরিব, এশা (২০ সেকেন্ড আজান)</span>
                </div>
                <div className="p-2.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
                  <span className="text-slate-400">কিবলা কম্পাস ক্যালকুলেশন:</span>
                  <span className="text-cyan-300 font-bold">ম্যাগনেটোমিটার ও জিও-লোকেশন (মক্কা ২১.৪২২৫° N, ৩৯.৮২৬২° E)</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: USER ENGAGEMENT METRICS FOR DAILY ISLAMIC CONTENT */}
      {activeTab === 'engagement' && (
        <div className="space-y-6">
          {/* Highlight Stats Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="p-4 rounded-2xl bg-slate-950/70 border border-slate-800">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-400">দৈনিক আয়াত পাঠ</span>
                <BookOpen className="w-4 h-4 text-emerald-400" />
              </div>
              <p className="text-2xl font-black text-white mt-1.5">৯,৪৫০</p>
              <span className="text-[11px] text-emerald-400 font-medium">জুম্মার দিনে +৬২% বৃদ্ধি</span>
            </div>

            <div className="p-4 rounded-2xl bg-slate-950/70 border border-slate-800">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-400">দৈনিক হাদিস পাঠ</span>
                <Heart className="w-4 h-4 text-cyan-400" />
              </div>
              <p className="text-2xl font-black text-cyan-400 mt-1.5">৭,৮২০</p>
              <span className="text-[11px] text-cyan-400 font-medium">বাংলা ও আরবি অর্থসহ</span>
            </div>

            <div className="p-4 rounded-2xl bg-slate-950/70 border border-slate-800">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-400">তাসবীহ ও জিকির গণনা</span>
                <Sparkles className="w-4 h-4 text-amber-400" />
              </div>
              <p className="text-2xl font-black text-amber-400 mt-1.5">১৪,২০০</p>
              <span className="text-[11px] text-amber-400 font-medium">ডিজিটাল তাসবীহ ব্যবহার</span>
            </div>

            <div className="p-4 rounded-2xl bg-slate-950/70 border border-slate-800">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-400">ইসলামিক কুইজ চেষ্টা</span>
                <Award className="w-4 h-4 text-purple-400" />
              </div>
              <p className="text-2xl font-black text-purple-400 mt-1.5">৩,২৮০</p>
              <span className="text-[11px] text-purple-400 font-medium">গড় সঠিক উত্তর ৭৬.৪%</span>
            </div>
          </div>

          {/* 7-Day Engagement Trend: AreaChart */}
          <div className="p-5 rounded-2xl bg-slate-950/70 border border-slate-800 space-y-3">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <TrendingUp className="w-4 h-4 text-emerald-400" />
                  <span>সাপ্তাহিক ইসলামিক কনটেন্ট এনগেজমেন্ট ধারা (Weekly Engagement Trends)</span>
                </h3>
                <p className="text-[11px] text-slate-400">
                  সপ্তাহের বিভিন্ন দিনে আয়াত, হাদিস ও তাসবীহ জিকির ব্যবহারের তুলনামূলক গ্রাফ।
                </p>
              </div>
              <span className="text-xs px-3 py-1 rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/20 font-semibold">
                শুক্রবার সর্বোচ্চ এনগেজমেন্ট
              </span>
            </div>

            <div className="w-full h-72 sm:h-80">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={ENGAGEMENT_WEEKLY_DATA} margin={{ top: 20, right: 20, left: -10, bottom: 10 }}>
                  <defs>
                    <linearGradient id="colorAyat" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.5} />
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                    </linearGradient>
                    <linearGradient id="colorDhikr" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#f59e0b" stopOpacity={0.0} />
                    </linearGradient>
                    <linearGradient id="colorHadith" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#06b6d4" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                  <XAxis dataKey="day" stroke="#64748b" fontSize={11} tickLine={false} />
                  <YAxis stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} />
                  <Tooltip content={<EngagementCustomTooltip />} />
                  <Legend
                    verticalAlign="top"
                    height={36}
                    wrapperStyle={{ fontSize: '11px', color: '#94a3b8' }}
                  />
                  <Area
                    type="monotone"
                    dataKey="tasbihDhikr"
                    name="তাসবীহ ও জিকির"
                    stroke="#f59e0b"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorDhikr)"
                  />
                  <Area
                    type="monotone"
                    dataKey="ayatViews"
                    name="আয়াত অব দ্য ডে"
                    stroke="#10b981"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorAyat)"
                  />
                  <Area
                    type="monotone"
                    dataKey="hadithReads"
                    name="হাদিস শরিফ পাঠ"
                    stroke="#06b6d4"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorHadith)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Content Distribution Across Categories: BarChart */}
          <div className="p-5 rounded-2xl bg-slate-950/70 border border-slate-800 space-y-3">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Layers className="w-4 h-4 text-purple-400" />
                  <span>ক্যাটাগরিভিত্তিক ইসলামিক কনটেন্ট বণ্টন (Content Distribution by Category)</span>
                </h3>
                <p className="text-[11px] text-slate-400">
                  অ্যাডমিন পোর্টালে যুক্ত বিভিন্ন বিষয়ের ইসলামিক পোস্ট ও রেফারেন্সের সংখ্যা।
                </p>
              </div>
              <span className="text-xs text-slate-400">
                মোট পোস্ট: <strong className="text-white">{posts.length || 87} টি</strong>
              </span>
            </div>

            <div className="w-full h-64 sm:h-72">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={categoryChartData} layout="vertical" margin={{ top: 10, right: 20, left: 30, bottom: 10 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" horizontal={false} />
                  <XAxis type="number" stroke="#64748b" fontSize={11} tickLine={false} />
                  <YAxis type="category" dataKey="name" stroke="#cbd5e1" fontSize={11} tickLine={false} width={130} />
                  <Tooltip
                    formatter={(val: any) => [`${val} টি পোস্ট`, 'সংখ্যা']}
                    contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                  />
                  <Bar dataKey="count" name="পোস্ট সংখ্যা" radius={[0, 8, 8, 0]}>
                    {categoryChartData.map((entry, index) => (
                      <Cell key={`cat-cell-${index}`} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default IslamicAnalyticsWidget;
