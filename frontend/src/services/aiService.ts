import api from './api';

export interface AiHealthResponse {
  status: string;
  apiKeyConfigured: boolean;
  model: string;
  timestamp: string;
}

export interface AiChatRequest {
  message: string;
}

export interface AiChatResponse {
  response: string;
  status: string;
  timestamp: string;
}

export interface RagQueryRequest {
  query: string;
}

export interface VectorSearchResult {
  segmentId: string;
  text: string;
  score: number;
  metadata?: Record<string, string>;
}

export interface RagQueryResponse {
  query: string;
  answer: string;
  grounded: boolean;
  retrievedChunksCount: number;
  matchedChunks?: VectorSearchResult[];
  timestamp: string;
}

/**
 * Health check for AI Gemini Foundation service
 */
export const checkAiHealth = async (): Promise<AiHealthResponse> => {
  const response = await api.get<AiHealthResponse>('/ai/health');
  return response.data;
};

/**
 * Sends a user message prompt to the Gemini AI backend service
 */
export const sendAiChatMessage = async (message: string): Promise<AiChatResponse> => {
  const response = await api.post<AiChatResponse>('/ai/chat', { message }, { timeout: 30000 });
  return response.data;
};

/**
 * Sends a student query to the RAG knowledge base backend service (POST /api/ai/rag/query)
 */
export const sendRagQuery = async (query: string): Promise<RagQueryResponse> => {
  const response = await api.post<RagQueryResponse>('/ai/rag/query', { query }, { timeout: 35000 });
  return response.data;
};

/**
 * Helper to extract standardized error messages for AI assistant requests
 */
export const getAiErrorMessage = (err: any): string => {
  if (err?.response?.status === 503) {
    return 'Gemini AI is temporarily unavailable. Please wait a few seconds and try again.';
  }
  return (
    err?.response?.data?.response ||
    err?.response?.data?.answer ||
    err?.response?.data?.message ||
    'Failed to generate response from Gemini API. Please try again later.'
  );
};

export interface AiAssistantResponse {
  answer: string;
  isRag: boolean;
  grounded?: boolean;
}

/**
 * Automatically classifies whether a question is campus-related (RAG)
 * vs a general technical / broad knowledge question (Normal AI Chat).
 */
export const isCampusQuery = (query: string): boolean => {
  if (!query || !query.trim()) return false;
  const q = query.toLowerCase().trim();

  // 1. Explicit campus keywords
  const campusKeywords = [
    'campus',
    'college',
    'university',
    'club',
    'clubs',
    'event',
    'events',
    'announcement',
    'announcements',
    'academic',
    'academics',
    'resource',
    'resources',
    'internship',
    'internships',
    'opportunity',
    'opportunities',
    'placement',
    'placements',
    'salesforce',
    'faculty',
    'professor',
    'syllabus',
    'coursework',
    'semester',
    'gpa',
    'credits',
    'hostel',
    'canteen',
    'library',
    'exam',
    'exams',
    'midterm',
    'register',
    'registration',
    'organizer',
    'department',
    'dept',
    'schedule',
    'guideline',
    'guidelines',
    'lab',
    'labs',
    'fee',
    'fees',
    'admission',
    'admissions',
    'notice',
    'notices',
    'rule',
    'rules',
    'policy',
    'policies',
  ];

  if (campusKeywords.some((kw) => q.includes(kw))) {
    return true;
  }

  if (/\b(available|upcoming|happening|ongoing|apply|joining)\b/.test(q)) {
    return true;
  }

  // 2. General tech / non-campus query patterns
  const isGeneralTechPattern =
    /^(what is|explain|how does|define|difference between|what are) (java|spring|spring boot|jwt|react|rag|microservices|python|javascript|typescript|c\+\+|sql|html|css|docker|kubernetes|git|rest|api|json|oauth|database|oop|recursion|pointers|operating system|linux|windows|architecture|design patterns?|data structures?)\b/i.test(q);

  if (isGeneralTechPattern) {
    return false;
  }

  return false;
};

/**
 * Unified AI Assistant Query Service that automatically routes:
 * - Campus queries -> RAG (POST /api/ai/rag/query)
 * - General queries -> Normal AI Chat (POST /api/ai/chat)
 */
export const sendAiAssistantQuery = async (query: string): Promise<AiAssistantResponse> => {
  if (isCampusQuery(query)) {
    const res = await sendRagQuery(query);
    return {
      answer: res.answer,
      isRag: true,
      grounded: res.grounded,
    };
  } else {
    const res = await sendAiChatMessage(query);
    return {
      answer: res.response,
      isRag: false,
    };
  }
};


