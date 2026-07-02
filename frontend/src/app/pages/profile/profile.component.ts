import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ApiService } from '../../core/services/api.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, MatSnackBarModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  isEditing = false;
  isSaving = false;
  skillsInput = '';

  profile: any = {
    first_name: '', last_name: '', initials: '',
    position: '', company: '', location: '',
    bio: '', email: '', phone: '',
    linkedin: '', github: '',
    skills: [] as string[],
    role: '', graduation_year: null, is_mentor: false
  };

  stats = { connections: 0, posts: 0 };

  activities: any[] = [];

  constructor(private authService: AuthService, private apiService: ApiService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(u => {
      if (u) {
        this.profile = {
          first_name: u.first_name || '',
          last_name: u.last_name || '',
          initials: `${u.first_name?.[0] || ''}${u.last_name?.[0] || ''}`.toUpperCase(),
          position: u.current_position || '',
          company: u.company_name || '',
          location: u.location || '',
          bio: u.bio || '',
          email: u.email || '',
          phone: u.phone || '',
          linkedin: u.linkedin_url || '',
          github: u.github_url || '',
          skills: u.skills ? u.skills.split(',').map((s: string) => s.trim()).filter((s: string) => s) : [],
          role: u.role || '',
          graduation_year: u.graduation_year,
          is_mentor: u.is_mentor || false
        };
        this.skillsInput = this.profile.skills.join(', ');
      }
    });
    this.loadStats();
  }

  loadStats() {
    this.apiService.getConnectionCount().subscribe({
      next: (res: any) => this.stats.connections = res.count || 0,
      error: () => {}
    });
    this.apiService.getPosts().subscribe({
      next: (res: any) => {
        const results = res.results || res;
        const allPosts = Array.isArray(results) ? results : [];
        const currentUser = this.authService.currentUser;
        if (currentUser) {
          this.stats.posts = allPosts.filter((p: any) => {
            const authorId = p.author_details?.id || p.author;
            return authorId === currentUser.id;
          }).length;
        }
      },
      error: () => {}
    });
  }

  splitSkills(input: string): string[] {
    return input ? input.split(',').map(s => s.trim()).filter(s => s) : [];
  }

  saveProfile() {
    this.isSaving = true;
    this.profile.skills = this.splitSkills(this.skillsInput);
    const data: any = {
      first_name: this.profile.first_name,
      last_name: this.profile.last_name,
      bio: this.profile.bio,
      phone: this.profile.phone,
      linkedin_url: this.profile.linkedin,
      github_url: this.profile.github,
      current_position: this.profile.position,
      company_name: this.profile.company,
      location: this.profile.location,
      skills: this.profile.skills.join(', '),
    };

    this.authService.updateProfile(data).subscribe({
      next: () => {
        this.isSaving = false;
        this.isEditing = false;
        this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000, panelClass: 'success-snack' });
      },
      error: () => {
        this.isSaving = false;
        this.snackBar.open('Failed to update profile.', 'Close', { duration: 3000, panelClass: 'error-snack' });
      }
    });
  }
}
