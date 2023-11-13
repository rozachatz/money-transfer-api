# MoneyTransfer API

## Table of Contents
- [Introduction](#introduction)
- [Acceptance Criteria](#acceptance-criteria)
- [Requests](#requests)
- [Idempotency](#idempotency)
- [API Documentation](#api-documentation)
- [Data Model](#data-model)
- [Architecture](#architecture)
- [Testing](#testing)
- [Docker](#docker)

## Introduction
This project includes a simple REST microservice for handling financial transactions with SpringBoot.

### Acceptance Criteria
- AC 1: Happy path
- AC 2: Insufficient balance to process money transfer
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist

## Requests
````bash
curl -X POST -H "Content-Type: application/json" -d "{\"sourceAccountId\": \"79360a7e-5249-4822-b3fe-dabfd40b8737\", \"targetAccountId\": \"ef30b8d1-6c5d-4187-b2c4-ab3c640d1b18\", \"amount\": 30.00}" "http://localhost:8080/api/transfer/optimistic"
````
A POST request to the endpoint "http://localhost:8080/api/transfer" initiates a transfer between two accounts. Option for pessimistic locking is also available by the endpoint "http://localhost:8080/api/transfer/pessimistic".

Caching is also supported for GET requests to endpoints that fetch many results, e.g. "http://localhost:8080/api/transactions/{minAmount}/{maxAmount}".

### Idempotency
This microservice also supports idempotent POST requests via the endpoint: "http://localhost:8080/api/transfer/{transferRequestId}".

## API Documentation
Visit "http://localhost:8080/api/swagger-ui/index.html" to explore the endpoints and try-out the app :)

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
The TransactionRequest entity stores information that is crucial for providing idempotent behavior for POST requests (regarding financial Transactions).

| Field                 | Description                                          |
|-----------------------|------------------------------------------------------|
| transactionRequest_id | Unique identifier of the TransactionRequest          |
| transaction_id        | ID of the successful Transaction                     |
| errorMessage          | Error message                                        |
| requestStatus         | Status of the TransactionRequest                     |
| jsonBody              | String representation of the jsonBody of the request |

## Architecture
### Controller
**Controller**: Exposes the endpoints of the application, processes the HTTP requests and sends the appropriate response to the client.

### Data Transfer Objects (Dtos)
Container classes, read-only purposes.

### Service
#### TransactionRequestService
Business Logic for executing a request for a financial transaction.

#### TransactionService
Business logic for performing a financial transactions between two accounts.

### Repository
Using JPA.

### Entity
- TransactionRequest
- Transaction
- Account

### Exceptions
- Custom exceptions
- @ControllerAdvice for returning the appropriate HTTP status

## Testing
At the moment, service integration tests and a repo unit are provided. More to come, as the app progresses! Integration tests use H2 embedded DB.

## Docker
The app and (Postgres) db are now dockerized! <3 Let the magic happen by executing the following command:
````bash
docker compose up -- build
````



