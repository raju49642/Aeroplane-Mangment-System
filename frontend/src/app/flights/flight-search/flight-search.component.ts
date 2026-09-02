import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { FlightService } from '../../core/services/flight.service';
import { NotificationService } from '../../core/services/notification.service';
import { FlightSearchResult } from '../../core/models/models';
import { AIRPORTS_DATA, Airport } from '../../core/models/airports';

function originNotDestination(control: AbstractControl): ValidationErrors | null {
  const origin = control.get('origin')?.value;
  const destination = control.get('destination')?.value;
  return origin && destination && origin.trim().toLowerCase() === destination.trim().toLowerCase()
    ? { sameRoute: true } : null;
}

@Component({
  selector: 'app-flight-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container">
      <div class="card">
        <h2>Search Flights</h2>
        <form [formGroup]="form" (ngSubmit)="search()">
          <div class="form-row">
            <div class="form-group">
              <label>Origin</label>
              <select formControlName="origin">
                <option value="" disabled>Select origin</option>
                <option *ngFor="let airport of airports" [value]="airport.city">{{ airport.city }} ({{ airport.code }})</option>
              </select>
            </div>
            <div class="form-group">
              <label>Destination</label>
              <select formControlName="destination">
                <option value="" disabled>Select destination</option>
                <option *ngFor="let airport of airports" [value]="airport.city">{{ airport.city }} ({{ airport.code }})</option>
              </select>
            </div>
            <div class="form-group">
              <label>Travel Date</label>
              <input type="date" formControlName="travelDate" [min]="minDate">
            </div>
          </div>
          <small class="field-error" *ngIf="form.errors?.['sameRoute'] && form.touched">
            Origin and destination cannot be the same.
          </small>
          <button class="btn btn-primary mt-2" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Searching...' : 'Search Flights' }}
          </button>
        </form>
      </div>

      <div *ngIf="searched">
        <h3 *ngIf="results.length > 0">{{ results.length }} flight(s) found</h3>

        <div class="card" *ngIf="!loading && results.length === 0">
          <p class="text-muted">No flights found for this route and date. Try a different search.</p>
        </div>

        <div class="flight-card card" *ngFor="let flight of results">
          <div class="flight-card-header">
            <div>
              <strong>{{ flight.carrierName }}</strong>
              <span class="text-muted"> · {{ flight.flightNumber }}</span>
            </div>
            <div class="fare">Economy from ₹{{ flight.economyFare | number:'1.0-2' }}</div>
          </div>

          <div class="route">
            <div>
              <div class="city">{{ flight.origin }}</div>
              <div class="time">{{ flight.departureTime }}</div>
            </div>
            <div class="arrow">→</div>
            <div>
              <div class="city">{{ flight.destination }}</div>
              <div class="time">{{ flight.arrivalTime }}</div>
            </div>
            <div class="date text-muted">{{ flight.travelDate }}</div>
          </div>

          <div class="seats">
            <span>Economy: <strong>{{ flight.availableEconomySeats }}</strong> available</span>
            <span>Business: <strong>{{ flight.availableBusinessSeats }}</strong> available</span>
            <span>Executive: <strong>{{ flight.availableExecutiveSeats }}</strong> available</span>
          </div>
          <div class="seats">
            <span>Economy: ₹{{ flight.economyFare | number:'1.2-2' }}</span>
            <span>Business: ₹{{ flight.businessFare | number:'1.2-2' }}</span>
            <span>Executive: ₹{{ flight.executiveFare | number:'1.2-2' }}</span>
          </div>

          <button class="btn btn-primary" (click)="selectFlight(flight)">Select Flight</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .field-error { color: #dc3545; font-size: 12px; }
    .flight-card { margin-top: 16px; }
    .flight-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .fare { font-weight: 800; color: #9bceff; }
    .route { display: flex; align-items: center; gap: 20px; margin-bottom: 12px; }
    .city { font-weight: 700; font-size: 16px; }
    .time { color: #d4e1f5; font-size: 14px; }
    .arrow { font-size: 20px; color: #9bceff; }
    .date { margin-left: auto; }
    .seats { display: flex; gap: 20px; flex-wrap: wrap; font-size: 13px; margin-bottom: 12px; color: #e1ebfb; }
    @media (max-width: 620px) { .route { gap: 12px; flex-wrap: wrap; }.date { margin-left: 0; flex-basis: 100%; } }
  `]
})
export class FlightSearchComponent {
  airports: readonly Airport[] = AIRPORTS_DATA;
  results: FlightSearchResult[] = [];
  loading = false;
  searched = false;
  minDate = new Date().toISOString().substring(0, 10);

  private fb = inject(FormBuilder);

  form = this.fb.group({
    origin: ['', Validators.required],
    destination: ['', Validators.required],
    travelDate: ['', Validators.required]
  }, { validators: originNotDestination });

  constructor(
    private flightService: FlightService,
    private notification: NotificationService,
    private router: Router
  ) {}

  search(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.searched = true;
    const { origin, destination, travelDate } = this.form.getRawValue();

    this.flightService.search(origin!, destination!, travelDate!).subscribe({
      next: results => {
        this.results = results;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.results = [];
      }
    });
  }

  selectFlight(flight: FlightSearchResult): void {
    this.router.navigate(['/bookings/new'], { state: { flight } });
  }
}
