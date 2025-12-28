import React, { useCallback, useState } from 'react';

import { Upload, X, File, AlertCircle } from 'lucide-react';

interface FileUploadProps {
  onFileSelect: (file: File) => void;
  onFileRemove?: () => void;
  maxSize?: number; // in bytes
  acceptedFileTypes?: string[];
  disabled?: boolean;
  selectedFile?: File | null;
}

const FileUpload: React.FC<FileUploadProps> = ({
  onFileSelect,
  onFileRemove,
  maxSize = 25 * 1024 * 1024, // 25MB default
  acceptedFileTypes = [
    'image/jpeg',
    'image/png',
    'image/gif',
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain',
  ],
  disabled = false,
  selectedFile = null,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const validateFile = useCallback(
    (file: File): string | null => {
      // Check file size
      if (file.size > maxSize) {
        return `File size exceeds maximum allowed size of ${(maxSize / (1024 * 1024)).toFixed(0)}MB`;
      }

      // Check file type
      if (acceptedFileTypes.length > 0 && !acceptedFileTypes.includes(file.type)) {
        return 'File type not supported';
      }

      // Check for path traversal in filename
      if (file.name.includes('..') || file.name.includes('/') || file.name.includes('\\')) {
        return 'Invalid file name';
      }

      return null;
    },
    [maxSize, acceptedFileTypes]
  );

  const handleFile = useCallback(
    (file: File) => {
      const validationError = validateFile(file);
      if (validationError) {
        setError(validationError);
        return;
      }

      setError(null);
      onFileSelect(file);
    },
    [validateFile, onFileSelect]
  );

  const handleDrop = useCallback(
    (e: React.DragEvent<HTMLDivElement>) => {
      e.preventDefault();
      setIsDragging(false);

      if (disabled) return;

      const files = Array.from(e.dataTransfer.files);
      if (files.length > 0) {
        handleFile(files[0]);
      }
    },
    [disabled, handleFile]
  );

  const handleDragOver = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  }, []);

  const handleFileInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (files && files.length > 0) {
        handleFile(files[0]);
      }
    },
    [handleFile]
  );

  const handleRemove = useCallback(() => {
    setError(null);
    if (onFileRemove) {
      onFileRemove();
    }
  }, [onFileRemove]);

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const getFileIcon = (file: File) => {
    if (file.type.startsWith('image/')) {
      return (
        <img
          src={URL.createObjectURL(file)}
          alt={file.name}
          className="h-16 w-16 rounded object-cover"
        />
      );
    }
    return <File className="h-16 w-16 text-gray-400" />;
  };

  const getAcceptString = () => {
    return acceptedFileTypes.join(',');
  };

  return (
    <div className="w-full">
      {selectedFile ? (
        <div className="rounded-lg border-2 border-gray-300 bg-white p-4">
          <div className="flex items-center space-x-4">
            {getFileIcon(selectedFile)}
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium text-gray-900">{selectedFile.name}</p>
              <p className="text-sm text-gray-500">{formatFileSize(selectedFile.size)}</p>
            </div>
            <button
              type="button"
              onClick={handleRemove}
              disabled={disabled}
              className="rounded-full p-2 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
              aria-label="Remove file"
            >
              <X className="h-5 w-5 text-gray-500" />
            </button>
          </div>
        </div>
      ) : (
        <div
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          className={`rounded-lg border-2 border-dashed p-8 text-center transition-colors ${
            isDragging
              ? 'border-blue-500 bg-blue-50'
              : 'border-gray-300 bg-white hover:border-gray-400'
          } ${disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'}`}
        >
          <input
            type="file"
            onChange={handleFileInputChange}
            accept={getAcceptString()}
            disabled={disabled}
            className="hidden"
            id="file-upload-input"
          />
          <label
            htmlFor="file-upload-input"
            className={`flex flex-col items-center ${
              disabled ? 'cursor-not-allowed' : 'cursor-pointer'
            }`}
          >
            <Upload
              className={`mb-3 h-12 w-12 ${isDragging ? 'text-blue-500' : 'text-gray-400'}`}
            />
            <p className="mb-1 text-sm font-medium text-gray-900">
              Drop your file here or click to browse
            </p>
            <p className="text-xs text-gray-500">
              Maximum file size: {(maxSize / (1024 * 1024)).toFixed(0)}MB
            </p>
          </label>
        </div>
      )}

      {error && (
        <div className="mt-2 flex items-center text-sm text-red-600">
          <AlertCircle className="mr-1 h-4 w-4" />
          {error}
        </div>
      )}
    </div>
  );
};

export default FileUpload;
