import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FlightService } from '../../core/services/flight.service';
import { CarrierService } from '../../core/services/carrier.service';
import { NotificationService } from '../../core/services/notification.service';
import { Carrier } from '../../core/models/models';
import { AIRPORTS_DATA, Airport, normalizeAirport } from '../../core/models/airports';

const FLIGHT_NUMBER_PATTERN = /^[A-Z]{2}\d{2,3}$/;

function originNotDestination(control: AbstractControl): ValidationErrors | null {
  const origin = control.get('origin')?.value;
  const destination = control.get('destination')?.value;
  return origin && destination && origin.trim().toLowerCase() === destination.trim().toLowerCase()
    ? { sameRoute: true } : null;
}
function valueStartsWithCarrier(value: string | null | undefined, carrierCode: string): boolean {
  return !!value && value.toUpperCase().startsWith(carrierCode);
}

@Component({
  selector: 'app-flight-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container" style="max-width: 640px;">
      <div class="card">
        <h2>{{ flightId ? 'Edit Flight' : 'New Flight' }}</h2>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-group">
            <label>Carrier</label>
            <select formControlName="carrierId">
              <option [ngValue]="null" disabled>Select a carrier</option>
              <option *ngFor="let carrier of carriers" [ngValue]="carrier.carrierId">{{ carrier.carrierName }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Flight Number</label>
            <input type="text" formControlName="flightNumber" placeholder="e.g. AI101, 6E203, UK811" style="text-transform: uppercase;">
            <small class="text-muted" *ngIf="selectedCarrier">Must start with: {{ selectedCarrier.carrierCode }}</small>
            <small class="field-error" *ngIf="form.get('flightNumber')?.touched && form.get('flightNumber')?.invalid">
              Flight number must be two letters followed by 2-3 digits and use the carrier code.
            </small>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Origin</label>
              <select formControlName="origin">
                <option [ngValue]="null" disabled>Select origin city</option>
                <option *ngFor="let airport of airports" [value]="airport.city">{{ airport.city }} ({{ airport.code }})</option>
              </select>
              <small class="field-error" *ngIf="form.get('origin')?.touched && form.get('origin')?.invalid">
                Origin is required.
              </small>
            </div>
            <div class="form-group">
              <label>Destination</label>
              <select formControlName="destination">
                <option [ngValue]="null" disabled>Select destination city</option>
                <option *ngFor="let airport of airports" 
                        [value]="airport.city"
                        [disabled]="isDestinationDisabled(airport)">
                  {{ airport.city }} ({{ airport.code }})
                </option>
              </select>
              <small class="field-error" *ngIf="form.get('destination')?.touched && form.get('destination')?.invalid">
                Destination is required.
              </small>
            </div>
          </div>
          <small class="field-error" *ngIf="form.errors?.['sameRoute'] && form.touched">
            Origin and destination cannot be the same.
          </small>

          <div class="form-group">
            <label>Base Fare</label>
            <input type="number" formControlName="baseFare" min="3000" max="50000">
            <small class="field-error" *ngIf="form.get('baseFare')?.touched && form.get('baseFare')?.invalid">Base fare must be between ₹3,000 and ₹50,000.</small>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Business Class Seats</label>
              <input type="number" formControlName="seatCapacityBusinessClass" min="30" max="120">
            </div>
            <div class="form-group">
              <label>Economy Class Seats</label>
              <input type="number" formControlName="seatCapacityEconomyClass" min="100" max="500">
            </div>
            <div class="form-group">
              <label>Executive Class Seats</label>
              <input type="number" formControlName="seatCapacityExecutiveClass" min="25" max="75">
            </div>
          </div>
          <small class="field-error" *ngIf="totalCapacity() < 150 || totalCapacity() > 750">Total capacity ({{ totalCapacity() }}) must be between 150 and 750.</small>

          <div class="flex mt-2">
            <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">
              {{ loading ? 'Saving...' : (flightId ? 'Update Flight' : 'Create Flight') }}
            </button>
            <a routerLink="/flights" class="btn btn-secondary">Cancel</a>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`.field-error { color: #dc3545; font-size: 12px; }
    select:disabled { background-color: #e9ecef; cursor: not-allowed; }
    select option:disabled { background-color: #e9ecef; color: #999; }`]
})
export class FlightFormComponent implements OnInit {
  carriers: Carrier[] = [];
  airports: readonly Airport[] = AIRPORTS_DATA;
  flightId: number | null = null;
  loading = false;
  get selectedCarrier(): Carrier | undefined { return this.carriers.find(c => c.carrierId === this.form.get('carrierId')?.value); }

