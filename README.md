# AMS — Airline Management System (Full Stack)

A realistic Airline Management System with role-based access (Super Admin / Admin / Customer),
an admin-approval workflow, JWT authentication, real database-backed flight search, and a
concurrency-safe booking/cancellation engine with discount and refund calculation.

## Enhancement notes

Carriers use a unique two-letter uppercase code and flights use matching `AA101` numbers. Booking returns a payment-backed mock ticket number, and `GET /api/bookings/{bookingId}/cancellation-preview` provides the refund before cancellation.

Recurring `FlightScheduleTemplate` records use pre-generation: concrete `FlightSchedule` rows are generated for a 90-day window, preserving search and booking schedule IDs. Admin APIs: `POST`/`GET /api/flights/{flightId}/schedule-templates`, `PUT /api/flights/schedule-templates/{templateId}`, and `PUT /api/flights/schedule-templates/{templateId}/deactivate`.

The email allow-list is a pragmatic typo-catcher, not ownership verification. Registration age is 18–75. Set `SUPER_ADMIN_SECURITY_SPORT` and `SUPER_ADMIN_SECURITY_HOBBY` with bootstrap credentials for password recovery.

```
AMS/
├── backend/     Spring Boot 3 (Java 17) REST API
├── frontend/    Angular 17 (standalone components) SPA
└── README.md    This file
```

## 1. Architecture

```
Angular SPA (localhost:4200)
        │  JWT (Authorization: Bearer <token>)
        ▼
Spring Boot API (localhost:8080)
        │
        ▼
MySQL (ams_db)
```

- **Auth**: stateless JWT. Login (`POST /api/auth/login`) returns a token; every subsequent
  request carries it as `Authorization: Bearer <token>`. There is no browser Basic-Auth popup.
- **Roles**: `SUPER_ADMIN` (exactly one, ever), `ADMIN` (must be approved by the Super Admin
  before it can log in), `CUSTOMER` (self-registers freely).
- **Authorization is enforced server-side** via `@PreAuthorize` and, for booking ownership,
  by reading the authenticated principal out of the Spring Security context — never by
  trusting a `userId` sent from the frontend.
- **Flight model**: `Flight` (the permanent service definition, e.g. "AI101 Chennai→Delhi") is
  separate from `FlightSchedule` (a specific date/time instance of that flight, with its own
  seat-booking counters). Customers search and book against a `FlightSchedule`
  (`scheduleId`), never a bare `flightId`.
- **Concurrency safety**: booking and cancellation acquire a pessimistic write lock on the
  `FlightSchedule` row (plus an `@Version` optimistic-lock column as a second line of
  defense), so two simultaneous booking requests against the same schedule cannot both
  succeed against the same last remaining seat.

## 2. Backend Setup

See `backend/README.md` for full detail. Quick start:

```bash
cd backend
export DB_USERNAME=root
export DB_PASSWORD=your_password
export SUPER_ADMIN_USERNAME=superadmin
export SUPER_ADMIN_PASSWORD='Sup3rAdmin!Pass'
export JWT_SECRET='replace-with-a-long-random-secret-in-any-real-deployment'

mvn clean test        # run the test suite (uses in-memory H2, no MySQL needed)
mvn clean package
mvn spring-boot:run   # starts on http://localhost:8080
```

MySQL database creation (once):
```sql
CREATE DATABASE ams_db;
```

Swagger UI (no login popup — it's public, but the endpoints it documents still require a
JWT where applicable): `http://localhost:8080/swagger-ui.html`

## 3. Frontend Setup

See `frontend/README.md` for full detail. Quick start:

```bash
cd frontend
npm install
npm start   # starts on http://localhost:4200
```

Confirm `frontend/src/environments/environment.ts` points at your backend
(`apiBaseUrl: 'http://localhost:8080/api'` by default).

## 4. Super Admin Initialization

There is **no public API to create a Super Admin** — this is enforced structurally (no
controller endpoint exists for it) and defensively (`UserRepository.existsByRole("SUPER_ADMIN")`
is checked before creation).

The single, initial Super Admin is created automatically on backend startup from
`SUPER_ADMIN_USERNAME` / `SUPER_ADMIN_PASSWORD` environment variables:

- If a `SUPER_ADMIN` already exists in the database → nothing happens (idempotent).
- If none exists and the env vars are set → exactly one is created.
- If none exists and the env vars are **not** set → the app still starts, but logs a warning;
  set the env vars and restart to bootstrap it.

## 5. Admin Approval Workflow

```
Anyone → POST /api/admin/register-request
              │
              ▼
      User row created: role=ADMIN, adminStatus=PENDING
              │
              ▼
      Login attempt while PENDING → rejected (AdminApprovalException, 400)
              │
              ▼
Super Admin → GET /api/super-admin/admin-requests   (list pending)
Super Admin → PUT /api/super-admin/admin-requests/{id}/approve
                  or
              PUT /api/super-admin/admin-requests/{id}/reject
              │
              ▼
      adminStatus=APPROVED → can now log in and receives a JWT with ROLE_ADMIN
      adminStatus=REJECTED → login permanently rejected with a clear message
```

Only `SUPER_ADMIN` can call the `/api/super-admin/**` endpoints (`@PreAuthorize` +
`SecurityConfig` both enforce this) — a regular `ADMIN` cannot approve another admin.

