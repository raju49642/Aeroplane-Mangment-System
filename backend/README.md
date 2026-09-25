# AMS Backend — Airline Management System

Spring Boot 3 (Java 25) REST API implementing a realistic airline booking system: JWT auth,

## Pricing, payments, and recovery

Carriers configure business and executive pricing multipliers (minimum 1.0), and searches return a fare for every seat category. Membership payment endpoints are `POST /api/payments/membership`, `POST /api/payments/membership/{paymentId}/confirm`, and `GET /api/payments/my`. This is a mock payment flow only: never use real card details. Raw card numbers and CVVs are never persisted. Password recovery verifies BCrypt-hashed security answers and issues a 10-minute reset-only JWT. Hibernate `ddl-auto=update` creates the new columns and payments table.
a Super Admin / Admin-approval workflow, real database-backed flight search against separate
`Flight`/`FlightSchedule` entities, and a concurrency-safe booking/cancellation engine.

## 1. Technology Stack

- Java 25 runtime with Java 21-compatible bytecode, Spring Boot 3.3.4, Maven
- Spring Web, Spring Data JPA, Spring Validation, Spring Security
- JWT (jjwt) for stateless authentication
- MySQL (runtime), H2 (tests)
- springdoc-openapi (Swagger UI)
- BCrypt password hashing

## 2. Project Structure

```
backend/
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/ams
    │   │   ├── AMSApplication.java
    │   │   ├── config       SecurityConfig, JwtService, JwtAuthFilter,
    │   │   │                SuperAdminInitializer, OpenApiConfig
    │   │   ├── controller   User, Admin, SuperAdmin, Auth, Carrier, Flight, Booking
    │   │   ├── dto
    │   │   ├── entity       User, Carrier, Flight, FlightSchedule, Booking
    │   │   ├── exception    custom exceptions + GlobalExceptionHandler
    │   │   ├── repository
    │   │   ├── service / service/impl
    │   │   └── validation   @MinimumAge, @NotPastDate custom validators
    │   └── resources/application.properties
    └── test
        ├── java/com/ams/dto      pure Bean Validation tests
        ├── java/com/ams/service  Spring-context service/integration tests
        └── resources/application-test.properties (H2)
```

## 3. Database Setup

```sql
CREATE DATABASE ams_db;
```

`spring.jpa.hibernate.ddl-auto=update` creates/updates all tables automatically on startup.

## 4. Environment Variables

| Variable | Purpose | Default (dev only) |
|---|---|---|
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials | `root` / `root` |
| `JWT_SECRET` | HMAC signing key for JWTs (32+ chars) | a placeholder dev string — **override in any real deployment** |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `86400000` (24h) |
| `SUPER_ADMIN_USERNAME` / `SUPER_ADMIN_PASSWORD` | Bootstraps the one-and-only initial Super Admin on first startup | unset (no Super Admin created until set) |

## 5. How to Run

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export SUPER_ADMIN_USERNAME=superadmin
export SUPER_ADMIN_PASSWORD='Sup3rAdmin!Pass'
export JWT_SECRET='replace-with-a-long-random-secret'

mvn clean test
mvn clean package
mvn spring-boot:run
```

API starts on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`
(public — no login popup; paste a JWT into the "Authorize" button to call protected endpoints
from the docs page).

## 6. Authentication

Stateless JWT. `POST /api/auth/login` returns a token; send it on every subsequent request as:

```
Authorization: Bearer <token>
```

There is no HTTP Basic / browser popup. `JwtAuthFilter` reads the header, validates the
token, and populates Spring Security's context so `@PreAuthorize` and
`SecurityContextHolder.getContext().getAuthentication()` work normally in controllers/services.

## 7. Roles

- **SUPER_ADMIN** — exactly one, ever (see root README §4). Full system access, plus
  exclusive rights to approve/reject admin requests.
- **ADMIN** — must be approved by the Super Admin before `adminStatus=APPROVED`; a `PENDING`
  or `REJECTED` admin account cannot log in and obtain a working token. Manages carriers,
  flights, and schedules.
- **CUSTOMER** — self-registers freely via `POST /api/users/register` (role is always forced
  server-side; the request body has no role field at all). Searches, books, views/cancels
  own bookings only.

## 8. API Endpoints

### Public
| Method | Path |
|---|---|
| POST | /api/users/register |
| POST | /api/admin/register-request |
| POST | /api/auth/login |

### Super Admin only
| Method | Path |
|---|---|
| GET | /api/super-admin/admin-requests |
| PUT | /api/super-admin/admin-requests/{id}/approve |
| PUT | /api/super-admin/admin-requests/{id}/reject |

### Carrier (ADMIN/SUPER_ADMIN write, any authenticated user read)
| Method | Path |
|---|---|
| POST | /api/carriers |
| GET | /api/carriers |
| GET | /api/carriers/{carrierId} |
| PUT | /api/carriers/{carrierId} |

