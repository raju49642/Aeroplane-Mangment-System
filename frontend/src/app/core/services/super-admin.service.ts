import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminRequestResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class SuperAdminService {

  private readonly baseUrl = `${environment.apiBaseUrl}/super-admin`;

  constructor(private http: HttpClient) {}

  getPendingAdminRequests(): Observable<AdminRequestResponse[]> {
    return this.http.get<AdminRequestResponse[]>(`${this.baseUrl}/admin-requests`);
  }

  approve(userId: number): Observable<AdminRequestResponse> {
    return this.http.put<AdminRequestResponse>(`${this.baseUrl}/admin-requests/${userId}/approve`, {});
  }

  reject(userId: number): Observable<AdminRequestResponse> {
    return this.http.put<AdminRequestResponse>(`${this.baseUrl}/admin-requests/${userId}/reject`, {});
  }
}