## 6. Customer Workflow

```
Register (POST /api/users/register, forces role=CUSTOMER server-side)
        ↓
Login (POST /api/auth/login) → JWT
        ↓
Search Flights (GET /api/flights/search?origin=&destination=&travelDate=)
        ↓
Select a result (carries scheduleId)
        ↓
Confirm Booking (POST /api/bookings, body: { scheduleId, noOfSeats, seatCategory })
        ↓
View My Bookings (GET /api/bookings/my — always the authenticated user's own, never
                   determined by a client-supplied userId)
        ↓
Cancel (PUT /api/bookings/{id}/cancel — rejected with 403 if not the booking's owner)
```

## 7. Flight Search Workflow

`GET /api/flights/search?origin=Chennai&destination=Delhi&travelDate=2026-09-20` queries the
database directly:

```sql
flight.origin = :origin
AND flight.destination = :destination
AND schedule.travelDate = :travelDate
AND schedule.status = 'SCHEDULED'
-- then filtered in-memory for any seat category with availableSeats > 0
```

Nothing is hardcoded or mocked; every result comes from `Flight` + `FlightSchedule` rows.

## 8. Booking Workflow

Base amount = `baseFare × noOfSeats`. Discounts are additive percentages, capped at 100%:

```
totalDiscountPercentage = advanceDiscount + categoryDiscount + bulkDiscount   (capped at 100)
discountAmount = baseAmount × totalDiscountPercentage / 100
finalAmount = max(0, baseAmount − discountAmount)
```

- **Advance discount**: 90+/60–89/30–59 days out → carrier's respective discount tier; <30 days → 0.
- **Category discount**: SILVER/GOLD/PLATINUM → carrier's respective tier; REGULAR → 0.
- **Bulk discount**: `noOfSeats >= 10` → carrier's `bulkBookingDiscount`.

The booking is created against the locked `FlightSchedule` row, and its seat counters are
incremented in the same transaction — both succeed or both roll back.

## 9. Cancellation Workflow

```
daysRemaining = travelDate − today
refundPercentage = 20+ days: refund20DaysOrMore
                    10–19 days: refund10DaysBefore
                    2–9 days: refund2DaysBefore
                    0–1 days: cancellation REJECTED (no refund tier covers this window)
refundAmount = bookingAmount × refundPercentage / 100
```

Only the booking's owner (or an ADMIN/SUPER_ADMIN) may cancel it — enforced against the
authenticated principal, not a request parameter.

## 10. API Documentation

Full endpoint list, sample requests/responses, and error-response shape are documented in
`backend/README.md`. Interactive docs are also available via Swagger UI once the backend is
running: `http://localhost:8080/swagger-ui.html`.

## 11. Validation Rules Summary

| Field | Rule |
|---|---|
| Username | 4–30 chars, starts with a letter, letters/digits/underscore only, unique (case-insensitive) |
| Password | 8+ chars, ≥1 uppercase, ≥1 lowercase, ≥1 digit, ≥1 special char (`@$!%*?&`) |
| Phone | Exactly 10 digits, starts with 6/7/8/9 |
| Email | Valid email format, unique (case-insensitive) |
| ZIP code | 6 digits, doesn't start with 0 |
| DOB | Not in the future, minimum age 12 |
| Customer category | REGULAR / SILVER / GOLD / PLATINUM only |
| Carrier discounts/refunds | 0–100 (inclusive) |
| Flight number | e.g. `AI101`, `6E203`, `UK811`; unique |
| Origin/Destination | Required, must differ from each other |
| Schedule departure/arrival | Departure must be before arrival; travel date can't be in the past |
| Booking seats | ≥1, can't exceed the schedule's available seats in that category |

All of the above are enforced **server-side** via Jakarta Bean Validation (with custom
`@MinimumAge`/`@NotPastDate` annotations where needed) and service-layer business-rule checks
— the frontend mirrors them for UX, but the backend is authoritative.

## 12. Role Permissions Summary

| Action | CUSTOMER | ADMIN (approved) | SUPER_ADMIN |
|---|---|---|---|
| Register / login | ✅ | ✅ (after approval) | ✅ |
| Search / book / cancel own flights | ✅ | – | – |
| View own bookings | ✅ | – | – |
| View all bookings | ❌ | ✅ | ✅ |
| Manage carriers/flights/schedules | ❌ | ✅ | ✅ |
| View/approve/reject admin requests | ❌ | ❌ | ✅ |
| Create Super Admin | ❌ | ❌ | N/A (bootstrap-only, see §4) |

## 13. Running Tests

```bash
cd backend
mvn test
```

Covers: password/username/phone/email/zip/DOB validation, Super Admin uniqueness, the
pending → approved/rejected admin workflow (and that pending admins can't log in as active
admins), carrier/flight/schedule validation (duplicate names, same origin/destination,
invalid times), real search behavior, and the full booking/cancellation lifecycle
(discounts, bulk pricing, insufficient seats, ownership enforcement, refund calculation,
seat-count restoration).

## 14. Postman / Manual API Testing

See `backend/README.md` section "Postman Testing Instructions" for a step-by-step sequence
(register → admin request → super admin login/approve → carrier → flight → schedule →
search → book → cancel) with sample JSON for every call.
