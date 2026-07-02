import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiService } from '../../core/services/ai.service';

@Component({
  selector: 'app-ai-cv-reviewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-cv-reviewer.component.html',
  styleUrl: './ai-cv-reviewer.component.scss'
})
export class AiCvReviewerComponent {
  isAnalyzing = false;
  isAnalyzed = false;
  fileName = '';
  overallScore = 0;
  selectedFile: File | null = null;
  errorMessage = '';

  scores: {label: string; score: number; icon: string; color: string; tip: string}[] = [];
  improvements: {type: string; icon: string; text: string; section: string}[] = [];
  matchingJobs: {title: string; company: string; match: number; missing: string[]}[] = [];
  certRecommendations: {name: string; impact: string; difficulty: string; time: string}[] = [];

  constructor(private aiService: AiService) {}

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    const validTypes = ['.pdf', '.docx', '.txt'];
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    if (!validTypes.includes(ext)) {
      this.errorMessage = 'Please upload a PDF, DOCX, or TXT file.';
      return;
    }

    this.fileName = file.name;
    this.selectedFile = file;
    this.isAnalyzing = true;
    this.isAnalyzed = false;
    this.errorMessage = '';

    this.aiService.analyzeCv(file).subscribe({
      next: (res) => {
        this.overallScore = res.overall_score ?? 0;

        if (res.scores?.length) {
          this.scores = res.scores.map((s: any) => ({
            label: s.label, score: s.score, icon: s.icon || 'analytics',
            color: this.getScoreColor(s.score),
            tip: s.tip || s.description || ''
          }));
        }
        if (res.improvements?.length) {
          this.improvements = res.improvements.map((imp: any) => ({
            type: imp.priority === 'high' ? 'critical' : imp.priority === 'medium' ? 'warning' : 'suggestion',
            icon: imp.priority === 'high' ? 'error' : imp.priority === 'medium' ? 'warning' : 'lightbulb',
            text: imp.description || imp.title,
            section: imp.title || imp.section || ''
          }));
        }
        if (res.matching_jobs?.length) {
          this.matchingJobs = res.matching_jobs.map((j: any) => ({
            title: j.title, company: j.company, match: j.match, missing: j.missing || []
          }));
        }
        if (res.cert_recommendations?.length) {
          this.certRecommendations = res.cert_recommendations.map((c: any) => ({
            name: c.name, impact: c.relevance || c.impact || '',
            difficulty: c.difficulty || 'Intermediate', time: c.time || '6 weeks'
          }));
        }
        this.isAnalyzing = false;
        this.isAnalyzed = true;
      },
      error: (err) => {
        this.isAnalyzing = false;
        this.isAnalyzed = false;
        this.errorMessage = err.error?.error || 'Failed to analyze CV. Please try again.';
      }
    });
  }

  resetUpload() {
    this.isAnalyzed = false;
    this.isAnalyzing = false;
    this.fileName = '';
    this.selectedFile = null;
    this.errorMessage = '';
    this.overallScore = 0;
    this.scores = [];
    this.improvements = [];
    this.matchingJobs = [];
    this.certRecommendations = [];
  }

  getTypeColor(type: string): string {
    if (type === 'critical') return '#E63946';
    if (type === 'warning') return '#F59E0B';
    return '#4338CA';
  }

  getScoreColor(score: number): string {
    if (score >= 80) return '#059669';
    if (score >= 60) return '#4338CA';
    if (score >= 40) return '#EA580C';
    return '#E63946';
  }
}
