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

### Get transaction with currency conversion

`GET /api/transactions/{uniqueIdentifier}?currency=<treasury-currency-code>`

The stored **transaction date** on the record is used together with `currency` when calling the Treasury [rates of exchange](https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange) API. Query parameters sent to Treasury: `fields=country_currency_desc,exchange_rate,record_date` and  

`filter=country_currency_desc:eq:<currency>,record_date:gte:<transaction_date minus 6 months>`.

| Query | Rules |
|-------|--------|
| `currency` | Required. Must match Treasury `country_currency_desc` (for example `Canada-Dollar`, `Euro Zone-Euro`). |

| Outcome | HTTP |
|---------|------|
| Unknown `uniqueIdentifier` | `404 Not Found` |
| No exchange rates in that 6‑month window | `404 Not Found` — message explains no data in the window |
| Success | `200 OK` |

**Success response:**

```json
{
  "uniqueIdentifier": "txn-001",
  "description": "Coffee",
  "transactionDate": "2026-05-14",
  "purchaseAmount": 10.00,
  "exchangeRate": 1.393,
  "convertedAmount": 13.93
}
```

`exchangeRate` is the Treasury rate for the `record_date` **closest** (by calendar days) to the stored transaction date among rates returned in the window. `convertedAmount` is `purchaseAmount * exchangeRate`, rounded to **2** decimal places (half-up).

Configurable base URL (default is the live Treasury API):

```properties
treasury.api.base-url=https://api.fiscaldata.treasury.gov/services/api/fiscal_service/
```

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

The app now uses a file-backed H2 database by default so data persists across restarts. Database files are stored in the project `./data` directory (relative to the project root).

**Default connection settings** (configured in `src/main/resources/application.properties`):

| Setting | Value |
|---------|--------|
| JDBC URL | `jdbc:h2:file:./data/toydemo;DB_CLOSE_DELAY=-1` |
| User Name | `sa` |
| Password | *(leave empty)* |

If you prefer a transient in-memory database for quick testing, change the JDBC URL to `jdbc:h2:mem:toydemo` in `application.properties`. The file-backed database is useful when you want records to survive application restarts.

### Option 1: Web console

1. Start the app (`mvn spring-boot:run`).
2. Open **http://localhost:8080/h2-console**
3. Enter the connection settings from the table above and click **Connect**.

### Option 2: IDE test connection

1. Start the app (`mvn spring-boot:run`).
2. In your IDE (IntelliJ, etc.), add an H2 data source:
  - **URL:** `jdbc:h2:file:./data/toydemo;DB_CLOSE_DELAY=-1`
  - **User:** `sa`
  - **Password:** *(empty)*
  - **Driver:** H2 (`com.h2database:h2` from Maven)
3. Run **Test Connection**.

**Note:** Because the database is file-backed, the same `./data` files will be reused across restarts. If you need a clean slate, stop the app and remove the files under `./data`.
