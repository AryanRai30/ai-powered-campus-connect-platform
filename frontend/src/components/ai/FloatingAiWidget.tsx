import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { sendAiChatMessage, getAiErrorMessage } from '../../services/aiService';
import { MarkdownRenderer } from './MarkdownRenderer';
import {
  SparklesIcon,
  XIcon,
  SendIcon,
  Maximize2Icon,
  BotIcon,
  AlertCircleIcon,
} from '../common/Icons';

interface ChatMessage {
  id: string;
  sender: 'user' | 'ai';
  text: string;
  timestamp: string;
}

export const FloatingAiWidget: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome-1',
      sender: 'ai',
      text: 'Hello! I am your Campus AI Assistant. Ask me anything about campus life, courses, or events!',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen, loading]);

  if (!isAuthenticated) {
    return null;
  }

  const handleSend = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const prompt = inputPrompt.trim();
    if (!prompt || loading) return;

    const userMessage: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: prompt,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputPrompt('');
    setLoading(true);
    setError(null);

    try {
      const res = await sendAiChatMessage(prompt);
      const aiMessage: ChatMessage = {
        id: `ai-${Date.now()}`,
        sender: 'ai',
        text: res.response || 'No response generated.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, aiMessage]);
    } catch (err: any) {
      setError(getAiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleOpenFullPage = () => {
    setIsOpen(false);
    navigate('/ai-assistant');
  };

  return (
    <div className="fixed bottom-6 right-6 z-50 select-none">
      {/* Expanded Widget Popover Window */}
      {isOpen && (
        <div className="mb-4 w-[calc(100vw-3rem)] sm:w-96 h-[480px] bg-white rounded-2xl border border-slate-200/90 shadow-2xl flex flex-col overflow-hidden animate-slide-up">
          {/* Header */}
          <div className="p-3.5 bg-gradient-to-r from-slate-900 via-indigo-950 to-brand-950 text-white flex items-center justify-between shrink-0">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-brand-500/20 border border-brand-400/40 flex items-center justify-center text-brand-300">
                <SparklesIcon size={18} />
              </div>
              <div className="flex flex-col">
                <div className="flex items-center gap-1.5">
                  <span className="font-bold text-sm tracking-tight">AI Assistant</span>
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                </div>
                <span className="text-[10px] text-slate-300 font-medium">Gemini 3.6 Flash</span>
              </div>
            </div>

            <div className="flex items-center gap-1">
              <button
                onClick={handleOpenFullPage}
                className="p-1.5 text-slate-300 hover:text-white hover:bg-white/10 rounded-lg transition-colors"
                title="Expand to Full Page"
              >
                <Maximize2Icon size={16} />
              </button>
              <button
                onClick={() => setIsOpen(false)}
                className="p-1.5 text-slate-300 hover:text-white hover:bg-white/10 rounded-lg transition-colors"
                title="Close"
              >
                <XIcon size={18} />
              </button>
            </div>
          </div>

          {/* Chat Messages Body */}
          <div className="flex-1 p-3.5 overflow-y-auto space-y-3 bg-slate-50/50">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex gap-2.5 ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                {msg.sender === 'ai' && (
                  <div className="w-7 h-7 rounded-xl bg-brand-600 text-white flex items-center justify-center shrink-0 shadow-xs mt-0.5">
                    <BotIcon size={15} />
                  </div>
                )}
                <div
                  className={`max-w-[82%] px-3.5 py-2.5 rounded-2xl text-xs leading-relaxed ${
                    msg.sender === 'user'
                      ? 'bg-brand-600 text-white rounded-br-xs shadow-xs'
                      : 'bg-white text-slate-800 border border-slate-200/80 rounded-bl-xs shadow-2xs'
                  }`}
                >
                  <MarkdownRenderer content={msg.text} isUser={msg.sender === 'user'} />
                  <span
                    className={`block text-[9px] mt-1 ${
                      msg.sender === 'user' ? 'text-brand-200 text-right' : 'text-slate-400'
                    }`}
                  >
                    {msg.timestamp}
                  </span>
                </div>
              </div>
            ))}

            {loading && (
              <div className="flex items-center gap-2.5 justify-start">
                <div className="w-7 h-7 rounded-xl bg-brand-600 text-white flex items-center justify-center shrink-0 shadow-xs">
                  <BotIcon size={15} />
                </div>
                <div className="bg-white border border-slate-200/80 px-4 py-3 rounded-2xl rounded-bl-xs text-xs text-slate-500 flex items-center gap-1.5 shadow-2xs">
                  <span className="w-2 h-2 rounded-full bg-brand-500 animate-bounce" />
                  <span
                    className="w-2 h-2 rounded-full bg-brand-500 animate-bounce"
                    style={{ animationDelay: '0.15s' }}
                  />
                  <span
                    className="w-2 h-2 rounded-full bg-brand-500 animate-bounce"
                    style={{ animationDelay: '0.3s' }}
                  />
                  <span className="text-[11px] font-medium text-slate-400 ml-1">AI is thinking...</span>
                </div>
              </div>
            )}

            {error && (
              <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl flex items-start gap-2 animate-fade-in">
                <AlertCircleIcon size={16} className="shrink-0 mt-0.5 text-rose-500" />
                <div className="flex-1">
                  <p className="font-semibold">Generation Error</p>
                  <p className="text-[11px] mt-0.5 text-rose-600">{error}</p>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Footer Input Form */}
          <form
            onSubmit={handleSend}
            className="p-3 bg-white border-t border-slate-100 flex items-center gap-2 shrink-0"
          >
            <input
              type="text"
              value={inputPrompt}
              onChange={(e) => setInputPrompt(e.target.value)}
              placeholder="Ask AI anything..."
              disabled={loading}
              className="flex-1 px-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all placeholder:text-slate-400"
            />
            <button
              type="submit"
              disabled={loading || !inputPrompt.trim()}
              className="p-2 rounded-xl bg-brand-600 text-white hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-xs shrink-0"
              title="Send Message"
            >
              <SendIcon size={16} />
            </button>
          </form>
        </div>
      )}

      {/* Launcher Floating Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2.5 px-4 py-3 bg-gradient-to-r from-brand-600 via-indigo-600 to-brand-700 text-white rounded-2xl shadow-xl hover:shadow-brand-500/25 transform hover:-translate-y-0.5 active:translate-y-0 transition-all duration-200 group"
        title="Open AI Campus Assistant"
      >
        <div className="w-6 h-6 rounded-lg bg-white/20 flex items-center justify-center group-hover:scale-110 transition-transform">
          <SparklesIcon size={16} className="text-white animate-pulse" />
        </div>
        <span className="font-bold text-xs tracking-wide">AI Assistant</span>
      </button>
    </div>
  );
};
