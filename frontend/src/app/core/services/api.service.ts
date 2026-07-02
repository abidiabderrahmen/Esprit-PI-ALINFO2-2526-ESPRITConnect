import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Users
  getUsers(params?: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/`, { params });
  }

  getUserById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/${id}/`);
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/me/`, data);
  }

  // Directory
  getDirectory(params?: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/directory/`, { params });
  }

  getMentors(): Observable<any> {
    return this.http.get(`${this.apiUrl}/directory/mentors/`);
  }

  requestMentorship(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/mentorship/`, data);
  }

  // Opportunities
  getOpportunities(params?: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/opportunities/`, { params });
  }

  getOpportunityById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/opportunities/${id}/`);
  }

  createOpportunity(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/opportunities/`, data);
  }

  applyToOpportunity(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/applications/`, data);
  }

  // Events
  getEvents(params?: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/events/`, { params });
  }

  getEventById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/events/${id}/`);
  }

  createEvent(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/events/`, data);
  }

  registerForEvent(eventId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/events/${eventId}/register/`, {});
  }

  unregisterFromEvent(eventId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/events/${eventId}/unregister/`, {});
  }

  // Feed
  getPosts(params?: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/feed/`, { params });
  }

  createPost(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/feed/`, data);
  }

  likePost(postId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/feed/${postId}/like/`, {});
  }

  unlikePost(postId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/feed/${postId}/unlike/`, {});
  }

  getComments(postId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/feed/${postId}/get_comments/`);
  }

  commentOnPost(postId: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/feed/${postId}/comment/`, { content });
  }

  // Dashboard
  getDashboardStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard/stats/`);
  }

  // Connections
  sendConnectionRequest(receiverId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/connections/send/`, { receiver_id: receiverId });
  }

  acceptConnection(connectionId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/connections/${connectionId}/accept/`, {});
  }

  rejectConnection(connectionId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/connections/${connectionId}/reject/`, {});
  }

  removeConnection(connectionId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/connections/${connectionId}/`);
  }

  getConnections(): Observable<any> {
    return this.http.get(`${this.apiUrl}/connections/`);
  }

  getPendingConnections(): Observable<any> {
    return this.http.get(`${this.apiUrl}/connections/pending/`);
  }

  getConnectionStatus(userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/connections/status/${userId}/`);
  }

  getConnectionCount(): Observable<any> {
    return this.http.get(`${this.apiUrl}/connections/count/`);
  }

  // Jobs (AI-powered external job search)
  searchJobs(params?: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/jobs/search/`, { params });
  }
}
