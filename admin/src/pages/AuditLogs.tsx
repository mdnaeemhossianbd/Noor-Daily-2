import React, { useEffect, useState } from 'react';
import { limitToLast, onValue, query, ref } from 'firebase/database';
import { AlertCircle, History, RefreshCw } from 'lucide-react';
import { db } from '../api/firebase';

type AuditRecord = {
  id: string;
  action?: string;
  details?: string;
  timestamp?: number | string;
  user?: string;
  adminEmail?: string;
  adminUid?: string;
  targetCollection?: string;
  targetDocumentId?: string;
};

function formatTimestamp(value: AuditRecord['timestamp']): string {
  if (value === undefined || value === null || value === '') return 'Unknown time';
  const numeric = typeof value === 'number' ? value : Number(value);
  const date = Number.isFinite(numeric) ? new Date(numeric) : new Date(String(value));
  return Number.isNaN(date.getTime()) ? 'Invalid date' : date.toLocaleString();
}

export const AuditLogs: React.FC = () => {
  const [records, setRecords] = useState<AuditRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    setLoading(true);
    setErrorMessage(null);

    const logsQuery = query(ref(db, 'audit_logs'), limitToLast(100));
    const unsubscribe = onValue(
      logsQuery,
      (snapshot) => {
        const data = snapshot.val();
        const list: AuditRecord[] = data
          ? Object.entries(data).map(([id, value]) => ({
              id,
              ...(value && typeof value === 'object' ? value : {}),
            })) as AuditRecord[]
          : [];
        list.sort((a, b) => {
          const timeA = Number(a.timestamp) || 0;
          const timeB = Number(b.timestamp) || 0;
          return timeB - timeA;
        });
        setRecords(list);
        setLoading(false);
      },
      (error) => {
        console.error('Failed to read Firebase /audit_logs:', error);
        setErrorMessage(
          error.code === 'PERMISSION_DENIED'
            ? 'Firebase permission denied while reading /audit_logs. Check the authenticated admin read rule.'
            : `Could not load audit logs: ${error.message}`
        );
        setRecords([]);
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [refreshKey]);

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <header className="flex flex-col gap-4 rounded-2xl border border-slate-800 bg-slate-900 p-6 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <History className="h-6 w-6 text-emerald-400" />
          <div>
            <h1 className="text-xl font-bold text-white sm:text-2xl">Audit Log</h1>
            <p className="mt-1 text-sm text-slate-400">Recent administrative and content activity (latest 100 records).</p>
          </div>
        </div>
        <button
          type="button"
          onClick={() => setRefreshKey((value) => value + 1)}
          disabled={loading}
          className="inline-flex items-center justify-center gap-2 rounded-xl border border-slate-700 bg-slate-800 px-4 py-2.5 text-sm font-medium text-slate-200 hover:bg-slate-700 disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </header>

      {errorMessage && (
        <div role="alert" className="flex gap-3 rounded-xl border border-rose-500/30 bg-rose-500/10 p-4 text-sm text-rose-200">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      <section className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900">
        {loading ? (
          <div className="p-10 text-center text-sm text-slate-400">Loading audit records…</div>
        ) : records.length === 0 && !errorMessage ? (
          <div className="p-10 text-center">
            <History className="mx-auto h-10 w-10 text-slate-600" />
            <h2 className="mt-3 text-base font-semibold text-white">No audit records yet</h2>
            <p className="mt-1 text-sm text-slate-400">Records will appear here after an action is logged by the Admin Panel.</p>
          </div>
        ) : records.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="bg-slate-950/70 text-xs uppercase tracking-wide text-slate-400">
                <tr>
                  <th className="px-5 py-4 font-semibold">Date / Time</th>
                  <th className="px-5 py-4 font-semibold">Action</th>
                  <th className="px-5 py-4 font-semibold">Details</th>
                  <th className="px-5 py-4 font-semibold">User</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {records.map((record) => (
                  <tr key={record.id} className="align-top hover:bg-slate-800/40">
                    <td className="whitespace-nowrap px-5 py-4 text-slate-300">{formatTimestamp(record.timestamp)}</td>
                    <td className="px-5 py-4">
                      <span className="inline-flex rounded-lg border border-emerald-500/20 bg-emerald-500/10 px-2.5 py-1 text-xs font-semibold text-emerald-300">
                        {record.action || 'Unknown action'}
                      </span>
                    </td>
                    <td className="max-w-xl whitespace-pre-wrap break-words px-5 py-4 text-slate-300">
                      {record.details || record.targetCollection || '—'}
                    </td>
                    <td className="break-all px-5 py-4 text-slate-400">
                      {record.user || record.adminEmail || record.adminUid || 'Unknown user'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </div>
  );
};

export default AuditLogs;
