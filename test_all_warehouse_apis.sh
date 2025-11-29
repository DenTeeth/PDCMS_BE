#!/bin/bash
# Comprehensive Test Script for Warehouse APIs 6.1 to 6.11
# Date: November 28, 2025

BASE_URL="http://localhost:8080"
RESULTS_FILE="api_test_results_$(date +%Y%m%d_%H%M%S).log"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counter
PASS_COUNT=0
FAIL_COUNT=0

echo "=================================================="
echo "WAREHOUSE API TEST SUITE - APIs 6.1 to 6.11"
echo "=================================================="
echo "Base URL: $BASE_URL"
echo "Results will be saved to: $RESULTS_FILE"
echo ""

# Function to test API
test_api() {
    local api_num=$1
    local method=$2
    local endpoint=$3
    local description=$4
    local curl_cmd=$5
    
    echo -e "${BLUE}=== TEST API $api_num: $description ===${NC}"
    echo "Method: $method"
    echo "Endpoint: $endpoint"
    echo ""
    
    # Execute curl command and capture response
    response=$(eval "$curl_cmd" 2>&1)
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d':' -f2)
    body=$(echo "$response" | grep -v "HTTP_CODE:")
    
    # Log to file
    {
        echo "=========================================="
        echo "API $api_num: $description"
        echo "Time: $(date)"
        echo "Method: $method"
        echo "Endpoint: $endpoint"
        echo "HTTP Code: $http_code"
        echo "Response:"
        echo "$body"
        echo ""
    } >> "$RESULTS_FILE"
    
    # Check result
    if [[ "$http_code" =~ ^(200|201)$ ]]; then
        echo -e "${GREEN}✅ PASS - HTTP $http_code${NC}"
        ((PASS_COUNT++))
    else
        echo -e "${RED}❌ FAIL - HTTP $http_code${NC}"
        ((FAIL_COUNT++))
    fi
    
    # Show response preview (last 15 lines)
    echo "Response preview:"
    echo "$body" | tail -15
    echo ""
    echo ""
}

# ================================================
# STEP 0: LOGIN
# ================================================
echo -e "${YELLOW}=== LOGGING IN ===${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' 2>/dev/null)

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo -e "${RED}❌ Login failed! Cannot proceed with tests.${NC}"
  exit 1
fi

echo -e "${GREEN}✅ Login successful${NC}"
echo "Token length: ${#TOKEN}"
echo ""
echo ""

# ================================================
# API 6.1 - Inventory Summary
# ================================================
test_api "6.1" "GET" "/api/v1/warehouse/summary" \
    "Inventory Summary - Get paginated list of items with stock info" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/summary?page=0&size=5'"

# Test with filters - Use correct enum: OUT_OF_STOCK, LOW_STOCK, NORMAL, OVERSTOCK
test_api "6.1b" "GET" "/api/v1/warehouse/summary (with filters)" \
    "Inventory Summary - Filter by stock status" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/summary?page=0&size=5&stockStatus=NORMAL'"

# ================================================
# API 6.2 - Item Batches
# ================================================
test_api "6.2" "GET" "/api/v1/warehouse/batches/1" \
    "Item Batches - Get FEFO-sorted batches for item master ID" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/batches/1'"

# ================================================
# API 6.3 - Expiring Alerts
# ================================================
test_api "6.3" "GET" "/api/v1/warehouse/alerts/expiring" \
    "Expiring Alerts - Get items expiring within specified days" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/alerts/expiring?days=90&page=0&size=10'"

# ================================================
# API 6.4 - Import Transaction
# ================================================
test_api "6.4" "POST" "/api/v1/warehouse/import" \
    "Import Transaction - Create new import with batches" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -X POST \
      -H 'Authorization: Bearer $TOKEN' \
      -H 'Content-Type: application/json' \
      -d '{
        \"transactionDate\": \"2025-11-28\",
        \"supplierId\": 1,
        \"invoiceNumber\": \"INV-TEST-$(date +%s)\",
        \"items\": [
          {
            \"itemMasterId\": 1,
            \"quantity\": 50,
            \"unitId\": 1,
            \"purchasePrice\": 45000,
            \"lotNumber\": \"LOT-TEST-$(date +%s)\",
            \"expiryDate\": \"2026-12-31\"
          }
        ]
      }' \
      '$BASE_URL/api/v1/warehouse/import'"

# ================================================
# API 6.5 - Export Transaction
# ================================================
test_api "6.5" "POST" "/api/v1/inventory/export" \
    "Export Transaction - Create export with FEFO allocation" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -X POST \
      -H 'Authorization: Bearer $TOKEN' \
      -H 'Content-Type: application/json' \
      -d '{
        \"transactionDate\": \"2025-11-28\",
        \"exportType\": \"USAGE\",
        \"items\": [
          {
            \"itemMasterId\": 1,
            \"quantity\": 5,
            \"unitId\": 1
          }
        ]
      }' \
      '$BASE_URL/api/v1/inventory/export'"

