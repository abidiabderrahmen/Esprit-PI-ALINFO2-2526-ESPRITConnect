import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss'
})
export class FeedComponent implements OnInit {
  isLoading = true;
  showCreateModal = false;
  newPostContent = '';
  newPostType = 'general';
  showComments: {[key: number]: boolean} = {};
  newComment: {[key: number]: string} = {};
  likedPosts: {[key: number]: boolean} = {};

  postTypes = [
    { value: 'general', label: 'General', icon: 'chat_bubble_outline' },
    { value: 'achievement', label: 'Achievement', icon: 'emoji_events' },
    { value: 'article', label: 'Article', icon: 'article' },
    { value: 'question', label: 'Question', icon: 'help_outline' }
  ];

  posts: any[] = [];

  trendingTopics = [
    { tag: '#', name: 'TechTunisia2026', posts: '1.2k posts' },
    { tag: '#', name: 'ESPRITAlumni', posts: '856 posts' },
    { tag: '#', name: 'MachineLearning', posts: '643 posts' },
    { tag: '#', name: 'FullStack', posts: '428 posts' },
  ];

  suggestedPeople: any[] = [];
  pendingRequests: any[] = [];
  connectionCount = 0;
  connectionStatuses: {[userId: number]: string} = {};

  private colors = ['#4338CA', '#E63946', '#059669', '#EA580C', '#7C3AED', '#0891B2', '#DB2777', '#65A30D'];

  currentUser: any = null;

  constructor(private apiService: ApiService, private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(u => {
      this.currentUser = u;
      this.loadSuggestedPeople();
      this.loadConnectionData();
    });
    if (!this.currentUser) this.authService.getProfile().subscribe();
    this.loadPosts();
    this.loadSuggestedPeople();
    this.loadConnectionData();
  }

  get userInitials(): string {
    if (!this.currentUser) return 'U';
    return ((this.currentUser.first_name?.[0] || '') + (this.currentUser.last_name?.[0] || '')).toUpperCase() || 'U';
  }

  get userName(): string {
    if (!this.currentUser) return 'Welcome Back';
    return `${this.currentUser.first_name || ''} ${this.currentUser.last_name || ''}`.trim() || this.currentUser.username || 'User';
  }

  get userRole(): string {
    return this.currentUser?.current_position || this.currentUser?.role || 'ESPRIT Member';
  }

