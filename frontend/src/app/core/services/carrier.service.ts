import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Carrier, CarrierRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class CarrierService {

  private readonly baseUrl = `${environment.apiBaseUrl}/carriers`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Carrier[]> {
    return this.http.get<Carrier[]>(this.baseUrl);
  }

  getById(carrierId: number): Observable<Carrier> {
    return this.http.get<Carrier>(`${this.baseUrl}/${carrierId}`);
  }

  create(request: CarrierRequest): Observable<Carrier> {
    return this.http.post<Carrier>(this.baseUrl, request);
  }

  update(carrierId: number, request: CarrierRequest): Observable<Carrier> {
    return this.http.put<Carrier>(`${this.baseUrl}/${carrierId}`, request);
  }
  delete(carrierId: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/${carrierId}`); }
  suggestedFlightNumber(carrierId: number): Observable<{suggestedFlightNumber: string}> { return this.http.get<{suggestedFlightNumber: string}>(`${this.baseUrl}/${carrierId}/suggested-flight-number`); }
}
