import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { superAdminGuard } from './core/guards/super-admin.guard';
import { customerGuard } from './core/guards/customer.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },

  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent)
  },
  { path: 'forgot-password', loadComponent: () => import('./auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  {
    path: 'register-admin',
    loadComponent: () => import('./auth/register-admin/register-admin.component').then(m => m.RegisterAdminComponent)
  },
  { path: 'payments', loadComponent: () => import('./payments/payment-list.component').then(m => m.PaymentListComponent), canActivate: [customerGuard] },
  { path: 'membership-payment', loadComponent: () => import('./payments/membership-payment.component').then(m => m.MembershipPaymentComponent), canActivate: [customerGuard] },

  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },

  // Customer-facing flight search + booking flow
  {
    path: 'flights/search',
    loadComponent: () => import('./flights/flight-search/flight-search.component').then(m => m.FlightSearchComponent),
    canActivate: [customerGuard]
  },
  { path: 'flights/scheduled', loadComponent: () => import('./flights/scheduled-flights/scheduled-flights.component').then(m => m.ScheduledFlightsComponent), canActivate: [customerGuard] },

  // Admin flight/schedule management
  {
    path: 'flights',
    loadComponent: () => import('./flights/flight-list/flight-list.component').then(m => m.FlightListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'flights/new',
    loadComponent: () => import('./flights/flight-form/flight-form.component').then(m => m.FlightFormComponent),
    canActivate: [adminGuard]
  },
  {
    path: 'flights/:id/edit',
    loadComponent: () => import('./flights/flight-form/flight-form.component').then(m => m.FlightFormComponent),
    canActivate: [adminGuard]
  },
  {
    path: 'flights/:id/schedules/new',
    loadComponent: () => import('./flights/flight-schedule-form/flight-schedule-form.component').then(m => m.FlightScheduleFormComponent),
    canActivate: [adminGuard]
  },
  { path: 'flights/:id/schedules', loadComponent: () => import('./flights/schedule-management/schedule-management.component').then(m => m.ScheduleManagementComponent), canActivate: [adminGuard] },

  {
    path: 'carriers',
    loadComponent: () => import('./carriers/carrier-list/carrier-list.component').then(m => m.CarrierListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'carriers/new',
    loadComponent: () => import('./carriers/carrier-form/carrier-form.component').then(m => m.CarrierFormComponent),
    canActivate: [adminGuard]
  },
  {
    path: 'carriers/:id/edit',
    loadComponent: () => import('./carriers/carrier-form/carrier-form.component').then(m => m.CarrierFormComponent),
    canActivate: [adminGuard]
  },

  {
    path: 'bookings',
    loadComponent: () => import('./bookings/booking-list/booking-list.component').then(m => m.BookingListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'bookings/new',
    loadComponent: () => import('./bookings/booking-form/booking-form.component').then(m => m.BookingFormComponent),
    canActivate: [customerGuard]
  },
  { path: 'bookings/:bookingId/payment', loadComponent: () => import('./bookings/booking-payment.component').then(m => m.BookingPaymentComponent), canActivate: [customerGuard] },
  { path: 'tickets/booking/:bookingId', loadComponent: () => import('./tickets/ticket.component').then(m => m.TicketComponent), canActivate: [authGuard] },

  {
    path: 'super-admin/admin-requests',
    loadComponent: () => import('./super-admin/admin-requests/admin-requests.component').then(m => m.AdminRequestsComponent),
    canActivate: [superAdminGuard]
  },

  { path: '**', redirectTo: 'dashboard' }
];
