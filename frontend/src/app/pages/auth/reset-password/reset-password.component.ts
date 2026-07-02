import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatSnackBarModule],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  uid = '';
  token = '';
  password = '';
  password_confirm = '';
  isLoading = false;
  isSuccess = false;
  errorMessage = '';
  showPassword = false;

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.uid = params['uid'] || '';
      this.token = params['token'] || '';
    });
  }

  onSubmit() {
    if (this.password !== this.password_confirm) {
      this.snackBar.open('Passwords do not match.', 'Close', { duration: 4000, panelClass: 'error-snack' });
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.resetPassword({
      uid: this.uid, token: this.token,
      password: this.password, password_confirm: this.password_confirm
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.isSuccess = true;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.detail || 'Failed to reset password. The link may have expired.';
      }
    });
  }
}
