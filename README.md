# MoneyTransfer API

## Table of Contents
- [Introduction](#introduction)
- [Technologies](#technologies)
- [Database](#database)
- [Architecture](#architecture)
- [Testing](#testing)
- [Future Containerization](#future-containerization)

## Introduction
This project includes a simple microservice for handling financial transactions. The usage of a RESTFull API enables the user to initiate a transfer between two accounts or retrieve a resource by sending an HTTP request to the appropriate endpoint.

## Technologies
* Java
* Spring Boot
* Maven
* H2 (Embedded Database)

## Usage
You can interact with the Money Transfer API by sending POST/GET HTTP requests to the provided endpoints.
### POST Requests
````bash
curl -X POST -H "Content-Type: application/json" -d "{\"sourceAccountId\": \"79360a7e-5249-4822-b3fe-dabfd40b8737\", \"targetAccountId\": \"ef30b8d1-6c5d-4187-b2c4-ab3c640d1b18\", \"amount\": 30.00}" "http://localhost:8080/api/transfer"
````
A POST request to the endpoint "http://localhost:8080/api/transfer" initiates a transfer between two accounts with amount and ids as specified in the .json payload.
Option for optimistic and pessimistic type of locking is also available by sending a POST request to the endpoints "http://localhost:8080/api/transfer/optimistic" and "http://localhost:8080/api/transfer/pessimistic", respectively.
 
### GET Requests 
The endpoint "http://localhost:8080/api/transfer/{transactionId}" is used to retrieve information for a transaction with id equal to {transactionId} (type: UUID).

## Database
### Account
The Account entity represents a bank account and has the following attributes:

| Field     | Description                    |
|-----------|--------------------------------|
| account_id        | Unique identifier of the account |
| balance           | Decimal number representing the account balance |
| currency          | Currency of the account (e.g., "GBP") |
| createdAt         | Date and time when the account was created |

### Transaction
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
### Presentation Layer
**Controller**: Exposes the endpoints of the application, processes the HTTP requests and sends the appropriate response to the client.

### Data Transfer Objects (Dtos)
Classes that are used only as containers e.g., TransferRequestDto.java holds the data that the client sends to the server when requesting a new transfer.

### Service
Contains the business logic of the application.

### JPA Repositories
- Provide direct access to the database, as well as CRUD operations.
- Allow the execution of custom queries.

### Entities
- Represents the data model of the application.
- Defines the structure and relationships between entities (tables) in the database.

### Exception Package
- GlobalAPIExceptionHandler.java: @ControllerAdvice enables handling of all different exceptions in the application.
- Exceptions can be grouped using hierarchy (i.e., all exceptions extend MoneyTransferException.java).

## Unit Tests
The `TransactionServiceImplTest.java` file located at `src/test/java/com.moneytransfer/service` contains tests for the service implementation, thus external dependencies (e.g. repositories) are mocked.

The unit tests cover the following ACs:
- AC 1: Happy path for money transfer between two accounts (results in successful transfer)
- AC 2: Insufficient balance to process money transfer
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist

## Future Containerization
In future versions, a Docker container will be provided for easier installation and running of the application in different environments.