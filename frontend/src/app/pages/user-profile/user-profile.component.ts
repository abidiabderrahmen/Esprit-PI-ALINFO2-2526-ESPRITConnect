import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss'
})
export class UserProfileComponent implements OnInit {
  user: any = null;
  isLoading = true;
  isConnected = false;
  connectionStatus = 'none';
  connectionId: number | null = null;
  isSender = false;
  showPrivatePopup = false;
  isOwnProfile = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const userId = Number(this.route.snapshot.paramMap.get('id'));
    if (!userId) { this.router.navigate(['/app/feed']); return; }

    const currentUser = this.authService.currentUser;
    if (currentUser && currentUser.id === userId) {
      this.router.navigate(['/app/profile']);
      return;
    }

    this.apiService.getUserById(userId).subscribe({
      next: (user: any) => {
        this.user = user;
        this.checkConnectionStatus(userId);
      },
      error: () => {
        this.isLoading = false;
        this.router.navigate(['/app/feed']);
      }
    });
  }

  private checkConnectionStatus(userId: number) {
    this.apiService.getConnectionStatus(userId).subscribe({
      next: (res: any) => {
        this.connectionStatus = res.status || 'none';
        this.connectionId = res.connection_id || null;
        this.isSender = res.is_sender || false;
        this.isConnected = this.connectionStatus === 'accepted';
        this.isLoading = false;
      },
      error: () => {
        this.connectionStatus = 'none';
        this.isConnected = false;
        this.isLoading = false;
      }
    });
  }

  get initials(): string {
    return (`${this.user?.first_name?.[0] || ''}${this.user?.last_name?.[0] || ''}`).toUpperCase() || 'U';
  }

  get fullName(): string {
    return `${this.user?.first_name || ''} ${this.user?.last_name || ''}`.trim() || this.user?.username || 'User';
  }

  get skills(): string[] {
    if (!this.user?.skills) return [];
    return this.user.skills.split(',').map((s: string) => s.trim()).filter((s: string) => s);
  }

  sendConnection() {
    this.apiService.sendConnectionRequest(this.user.id).subscribe({
      next: () => { this.connectionStatus = 'pending'; },
      error: () => {}
    });
  }

  removeConnection() {
    if (this.connectionId) {
      this.apiService.removeConnection(this.connectionId).subscribe({
        next: () => {
          this.connectionStatus = 'none';
          this.isConnected = false;
          this.connectionId = null;
        }
      });
    }
  }

  tryViewDetails() {
    if (!this.isConnected) {
      this.showPrivatePopup = true;
    }
  }

  closePopup() {
    this.showPrivatePopup = false;
  }
}
