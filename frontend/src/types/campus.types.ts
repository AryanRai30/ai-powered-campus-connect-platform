export interface EventItem {
  id: number;
  title: string;
  description: string;
  eventDate: string;
  eventTime?: string;
  venue: string;
  category?: string;
  organizer?: string;
  registrationRequired: boolean;
  registrationCount: number;
  registered: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface RegistrationStatus {
  eventId: number;
  registered: boolean;
  registeredAt?: string;
}

export interface AnnouncementItem {
  id: number;
  title: string;
  content: string;
  category?: string;
  publishedAt: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ClubItem {
  id: number;
  name: string;
  description: string;
  category?: string;
  presidentName?: string;
  meetingDay?: string;
  meetingTime?: string;
  meetingVenue?: string;
  memberCount: number;
  joined: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface ClubMembershipStatus {
  clubId: number;
  joined: boolean;
  joinedAt?: string;
}

export interface AcademicResourceItem {
  id: number;
  title: string;
  description: string;
  category?: string;
  subject: string;
  resourceType?: string; // NOTES, PDF, VIDEO, WEBSITE, OTHER
  resourceUrl?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface OpportunityItem {
  id: number;
  title: string;
  description: string;
  organization: string;
  opportunityType?: string; // INTERNSHIP, JOB, SCHOLARSHIP, COMPETITION, WORKSHOP, OTHER
  location?: string;
  skills?: string;
  deadline?: string;
  applicationUrl?: string;
  bookmarked: boolean;
  applied: boolean;
  applicationStatus?: string; // APPLIED, INTERVIEW, SELECTED, REJECTED
  createdAt: string;
  updatedAt?: string;
}

export interface OpportunityBookmarkStatus {
  opportunityId: number;
  bookmarked: boolean;
  bookmarkedAt?: string;
}

export interface OpportunityApplicationStatus {
  opportunityId: number;
  applied: boolean;
  status: string;
  appliedAt?: string;
}
