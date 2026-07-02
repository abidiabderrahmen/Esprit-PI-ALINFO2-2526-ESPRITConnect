import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  isLoading = true;

  stats = [
    { label: 'Total Users', value: '0', change: '', positive: true, icon: 'people', iconBg: '#EEF2FF', iconColor: '#4338CA' },
    { label: 'Active Opportunities', value: '0', change: '', positive: true, icon: 'work', iconBg: '#ECFDF5', iconColor: '#059669' },
    { label: 'Upcoming Events', value: '0', change: '', positive: true, icon: 'event', iconBg: '#FFF7ED', iconColor: '#EA580C' },
    { label: 'Monthly Posts', value: '0', change: '', positive: true, icon: 'forum', iconBg: '#F3E8FF', iconColor: '#7C3AED' },
  ];

  chartData = [
    { month: 'Jan', students: 0, alumni: 0, companies: 0 },
    { month: 'Feb', students: 0, alumni: 0, companies: 0 },
    { month: 'Mar', students: 0, alumni: 0, companies: 0 },
    { month: 'Apr', students: 0, alumni: 0, companies: 0 },
    { month: 'May', students: 0, alumni: 0, companies: 0 },
    { month: 'Jun', students: 0, alumni: 0, companies: 0 },
  ];

  activityMetrics = [
    { label: 'Profile Completeness', value: 78, color: '#4338CA' },
    { label: 'Job Application Rate', value: 64, color: '#059669' },
    { label: 'Event Attendance', value: 82, color: '#EA580C' },
    { label: 'Mentor Engagement', value: 45, color: '#7C3AED' },
  ];

  recentUsers: any[] = [];
  recentOpportunities: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getDashboardStats().subscribe({
      next: (res) => {
        const byRole = res.users_by_role || {};
        this.stats = [
          { label: 'Total Users', value: String(res.total_users || 0), change: '+' + (byRole.STUDENT || 0) + ' students', positive: true, icon: 'people', iconBg: '#EEF2FF', iconColor: '#4338CA' },
          { label: 'Active Opportunities', value: String(res.total_opportunities || 0), change: '', positive: true, icon: 'work', iconBg: '#ECFDF5', iconColor: '#059669' },
          { label: 'Upcoming Events', value: String(res.total_events || 0), change: '', positive: true, icon: 'event', iconBg: '#FFF7ED', iconColor: '#EA580C' },
          { label: 'Total Posts', value: String(res.total_posts || 0), change: '', positive: true, icon: 'forum', iconBg: '#F3E8FF', iconColor: '#7C3AED' },
        ];

        this.recentUsers = (res.recent_users || []).map((u: any) => ({
          name: `${u.first_name || ''} ${u.last_name || ''}`.trim() || u.username,
          email: u.email,
          initials: `${u.first_name?.[0] || ''}${u.last_name?.[0] || ''}`.toUpperCase() || 'U',
          color: ['#4338CA', '#E63946', '#059669', '#7C3AED'][u.id % 4],
          role: (u.role || 'student').toLowerCase(),
          date: new Date(u.created_at).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
          status: 'active'
        }));

        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }
}
