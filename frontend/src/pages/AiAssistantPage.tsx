import React, { useState, useRef, useEffect } from 'react';
import { sendAiAssistantQuery, checkAiHealth, getAiErrorMessage } from '../services/aiService';
import { MarkdownRenderer } from '../components/ai/MarkdownRenderer';
import {
  SparklesIcon,
  SendIcon,
  BotIcon,
  UserIcon,
  RefreshCwIcon,
  AlertCircleIcon,
} from '../components/common/Icons';

interface ChatMessage {
  id: string;
  sender: 'user' | 'ai';
  text: string;
  timestamp: string;
}

const PRESET_PROMPTS = [
  'What academic study resources are available on campus?',
  'How do I join student clubs and register for upcoming events?',
  'Where can I explore career and internship opportunities?',
  'Provide recommendations for managing semester coursework effectively.',
];

export const AiAssistantPage: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome-1',
      sender: 'ai',
      text: 'Hello! I am your AI Campus Assistant powered by Google Gemini. How can I assist you with your academic journey or campus activities today?',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [modelStatus, setModelStatus] = useState<{ configured: boolean; model: string }>({
    configured: true,
    model: 'gemini-3.6-flash',
  });

  const chatContainerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Perform initial health check to verify AI status
    checkAiHealth()
      .then((res) => {
        setModelStatus({
          configured: res.apiKeyConfigured,
          model: res.model || 'gemini-1.5-flash',
        });
        if (!res.apiKeyConfigured) {
          setError('Gemini API key is not configured on the server. Please set GEMINI_API_KEY environment variable.');
        }
      })
      .catch(() => {
        setModelStatus((prev) => ({ ...prev, configured: false }));
      });
  }, []);

  useEffect(() => {
    chatContainerRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const handleSend = async (customPrompt?: string) => {
    const promptText = (customPrompt || inputPrompt).trim();
    if (!promptText || loading) return;

    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: promptText,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputPrompt('');
    setLoading(true);
    setError(null);

    try {
      const res = await sendAiAssistantQuery(promptText);
      const aiMsg: ChatMessage = {
        id: `ai-${Date.now()}`,
        sender: 'ai',
        text: res.answer || 'No response returned from Gemini.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch (err: any) {
      setError(getAiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleClearChat = () => {
    setMessages([
      {
        id: `welcome-${Date.now()}`,
        sender: 'ai',
        text: 'Conversation reset. What else would you like to ask?',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
    setError(null);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Page Header */}
      <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-brand-600 via-indigo-600 to-brand-700 text-white flex items-center justify-center shadow-md shrink-0">
            <SparklesIcon size={24} className="animate-pulse" />
          </div>
          <div>
            <h1 className="text-2xl font-black text-slate-900 tracking-tight">AI Campus Assistant</h1>
            <p className="text-xs text-slate-500 font-medium mt-0.5">
              Intelligent companion powered by Google Gemini for student support & campus guidance.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-100 border border-slate-200 text-xs font-semibold text-slate-700">
            <span
              className={`w-2 h-2 rounded-full ${
                modelStatus.configured ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'
              }`}
            />
            <span>{modelStatus.model}</span>
          </div>

          <button
            onClick={handleClearChat}
            className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-xl transition-colors border border-slate-200"
            title="Reset Conversation"
          >
            <RefreshCwIcon size={18} />
          </button>
        </div>
      </div>

      {/* Error Alert Banner */}
      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl flex items-start gap-3 shadow-xs animate-fade-in">
          <AlertCircleIcon size={20} className="text-rose-600 shrink-0 mt-0.5" />
          <div className="flex-1">
            <h4 className="font-bold text-sm">AI Communication Error</h4>
            <p className="text-xs text-rose-700 mt-1">{error}</p>
          </div>
        </div>
      )}

      {/* Main SaaS Chat Workspace */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-subtle flex flex-col h-[620px] overflow-hidden">
        {/* Chat Messages Body */}
        <div className="flex-1 p-6 overflow-y-auto space-y-6 bg-slate-50/40">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`flex gap-3.5 ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              {msg.sender === 'ai' && (
                <div className="w-9 h-9 rounded-2xl bg-gradient-to-tr from-brand-600 to-indigo-600 text-white flex items-center justify-center shrink-0 shadow-sm mt-1">
                  <BotIcon size={20} />
                </div>
              )}

              <div className={`space-y-1 max-w-[80%] md:max-w-[70%]`}>
                <div className="flex items-center gap-2 px-1">
                  <span className="text-[11px] font-bold text-slate-500">
                    {msg.sender === 'user' ? 'You' : 'Campus AI'}
                  </span>
                  <span className="text-[10px] text-slate-400">{msg.timestamp}</span>
                </div>

                <div
                  className={`p-4 rounded-2xl text-sm leading-relaxed ${
                    msg.sender === 'user'
                      ? 'bg-gradient-to-r from-brand-600 to-indigo-600 text-white rounded-tr-xs shadow-sm'
                      : 'bg-white text-slate-800 border border-slate-200/80 rounded-tl-xs shadow-2xs'
                  }`}
                >
                  <MarkdownRenderer content={msg.text} isUser={msg.sender === 'user'} />
                </div>
              </div>

              {msg.sender === 'user' && (
                <div className="w-9 h-9 rounded-2xl bg-slate-200 text-slate-700 flex items-center justify-center shrink-0 shadow-2xs mt-1">
                  <UserIcon size={20} />
                </div>
              )}
            </div>
          ))}

          {loading && (
            <div className="flex gap-3.5 justify-start">
              <div className="w-9 h-9 rounded-2xl bg-gradient-to-tr from-brand-600 to-indigo-600 text-white flex items-center justify-center shrink-0 shadow-sm mt-1">
                <BotIcon size={20} />
              </div>
              <div className="bg-white border border-slate-200/80 px-5 py-4 rounded-2xl rounded-tl-xs text-sm text-slate-500 flex items-center gap-2 shadow-2xs">
                <span className="w-2.5 h-2.5 rounded-full bg-brand-500 animate-bounce" />
                <span
                  className="w-2.5 h-2.5 rounded-full bg-brand-500 animate-bounce"
                  style={{ animationDelay: '0.15s' }}
                />
                <span
                  className="w-2.5 h-2.5 rounded-full bg-brand-500 animate-bounce"
                  style={{ animationDelay: '0.3s' }}
                />
                <span className="text-xs font-semibold text-slate-500 ml-1">Generating response...</span>
              </div>
            </div>
          )}

          <div ref={chatContainerRef} />
        </div>

        {/* Preset Prompt Suggestion Chips */}
        {messages.length <= 2 && (
          <div className="px-6 py-3 bg-white border-t border-slate-100 flex flex-wrap gap-2">
            <span className="text-xs font-bold text-slate-400 self-center mr-1">Suggestions:</span>
            {PRESET_PROMPTS.map((prompt, idx) => (
              <button
                key={idx}
                onClick={() => handleSend(prompt)}
                disabled={loading}
                className="text-xs bg-slate-100 hover:bg-brand-50 hover:text-brand-600 text-slate-600 px-3 py-1.5 rounded-xl transition-all border border-slate-200/80 text-left font-medium"
              >
                {prompt}
              </button>
            ))}
          </div>
        )}

        {/* Input Bar Form */}
        <div className="p-4 bg-white border-t border-slate-100">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
            className="flex items-end gap-3 bg-slate-50 border border-slate-200 rounded-2xl p-2.5 focus-within:bg-white focus-within:ring-2 focus-within:ring-brand-500/20 focus-within:border-brand-500 transition-all"
          >
            <textarea
              value={inputPrompt}
              onChange={(e) => setInputPrompt(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask a question about campus, academics, or events... (Press Enter to send, Shift+Enter for new line)"
              rows={2}
              disabled={loading}
              className="flex-1 bg-transparent border-0 text-sm focus:outline-none resize-none px-2 py-1 text-slate-900 placeholder:text-slate-400"
            />
            <button
              type="submit"
              disabled={loading || !inputPrompt.trim()}
              className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-brand-600 to-indigo-600 text-white font-bold text-xs hover:shadow-md disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center gap-2 shrink-0"
            >
              <span>Send</span>
              <SendIcon size={16} />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AiAssistantPage;
