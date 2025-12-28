import React, { useState, useEffect } from 'react';

export type Frequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';

export interface RecurrencePattern {
  frequency: Frequency;
  intervalValue: number;
  startDate: string;
  endDate?: string;
  daysOfWeek?: string[];
  dayOfMonth?: number;
  maxOccurrences?: number;
}

interface RecurrenceSelectorProps {
  value?: RecurrencePattern;
  onChange: (pattern: RecurrencePattern | undefined) => void;
}

const DAYS_OF_WEEK = [
  { value: 'MONDAY', label: 'Mon' },
  { value: 'TUESDAY', label: 'Tue' },
  { value: 'WEDNESDAY', label: 'Wed' },
  { value: 'THURSDAY', label: 'Thu' },
  { value: 'FRIDAY', label: 'Fri' },
  { value: 'SATURDAY', label: 'Sat' },
  { value: 'SUNDAY', label: 'Sun' },
];

const RecurrenceSelector: React.FC<RecurrenceSelectorProps> = ({ value, onChange }) => {
  const [enabled, setEnabled] = useState(!!value);
  const [frequency, setFrequency] = useState<Frequency>(value?.frequency || 'DAILY');
  const [intervalValue, setIntervalValue] = useState(value?.intervalValue || 1);
  const [startDate, setStartDate] = useState(value?.startDate || new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState(value?.endDate || '');
  const [daysOfWeek, setDaysOfWeek] = useState<string[]>(value?.daysOfWeek || []);
  const [dayOfMonth, setDayOfMonth] = useState(value?.dayOfMonth || 1);
  const [maxOccurrences, setMaxOccurrences] = useState<number | undefined>(value?.maxOccurrences);
  const [endType, setEndType] = useState<'never' | 'date' | 'count'>(
    value?.endDate ? 'date' : value?.maxOccurrences ? 'count' : 'never'
  );

  const handleEnabledChange = (isEnabled: boolean) => {
    setEnabled(isEnabled);
    if (!isEnabled) {
      onChange(undefined);
    } else {
      updatePattern();
    }
  };

  const updatePattern = () => {
    const pattern: RecurrencePattern = {
      frequency,
      intervalValue,
      startDate,
    };

    // Add end condition
    if (endType === 'date' && endDate) {
      pattern.endDate = endDate;
    } else if (endType === 'count' && maxOccurrences) {
      pattern.maxOccurrences = maxOccurrences;
    }

    // Add frequency-specific fields
    if (frequency === 'WEEKLY') {
      pattern.daysOfWeek = daysOfWeek.length > 0 ? daysOfWeek : ['MONDAY'];
    } else if (frequency === 'MONTHLY') {
      pattern.dayOfMonth = dayOfMonth;
    }

    onChange(pattern);
  };

  useEffect(() => {
    if (enabled) {
      updatePattern();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [frequency, intervalValue, startDate, endDate, daysOfWeek, dayOfMonth, maxOccurrences, endType, enabled]);

  const toggleDayOfWeek = (day: string) => {
    const newDays = daysOfWeek.includes(day)
      ? daysOfWeek.filter((d) => d !== day)
      : [...daysOfWeek, day];
    setDaysOfWeek(newDays);
  };

  return (
    <div className="space-y-4">
      {/* Enable/Disable Toggle */}
      <div className="flex items-center space-x-2">
        <input
          type="checkbox"
          id="recurrence-enabled"
          checked={enabled}
          onChange={(e) => handleEnabledChange(e.target.checked)}
          className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
        />
        <label htmlFor="recurrence-enabled" className="text-sm font-medium text-gray-700">
          Recurring task
        </label>
      </div>

      {enabled && (
        <>
          {/* Frequency Selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Repeat</label>
            <div className="flex space-x-2">
              <select
                value={frequency}
                onChange={(e) => setFrequency(e.target.value as Frequency)}
                className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
              >
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
              </select>

              <div className="flex items-center space-x-2">
                <label className="text-sm text-gray-700">Every</label>
                <input
                  type="number"
                  min="1"
                  value={intervalValue}
                  onChange={(e) => setIntervalValue(parseInt(e.target.value) || 1)}
                  className="w-20 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                />
                <span className="text-sm text-gray-700">
                  {frequency === 'DAILY' ? 'day(s)' : frequency === 'WEEKLY' ? 'week(s)' : 'month(s)'}
                </span>
              </div>
            </div>
          </div>

          {/* Weekly: Day of Week Selection */}
          {frequency === 'WEEKLY' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Repeat on</label>
              <div className="flex flex-wrap gap-2">
                {DAYS_OF_WEEK.map((day) => (
                  <button
                    key={day.value}
                    type="button"
                    onClick={() => toggleDayOfWeek(day.value)}
                    className={`px-3 py-2 rounded-md text-sm font-medium ${
                      daysOfWeek.includes(day.value)
                        ? 'bg-indigo-600 text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    {day.label}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Monthly: Day of Month */}
          {frequency === 'MONTHLY' && (
            <div>
              <label htmlFor="day-of-month" className="block text-sm font-medium text-gray-700 mb-2">
                Day of month
              </label>
              <input
                id="day-of-month"
                type="number"
                min="1"
                max="31"
                value={dayOfMonth}
                onChange={(e) => setDayOfMonth(parseInt(e.target.value) || 1)}
                className="w-24 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
              />
            </div>
          )}

          {/* Start Date */}
          <div>
            <label htmlFor="start-date" className="block text-sm font-medium text-gray-700 mb-2">
              Start date
            </label>
            <input
              id="start-date"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            />
          </div>

          {/* End Condition */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Ends</label>
            <div className="space-y-2">
              <div className="flex items-center">
                <input
                  type="radio"
                  id="end-never"
                  checked={endType === 'never'}
                  onChange={() => setEndType('never')}
                  className="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-500"
                />
                <label htmlFor="end-never" className="ml-2 text-sm text-gray-700">
                  Never
                </label>
              </div>

              <div className="flex items-center space-x-2">
                <input
                  type="radio"
                  id="end-date"
                  checked={endType === 'date'}
                  onChange={() => setEndType('date')}
                  className="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-500"
                />
                <label htmlFor="end-date" className="text-sm text-gray-700">
                  On
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => {
                    setEndDate(e.target.value);
                    setEndType('date');
                  }}
                  disabled={endType !== 'date'}
                  className="rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm disabled:bg-gray-100"
                />
              </div>

              <div className="flex items-center space-x-2">
                <input
                  type="radio"
                  id="end-count"
                  checked={endType === 'count'}
                  onChange={() => setEndType('count')}
                  className="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-500"
                />
                <label htmlFor="end-count" className="text-sm text-gray-700">
                  After
                </label>
                <input
                  type="number"
                  min="1"
                  value={maxOccurrences || ''}
                  onChange={(e) => {
                    setMaxOccurrences(parseInt(e.target.value) || undefined);
                    setEndType('count');
                  }}
                  disabled={endType !== 'count'}
                  className="w-20 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm disabled:bg-gray-100"
                />
                <span className="text-sm text-gray-700">occurrence(s)</span>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default RecurrenceSelector;
