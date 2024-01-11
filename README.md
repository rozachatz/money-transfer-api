# MoneyTransfer API 💸 💸 

## Table of Contents
- [Introduction](#introduction)
- [Documentation](#documentation)
- [Data Model](#data-model)
- [Architecture](#architecture)
- [Testing](#testing)
- [Docker](#docker)

## Introduction 
This project includes a SpringBoot application for handling financial transactions 💸 .  
Update: Currency exchange is now performed to the transferred amount (if necessary) by fetching the latest exchange rates from "https://freecurrencyapi.com/"! 💱

## Documentation
Powered by Swagger. Visit "http://localhost:8080/api/swagger-ui/index.html" to explore endpoints and try-out the app! 😊

## Data Model
### Account
The Account entity represents a bank account with the following properties:

| Field     | Description                    |
|-----------|--------------------------------|
| account_id        | Unique identifier of the account |
| balance           | Decimal number representing the account balance |
| currency          | Currency of the account (e.g., "GBP") |
| createdAt         | Date and time when the account was created |

### Transaction
The Transaction entity represents a financial transaction between two accounts and includes the following properties:

| Field            | Description                          |
|------------------|--------------------------------------|
| transaction_id   | Unique identifier of the transaction |
| source_account_id  | ID of the account sending the funds   |
| target_account_id  | ID of the account receiving the funds |
| amount           | Amount being transferred              |
| currency         | Currency of the transaction           |

### TransactionRequest
The TransactionRequest entity provides idempotent behavior for POST transfer requests.

| Field                 | Description                                          |
|-----------------------|------------------------------------------------------|
| transactionRequest_id | Unique identifier of the TransactionRequest          |
| transaction_id        | ID of the successful Transaction                     |
| errorMessage          | Error message                                        |
| requestStatus         | Status of the TransactionRequest                     |
| jsonBody              | String representation of the jsonBody of the request |

## Architecture
### Controller
- TransactionController

### Data Transfer Objects (Dtos)
Container classes, read-only purposes.

### Services
#### TransactionRequestService
Business Logic for executing a request for a financial transaction.

#### TransactionService
Business logic for performing a financial transactions between two accounts.

#### CurrencyExchangeService
Business logic for performing currency exchange.

### Entities
- TransactionRequest
- Transaction
- Account

### Repositories
JPA repository for each entity.

### Exceptions
- Custom exceptions
- GlobalAPIExceptionHandler returns the appropriate HTTP status for each custom exception.
  
## Testing
At the moment, integration tests for services are provided. More to come, as the app progresses! 
*Note: Integration tests use H2 embedded db.*

### Acceptance Criteria
- AC 1: Happy path
- AC 2: Insufficient balance
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist
  
## Docker
The app and (Postgres) db are now dockerized! ❤️ Let the magic happen by executing the following commands:

**First-time setup:**
````bash
docker compose up db --build
docker compose up app --build
````
Note: allow database setup to complete before starting the app container.

**Subsequent runs:**
````bash
docker compose up
````



