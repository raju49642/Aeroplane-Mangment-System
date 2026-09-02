import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FlightService } from '../../core/services/flight.service';
import { AuthService } from '../../core/services/auth.service';
import { Flight } from '../../core/models/models';

@Component({
  selector: 'app-flight-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="flex-between">
        <h2>Flights</h2>
        <a *ngIf="auth.isAdmin()" routerLink="/flights/new" class="btn btn-primary">+ New Flight</a>
      </div>

      <div class="card" *ngIf="!loading && flights.length === 0">
        <p class="text-muted">No flights available yet.</p>
      </div>

      <div class="alert alert-error" *ngIf="errorMessage">{{ errorMessage }}</div>

      <div class="card" *ngIf="flights.length > 0">
        <table>
          <thead>
            <tr>
              <th>Flight #</th>
              <th>Carrier</th>
              <th>Route</th>
              <th>Base Fare</th>
              <th>Business</th>
              <th>Economy</th>
              <th>Executive</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let flight of flights">
              <td><strong>{{ flight.flightNumber }}</strong></td>
           <td>{{ flight.carrierName }}</td>
              <td>{{ flight.origin }} → {{ flight.destination }}</td>
              <td>₹{{ flight.baseFare | number:'1.0-2' }}</td>
              <td>{{ flight.seatCapacityBusinessClass }}</td>
              <td>{{ flight.seatCapacityEconomyClass }}</td>
              <td>{{ flight.seatCapacityExecutiveClass }}</td>
              <td class="flex" *ngIf="auth.isAdmin()">
                <a class="btn btn-secondary" [routerLink]="['/flights', flight.flightId, 'edit']">Edit</a>
                <a class="btn btn-primary" [routerLink]="['/flights', flight.flightId, 'schedules', 'new']">+ Schedule</a>
                <a class="btn btn-secondary" [routerLink]="['/flights', flight.flightId, 'schedules']">Manage schedules</a>
                <button class="btn btn-danger" (click)="deleteFlight(flight)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class FlightListComponent implements OnInit {
  flights: Flight[] = [];
  loading = true;
  errorMessage = '';

  constructor(private flightService: FlightService, public auth: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.errorMessage = '';
    this.flightService.getAll().subscribe({
      next: flights => {
        this.flights = flights;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  deleteFlight(flight: Flight): void {
    if (!confirm(`Delete ${flight.flightNumber}? This is only possible when it has no schedules or schedule templates.`)) return;

    this.errorMessage = '';
    this.flightService.delete(flight.flightId).subscribe({
      next: () => this.load(),
      error: error => {
        this.errorMessage = error?.error?.message ?? 'Unable to delete this flight.';
      }
    });
  }
}
