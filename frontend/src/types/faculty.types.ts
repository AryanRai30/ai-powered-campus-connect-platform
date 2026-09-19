export interface FacultyDashboardResponse {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  status: string;
}

export interface FacultyDashboardStats {
  resourceCount: number;
  announcementCount: number;
  eventCount: number;
  opportunityCount: number;
  clubCount: number;
  totalEventRegistrations: number;
  totalClubMembers: number;
  totalOpportunityApplications: number;
}

export interface FacultyStudent {
  id: number;
  studentId: string;
  firstName: string;
  lastName: string;
  email: string;
  course?: string;
  department?: string;
  year?: string;
  semester?: string;
}

export interface FacultyResource {
  id: number;
  title: string;
  description: string;
  category?: string;
  subject: string;
  resourceType?: string;
  resourceUrl?: string;
  originalFileName?: string;
  storedFileName?: string;
  fileContentType?: string;
  fileSize?: number;
  hasFile?: boolean;
  published: boolean;
  active: boolean;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  targetSection?: string;
  createdByEmail?: string;
  createdByName?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface FacultyResourceRequest {
  title: string;
  description: string;
  category?: string;
  subject: string;
  resourceType?: string;
  resourceUrl?: string;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  targetSection?: string;
  published?: boolean;
}

export interface FacultyAnnouncement {
  id: number;
  title: string;
  content: string;
  category?: string;
  published: boolean;
  active: boolean;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  createdByEmail?: string;
  createdByName?: string;
  publishedAt?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface FacultyAnnouncementRequest {
  title: string;
  content: string;
  category?: string;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  published?: boolean;
}

export interface FacultyEvent {
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
  published: boolean;
  active: boolean;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  createdByEmail?: string;
  createdByName?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface FacultyEventRequest {
  title: string;
  description: string;
  eventDate: string;
  eventTime?: string;
  venue: string;
  category?: string;
  organizer?: string;
  registrationRequired?: boolean;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  published?: boolean;
}

export interface EventRegistrationItem {
  registrationId: number;
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  department?: string;
  year?: string | number;
  registeredAt: string;
}

export interface FacultyOpportunity {
  id: number;
  title: string;
  description: string;
  organization: string;
  opportunityType?: string;
  location?: string;
  skills?: string;
  deadline?: string;
  applicationUrl?: string;
  eligibility?: string;
  published: boolean;
  active: boolean;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  createdByEmail?: string;
  createdByName?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface FacultyOpportunityRequest {
  title: string;
  description: string;
  organization: string;
  opportunityType?: string;
  location?: string;
  skills?: string;
  deadline?: string;
  applicationUrl?: string;
  eligibility?: string;
  targetDepartment?: string;
  targetCourse?: string;
  targetYear?: number;
  targetSemester?: number;
  published?: boolean;
}

export interface OpportunityApplicationItem {
  applicationId: number;
  opportunityId: number;
  opportunityTitle?: string;
  studentUserId?: number;
  studentId?: string;
  firstName: string;
  lastName: string;
  email: string;
  course?: string;
  department?: string;
  year?: string;
  applicationStatus: string;
  appliedAt: string;
}

export interface FacultyClub {
  id: number;
  name: string;
  description: string;
  category?: string;
  presidentName?: string;
  meetingDay?: string;
  meetingTime?: string;
  meetingVenue?: string;
  department?: string;
  memberCount: number;
  published: boolean;
  active: boolean;
  createdByEmail?: string;
  createdByName?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface FacultyClubRequest {
  name: string;
  description: string;
  category?: string;
  presidentName?: string;
  meetingDay?: string;
  meetingTime?: string;
  meetingVenue?: string;
  department?: string;
  published?: boolean;
}

export interface ClubMemberItem {
  membershipId: number;
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  department?: string;
  year?: string | number;
  joinedAt: string;
}
