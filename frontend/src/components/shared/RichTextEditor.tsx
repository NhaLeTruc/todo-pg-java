import React, { useState, useCallback } from 'react';

import { Bold, Italic, List, ListOrdered, Link } from 'lucide-react';

interface RichTextEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
  minHeight?: string;
}

const RichTextEditor: React.FC<RichTextEditorProps> = ({
  value,
  onChange,
  placeholder = 'Enter description...',
  disabled = false,
  minHeight = '150px',
}) => {
  const [showPreview, setShowPreview] = useState(false);

  const insertMarkdown = useCallback(
    (before: string, after: string = '') => {
      const textarea = document.getElementById('rich-text-input') as HTMLTextAreaElement;
      if (!textarea) return;

      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const selectedText = value.substring(start, end);
      const newText =
        value.substring(0, start) + before + selectedText + after + value.substring(end);

      onChange(newText);

      // Restore cursor position
      setTimeout(() => {
        textarea.focus();
        const newCursorPos = start + before.length + selectedText.length;
        textarea.setSelectionRange(newCursorPos, newCursorPos);
      }, 0);
    },
    [value, onChange]
  );

  const handleBold = () => insertMarkdown('**', '**');
  const handleItalic = () => insertMarkdown('*', '*');
  const handleBulletList = () => {
    const textarea = document.getElementById('rich-text-input') as HTMLTextAreaElement;
    const start = textarea.selectionStart;
    const lineStart = value.lastIndexOf('\n', start - 1) + 1;
    onChange(value.substring(0, lineStart) + '- ' + value.substring(lineStart));
  };
  const handleNumberedList = () => {
    const textarea = document.getElementById('rich-text-input') as HTMLTextAreaElement;
    const start = textarea.selectionStart;
    const lineStart = value.lastIndexOf('\n', start - 1) + 1;
    onChange(value.substring(0, lineStart) + '1. ' + value.substring(lineStart));
  };
  const handleLink = () => insertMarkdown('[', '](url)');

  const renderMarkdown = (text: string): string => {
    let html = text;

    // Convert bold
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

    // Convert italic
    html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');

    // Convert links
    html = html.replace(
      /\[(.*?)\]\((.*?)\)/g,
      '<a href="$2" class="text-blue-600 underline">$1</a>'
    );

    // Convert bullet lists
    html = html.replace(/^- (.*)$/gm, '<li>$1</li>');
    html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul class="list-disc list-inside">$&</ul>');

    // Convert numbered lists
    html = html.replace(/^\d+\. (.*)$/gm, '<li>$1</li>');
    html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ol class="list-decimal list-inside">$&</ol>');

    // Convert line breaks
    html = html.replace(/\n/g, '<br />');

    return html;
  };

  return (
    <div className="overflow-hidden rounded-lg border border-gray-300">
      {/* Toolbar */}
      <div className="flex items-center space-x-1 border-b border-gray-300 bg-gray-50 px-2 py-1">
        <button
          type="button"
          onClick={handleBold}
          disabled={disabled}
          className="rounded p-2 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-50"
          title="Bold (Ctrl+B)"
        >
          <Bold className="h-4 w-4" />
        </button>
        <button
          type="button"
          onClick={handleItalic}
          disabled={disabled}
          className="rounded p-2 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-50"
          title="Italic (Ctrl+I)"
        >
          <Italic className="h-4 w-4" />
        </button>
        <div className="mx-1 h-6 w-px bg-gray-300"></div>
        <button
          type="button"
          onClick={handleBulletList}
          disabled={disabled}
          className="rounded p-2 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-50"
          title="Bullet List"
        >
          <List className="h-4 w-4" />
        </button>
        <button
          type="button"
          onClick={handleNumberedList}
          disabled={disabled}
          className="rounded p-2 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-50"
          title="Numbered List"
        >
          <ListOrdered className="h-4 w-4" />
        </button>
        <div className="mx-1 h-6 w-px bg-gray-300"></div>
        <button
          type="button"
          onClick={handleLink}
          disabled={disabled}
          className="rounded p-2 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-50"
          title="Insert Link"
        >
          <Link className="h-4 w-4" />
        </button>
        <div className="flex-1"></div>
        <button
          type="button"
          onClick={() => setShowPreview(!showPreview)}
          disabled={disabled}
          className="rounded px-3 py-1 text-sm text-gray-700 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {showPreview ? 'Edit' : 'Preview'}
        </button>
      </div>

      {/* Editor / Preview */}
      {showPreview ? (
        <div
          className="prose prose-sm max-w-none p-3"
          style={{ minHeight }}
          dangerouslySetInnerHTML={{ __html: renderMarkdown(value) }}
        />
      ) : (
        <textarea
          id="rich-text-input"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          disabled={disabled}
          className="w-full resize-none p-3 focus:outline-none disabled:cursor-not-allowed disabled:bg-gray-50"
          style={{ minHeight }}
        />
      )}

      {/* Hint */}
      <div className="border-t border-gray-300 bg-gray-50 px-3 py-1 text-xs text-gray-500">
        Supports Markdown: **bold**, *italic*, [link](url), - bullet list, 1. numbered list
      </div>
    </div>
  );
};

export default RichTextEditor;
