import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

let isRefreshing = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const http = inject(HttpClient);

  const token = localStorage.getItem('accessToken');
  let authReq = req;

  if (token) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/token')) {
        if (!isRefreshing) {
          isRefreshing = true;
          const refreshToken = localStorage.getItem('refreshToken');

          if (refreshToken) {
            return http.post<any>(`${environment.apiUrl}/auth/token/refresh/`, { refresh: refreshToken }).pipe(
              switchMap((res) => {
                isRefreshing = false;
                localStorage.setItem('accessToken', res.access);
                const retryReq = req.clone({
                  setHeaders: { Authorization: `Bearer ${res.access}` }
                });
                return next(retryReq);
              }),
              catchError((refreshError) => {
                isRefreshing = false;
                localStorage.clear();
                router.navigate(['/login']);
                return throwError(() => refreshError);
              })
            );
          }
        }

        localStorage.clear();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
