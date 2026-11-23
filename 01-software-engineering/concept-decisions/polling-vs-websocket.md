# Concept Decision - Polling vs WebSocket

## 1. Problem Context
When a client needs to react to server-side events (new messages, order status changes, live scores, price updates), you need to decide how that data gets from server to client. Polling is a pull model — the client asks repeatedly. WebSocket is a push model — the server sends data when it's ready. The right choice depends on your latency requirements, scale, and operational constraints.

---

## 2. Options

### Option A: Polling (Short Polling / Long Polling)
- **How it works:**
  - **Short polling:** Client sends an HTTP request every N seconds regardless of whether new data exists. Server returns immediately with current state or empty.
  - **Long polling:** Client sends a request; server holds it open until new data is available or a timeout occurs, then responds. Client immediately re-issues the request.
- **Pros:**
  - Works over standard HTTP — no special infrastructure needed
  - Stateless on the server — each request is independent, load balancers work normally
  - Easy to implement, debug, and monitor with existing HTTP tooling
  - Degrades gracefully behind proxies and firewalls
- **Cons:**
  - Short polling wastes resources when data changes infrequently (most requests return nothing)
  - Long polling still has per-request overhead (connection teardown and re-establishment)
  - Latency is bounded by the poll interval, not true real-time
  - At high scale, polling creates thundering herd — all clients polling simultaneously spike server load

### Option B: WebSocket
- **How it works:** A full-duplex persistent TCP connection is established via an HTTP Upgrade handshake. Server and client can both send frames at any time over the single connection.
- **Pros:**
  - True push — server sends data the moment it's available, sub-100ms latency achievable
  - Low overhead per message — no HTTP headers re-sent on every event
  - Bidirectional — client can send messages without opening a new connection
  - Efficient at scale when events are frequent (fewer connections than repeated HTTP)
- **Cons:**
  - Stateful connections — sticky sessions or a pub/sub layer (Redis) required to route messages across horizontally scaled servers
  - Proxies, firewalls, and some CDNs don't support WebSocket or have aggressive idle timeouts
  - More complex to implement: connection lifecycle, reconnect logic, heartbeats, backpressure
  - Harder to debug and monitor than plain HTTP requests

---

## 3. Key Differences
- **Latency:** WebSocket delivers sub-second push; short polling is limited by interval (often 1–5s); long polling can approach real-time but with connection overhead
- **Scalability:** Polling is stateless and scales horizontally without coordination; WebSocket requires a message broker (Redis Pub/Sub, Kafka) to fan out across servers
- **Simplicity:** Polling is trivially simple; WebSocket requires managing connection state, reconnection, and broadcast infrastructure
- **Resource usage:** Polling wastes bandwidth/compute on empty responses; WebSocket wastes connections when events are rare
- **Infrastructure compatibility:** Polling works everywhere; WebSocket can fail behind restrictive proxies or load balancers not configured for long-lived connections

---

## 4. Real-world Trade-offs
- **When to choose Polling:**
  - Low-frequency updates (every 30–60 seconds is acceptable) — email inbox, dashboard metrics, background job status
  - Environments where WebSocket support is uncertain (enterprise proxies, embedded clients)
  - Small scale where simplicity matters more than latency
  - When you need HTTP caching for the polled endpoint
- **When to choose WebSocket:**
  - True real-time requirements: chat, collaborative editing, live trading data, multiplayer games
  - High-frequency events where polling would create excessive load
  - Bidirectional communication where the client also sends frequent events
  - Large-scale notification systems where push is more efficient than thundering herd polling

---

## 5. Production Scenarios

**Slack:** Uses WebSocket for message delivery to connected clients. A message sent in a channel must appear in milliseconds across all connected members. Long-polling at Slack's scale would be catastrophic in terms of server load.

**GitHub (Pull Request checks):** Uses short polling in the UI — check statuses are fetched every 10–15 seconds. Events are infrequent enough that a WebSocket connection per tab would waste resources with no latency benefit.

**Robinhood (Stock Prices):** Uses WebSocket to stream live price quotes to the client. A stock moving from $150.00 to $150.05 in milliseconds cannot wait for a 5-second poll interval. WebSocket provides sub-second delivery.

**Intercom (Chat Widget):** Uses long polling as a fallback when WebSocket connections are blocked by corporate proxies. The widget detects WebSocket failures and silently falls back to long polling.

**Uber (Driver Location):** Drivers broadcast GPS coordinates via WebSocket every 4 seconds. The server fans this out to affected rider clients. Polling would require each rider app to repeatedly ask "where is my driver?" — multiplied across millions of rides, it would be unsustainable.

---

## 6. Common Mistakes
- Using short polling with a 1-second interval "because it's simpler" without realizing it creates N requests/second per connected client — a load you'd never tolerate with WebSocket
- Building WebSocket infrastructure without a pub/sub layer (Redis, Kafka) — when you deploy a second server instance, messages sent to server A never reach clients connected to server B
- Forgetting WebSocket reconnection logic — mobile clients lose connectivity constantly; without auto-reconnect and message replay, clients silently go out of sync
- Not implementing heartbeats on WebSocket connections — idle connections are killed by load balancers and proxies after 30–60 seconds without data
- Using WebSocket for infrequent, unidirectional updates (e.g., a nightly report) where SSE (Server-Sent Events) or polling would be far simpler

---

## 7. Final Decision Rule (IMPORTANT)

👉 If events are frequent, latency-sensitive, or bidirectional → use **WebSocket**  
👉 If events are infrequent, unidirectional, or WebSocket infrastructure is not available → use **Polling** (long polling if latency matters)  
👉 If events are infrequent but need sub-second delivery and are server-to-client only → consider **SSE (Server-Sent Events)** as a simpler middle ground  

---

## 8. Lesson Learned
Polling is the right default for most features — simple, stateless, and easy to debug. But when users need to see something the moment it happens, polling's latency and resource cost become unacceptable. The WebSocket infrastructure cost is real — sticky sessions, pub/sub, reconnection handling. Don't pay that cost unless you have a genuine real-time requirement. When you do need real-time, design for reconnection and message replay from day one — connections drop constantly in production.
