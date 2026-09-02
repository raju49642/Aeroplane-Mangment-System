import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CarrierService } from '../../core/services/carrier.service';
import { NotificationService } from '../../core/services/notification.service';
import { Subject, debounceTime, distinctUntilChanged, map, of, switchMap, takeUntil } from 'rxjs';

@Component({
  selector: 'app-carrier-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container" style="max-width: 700px;">
      <div class="card">
        <h2>{{ carrierId ? 'Edit Carrier' : 'New Carrier' }}</h2>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-group">
            <label>Carrier Name</label>
            <input type="text" formControlName="carrierName" minlength="4" maxlength="50">
            <small class="field-error" *ngIf="form.get('carrierName')?.touched && (form.get('carrierName')?.hasError('required') || form.get('carrierName')?.hasError('minlength') || form.get('carrierName')?.hasError('maxlength'))">Carrier name must be between 4 and 50 characters.</small>
            <small class="field-error" *ngIf="form.get('carrierName')?.hasError('duplicateName')">Carrier name already exists.</small>
          </div>
          <div class="form-group">
            <label>Carrier Code</label>
            <input type="text" formControlName="carrierCode" maxlength="2" style="text-transform:uppercase" placeholder="e.g. IN">
            <small class="field-error" *ngIf="form.get('carrierCode')?.touched && (form.get('carrierCode')?.hasError('required') || form.get('carrierCode')?.hasError('pattern'))">Enter exactly 2 uppercase letters.</small>
            <small class="field-error" *ngIf="form.get('carrierCode')?.hasError('duplicateCode')">Carrier code already exists.</small>
          </div>

          <h3 class="mt-3">Advance Booking Discounts (%)</h3>
          <div class="form-row">
            <div class="form-group">
              <label>30 Days</label>
              <input type="number" formControlName="discount30DaysAdvance" min="10" max="30" step="0.01">
              <small class="field-error" *ngIf="form.get('discount30DaysAdvance')?.touched && (form.get('discount30DaysAdvance')?.hasError('min') || form.get('discount30DaysAdvance')?.hasError('max'))">Enter a discount between 10% and 30%.</small>
              <small class="field-error" *ngIf="form.hasError('discount30MustBeLessThan60')">30-day discount must be less than 60-day discount.</small>
            </div>
            <div class="form-group">
              <label>60 Days</label>
              <input type="number" formControlName="discount60DaysAdvance" min="10" max="30" step="0.01">
              <small class="field-error" *ngIf="form.get('discount60DaysAdvance')?.touched && (form.get('discount60DaysAdvance')?.hasError('min') || form.get('discount60DaysAdvance')?.hasError('max'))">Enter a discount between 10% and 30%.</small>
              <small class="field-error" *ngIf="form.hasError('discount60MustBeLessThan90')">60-day discount must be less than 90-day discount.</small>
            </div>
            <div class="form-group">
              <label>90 Days</label>
              <input type="number" formControlName="discount90DaysAdvance" min="10" max="30">
              <small class="field-error" *ngIf="form.get('discount90DaysAdvance')?.touched && (form.get('discount90DaysAdvance')?.hasError('min') || form.get('discount90DaysAdvance')?.hasError('max'))">Enter a discount between 10% and 30%.</small>
            </div>
          </div>

          <h3 class="mt-3">Customer Category Discounts (%)</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Silver</label>
              <input type="number" formControlName="silverUserDiscount" min="10" max="30" step="0.01">
              <small class="field-error" *ngIf="form.get('silverUserDiscount')?.touched && (form.get('silverUserDiscount')?.hasError('min') || form.get('silverUserDiscount')?.hasError('max'))">Enter the discount between 10% and 30%.</small>
            </div>
            <div class="form-group">
              <label>Gold</label>
              <input type="number" formControlName="goldUserDiscount" min="10" max="30" step="0.01">
              <small class="field-error" *ngIf="form.get('goldUserDiscount')?.touched && (form.get('goldUserDiscount')?.hasError('min') || form.get('goldUserDiscount')?.hasError('max'))">Enter the discount between 10% and 30%.</small>
            </div>
            <div class="form-group">
              <label>Platinum</label>
              <input type="number" formControlName="platinumUserDiscount" min="10" max="30" step="0.01">
              <small class="field-error" *ngIf="form.get('platinumUserDiscount')?.touched && (form.get('platinumUserDiscount')?.hasError('min') || form.get('platinumUserDiscount')?.hasError('max'))">Enter the discount between 10% and 30%.</small>
            </div>
          </div>

          <h3 class="mt-3">Other</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Business Fare Multiplier</label>
              <input type="number" formControlName="businessClassMultiplier" min="1" step="0.01">
            </div>
            <div class="form-group">
              <label>Executive Fare Multiplier</label>
              <input type="number" formControlName="executiveClassMultiplier" min="1" step="0.01">
            </div>
            <div class="form-group">
              <label>Bulk Booking Discount (10+ seats) %</label>
              <input type="number" formControlName="bulkBookingDiscount" min="10" max="30" step="0.01">
              <small class="field-error" *ngIf="form.get('bulkBookingDiscount')?.touched && (form.get('bulkBookingDiscount')?.hasError('min') || form.get('bulkBookingDiscount')?.hasError('max'))">Enter the discount between 10% and 30%.</small>
            </div>
          </div>

          <h3 class="mt-3">Cancellation Refund (%)</h3>
          <div class="form-row">
            <div class="form-group">
              <label>2-9 Days Before</label>
              <input type="number" formControlName="refund2DaysBefore" min="75" max="95" step="0.01">
              <small class="field-error" *ngIf="form.get('refund2DaysBefore')?.touched && (form.get('refund2DaysBefore')?.hasError('min') || form.get('refund2DaysBefore')?.hasError('max'))">Enter the refund percentage between 75% and 95%.</small>
              <small class="field-error" *ngIf="form.hasError('refund2MustBeLessThan10')">2–9 day refund must be less than 10–19 day refund.</small>
            </div>
            <div class="form-group">
              <label>10-19 Days Before</label>
              <input type="number" formControlName="refund10DaysBefore" min="75" max="95" step="0.01">
              <small class="field-error" *ngIf="form.get('refund10DaysBefore')?.touched && (form.get('refund10DaysBefore')?.hasError('min') || form.get('refund10DaysBefore')?.hasError('max'))">Enter the refund percentage between 75% and 95%.</small>
              <small class="field-error" *ngIf="form.hasError('refund10MustBeLessThan20')">10–19 day refund must be less than 20+ day refund.</small>
            </div>
            <div class="form-group">
              <label>20+ Days Before</label>
              <input type="number" formControlName="refund20DaysOrMore" min="75" max="95" step="0.01">
              <small class="field-error" *ngIf="form.get('refund20DaysOrMore')?.touched && (form.get('refund20DaysOrMore')?.hasError('min') || form.get('refund20DaysOrMore')?.hasError('max'))">Enter the refund percentage between 75% and 95%.</small>
            </div>
          </div>

          <div class="flex mt-2">
            <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">
              {{ loading ? 'Saving...' : (carrierId ? 'Update Carrier' : 'Create Carrier') }}
            </button>
            <a routerLink="/carriers" class="btn btn-secondary">Cancel</a>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`.field-error { color: #dc3545; font-size: 12px; }`]
})
export class CarrierFormComponent implements OnInit, OnDestroy {
  carrierId: number | null = null;
  loading = false;

