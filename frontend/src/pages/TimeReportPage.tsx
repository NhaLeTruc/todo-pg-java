import { useState, useEffect } from 'react';

import { format } from 'date-fns';
import { Clock, BarChart3, Calendar } from 'lucide-react';

interface TimeEntry {
  id: number;
  taskId: number;
  userId: number;
  entryType: 'TIMER' | 'MANUAL';
  startTime?: string;
  endTime?: string;
  durationMinutes?: number;
  loggedAt?: string;
  notes?: string;
  running: boolean;
}

interface TimeReport {
  entries: TimeEntry[];
  totalMinutes: number;
  startDate: string;
  endDate: string;
}

const TimeReportPage: React.FC = () => {
  const [startDate, setStartDate] = useState<string>(
    new Date(new Date().setDate(new Date().getDate() - 7)).toISOString().slice(0, 10)
  );
  const [endDate, setEndDate] = useState<string>(new Date().toISOString().slice(0, 10));
  const [report, setReport] = useState<TimeReport | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchReport();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchReport = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const start = new Date(startDate);
      start.setHours(0, 0, 0, 0);
      const end = new Date(endDate);
      end.setHours(23, 59, 59, 999);

      const response = await fetch(
        `/api/v1/time-entries/report?startDate=${start.toISOString()}&endDate=${end.toISOString()}`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error('Failed to fetch time report');
      }

      const data = await response.json();
      setReport(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch time report');
    } finally {
      setIsLoading(false);
    }
  };

  const formatMinutes = (minutes: number): string => {
    if (minutes < 60) {
      return `${minutes}m`;
    }
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
  };

  const formatHours = (minutes: number): string => {
    const hours = (minutes / 60).toFixed(1);
    return `${hours}h`;
  };

  const getTimeByDay = (): Map<string, number> => {
    const timeByDay = new Map<string, number>();

    if (!report) return timeByDay;

    report.entries.forEach((entry) => {
      const date = entry.startTime
        ? new Date(entry.startTime).toISOString().slice(0, 10)
        : entry.loggedAt
          ? new Date(entry.loggedAt).toISOString().slice(0, 10)
          : '';

      if (date && entry.durationMinutes) {
        timeByDay.set(date, (timeByDay.get(date) || 0) + entry.durationMinutes);
      }
    });

    return timeByDay;
  };

  const timeByDay = getTimeByDay();
  const maxDailyTime = Math.max(...Array.from(timeByDay.values()), 1);

  return (
    <div className="container mx-auto max-w-6xl px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Time Tracking Report</h1>
        <p className="mt-2 text-gray-600">View and analyze your time tracking data</p>
      </div>

      {/* Date Range Selector */}
      <div className="mb-6 rounded-lg bg-white p-6 shadow">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <Calendar className="h-5 w-5 text-gray-400" />
            <span className="text-sm font-medium text-gray-700">Date Range:</span>
          </div>

          <div className="flex items-center gap-2">
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            />
            <span className="text-gray-500">to</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            />
          </div>

          <button
            onClick={fetchReport}
            disabled={isLoading}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:bg-indigo-400"
          >
            {isLoading ? 'Loading...' : 'Generate Report'}
          </button>
        </div>
      </div>

      {error && <div className="mb-6 rounded-md bg-red-50 p-4 text-sm text-red-800">{error}</div>}

      {report && (
        <>
          {/* Summary Cards */}
          <div className="mb-6 grid gap-4 md:grid-cols-3">
            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100">
                  <Clock className="h-6 w-6 text-blue-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Total Time</p>
                  <p className="text-2xl font-semibold text-gray-900">
                    {formatHours(report.totalMinutes)}
                  </p>
                </div>
              </div>
            </div>

            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-green-100">
                  <BarChart3 className="h-6 w-6 text-green-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Entries</p>
                  <p className="text-2xl font-semibold text-gray-900">{report.entries.length}</p>
                </div>
              </div>
            </div>

            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-purple-100">
                  <Calendar className="h-6 w-6 text-purple-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Avg per Day</p>
                  <p className="text-2xl font-semibold text-gray-900">
                    {timeByDay.size > 0 ? formatHours(report.totalMinutes / timeByDay.size) : '0h'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Time by Day Chart */}
          {timeByDay.size > 0 && (
            <div className="mb-6 rounded-lg bg-white p-6 shadow">
              <h2 className="mb-4 text-lg font-semibold text-gray-900">Time by Day</h2>
              <div className="space-y-3">
                {Array.from(timeByDay.entries())
                  .sort((a, b) => a[0].localeCompare(b[0]))
                  .map(([date, minutes]) => (
                    <div key={date}>
                      <div className="mb-1 flex justify-between text-sm">
                        <span className="font-medium text-gray-700">
                          {format(new Date(date), 'MMM d, yyyy')}
                        </span>
                        <span className="text-gray-600">{formatMinutes(minutes)}</span>
                      </div>
                      <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
                        <div
                          className="h-full rounded-full bg-indigo-600"
                          style={{ width: `${(minutes / maxDailyTime) * 100}%` }}
                        ></div>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          )}

          {/* Time Entries Table */}
          <div className="rounded-lg bg-white shadow">
            <div className="border-b border-gray-200 px-6 py-4">
              <h2 className="text-lg font-semibold text-gray-900">Time Entries</h2>
            </div>
            <div className="overflow-x-auto">
              {report.entries.length === 0 ? (
                <div className="p-8 text-center text-gray-500">
                  No time entries found for the selected date range.
                </div>
              ) : (
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Date
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Type
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Duration
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Notes
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white">
                    {report.entries.map((entry) => (
                      <tr key={entry.id} className="hover:bg-gray-50">
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                          {entry.startTime
                            ? format(new Date(entry.startTime), 'MMM d, yyyy HH:mm')
                            : entry.loggedAt
                              ? format(new Date(entry.loggedAt), 'MMM d, yyyy HH:mm')
                              : '-'}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm">
                          <span
                            className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
                              entry.entryType === 'TIMER'
                                ? 'bg-green-100 text-green-800'
                                : 'bg-blue-100 text-blue-800'
                            }`}
                          >
                            {entry.entryType}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                          {entry.durationMinutes ? formatMinutes(entry.durationMinutes) : '-'}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500">{entry.notes || '-'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </>
      )}

      {!report && !isLoading && !error && (
        <div className="rounded-lg bg-white p-12 text-center shadow">
          <Clock className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">No report generated</h3>
          <p className="mt-1 text-sm text-gray-500">
            Select a date range and click &quot;Generate Report&quot; to view your time tracking
            data.
          </p>
        </div>
      )}
    </div>
  );
};

export default TimeReportPage;
