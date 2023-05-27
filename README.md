# MoneyTransfer API

## Introduction


## Technologies
* Java
* Spring Boot
* Maven
* H2 (Embedded Database)

## Getting Started

## Accessing the H2 Console
To access the H2 console for the MoneyTransfer API, follow these steps:
1. Start the MoneyTransfer API application.
2. Open a web browser.
3. Enter the following URL: `http://localhost:8080/h2-console`.
4. In the login page of the H2 console, configure the following settings:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (leave it empty)
5. Click the "Connect" button to log in to the H2 console.

Once you are logged in to the H2 console, you can view and interact with the database used by the MoneyTransfer API.

## API Documentation
### Making Requests
You can interact with the Money Transfer API by sending HTTP requests to the provided endpoints. Here's an example of how to make a request using curl:

````bash
curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00", \"amount\": "30.00"}" "http://localhost:8080/transferMoney"
````

This curl command is used to initiate a transfer of 30.00 EUR (default currency) from account with ID 1 to account with ID 2, using the /transferMoney endpoint of the MoneyTransfer API.

## Architecture


## Database


## Testing


## Deployment
