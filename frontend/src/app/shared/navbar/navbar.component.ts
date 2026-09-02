import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="navbar-inner">
        <a routerLink="/dashboard" class="brand">✈ AMS</a>

        <div class="links" *ngIf="auth.isLoggedIn()">
          <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          <a *ngIf="auth.isCustomer()" routerLink="/flights/search" routerLinkActive="active">Search Flights</a>
          <a *ngIf="auth.isCustomer()" routerLink="/payments" routerLinkActive="active">Payments</a>
          <a *ngIf="auth.isAdmin()" routerLink="/flights" routerLinkActive="active">Flights</a>
          <a *ngIf="auth.isAdmin()" routerLink="/carriers" routerLinkActive="active">Carriers</a>
          <a routerLink="/bookings" routerLinkActive="active">Bookings</a>
          <a *ngIf="auth.isSuperAdmin()" routerLink="/super-admin/admin-requests" routerLinkActive="active">Admin Requests</a>
        </div>

        <div class="right">
          <ng-container *ngIf="auth.isLoggedIn(); else guestLinks">
            <span class="user-chip">{{ auth.currentUser()?.userName }} · {{ auth.currentUser()?.role }}</span>
            <button class="btn btn-secondary" (click)="logout()">Logout</button>
          </ng-container>
          <ng-template #guestLinks>
            <a routerLink="/login" class="btn btn-secondary">Login</a>
            <a routerLink="/register" class="btn btn-primary">Register</a>
          </ng-template>
        </div>
      </div>
    </nav>

    <div class="banner-wrap" *ngIf="notification.notification() as note">
      <div class="container">
        <div class="alert" [ngClass]="note.type === 'error' ? 'alert-error' : 'alert-success'">
          {{ note.message }}
        </div>
      </div>
    </div>
  `,
  styles: [`
    .navbar { position: sticky; top: 0; z-index: 20; color: #fff; background: rgba(4, 22, 59, .82); border-bottom: 1px solid rgba(255,255,255,.15); backdrop-filter: blur(18px); }
    .navbar-inner { max-width: 1440px; margin: 0 auto; padding: 14px 28px; display: flex; align-items: center; gap: 26px; }
    .brand { color: #fff; font-weight: 800; font-size: 23px; letter-spacing: -.04em; white-space: nowrap; }
    .links { display: flex; gap: 7px; flex: 1; flex-wrap: wrap; }.links a { color: #dce8ff; font-weight: 700; font-size: 14px; padding: 10px 14px; border-radius: 12px; }.links a.active, .links a:hover { color: #fff; background: rgba(50, 135, 255, .18); box-shadow: inset 0 -2px #45a8ff; }
    .right { display: flex; align-items: center; gap: 10px; margin-left: auto; }.user-chip { font-size: 13px; background: rgba(255,255,255,.10); border: 1px solid rgba(255,255,255,.13); padding: 9px 13px; border-radius: 999px; white-space: nowrap; }
    .banner-wrap { padding-top: 16px; }
    @media (max-width: 860px) { .navbar-inner { padding: 12px 16px; gap: 12px; align-items: flex-start; flex-wrap: wrap; }.links { order: 3; flex-basis: 100%; overflow-x: auto; flex-wrap: nowrap; }.links a { white-space: nowrap; }.right { margin-left: auto; }.user-chip { display: none; } }
  `]
})
export class NavbarComponent {
  constructor(public auth: AuthService, public notification: NotificationService, private router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
