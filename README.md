# Aranthangi Mart 🛒

A local grocery store web app for Aranthangi — Angular frontend + Java 21 / Spring Boot API, with H2 in-memory data.
Customers browse groceries, add to cart, and place a **demo** delivery order (no real payment).

## Features
- Product catalog with search, category filter, and sorting (price / name)
- Stock tracking: "Only 3 left" / "Out of stock" badges, quantity capped at stock
- Cart with +/- on the product card and in the drawer, saved in the browser (survives refresh)
- Delivery charge rules from the backend: ₹30, **free above ₹499** (configurable)
- Checkout with name, mobile number (10-digit Indian format) and delivery address — validated on both sides
- **Server-side pricing & stock check**: prices come from the DB, stock rows are locked while ordering so the last item can't be sold twice; failed orders roll back
- "My orders" — look up past orders by mobile number
- Separate Dockerfiles for frontend and backend, external-database support via env vars, health check endpoint, API tests

## Run locally

Backend (Java 21 + Maven):
```bash
cd backend
mvn spring-boot:run          # http://localhost:8080
mvn test                     # API tests
```

Frontend (Node 20+):
```bash
cd frontend
npm install
npm start                    # http://localhost:4200  (proxies /api to :8080)
```

## Run with Docker (separate images)

**Backend**
```bash
cd backend
docker build -t aranthangi-mart-api .

# quick try with the built-in in-memory H2 (data resets on restart)
docker run -p 8080:8080 aranthangi-mart-api

# with your own database
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/aranthangimart \
  -e SPRING_DATASOURCE_USERNAME=<user> \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e H2_CONSOLE_ENABLED=false \
  aranthangi-mart-api
```
PostgreSQL (`jdbc:postgresql://...`) and MySQL (`jdbc:mysql://...`) drivers are bundled. Tables are created automatically on first start (`DDL_AUTO=update`) and the sample grocery products are seeded only if the products table is empty.

**Frontend**
```bash
cd frontend
docker build -t aranthangi-mart-web .
docker run -p 8081:80 -e BACKEND_URL=http://<backend-host>:8080 aranthangi-mart-web
# open http://localhost:8081
```
nginx serves the Angular build and proxies `/api/*` to `BACKEND_URL`, so the browser talks to a single origin (no CORS setup needed). `BACKEND_URL` must be reachable from the frontend container (for example the backend container name on a shared Docker network, a private IP, or `http://host.docker.internal:8080`). nginx resolves the host when it starts, so start the backend first.

Backend health: `GET /actuator/health` (the image also has a built-in `HEALTHCHECK`).

## API
| Method | Path | Notes |
|---|---|---|
| GET | `/api/products?search=&category=&sort=` | `sort`: `default`, `priceAsc`, `priceDesc`, `name` |
| GET | `/api/products/categories` | distinct category names |
| GET | `/api/products/{id}` | |
| GET | `/api/store` | name, tagline, delivery fee, free-delivery threshold |
| POST | `/api/orders` | `{customerName, phone, address, items:[{productId, quantity}]}` |
| GET | `/api/orders?phone=` | order history for a mobile number |
| GET | `/api/orders/{id}?phone=` | single order; phone must match |
| GET | `/actuator/health` | liveness/health |

Errors return JSON with a `message` field (400 validation, 409 insufficient stock, 404 not found).

## Configuration (env vars)
| Variable | Default | Purpose |
|---|---|---|
| `MART_DELIVERY_FEE` | `30` | Delivery charge in ₹ |
| `MART_DELIVERY_FREE_ABOVE` | `499` | Subtotal at/above which delivery is free |
| `MART_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Comma-separated browser origins allowed to call the API |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | in-memory H2 | Your database connection |
| `DDL_AUTO` | `update` | Hibernate schema mode (`validate` once you manage the schema yourself) |
| `H2_CONSOLE_ENABLED` | `true` | Set `false` outside local dev |
| `BACKEND_URL` (frontend container) | `http://host.docker.internal:8080` | Where nginx forwards `/api` |

## Renaming the store
The store name/tagline are served from the backend (`mart.name`, `mart.tagline` in `application.properties`), so the UI header, hero and footer pick them up automatically. Static places to change as well: `frontend/src/index.html` (page title) and the Docker/package names.

## Known limitations (next steps)
- Default H2 is in-memory (resets on restart); connect a real database via env vars. Next: Flyway migrations instead of `ddl-auto`.
- No login. Orders are looked up by mobile number only; add OTP/auth before real use.
- No admin screen for products/stock/order status yet (`OrderStatus` enum is ready for it).
- No real payments (UPI/Razorpay), no delivery-slot or pincode check.
