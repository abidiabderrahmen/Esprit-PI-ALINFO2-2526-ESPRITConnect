export interface Post {
  id: number;
  author: any;
  content: string;
  image?: string;
  post_type: 'GENERAL' | 'ACHIEVEMENT' | 'ARTICLE' | 'QUESTION';
  likes_count: number;
  comments_count: number;
  is_liked: boolean;
  comments: Comment[];
  created_at: string;
  updated_at: string;
}

export interface Comment {
  id: number;
  author: any;
  content: string;
  created_at: string;
}