  private fb = inject(FormBuilder);
  private readonly destroy$ = new Subject<void>();

  form = this.fb.group({
    carrierName: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    carrierCode: ['', [Validators.required, Validators.pattern(/^[A-Z]{2}$/)]],
    discount30DaysAdvance: [10, [Validators.required, Validators.min(10), Validators.max(30)]],
    discount60DaysAdvance: [20, [Validators.required, Validators.min(10), Validators.max(30)]],
    discount90DaysAdvance: [30, [Validators.required, Validators.min(10), Validators.max(30)]],
    bulkBookingDiscount: [10, [Validators.required, Validators.min(10), Validators.max(30)]],
    silverUserDiscount: [10, [Validators.required, Validators.min(10), Validators.max(30)]],
    goldUserDiscount: [10, [Validators.required, Validators.min(10), Validators.max(30)]],
    platinumUserDiscount: [10, [Validators.required, Validators.min(10), Validators.max(30)]],
    refund2DaysBefore: [75, [Validators.required, Validators.min(75), Validators.max(95)]],
    refund10DaysBefore: [85, [Validators.required, Validators.min(75), Validators.max(95)]],
    refund20DaysOrMore: [95, [Validators.required, Validators.min(75), Validators.max(95)]],
    businessClassMultiplier: [1, [Validators.required, Validators.min(1)]],
    executiveClassMultiplier: [1, [Validators.required, Validators.min(1)]]
  }, { validators: carrierDiscountOrderValidator });

