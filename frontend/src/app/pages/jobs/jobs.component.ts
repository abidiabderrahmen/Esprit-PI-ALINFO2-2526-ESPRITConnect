import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-jobs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './jobs.component.html',
  styleUrl: './jobs.component.scss'
})
export class JobsComponent implements OnInit {
  isLoading = true;
  searchQuery = '';
  userSkills: string[] = [];
  hasSkills = false;
  jobs: any[] = [];
  filteredJobs: any[] = [];
  selectedFilter = 'all';

  constructor(private apiService: ApiService, private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(u => {
      if (u) {
        this.userSkills = u.skills ? u.skills.split(',').map((s: string) => s.trim()).filter((s: string) => s) : [];
      }
    });
    if (!this.authService.currentUser) {
      this.authService.getProfile().subscribe(() => this.loadJobs());
    } else {
      this.loadJobs();
    }
  }

  loadJobs() {
    this.isLoading = true;
    const params: any = {};
    if (this.searchQuery.trim()) params.query = this.searchQuery;

    this.apiService.searchJobs(params).subscribe({
      next: (res: any) => {
        this.jobs = res.jobs || [];
        this.userSkills = res.skills || this.userSkills;
        this.hasSkills = res.has_skills ?? this.userSkills.length > 0;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.jobs = [];
        this.filteredJobs = [];
      }
    });
  }

  onSearch() {
    this.loadJobs();
  }

  setFilter(filter: string) {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  applyFilter() {
    if (this.selectedFilter === 'all') {
      this.filteredJobs = [...this.jobs];
    } else {
      this.filteredJobs = this.jobs.filter(j => {
        const seniority = (j.seniority || '').toLowerCase();
        if (this.selectedFilter === 'entry') return seniority === 'entry' || seniority === 'junior' || seniority === 'intern';
        if (this.selectedFilter === 'mid') return seniority === 'mid' || seniority === 'middle';
        if (this.selectedFilter === 'senior') return seniority === 'senior' || seniority === 'lead' || seniority === 'executive';
        return true;
      });
    }
  }

  openJob(job: any) {
    if (job.url) {
      window.open(job.url, '_blank');
    }
  }

  goToProfile() {
    this.router.navigate(['/app/profile']);
  }

  getTimeAgo(dateStr: string): string {
    if (!dateStr) return '';
    try {
      const posted = new Date(dateStr);
      const now = new Date();
      const diffMs = now.getTime() - posted.getTime();
      const diffH = Math.floor(diffMs / 3600000);
      if (diffH < 1) return 'Just now';
      if (diffH < 24) return diffH + 'h ago';
      const diffD = Math.floor(diffH / 24);
      if (diffD === 1) return '1 day ago';
      if (diffD < 7) return diffD + ' days ago';
      if (diffD < 30) return Math.floor(diffD / 7) + 'w ago';
      return Math.floor(diffD / 30) + 'mo ago';
    } catch {
      return '';
    }
  }

  getCompanyInitial(name: string): string {
    if (!name) return '?';
    return name.charAt(0).toUpperCase();
  }

  getSkillMatch(job: any): number {
    if (!this.userSkills.length || !job.skills?.length) return 0;
    const jobSkills = job.skills.map((s: string) => s.toLowerCase());
    const matched = this.userSkills.filter(s => jobSkills.some((js: string) => js.includes(s.toLowerCase()) || s.toLowerCase().includes(js)));
    return Math.min(Math.round((matched.length / this.userSkills.length) * 100), 100);
  }

  isSkillMatched(skill: string): boolean {
    if (!this.userSkills.length || !skill) return false;
    const lower = skill.toLowerCase();
    return this.userSkills.some(us => us.toLowerCase().includes(lower) || lower.includes(us.toLowerCase()));
  }

  getSeniorityLabel(s: string): string {
    if (!s) return '';
    const map: any = { entry: 'Entry Level', junior: 'Junior', mid: 'Mid-Level', middle: 'Mid-Level', senior: 'Senior', lead: 'Lead', executive: 'Executive', intern: 'Internship' };
    return map[s.toLowerCase()] || s;
  }
}
