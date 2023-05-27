# MoneyTransfer API

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


## Deployment
