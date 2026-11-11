# ADR-0001: Adoption of Hexagonal Modular Monolith Architecture

- **Date:** 2026-11-11
- **Status:** Accepted
- **Deciders:** Governance Architect, Backend Engineering Team

## 1. Context and Problem Statement

The Sonido Vivo backend must simultaneously satisfy two distinct operational profiles:
1. A reactive REST API consumed by a decoupled React (Vite) administrative dashboard and storefront (`CONTRACT_FULLSTACK_REST.md`).
2. Dual database integration spanning polymorphic document workflows in MongoDB (`sonido_vivo_nosql`) and legacy/ACID PL/SQL procedures in Oracle Database (`PKG_SONIDO_VIVO`).

Without strict internal boundaries, Spring Data annotations, BSON document classes, and `JdbcTemplate` calls leak directly into business services and domain entities, tightly coupling pricing, stock, and validation rules to persistence frameworks and preventing fast offline unit testing.

## 2. Evaluated Alternatives

### Alternative 1: Traditional Layered Architecture (Controller -> Service -> Repository)
- **Pros:** Minimal boilerplate and low initial learning curve with standard Spring Boot `@Service` / `@Repository` stereotypes.
- **Cons (Why Rejected):** Encourages database-driven design where domain objects carry `@Document` or `@Entity` annotations. Domain invariants become entangled with persistence models, making it difficult to isolate Oracle PL/SQL changes from MongoDB schema evolution or run framework-free domain tests.

### Alternative 2: Distributed Microservices (Separate Catalog, Customer, and Sales Services)
- **Pros:** Independent deployment lifecycles and physical process isolation per bounded context.
- **Cons (Why Rejected):** Introduces distributed network latency, dual-write coordination complexity, service discovery overhead, and multi-repository CI/CD friction that is disproportionate for the current team size and academic/production delivery timeline.

### Alternative 3: Hexagonal Modular Monolith (Ports & Adapters + ArchUnit Governance) [CHOSEN]
- **Pros:** Isolates pure Java 21 domain logic (`domain/`) and use-case orchestration (`application/`) behind explicit Inbound and Outbound Ports (`domain/port/out`, `application/port/in`). Keeps deployment as a single cohesive Spring Boot artifact while enabling deterministic offline unit tests and automated ArchUnit boundary enforcement.
- **Cons:** Requires mapping boilerplate between Domain Models, REST DTOs, and persistence representations (`MongoProductDocument`), plus manual Spring `@Bean` wiring in `BeanConfig`.

## 3. Decision and Consequences

We adopt the **Hexagonal Modular Monolith** architecture (`cl.duoc.sonidovivo.domain`, `cl.duoc.sonidovivo.application`, `cl.duoc.sonidovivo.infrastructure`) enforced by ArchUnit fitness tests (`com.tngtech.archunit:archunit-junit5:1.3.0`).

- **Positive Consequences:**
  - `domain/` has zero Spring, Jakarta, JPA, BSON, or MongoDB dependencies and can be tested in milliseconds using pure JUnit 5.
  - `application/` use cases can be unit-tested with Mockito mocks without booting a Spring context or database.
  - Persistence engines (MongoDB and Oracle) are swappable behind Outbound Ports (`ProductRepositoryPort`, `CustomerRepositoryPort`, `DashboardMetricsPort`, `OracleSalesPort`).
- **Negative Consequences / Trade-offs:**
  - Developers must maintain explicit mappers between `MongoProductDocument` / REST records and `Product` domain models.
  - Build pipelines enforce ArchUnit rules strictly; shortcutting layer boundaries breaks `mvnw test`.
