# AMS Frontend (Angular)

Angular 17 (standalone components) SPA for the AMS backend. Implements the full customer

## Membership and recovery

Premium membership registration proceeds through a mock payment screen after creating a regular account. UPI QR codes are rendered locally with `qrcode`; do not enter real card details. `/payments` shows membership-payment history, and `/forgot-password` verifies the registration security answers before accepting a new password.
booking workflow (search → select → book → view/cancel), Admin carrier/flight/schedule
management, and the Super Admin admin-approval dashboard.

## Setup

```bash
npm install
npm start
```

Serves at `http://localhost:4200`. Confirm `src/environments/environment.ts` points at your
running backend (`apiBaseUrl: 'http://localhost:8080/api'` by default).

## Authentication

JWT-based. `AuthService.login()` calls `POST /api/auth/login`, stores the returned token +
`userId`/`role` in `localStorage`, and a functional interceptor (`jwtInterceptor`) attaches
`Authorization: Bearer <token>` to every request to `/api/**`. A 401 response triggers logout
and redirect to `/login` (see `errorInterceptor`).

## Route Map

| Route | Component | Access |
|---|---|---|
| `/login` | LoginComponent | Public |
| `/register` | RegisterComponent | Public (customer self-registration) |
| `/register-admin` | RegisterAdminComponent | Public (creates a **pending** admin request) |
| `/dashboard` | DashboardComponent | Authenticated (content varies by role) |
| `/flights/search` | FlightSearchComponent | CUSTOMER |
| `/flights` | FlightListComponent | Authenticated |
| `/flights/new`, `/flights/:id/edit` | FlightFormComponent | ADMIN/SUPER_ADMIN |
| `/flights/:id/schedules/new` | FlightScheduleFormComponent | ADMIN/SUPER_ADMIN |
| `/carriers`, `/carriers/new`, `/carriers/:id/edit` | Carrier* | Authenticated read / ADMIN write |
| `/bookings` | BookingListComponent | Authenticated (own bookings, or all for ADMIN) |
| `/bookings/new` | BookingFormComponent | CUSTOMER (requires navigation state from search) |
| `/super-admin/admin-requests` | AdminRequestsComponent | SUPER_ADMIN |

Guards: `authGuard` (any logged-in user), `customerGuard`, `adminGuard` (ADMIN or
SUPER_ADMIN), `superAdminGuard`.

## The Booking Flow, In Detail

1. Customer goes to **Search Flights** (`/flights/search`), picks origin/destination/date.
2. `FlightService.search()` calls `GET /api/flights/search` — real results from the database,
   each result carrying a `scheduleId` (not just a `flightId`).
3. Clicking **Select Flight** navigates to `/bookings/new`, passing the full
   `FlightSearchResult` via Angular Router **navigation state** (not query params) — so the
   booking form has the schedule's live availability/fare without a second round trip.
4. The booking form lets the customer pick seat category + seat count (capped client-side to
   the available seats shown), then submits `POST /api/bookings` with
   `{ scheduleId, noOfSeats, seatCategory }` — **no `userId` is ever sent**; the backend
   resolves the booking owner from the JWT.

## Project Structure

```
src/app
├── app.routes.ts, app.config.ts, app.component.ts
├── core
│   ├── models/models.ts        Mirrors backend DTOs exactly (flightNumber, baseFare,
│   │                            scheduleId-based booking, adminStatus, etc.)
│   ├── services/                Auth (JWT), Carrier, Flight (+ search/schedules),
│   │                            Booking (/my, no userId), SuperAdmin, Notification
│   ├── guards/                  auth, admin, super-admin, customer
│   └── interceptors/            jwt (Bearer), error (surfaces backend messages)
├── shared/navbar/                Role-aware nav + global alert banner
├── auth/{login,register,register-admin}/
├── dashboard/                    Role-aware landing page
├── carriers/{carrier-list,carrier-form}/
├── flights/{flight-list,flight-form,flight-schedule-form,flight-search}/
├── bookings/{booking-list,booking-form}/
└── super-admin/admin-requests/   Pending-request review + approve/reject
```

## Frontend Validation

Mirrors the backend rules for a good UX (inline error messages under each field), but the
backend is always authoritative:

- Username: 4–30 chars, starts with a letter, letters/digits/underscore only.
- Password: 8+ chars, uppercase, lowercase, digit, special character.
- Phone: 10 digits, starts with 6/7/8/9.
- ZIP: 6 digits, doesn't start with 0.
- DOB: not in the future, minimum age 12.
- Flight number: pattern like `AI101`.
- Origin ≠ destination (flight creation and search forms both cross-validate this).
- Schedule departure time must be before arrival time; travel date can't be in the past.
- Carrier discount/refund percentages: 0–100.

## Building for Production

```bash
npm run build
```

Output goes to `dist/ams-frontend`. Update `src/environments/environment.prod.ts` to point
at your deployed backend.
