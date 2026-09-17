export interface AdminStatsResponse {
  totalStudents: number;
  totalFaculty: number;
  totalAdmins: number;
  totalEvents: number;
  totalAnnouncements: number;
  totalClubs: number;
  totalAcademicResources: number;
  totalResources?: number;
  totalOpportunities: number;
}

export interface AdminFacultyResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface AdminFacultyDetailResponse extends AdminFacultyResponse {
  resourceCount: number;
  announcementCount: number;
  eventCount: number;
  clubCount: number;
  opportunityCount: number;
}

export interface CreateFacultyRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
}

export interface UpdateFacultyRequest {
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface UpdateFacultyStatusRequest {
  active: boolean;
}

export interface AdminStudentResponse {
  id: number;
  studentId?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  department?: string;
  course?: string;
  year?: string;
  semester?: string;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface AdminStudentDetailResponse extends AdminStudentResponse {
  skills?: string;
  interests?: string;
  bio?: string;
}

export interface UpdateStudentStatusRequest {
  active: boolean;
}
