import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
const USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9_]{3,29}$/;
const PHONE_PATTERN = /^[6-9]\d{9}$/;

@Component({
  selector: 'app-register-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container" style="max-width: 480px;">
      <div class="card">
        <h2>Request Admin Access</h2>
        <p class="text-muted">
          Submitting this creates a <strong>pending</strong> admin request. You will not have
          any admin privileges — and cannot log in as an admin — until the Super Admin reviews
          and approves your request.
        </p>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-group">
            <label>Username</label>
            <input type="text" formControlName="userName">
            <small class="field-error" *ngIf="form.get('userName')?.touched && form.get('userName')?.invalid">
              Username must be 4-30 characters, start with a letter, and contain only letters, numbers, and underscores.
            </small>
          </div>
          <div class="form-group"><label>Favourite sport</label><input type="text" formControlName="favouriteSport"></div>
          <div class="form-group"><label>Favourite hobby</label><input type="text" formControlName="favouriteHobby"></div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" formControlName="password">
            <small class="field-error" *ngIf="form.get('password')?.touched && form.get('password')?.invalid">
              Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.
            </small>
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" formControlName="emailId">
          </div>
          <div class="form-group">
            <label>Phone</label>
            <input type="text" formControlName="phone" placeholder="10-digit mobile">
            <small class="field-error" *ngIf="form.get('phone')?.touched && form.get('phone')?.invalid">
              Enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.
            </small>
          </div>

          <button class="btn btn-primary mt-2" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Submitting...' : 'Submit Admin Request' }}
          </button>
        </form>
        <p class="text-muted mt-3">
          <a routerLink="/login">Back to login</a>
        </p>
      </div>
    </div>
  `,
  styles: [`.field-error { color: #dc3545; font-size: 12px; }`]
})
export class RegisterAdminComponent {
  loading = false;
  private fb = inject(FormBuilder);

  form = this.fb.group({
    userName: ['', [Validators.required, Validators.pattern(USERNAME_PATTERN)]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
    emailId: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    favouriteSport: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    favouriteHobby: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]]
  });

  constructor(
    private authService: AuthService,
    private notification: NotificationService,
    private router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const value = this.form.getRawValue();

    this.authService.requestAdminAccess({
      userName: value.userName!,
      password: value.password!,
      emailId: value.emailId!,
      phone: value.phone!, favouriteSport: value.favouriteSport!, favouriteHobby: value.favouriteHobby!
    }).subscribe({
      next: () => {
        this.loading = false;
        this.notification.showSuccess('Admin request submitted. You will be able to log in once approved.');
        this.router.navigate(['/login']);
      },
      error: () => this.loading = false
    });
  }
}
