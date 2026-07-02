export interface Opportunity {
  id: number;
  posted_by: any;
  title: string;
  description: string;
  opportunity_type: 'JOB' | 'INTERNSHIP' | 'FREELANCE' | 'PROJECT';
  company_name: string;
  location: string;
  salary_range?: string;
  requirements: string;
  is_remote: boolean;
  deadline: string;
  created_at: string;
  updated_at: string;
  is_active: boolean;
}

export interface Application {
  id: number;
  opportunity: Opportunity;
  applicant: any;
  cover_letter: string;
  status: 'PENDING' | 'REVIEWED' | 'ACCEPTED' | 'REJECTED';
  applied_at: string;
}
