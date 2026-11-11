# ADR-0003: Stateless JWT Authentication and Role-Based Access Control

- **Date:** 2026-11-11
- **Status:** Accepted
- **Deciders:** Governance Architect, Security & Fullstack Integration Lead

## 1. Context and Problem Statement

The Sonido Vivo backend exposes administrative endpoints (`/api/v1/admin/**`) for catalog mutations, atomic stock adjustments, bulk price re-adjustments, customer management, and financial/inventory KPIs, consumed by a decoupled React (Vite) SPA running on a separate origin (`http://localhost:5173` or `http://localhost:3000`).

The security layer must authenticate administrators, enforce `ROLE_ADMIN` authorization cleanly across stateless HTTP requests, integrate seamlessly with React SPAs via `Authorization: Bearer <token>`, and avoid storing server-side HTTP session state.

## 2. Evaluated Alternatives

### Alternative 1: Stateful Cookie-Based Sessions (`JSESSIONID`)
- **Pros:** Native browser cookie handling and built-in immediate server-side session invalidation.
- **Cons (Why Rejected):** Couples API scalability to sticky sessions or shared Redis session stores, complicates cross-origin SPA consumption and CSRF token synchronization across Vite dev servers, and deviates from the `BearerAuth` OpenAPI contract in `CONTRACT_FULLSTACK_REST.md`.

### Alternative 2: HTTP Basic Authentication on Every Request
- **Pros:** Trivial to configure in Spring Security with zero token libraries.
- **Cons (Why Rejected):** Requires the React client to cache raw user credentials in memory or `localStorage`, transmits credentials on every API call, and offers no token expiration or claims-based role isolation.

### Alternative 3: Stateless Bearer JWT (`io.jsonwebtoken:jjwt` 0.12.6 + Spring Security) [CHOSEN]
- **Pros:** Fully stateless (`SessionCreationPolicy.STATELESS`), aligns directly with the OpenAPI `BearerAuth` security scheme, encodes subject and `ROLE_ADMIN` claims with configurable expiration (`JWT_EXPIRATION_MS`), and separates credential verification (`POST /api/v1/auth/login`) from request filtering (`JwtAuthenticationFilter`).
- **Cons:** Issued JWTs cannot be revoked instantaneously before expiration without introducing a token blocklist, and security relies strictly on keeping `JWT_SECRET` strong and externalized.

## 3. Decision and Consequences

We adopt **Stateless JWT Security** implemented with Spring Security and `io.jsonwebtoken` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` `0.12.6`):
- Signing secret (`app.jwt.secret`) MUST be supplied via the `${JWT_SECRET}` environment variable (no hardcoded production fallback in `src/main/resources/application.yaml`).
- Token TTL (`app.jwt.expiration-ms`) defaults to `86400000` ms (24 hours) via `${JWT_EXPIRATION_MS:86400000}`.
- Public routes are restricted strictly to `/api/v1/auth/**` and `/actuator/health`; all `/api/v1/admin/**` routes require `ROLE_ADMIN`.

- **Positive Consequences:**
  - Horizontal scalability with zero server-side session state.
  - Clean integration with React SPA `ProtectedRoute` guards and `Authorization: Bearer` headers.
- **Negative Consequences / Trade-offs:**
  - Immediate token revocation (e.g., compromised admin token prior to expiration) is not supported without adding a stateful revocation store (such as Redis).
  - Application startup in production/dev fails fast if `JWT_SECRET` is unset, preventing accidental deployment with weak default keys.
