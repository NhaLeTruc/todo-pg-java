import { useState, useEffect } from 'react';

import { Play, Square, Clock } from 'lucide-react';

import TimerDisplay from './TimerDisplay';

interface TimeEntry {
  id: number;
  taskId: number;
  userId: number;
  entryType: 'TIMER' | 'MANUAL';
  startTime?: string;
  endTime?: string;
  durationMinutes?: number;
  notes?: string;
  running: boolean;
}

interface TimeTrackerProps {
  taskId: number;
  onTimerStarted?: () => void;
  onTimerStopped?: () => void;
}

const TimeTracker: React.FC<TimeTrackerProps> = ({
  taskId,
  onTimerStarted,
  onTimerStopped,
}) => {
  const [activeTimer, setActiveTimer] = useState<TimeEntry | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [totalTime, setTotalTime] = useState<number>(0);

  useEffect(() => {
    fetchActiveTimer();
    fetchTotalTime();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskId]);

  const fetchActiveTimer = async () => {
    try {
      const response = await fetch(`/api/v1/tasks/${taskId}/time-entries/active`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (response.status === 204) {
        setActiveTimer(null);
        return;
      }

      if (!response.ok) {
        throw new Error('Failed to fetch active timer');
      }

      const data = await response.json();
      setActiveTimer(data);
    } catch (err) {
      console.error('Error fetching active timer:', err);
    }
  };

  const fetchTotalTime = async () => {
    try {
      const response = await fetch(`/api/v1/tasks/${taskId}/time-entries/total`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch total time');
      }

      const data = await response.json();
      setTotalTime(data.totalMinutes || 0);
    } catch (err) {
      console.error('Error fetching total time:', err);
    }
  };

  const startTimer = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/v1/tasks/${taskId}/time-entries/start`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({}),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to start timer');
      }

      const data = await response.json();
      setActiveTimer(data);

      if (onTimerStarted) {
        onTimerStarted();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start timer');
    } finally {
      setIsLoading(false);
    }
  };

  const stopTimer = async () => {
    if (!activeTimer) return;

    setIsLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/v1/time-entries/${activeTimer.id}/stop`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to stop timer');
      }

      setActiveTimer(null);
      await fetchTotalTime();

      if (onTimerStopped) {
        onTimerStopped();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to stop timer');
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

  return (
    <div className="space-y-4">
      {error && (
        <div className="rounded-md bg-red-50 p-3 text-sm text-red-800">
          {error}
        </div>
      )}

      <div className="flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
        <div className="flex items-center space-x-4">
          {activeTimer ? (
            <>
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-green-100">
                <div className="h-3 w-3 animate-pulse rounded-full bg-green-600"></div>
              </div>
              <div>
                <div className="text-sm font-medium text-gray-900">Timer Running</div>
                {activeTimer.startTime && (
                  <TimerDisplay
                    startTime={activeTimer.startTime}
                    className="text-green-600"
                  />
                )}
              </div>
            </>
          ) : (
            <>
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gray-100">
                <Clock className="h-5 w-5 text-gray-600" />
              </div>
              <div>
                <div className="text-sm font-medium text-gray-900">No Active Timer</div>
                <div className="text-xs text-gray-500">
                  Total: {formatMinutes(totalTime)}
                </div>
              </div>
            </>
          )}
        </div>

        <button
          onClick={activeTimer ? stopTimer : startTimer}
          disabled={isLoading}
          className={`flex items-center space-x-2 rounded-md px-4 py-2 text-sm font-medium transition-colors ${
            activeTimer
              ? 'bg-red-600 text-white hover:bg-red-700 disabled:bg-red-400'
              : 'bg-green-600 text-white hover:bg-green-700 disabled:bg-green-400'
          }`}
        >
          {activeTimer ? (
            <>
              <Square className="h-5 w-5" />
              <span>Stop</span>
            </>
          ) : (
            <>
              <Play className="h-5 w-5" />
              <span>Start</span>
            </>
          )}
        </button>
      </div>

      {totalTime > 0 && !activeTimer && (
        <div className="rounded-md bg-blue-50 p-3 text-sm text-blue-800">
          <div className="flex items-center">
            <Clock className="mr-2 h-4 w-4" />
            <span>Total time tracked: {formatMinutes(totalTime)}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default TimeTracker;
