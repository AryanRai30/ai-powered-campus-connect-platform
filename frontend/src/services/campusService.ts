import api from './api';
import {
  EventItem,
  RegistrationStatus,
  AnnouncementItem,
  ClubItem,
  ClubMembershipStatus,
  AcademicResourceItem
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
