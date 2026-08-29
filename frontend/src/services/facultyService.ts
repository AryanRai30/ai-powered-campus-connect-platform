import api from './api';
import {
  FacultyDashboardResponse,
  FacultyDashboardStats,
  FacultyStudent,
  FacultyResource,
  FacultyResourceRequest,
  FacultyAnnouncement,
  FacultyAnnouncementRequest,
  FacultyEvent,
  FacultyEventRequest,
  EventRegistrationItem,
  FacultyOpportunity,
  FacultyOpportunityRequest,
  OpportunityApplicationItem,
  FacultyClub,
  FacultyClubRequest,
  ClubMemberItem,
} from '../types/faculty.types';

// Dashboard & Stats
export const getFacultyDashboard = async (): Promise<FacultyDashboardResponse> => {
  const response = await api.get<FacultyDashboardResponse>('/faculty/dashboard');
  return response.data;
};

export const getFacultyStats = async (): Promise<FacultyDashboardStats> => {
  const response = await api.get<FacultyDashboardStats>('/faculty/stats');
  return response.data;
};

// Students Overview
export const getFacultyStudents = async (params?: {
  department?: string;
  course?: string;
  year?: string;
  semester?: string;
  search?: string;
}): Promise<FacultyStudent[]> => {
  const response = await api.get<FacultyStudent[]>('/faculty/students', { params });
  return response.data;
};

// Resources
export const getFacultyResources = async (): Promise<FacultyResource[]> => {
  const response = await api.get<FacultyResource[]>('/faculty/resources');
  return response.data;
};

export const createFacultyResource = async (data: FacultyResourceRequest): Promise<FacultyResource> => {
  const response = await api.post<FacultyResource>('/faculty/resources', data);
  return response.data;
};

export const updateFacultyResource = async (id: number, data: FacultyResourceRequest): Promise<FacultyResource> => {
  const response = await api.put<FacultyResource>(`/faculty/resources/${id}`, data);
  return response.data;
};

export const deleteFacultyResource = async (id: number): Promise<void> => {
  await api.delete(`/faculty/resources/${id}`);
};

export const publishFacultyResource = async (id: number): Promise<FacultyResource> => {
  const response = await api.post<FacultyResource>(`/faculty/resources/${id}/publish`);
  return response.data;
};

export const unpublishFacultyResource = async (id: number): Promise<FacultyResource> => {
  const response = await api.post<FacultyResource>(`/faculty/resources/${id}/unpublish`);
  return response.data;
};

// Announcements
export const getFacultyAnnouncements = async (): Promise<FacultyAnnouncement[]> => {
  const response = await api.get<FacultyAnnouncement[]>('/faculty/announcements');
  return response.data;
};

export const createFacultyAnnouncement = async (data: FacultyAnnouncementRequest): Promise<FacultyAnnouncement> => {
  const response = await api.post<FacultyAnnouncement>('/faculty/announcements', data);
  return response.data;
};

export const updateFacultyAnnouncement = async (id: number, data: FacultyAnnouncementRequest): Promise<FacultyAnnouncement> => {
  const response = await api.put<FacultyAnnouncement>(`/faculty/announcements/${id}`, data);
  return response.data;
};

export const deleteFacultyAnnouncement = async (id: number): Promise<void> => {
  await api.delete(`/faculty/announcements/${id}`);
};

export const publishFacultyAnnouncement = async (id: number): Promise<FacultyAnnouncement> => {
  const response = await api.post<FacultyAnnouncement>(`/faculty/announcements/${id}/publish`);
  return response.data;
};

export const unpublishFacultyAnnouncement = async (id: number): Promise<FacultyAnnouncement> => {
  const response = await api.post<FacultyAnnouncement>(`/faculty/announcements/${id}/unpublish`);
  return response.data;
};

// Events
export const getFacultyEvents = async (): Promise<FacultyEvent[]> => {
  const response = await api.get<FacultyEvent[]>('/faculty/events');
  return response.data;
};

