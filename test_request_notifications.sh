#!/bin/bash

# Test Request Notification System
# Date: December 22, 2025
# Feature: BE-904 Push Notification for Employee Requests

echo "=========================================="
echo "Testing Request Notification System"
echo "=========================================="
echo ""

# Configuration
BASE_URL="http://localhost:8080"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="123456"
DOCTOR_USERNAME="doctor"
DOCTOR_PASSWORD="123456"

echo "Step 1: Login as ADMIN"
echo "----------------------------"
ADMIN_TOKEN=$(curl -s -X POST "${BASE_URL}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  | jq -r '.token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo "ERROR: Failed to login as admin"
  exit 1
fi
echo "Admin token: ${ADMIN_TOKEN:0:20}..."
echo ""

echo "Step 2: Login as DOCTOR"
echo "----------------------------"
DOCTOR_TOKEN=$(curl -s -X POST "${BASE_URL}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${DOCTOR_USERNAME}\",\"password\":\"${DOCTOR_PASSWORD}\"}" \
  | jq -r '.token')

if [ "$DOCTOR_TOKEN" = "null" ] || [ -z "$DOCTOR_TOKEN" ]; then
  echo "ERROR: Failed to login as doctor"
  exit 1
fi
echo "Doctor token: ${DOCTOR_TOKEN:0:20}..."
echo ""

echo "Step 3: Get Admin Notification Count (Before)"
echo "----------------------------"
BEFORE_COUNT=$(curl -s -X GET "${BASE_URL}/api/v1/notifications/unread-count" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  | jq -r '.unreadCount')
echo "Admin unread notifications BEFORE: ${BEFORE_COUNT}"
echo ""

echo "Step 4: Create Time-off Request (as Doctor)"
echo "----------------------------"
TIMEOFF_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/time-off-requests" \
  -H "Authorization: Bearer ${DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 2,
    "timeOffTypeId": "TOT001",
    "startDate": "2025-12-25",
    "endDate": "2025-12-27",
    "reason": "Test notification - Time off request"
  }')

TIMEOFF_REQUEST_ID=$(echo $TIMEOFF_RESPONSE | jq -r '.requestId')
echo "Created time-off request ID: ${TIMEOFF_REQUEST_ID}"
echo "Response: ${TIMEOFF_RESPONSE}" | jq '.'
echo ""

sleep 2

echo "Step 5: Create Overtime Request (as Doctor)"
echo "----------------------------"
OVERTIME_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/overtime-requests" \
  -H "Authorization: Bearer ${DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "workDate": "2025-12-24",
    "workShiftId": "WS001",
    "reason": "Test notification - Overtime request"
  }')

OVERTIME_REQUEST_ID=$(echo $OVERTIME_RESPONSE | jq -r '.requestId')
echo "Created overtime request ID: ${OVERTIME_REQUEST_ID}"
echo "Response: ${OVERTIME_RESPONSE}" | jq '.'
echo ""

sleep 2

echo "Step 6: Get Admin Notification Count (After)"
echo "----------------------------"
AFTER_COUNT=$(curl -s -X GET "${BASE_URL}/api/v1/notifications/unread-count" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  | jq -r '.unreadCount')
echo "Admin unread notifications AFTER: ${AFTER_COUNT}"
echo "New notifications: $((AFTER_COUNT - BEFORE_COUNT))"
echo ""

echo "Step 7: Get Admin Notifications List"
echo "----------------------------"
NOTIFICATIONS=$(curl -s -X GET "${BASE_URL}/api/v1/notifications?page=0&size=5&sort=createdAt,desc" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}")

echo "Recent notifications:"
echo "${NOTIFICATIONS}" | jq '.content[] | {
  notificationId,
  type,
  title,
  message,
  relatedEntityType,
  relatedEntityId,
  isRead,
  createdAt
}'
echo ""

echo "Step 8: Verify Notification Types"
echo "----------------------------"
TIMEOFF_NOTIF=$(echo "${NOTIFICATIONS}" | jq '.content[] | select(.type == "REQUEST_TIME_OFF_PENDING")')
OVERTIME_NOTIF=$(echo "${NOTIFICATIONS}" | jq '.content[] | select(.type == "REQUEST_OVERTIME_PENDING")')

if [ ! -z "$TIMEOFF_NOTIF" ]; then
  echo "✓ Time-off notification found:"
  echo "$TIMEOFF_NOTIF" | jq '{type, title, relatedEntityId}'
else
  echo "✗ Time-off notification NOT found"
fi
echo ""

if [ ! -z "$OVERTIME_NOTIF" ]; then
  echo "✓ Overtime notification found:"
  echo "$OVERTIME_NOTIF" | jq '{type, title, relatedEntityId}'
else
  echo "✗ Overtime notification NOT found"
fi
echo ""

echo "Step 9: Mark Notifications as Read"
echo "----------------------------"
FIRST_NOTIF_ID=$(echo "${NOTIFICATIONS}" | jq -r '.content[0].notificationId')
if [ ! -z "$FIRST_NOTIF_ID" ] && [ "$FIRST_NOTIF_ID" != "null" ]; then
  curl -s -X PATCH "${BASE_URL}/api/v1/notifications/${FIRST_NOTIF_ID}/read" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}"
  echo "Marked notification ${FIRST_NOTIF_ID} as read"
else
  echo "No notification to mark as read"
fi
echo ""

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo "Notifications before: ${BEFORE_COUNT}"
echo "Notifications after: ${AFTER_COUNT}"
echo "Expected new notifications: 2 (1 time-off + 1 overtime)"
echo "Actual new notifications: $((AFTER_COUNT - BEFORE_COUNT))"
echo ""

if [ $((AFTER_COUNT - BEFORE_COUNT)) -ge 2 ]; then
  echo "✓ TEST PASSED: Notifications are being created"
else
  echo "✗ TEST FAILED: Expected at least 2 new notifications"
fi
echo ""

echo "Created Requests:"
echo "- Time-off Request ID: ${TIMEOFF_REQUEST_ID}"
echo "- Overtime Request ID: ${OVERTIME_REQUEST_ID}"
echo ""
echo "=========================================="
