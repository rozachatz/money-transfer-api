#!/bin/bash

URL1="http://localhost:8080/transfer/optimistic"
URL2="http://localhost:8080/transfer/pessimistic"

executeRequest() {
    local url=$1
    local sourceAccountId=$2
    local targetAccountId=$3
    response=$(curl  -o /dev/null -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": $sourceAccountId, \"targetAccountId\": $targetAccountId, \"amount\": \"0.01\" }" $url)
    if [[ $response -eq 500 ]]; then
        echo "Internal Server Error: Request failed for $url with sourceAccountId=$sourceAccountId and targetAccountId=$targetAccountId"
    fi
}

echo "Performing 200 concurrent conflicting transfer requests using Optimistic and Pessimistic locking..."
echo " "
echo "Note: messages for internal server error will be printed in this terminal window."
sourceAccountId=1
targetAccountId=2
echo "----------------------------------"
echo "Optimistic Locking"
start_time=$(date +%s.%N)
for i in {1..100} ; do
    executeRequest "$URL1" $sourceAccountId $targetAccountId &
    executeRequest "$URL1" $targetAccountId $sourceAccountId &
done
wait
# Capture the end time
end_time=$(date +%s.%N)

# Calculate the elapsed time
elapsed_time=$(awk "BEGIN {printf \"%.3f\", $end_time - $start_time}")
echo "Elapsed time for Optimistic locking: $elapsed_time seconds"
# Pause and wait for user input
read -p "Press Enter to continue..."
echo "----------------------------------"
echo "Pessimistic Locking (nothing should be printed)"
start_time=$(date +%s.%N)
for i in {1..100} ; do
    executeRequest "$URL2" $sourceAccountId $targetAccountId &
    executeRequest "$URL2" $targetAccountId $sourceAccountId &
done
wait
# Capture the end time
end_time=$(date +%s.%N)

# Calculate the elapsed time
elapsed_time=$(awk "BEGIN {printf \"%.3f\", $end_time - $start_time}")

echo "Elapsed time for Pessimistic locking: $elapsed_time seconds"
read -p "Press Enter to exit..."