# MoneyTransfer API

## Table of Contents
- [Introduction](#introduction)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
  - [Presentation Layer (Controller)](#presentation-layer-controller)
  - [Service Layer](#service-layer)
  - [Repository Layer](#repository-layer)
  - [Entity Layer](#entity-layer)
  - [Exception Handling](#exception-handling)
- [Database](#database)
  - [Accessing the H2 Console](#accessing-the-h2-console)
  - [Data Model](#data-model)
    - [Account Entity](#account-entity)
    - [Transaction Entity](#transaction-entity)
- [Testing](#testing)
- [Deployment](#deployment)

## Introduction


## Technologies
* Java
* Spring Boot
* Maven
* H2 (Embedded Database)

## Getting Started



## API Documentation
You can interact with the Money Transfer API by sending HTTP requests to the provided endpoints. Here's an example of how to make a request using curl:

````bash
curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00", \"amount\": "30.00"}" "http://localhost:8080/transferMoney"
````

This curl command is used to initiate a transfer of 30.00 EUR (default currency) from account with ID 1 to account with ID 2, using the /transferMoney endpoint of the MoneyTransfer API.

## Architecture
### Presentation Layer (Controller):

### Service Layer:

### Repository Layer:

### Entity Layer:

### Exception Handling:
Defines exception classes for different error scenarios.

## Database
### Accessing the H2 Console
To access the H2 console for the MoneyTransfer API, follow these steps:
1. Start the MoneyTransfer API application.
2. Open a web browser.
3. Enter the following URL: "http://localhost:8080/h2-console".
4. In the login page of the H2 console, configure the following settings:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (leave it empty)
5. Click the "Connect" button to log in to the H2 console.

Once you are logged in to the H2 console, you can view and interact with the database used by the MoneyTransfer API.

### Data Model
In this section, you will find an overview of the entities (or tables) used in the application's data model.
#### Account Entity
The Account entity represents a bank account and has the following attributes:

| Field     | Description                    |
|-----------|--------------------------------|
| accountId        | Unique identifier of the account |
| balance           | Decimal number representing the account balance |
| currency          | Currency of the account (e.g., "GBP") |
| createdAt         | Date and time when the account was created |

##### Transaction Entity
The Transaction entity represents a financial transaction between two accounts and includes the following attributes:

| Field            | Description                          |
|------------------|--------------------------------------|
| transactionId   | Unique identifier of the transaction |
| sourceAccountId  | ID of the account sending the funds   |
| targetAccountId  | ID of the account receiving the funds |
| amount           | Amount being transferred              |
| currency         | Currency of the transaction           |


## Testing
The `ApplicationTests.java` file located in the `service` package at `src/test/java/service` contains mock tests that validate the fulfillment of all acceptance criteria (ACs) of the MoneyTransfer API. These tests simulate the behavior of the service layer using mock objects and verify the expected functionality.

The mock tests cover the following ACs:
- AC 1: Happy path for money transfer between two accounts
- AC 2: Insufficient balance to process money transfer
- AC 3: Transfer between the same account
- AC 4: One or more of the accounts does not exist

## Deployment
