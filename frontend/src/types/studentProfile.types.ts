export interface StudentProfileRequest {
  studentId: string;
  course: string;
  department: string;
  year: string;
  semester: string;
  skills?: string;
  interests?: string;
  bio?: string;
}

export interface StudentProfileResponse {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  studentId: string;
  course: string;
  department: string;
  year: string;
  semester: string;
  skills?: string;
  interests?: string;
  bio?: string;
  createdAt: string;
  updatedAt?: string;
}
