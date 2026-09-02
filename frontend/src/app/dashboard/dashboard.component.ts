import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-dashboard', standalone: true, imports: [CommonModule, RouterLink],
  template: `
    <div class="container dashboard">
      <section class="hero">
        <p class="eyebrow">WELCOME BACK</p>
        <h1>{{ auth.currentUser()?.userName }}</h1>
        <p>Your journey, our priority. Manage flights, payments and bookings all in one place.</p>
      </section>

      <div *ngIf="auth.isCustomer()" class="action-grid">
        <article class="card action-card search"><span class="action-icon">⌕</span><h3>Search Flights</h3><p class="text-muted">Search flights for a specific travel date.</p><a routerLink="/flights/search" class="btn btn-primary">Search Flights →</a></article>
        <article class="card action-card search"><span class="action-icon">✈</span><h3>Scheduled Flights</h3><p class="text-muted">Explore flights scheduled for the upcoming days.</p><a routerLink="/flights/scheduled" class="btn btn-primary">View Scheduled Flights →</a></article>
        <article class="card action-card payment"><span class="action-icon">▣</span><h3>My Payments</h3><p class="text-muted">View your membership payment history.</p><a routerLink="/payments" class="btn btn-secondary">View Payments →</a></article>
        <article class="card action-card booking"><span class="action-icon">✧</span><h3>My Bookings</h3><p class="text-muted">View or cancel your existing bookings.</p><a routerLink="/bookings" class="btn btn-secondary">View Bookings →</a></article>
      </div>

      <div *ngIf="auth.isAdmin()" class="card admin-actions">
        <h3>Carrier & Flight Management</h3><p class="text-muted">Register carriers, flights, and schedules.</p>
        <div class="flex"><a routerLink="/carriers" class="btn btn-secondary">Manage Carriers</a><a routerLink="/flights" class="btn btn-secondary">Manage Flights</a><a routerLink="/bookings" class="btn btn-secondary">View Bookings</a></div>
      </div>
      <div *ngIf="auth.isSuperAdmin()" class="card admin-actions"><h3>Super Admin</h3><p class="text-muted">Review and approve pending admin access requests.</p><a routerLink="/super-admin/admin-requests" class="btn btn-primary">Admin Requests</a></div>
    </div>`,
  styles: [`
    .dashboard { min-height: calc(100vh - 80px); }.hero { max-width: 560px; padding: 42px 0 28px; }.eyebrow { margin: 0 0 9px; color: #8fc8ff; font-weight: 800; letter-spacing: .14em; font-size: .75rem; }.hero h1 { margin: 0; font-size: clamp(2.65rem, 6vw, 4.6rem); background: linear-gradient(100deg, #c285ff, #5ab7ff); -webkit-background-clip: text; color: transparent; }.hero > p:last-child { max-width: 470px; color: #e5edff; font-size: 1.08rem; line-height: 1.6; }.action-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }.action-card { min-height: 270px; display: flex; flex-direction: column; align-items: flex-start; margin: 0; }.action-card .btn { margin-top: auto; }.action-icon { display: grid; place-items: center; height: 58px; width: 58px; margin-bottom: 22px; border-radius: 50%; font-size: 30px; background: linear-gradient(135deg, #48b5ff, #4a4aff); box-shadow: 0 12px 28px rgba(37, 120, 255, .32); }.payment .action-icon { background: linear-gradient(135deg, #6f9dff, #2085e8); }.booking .action-icon { background: linear-gradient(135deg, #fd5b91, #ff9452); }.admin-actions { max-width: 760px; } @media (max-width: 1000px) { .action-grid { grid-template-columns: repeat(2, 1fr); } } @media (max-width: 800px) { .action-grid { grid-template-columns: 1fr; }.hero { padding-top: 24px; } }
  `]
})
export class DashboardComponent { constructor(public auth: AuthService) {} }
