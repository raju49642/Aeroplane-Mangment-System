import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container" style="max-width: 420px;">
      <div class="card">
        <h2>Login</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-group">
            <label>Username</label>
            <input type="text" formControlName="userName" placeholder="e.g. john">
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" formControlName="password" placeholder="••••••••">
          </div>
          <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading" style="width:100%;">
            {{ loading ? 'Signing in...' : 'Login' }}
          </button>
        </form>
        <p class="text-muted mt-3">
          Don't have an account? <a routerLink="/register">Register here</a><br>
          Want to manage flights? <a routerLink="/register-admin">Request Admin Access</a>
          <br><a routerLink="/forgot-password">Forgot password?</a>
        </p>
      </div>
    </div>
  `
})
export class LoginComponent {
  loading = false;

  private fb = inject(FormBuilder);

  form = this.fb.group({
    userName: ['', Validators.required],
    password: ['', Validators.required]
  });

  constructor(
    private authService: AuthService,
    private notification: NotificationService,
    private router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading = true;
    const { userName, password } = this.form.getRawValue();

    this.authService.login({ userName: userName!, password: password! }).subscribe({
      next: () => {
        this.loading = false;
        this.notification.showSuccess('Logged in successfully');
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
