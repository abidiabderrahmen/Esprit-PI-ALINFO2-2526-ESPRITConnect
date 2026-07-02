export interface User {
  id: number;
  username: string;
  email: string;
  first_name: string;
  last_name: string;
  role: 'STUDENT' | 'ALUMNI' | 'COMPANY' | 'ADMIN';
  avatar?: string;
  bio?: string;
  phone?: string;
  linkedin_url?: string;
  github_url?: string;
  graduation_year?: number;
  field_of_study?: string;
  current_position?: string;
  company_name?: string;
  location?: string;
  skills?: string;
  is_mentor: boolean;
  created_at: string;
  updated_at: string;
}

export interface AuthResponse {
  access: string;
  refresh: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
  password_confirm: string;
  first_name: string;
  last_name: string;
  role: string;
}
