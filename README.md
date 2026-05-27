# shophelp

A web app for the price-conscious shopper. Compare product prices across stores and plan a map-based shopping route so you spend less and shop smarter.

## Tech stack

- **Frontend:** Angular 18 + Leaflet (OpenStreetMap tiles, no API key)
- **Backend:** Java 21 + Spring Boot 3, Spring Data JPA / Hibernate
- **Database:** PostgreSQL 16, schema managed by Flyway migrations
- **API:** OpenAPI 3 — generated from the Spring controllers by springdoc, with Swagger UI for trying endpoints
- **Everything runs in Docker** — no local JDK, Node, or Maven needed

## Quick start

You only need Docker (with Compose). From the project root:

```bash
docker compose up --build
```

Then open:

| Service     | URL                                   |
|-------------|---------------------------------------|
| Frontend    | http://localhost:4200                 |
| Backend API | http://localhost:8080/api             |
| Swagger UI  | http://localhost:8080/swagger-ui.html |
| OpenAPI doc | http://localhost:8080/v3/api-docs     |
| PostgreSQL  | localhost:5432 (db/user/pass: `shophelp`) |

The frontend (nginx) proxies `/api` to the backend, so the app is served from a single origin. Stop with `docker compose down` (add `-v` to also drop the database volume).

## Project structure

```
shophelp/
├── docker-compose.yml      # db + backend + frontend
├── backend/                # Spring Boot service
│   ├── Dockerfile          # multi-stage Maven build → JRE image
│   └── src/main/
│       ├── java/.../model      # JPA entities (Store, Product, StorePrice)
│       ├── java/.../repo        # Spring Data repositories
│       ├── java/.../service     # ComparisonService, RouteService
│       ├── java/.../web         # REST controllers
│       └── resources/db/migration  # Flyway V1 schema + V2 seed data
└── frontend/               # Angular app
    ├── Dockerfile          # multi-stage Node build → nginx image
    ├── nginx.conf          # serves the SPA, proxies /api to the backend
    └── src/app             # ApiService + main component with the Leaflet map
```

## API endpoints

| Method | Path                     | Purpose                                                        |
|--------|--------------------------|----------------------------------------------------------------|
| GET    | `/api/products`          | List products                                                  |
| GET    | `/api/products/{id}/prices` | One product's price at every store, cheapest first          |
| GET    | `/api/stores`            | List stores (with coordinates)                                 |
| POST   | `/api/basket/compare`    | Per-store total for a basket; cheapest complete basket first   |
| POST   | `/api/route/plan`        | Ordered map route buying each item at its cheapest store       |

Example:

```bash
curl -X POST http://localhost:8080/api/basket/compare \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"productId":1,"quantity":2},{"productId":5,"quantity":1}]}'
```

## How it works

- **Price comparison** sums each store's price for the basket and ranks stores, putting those that carry the whole basket first.
- **Route planning** picks the cheapest store for each item, then orders the resulting stores with a nearest-neighbour heuristic from your start location, returning waypoints the frontend draws on the map.

Seed data ships with sample supermarkets in and around **Bolsward, Friesland** (Albert Heijn, Jumbo, Lidl, Poiesz, and Aldi in Sneek) plus ~10 common products with varying prices.

## Roadmap

### Phase 1 — Foundations ✅
- [x] Project scaffolding: backend, frontend, and `docker-compose` for PostgreSQL
- [x] OpenAPI via springdoc (Swagger UI + spec at `/v3/api-docs`)
- [x] Database schema + Flyway migrations
- [x] Seed data with sample stores, products, and prices

### Phase 2 — Price comparison
- [x] Basket builder (add/remove items, quantities) in the UI
- [x] Per-store basket totals with the cheapest option highlighted
- [x] Angular price-comparison view
- [ ] Persisted shopping lists and product/price CRUD

### Phase 3 — Shopping route on a map ✅
- [x] Store locations with latitude/longitude
- [x] Route planning: cheapest store per item, ordered into a visit route
- [x] Leaflet map view with numbered store markers and the route drawn
- [ ] Road-accurate routing (OSRM) with straight-line fallback

### Phase 4 — Polish
- [ ] User accounts and saved lists
- [ ] Price history and trends
- [ ] Deal/discount alerts
- [ ] Mobile-friendly responsive UI
- [ ] CI pipeline and deployment

> Status: a working vertical slice (price comparison + map route) runs end-to-end in Docker.
