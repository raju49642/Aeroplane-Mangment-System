import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookingService } from '../../core/services/booking.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { FlightSearchResult } from '../../core/models/models';

/**
 * The customer arrives here only after selecting a flight from search results
 * (see FlightSearchComponent), carrying a scheduleId in the query params. The
 * booking is made against that exact FlightSchedule — never a bare flightId —
 * and the travel date / times shown here are read-only, sourced from the
 * schedule itself.
 */
@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container" style="max-width: 560px;">
      <div class="card" *ngIf="!scheduleId">
        <p class="text-muted">No flight selected. Please search and select a flight first.</p>
        <a routerLink="/flights/search" class="btn btn-primary">Search Flights</a>
      </div>

      <div class="card" *ngIf="scheduleId && flightInfo">
        <h2>Confirm Booking</h2>

        <div class="flight-summary">
          <div><strong>{{ flightInfo.carrierName }}</strong> · {{ flightInfo.flightNumber }}</div>
          <div class="route">{{ flightInfo.origin }} → {{ flightInfo.destination }}</div>
          <div class="text-muted">{{ flightInfo.travelDate }} · {{ flightInfo.departureTime }} - {{ flightInfo.arrivalTime }}</div>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-row mt-2">
            <div class="form-group">
              <label>Seat Category</label>
              <select formControlName="seatCategory">
                <option value="ECONOMY">Economy ({{ flightInfo.availableEconomySeats }} available)</option>
                <option value="BUSINESS">Business ({{ flightInfo.availableBusinessSeats }} available)</option>
                <option value="EXECUTIVE">Executive ({{ flightInfo.availableExecutiveSeats }} available)</option>
              </select>
            </div>
            <div class="form-group">
              <label>Number of Seats</label>
              <input type="number" formControlName="noOfSeats" min="1" [max]="maxSeatsForSelectedCategory()">
              <small class="field-error" *ngIf="form.get('noOfSeats')?.touched && form.get('noOfSeats')?.invalid">
                Enter between 1 and {{ maxSeatsForSelectedCategory() }} seats.
              </small>
            </div>
          </div>

          <section class="passengers" formArrayName="passengers">
            <div class="flex-between"><h3>Passengers</h3><button type="button" class="btn btn-secondary" (click)="useMyDetails()">Use my details</button></div>
            <p class="text-muted">Enter one traveller for each selected seat.</p>
            <div class="passenger" *ngFor="let passenger of passengers.controls; let i = index" [formGroupName]="i">
              <strong>Passenger {{ i + 1 }}</strong>
              <div class="form-row mt-2"><div class="form-group"><label>Full Name</label><input formControlName="passengerName" maxlength="100"><small class="field-error" *ngIf="passenger.get('passengerName')?.touched && passenger.get('passengerName')?.invalid">Name must be 2–100 characters.</small></div><div class="form-group"><label>Age</label><input type="number" formControlName="age" min="1" max="120"><small class="field-error" *ngIf="passenger.get('age')?.touched && passenger.get('age')?.invalid">Enter an age from 1–120.</small></div></div>
            </div>
          </section>

          <section class="payment-method">
            <h3>Payment Method</h3>
            <label class="method-option" [class.selected]="form.get('paymentMethod')?.value === 'UPI'"><input type="radio" formControlName="paymentMethod" value="UPI"><span><strong>UPI</strong><small>Pay using UPI / QR</small></span></label>
            <label class="method-option" [class.selected]="form.get('paymentMethod')?.value === 'CREDIT_CARD'"><input type="radio" formControlName="paymentMethod" value="CREDIT_CARD"><span><strong>Credit Card</strong><small>Pay using credit or debit card</small></span></label>
            <small class="field-error" *ngIf="form.get('paymentMethod')?.touched && form.get('paymentMethod')?.invalid">Please select a payment method.</small>
          </section>

          <div class="card mt-2 fare-panel">
            <p class="text-muted" style="margin:0;">
              Fare per seat: <strong>₹{{ selectedFare() | number:'1.2-2' }}</strong><br>
              Final amount (after any applicable discounts) will be calculated by the server on confirmation.
            </p>
          </div>

          <div class="flex mt-3">
            <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">
              {{ loading ? 'Booking...' : 'Confirm Booking' }}
            </button>
            <a routerLink="/flights/search" class="btn btn-secondary">Cancel</a>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .field-error { color: #dc3545; font-size: 12px; }
    .flight-summary { padding: 16px; background: rgba(65, 143, 255, .14); border: 1px solid rgba(150, 208, 255, .25); border-radius: 12px; margin-bottom: 14px; }
    .route { font-weight: 700; font-size: 16px; margin: 4px 0; }
    .fare-panel { background: rgba(255,255,255,.08); box-shadow: none; }
    .passengers { margin-top: 18px; }.passengers h3 { margin: 0; }.passenger { padding: 14px; margin-top: 10px; border: 1px solid rgba(150,208,255,.2); border-radius: 10px; }
    .payment-method { margin-top: 18px; }.payment-method h3 { margin-bottom: 10px; }.method-option { display:flex; gap:10px; align-items:flex-start; padding:12px; margin:8px 0; border:1px solid rgba(150,208,255,.25); border-radius:10px; cursor:pointer; }.method-option.selected { border-color:#70b7ff; background:rgba(65,143,255,.16); }.method-option span { display:grid; gap:3px; }.method-option small { color:var(--ams-muted); }
  `]
})
export class BookingFormComponent implements OnInit {
  scheduleId: number | null = null;
  flightInfo: FlightSearchResult | null = null;
  loading = false;

  private fb = inject(FormBuilder);

  form = this.fb.group({
    seatCategory: ['ECONOMY', Validators.required],
    noOfSeats: [1, [Validators.required, Validators.min(1), Validators.max(6)]],
    paymentMethod: ['', Validators.required],
    passengers: this.fb.array([])
  });

  constructor(
    private bookingService: BookingService,
    private auth: AuthService,
    private notification: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // The flight/schedule details arrive via router navigation state, set by
    // FlightSearchComponent when the customer selects a flight from search results.
    // If the user lands here directly (e.g. page refresh) with no state, we prompt
    // them to search again rather than guessing at a scheduleId.
    const state = this.router.getCurrentNavigation()?.extras.state ?? history.state;
    const flight = state?.['flight'] as FlightSearchResult | undefined;

    if (flight) {
      this.flightInfo = flight;
      this.scheduleId = flight.scheduleId;
    }
    this.syncPassengers();
    this.form.get('noOfSeats')!.valueChanges.subscribe(() => this.syncPassengers());
  }

  maxSeatsForSelectedCategory(): number {
    if (!this.flightInfo) return 1;
    const category = this.form.getRawValue().seatCategory;
    if (category === 'BUSINESS') return Math.min(6, this.flightInfo.availableBusinessSeats);
    if (category === 'EXECUTIVE') return Math.min(6, this.flightInfo.availableExecutiveSeats);
    return Math.min(6, this.flightInfo.availableEconomySeats);
  }

  selectedFare(): number {
    if (!this.flightInfo) return 0;
    const category = this.form.getRawValue().seatCategory;
    if (category === 'BUSINESS') return this.flightInfo.businessFare;
    if (category === 'EXECUTIVE') return this.flightInfo.executiveFare;
    return this.flightInfo.economyFare;
  }

  get passengers(): FormArray { return this.form.get('passengers') as FormArray; }
  private passengerForm() { return this.fb.group({ passengerName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]], age: [null, [Validators.required, Validators.min(1), Validators.max(120)]] }); }
  private syncPassengers(): void { const count = Math.max(1, Math.min(6, Number(this.form.get('noOfSeats')?.value) || 1)); while (this.passengers.length < count) this.passengers.push(this.passengerForm()); while (this.passengers.length > count) this.passengers.removeAt(this.passengers.length - 1); }
  useMyDetails(): void {
    const firstPassenger = this.passengers.at(0);
    const session = this.auth.currentUser();
    if (!firstPassenger || !session) return;

    // The session gives an immediate name prefill; the profile request supplies DOB
    // so the age field is also filled with the current value.
    firstPassenger.patchValue({ passengerName: session.userName });
    this.auth.getMyProfile().subscribe({
      next: user => firstPassenger.patchValue({ passengerName: user.userName, age: this.ageFromDob(user.dob) }),
      error: () => this.notification.showError('Your name was filled, but we could not load your age. Please enter it manually.')
    });
  }

  private ageFromDob(dob?: string): number | null {
    if (!dob) return null;
    const birthDate = new Date(`${dob}T00:00:00`);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const beforeBirthday = today.getMonth() < birthDate.getMonth()
      || (today.getMonth() === birthDate.getMonth() && today.getDate() < birthDate.getDate());
    return beforeBirthday ? age - 1 : age;
  }

  submit(): void {
    if (this.form.invalid || !this.scheduleId) {
      this.form.markAllAsTouched();
      return;
    }
    const passengerNames = this.passengers.getRawValue().map((passenger: { passengerName: string }) => passenger.passengerName.trim().replace(/\s+/g, ' ').toLowerCase());
    if (new Set(passengerNames).size !== passengerNames.length) {
      this.notification.showError('Each ticket must be for a different passenger.');
      return;
    }
    this.loading = true;
    const value = this.form.getRawValue();

    // Note: no userId is sent — the backend derives the booking owner from the
    // authenticated JWT principal exclusively.
    this.bookingService.book({
      scheduleId: this.scheduleId,
      noOfSeats: value.noOfSeats!,
      seatCategory: value.seatCategory as any,
      paymentMethod: value.paymentMethod as any,
      passengers: this.passengers.getRawValue().map((passenger: { passengerName: string; age: number }) => ({ passengerName: passenger.passengerName.trim(), age: Number(passenger.age) }))
    }).subscribe({
      next: response => {
        this.loading = false;
        this.router.navigate(['/bookings', response.bookingId, 'payment'], { state: { booking: response, paymentMethod: value.paymentMethod } });
      },
      error: () => this.loading = false
    });
  }
}
