#!/bin/bash
curl -v -X 'POST' 'http://localhost:8080/api/auth/tokens' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "test@example.com",
  "password": "password"
}'
