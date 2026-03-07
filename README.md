# Simple Real-time Auction Backend (Spring Boot + MySQL)

This project is a backend for a simplified real-time auction system.

Implemented so far:
- Authentication (signup/login with role: `ADMIN` or `BIDDER`)
- Item management
- Bidding
- Automatic auction closing (scheduler)
- Optimistic locking for concurrent bids

## Tech Stack
- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Validation
- Spring Security (basic setup)
- MySQL
- Springdoc OpenAPI (Swagger UI)
- Maven

## Build And Run

1. Clone repository and go to project:
```bash
git clone https://github.com/ManjulaPradeep/bidly-auction.git
cd auction
```

2. Configure environment variables (optional, defaults are below):
- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `3306`)
- `DB_NAME` (default: `bidly_auction`)
- `FLYWAY_ENABLED` (default: `false`)

3. Run application:
```bash
mvn spring-boot:run
```

4. Run tests:
```bash
mvn test
```

## MySQL Setup

Default DB config from `application.properties`:
- URL: `jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:bidly_auction}`
- Username: `root`
- Password: `test`

Create database:
```sql
CREATE DATABASE bidly_auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Create DB user and grant privileges (example):
```sql
CREATE USER 'auction_user'@'localhost' IDENTIFIED BY 'auction_pass';
GRANT ALL PRIVILEGES ON bidly_auction.* TO 'auction_user'@'localhost';
FLUSH PRIVILEGES;
```

If using this user, set:
- `spring.datasource.username=auction_user`
- `spring.datasource.password=auction_pass`

Schema creation:
- Current app uses `spring.jpa.hibernate.ddl-auto=update` (auto update schema).
- Flyway scripts are available in `src/main/resources/db/migration`.
- Enable Flyway with `FLYWAY_ENABLED=true` if you want migration-based schema initialization.

## Default Seed Data

- Roles seeded by migration: `ADMIN`, `BIDDER`
- `DataInitializer` ensures this admin exists (if missing):
  - Email: `admin@bidly.com`
  - Password: `admin123`
  - Role: `ADMIN`

## Architecture / Key Components

Controllers:
- `AuthController`: signup/login
- `ItemController`: create item, list items, get item, place bid

Services:
- `AuthService`: user signup/login logic
- `ItemService`: item creation, item listing/details, close expired items
- `BidService`: bid validation + place bid
- `AuctionClosingScheduler`: periodic auto-close task
- `CurrentUserService`: resolves user from `X-User-Email` header

Repositories:
- `UserRepository`, `RoleRepository`, `ItemRepository`, `BidRepository`

Entities:
- `User`, `Role`, `Item`, `Bid`

## Core Logic (Assignment)

### Bid Validation
On `POST /items/{itemId}/bids`:
- Bidder must be role `BIDDER`
- Item must exist
- Auction must be open (`status=ACTIVE` and end time not reached)
- Bid amount must be greater than current highest bid

### Auction Closing
Automatic closing implemented in background scheduler:
- `AuctionClosingScheduler` runs every `auction.close.delay-ms` (default `15000` ms)
- Finds active items with `auctionEndTime <= now`
- Updates them to `CLOSED`

### Concurrency (Conceptual + Implemented)
Optimistic locking is implemented on `Item`:
- `@Version` field: `Long version`
- Hibernate manages version automatically
- Concurrent conflicting updates throw optimistic lock exception
- API returns `409 CONFLICT` with retry message

Real-system options (if traffic is very high):
- Bounded retries with backoff
- Pessimistic locking (`SELECT ... FOR UPDATE`)
- Queue/event-driven per-item bid serialization

## Data Persistence
- Uses Spring Data JPA + MySQL
- Item and bid data are persisted in tables:
  - `items`
  - `bids`

## Error Handling And Validation
- Global exception handling via `GlobalExceptionHandler`
- Handles:
  - `ResponseStatusException` (business errors)
  - `MethodArgumentNotValidException` (request validation errors)
  - `ObjectOptimisticLockingFailureException` (`409 CONFLICT`)
- Request payloads validated with Spring Validation annotations (`@Valid`, `@NotBlank`, `@NotNull`, `@Future`, `@DecimalMin`, etc.)

## Authentication / Authorization Model (Current Simple Approach)
- Auth endpoints are open:
  - `POST /api/auth/signup`
  - `POST /api/auth/login`
- Business endpoints identify user by header:
  - `X-User-Email: <registered-user-email>`
- Access rules:
  - Create item: `ADMIN` only
  - List/get items: `BIDDER` or `ADMIN`
  - Place bid: `BIDDER` only

## API Documentation

Swagger UI:
- `http://localhost:8080/swagger-ui/index.html`
- root path `/` redirects to Swagger UI

OpenAPI JSON:
- `http://localhost:8080/v3/api-docs`

### Endpoints
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /items` (ADMIN)
- `GET /items` (BIDDER/ADMIN)
- `GET /items/{itemId}` (BIDDER/ADMIN)
- `POST /items/{itemId}/bids` (BIDDER)

## Curl Examples

Signup bidder:
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Bidder One\",\"email\":\"bidder1@example.com\",\"password\":\"secret123\",\"role\":\"BIDDER\"}"
```

Signup admin:
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Admin One\",\"email\":\"admin1@example.com\",\"password\":\"secret123\",\"role\":\"ADMIN\"}"
```

Create item (ADMIN):
```bash
curl -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -H "X-User-Email: admin1@example.com" \
  -d "{\"title\":\"Vintage Clock\",\"description\":\"Charity donation\",\"startingPrice\":100.00,\"auctionEndTime\":\"2026-03-10T18:00:00\"}"
```

List active items:
```bash
curl -X GET http://localhost:8080/items \
  -H "X-User-Email: bidder1@example.com"
```

Get item by id:
```bash
curl -X GET http://localhost:8080/items/1 \
  -H "X-User-Email: bidder1@example.com"
```

Place bid:
```bash
curl -X POST http://localhost:8080/items/1/bids \
  -H "Content-Type: application/json" \
  -H "X-User-Email: bidder1@example.com" \
  -d "{\"bidAmount\":120.00}"
```

## Postman Collection

A ready collection is included:
- `postman/Auction.postman_collection.json`

Import it into Postman and set variables:
- `baseUrl` (default: `http://localhost:8080`)
- `adminEmail`
- `bidderEmail`
