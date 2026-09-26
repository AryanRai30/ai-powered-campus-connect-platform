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
 * Helper to extract standardized error messages for AI assistant requests
 */
export const getAiErrorMessage = (err: any): string => {
  if (err?.response?.status === 503) {
    return 'Gemini AI is temporarily unavailable. Please wait a few seconds and try again.';
  }
  return (
    err?.response?.data?.response ||
    err?.response?.data?.message ||
    'Failed to generate response from Gemini API. Please try again later.'
  );
};
