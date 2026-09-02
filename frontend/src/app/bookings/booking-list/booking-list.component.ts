import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../core/services/booking.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { BookingResponse } from '../../core/models/models';

@Component({
  selector: 'app-booking-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="flex-between">
        <h2>{{ auth.isAdmin() ? 'All Bookings' : 'My Bookings' }}</h2>
        <a routerLink="/flights/search" class="btn btn-primary" *ngIf="auth.isCustomer()">+ New Booking</a>
      </div>

      <div class="card" *ngIf="!loading && bookings.length === 0">
        <p class="text-muted">No bookings yet.</p>
      </div>

      <div class="card" *ngIf="bookings.length > 0">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Flight</th>
              <th>Route</th>
              <th>Carrier</th>
              <th *ngIf="auth.isAdmin()">Customer</th>
              <th>Seats</th>
              <th>Category</th>
              <th>Travel Date</th>
              <th>Booked On</th>
              <th>Status</th>
              <th>Amount</th>
              <th>Passengers</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let booking of bookings">
              <td>{{ booking.bookingId }}</td>
              <td>{{ booking.flightNumber }}</td>
              <td>{{ booking.origin }} → {{ booking.destination }}</td>
              <td>{{ booking.carrierName }}</td>
              <td *ngIf="auth.isAdmin()">{{ booking.userName }}</td>
              <td>{{ booking.noOfSeats }}</td>
              <td>{{ booking.seatCategory }}</td>
              <td>{{ booking.travelDate }} {{ booking.departureTime }}</td>
              <td>{{ booking.bookingDateTime | date:'short' }}</td>
              <td>
                <span class="badge" [ngClass]="booking.bookingStatus === 'BOOKED' ? 'badge-booked' : 'badge-cancelled'">
                  {{ booking.bookingStatus }}
                </span>
              </td>
              <td>₹{{ booking.bookingAmount | number:'1.2-2' }}</td>
              <td><div *ngFor="let passenger of booking.passengers">{{ passenger.passengerName }} — {{ passenger.age }}</div><span class="text-muted" *ngIf="!booking.passengers?.length">Historical booking</span></td>
              <td>
                <a class="btn btn-secondary" *ngIf="booking.ticketNumber" [routerLink]="['/tickets/booking', booking.bookingId]">View Tickets</a>
                <button
                  class="btn btn-danger"
                  *ngIf="booking.bookingStatus === 'BOOKED' && (auth.isAdmin() || auth.isCustomer())"
                  [disabled]="cancellingId === booking.bookingId"
                  (click)="cancel(booking)">
                  {{ cancellingId === booking.bookingId ? 'Cancelling...' : 'Cancel' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class BookingListComponent implements OnInit {
  bookings: BookingResponse[] = [];
  loading = true;
  cancellingId: number | null = null;

  constructor(
    private bookingService: BookingService,
    public auth: AuthService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading = true;
    // Admin/Super Admin see everything; a customer only ever sees their own bookings,
    // resolved server-side from the authenticated JWT principal — never a client-supplied userId.
    const request$ = this.auth.isAdmin() ? this.bookingService.getAll() : this.bookingService.getMyBookings();

    request$.subscribe({
      next: bookings => {
        this.bookings = bookings;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  cancel(booking: BookingResponse): void {
    this.cancellingId = booking.bookingId;
    this.bookingService.cancellationPreview(booking.bookingId).subscribe({
      next: preview => {
        if (!preview.eligible) { this.cancellingId = null; this.notification.showError(preview.reason || 'This booking cannot be cancelled.'); return; }
        if (!confirm(`Cancelling this booking will refund ₹${preview.refundAmount.toFixed(2)} (${preview.refundPercentage}% of the paid amount). This cannot be undone. Proceed?`)) { this.cancellingId = null; return; }
        this.performCancel(booking.bookingId);
      }, error: () => this.cancellingId = null
    });
  }

  private performCancel(bookingId: number): void {
    this.bookingService.cancel(bookingId).subscribe({
      next: response => {
        this.cancellingId = null;
        this.notification.showSuccess(`Booking cancelled. Refund amount: ₹${response.bookingAmount.toFixed(2)}`);
        this.load();
      },
      error: () => this.cancellingId = null
    });
  }
}
