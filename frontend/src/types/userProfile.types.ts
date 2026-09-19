export interface UserProfileResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  active: boolean;
  roles: string[];
  createdAt?: string;
  updatedAt?: string;

  // Student specific fields
  studentId?: string;
  course?: string;
  department?: string;
  year?: string;
  semester?: string;
  skills?: string;
  interests?: string;
  bio?: string;
}

export interface UserProfileUpdateRequest {
  firstName: string;
  lastName: string;
  phone?: string;

  // Student specific fields
  studentId?: string;
  course?: string;
  department?: string;
  year?: string;
  semester?: string;
  skills?: string;
  interests?: string;
  bio?: string;
}
