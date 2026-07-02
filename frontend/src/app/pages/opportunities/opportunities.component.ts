import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-opportunities',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './opportunities.component.html',
  styleUrl: './opportunities.component.scss'
})
export class OpportunitiesComponent implements OnInit {
  isLoading = true;
  searchQuery = '';
  selectedType = 'all';

  types = [
    { value: 'all', label: 'All', icon: 'apps' },
    { value: 'job', label: 'Jobs', icon: 'work' },
    { value: 'internship', label: 'Internships', icon: 'school' },
    { value: 'freelance', label: 'Freelance', icon: 'laptop' },
  ];

  opportunities: any[] = [];
  showCreateModal = false;
  showApplyModal = false;
  selectedOpp: any = null;
  newOpp: any = { title: '', description: '', opportunity_type: 'JOB', company_name: '', location: '', salary_range: '', requirements: '', is_remote: false, deadline: '' };
  coverLetter = '';

  private typeStyles: {[key: string]: any} = {
    'job': { typeColor: '#EEF2FF', typeTextColor: '#4338CA', iconBg: '#EEF2FF', iconColor: '#4338CA' },
    'internship': { typeColor: '#ECFDF5', typeTextColor: '#059669', iconBg: '#ECFDF5', iconColor: '#059669' },
    'freelance': { typeColor: '#FFF7ED', typeTextColor: '#EA580C', iconBg: '#FFF7ED', iconColor: '#EA580C' },
    'project': { typeColor: '#F3E8FF', typeTextColor: '#7C3AED', iconBg: '#F3E8FF', iconColor: '#7C3AED' },
  };

  constructor(private apiService: ApiService, private authService: AuthService) {}

  ngOnInit() { this.loadOpportunities(); }

  loadOpportunities() {
    this.isLoading = true;
    const params: any = {};
    if (this.searchQuery) params.search = this.searchQuery;
    if (this.selectedType !== 'all') params.opportunity_type = this.selectedType.toUpperCase();

    this.apiService.getOpportunities(params).subscribe({
      next: (res) => {
        const results = res.results || res;
        this.opportunities = (Array.isArray(results) ? results : []).map((o: any) => {
          const type = (o.opportunity_type || 'job').toLowerCase();
          const style = this.typeStyles[type] || this.typeStyles['job'];
          return {
            id: o.id,
            title: o.title,
            company: o.company_name,
            type,
            ...style,
            location: o.location,
            salary: o.salary_range || 'Not specified',
            remote: o.is_remote,
            deadline: this.formatDate(o.deadline),
            description: o.description,
            posted: this.getRelativeTime(o.created_at),
            aiMatch: null
          };
        });
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  onSearch() { this.loadOpportunities(); }
  onFilterChange() { this.loadOpportunities(); }

  get filteredOpportunities() {
    return this.opportunities.filter(o => {
      const matchType = this.selectedType === 'all' || o.type === this.selectedType;
      const matchSearch = !this.searchQuery || o.title.toLowerCase().includes(this.searchQuery.toLowerCase()) || o.company.toLowerCase().includes(this.searchQuery.toLowerCase());
      return matchType && matchSearch;
    });
  }

  createOpportunity() {
    if (!this.newOpp.title?.trim() || !this.newOpp.description?.trim()) return;
    this.apiService.createOpportunity(this.newOpp).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.newOpp = { title: '', description: '', opportunity_type: 'JOB', company_name: '', location: '', salary_range: '', requirements: '', is_remote: false, deadline: '' };
        this.loadOpportunities();
      },
      error: () => {}
    });
  }

  openApply(opp: any) {
    this.selectedOpp = opp;
    this.coverLetter = '';
    this.showApplyModal = true;
  }

  submitApplication() {
    if (!this.coverLetter.trim() || !this.selectedOpp) return;
    this.apiService.applyToOpportunity({ opportunity: this.selectedOpp.id, cover_letter: this.coverLetter }).subscribe({
      next: () => {
        this.showApplyModal = false;
        this.selectedOpp = null;
        this.coverLetter = '';
      },
      error: () => {}
    });
  }

  private formatDate(d: string): string {
    if (!d) return '';
    return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  private getRelativeTime(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const days = Math.floor(diff / 86400000);
    if (days < 1) return 'Today';
    if (days === 1) return '1 day ago';
    if (days < 7) return `${days} days ago`;
    if (days < 30) return `${Math.floor(days / 7)} week${days >= 14 ? 's' : ''} ago`;
    return `${Math.floor(days / 30)} month${days >= 60 ? 's' : ''} ago`;
  }
}
