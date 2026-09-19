import api from './api';
import {
  EventItem,
  RegistrationStatus,
  AnnouncementItem,
  ClubItem,
  ClubMembershipStatus,
  AcademicResourceItem,
  OpportunityItem,
  OpportunityBookmarkStatus,
  OpportunityApplicationStatus
} from '../types/campus.types';

/**
 * Service for Campus Events API calls
 */
export const fetchEvents = async (category?: string, search?: string): Promise<EventItem[]> => {
  const params: Record<string, string> = {};
  if (category && category !== 'All') params.category = category;
  if (search) params.search = search;

  const response = await api.get<EventItem[]>('/events', { params });
  return response.data;
};

export const fetchEventById = async (id: number): Promise<EventItem> => {
  const response = await api.get<EventItem>(`/events/${id}`);
  return response.data;
};

export const registerForEvent = async (eventId: number): Promise<RegistrationStatus> => {
  const response = await api.post<RegistrationStatus>(`/events/${eventId}/register`);
  return response.data;
};

export const checkEventRegistration = async (eventId: number): Promise<RegistrationStatus> => {
  const response = await api.get<RegistrationStatus>(`/events/${eventId}/registration`);
  return response.data;
};

/**
 * Service for Campus Announcements API calls
 */
export const fetchAnnouncements = async (category?: string, search?: string): Promise<AnnouncementItem[]> => {
  const params: Record<string, string> = {};
  if (category && category !== 'All') params.category = category;
  if (search) params.search = search;

  const response = await api.get<AnnouncementItem[]>('/announcements', { params });
  return response.data;
};

export const fetchAnnouncementById = async (id: number): Promise<AnnouncementItem> => {
  const response = await api.get<AnnouncementItem>(`/announcements/${id}`);
  return response.data;
};

/**
 * Service for Campus Clubs & Communities API calls
 */
export const fetchClubs = async (category?: string, search?: string): Promise<ClubItem[]> => {
  const params: Record<string, string> = {};
  if (category && category !== 'All') params.category = category;
  if (search) params.search = search;

  const response = await api.get<ClubItem[]>('/clubs', { params });
  return response.data;
};

export const fetchMyClubs = async (): Promise<ClubItem[]> => {
  const response = await api.get<ClubItem[]>('/clubs/my-clubs');
  return response.data;
};

export const fetchClubById = async (id: number): Promise<ClubItem> => {
  const response = await api.get<ClubItem>(`/clubs/${id}`);
  return response.data;
};

export const joinClub = async (clubId: number): Promise<ClubMembershipStatus> => {
  const response = await api.post<ClubMembershipStatus>(`/clubs/${clubId}/join`);
  return response.data;
};

export const checkClubMembership = async (clubId: number): Promise<ClubMembershipStatus> => {
  const response = await api.get<ClubMembershipStatus>(`/clubs/${clubId}/membership`);
  return response.data;
};

/**
 * Service for Academic Resources & Study Materials API calls
 */
export const fetchAcademicResources = async (
  category?: string,
  subject?: string,
  resourceType?: string,
  search?: string
): Promise<AcademicResourceItem[]> => {
  const params: Record<string, string> = {};
  if (category && category !== 'All') params.category = category;
  if (subject && subject !== 'All') params.subject = subject;
  if (resourceType && resourceType !== 'All') params.resourceType = resourceType;
  if (search) params.search = search;

  const response = await api.get<AcademicResourceItem[]>('/resources', { params });
  return response.data;
};

export const fetchAcademicResourceById = async (id: number): Promise<AcademicResourceItem> => {
  const response = await api.get<AcademicResourceItem>(`/resources/${id}`);
  return response.data;
};

export const getResourceFileBlob = async (
  id: number,
  mode: 'view' | 'download' = 'view'
): Promise<{ blob: Blob; filename: string; contentType: string }> => {
  const endpoint = mode === 'download' ? `/resources/${id}/download` : `/resources/${id}/file`;
  const response = await api.get(endpoint, { responseType: 'blob' });

  let filename = 'resource-file';
  const disposition = response.headers['content-disposition'];
  if (disposition && disposition.includes('filename=')) {
    const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(disposition);
    if (matches && matches[1]) {
      filename = matches[1].replace(/['"]/g, '');
    }
  }

  const contentType = response.headers['content-type'] || response.data?.type || 'application/octet-stream';
  return { blob: response.data, filename, contentType };
};

export const viewResourceFile = async (id: number): Promise<string> => {
  const { blob, contentType } = await getResourceFileBlob(id, 'view');
  const typedBlob = new Blob([blob], { type: contentType });
  const fileURL = URL.createObjectURL(typedBlob);
  window.open(fileURL, '_blank', 'noopener,noreferrer');
  return fileURL;
};

export const fetchResourceObjectUrl = async (id: number): Promise<{ objectUrl: string; contentType: string; filename: string }> => {
  const { blob, filename, contentType } = await getResourceFileBlob(id, 'view');
  const typedBlob = new Blob([blob], { type: contentType });
  const objectUrl = URL.createObjectURL(typedBlob);
  return { objectUrl, contentType, filename };
};

export const downloadResourceFile = async (id: number, fallbackName?: string): Promise<void> => {
  const { blob, filename, contentType } = await getResourceFileBlob(id, 'download');
  const typedBlob = new Blob([blob], { type: contentType });
  const fileURL = URL.createObjectURL(typedBlob);
  const link = document.createElement('a');
  link.href = fileURL;
  link.setAttribute('download', fallbackName || filename || 'downloaded-resource');
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(fileURL), 10000);
};

/**
 * Service for Opportunities & Career Support API calls
 */
export const fetchOpportunities = async (
  opportunityType?: string,
  location?: string,
  search?: string
): Promise<OpportunityItem[]> => {
  const params: Record<string, string> = {};
  if (opportunityType && opportunityType !== 'All') params.opportunityType = opportunityType;
  if (location && location !== 'All') params.location = location;
  if (search) params.search = search;

  const response = await api.get<OpportunityItem[]>('/opportunities', { params });
  return response.data;
};

export const fetchMyBookmarks = async (): Promise<OpportunityItem[]> => {
  const response = await api.get<OpportunityItem[]>('/opportunities/my-bookmarks');
  return response.data;
};

export const fetchMyApplications = async (): Promise<OpportunityItem[]> => {
  const response = await api.get<OpportunityItem[]>('/opportunities/my-applications');
  return response.data;
};

export const fetchOpportunityById = async (id: number): Promise<OpportunityItem> => {
  const response = await api.get<OpportunityItem>(`/opportunities/${id}`);
  return response.data;
};

export const bookmarkOpportunity = async (opportunityId: number): Promise<OpportunityBookmarkStatus> => {
  const response = await api.post<OpportunityBookmarkStatus>(`/opportunities/${opportunityId}/bookmark`);
  return response.data;
};

export const applyForOpportunity = async (opportunityId: number): Promise<OpportunityApplicationStatus> => {
  const response = await api.post<OpportunityApplicationStatus>(`/opportunities/${opportunityId}/apply`);
  return response.data;
};
