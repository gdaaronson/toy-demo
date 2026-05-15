# toy-demo

Simple Spring Boot REST API with an embedded H2 database.

## Prerequisites

- **Java 17** or newer (`java -version`)
- **Apache Maven** (`mvn -version`)

## Start the app (CLI)

From the project root:

```powershell
cd <project-root>
mvn spring-boot:run
```

The server starts on **http://localhost:8080**. Stop it with `Ctrl+C`.

On first run, Maven downloads dependencies; that can take a minute.

## Run tests (CLI)

From the project root:

```powershell
cd <project-root>
mvn test
```

Run a single test class:

```powershell
mvn test -Dtest=TransactionControllerTest
```

## API

### Create transaction

`POST /api/transactions`

```json
{
  "description": "Coffee",
  "transactionDate": "2026-05-14",
  "purchaseAmount": 4.50,
  "uniqueIdentifier": "txn-001"
}
```

| Field | Rules |
|-------|--------|
| `description` | Required, max 50 characters |
| `transactionDate` | `yyyy-MM-dd`, today or earlier (not in the future) |
| `purchaseAmount` | Positive; rounded to nearest cent on save (half-up, e.g. `10.005` → `10.01`) |
| `uniqueIdentifier` | Required, must be unique |

**Success:** `201 Created` with the saved record.

Each row gets an auto-incrementing `BIGINT` primary key (`id`) in the database. That `id` is internal only and is **not** returned by the API.

**API response:**

```json
{
  "uniqueIdentifier": "txn-001",
  "description": "Coffee",
  "transactionDate": "2026-05-14",
  "purchaseAmount": 4.50
}
```

**Database row** (e.g. via H2 console — includes `id`):

| id | unique_identifier | description | transaction_date | purchase_amount |
|----|-------------------|-------------|------------------|-----------------|
| 1  | txn-001           | Coffee      | 2026-05-14       | 4.50            |

**Validation error:** `400 Bad Request` with field-level errors.

### Example requests

Use a `transactionDate` of today or earlier (`yyyy-MM-dd`).

#### PowerShell (recommended)

`curl` in PowerShell is an alias for `Invoke-WebRequest`, which does not accept `-d` the same way. Use `Invoke-RestMethod` instead:

```powershell
$body = @{
    description      = "Coffee"
    transactionDate  = (Get-Date).AddDays(-1).ToString("yyyy-MM-dd")
    purchaseAmount   = 4.50
    uniqueIdentifier = "txn-001"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:8080/api/transactions -Method Post -ContentType "application/json" -Body $body
```

#### curl.exe (CMD, Commander, or PowerShell)

On Windows, call **`curl.exe`** (not `curl`) so you get real curl, not the PowerShell alias:

```text
curl.exe -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d "{\"description\":\"Coffee\",\"transactionDate\":\"2026-05-14\",\"purchaseAmount\":4.50,\"uniqueIdentifier\":\"txn-001\"}"
```

Or put the JSON in a file (works in any shell):

```json
{
  "description": "Coffee",
  "transactionDate": "2026-05-14",
  "purchaseAmount": 4.50,
  "uniqueIdentifier": "txn-001"
}
```

Save as `request.json` in the project root, then:

```text
curl.exe -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d @request.json
```

## H2 database access (optional)

The app uses an **in-memory** database. It only exists while `mvn spring-boot:run` is running.

**Connection settings** (use for either option below):

| Setting | Value |
|---------|--------|
| JDBC URL | `jdbc:h2:mem:toydemo` |
| User Name | `sa` |
| Password | *(leave empty)* |

Do **not** use a file path such as `jdbc:h2:C:/Users/.../test` — that looks for a database file on disk and will fail with “Database not found”.

### Option 1: Web console

1. Start the app (`mvn spring-boot:run`).
2. Open **http://localhost:8080/h2-console**
3. Enter the connection settings from the table above and click **Connect**.

### Option 2: IDE test connection

1. Start the app (`mvn spring-boot:run`).
2. In your IDE (IntelliJ, etc.), add an H2 data source:
   - **URL:** `jdbc:h2:mem:toydemo`
   - **User:** `sa`
   - **Password:** *(empty)*
   - **Driver:** H2 (`com.h2database:h2` from Maven)
3. Run **Test Connection**.

**Note:** In-memory H2 is tied to the JVM that started the app. Some IDE tools use a separate JVM and may show an empty database even with the correct URL. If that happens, try the web console (Option 1), which connects through the running app.
