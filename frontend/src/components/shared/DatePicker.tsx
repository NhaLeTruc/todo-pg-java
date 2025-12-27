import React from 'react';

interface DatePickerProps {
  value: string | null;
  onChange: (date: string | null) => void;
  label?: string;
  disabled?: boolean;
  minDate?: string;
  placeholder?: string;
}

export const DatePicker: React.FC<DatePickerProps> = ({
  value,
  onChange,
  label,
  disabled = false,
  minDate,
  placeholder = 'Select date and time',
}) => {
  const handleClear = () => {
    onChange(null);
  };

  // Convert ISO string to datetime-local format
  const formatForInput = (dateString: string | null): string => {
    if (!dateString) return '';
    try {
      // Remove timezone and seconds if present
      return dateString.slice(0, 16);
    } catch {
      return '';
    }
  };

  // Convert datetime-local to ISO string
  const formatForSubmit = (localDatetime: string): string => {
    if (!localDatetime) return '';
    try {
      // Append seconds and convert to ISO format
      return new Date(localDatetime).toISOString();
    } catch {
      return localDatetime;
    }
  };

  return (
    <div className="flex flex-col gap-1">
      {label && <label className="text-sm font-medium text-gray-700">{label}</label>}
      <div className="flex gap-2 items-center">
        <input
          type="datetime-local"
          value={formatForInput(value)}
          onChange={(e) => {
            const formatted = formatForSubmit(e.target.value);
            onChange(formatted || null);
          }}
          disabled={disabled}
          min={minDate}
          placeholder={placeholder}
          className="
            flex-1 px-3 py-2 border border-gray-300 rounded-md shadow-sm
            focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500
            disabled:bg-gray-100 disabled:cursor-not-allowed
            text-sm
          "
        />
        {value && !disabled && (
          <button
            type="button"
            onClick={handleClear}
            className="
              px-3 py-2 text-sm font-medium text-gray-700 bg-gray-100
              border border-gray-300 rounded-md hover:bg-gray-200
              focus:outline-none focus:ring-2 focus:ring-gray-500
            "
          >
            Clear
          </button>
        )}
      </div>
    </div>
  );
};
