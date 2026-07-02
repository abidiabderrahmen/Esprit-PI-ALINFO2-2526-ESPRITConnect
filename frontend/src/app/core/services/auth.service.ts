import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, AuthResponse, RegisterData } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const user = localStorage.getItem('currentUser');
    if (user) {
      this.currentUserSubject.next(JSON.parse(user));
    }
  }

  login(credentials: { username: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/token/`, credentials).pipe(
      tap(res => {
        localStorage.setItem('accessToken', res.access);
        localStorage.setItem('refreshToken', res.refresh);
      })
    );
  }

  register(userData: RegisterData): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/users/register/`, userData);
  }

  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/me/`).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        localStorage.setItem('currentUser', JSON.stringify(user));
      })
    );
  }

  updateProfile(data: any): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/users/me/`, data).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        localStorage.setItem('currentUser', JSON.stringify(user));
      })
    );
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/forgot-password/`, { email });
  }

  resetPassword(data: { uid: string; token: string; password: string; password_confirm: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/reset-password/`, data);
  }

  changePassword(data: { old_password: string; new_password: string; new_password_confirm: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/change-password/`, data);
  }

  logout(): void {
    localStorage.clear();
    this.currentUserSubject.next(null);
  }

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  get token(): string | null {
    return localStorage.getItem('accessToken');
  }
}
