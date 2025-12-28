import React from 'react';

import {
  Download,
  Trash2,
  File,
  Image,
  FileText,
  AlertTriangle,
  Clock,
  CheckCircle,
  XCircle,
} from 'lucide-react';

interface FileAttachment {
  id: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  scanStatus: 'PENDING' | 'SCANNING' | 'CLEAN' | 'INFECTED' | 'SCAN_FAILED';
  downloadable: boolean;
  createdAt: string;
  scannedAt?: string;
}

interface AttachmentListProps {
  attachments: FileAttachment[];
  onDownload: (id: string, fileName: string) => void;
  onDelete: (id: string) => void;
  isLoading?: boolean;
}

const AttachmentList: React.FC<AttachmentListProps> = ({
  attachments,
  onDownload,
  onDelete,
  isLoading = false,
}) => {
  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const getFileIcon = (mimeType: string) => {
    if (mimeType.startsWith('image/')) {
      return <Image className="h-8 w-8 text-blue-500" />;
    } else if (mimeType.includes('pdf')) {
      return <FileText className="h-8 w-8 text-red-500" />;
    } else if (
      mimeType.includes('word') ||
      mimeType.includes('document') ||
      mimeType.includes('text')
    ) {
      return <FileText className="h-8 w-8 text-blue-600" />;
    } else if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) {
      return <FileText className="h-8 w-8 text-green-600" />;
    }
    return <File className="h-8 w-8 text-gray-500" />;
  };

  const getScanStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="inline-flex items-center rounded bg-yellow-100 px-2 py-1 text-xs font-medium text-yellow-700">
            <Clock className="mr-1 h-3 w-3" />
            Pending Scan
          </span>
        );
      case 'SCANNING':
        return (
          <span className="inline-flex items-center rounded bg-blue-100 px-2 py-1 text-xs font-medium text-blue-700">
            <Clock className="mr-1 h-3 w-3 animate-spin" />
            Scanning...
          </span>
        );
      case 'CLEAN':
        return (
          <span className="inline-flex items-center rounded bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
            <CheckCircle className="mr-1 h-3 w-3" />
            Clean
          </span>
        );
      case 'INFECTED':
        return (
          <span className="inline-flex items-center rounded bg-red-100 px-2 py-1 text-xs font-medium text-red-700">
            <XCircle className="mr-1 h-3 w-3" />
            Infected
          </span>
        );
      case 'SCAN_FAILED':
        return (
          <span className="inline-flex items-center rounded bg-orange-100 px-2 py-1 text-xs font-medium text-orange-700">
            <AlertTriangle className="mr-1 h-3 w-3" />
            Scan Failed
          </span>
        );
      default:
        return null;
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return (
      date.toLocaleDateString() +
      ' ' +
      date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    );
  };

  if (isLoading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3].map((i) => (
          <div key={i} className="animate-pulse rounded-lg border border-gray-200 p-4">
            <div className="flex items-center space-x-4">
              <div className="h-8 w-8 rounded bg-gray-200"></div>
              <div className="flex-1 space-y-2">
                <div className="h-4 w-3/4 rounded bg-gray-200"></div>
                <div className="h-3 w-1/2 rounded bg-gray-200"></div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (attachments.length === 0) {
    return (
      <div className="py-8 text-center text-gray-500">
        <File className="mx-auto mb-2 h-12 w-12 text-gray-400" />
        <p>No attachments yet</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {attachments.map((attachment) => (
        <div
          key={attachment.id}
          className="rounded-lg border border-gray-200 p-4 transition-shadow hover:shadow-md"
        >
          <div className="flex items-start space-x-4">
            <div className="flex-shrink-0">{getFileIcon(attachment.mimeType)}</div>
            <div className="min-w-0 flex-1">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <p className="truncate text-sm font-medium text-gray-900">
                    {attachment.fileName}
                  </p>
                  <div className="mt-1 flex items-center space-x-2">
                    <p className="text-xs text-gray-500">{formatFileSize(attachment.fileSize)}</p>
                    <span className="text-gray-300">•</span>
                    <p className="text-xs text-gray-500">{formatDate(attachment.createdAt)}</p>
                  </div>
                  <div className="mt-2">{getScanStatusBadge(attachment.scanStatus)}</div>
                </div>
                <div className="ml-4 flex items-center space-x-2">
                  <button
                    onClick={() => onDownload(attachment.id, attachment.fileName)}
                    disabled={!attachment.downloadable}
                    className="rounded-lg p-2 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
                    title={
                      attachment.downloadable ? 'Download file' : 'File not available for download'
                    }
                  >
                    <Download className="h-5 w-5 text-blue-600" />
                  </button>
                  <button
                    onClick={() => onDelete(attachment.id)}
                    className="rounded-lg p-2 transition-colors hover:bg-red-50"
                    title="Delete attachment"
                  >
                    <Trash2 className="h-5 w-5 text-red-600" />
                  </button>
                </div>
              </div>
              {attachment.scanStatus === 'INFECTED' && (
                <div className="mt-2 rounded border border-red-200 bg-red-50 p-2 text-xs text-red-700">
                  <AlertTriangle className="mr-1 inline h-4 w-4" />
                  This file has been flagged as potentially dangerous and cannot be downloaded.
                </div>
              )}
              {attachment.scanStatus === 'SCAN_FAILED' && attachment.downloadable && (
                <div className="mt-2 rounded border border-orange-200 bg-orange-50 p-2 text-xs text-orange-700">
                  <AlertTriangle className="mr-1 inline h-4 w-4" />
                  Virus scan failed. Download at your own risk.
                </div>
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default AttachmentList;
