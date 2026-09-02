import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BookingRequest, BookingResponse, BookingPaymentRequest, CancellationPreview } from '../models/models';

@Injectable({ providedIn: 'root' })
export class BookingService {

  private readonly baseUrl = `${environment.apiBaseUrl}/bookings`;

  constructor(private http: HttpClient) {}

  /** ADMIN/SUPER_ADMIN only. */
  getAll(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(this.baseUrl);
  }

  /** The authenticated customer's own bookings — no userId is ever sent by the client. */
  getMyBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.baseUrl}/my`);
  }

  getById(bookingId: number): Observable<BookingResponse> {
    return this.http.get<BookingResponse>(`${this.baseUrl}/${bookingId}`);
  }

  book(request: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(this.baseUrl, request);
  }
  pay(bookingId: number, request: BookingPaymentRequest): Observable<BookingResponse> { return this.http.post<BookingResponse>(`${this.baseUrl}/${bookingId}/pay`, request); }

  cancel(bookingId: number): Observable<BookingResponse> {
    return this.http.put<BookingResponse>(`${this.baseUrl}/${bookingId}/cancel`, {});
  }
  cancellationPreview(bookingId: number): Observable<CancellationPreview> { return this.http.get<CancellationPreview>(`${this.baseUrl}/${bookingId}/cancellation-preview`); }
}