export const createFacultyEvent = async (data: FacultyEventRequest): Promise<FacultyEvent> => {
  const response = await api.post<FacultyEvent>('/faculty/events', data);
  return response.data;
};

export const updateFacultyEvent = async (id: number, data: FacultyEventRequest): Promise<FacultyEvent> => {
  const response = await api.put<FacultyEvent>(`/faculty/events/${id}`, data);
  return response.data;
};

export const deleteFacultyEvent = async (id: number): Promise<void> => {
  await api.delete(`/faculty/events/${id}`);
};

export const publishFacultyEvent = async (id: number): Promise<FacultyEvent> => {
  const response = await api.post<FacultyEvent>(`/faculty/events/${id}/publish`);
  return response.data;
};

export const unpublishFacultyEvent = async (id: number): Promise<FacultyEvent> => {
  const response = await api.post<FacultyEvent>(`/faculty/events/${id}/unpublish`);
  return response.data;
};

export const getEventRegistrations = async (eventId: number): Promise<EventRegistrationItem[]> => {
  const response = await api.get<EventRegistrationItem[]>(`/faculty/events/${eventId}/registrations`);
  return response.data;
};

// Opportunities
export const getFacultyOpportunities = async (): Promise<FacultyOpportunity[]> => {
  const response = await api.get<FacultyOpportunity[]>('/faculty/opportunities');
  return response.data;
};

export const createFacultyOpportunity = async (data: FacultyOpportunityRequest): Promise<FacultyOpportunity> => {
  const response = await api.post<FacultyOpportunity>('/faculty/opportunities', data);
  return response.data;
};

export const updateFacultyOpportunity = async (id: number, data: FacultyOpportunityRequest): Promise<FacultyOpportunity> => {
  const response = await api.put<FacultyOpportunity>(`/faculty/opportunities/${id}`, data);
  return response.data;
};

export const deleteFacultyOpportunity = async (id: number): Promise<void> => {
  await api.delete(`/faculty/opportunities/${id}`);
};

export const publishFacultyOpportunity = async (id: number): Promise<FacultyOpportunity> => {
  const response = await api.post<FacultyOpportunity>(`/faculty/opportunities/${id}/publish`);
  return response.data;
};

export const unpublishFacultyOpportunity = async (id: number): Promise<FacultyOpportunity> => {
  const response = await api.post<FacultyOpportunity>(`/faculty/opportunities/${id}/unpublish`);
  return response.data;
};

export const getOpportunityApplications = async (opportunityId: number): Promise<OpportunityApplicationItem[]> => {
  const response = await api.get<OpportunityApplicationItem[]>(`/faculty/opportunities/${opportunityId}/applications`);
  return response.data;
};

// Clubs
export const getFacultyClubs = async (): Promise<FacultyClub[]> => {
  const response = await api.get<FacultyClub[]>('/faculty/clubs');
  return response.data;
};

export const createFacultyClub = async (data: FacultyClubRequest): Promise<FacultyClub> => {
  const response = await api.post<FacultyClub>('/faculty/clubs', data);
  return response.data;
};

export const updateFacultyClub = async (id: number, data: FacultyClubRequest): Promise<FacultyClub> => {
  const response = await api.put<FacultyClub>(`/faculty/clubs/${id}`, data);
  return response.data;
};

export const deleteFacultyClub = async (id: number): Promise<void> => {
  await api.delete(`/faculty/clubs/${id}`);
};

export const publishFacultyClub = async (id: number): Promise<FacultyClub> => {
  const response = await api.post<FacultyClub>(`/faculty/clubs/${id}/publish`);
  return response.data;
};

export const unpublishFacultyClub = async (id: number): Promise<FacultyClub> => {
  const response = await api.post<FacultyClub>(`/faculty/clubs/${id}/unpublish`);
  return response.data;
};

export const getClubMembers = async (clubId: number): Promise<ClubMemberItem[]> => {
  const response = await api.get<ClubMemberItem[]>(`/faculty/clubs/${clubId}/members`);
  return response.data;
};
