# shophelp

A web app for the price-conscious shopper. Compare product prices across stores and plan a map-based shopping route so you spend less and shop smarter.

## Tech stack

- **Frontend:** Angular + Leaflet (OpenStreetMap)
- **Backend:** Java 21 + Spring Boot (Maven), Spring Data JPA / Hibernate
- **Database:** PostgreSQL (Docker)
- **API:** OpenAPI contract-first (single `openapi.yaml` generates both the Spring API interfaces and the Angular client)

## Core features

- Compare prices for a basket across multiple stores
- Build and manage shopping lists
- Plan a geographic shopping route on a map (which stores to visit, in what order)

## Roadmap

### Phase 1 — Foundations
- [ ] Project scaffolding: backend, frontend, and `docker-compose` for PostgreSQL
- [ ] OpenAPI contract (`api/openapi.yaml`) as the single source of truth
- [ ] Database schema + Flyway migrations (stores, products, prices, shopping lists)
- [ ] Seed data with sample stores, products, and prices

### Phase 2 — Price comparison
- [ ] CRUD for stores, products, and prices
- [ ] Shopping-list builder (add/remove items, quantities)
- [ ] Per-store basket totals with the cheapest option highlighted
- [ ] Angular price-comparison view

### Phase 3 — Shopping route on a map
- [ ] Store locations with latitude/longitude
- [ ] Route planning: select stores carrying the basket and order the visits
- [ ] Leaflet map view with store markers and the route drawn
- [ ] Road-accurate routing (OSRM) with straight-line fallback

### Phase 4 — Polish
- [ ] User accounts and saved lists
- [ ] Price history and trends
- [ ] Deal/discount alerts
- [ ] Mobile-friendly responsive UI
- [ ] CI pipeline and deployment

> Status: early development. Phase 1 in progress.