# ================================================
# API 6.6 - Transaction History
# ================================================
test_api "6.6" "GET" "/api/v1/warehouse/transactions" \
    "Transaction History - Get filtered list of transactions" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/transactions?page=0&size=10'"

# Test with filters
test_api "6.6b" "GET" "/api/v1/warehouse/transactions (filter by type)" \
    "Transaction History - Filter by IMPORT type" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/transactions?page=0&size=5&type=IMPORT'"

# ================================================
# API 6.7 - Transaction Detail
# ================================================
test_api "6.7" "GET" "/api/v1/warehouse/transactions/1" \
    "Transaction Detail - Get full details of specific transaction" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/transactions/1'"

# ================================================
# API 6.8 - Item Master List
# ================================================
test_api "6.8" "GET" "/api/v1/warehouse/items" \
    "Item Master List - Get paginated list with filters" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/items?page=0&size=10'"

# Test with search
test_api "6.8b" "GET" "/api/v1/warehouse/items (search)" \
    "Item Master List - Search by keyword" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/items?page=0&size=10&search=syringe'"

# ================================================
# API 6.9 - Create Item Master
# ================================================
test_api "6.9" "POST" "/api/v1/warehouse/items" \
    "Create Item Master - Create new item with unit hierarchy" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -X POST \
      -H 'Authorization: Bearer $TOKEN' \
      -H 'Content-Type: application/json' \
      -d '{
        \"itemCode\": \"TEST-ITEM-$(date +%s)\",
        \"itemName\": \"Test Item $(date +%H%M%S)\",
        \"description\": \"Test item created by automated test\",
        \"categoryId\": 1,
        \"warehouseType\": \"NORMAL\",
        \"minStockLevel\": 10,
        \"maxStockLevel\": 100,
        \"requiresPrescription\": false,
        \"defaultShelfLifeDays\": 365,
        \"units\": [
          {
            \"unitName\": \"Viên\",
            \"isBaseUnit\": true,
            \"conversionRate\": 1.0,
            \"displayOrder\": 1
          },
          {
            \"unitName\": \"Vỉ\",
            \"isBaseUnit\": false,
            \"conversionRate\": 10.0,
            \"displayOrder\": 2
          }
        ]
      }' \
      '$BASE_URL/api/v1/warehouse/items'"

# ================================================
# API 6.10 - Update Item Master
# ================================================
# First, get an item to update
ITEM_ID=$(curl -s -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/v1/warehouse/items?page=0&size=1" | \
  grep -o '"itemMasterId":[0-9]*' | head -1 | cut -d':' -f2)

if [ ! -z "$ITEM_ID" ]; then
    test_api "6.10" "PUT" "/api/v1/warehouse/items/$ITEM_ID" \
        "Update Item Master - Update item details (Safe changes only)" \
        "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
          -X PUT \
          -H 'Authorization: Bearer $TOKEN' \
          -H 'Content-Type: application/json' \
          -d '{
            \"itemName\": \"Updated Item Name $(date +%H%M%S)\",
            \"description\": \"Updated description\",
            \"categoryId\": 1,
            \"warehouseType\": \"NORMAL\",
            \"minStockLevel\": 15,
            \"maxStockLevel\": 150,
            \"requiresPrescription\": false,
            \"defaultShelfLifeDays\": 365,
            \"units\": []
          }' \
          '$BASE_URL/api/v1/warehouse/items/$ITEM_ID'"
else
    echo -e "${YELLOW}⚠️  Skipping API 6.10 - No item found to update${NC}"
    echo ""
fi

# ================================================
# API 6.11 - Get Item Units
# ================================================
test_api "6.11" "GET" "/api/v1/warehouse/items/1/units" \
    "Get Item Units - Get unit hierarchy for dropdown" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/items/1/units'"

# Test with status filter
test_api "6.11b" "GET" "/api/v1/warehouse/items/1/units (all statuses)" \
    "Get Item Units - Get all units including inactive" \
    "curl -s -w '\nHTTP_CODE:%{http_code}\n' \
      -H 'Authorization: Bearer $TOKEN' \
      '$BASE_URL/api/v1/warehouse/items/1/units?status=all'"

# ================================================
# SUMMARY
# ================================================
echo ""
echo "=================================================="
echo "TEST SUMMARY"
echo "=================================================="
echo -e "${GREEN}✅ PASSED: $PASS_COUNT${NC}"
echo -e "${RED}❌ FAILED: $FAIL_COUNT${NC}"
echo "Total Tests: $((PASS_COUNT + FAIL_COUNT))"
echo ""
echo "Detailed results saved to: $RESULTS_FILE"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}"
    exit 0
else
    echo -e "${RED}⚠️  SOME TESTS FAILED - Check $RESULTS_FILE for details${NC}"
    exit 1
fi
