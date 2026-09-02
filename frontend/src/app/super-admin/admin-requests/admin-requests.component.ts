import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SuperAdminService } from '../../core/services/super-admin.service';
import { NotificationService } from '../../core/services/notification.service';
import { AdminRequestResponse } from '../../core/models/models';

@Component({
  selector: 'app-admin-requests',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="flex-between">
        <h2>Super Admin Dashboard</h2>
        <span class="badge" style="background:#dbeafe;color:#1e40af;">
          Pending Admin Requests: {{ requests.length }}
        </span>
      </div>

      <div class="card" *ngIf="!loading && requests.length === 0">
        <p class="text-muted">No pending admin requests.</p>
      </div>

      <div class="card" *ngIf="requests.length > 0">
        <table>
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let request of requests">
              <td>{{ request.userName }}</td>
              <td>{{ request.emailId }}</td>
              <td>{{ request.phone }}</td>
              <td><span class="badge" style="background:#fef3c7;color:#92400e;">{{ request.status }}</span></td>
              <td class="flex">
                <button class="btn btn-primary" [disabled]="processingId === request.userId" (click)="approve(request)">
                  Approve
                </button>
                <button class="btn btn-danger" [disabled]="processingId === request.userId" (click)="reject(request)">
                  Reject
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AdminRequestsComponent implements OnInit {
  requests: AdminRequestResponse[] = [];
  loading = true;
  processingId: number | null = null;

  constructor(
    private superAdminService: SuperAdminService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading = true;
    this.superAdminService.getPendingAdminRequests().subscribe({
      next: requests => {
        this.requests = requests;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  approve(request: AdminRequestResponse): void {
    this.processingId = request.userId;
    this.superAdminService.approve(request.userId).subscribe({
      next: () => {
        this.processingId = null;
        this.notification.showSuccess(`${request.userName} approved as ADMIN`);
        this.load();
      },
      error: () => this.processingId = null
    });
  }

  reject(request: AdminRequestResponse): void {
    this.processingId = request.userId;
    this.superAdminService.reject(request.userId).subscribe({
      next: () => {
        this.processingId = null;
        this.notification.showSuccess(`${request.userName}'s admin request rejected`);
        this.load();
      },
      error: () => this.processingId = null
    });
  }
}
