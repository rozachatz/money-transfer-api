# MoneyTransfer API

## Introduction


## Technologies
* Java
* Spring Boot
* Maven
* H2 (Embedded Database)

## Getting Started


## API Documentation
### Making Requests
You can interact with the Money Transfer API by sending HTTP requests to the provided endpoints. Here's an example of how to make a request using curl:
curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00", \"amount\": "30.00"}" "http://localhost:8080/transferMoney"

This curl command is used to initiate a transfer of 30.00 EUR (default currency) from account with ID 1 to account with ID 2, using the /transferMoney endpoint of the MoneyTransfer API.

## Architecture


## Database


## Testing


## Deployment
