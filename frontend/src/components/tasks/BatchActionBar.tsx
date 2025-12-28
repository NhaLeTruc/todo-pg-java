import { useState } from 'react';

import { Check, Trash2, Tag, Folder, X } from 'lucide-react';
import { toast } from 'react-hot-toast';

interface BatchActionBarProps {
  selectedTaskIds: number[];
  onClearSelection: () => void;
  onOperationComplete: () => void;
  availableCategories?: Array<{ id: number; name: string; color?: string }>;
  availableTags?: Array<{ id: number; name: string; color?: string }>;
}

type Operation = 'COMPLETE' | 'DELETE' | 'ASSIGN_CATEGORY' | 'ASSIGN_TAGS';

const BatchActionBar: React.FC<BatchActionBarProps> = ({
  selectedTaskIds,
  onClearSelection,
  onOperationComplete,
  availableCategories = [],
  availableTags = [],
}) => {
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [pendingOperation, setPendingOperation] = useState<Operation | null>(null);
  const [showCategoryPicker, setShowCategoryPicker] = useState(false);
  const [showTagPicker, setShowTagPicker] = useState(false);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const handleBatchOperation = async (operation: Operation) => {
    setIsLoading(true);

    try {
      const requestBody: {
        taskIds: number[];
        operationType: Operation;
        categoryId?: number;
        tagIds?: number[];
      } = {
        taskIds: selectedTaskIds,
        operationType: operation,
      };

      if (operation === 'ASSIGN_CATEGORY' && selectedCategoryId !== null) {
        requestBody.categoryId = selectedCategoryId;
      }

      if (operation === 'ASSIGN_TAGS') {
        requestBody.tagIds = selectedTagIds;
      }

      const response = await fetch('/api/v1/tasks/batch', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        throw new Error('Batch operation failed');
      }

      toast.success(
        `Successfully ${operation.toLowerCase().replace('_', ' ')} ${selectedTaskIds.length} task(s)`
      );
      onOperationComplete();
      onClearSelection();
      setShowConfirmDialog(false);
      setShowCategoryPicker(false);
      setShowTagPicker(false);
      setSelectedCategoryId(null);
      setSelectedTagIds([]);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Batch operation failed');
    } finally {
      setIsLoading(false);
    }
  };

  const requestOperation = (operation: Operation) => {
    if (operation === 'ASSIGN_CATEGORY') {
      setShowCategoryPicker(true);
    } else if (operation === 'ASSIGN_TAGS') {
      setShowTagPicker(true);
    } else {
      setPendingOperation(operation);
      setShowConfirmDialog(true);
    }
  };

  const confirmOperation = () => {
    if (pendingOperation) {
      handleBatchOperation(pendingOperation);
    }
  };

  const handleCategorySelect = (categoryId: number) => {
    setSelectedCategoryId(categoryId);
    setPendingOperation('ASSIGN_CATEGORY');
    setShowCategoryPicker(false);
    setShowConfirmDialog(true);
  };

  const handleTagToggle = (tagId: number) => {
    setSelectedTagIds((prev) =>
      prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId]
    );
  };

  const handleTagsConfirm = () => {
    setPendingOperation('ASSIGN_TAGS');
    setShowTagPicker(false);
    setShowConfirmDialog(true);
  };

  const getOperationMessage = () => {
    switch (pendingOperation) {
      case 'COMPLETE':
        return `Mark ${selectedTaskIds.length} task(s) as completed?`;
      case 'DELETE':
        return `Permanently delete ${selectedTaskIds.length} task(s)?`;
      case 'ASSIGN_CATEGORY': {
        const category = availableCategories.find((c) => c.id === selectedCategoryId);
        return `Assign category "${category?.name}" to ${selectedTaskIds.length} task(s)?`;
      }
      case 'ASSIGN_TAGS': {
        const tagNames = availableTags
          .filter((t) => selectedTagIds.includes(t.id))
          .map((t) => t.name)
          .join(', ');
        return `Assign tags "${tagNames}" to ${selectedTaskIds.length} task(s)?`;
      }
      default:
        return '';
    }
  };

  if (selectedTaskIds.length === 0) {
    return null;
  }

  return (
    <>
      <div className="fixed bottom-6 left-1/2 z-50 -translate-x-1/2 rounded-lg border border-gray-200 bg-white shadow-xl">
        <div className="flex items-center gap-2 p-4">
          <span className="text-sm font-medium text-gray-700">
            {selectedTaskIds.length} selected
          </span>

          <div className="mx-2 h-6 w-px bg-gray-300"></div>

          <button
            onClick={() => requestOperation('COMPLETE')}
            disabled={isLoading}
            className="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-green-700 hover:bg-green-50 disabled:opacity-50"
            title="Mark as complete"
          >
            <Check className="h-4 w-4" />
            Complete
          </button>

          <button
            onClick={() => requestOperation('DELETE')}
            disabled={isLoading}
            className="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-red-700 hover:bg-red-50 disabled:opacity-50"
            title="Delete tasks"
          >
            <Trash2 className="h-4 w-4" />
            Delete
          </button>

          {availableCategories.length > 0 && (
            <button
              onClick={() => requestOperation('ASSIGN_CATEGORY')}
              disabled={isLoading}
              className="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-purple-700 hover:bg-purple-50 disabled:opacity-50"
              title="Assign category"
            >
              <Folder className="h-4 w-4" />
              Category
            </button>
          )}

          {availableTags.length > 0 && (
            <button
              onClick={() => requestOperation('ASSIGN_TAGS')}
              disabled={isLoading}
              className="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-blue-700 hover:bg-blue-50 disabled:opacity-50"
              title="Assign tags"
            >
              <Tag className="h-4 w-4" />
              Tags
            </button>
          )}

          <div className="mx-2 h-6 w-px bg-gray-300"></div>

          <button
            onClick={onClearSelection}
            disabled={isLoading}
            className="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 disabled:opacity-50"
            title="Clear selection"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* Category Picker Dialog */}
      {showCategoryPicker && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h3 className="mb-4 text-lg font-semibold text-gray-900">Select Category</h3>
            <div className="space-y-2">
              {availableCategories.map((category) => (
                <button
                  key={category.id}
                  onClick={() => handleCategorySelect(category.id)}
                  className="w-full rounded-md border border-gray-200 px-4 py-2 text-left hover:bg-gray-50"
                  style={
                    category.color
                      ? { borderLeftColor: category.color, borderLeftWidth: '4px' }
                      : undefined
                  }
                >
                  {category.name}
                </button>
              ))}
            </div>
            <div className="mt-4 flex justify-end">
              <button
                onClick={() => setShowCategoryPicker(false)}
                className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Tag Picker Dialog */}
      {showTagPicker && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h3 className="mb-4 text-lg font-semibold text-gray-900">Select Tags</h3>
            <div className="space-y-2">
              {availableTags.map((tag) => (
                <label
                  key={tag.id}
                  className="flex items-center gap-3 rounded-md border border-gray-200 px-4 py-2 hover:bg-gray-50"
                >
                  <input
                    type="checkbox"
                    checked={selectedTagIds.includes(tag.id)}
                    onChange={() => handleTagToggle(tag.id)}
                    className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span
                    className="flex-1"
                    style={tag.color ? { color: tag.color, fontWeight: 500 } : undefined}
                  >
                    {tag.name}
                  </span>
                </label>
              ))}
            </div>
            <div className="mt-4 flex justify-end gap-2">
              <button
                onClick={() => {
                  setShowTagPicker(false);
                  setSelectedTagIds([]);
                }}
                className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
              >
                Cancel
              </button>
              <button
                onClick={handleTagsConfirm}
                disabled={selectedTagIds.length === 0}
                className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:bg-indigo-400"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Dialog */}
      {showConfirmDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h3 className="mb-4 text-lg font-semibold text-gray-900">Confirm Action</h3>
            <p className="mb-6 text-gray-600">{getOperationMessage()}</p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => {
                  setShowConfirmDialog(false);
                  setPendingOperation(null);
                }}
                disabled={isLoading}
                className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={confirmOperation}
                disabled={isLoading}
                className={`rounded-md px-4 py-2 text-sm font-medium text-white ${
                  pendingOperation === 'DELETE'
                    ? 'bg-red-600 hover:bg-red-700'
                    : 'bg-indigo-600 hover:bg-indigo-700'
                } disabled:opacity-50`}
              >
                {isLoading ? 'Processing...' : 'Confirm'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default BatchActionBar;
