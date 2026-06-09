#!/bin/bash

# WOOSH API Testing Commands
# Run these commands to test all API endpoints

BASE_URL="http://localhost:8000"

echo "=========================================="
echo "WOOSH API Testing Suite"
echo "=========================================="
echo ""

# Test 1: Sync User API
echo "Test 1: Sync User API"
echo "POST /api/v1/sync-user"
curl -X POST "$BASE_URL/api/v1/sync-user" \
  -H "Content-Type: application/json" \
  -d '{
    "firebase_uid": "test-uid-123",
    "name": "Test User",
    "email": "test@example.com",
    "phone": "08123456789"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# Test 2: Get Trips API
echo "Test 2: Get Trips API"
echo "GET /api/v1/trips"
curl -X GET "$BASE_URL/api/v1/trips" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Test 3: Update Profile API
echo "Test 3: Update Profile API"
echo "POST /api/v1/update-profile"
curl -X POST "$BASE_URL/api/v1/update-profile" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Updated Name",
    "phone": "08987654321"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# Test 4: Get User Tickets API
echo "Test 4: Get User Tickets API"
echo "POST /api/v1/user-tickets"
curl -X POST "$BASE_URL/api/v1/user-tickets" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# Test 5: Validate Ticket API
echo "Test 5: Validate Ticket API"
echo "POST /api/v1/validate-ticket"
curl -X POST "$BASE_URL/api/v1/validate-ticket" \
  -H "Content-Type: application/json" \
  -d '{
    "ticket_code": "WSH-TK-123456"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# Test 6: Refund Ticket API
echo "Test 6: Refund Ticket API"
echo "POST /api/v1/refund-ticket"
curl -X POST "$BASE_URL/api/v1/refund-ticket" \
  -H "Content-Type: application/json" \
  -d '{
    "ticket_code": "WSH-TK-123456",
    "email": "test@example.com",
    "reason": "Change of plans"
  }' \
  -w "\nStatus: %{http_code}\n\n"

echo "=========================================="
echo "API Testing Complete"
echo "=========================================="