### Flight & Schedule
| Method | Path | Access |
|---|---|---|
| POST | /api/flights | ADMIN/SUPER_ADMIN |
| GET | /api/flights | Authenticated |
| GET | /api/flights/{flightId} | Authenticated |
| GET | /api/flights/carrier/{carrierName} | Authenticated |
| PUT | /api/flights/{flightId} | ADMIN/SUPER_ADMIN |
| POST | /api/flights/{flightId}/schedules | ADMIN/SUPER_ADMIN |
| GET | /api/flights/search?origin=&destination=&travelDate= | Authenticated |

### Booking
| Method | Path | Access |
|---|---|---|
| POST | /api/bookings | CUSTOMER |
| GET | /api/bookings | ADMIN/SUPER_ADMIN |
| GET | /api/bookings/my | Authenticated (own bookings only) |
| GET | /api/bookings/{bookingId} | Owner or ADMIN/SUPER_ADMIN |
| PUT | /api/bookings/{bookingId}/cancel | Owner or ADMIN/SUPER_ADMIN |

## 9. Sample Requests

**Register (customer)** — `POST /api/users/register`
```json
{
  "userName": "john_doe",
  "password": "Passw0rd!",
  "customerCategory": "REGULAR",
  "phone": "9876543210",
  "emailId": "john@example.com",
  "address1": "12 MG Road",
  "address2": "",
  "city": "Chennai",
  "state": "Tamil Nadu",
  "zipCode": "600001",
  "dob": "2000-05-10"
}
```

**Admin access request** — `POST /api/admin/register-request`
```json
{ "userName": "admin_ops", "password": "Passw0rd!", "emailId": "ops@ams.com", "phone": "9876543211" }
```

**Login** — `POST /api/auth/login`
```json
{ "userName": "john_doe", "password": "Passw0rd!" }
```
Response:
```json
{ "userId": 1, "userName": "john_doe", "role": "CUSTOMER", "token": "eyJhbGciOi...", "message": "Login successful" }
```

**Create flight** — `POST /api/flights` (ADMIN, `Authorization: Bearer <token>`)
```json
{
  "carrierId": 1,
  "flightNumber": "AI101",
  "origin": "Chennai",
  "destination": "Delhi",
  "baseFare": 7500,
  "seatCapacityBusinessClass": 20,
  "seatCapacityEconomyClass": 100,
  "seatCapacityExecutiveClass": 10
}
```

**Create schedule** — `POST /api/flights/1/schedules`
```json
{ "travelDate": "2026-09-20", "departureTime": "06:30", "arrivalTime": "09:15" }
```

**Search** — `GET /api/flights/search?origin=Chennai&destination=Delhi&travelDate=2026-09-20`

**Book** — `POST /api/bookings` (CUSTOMER)
```json
{ "scheduleId": 101, "noOfSeats": 2, "seatCategory": "ECONOMY" }
```
Note: no `userId` field — the backend resolves the booking owner from the JWT.

## 10. Error Response Shape

```json
{
  "timestamp": "2026-08-15T20:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "password: Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.",
  "path": "/api/users/register"
}
```

| Exception | HTTP Status |
|---|---|
| ResourceNotFoundException | 404 |
| DuplicateResourceException | 409 |
| InvalidCredentialsException | 401 |
| InsufficientSeatsException | 400 |
| InvalidBookingException | 400 |
| AdminApprovalException | 400 |
| AccessDeniedException | 403 |
| MethodArgumentNotValidException | 400 |
| DataIntegrityViolationException | 409 |
| ObjectOptimisticLockingFailureException | 409 |
| Any other Exception | 500 |

## 11. Postman Testing Instructions

1. `POST /api/users/register` — create a customer.
2. `POST /api/admin/register-request` — create a pending admin.
3. `POST /api/auth/login` with the bootstrapped Super Admin credentials → copy the `token`.
4. In Postman, set an environment variable from that token; add header
   `Authorization: Bearer {{token}}` to all subsequent admin/super-admin requests.
5. `GET /api/super-admin/admin-requests` → find the pending admin's `userId`.
6. `PUT /api/super-admin/admin-requests/{userId}/approve`.
7. `POST /api/auth/login` as that now-approved admin → copy their token.
8. `POST /api/carriers` (as admin) → note `carrierId`.
9. `POST /api/flights` (as admin) → note `flightId`.
10. `POST /api/flights/{flightId}/schedules` (as admin).
11. `POST /api/auth/login` as the customer from step 1 → copy their token.
12. `GET /api/flights/search?origin=...&destination=...&travelDate=...` (as customer).
13. `POST /api/bookings` with the returned `scheduleId` (as customer).
14. `GET /api/bookings/my` (as customer).
15. `PUT /api/bookings/{bookingId}/cancel` (as customer).

## 12. Running Tests

```bash
mvn test
```

Runs against in-memory H2 — no MySQL needed. Covers Bean Validation rules (password,
username, phone, email, zip, DOB), Super Admin single-instance enforcement, the
pending/approve/reject admin workflow, carrier/flight/schedule validation, real search
behavior, and the full booking/cancellation lifecycle including ownership enforcement and
refund calculation.
