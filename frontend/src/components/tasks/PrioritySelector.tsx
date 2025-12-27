import React from 'react';

import { Priority } from '@/types/task';
import { getPriorityColor } from '@/utils/priorityUtils';

interface PrioritySelectorProps {
  value: Priority;
  onChange: (priority: Priority) => void;
  disabled?: boolean;
}

const PRIORITY_OPTIONS: { value: Priority; label: string; color: string }[] = [
  { value: 'HIGH', label: 'High', color: 'bg-red-100 text-red-800 border-red-300' },
  { value: 'MEDIUM', label: 'Medium', color: 'bg-yellow-100 text-yellow-800 border-yellow-300' },
  { value: 'LOW', label: 'Low', color: 'bg-green-100 text-green-800 border-green-300' },
];

export const PrioritySelector: React.FC<PrioritySelectorProps> = ({ value, onChange, disabled = false }) => {
  return (
    <div className="flex gap-2">
      {PRIORITY_OPTIONS.map((option) => (
        <button
          key={option.value}
          type="button"
          onClick={() => onChange(option.value)}
          disabled={disabled}
          className={`
            px-3 py-1.5 rounded-md text-sm font-medium border transition-all
            ${value === option.value ? option.color : 'bg-gray-50 text-gray-700 border-gray-300'}
            ${disabled ? 'opacity-50 cursor-not-allowed' : 'hover:opacity-80 cursor-pointer'}
          `}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
};

export const PriorityBadge: React.FC<{ priority: Priority }> = ({ priority }) => {
  return (
    <span className={`px-2 py-1 rounded text-xs font-medium ${getPriorityColor(priority)}`}>
      {priority}
    </span>
  );
};
