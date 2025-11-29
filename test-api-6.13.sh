#!/bin/bash

# API 6.13 - Get Suppliers Testing Script
# Test all scenarios for supplier listing with filters

BASE_URL="http://localhost:8080/api/v1/warehouse/suppliers"
echo "========================================="
echo "API 6.13 - GET SUPPLIERS TESTING"
echo "========================================="
echo ""

# Get JWT token first (login as admin)
echo "🔐 Step 1: Login to get JWT token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | sed 's/"accessToken":"//')

if [ -z "$TOKEN" ]; then
  echo "❌ LOGIN FAILED! Cannot get token."
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

echo "✅ Login successful! Token: ${TOKEN:0:50}..."
echo ""

# Test Scenario 1: Get all suppliers (default pagination)
echo "========================================="
echo "📋 Scenario 1: Get All Suppliers (Default)"
echo "========================================="
curl -s -X GET "${BASE_URL}?page=0&size=20" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 2: Search by name
echo "========================================="
echo "🔍 Scenario 2: Search by Name (ABC)"
echo "========================================="
curl -s -X GET "${BASE_URL}?search=ABC&page=0&size=10" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 3: Filter by active status
echo "========================================="
echo "✅ Scenario 3: Filter Active Suppliers"
echo "========================================="
curl -s -X GET "${BASE_URL}?isActive=true&page=0&size=20" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 4: Filter blacklisted suppliers
echo "========================================="
echo "⚠️ Scenario 4: Filter Blacklisted Suppliers"
echo "========================================="
curl -s -X GET "${BASE_URL}?isBlacklisted=true&page=0&size=20" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 5: Filter non-blacklisted suppliers
echo "========================================="
echo "✅ Scenario 5: Filter Non-Blacklisted Suppliers"
echo "========================================="
curl -s -X GET "${BASE_URL}?isBlacklisted=false&page=0&size=20" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 6: Sort by totalOrders DESC
echo "========================================="
echo "📊 Scenario 6: Sort by Total Orders (DESC)"
echo "========================================="
curl -s -X GET "${BASE_URL}?sortBy=totalOrders&sortDir=DESC&page=0&size=10" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 7: Sort by lastOrderDate DESC (recent orders first)
echo "========================================="
echo "📅 Scenario 7: Sort by Last Order Date (Recent First)"
echo "========================================="
curl -s -X GET "${BASE_URL}?sortBy=lastOrderDate&sortDir=DESC&page=0&size=10" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 8: Combined filters (active + non-blacklisted + search)
echo "========================================="
echo "🎯 Scenario 8: Combined Filters"
echo "Active + Non-Blacklisted + Search 'Công ty'"
echo "========================================="
curl -s -X GET "${BASE_URL}?isActive=true&isBlacklisted=false&search=Công%20ty&page=0&size=20" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 9: Pagination test (page 2)
echo "========================================="
echo "📄 Scenario 9: Pagination (Page 2)"
echo "========================================="
curl -s -X GET "${BASE_URL}?page=1&size=5" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 10: Invalid sortBy parameter (should fallback to default)
echo "========================================="
echo "❓ Scenario 10: Invalid Sort Field (should use default)"
echo "========================================="
curl -s -X GET "${BASE_URL}?sortBy=invalidField&sortDir=ASC" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 11: Search by phone number
echo "========================================="
echo "📞 Scenario 11: Search by Phone Number"
echo "========================================="
curl -s -X GET "${BASE_URL}?search=0901" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 12: Search by email
echo "========================================="
echo "📧 Scenario 12: Search by Email"
echo "========================================="
curl -s -X GET "${BASE_URL}?search=@dental" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

# Test Scenario 13: Unauthorized access (no token)
echo "========================================="
echo "🚫 Scenario 13: Unauthorized Access (No Token)"
echo "========================================="
curl -s -X GET "${BASE_URL}" | python -m json.tool
echo ""
echo ""

# Test Scenario 14: Large page size (should be limited to 100)
echo "========================================="
echo "📏 Scenario 14: Large Page Size (should limit to 100)"
echo "========================================="
curl -s -X GET "${BASE_URL}?size=500" \
  -H "Authorization: Bearer ${TOKEN}" | python -m json.tool
echo ""
echo ""

echo "========================================="
echo "✅ ALL TESTS COMPLETED!"
echo "========================================="
