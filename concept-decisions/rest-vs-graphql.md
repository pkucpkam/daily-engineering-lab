# Concept Decision - REST vs GraphQL

## 1. Problem Context
When designing an API layer between backend services and clients (web, mobile, third-party), teams must decide how to expose data and operations. REST has been the default for over a decade. GraphQL emerged as a response to specific pain points at scale — primarily over-fetching and under-fetching — but introduces its own operational complexity.

---

## 2. Options

### Option A: REST
- **How it works:** Resources are exposed as URLs. HTTP verbs (GET, POST, PUT, DELETE) map to operations. Each endpoint returns a fixed response shape defined by the server.
- **Pros:**
  - Simple mental model — URLs map to resources, verbs map to actions
  - HTTP caching works natively (GET responses can be cached by CDN/browser)
  - Easy to debug with standard tools (curl, Postman, browser network tab)
  - Mature ecosystem for auth, rate limiting, versioning, documentation (OpenAPI/Swagger)
- **Cons:**
  - Over-fetching: endpoints return fields the client doesn't need (wasted bandwidth)
  - Under-fetching: a single view often requires multiple round-trips to different endpoints
  - API versioning becomes painful as clients diverge in requirements
  - Backend must anticipate every client's data needs up front

### Option B: GraphQL
- **How it works:** Clients send a query specifying exactly which fields they need. A single endpoint (`/graphql`) handles all requests. The server resolves fields via a typed schema.
- **Pros:**
  - Clients fetch exactly what they need — eliminates over-fetching
  - Reduces round-trips — a single query can fetch deeply nested, related data
  - Self-documenting via schema introspection
  - Strongly typed schema serves as a contract between frontend and backend
- **Cons:**
  - N+1 query problem — naive resolvers trigger one DB query per nested object (requires DataLoader or batching)
  - HTTP caching is broken by default (all queries are POST to one endpoint)
  - Complex queries from clients can be expensive — requires query depth/cost limiting
  - Steeper learning curve and more complex tooling compared to REST
  - Error handling is non-standard (HTTP 200 with errors in the response body)

---

## 3. Key Differences
- **Flexibility:** GraphQL gives clients full control over shape; REST gives server control
- **Over/Under-fetching:** GraphQL eliminates both; REST requires multiple endpoints or custom wrappers
- **Caching:** REST caches naturally at HTTP level; GraphQL requires application-level caching (e.g., persisted queries, Apollo cache)
- **Complexity:** REST is operationally simpler; GraphQL pushes complexity to schema design and resolver performance
- **Discoverability:** GraphQL is self-documenting via introspection; REST requires maintained OpenAPI specs

---

## 4. Real-world Trade-offs
- **When to choose REST:**
  - Public APIs consumed by unknown third parties (simpler contract, easier to document)
  - APIs where HTTP caching is critical (CDN-cacheable static-ish data)
  - Simple CRUD services with stable, uniform response shapes
  - Small teams where GraphQL's operational overhead isn't justified
- **When to choose GraphQL:**
  - Products with multiple client types (web, iOS, Android) each needing different data shapes from the same backend
  - BFF (Backend for Frontend) layer aggregating multiple microservices
  - Rapid product iteration where frontend teams frequently change data requirements
  - Large engineering orgs where schema ownership and type safety across teams matters

---

## 5. Production Scenarios

**Facebook:** GraphQL was invented at Facebook to solve the problem of different mobile clients needing different data shapes from the same API. A News Feed post on desktop shows different fields than on a 2G mobile connection.

**GitHub API v4:** Migrated from REST (v3) to GraphQL to let integrators fetch exactly the repository, PR, or issue fields they need in a single query — reducing over-fetching significantly for power users.

**Shopify:** Uses GraphQL for its Storefront and Admin APIs. Merchants and app developers can query exactly the product, order, or customer fields needed, reducing data transfer on bandwidth-constrained storefronts.

**Netflix:** Uses REST for most microservice-to-microservice communication (simple, cacheable, well-understood). Uses GraphQL at the BFF layer to aggregate data from many services for the UI tier.

---

## 6. Common Mistakes
- Using GraphQL without DataLoader — the N+1 problem will silently destroy DB performance under load
- Exposing GraphQL directly to the public internet without query depth limiting or cost analysis — malicious clients can craft queries that join thousands of objects
- Ignoring HTTP caching needs — if your API serves heavily read data, GraphQL's broken caching is a real cost
- Designing GraphQL mutations like RPC calls instead of following the schema-first resource model — leads to an unmaintainable schema
- Choosing GraphQL for a simple CRUD API with one client — the overhead is not justified and adds operational burden

---

## 7. Final Decision Rule (IMPORTANT)

👉 If you have multiple clients with diverging data needs, or a BFF aggregating microservices → use **GraphQL**  
👉 If you have a public API, need HTTP caching, or have a simple resource model → use **REST**  
👉 If you're a small team building a new product → start with **REST**, introduce GraphQL at the BFF layer when client diversity demands it  

---

## 8. Lesson Learned
GraphQL solves real problems — over-fetching and under-fetching are genuinely painful at scale. But it shifts complexity from the URL surface area to the schema and resolver layer. The N+1 problem is non-obvious and expensive to fix reactively. The right default is REST for most services; adopt GraphQL deliberately where client flexibility is the actual bottleneck.
