#!/bin/bash

# Base URL
URL="http://localhost:8082"

echo "1. Registering User (Admin)..."
curl -s -X POST $URL/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"admin", "password":"password", "role":"ROLE_ADMIN"}' | jq .

echo -e "\n\n2. Logging in..."
TOKEN=$(curl -s -X POST $URL/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"admin", "password":"password"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "Login failed! Exiting."
    exit 1
fi

echo "Token received: $TOKEN"

echo -e "\n3. Creating Affiliate..."
curl -s -X POST $URL/api/afiliados \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "document": "12345678",
  "name": "Juan Perez",
  "salary": 5000000,
  "affiliationDate": "2023-01-01"
}' | jq .

echo -e "\n\n4. Creating Credit Application..."
curl -s -X POST $URL/api/solicitudes \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "affiliateDocument": "12345678",
  "amount": 1000000,
  "term": 12
}' | jq .

echo -e "\n\nTest Finished."
