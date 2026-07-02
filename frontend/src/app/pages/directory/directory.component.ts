import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-directory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './directory.component.html',
  styleUrl: './directory.component.scss'
})
export class DirectoryComponent implements OnInit {
  isLoading = true;
  searchQuery = '';
  selectedRole = 'all';

  roles = [
    { value: 'all', label: 'All', icon: 'groups' },
    { value: 'student', label: 'Students', icon: 'school' },
    { value: 'alumni', label: 'Alumni', icon: 'people' },
    { value: 'company', label: 'Companies', icon: 'business' },
  ];

  users: any[] = [];
  private colors = ['#4338CA', '#E63946', '#059669', '#EA580C', '#7C3AED', '#0891B2', '#DB2777', '#65A30D'];
  private bgs = ['#1B2A4A', '#7F1D1D', '#064E3B', '#7C2D12', '#4C1D95', '#164E63', '#831843', '#365314'];

  connectionStatuses: {[userId: number]: {status: string, id?: number}} = {};
  currentUser: any = null;

  constructor(private apiService: ApiService, private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(u => this.currentUser = u);
    if (!this.currentUser) this.authService.getProfile().subscribe();
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading = true;
    const params: any = {};
    if (this.searchQuery) params.search = this.searchQuery;
    if (this.selectedRole !== 'all') params.role = this.selectedRole.toUpperCase();

    this.apiService.getDirectory(params).subscribe({
      next: (res) => {
        const results = res.results || res;
        this.users = (Array.isArray(results) ? results : []).map((u: any) => ({
          id: u.id,
          name: `${u.first_name || ''} ${u.last_name || ''}`.trim() || u.username,
          initials: `${u.first_name?.[0] || ''}${u.last_name?.[0] || ''}`.toUpperCase() || 'U',
          color: this.colors[u.id % this.colors.length],
          role: (u.role || 'student').toLowerCase(),
          position: u.current_position || u.role || '',
          company: u.company_name || '',
          location: u.location || '',
          skills: u.skills ? u.skills.split(',').map((s: string) => s.trim()).filter((s: string) => s) : [],
          mentor: u.is_mentor || false,
          bg: this.bgs[u.id % this.bgs.length]
        }));
        this.isLoading = false;
        this.loadConnectionStatuses();
      },
      error: () => { this.isLoading = false; }
    });
  }

  loadConnectionStatuses() {
    this.users.forEach(u => {
      if (u.id !== this.currentUser?.id) {
        this.apiService.getConnectionStatus(u.id).subscribe({
          next: (res: any) => {
            this.connectionStatuses[u.id] = { status: res.status || 'none', id: res.connection_id };
          },
          error: () => { this.connectionStatuses[u.id] = { status: 'none' }; }
        });
      }
    });
  }

  getConnStatus(userId: number): string {
    return this.connectionStatuses[userId]?.status || 'none';
  }

  viewProfile(userId: number) {
    if (userId === this.currentUser?.id) {
      this.router.navigate(['/app/profile']);
    } else {
      this.router.navigate(['/app/user', userId]);
    }
  }

  sendConnection(userId: number) {
    this.apiService.sendConnectionRequest(userId).subscribe({
      next: () => { this.connectionStatuses[userId] = { status: 'pending' }; },
      error: () => {}
    });
  }

  onSearch() { this.loadUsers(); }
  onFilterChange() { this.loadUsers(); }

  get filteredUsers() {
    return this.users.filter(u => {
      const matchRole = this.selectedRole === 'all' || u.role === this.selectedRole;
      const matchSearch = !this.searchQuery || u.name.toLowerCase().includes(this.searchQuery.toLowerCase()) || u.company.toLowerCase().includes(this.searchQuery.toLowerCase());
      return matchRole && matchSearch;
    });
  }

  get statsCount() {
    return {
      total: this.users.length,
      alumni: this.users.filter(u => u.role === 'alumni').length,
      students: this.users.filter(u => u.role === 'student').length,
      companies: this.users.filter(u => u.role === 'company').length,
    };
  }
}
