# MoneyTransfer API 💸 💸 

## Table of Contents
- [Introduction](#introduction)
- [Documentation](#documentation)
- [Data Model](#data-model)
- [Architecture](#architecture)
- [Testing](#testing)
- [Docker Manual](#docker-manual)

## Introduction 
This project includes a SpringBoot application for handling financial transactions 💸 .  

## Documentation
Powered by Swagger. Power-up the application (preferably with [Docker](#docker)) and visit "http://localhost:8080/api/swagger-ui/index.html" to explore endpoints, read API documentation and try-out the app! 😊

## Data Model
### Account
The Account entity represents a bank account with the following properties:

| Field      | Description                                     |
|------------|-------------------------------------------------|
| account_id | Unique identifier of the account                |
| owner_name | Name of the account owner                       |
| balance    | Decimal number representing the account balance |
| currency   | Currency of the account (e.g., "GBP")           |
| createdAt  | Date and time when the account was created      |

### Transaction
The Transaction entity represents a financial transaction between two accounts and includes the following properties:

| Field             | Description                          |
|-------------------|--------------------------------------|
| id                | Unique identifier of the transaction |
| source_account_id | ID of the account sending the funds  |
| target_account_id | ID of the account receiving the funds |
| amount            | Amount being transferred             |
| currency          | currency of the transaction          |
| hashedPayload     | hash value of the payload            |
| status            | status of the Transaction            |
| message           | detailed information for the  status |

## Architecture
### Controller
MoneyTransferAPIController

### Data Transfer Objects
Container classes, read-only purposes.

### Services
#### TransactionManagementService
Manages all transactions within the system. It is responsible for processing new transaction requests and for retrieving transaction resources. To enhance the reliability of the app, all transaction requests have been designed to be *idempotent*.

#### MoneyTransferService
This microservice is invoked by the TransactionManagementService to facilitate a money transfer between two accounts and persist the new transaction into the system.

#### CurrencyExchangeService
Performs currency exchange, if necessary, from the source account's currency to the target account's currency by retrieving the latest exchange rates from "https://freecurrencyapi.com/"! 💱

#### AccountManagementService
Manages all accounts within the system.

### Entities
- Transaction
- Account

### Repositories
In this project, JPA repositories are used.

### Exceptions
@ControllerAdvice for handling all custom exceptions of the application.
  
## Testing
At the moment, service integration tests using H2 embedded db are provided.

### Acceptance Criteria
- AC 1: Happy path
- AC 2: Insufficient balance
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist
  
## Docker Manual
The application and database are now dockerized! Let the magic ✨ happen by following the instructions.

First package the application into a JAR by executing:
````bash
mvn clean package
````
Then, allow the database container to finish initialization:
````bash
docker compose up db --build
````
Now you can power up the application *anytime* by just executing:
````bash
docker compose up --build
````
Note: skip the '--build' for subsequent runs.

