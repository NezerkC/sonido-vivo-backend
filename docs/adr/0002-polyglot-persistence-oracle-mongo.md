# ADR-0002: Polyglot Persistence Strategy with Oracle Database and MongoDB

- **Date:** 2026-11-11
- **Status:** Accepted
- **Deciders:** Governance Architect, Data Engineering Lead

## 1. Context and Problem Statement

Sonido Vivo manages two fundamentally different categories of data and operational requirements:
1. **Musical Instrument & Pro-Audio Catalog:** Heterogeneous technical attributes per instrument family (e.g., guitar wood/pickups vs. studio monitor wattage/impedance), embedded customer reviews, dynamic tag search, real-time inventory valuation aggregations, and atomic stock updates.
2. **Enterprise Sales, Discounts, and Customer Liquidation:** Strict relational ACID transactions and existing Oracle PL/SQL business routines (`PKG_SONIDO_VIVO`: `SP_REGISTRAR_VENTA`, `SP_LIQUIDAR_CLIENTES`, `FN_CALCULAR_DESCUENTO`).

Forcing both workloads into a single database paradigm causes either rigid EAV (Entity-Attribute-Value) anti-patterns in Oracle or weak multi-table relational integrity in MongoDB.

## 2. Evaluated Alternatives

### Alternative 1: Oracle Database Only (Relational + JSON Columns)
- **Pros:** Single database engine, unified ACID transactions across catalog and sales, and simplified backup/operations.
- **Cons (Why Rejected):** Complex polymorphic indexing, nested review array updates (`$push`), and aggregation pipelines for dashboard KPIs are less ergonomic than native MongoDB document operations and fail to leverage the NoSQL document model specified in `CONTRACT_DATABASE_MONGODB.md`.

### Alternative 2: MongoDB Only (Full Migration of PL/SQL Routines)
- **Pros:** Single NoSQL operational stack and uniform Spring Data repository abstractions.
- **Cons (Why Rejected):** Loses native integration with certified Oracle PL/SQL stored procedures (`SP_REGISTRAR_VENTA`, `SP_LIQUIDAR_CLIENTES`, `FN_CALCULAR_DESCUENTO`) and requires re-implementing complex relational financial/liquidation transactions outside an RDBMS.

### Alternative 3: Polyglot Persistence (MongoDB for Catalog/Analytics + Oracle for Transactional PL/SQL) [CHOSEN]
- **Pros:** Matches each workload to the optimal storage engine—MongoDB (`sonido_vivo_nosql`) handles polymorphic catalog documents, atomic `$inc` stock mutations, `$push` reviews, and `$aggregate` KPIs, while Oracle (`FREEPDB1`) executes ACID PL/SQL sales and liquidation procedures via `JdbcTemplate` / `SimpleJdbcCall`.
- **Cons:** Introduces dual-engine operational configuration, requires separate connection pools/health checks, and lacks a single distributed XA transaction across MongoDB and Oracle.

## 3. Decision and Consequences

We adopt **Polyglot Persistence** governed by separate Hexagonal Outbound Ports:
- `ProductRepositoryPort` and `DashboardMetricsPort` are implemented by MongoDB adapters (`MongoProductRepositoryAdapter`, `MongoMetricsRepositoryAdapter`) using `spring.mongodb.uri`.
- `OracleSalesPort` (and relational customer operations as applicable) is implemented by `OraclePlSqlAdapter` using `spring.datasource.*` with `spring.jpa.open-in-view: false` and `spring.jpa.hibernate.ddl-auto: none`.

- **Positive Consequences:**
  - Zero-impedance storage for polymorphic instrument specifications and real-time `$aggregate` dashboard pipelines.
  - Preserves transactional integrity and direct execution of Oracle `PKG_SONIDO_VIVO` PL/SQL routines.
- **Negative Consequences / Trade-offs:**
  - Cross-engine workflows (e.g., decrementing stock in MongoDB alongside registering a sale in Oracle) cannot rely on a single two-phase commit (2PC) and must handle compensating actions or explicit application-level error handling on partial failures.
  - Local integration tests and deployments must provision both MongoDB and Oracle instances (mitigated in unit/ArchUnit test runs via Spring Boot test auto-configuration exclusions).