  loadPosts() {
    this.isLoading = true;
    this.apiService.getPosts().subscribe({
      next: (res) => {
        const results = res.results || res;
        this.posts = (Array.isArray(results) ? results : []).map((p: any) => {
          const a = p.author_details || p.author || {};
          const isObj = typeof a === 'object' && a !== null;
          return {
          id: p.id,
          author: isObj ? (`${a.first_name || ''} ${a.last_name || ''}`.trim() || a.username || 'User') : 'User',
          role: isObj ? (a.current_position || a.role || '') : '',
          initials: isObj ? (`${a.first_name?.[0] || ''}${a.last_name?.[0] || ''}`.toUpperCase() || 'U') : 'U',
          color: this.getColor(isObj ? (a.id || 0) : 0),
          type: (p.post_type || 'general').toLowerCase(),
          time: this.getRelativeTime(p.created_at),
          content: p.content,
          likes: p.likes_count || 0,
          isLiked: p.is_liked || false,
          comments: (p.comments || []).map((c: any) => {
            const ca = c.author_details || c.author || {};
            const caObj = typeof ca === 'object' && ca !== null;
            return {
            author: caObj ? (`${ca.first_name || ''} ${ca.last_name || ''}`.trim() || ca.username || 'User') : 'User',
            initials: caObj ? (`${ca.first_name?.[0] || ''}${ca.last_name?.[0] || ''}`.toUpperCase() || 'U') : 'U',
            color: this.getColor(caObj ? (ca.id || 0) : 0),
            text: c.content
          }; })
        }; });
        this.posts.forEach(p => { if (p.isLiked) this.likedPosts[p.id] = true; });
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  toggleLike(postId: number) {
    const wasLiked = this.likedPosts[postId];
    this.likedPosts[postId] = !wasLiked;
    const post = this.posts.find(p => p.id === postId);
    if (post) post.likes += wasLiked ? -1 : 1;

    const call = wasLiked ? this.apiService.unlikePost(postId) : this.apiService.likePost(postId);
    call.subscribe({ error: () => {
      this.likedPosts[postId] = wasLiked;
      if (post) post.likes += wasLiked ? 1 : -1;
    }});
  }

  toggleComments(postId: number) { this.showComments[postId] = !this.showComments[postId]; }

  addComment(postId: number) {
    const text = this.newComment[postId]?.trim();
    if (!text) return;
    this.apiService.commentOnPost(postId, text).subscribe({
      next: () => {
        const post = this.posts.find(p => p.id === postId);
        const user = this.authService.currentUser;
        if (post) {
          post.comments.push({
            author: user ? `${user.first_name} ${user.last_name}` : 'You',
            initials: user ? `${user.first_name?.[0] || ''}${user.last_name?.[0] || ''}` : 'YO',
            color: '#1B2A4A', text
          });
        }
        this.newComment[postId] = '';
      }
    });
  }

  createPost() {
    if (!this.newPostContent.trim()) return;
    this.apiService.createPost({ content: this.newPostContent, post_type: this.newPostType.toUpperCase() }).subscribe({
      next: () => {
        this.newPostContent = '';
        this.showCreateModal = false;
        this.loadPosts();
      },
      error: () => {
        this.newPostContent = '';
        this.showCreateModal = false;
      }
    });
  }

  loadSuggestedPeople() {
    this.apiService.getDirectory({ page_size: 20 }).subscribe({
      next: (res: any) => {
        const results = res.results || res;
        const users = Array.isArray(results) ? results : [];
        const currentId = this.currentUser?.id;
        const others = users.filter((u: any) => u.id !== currentId);
        const shuffled = others.sort(() => Math.random() - 0.5).slice(0, 5);
        this.suggestedPeople = shuffled.map((u: any) => ({
          id: u.id,
          name: `${u.first_name || ''} ${u.last_name || ''}`.trim() || u.username || 'User',
          role: u.current_position || u.company_name || u.role || '',
          initials: (`${u.first_name?.[0] || ''}${u.last_name?.[0] || ''}`).toUpperCase() || 'U',
          color: this.getColor(u.id || 0),
          connectionStatus: 'none'
        }));
        this.suggestedPeople.forEach(p => {
          if (p.id) {
            this.apiService.getConnectionStatus(p.id).subscribe({
              next: (s: any) => {
                p.connectionStatus = s.status || 'none';
                p.connectionId = s.connection_id;
              },
              error: () => {}
            });
          }
        });
      }
    });
  }

  loadConnectionData() {
    this.apiService.getConnectionCount().subscribe({
      next: (res: any) => this.connectionCount = res.count || 0,
      error: () => {}
    });
    this.apiService.getPendingConnections().subscribe({
      next: (res: any) => this.pendingRequests = Array.isArray(res) ? res : [],
      error: () => {}
    });
  }

  sendConnection(person: any) {
    if (!person.id) return;
    this.apiService.sendConnectionRequest(person.id).subscribe({
      next: () => {
        person.connectionStatus = 'pending';
      },
      error: (err: any) => {
        const msg = err.error?.detail || 'Failed to send request';
        console.error(msg);
      }
    });
  }

  acceptPending(req: any) {
    this.apiService.acceptConnection(req.id).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id);
        this.connectionCount++;
      }
    });
  }

  rejectPending(req: any) {
    this.apiService.rejectConnection(req.id).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id);
      }
    });
  }

  viewProfile(userId: number) {
    this.router.navigate(['/app/user', userId]);
  }

  private getColor(id: number): string {
    return this.colors[id % this.colors.length];
  }

  private getRelativeTime(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'Just now';
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 30) return `${days}d ago`;
    return `${Math.floor(days / 30)}mo ago`;
  }
}
