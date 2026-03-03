# Mendel – Transactions Service

A RESTful Spring Boot service that stores financial transactions in memory and exposes endpoints to create them, query by type, and compute recursive sums across linked transaction trees.

---

## Requirements

| Tool | Minimum version |
|------|----------------|
| Java | 21 |
| Maven | 3.9 (or use the included `./mvnw` wrapper) |
| Docker | 24 (only for containerised run) |

---

## Running the application

### Option 1 – Maven (local)

```bash
./mvnw spring-boot:run
```

The service starts on **http://localhost:8080**.

### Option 2 – Docker Compose

```bash
docker compose up --build
```

The service starts on **http://localhost:8080**.

### Option 3 – Docker (manual)

```bash
docker build -t mendel-transactions .
docker run -p 8080:8080 mendel-transactions
```

---

## Running the tests

```bash
./mvnw test
```

The test suite includes:

| Test class | Scope |
|---|---|
| `InMemoryTransactionsRepositoryTest` | Unit – repository logic |
| `TransactionsServiceTest` | Unit – business rules (mocked repository) |
| `TransactionsControllerTest` | Unit – HTTP layer (mocked service, `@WebMvcTest`) |
| `TransactionsIntegrationTest` | Integration – full context, in-memory store |

---

## API reference

### `PUT /transactions/{transaction_id}`

Creates a new transaction.

**Path parameter**

| Name | Type | Description |
|---|---|---|
| `transaction_id` | `long` | Unique identifier for the new transaction |

**Request body**

```json
{
  "amount": 5000.0,
  "type": "cars",
  "parent_id": 10
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `amount` | `number` | ✅ | Positive monetary amount (up to 18 integer digits, 2 decimal places) |
| `type` | `string` | ✅ | Non-blank label that groups transactions |
| `parent_id` | `long` | ❌ | Id of an existing parent transaction |

**Responses**

| Status | Body | When |
|---|---|---|
| `201 Created` | `{ "status": "ok" }` | Transaction persisted |
| `400 Bad Request` | `{ "message": "..." }` | Validation failure, id already in use, or parent not found |

---

### `GET /transactions/types/{type}`

Returns all transaction ids that belong to the given type.

**Response** `200 OK`

```json
[10, 11]
```

Returns an empty array `[]` when no transactions match.

---

### `GET /transactions/sum/{transaction_id}`

Returns the sum of the transaction's own amount plus the amounts of all its transitive descendants (linked via `parent_id`).

**Response** `200 OK`

```json
{ "sum": 20000.00 }
```

| Status | When |
|---|---|
| `200 OK` | Transaction found |
| `404 Not Found` | No transaction exists with that id |

---

## Example walkthrough

```bash
# Create a root transaction
curl -X PUT http://localhost:8080/transactions/10 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000, "type": "cars"}'
# -> {"status":"ok"}

# Create a child
curl -X PUT http://localhost:8080/transactions/11 \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "type": "shopping", "parent_id": 10}'

# Create a grandchild
curl -X PUT http://localhost:8080/transactions/12 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000, "type": "shopping", "parent_id": 11}'

# Find all "cars" transactions
curl http://localhost:8080/transactions/types/cars
# -> [10]

# Sum starting from transaction 10 (5000 + 10000 + 5000)
curl http://localhost:8080/transactions/sum/10
# -> {"sum":20000.0}

# Sum starting from transaction 11 (10000 + 5000)
curl http://localhost:8080/transactions/sum/11
# -> {"sum":15000.0}
```

---

## Interactive API docs (Swagger UI)

While the service is running, open:

```
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI spec is also available at `http://localhost:8080/v3/api-docs`.

---

## Architecture

```
TransactionsController        ← HTTP layer (routing, request/response mapping)
        |
        V
TransactionsService           ← Business rules interface
TransactionsServiceImpl       ← Validates ids, delegates persistence
        |
        V
TransactionsRepository        ← Persistence interface
InMemoryTransactionsRepository← ConcurrentHashMap-backed, thread-safe implementation
```

Key design decisions:
- **PUT is treated as POST** - the challenge requirements clearly state that put is used to create a `new` transaction. Leading me to think it should be managed as if it was a post endpoint. Examples for different behaviour are missing.
- **In-memory, no SQL** - three indexes (`byId`, `byType`, `childrenByParent`) give O(1) look-ups for all query paths.
- **Thread safety** - `ConcurrentHashMap` for reads; `TransactionsServiceImpl.create` is `synchronized` so the check-then-act sequence (does this id exist? -> write) is atomic. The lock lives in the service because that is where the business invariant is expressed, keeping the repository a simple data store.
- **Iterative DFS for sum** - `sum` traverses the transaction tree using an explicit `Deque` stack instead of recursion, avoiding stack-overflow on deep trees.
- **`BigDecimal` for monetary values** - avoids the floating-point representation errors of `double` (e.g. `0.1 + 0.2 != 0.3` in IEEE 754).
- **Global exception handler** - `GlobalExceptionHandler` maps domain exceptions to consistent HTTP responses, keeping controllers free of try/catch.
- **`@Digits` + `@DecimalMin` validation** - `amount` is validated at the HTTP boundary before it reaches the service.
- **Bean validation** - `@Positive` and `@NotBlank` on the request DTO catch bad input at the HTTP boundary before it reaches the service layer.
- **SOLID principles** - controller, service, and repository are behind interfaces; each class has a single, well-defined responsibility.
