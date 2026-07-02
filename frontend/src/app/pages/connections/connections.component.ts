import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-connections',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './connections.component.html',
  styleUrl: './connections.component.scss'
})
export class ConnectionsComponent implements OnInit {
  activeTab: 'connections' | 'pending' = 'connections';
  connections: any[] = [];
  pendingRequests: any[] = [];
  isLoading = true;

  private colors = ['#4338CA', '#E63946', '#059669', '#EA580C', '#7C3AED', '#0891B2', '#DB2777', '#65A30D'];

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.isLoading = true;
    this.apiService.getConnections().subscribe({
      next: (res: any) => {
        this.connections = (Array.isArray(res) ? res : []).map((c: any) => ({
          connectionId: c.connection_id,
          user: c.user,
          name: `${c.user?.first_name || ''} ${c.user?.last_name || ''}`.trim() || c.user?.username || 'User',
          role: c.user?.current_position || c.user?.company_name || c.user?.role || '',
          initials: (`${c.user?.first_name?.[0] || ''}${c.user?.last_name?.[0] || ''}`).toUpperCase() || 'U',
          color: this.colors[(c.user?.id || 0) % this.colors.length],
          connectedAt: c.connected_at
        }));
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });

    this.apiService.getPendingConnections().subscribe({
      next: (res: any) => {
        this.pendingRequests = (Array.isArray(res) ? res : []).map((r: any) => ({
          id: r.id,
          sender: r.sender,
          name: `${r.sender?.first_name || ''} ${r.sender?.last_name || ''}`.trim() || r.sender?.username || 'User',
          role: r.sender?.current_position || r.sender?.role || '',
          initials: (`${r.sender?.first_name?.[0] || ''}${r.sender?.last_name?.[0] || ''}`).toUpperCase() || 'U',
          color: this.colors[(r.sender?.id || 0) % this.colors.length],
          sentAt: r.created_at
        }));
      }
    });
  }

  viewProfile(userId: number) {
    this.router.navigate(['/app/user', userId]);
  }

  acceptRequest(req: any) {
    this.apiService.acceptConnection(req.id).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id);
        this.loadAll();
      }
    });
  }

  rejectRequest(req: any) {
    this.apiService.rejectConnection(req.id).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id);
      }
    });
  }

  removeConnection(conn: any) {
    this.apiService.removeConnection(conn.connectionId).subscribe({
      next: () => {
        this.connections = this.connections.filter(c => c.connectionId !== conn.connectionId);
      }
    });
  }

  getRelativeTime(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return days < 30 ? `${days}d ago` : `${Math.floor(days / 30)}mo ago`;
  }
}
