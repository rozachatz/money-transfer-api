#!/bin/bash

URL1="http://localhost:8080/transactions/optimistic"
URL2="http://localhost:8080/transactions/pessimistic"
Requests1=0
Requests2=0

# Function to execute a request asynchronously
executeRequest() {
    local url=$1
    local sourceAccountId=$2
    local targetAccountId=$3

    response=$(curl  -o /dev/null -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": $sourceAccountId, \"targetAccountId\": $targetAccountId, \"amount\": \"0.01\" }" $url)

    if [[ $response -eq 500 ]]; then
        echo "Internal Server Error: Request failed for $url with sourceAccountId=$sourceAccountId and targetAccountId=$targetAccountId"
    fi
}

echo "Performing 100 conflicting requests using Optimistic and Pessimistic locking..."
echo " "
echo "Note: internal server errors (if any) will be printed in this terminal window."
sourceAccountId=1
targetAccountId=2
echo "----------------------------------"
echo "Optimistic Locking"
for i in {1..100} ; do
    executeRequest "$URL1" $sourceAccountId $targetAccountId &
    executeRequest "$URL1" $targetAccountId $sourceAccountId &
done
wait

# Pause and wait for user input
read -p "Press Enter to continue..."
echo "----------------------------------"
echo "Pessimistic Locking (nothing should be printed)"
for i in {1..100} ; do
    executeRequest "$URL2" $sourceAccountId $targetAccountId &
    executeRequest "$URL2" $targetAccountId $sourceAccountId &
done
wait
read -p "Press Enter to exit..."