  constructor(
    private carrierService: CarrierService,
    private notification: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.watchForDuplicateName();
    this.watchForDuplicateCode();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.carrierId = Number(idParam);
      this.carrierService.getById(this.carrierId).subscribe(carrier => {
        this.form.patchValue(carrier);
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading = true;
    const request = this.form.getRawValue() as any;

    const request$ = this.carrierId
      ? this.carrierService.update(this.carrierId, request)
      : this.carrierService.create(request);

    request$.subscribe({
      next: () => {
        this.loading = false;
        this.notification.showSuccess(this.carrierId ? 'Carrier updated' : 'Carrier created');
        this.router.navigate(['/carriers']);
      },
      error: () => this.loading = false
    });
  }

  private watchForDuplicateName(): void {
    const control = this.form.controls.carrierName;
    control.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(name => name && name.length >= 4
        ? this.carrierService.getAll().pipe(map(carriers => carriers.some(carrier =>
            carrier.carrierId !== this.carrierId && carrier.carrierName.toLowerCase() === name.toLowerCase())))
        : of(false)),
      takeUntil(this.destroy$)
    ).subscribe(exists => this.setDuplicateError(control, 'duplicateName', exists));
  }

  private watchForDuplicateCode(): void {
    const control = this.form.controls.carrierCode;
    control.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(code => /^[A-Z]{2}$/.test(code ?? '')
        ? this.carrierService.getAll().pipe(map(carriers => carriers.some(carrier =>
            carrier.carrierId !== this.carrierId && carrier.carrierCode.toLowerCase() === (code ?? '').toLowerCase())))
        : of(false)),
      takeUntil(this.destroy$)
    ).subscribe(exists => this.setDuplicateError(control, 'duplicateCode', exists));
  }

  private setDuplicateError(control: AbstractControl, errorKey: string, exists: boolean): void {
    if (exists) {
      control.setErrors({ ...control.errors, [errorKey]: true });
      return;
    }

    const { [errorKey]: ignored, ...remainingErrors } = control.errors ?? {};
    control.setErrors(Object.keys(remainingErrors).length ? remainingErrors : null);
  }
}

const carrierDiscountOrderValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const errors: ValidationErrors = {};
  const discount30 = control.get('discount30DaysAdvance')?.value;
  const discount60 = control.get('discount60DaysAdvance')?.value;
  const discount90 = control.get('discount90DaysAdvance')?.value;
  const refund2 = control.get('refund2DaysBefore')?.value;
  const refund10 = control.get('refund10DaysBefore')?.value;
  const refund20 = control.get('refund20DaysOrMore')?.value;

  if (discount30 != null && discount60 != null && discount30 >= discount60) {
    errors['discount30MustBeLessThan60'] = true;
  }
  if (discount60 != null && discount90 != null && discount60 >= discount90) {
    errors['discount60MustBeLessThan90'] = true;
  }
  if (refund2 != null && refund10 != null && refund2 >= refund10) {
    errors['refund2MustBeLessThan10'] = true;
  }
  if (refund10 != null && refund20 != null && refund10 >= refund20) {
    errors['refund10MustBeLessThan20'] = true;
  }
  return Object.keys(errors).length ? errors : null;
};
