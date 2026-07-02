import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatSnackBarModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  currentStep = 1;
  showPassword = false;
  isLoading = false;

  formData = {
    username: '', email: '', password: '', password_confirm: '',
    first_name: '', last_name: '', role: 'STUDENT'
  };

  roles = [
    { value: 'STUDENT', label: 'Student', desc: 'Currently studying at ESPRIT', icon: 'school', color: '#1a237e' },
    { value: 'ALUMNI', label: 'Alumni', desc: 'ESPRIT graduate', icon: 'workspace_premium', color: '#ff6b35' },
    { value: 'COMPANY', label: 'Company', desc: 'Recruiter or employer', icon: 'business', color: '#10b981' },
  ];

  constructor(private authService: AuthService, private router: Router, private snackBar: MatSnackBar) {}

  get passwordStrength(): number {
    const p = this.formData.password;
    if (!p) return 0;
    let s = 0;
    if (p.length >= 8) s += 25;
    if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s += 25;
    if (/\d/.test(p)) s += 25;
    if (/[^a-zA-Z0-9]/.test(p)) s += 25;
    return s;
  }

  get strengthColor(): string {
    const s = this.passwordStrength;
    if (s <= 25) return '#ef4444';
    if (s <= 50) return '#f59e0b';
    if (s <= 75) return '#3b82f6';
    return '#10b981';
  }

  get strengthLabel(): string {
    const s = this.passwordStrength;
    if (s <= 25) return 'Weak';
    if (s <= 50) return 'Fair';
    if (s <= 75) return 'Good';
    return 'Strong';
  }

  nextStep() {
    if (this.currentStep === 1 && !this.formData.role) return;
    if (this.currentStep === 2 && (!this.formData.first_name || !this.formData.last_name || !this.formData.username || !this.formData.email)) return;
    this.currentStep++;
  }

  onRegister() {
    if (this.formData.password !== this.formData.password_confirm) {
      this.snackBar.open('Passwords do not match.', 'Close', { duration: 4000, panelClass: 'error-snack' });
      return;
    }
    this.isLoading = true;
    this.authService.register(this.formData).subscribe({
      next: () => {
        this.snackBar.open('Account created successfully! Please sign in.', 'Close', { duration: 4000, panelClass: 'success-snack' });
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        const msg = err.error?.username?.[0] || err.error?.email?.[0] || 'Registration failed. Please try again.';
        this.snackBar.open(msg, 'Close', { duration: 4000, panelClass: 'error-snack' });
      }
    });
  }
}
