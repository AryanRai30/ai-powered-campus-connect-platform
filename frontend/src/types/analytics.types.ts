import { EventItem, ClubItem, OpportunityItem } from './campus.types';

export interface ActivityLogItem {
  type: 'EVENT_REGISTRATION' | 'CLUB_JOIN' | 'OPPORTUNITY_APPLY' | 'OPPORTUNITY_BOOKMARK' | 'RESOURCE_ACCESS' | string;
  title: string;
  description: string;
  timestamp: string;
  referenceId?: number;
}

export interface ContentActivityItem {
  id: number;
  contentType: 'RESOURCE' | 'EVENT' | 'ANNOUNCEMENT' | 'OPPORTUNITY' | 'CLUB' | string;
  title: string;
  category?: string;
  status: 'PUBLISHED' | 'DRAFT' | 'ACTIVE' | string;
  createdAt: string;
  engagementCount: number;
}

export interface EntityEngagementSummary {
  id: number;
  title: string;
  subTitle: string;
  entityType: 'EVENT' | 'CLUB' | 'OPPORTUNITY' | string;
  totalEngagement: number;
}

export interface DepartmentStat {
  department: string;
  studentCount: number;
}

export interface StudentActivityAnalytics {
  totalEventRegistrations: number;
  totalClubMemberships: number;
  totalOpportunityBookmarks: number;
  totalOpportunityApplications: number;
  totalAccessibleResources: number;
  recentActivities: ActivityLogItem[];
  registeredEvents: EventItem[];
  joinedClubs: ClubItem[];
  applications: OpportunityItem[];
  bookmarks: OpportunityItem[];
}

export interface FacultyActivityAnalytics {
  publishedResourcesCount: number;
  eventsCreatedCount: number;
  announcementsCreatedCount: number;
  opportunitiesCreatedCount: number;
  clubsSupervisedCount: number;

  totalEventRegistrationsReceived: number;
  totalClubMembersReceived: number;
  totalOpportunityApplicationsReceived: number;

  recentContent: ContentActivityItem[];
  eventEngagementBreakdown: EntityEngagementSummary[];
  opportunityEngagementBreakdown: EntityEngagementSummary[];
  clubEngagementBreakdown: EntityEngagementSummary[];
}

export interface AdminPlatformAnalytics {
  totalStudents: number;
  totalFaculty: number;
  totalAdmins: number;

  publishedResourcesCount: number;
  totalResourcesCount: number;

  eventsCount: number;
  publishedEventsCount: number;
  totalEventRegistrations: number;

  clubsCount: number;
  totalClubMemberships: number;

  opportunitiesCount: number;
  publishedOpportunitiesCount: number;
  totalOpportunityApplications: number;
  totalOpportunityBookmarks: number;

  announcementsCount: number;

  departmentDistribution: DepartmentStat[];
  recentPlatformActivities: ActivityLogItem[];
}
