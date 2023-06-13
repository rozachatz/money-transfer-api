# MoneyTransfer API

## Table of Contents
- [Introduction](#introduction)
- [Technologies](#technologies)
- [API Documentation](#api-documentation)
- [Database](#database)
    - [Data Model](#data-model)
      - [Account Entity](#account-entity)
      - [Transaction Entity](#transaction-entity)
    - [Accessing the H2 Console](#accessing-the-h2-console)
- [Architecture](#architecture)
  - [Presentation Layer](#presentation-layer)
    - [Controller](#controller)
    - [Data Transfer Object (DTO)](#data-transfer-object-dto)
  - [Service Layer](#service-layer)
  - [Repository Layer](#repository-layer)
  - [Entity Layer](#entity-layer)
  - [Exception Package](#exception-package)
- [Testing](#testing)
- [Future Containerization](#future-containerization)

## Introduction
This project is a simple microservice that handles financial transactions between bank accounts. In this README, you will find information about the project architecture, testing and other relevant details.

## Technologies
* Java
* Spring Boot
* Maven
* H2 (Embedded Database)

## API Documentation
You can interact with the Money Transfer API by sending HTTP requests to the provided endpoints. Here's an example of how to make a request using curl:

````bash
curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00"}" "http://localhost:8080/api/transfer/pessimistic"
````
````bash
curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00"}" "http://localhost:8080/api/transfer/optimistic"
````
Each of these curl commands is used to initiate a transfer of 30.00 EUR (default currency) from account with ID 1 to account with ID 2 with optimistic ("/api/transfer/optimistic") pessimistic ("/api/transfer/pessimistic") locking.
````bash
curl -X GET -H "Content-Type: application/json"  "http://localhost:8080/api/transfer/{transactionId}"
````
The endpoint "\api\transfer\{transactionId}" retrieves the details of the transfer with transaction id equal to {transactionId}.
## Database
### Data Model
In this section, you will find an overview of the entities (or tables) used in the application's data model.
#### Account Entity
The Account entity represents a bank account and has the following attributes:

| Field     | Description                    |
|-----------|--------------------------------|
| account_id        | Unique identifier of the account |
| balance           | Decimal number representing the account balance |
| currency          | Currency of the account (e.g., "GBP") |
| createdAt         | Date and time when the account was created |

##### Transaction Entity
The Transaction entity represents a financial transaction between two accounts and includes the following attributes:

| Field            | Description                          |
|------------------|--------------------------------------|
| transaction_id   | Unique identifier of the transaction |
| source_account_id  | ID of the account sending the funds   |
| target_account_id  | ID of the account receiving the funds |
| amount           | Amount being transferred              |
| currency         | Currency of the transaction           |

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

## Architecture
### Presentation Layer:
- **Controller**: Exposes the API, which acts as an intermediary between the server and the client. It processes the requests and returns the appropriate response.
- **Data Transfer Object (DTO)**: Container that represents the data that will be transferred between the client and the server.

### Service Layer:
- Operates on the data transferred between the client and the server and performs operations according to the business logic of the application.

### Repository Layer:
- Provides access to the database by extending the JPARepository.
- Performs custom queries (@Query) and CRUD (Create, Read, Update, Delete) operations, which are predefined methods like save(), findById(). 

### Entity Layer:
- Represents the data model of the application.
- Defines the structure and relationships between entities (tables) in the database.

### Exception Package:
- Global API Exception Handler: uses @ControllerAdvice to handle all different exceptions of the application.
- Uses hierarchy to define exception classes for handling different error scenarios.

## Testing
The `ApplicationTests.java` file located at `src/test/java` contains mock tests that validate the fulfillment of all acceptance criteria (ACs) of the MoneyTransfer API. These tests simulate the behavior of the service layer using mock objects and verify the expected functionality.

The mock tests cover the following ACs:
- AC 1: Happy path for money transfer between two accounts
- AC 2: Insufficient balance to process money transfer
- AC 3: Transfer between the same account
- AC 4: One or more of the accounts do not exist

## Future Containerization
In future versions, a Docker container will be provided for easier installation and running of the application in different environments. Stay tuned for updates on containerization.