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
