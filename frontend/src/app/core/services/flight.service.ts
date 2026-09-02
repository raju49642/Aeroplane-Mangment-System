import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Flight,
  FlightRequest,
  FlightSchedule,
  FlightScheduleRequest,
  FlightScheduleTemplate,
  FlightScheduleTemplateRequest,
  FlightSearchResult
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class FlightService {

  private readonly baseUrl = `${environment.apiBaseUrl}/flights`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Flight[]> {
    return this.http.get<Flight[]>(this.baseUrl);
  }

  getById(flightId: number): Observable<Flight> {
    return this.http.get<Flight>(`${this.baseUrl}/${flightId}`);
  }

  getByCarrierName(carrierName: string): Observable<Flight[]> {
    return this.http.get<Flight[]>(`${this.baseUrl}/carrier/${encodeURIComponent(carrierName)}`);
  }

  create(request: FlightRequest): Observable<Flight> {
    return this.http.post<Flight>(this.baseUrl, request);
  }

  update(flightId: number, request: FlightRequest): Observable<Flight> {
    return this.http.put<Flight>(`${this.baseUrl}/${flightId}`, request);
  }

  delete(flightId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${flightId}`);
  }

  createSchedule(flightId: number, request: FlightScheduleRequest): Observable<FlightSchedule> {
    return this.http.post<FlightSchedule>(`${this.baseUrl}/${flightId}/schedules`, request);
  }

  getSchedules(flightId: number): Observable<FlightSchedule[]> { return this.http.get<FlightSchedule[]>(`${this.baseUrl}/${flightId}/schedules`); }
  updateSchedule(scheduleId: number, request: FlightScheduleRequest): Observable<FlightSchedule> { return this.http.put<FlightSchedule>(`${this.baseUrl}/schedules/${scheduleId}`, request); }
  getScheduleTemplates(flightId: number): Observable<FlightScheduleTemplate[]> { return this.http.get<FlightScheduleTemplate[]>(`${this.baseUrl}/${flightId}/schedule-templates`); }
  createScheduleTemplate(flightId: number, request: FlightScheduleTemplateRequest): Observable<FlightScheduleTemplate> { return this.http.post<FlightScheduleTemplate>(`${this.baseUrl}/${flightId}/schedule-templates`, request); }
  updateScheduleTemplate(templateId: number, request: FlightScheduleTemplateRequest): Observable<FlightScheduleTemplate> { return this.http.put<FlightScheduleTemplate>(`${this.baseUrl}/schedule-templates/${templateId}`, request); }
  deactivateScheduleTemplate(templateId: number): Observable<FlightScheduleTemplate> { return this.http.put<FlightScheduleTemplate>(`${this.baseUrl}/schedule-templates/${templateId}/deactivate`, {}); }

  /** Real, database-backed flight search — the primary customer booking entry point. */
  search(origin: string, destination: string, travelDate: string): Observable<FlightSearchResult[]> {
    const params = new HttpParams()
      .set('origin', origin)
      .set('destination', destination)
      .set('travelDate', travelDate);
    return this.http.get<FlightSearchResult[]>(`${this.baseUrl}/search`, { params });
  }
  scheduled(origin: string, destination: string): Observable<FlightSearchResult[]> {
    return this.http.get<FlightSearchResult[]>(`${this.baseUrl}/scheduled`, { params: new HttpParams().set('origin', origin).set('destination', destination) });
  }
}
