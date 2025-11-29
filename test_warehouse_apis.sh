#!/bin/bash
# Test script for 5 warehouse API issues

BASE_URL="http://localhost:8080"

echo "=== LOGGING IN ==="
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' 2>/dev/null)

# Debug: Save raw response
echo "$LOGIN_RESPONSE" > /tmp/login_raw.json

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Login failed!"
  exit 1
fi

echo "✅ Login successful, token length: ${#TOKEN}"
echo ""

# Test API 6.1 - Inventory Summary
echo "=== TEST #18: API 6.1 - GET /api/v1/warehouse/summary ==="
curl -s -w "\nHTTP_CODE:%{http_code}\n" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/v1/warehouse/summary?page=0&size=10" \
  | tail -10
echo ""
echo ""

# Test API 6.2 - Item Batches
echo "=== TEST #19: API 6.2 - GET /api/v1/warehouse/batches/1 ==="
curl -s -w "\nHTTP_CODE:%{http_code}\n" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/v1/warehouse/batches/1" \
  | tail -10
echo ""
echo ""

# Test API 6.4 - Import Transaction
echo "=== TEST #20: API 6.4 - POST /api/v1/warehouse/import ==="
curl -s -w "\nHTTP_CODE:%{http_code}\n" \
  -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionDate": "2025-11-28",
    "supplierId": 1,
    "invoiceNumber": "INV-TEST-001",
    "items": [
      {
        "itemMasterId": 1,
        "quantity": 100,
        "unitId": 1,
        "purchasePrice": 50000,
        "lotNumber": "LOT-TEST-001",
        "expiryDate": "2026-12-31"
      }
    ]
  }' \
  "$BASE_URL/api/v1/warehouse/import" \
  | tail -10
echo ""
echo ""

# Test API 6.5 - Export Transaction
echo "=== TEST #21: API 6.5 - POST /api/v1/inventory/export ==="
curl -s -w "\nHTTP_CODE:%{http_code}\n" \
  -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionDate": "2025-11-28",
    "exportType": "USAGE",
    "items": [
      {
        "itemMasterId": 1,
        "quantity": 10,
        "unitId": 1
      }
    ]
  }' \
  "$BASE_URL/api/v1/inventory/export" \
  | tail -10
echo ""
echo ""

# Test API 6.7 - Transaction Detail
echo "=== TEST #22: API 6.7 - GET /api/v1/warehouse/transactions/1 ==="
curl -s -w "\nHTTP_CODE:%{http_code}\n" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/v1/warehouse/transactions/1" \
  | tail -10
echo ""

echo "=== ALL TESTS COMPLETED ==="
