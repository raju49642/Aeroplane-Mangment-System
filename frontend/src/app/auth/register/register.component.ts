import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
const USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9_]{3,29}$/;
const PHONE_PATTERN = /^[6-9]\d{9}$/;
const ZIP_PATTERN = /^[1-9][0-9]{5}$/;
const ALLOWED_EMAIL_DOMAINS = new Set(['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'icloud.com', 'protonmail.com']);

function maxDobForMinAge(years: number): string {
  const d = new Date();
  d.setFullYear(d.getFullYear() - years);
  return d.toISOString().substring(0, 10);
}

function minDobForMaxAge(years: number): string {
  const d = new Date();
  d.setFullYear(d.getFullYear() - years);
  return d.toISOString().substring(0, 10);
}

function allowedEmailDomain(control: any) {
  const value = String(control.value || '').trim().toLowerCase();
  const at = value.lastIndexOf('@');
  // Do not flag a domain until the user has entered a complete domain-like value.
  if (at < 1 || !value.substring(at + 1).includes('.')) return null;
  return ALLOWED_EMAIL_DOMAINS.has(value.substring(at + 1)) ? null : { emailDomain: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container" style="max-width: 640px;">
      <div class="card">
        <h2>Create your account</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-row">
            <div class="form-group">
              <label>Username</label>
              <input type="text" formControlName="userName">
              <small class="field-error" *ngIf="form.get('userName')?.touched && form.get('userName')?.invalid">
                Username must be 4-30 characters, start with a letter, and contain only letters, numbers, and underscores.
              </small>
            </div>
            <div class="form-group">
              <label>Password</label>
              <input type="password" formControlName="password">
              <small class="field-error" *ngIf="form.get('password')?.touched && form.get('password')?.invalid">
                Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.
              </small>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group"><label>Favourite sport</label><input type="text" formControlName="favouriteSport"><small class="text-muted">Used only for password recovery.</small></div>
            <div class="form-group"><label>Favourite hobby</label><input type="text" formControlName="favouriteHobby"><small class="text-muted">Used only for password recovery.</small></div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Email</label>
              <input type="email" formControlName="emailId">
              <small class="field-error" *ngIf="form.get('emailId')?.dirty && form.get('emailId')?.errors?.['emailDomain']">
                Please use a supported email domain (for example, gmail.com); check the domain spelling.
              </small>
              <small class="field-error" *ngIf="form.get('emailId')?.dirty && form.get('emailId')?.invalid && !form.get('emailId')?.errors?.['emailDomain']">
                Enter a valid email address.
              </small>
            </div>
            <div class="form-group">
              <label>Phone</label>
              <input type="text" formControlName="phone" placeholder="10-digit mobile">
              <small class="field-error" *ngIf="form.get('phone')?.touched && form.get('phone')?.invalid">
                Enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.
              </small>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Date of Birth</label>
              <input type="date" formControlName="dob" [min]="minDob" [max]="maxDob">
              <small class="field-error" *ngIf="form.get('dob')?.touched && form.get('dob')?.invalid">
                You must be between 12 and 75 years old, and the date cannot be in the future.
              </small>
            </div>
            <div class="form-group">
              <label>Customer Category</label>
              <select formControlName="customerCategory">
                <option value="REGULAR">Regular</option>
                <option value="SILVER">Silver</option>
                <option value="GOLD">Gold</option>
                <option value="PLATINUM">Platinum</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Address Line 1</label>
              <input type="text" formControlName="address1">
            </div>
            <div class="form-group">
              <label>Address Line 2 (optional)</label>
              <input type="text" formControlName="address2">
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>City</label>
              <input type="text" formControlName="city">
            </div>
            <div class="form-group">
              <label>State</label>
              <input type="text" formControlName="state">
            </div>
            <div class="form-group">
              <label>Zip Code</label>
              <input type="text" formControlName="zipCode" placeholder="6-digit ZIP">
              <small class="field-error" *ngIf="form.get('zipCode')?.touched && form.get('zipCode')?.invalid">
                Enter a valid 6-digit ZIP code.
              </small>
            </div>
          </div>

          <button class="btn btn-primary mt-2" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Creating account...' : 'Register' }}
          </button>
        </form>
        <p class="text-muted mt-3">
          Already have an account? <a routerLink="/login">Login here</a><br>
          Want to manage flights? <a routerLink="/register-admin">Request Admin Access</a>
        </p>
      </div>
    </div>
  `,
  styles: [`.field-error { color: #dc3545; font-size: 12px; }`]
})
export class RegisterComponent {
  loading = false;
  maxDob = maxDobForMinAge(12);
  minDob = minDobForMaxAge(75);

  private fb = inject(FormBuilder);

  form = this.fb.group({
    userName: ['', [Validators.required, Validators.pattern(USERNAME_PATTERN)]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
    emailId: ['', [Validators.required, Validators.email, allowedEmailDomain]],
    phone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    dob: ['', [Validators.required, this.notFutureAndMinAge]],
    customerCategory: ['REGULAR', Validators.required],
    address1: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
    address2: ['', Validators.maxLength(100)],
    city: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    state: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    zipCode: ['', [Validators.required, Validators.pattern(ZIP_PATTERN)]]
    ,favouriteSport: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]]
    ,favouriteHobby: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]]
  });

  constructor(
    private authService: AuthService,
    private notification: NotificationService,
    private router: Router
  ) {}

  private notFutureAndMinAge(control: any) {
    if (!control.value) return null;
    const dob = new Date(control.value);
    const today = new Date();
    if (dob > today) return { future: true };
    const age = today.getFullYear() - dob.getFullYear();
    return age < 12 || age > 75 ? { invalidAge: true } : null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const value = this.form.getRawValue();

    this.authService.register({
      userName: value.userName!,
      password: value.password!,
      customerCategory: 'REGULAR',
      phone: value.phone!,
      emailId: value.emailId!,
      address1: value.address1!,
      address2: value.address2 || undefined,
      city: value.city!,
      state: value.state!,
      zipCode: value.zipCode!,
      dob: value.dob!
      ,favouriteSport: value.favouriteSport!
      ,favouriteHobby: value.favouriteHobby!
    }).subscribe({
      next: () => {
        const chosenCategory = value.customerCategory!;
        if (chosenCategory === 'REGULAR') {
          this.loading = false;
          this.notification.showSuccess('Account created. You can now log in.');
          this.router.navigate(['/login']);
          return;
        }
        this.authService.login({ userName: value.userName!, password: value.password! }).subscribe({
          next: () => { this.loading = false; this.router.navigate(['/membership-payment'], { state: { category: chosenCategory } }); },
          error: () => this.loading = false
        });
      },
      error: () => this.loading = false
    });
  }
}
