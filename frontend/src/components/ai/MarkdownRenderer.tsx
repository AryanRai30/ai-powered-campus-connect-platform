import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface MarkdownRendererProps {
  content: string;
  className?: string;
  isUser?: boolean;
}

/**
 * Normalizes content by unescaping backslash-escaped markdown characters if present
 * (e.g. \*\*bold\*\* -> **bold**, \### header -> ### header, \* item -> * item)
 */
export const preprocessMarkdown = (raw: string): string => {
  if (!raw) return '';
  return raw.replace(/\\([\*\#\-\_\`\[\]\(\)\~\>\|])/g, '$1');
};

export const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({
  content,
  className = '',
  isUser = false,
}) => {
  const processedText = preprocessMarkdown(content);

  return (
    <div className={`markdown-body ${className} ${isUser ? 'text-white' : 'text-slate-800'}`}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          h1: ({ children }) => (
            <h1
              className={`text-base font-extrabold mt-3 mb-1.5 first:mt-0 pb-1 border-b ${
                isUser ? 'text-white border-white/20' : 'text-slate-900 border-slate-200'
              }`}
            >
              {children}
            </h1>
          ),
          h2: ({ children }) => (
            <h2
              className={`text-sm font-bold mt-2.5 mb-1 first:mt-0 ${
                isUser ? 'text-white' : 'text-slate-900'
              }`}
            >
              {children}
            </h2>
          ),
          h3: ({ children }) => (
            <h3
              className={`text-xs font-bold mt-2 mb-1 first:mt-0 ${
                isUser ? 'text-white' : 'text-slate-900'
              }`}
            >
              {children}
            </h3>
          ),
          h4: ({ children }) => (
            <h4
              className={`text-xs font-semibold mt-2 mb-1 first:mt-0 ${
                isUser ? 'text-white' : 'text-slate-800'
              }`}
            >
              {children}
            </h4>
          ),
          p: ({ children }) => (
            <p className="leading-relaxed mb-2 last:mb-0 whitespace-pre-wrap break-words">
              {children}
            </p>
          ),
          strong: ({ children }) => (
            <strong className={`font-bold ${isUser ? 'text-white' : 'text-slate-900'}`}>
              {children}
            </strong>
          ),
          em: ({ children }) => <em className="italic">{children}</em>,
          ul: ({ children }) => (
            <ul className="list-disc pl-5 space-y-1 my-2 leading-relaxed">{children}</ul>
          ),
          ol: ({ children }) => (
            <ol className="list-decimal pl-5 space-y-1 my-2 leading-relaxed">{children}</ol>
          ),
          li: ({ children }) => <li className="leading-relaxed">{children}</li>,
          code: ({ inline, className, children, ...props }: any) => {
            if (inline) {
              return (
                <code
                  className={`font-mono text-[0.85em] px-1.5 py-0.5 rounded border break-words ${
                    isUser
                      ? 'bg-white/20 text-white border-white/30 font-semibold'
                      : 'bg-slate-100 text-brand-700 border-slate-200/80 font-semibold'
                  }`}
                  {...props}
                >
                  {children}
                </code>
              );
            }
            return (
              <code className="font-mono text-xs leading-relaxed block" {...props}>
                {children}
              </code>
            );
          },
          pre: ({ children }) => (
            <pre
              className={`p-3.5 rounded-xl font-mono text-xs overflow-x-auto my-2.5 border shadow-inner ${
                isUser
                  ? 'bg-slate-950/80 text-slate-100 border-white/20'
                  : 'bg-slate-900 text-slate-100 border-slate-800'
              }`}
            >
              {children}
            </pre>
          ),
          a: ({ href, children }) => {
            const isSafe =
              href &&
              (href.startsWith('http://') ||
                href.startsWith('https://') ||
                href.startsWith('/') ||
                href.startsWith('#') ||
                href.startsWith('mailto:'));
            return (
              <a
                href={isSafe ? href : '#'}
                target="_blank"
                rel="noopener noreferrer"
                className={`underline underline-offset-2 font-semibold transition-colors ${
                  isUser
                    ? 'text-white hover:text-slate-200'
                    : 'text-brand-600 hover:text-brand-700'
                }`}
              >
                {children}
              </a>
            );
          },
          hr: () => (
            <hr className={`my-3 border-t ${isUser ? 'border-white/20' : 'border-slate-200'}`} />
          ),
          blockquote: ({ children }) => (
            <blockquote
              className={`border-l-4 pl-3 py-1 italic rounded-r my-2 ${
                isUser
                  ? 'border-white/60 bg-white/10 text-slate-100'
                  : 'border-brand-500 bg-slate-50 text-slate-700'
              }`}
            >
              {children}
            </blockquote>
          ),
          table: ({ children }) => (
            <div className="overflow-x-auto my-2">
              <table className="w-full border-collapse text-xs text-left">{children}</table>
            </div>
          ),
          th: ({ children }) => (
            <th
              className={`font-bold p-2 border ${
                isUser
                  ? 'bg-white/10 border-white/20'
                  : 'bg-slate-100 border-slate-200 text-slate-800'
              }`}
            >
              {children}
            </th>
          ),
          td: ({ children }) => (
            <td className={`p-2 border ${isUser ? 'border-white/20' : 'border-slate-200'}`}>
              {children}
            </td>
          ),
        }}
      >
        {processedText}
      </ReactMarkdown>
    </div>
  );
};

export default MarkdownRenderer;
