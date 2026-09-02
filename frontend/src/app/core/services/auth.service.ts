import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  LoginResponse,
  RegisterAdminRequest,
  RegisterUserRequest,
  Role,
  User
} from '../models/models';

const STORAGE_KEY = 'ams_session';

interface StoredSession {
  userId: number;
  userName: string;
  role: Role;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  readonly currentUser = signal<StoredSession | null>(this.loadSession());

  constructor(private http: HttpClient) {}

  private loadSession(): StoredSession | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) as StoredSession : null;
  }

  register(request: RegisterUserRequest): Observable<User> {
    return this.http.post<User>(`${environment.apiBaseUrl}/users/register`, request);
  }

  /** Public admin *request* — creates a PENDING admin, not an active one. */
  requestAdminAccess(request: RegisterAdminRequest): Observable<User> {
    return this.http.post<User>(`${environment.apiBaseUrl}/admin/register-request`, request);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, request).pipe(
      tap(response => {
        const session: StoredSession = {
          userId: response.userId,
          userName: response.userName,
          role: response.role,
          token: response.token
        };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
        this.currentUser.set(session);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.currentUser.set(null);
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }

  hasRole(...roles: Role[]): boolean {
    const role = this.currentUser()?.role;
    return !!role && roles.includes(role);
  }

  isSuperAdmin(): boolean { return this.hasRole('SUPER_ADMIN'); }
  isAdmin(): boolean { return this.hasRole('ADMIN', 'SUPER_ADMIN'); }
  isCustomer(): boolean { return this.hasRole('CUSTOMER'); }

  getToken(): string | null {
    return this.currentUser()?.token ?? null;
  }

  getUserId(): number | null {
    return this.currentUser()?.userId ?? null;
  }

  getMyProfile(): Observable<User> {
    return this.http.get<User>(`${environment.apiBaseUrl}/users/me`);
  }

  verifyPasswordRecovery(userName: string, favouriteSport: string, favouriteHobby: string): Observable<{resetToken: string}> {
    return this.http.post<{resetToken: string}>(`${environment.apiBaseUrl}/auth/forgot-password/verify`, { userName, favouriteSport, favouriteHobby });
  }

  resetPassword(resetToken: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/forgot-password/reset`, { resetToken, newPassword });
  }
}
