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
- Spring WebSocket (STOMP)
- Spring Security (basic setup)
- MySQL
- Springdoc OpenAPI (Swagger UI)
- Maven

---

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
mvn spring-boot\:run
```

4. Run tests:
```bash
mvn test
```

---

## MySQL Setup

### Option 1: Automatic Setup (Recommended)
Spring Boot will **automatically run migrations** when the project starts, if Flyway is enabled (`FLYWAY_ENABLED=true`).

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

### Option 2: Manual Import (For phpMyAdmin/MySQL Server)
If you cannot create the database manually, you can import the provided `.sql` dump file:

1. **Download the dump file:**
   - [`sql/bidly_auction_database.sql`](sql/bidly_auction_database.sql)

2. **Import using phpMyAdmin:**
   - Open phpMyAdmin and select your database.
   - Go to the **Import** tab.
   - Choose the `bidly_auction_database.sql` file and click **Go**.

3. **Import using MySQL Command Line:**
   ```bash
   mysql -u [username] -p[password] bidly_auction < sql/bidly_auction_database.sql
   ```

---

## Default Seed Data

- Roles seeded by migration: `ADMIN`, `BIDDER`
- `DataInitializer` ensures this admin exists (if missing):
  - Email: `admin@bidly.com`
  - Password: `admin123`
  - Role: `ADMIN`

---

## Architecture / Key Components

Controllers:
- `AuthController`: signup/login
- `ItemController`: create item, list items, search items, get item, place bid, get winner

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

---

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

### Real-time Updates (WebSockets)
The application uses Spring WebSockets with STOMP to notify clients about new bids in real-time.

- **Endpoint**: `/ws` (supports SockJS)
- **Subscription Topic**: `/topic/items/{itemId}`

**How it works:**
1. A client connects to the WebSocket endpoint.
2. The client subscribes to the topic for a specific item (e.g., `/topic/items/1`).
3. When a valid bid is placed via the REST API (`POST /items/1/bids`), the server broadcasts the new bid details to all subscribers of that topic.

---

## Data Persistence
- Uses Spring Data JPA + MySQL
- Item and bid data are persisted in tables:
  - `items`
  - `bids`

---

## Error Handling And Validation
- Global exception handling via `GlobalExceptionHandler`
- Handles:
  - `ResponseStatusException` (business errors)
  - `MethodArgumentNotValidException` (request validation errors)
  - `ObjectOptimisticLockingFailureException` (`409 CONFLICT`)
- Request payloads validated with Spring Validation annotations (`@Valid`, `@NotBlank`, `@NotNull`, `@Future`, `@DecimalMin`, etc.)

---

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

---

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
- `GET /items` (BIDDER/ADMIN, active items only)
- `GET /items/search` (BIDDER/ADMIN, filtered search)
- `GET /items/{itemId}` (BIDDER/ADMIN)
- `POST /items/{itemId}/bids` (BIDDER)
- `GET /items/{itemId}/winner` (BIDDER/ADMIN)

### Search/Filter Endpoint

`GET /items/search` supports query parameters:
- `keyword` (search in title/description)
- `status` (`ACTIVE` or `CLOSED`)
- `minStartingPrice`
- `maxStartingPrice`
- `auctionEndAfter` (ISO datetime, e.g. `2026-03-01T00:00:00`)
- `auctionEndBefore` (ISO datetime)

Rules:
- If `status` is not provided, default is `ACTIVE`
- `minStartingPrice` must be `<= maxStartingPrice`
- `auctionEndAfter` must be `<= auctionEndBefore`

---

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

Search items:
```bash
curl -X GET "http://localhost:8080/items/search?keyword=clock&status=ACTIVE&minStartingPrice=50&maxStartingPrice=500&auctionEndBefore=2026-12-31T23:59:59" \
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

Get winner for closed auction:
```bash
curl -X GET http://localhost:8080/items/1/winner \
  -H "X-User-Email: bidder1@example.com"
```

---

## Postman Collection

A ready collection is included:
- `postman/Auction.postman_collection.json`

Import it into Postman and set variables:
- `baseUrl` (default: `http://localhost:8080`)
- `adminEmail`
- `bidderEmail`
