import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./pages/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./pages/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
  },
  {
    path: 'app',
    loadComponent: () => import('./layouts/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'feed', pathMatch: 'full' },
      { path: 'feed', loadComponent: () => import('./pages/feed/feed.component').then(m => m.FeedComponent) },
      { path: 'directory', loadComponent: () => import('./pages/directory/directory.component').then(m => m.DirectoryComponent) },
      { path: 'opportunities', loadComponent: () => import('./pages/opportunities/opportunities.component').then(m => m.OpportunitiesComponent) },
      { path: 'jobs', loadComponent: () => import('./pages/jobs/jobs.component').then(m => m.JobsComponent) },
      { path: 'events', loadComponent: () => import('./pages/events/events.component').then(m => m.EventsComponent) },
      { path: 'profile', loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent) },
      { path: 'connections', loadComponent: () => import('./pages/connections/connections.component').then(m => m.ConnectionsComponent) },
      { path: 'user/:id', loadComponent: () => import('./pages/user-profile/user-profile.component').then(m => m.UserProfileComponent) },
      { path: 'admin', loadComponent: () => import('./pages/admin/admin-dashboard.component').then(m => m.AdminDashboardComponent), canActivate: [adminGuard] },
      { path: 'ai-copilot', loadComponent: () => import('./pages/ai-copilot/ai-copilot.component').then(m => m.AiCopilotComponent) },
      { path: 'ai-cv', loadComponent: () => import('./pages/ai-cv-reviewer/ai-cv-reviewer.component').then(m => m.AiCvReviewerComponent) },
    ]
  },
  { path: '**', redirectTo: '' }
];
