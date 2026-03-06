# Real-time Auction Backend (Spring Boot + MySQL)

## Tech Stack
- Java 21
- Spring Boot (Web, Data JPA, Validation, Security)
- JWT authentication
- Flyway migrations
- MySQL
- Springdoc OpenAPI (Swagger UI)

## User Types
- `ADMIN`: can create auction items
- `BIDDER`: can place bids

## Authentication Model
- This API uses **JWT Bearer tokens**.
- Get token from `POST /auth/login` (or auto-login after `POST /auth/register`).
- Pass token in header:
  - `Authorization: Bearer <token>`
- Authorization rules:
  - `POST /items` -> `ADMIN`
  - `POST /items/{itemId}/bids` -> `BIDDER`
  - `GET /items`, `GET /items/{id}`, `GET /items/{id}/winner` -> authenticated users

## Database Setup
1. Create MySQL database:
   ```sql
   CREATE DATABASE auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Update `src/main/resources/application.properties` with your MySQL username/password.
3. Start app; Flyway runs:
   - `V1__create_auction_schema.sql`
   - `V2__seed_roles_and_users.sql`

## Seeded Accounts
- `admin / Admin@123` (role: `ADMIN`)
- `bidder1 / Bidder@123` (role: `BIDDER`)

Note: Plain seeded passwords are automatically BCrypt-hashed at startup by `AdminPasswordBootstrap`.

## Run
```bash
./mvnw spring-boot:run
```

## Swagger
- URL: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Core API
- `POST /auth/register` (register bidder)
- `POST /auth/login` (get JWT)
- `POST /items` (admin creates auction item)
- `GET /items` (list active auctions, pageable)
- `GET /items/{itemId}` (item details + highest bid)
- `POST /items/{itemId}/bids` (place bid)
- `GET /items/{itemId}/winner` (winning bid for closed auction)

## curl Examples
### Login as admin
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"Admin@123\"}"
```

### Create item (admin token)
```bash
curl -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"title\":\"Antique Vase\",\"description\":\"Donated item\",\"startingPrice\":100.00,\"auctionEndTime\":\"2026-03-10T12:00:00\"}"
```

### Login as bidder
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"bidder1\",\"password\":\"Bidder@123\"}"
```

### Place bid (bidder token)
```bash
curl -X POST http://localhost:8080/items/1/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <BIDDER_TOKEN>" \
  -d "{\"amount\":120.00}"
```

## Auction Closing
- A scheduler runs every 5 seconds (`app.auction.close-check-delay-ms`) and closes expired auctions.
- On close, highest bid is selected as winner.

## Concurrency Approach
- `POST /items/{id}/bids` uses a pessimistic write lock on the item row (`findByIdForUpdate`) to reduce race conditions.
- `@Version` field on `items` supports optimistic concurrency as a second line of defense in real deployments.

