#!/bin/bash

echo "======================================"
echo "Testing Fixed Registration API"
echo "======================================"
echo ""

# Get JWT token first (assuming you have a test user)
echo "Step 1: Get JWT Token..."
echo "Please login first to get token"
echo ""

# Test the API
echo "Step 2: Testing POST /api/v1/fixed-registrations"
echo "Request body:"
cat << 'JSON'
{
  "employeeId": 1,
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [1, 2, 3],
  "effectiveFrom": "2025-11-01"
}
JSON

echo ""
echo "Run this curl command with your token:"
echo ""
cat << 'CURL'
curl -X POST http://localhost:8080/api/v1/fixed-registrations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "employeeId": 1,
    "workShiftId": "WKS_MORNING_02",
    "daysOfWeek": [1, 2, 3],
    "effectiveFrom": "2025-11-01"
  }'
CURL

echo ""
echo "======================================"
echo "Or use Postman/Thunder Client:"
echo "POST http://localhost:8080/api/v1/fixed-registrations"
echo "Headers:"
echo "  Content-Type: application/json"
echo "  Authorization: Bearer <your_token>"
echo "======================================"
