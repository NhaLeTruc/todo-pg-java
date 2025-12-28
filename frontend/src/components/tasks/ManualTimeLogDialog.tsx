import { useState } from 'react';

import { X } from 'lucide-react';

interface ManualTimeLogDialogProps {
  taskId: number;
  isOpen: boolean;
  onClose: () => void;
  onTimeLogged?: () => void;
}

const ManualTimeLogDialog: React.FC<ManualTimeLogDialogProps> = ({
  taskId,
  isOpen,
  onClose,
  onTimeLogged,
}) => {
  const [hours, setHours] = useState<number>(0);
  const [minutes, setMinutes] = useState<number>(0);
  const [notes, setNotes] = useState<string>('');
  const [loggedDate, setLoggedDate] = useState<string>(new Date().toISOString().slice(0, 16));
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const totalMinutes = hours * 60 + minutes;

    if (totalMinutes <= 0) {
      setError('Duration must be greater than 0');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(`/api/v1/tasks/${taskId}/time-entries`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({
          durationMinutes: totalMinutes,
          notes: notes.trim() || undefined,
          loggedAt: new Date(loggedDate).toISOString(),
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to log time');
      }

      setHours(0);
      setMinutes(0);
      setNotes('');
      setLoggedDate(new Date().toISOString().slice(0, 16));

      if (onTimeLogged) {
        onTimeLogged();
      }

      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to log time');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    if (!isLoading) {
      setError(null);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">Log Time Manually</h2>
          <button
            onClick={handleClose}
            disabled={isLoading}
            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        {error && <div className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="hours" className="block text-sm font-medium text-gray-700">
                Hours
              </label>
              <input
                id="hours"
                type="number"
                min="0"
                max="24"
                value={hours}
                onChange={(e) => setHours(parseInt(e.target.value) || 0)}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                disabled={isLoading}
              />
            </div>

            <div>
              <label htmlFor="minutes" className="block text-sm font-medium text-gray-700">
                Minutes
              </label>
              <input
                id="minutes"
                type="number"
                min="0"
                max="59"
                value={minutes}
                onChange={(e) => setMinutes(parseInt(e.target.value) || 0)}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                disabled={isLoading}
              />
            </div>
          </div>

          <div>
            <label htmlFor="loggedDate" className="block text-sm font-medium text-gray-700">
              Date and Time
            </label>
            <input
              id="loggedDate"
              type="datetime-local"
              value={loggedDate}
              onChange={(e) => setLoggedDate(e.target.value)}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
              disabled={isLoading}
            />
          </div>

          <div>
            <label htmlFor="notes" className="block text-sm font-medium text-gray-700">
              Notes (optional)
            </label>
            <textarea
              id="notes"
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="What did you work on?"
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
              disabled={isLoading}
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={handleClose}
              disabled={isLoading}
              className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:bg-indigo-400"
            >
              {isLoading ? 'Logging...' : 'Log Time'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ManualTimeLogDialog;
