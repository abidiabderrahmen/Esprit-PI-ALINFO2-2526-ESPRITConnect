import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { AiService } from '../../core/services/ai.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent implements OnInit {
  currentUser: User | null = null;
  showChat = false;
  showVoice = false;
  chatMessages: {role: string, text: string}[] = [
    {role: 'ai', text: 'Hi! I\'m your AI Career Assistant. I can help with CV reviews, job matching, interview prep, and career advice. What can I help you with?'}
  ];
  chatInput = '';
  isChatLoading = false;
  voiceListening = false;
  voiceText = '';

  navItems = [
    { icon: 'dynamic_feed', label: 'Feed', route: '/app/feed' },
    { icon: 'people', label: 'Directory', route: '/app/directory' },
    { icon: 'group', label: 'Connections', route: '/app/connections' },
    { icon: 'work_outline', label: 'Jobs', route: '/app/jobs' },
    { icon: 'event', label: 'Events', route: '/app/events' },
    { icon: 'person', label: 'Profile', route: '/app/profile' },
  ];

  aiItems = [
    { icon: 'auto_awesome', label: 'AI Copilot', route: '/app/ai-copilot' },
    { icon: 'description', label: 'CV Reviewer', route: '/app/ai-cv' },
  ];

  constructor(
    private authService: AuthService,
    private aiService: AiService,
    private router: Router
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(u => this.currentUser = u);
    if (!this.currentUser) this.authService.getProfile().subscribe();
  }

  get userInitials(): string {
    if (!this.currentUser) return 'U';
    return (this.currentUser.first_name?.[0] || '') + (this.currentUser.last_name?.[0] || '');
  }

  getPageTitle(): string {
    const p = this.router.url;
    if (p.includes('feed')) return 'News Feed';
    if (p.includes('directory')) return 'Directory';
    if (p.includes('jobs')) return 'AI Job Finder';
    if (p.includes('opportunities')) return 'Opportunities';
    if (p.includes('events')) return 'Events';
    if (p.includes('user/')) return 'User Profile';
    if (p.includes('connections')) return 'My Connections';
    if (p.includes('profile')) return 'My Profile';
    if (p.includes('admin')) return 'Admin Dashboard';
    if (p.includes('ai-copilot')) return 'AI Career Copilot';
    if (p.includes('ai-cv')) return 'AI CV Reviewer';
    return 'Dashboard';
  }

  getBreadcrumb(): string {
    const p = this.router.url;
    if (p.includes('ai-copilot')) return 'Home / AI / Career Copilot';
    if (p.includes('ai-cv')) return 'Home / AI / CV Reviewer';
    if (p.includes('feed')) return 'Home / Feed';
    if (p.includes('directory')) return 'Home / Directory';
    if (p.includes('jobs')) return 'Home / AI Job Finder';
    if (p.includes('opportunities')) return 'Home / Opportunities';
    if (p.includes('events')) return 'Home / Events';
    if (p.includes('user/')) return 'Home / User Profile';
    if (p.includes('connections')) return 'Home / Connections';
    if (p.includes('profile')) return 'Home / Profile';
    if (p.includes('admin')) return 'Home / Admin';
    return 'Home';
  }

  sendChat() {
    if (!this.chatInput.trim() || this.isChatLoading) return;
    const msg = this.chatInput;
    this.chatMessages.push({role: 'user', text: msg});
    this.chatInput = '';
    this.isChatLoading = true;

    this.chatMessages.push({role: 'ai', text: '...'});

    const historyForApi = this.chatMessages
      .filter(m => m.text !== '...')
      .slice(0, -1)
      .map(m => ({ role: m.role, text: m.text }));

    this.aiService.sendChatMessage(msg, historyForApi).subscribe({
      next: (res) => {
        this.chatMessages[this.chatMessages.length - 1] = {role: 'ai', text: res.response || 'I received your message.'};
        this.isChatLoading = false;
      },
      error: (err) => {
        const detail = err.error?.error || err.error?.detail || '';
        this.chatMessages[this.chatMessages.length - 1] = {
          role: 'ai',
          text: detail ? `Error: ${detail}` : 'Sorry, I encountered an error. Please try again.'
        };
        this.isChatLoading = false;
      }
    });
  }

  toggleVoice() {
    this.showVoice = !this.showVoice;
    if (this.showVoice) {
      this.voiceListening = true;
      this.voiceText = '';
      setTimeout(() => {
        this.voiceText = 'Show cybersecurity internships in Tunis';
        this.voiceListening = false;
      }, 2500);
    }
  }

  executeVoice() {
    this.showVoice = false;
    this.router.navigate(['/app/opportunities']);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
