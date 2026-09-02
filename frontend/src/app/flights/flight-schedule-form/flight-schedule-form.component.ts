import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FlightService } from '../../core/services/flight.service';
import { NotificationService } from '../../core/services/notification.service';
import { Flight } from '../../core/models/models';

function departureBeforeArrival(control: AbstractControl): ValidationErrors | null {
  const dep = control.get('departureTime')?.value;
  const arr = control.get('arrivalTime')?.value;
  return dep && arr && dep >= arr ? { invalidOrder: true } : null;
}

function maximumDomesticDuration(control: AbstractControl): ValidationErrors | null {
  const departure = control.get('departureTime')?.value as string | null;
  const arrival = control.get('arrivalTime')?.value as string | null;
  if (!departure || !arrival || departure >= arrival) return null;

  const [departureHour, departureMinute] = departure.split(':').map(Number);
  const [arrivalHour, arrivalMinute] = arrival.split(':').map(Number);
  const durationMinutes = (arrivalHour * 60 + arrivalMinute) - (departureHour * 60 + departureMinute);
  return durationMinutes > 12 * 60 ? { maximumDuration: true } : null;
}

@Component({
  selector: 'app-flight-schedule-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container" style="max-width: 480px;">
      <div class="card" *ngIf="flight">
        <h2>New Schedule</h2>
        <p class="text-muted">{{ flight.flightNumber }} · {{ flight.origin }} → {{ flight.destination }}</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-group">
            <label>Travel Date</label>
            <input type="date" formControlName="travelDate" [min]="minDate">
            <small class="field-error" *ngIf="form.get('travelDate')?.touched && form.get('travelDate')?.invalid">
              Travel date cannot be in the past.
            </small>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Departure Time</label>
              <input type="time" formControlName="departureTime">
            </div>
            <div class="form-group">
              <label>Arrival Time</label>
              <input type="time" formControlName="arrivalTime">
            </div>
          </div>
          <small class="field-error" *ngIf="form.errors?.['invalidOrder'] && form.touched">
            Departure time must be before arrival time.
          </small>
          <small class="field-error" *ngIf="form.errors?.['maximumDuration'] && form.touched">
            Flight duration cannot exceed 12 hours.
          </small>

          <div class="flex mt-3">
            <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">
              {{ loading ? 'Saving...' : 'Create Schedule' }}
            </button>
            <a routerLink="/flights" class="btn btn-secondary">Cancel</a>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`.field-error { color: #dc3545; font-size: 12px; }`]
})
export class FlightScheduleFormComponent implements OnInit {
  flight: Flight | null = null;
  loading = false;
  minDate = new Date().toISOString().substring(0, 10);

  private fb = inject(FormBuilder);

  form = this.fb.group({
    travelDate: ['', Validators.required],
    departureTime: ['', Validators.required],
    arrivalTime: ['', Validators.required]
  }, { validators: [departureBeforeArrival, maximumDomesticDuration] });

  constructor(
    private flightService: FlightService,
    private notification: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const flightId = Number(this.route.snapshot.paramMap.get('id'));
    this.flightService.getById(flightId).subscribe(flight => this.flight = flight);
  }

  submit(): void {
    if (this.form.invalid || !this.flight) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const value = this.form.getRawValue();

    this.flightService.createSchedule(this.flight.flightId, {
      travelDate: value.travelDate!,
      departureTime: value.departureTime!,
      arrivalTime: value.arrivalTime!
    }).subscribe({
      next: () => {
        this.loading = false;
        this.notification.showSuccess('Schedule created');
        this.router.navigate(['/flights']);
      },
      error: () => this.loading = false
    });
  }
}
