import React, { useState } from 'react';

interface ShareTaskDialogProps {
  taskId: number;
  onClose: () => void;
  onShare: (email: string, permission: 'VIEW' | 'EDIT') => Promise<void>;
}

export const ShareTaskDialog: React.FC<ShareTaskDialogProps> = ({ onClose, onShare }) => {
  const [email, setEmail] = useState('');
  const [permission, setPermission] = useState<'VIEW' | 'EDIT'>('VIEW');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!email.trim()) {
      setError('Email is required');
      return;
    }

    setIsLoading(true);
    try {
      await onShare(email, permission);
      setEmail('');
      setPermission('VIEW');
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to share task');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">Share Task</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
            aria-label="Close"
          >
            <svg
              className="h-6 w-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="email" className="mb-2 block text-sm font-medium text-gray-700">
              User Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              placeholder="Enter user email"
              disabled={isLoading}
            />
          </div>

          <div className="mb-6">
            <label htmlFor="permission" className="mb-2 block text-sm font-medium text-gray-700">
              Permission Level
            </label>
            <select
              id="permission"
              value={permission}
              onChange={(e) => setPermission(e.target.value as 'VIEW' | 'EDIT')}
              className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              disabled={isLoading}
            >
              <option value="VIEW">View Only</option>
              <option value="EDIT">Can Edit</option>
            </select>
          </div>

          {error && (
            <div className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</div>
          )}

          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
              disabled={isLoading}
            >
              {isLoading ? 'Sharing...' : 'Share Task'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
