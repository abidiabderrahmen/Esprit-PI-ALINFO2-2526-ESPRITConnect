import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AiService } from '../../core/services/ai.service';

@Component({
  selector: 'app-ai-copilot',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ai-copilot.component.html',
  styleUrl: './ai-copilot.component.scss'
})
export class AiCopilotComponent implements OnInit {
  isLoading = true;
  employabilityScore = 0;
  targetScore = 78;
  animatedSkills: any[] = [];

  skills: any[] = [];

  skillGaps: any[] = [];

  careerRoadmap: any[] = [];

  mentorMatches: any[] = [];

  jobMatches: any[] = [];

  interviewReadiness: any[] = [];

  cvScore: {[key: string]: number} = { ats: 0, content: 0, format: 0, keywords: 0 };

  constructor(private aiService: AiService) {}

  ngOnInit() {
    this.aiService.getCareerCopilot().subscribe({
      next: (res) => {
        if (res.employability_score) this.targetScore = res.employability_score;
        if (res.skills?.length) {
          this.skills = res.skills.map((s: any) => ({
            name: s.name, level: s.level, category: s.category || 'technical'
          }));
        }
        if (res.skill_gaps?.length) {
          this.skillGaps = res.skill_gaps.map((g: any) => ({
            skill: g.skill, priority: g.importance || g.priority || 'medium',
            reason: g.description || g.resources || '', progress: g.progress || 10
          }));
        }
        if (res.career_roadmap?.length) {
          this.careerRoadmap = res.career_roadmap.map((r: any, i: number) => ({
            phase: r.phase, title: r.title, status: i === 0 ? 'active' : i === 1 ? 'next' : 'future',
            items: r.description ? [r.description] : r.items || []
          }));
        }
        if (res.job_matches?.length) {
          this.jobMatches = res.job_matches.map((j: any) => ({
            title: j.title, company: j.company, match: j.match,
            salary: j.salary || '', type: j.type || 'job', location: j.location || ''
          }));
        }
        if (res.mentor_matches?.length) {
          this.mentorMatches = res.mentor_matches.map((m: any, i: number) => ({
            name: m.name, role: m.expertise || m.role || '',
            match: m.match, skills: m.skills || [],
            initials: m.name?.split(' ').map((w: string) => w[0]).join('').toUpperCase() || 'M',
            color: ['#7C3AED', '#4338CA', '#DB2777'][i % 3]
          }));
        }
        if (res.interview_readiness) {
          const ir = res.interview_readiness;
          this.interviewReadiness = [
            { area: 'Technical', score: ir.technical || 65, color: '#4338CA' },
            { area: 'System Design', score: ir.system_design || 35, color: '#E63946' },
            { area: 'Behavioral', score: ir.behavioral || 80, color: '#059669' },
            { area: 'Overall', score: ir.overall || 70, color: '#EA580C' },
          ];
        }
        if (res.cv_score) this.cvScore = res.cv_score;
        this.startAnimations();
        this.isLoading = false;
      },
      error: () => {
        this.startAnimations();
        this.isLoading = false;
      }
    });
  }

  private startAnimations() {
    this.animateScore();
    this.animatedSkills = this.skills.map(s => ({...s, animLevel: 0}));
    setTimeout(() => {
      this.animatedSkills = this.skills.map(s => ({...s, animLevel: s.level}));
    }, 300);
  }

  animateScore() {
    this.employabilityScore = 0;
    const interval = setInterval(() => {
      if (this.employabilityScore < this.targetScore) {
        this.employabilityScore += 1;
      } else { clearInterval(interval); }
    }, 20);
  }

  getScoreColor(score: number): string {
    if (score >= 80) return '#059669';
    if (score >= 60) return '#4338CA';
    if (score >= 40) return '#EA580C';
    return '#E63946';
  }

  getMatchColor(match: number): string {
    if (match >= 90) return '#059669';
    if (match >= 75) return '#4338CA';
    return '#EA580C';
  }

  getPriorityColor(p: string): string {
    if (p === 'high') return '#E63946';
    if (p === 'medium') return '#EA580C';
    return '#4338CA';
  }
}
