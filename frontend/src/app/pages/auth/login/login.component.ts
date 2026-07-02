import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatSnackBarModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  showPassword = false;
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  onLogin() {
    if (!this.credentials.username || !this.credentials.password) return;
    this.isLoading = true;
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.authService.getProfile().subscribe({
          next: () => this.router.navigate(['/app']),
          error: () => this.router.navigate(['/app'])
        });
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Invalid credentials. Please try again.', 'Close', {
          duration: 4000, panelClass: 'error-snack'
        });
      }
    });
  }
}
