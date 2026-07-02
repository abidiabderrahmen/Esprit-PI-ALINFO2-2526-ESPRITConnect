import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  analyzeCv(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/ai/analyze-cv/`, formData);
  }

  sendChatMessage(message: string, history: any[]): Observable<any> {
    return this.http.post(`${this.apiUrl}/ai/chat/`, { message, history });
  }

  generateCoverLetter(jobDescription: string, cvText?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/ai/generate-cover-letter/`, {
      job_description: jobDescription,
      cv_text: cvText || ''
    });
  }

  getCareerCopilot(): Observable<any> {
    return this.http.get(`${this.apiUrl}/ai/career-copilot/`);
  }

  getMatchScore(opportunityId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/ai/match-score/`, { opportunity_id: opportunityId });
  }
}
