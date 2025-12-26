import { useState } from 'react';

import { Plus } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { Priority, TaskCreateRequest } from '@/types/task';

interface TaskFormProps {
  onSubmit: (data: TaskCreateRequest) => Promise<void>;
  isLoading?: boolean;
}

const MIN_DESCRIPTION_LENGTH = 1;
const MAX_DESCRIPTION_LENGTH = 5000;

export function TaskForm({ onSubmit, isLoading = false }: TaskFormProps) {
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [validationError, setValidationError] = useState<string | null>(null);
  const [touched, setTouched] = useState(false);

  const validateDescription = (value: string): string | null => {
    const trimmed = value.trim();

    if (trimmed.length === 0) {
      return 'Task description cannot be empty';
    }

    if (trimmed.length < MIN_DESCRIPTION_LENGTH) {
      return `Description must be at least ${MIN_DESCRIPTION_LENGTH} character`;
    }

    if (value.length > MAX_DESCRIPTION_LENGTH) {
      return `Description cannot exceed ${MAX_DESCRIPTION_LENGTH} characters`;
    }

    return null;
  };

  const handleDescriptionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setDescription(value);

    if (touched) {
      setValidationError(validateDescription(value));
    }
  };

  const handleDescriptionBlur = () => {
    setTouched(true);
    setValidationError(validateDescription(description));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched(true);

    const error = validateDescription(description);
    if (error) {
      setValidationError(error);
      toast.error(error);
      return;
    }

    try {
      await onSubmit({
        description: description.trim(),
        priority,
      });

      setDescription('');
      setPriority('MEDIUM');
      setValidationError(null);
      setTouched(false);
    } catch (error) {
      console.error('Failed to create task:', error);
    }
  };

  const isDescriptionValid = !validationError && description.trim().length > 0;
  const showError = touched && validationError;

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <div className="flex gap-3">
        <div className="flex-1">
          <input
            type="text"
            value={description}
            onChange={handleDescriptionChange}
            onBlur={handleDescriptionBlur}
            placeholder="What needs to be done?"
            className={`input w-full ${showError ? 'border-red-500 focus:border-red-500 focus:ring-red-200' : ''}`}
            disabled={isLoading}
            autoFocus
            maxLength={MAX_DESCRIPTION_LENGTH}
            aria-invalid={showError ? 'true' : 'false'}
            aria-describedby={showError ? 'description-error' : undefined}
          />
        </div>

        <select
          value={priority}
          onChange={(e) => setPriority(e.target.value as Priority)}
          className="input w-32"
          disabled={isLoading}
        >
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>

        <button type="submit" className="btn-primary" disabled={isLoading || !isDescriptionValid}>
          <Plus className="h-5 w-5" />
          <span className="hidden sm:inline">Add Task</span>
        </button>
      </div>

      {showError && (
        <p id="description-error" className="text-sm text-red-600" role="alert">
          {validationError}
        </p>
      )}

      {!showError && description.trim() && (
        <p className="text-sm text-gray-500">
          Press Enter or click &quot;Add Task&quot; to create this task
        </p>
      )}
    </form>
  );
}
