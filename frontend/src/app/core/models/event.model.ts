export interface Event {
  id: number;
  organizer: any;
  title: string;
  description: string;
  event_type: 'CONFERENCE' | 'WORKSHOP' | 'MEETUP' | 'WEBINAR' | 'CAREER_FAIR';
  location: string;
  date: string;
  time: string;
  max_participants: number;
  registered_count?: number;
  image?: string;
  is_online: boolean;
  meeting_link?: string;
  is_active: boolean;
  created_at: string;
}
