import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CarrierService } from '../../core/services/carrier.service';
import { AuthService } from '../../core/services/auth.service';
import { Carrier } from '../../core/models/models';

@Component({
  selector: 'app-carrier-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="flex-between">
        <h2>Carriers</h2>
        <a *ngIf="auth.isAdmin()" routerLink="/carriers/new" class="btn btn-primary">+ New Carrier</a>
      </div>

      <div class="card" *ngIf="!loading && carriers.length === 0">
        <p class="text-muted">No carriers registered yet.</p>
      </div>

      <div class="card" *ngIf="carriers.length > 0">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>30/60/90 Day Discount</th>
              <th>Bulk Discount</th>
              <th>Silver/Gold/Platinum</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let carrier of carriers">
              <td>{{ carrier.carrierId }}</td>
              <td>{{ carrier.carrierName }}</td>
              <td>{{ carrier.discount30DaysAdvance }}% / {{ carrier.discount60DaysAdvance }}% / {{ carrier.discount90DaysAdvance }}%</td>
              <td>{{ carrier.bulkBookingDiscount }}%</td>
              <td>{{ carrier.silverUserDiscount }}% / {{ carrier.goldUserDiscount }}% / {{ carrier.platinumUserDiscount }}%</td>
              <td>
                <a *ngIf="auth.isAdmin()" class="btn btn-secondary" [routerLink]="['/carriers', carrier.carrierId, 'edit']">Edit</a>
                <button *ngIf="auth.isAdmin()" class="btn btn-danger" (click)="deleteCarrier(carrier)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class CarrierListComponent implements OnInit {
  carriers: Carrier[] = [];
  loading = true;

  constructor(private carrierService: CarrierService, public auth: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.carrierService.getAll().subscribe({
      next: carriers => {
        this.carriers = carriers;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  deleteCarrier(carrier: Carrier): void {
    if (!confirm(`Delete ${carrier.carrierName}? This is only possible when it has no flights.`)) return;
    this.carrierService.delete(carrier.carrierId).subscribe({ next: () => this.load() });
  }
}
