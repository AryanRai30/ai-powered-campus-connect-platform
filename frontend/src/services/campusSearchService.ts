import api from './api';

export interface CampusSearchRequest {
  query: string;
  category?: string;
  maxResults?: number;
  minScoreThreshold?: number;
}

export interface CampusSearchResultItem {
  id: string;
  title: string;
  content: string;
  sourceType: string;
  score: number;
  category?: string;
  sourceId?: string;
  sourceUrl?: string;
  metadata?: Record<string, string>;
}

export interface CampusSearchResponse {
  query: string;
  totalResults: number;
  results: CampusSearchResultItem[];
  timestamp: string;
}

/**
 * Sends a natural language query to the Phase 11.4 Semantic Campus Search endpoint (POST /api/ai/search)
 */
export const searchCampusKnowledge = async (
  query: string,
  category: string = 'ALL',
  maxResults: number = 15
): Promise<CampusSearchResponse> => {
  const response = await api.post<CampusSearchResponse>(
    '/ai/search',
    {
      query,
      category,
      maxResults,
    },
    { timeout: 30000 }
  );
  return response.data;
};