  private fb = inject(FormBuilder);

  form = this.fb.group({
    carrierId: [null as number | null, Validators.required],
    flightNumber: ['', [Validators.required, Validators.pattern(FLIGHT_NUMBER_PATTERN)]],
    origin: ['', Validators.required],
    destination: ['', Validators.required],
    baseFare: [null as number | null, [Validators.required, Validators.min(3000), Validators.max(50000)]],
    seatCapacityBusinessClass: [30, [Validators.required, Validators.min(30), Validators.max(120)]],
    seatCapacityEconomyClass: [100, [Validators.required, Validators.min(100), Validators.max(500)]],
    seatCapacityExecutiveClass: [25, [Validators.required, Validators.min(25), Validators.max(75)]]
  }, { validators: originNotDestination });

  constructor(
    private flightService: FlightService,
    private carrierService: CarrierService,
    private notification: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) this.flightId = Number(idParam);
    this.carrierService.getAll().subscribe(carriers => { this.carriers = carriers; });
    if (!this.flightId) {
      this.form.get('carrierId')?.valueChanges.subscribe(id => {
        if (!id || this.form.get('flightNumber')?.value) return;
        this.carrierService.suggestedFlightNumber(id).subscribe({ next: r => this.form.get('flightNumber')?.setValue(r.suggestedFlightNumber), error: () => console.warn('Flight number suggestion unavailable') });
      });
    }

    if (idParam) {
      this.flightService.getById(this.flightId!).subscribe(flight => {
        this.form.patchValue({
          carrierId: flight.carrierId,
          flightNumber: flight.flightNumber,
          // Supports records created with older spellings such as "Bangalore".
          origin: normalizeAirport(flight.origin) ?? flight.origin,
          destination: normalizeAirport(flight.destination) ?? flight.destination,
          baseFare: flight.baseFare,
          seatCapacityBusinessClass: flight.seatCapacityBusinessClass,
          seatCapacityEconomyClass: flight.seatCapacityEconomyClass,
          seatCapacityExecutiveClass: flight.seatCapacityExecutiveClass
        });
      });
    }
  }

  isDestinationDisabled(airport: Airport): boolean {
    const originValue = this.form.get('origin')?.value;
    return originValue?.trim().toLowerCase() === airport.city.toLowerCase();
  }
  totalCapacity(): number { const v = this.form.getRawValue(); return Number(v.seatCapacityBusinessClass || 0) + Number(v.seatCapacityEconomyClass || 0) + Number(v.seatCapacityExecutiveClass || 0); }

  submit(): void {
    if (this.form.invalid || this.totalCapacity() < 150 || this.totalCapacity() > 750 || (!!this.selectedCarrier && !valueStartsWithCarrier(this.form.get('flightNumber')?.value, this.selectedCarrier.carrierCode))) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const value = this.form.getRawValue();
    const request = {
      carrierId: value.carrierId!,
      flightNumber: value.flightNumber!.toUpperCase(),
      origin: value.origin!,
      destination: value.destination!,
      baseFare: value.baseFare!,
      seatCapacityBusinessClass: value.seatCapacityBusinessClass!,
      seatCapacityEconomyClass: value.seatCapacityEconomyClass!,
      seatCapacityExecutiveClass: value.seatCapacityExecutiveClass!
    };

    const request$ = this.flightId
      ? this.flightService.update(this.flightId, request)
      : this.flightService.create(request);

    request$.subscribe({
      next: () => {
        this.loading = false;
        this.notification.showSuccess(this.flightId ? 'Flight updated' : 'Flight created');
        this.router.navigate(['/flights']);
      },
      error: () => this.loading = false
    });
  }
}
