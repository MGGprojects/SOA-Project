# Event Service API Endpoints

## Overview
This document describes the Event Service API endpoints. **Note:** All endpoints currently use dummy data and do not connect to a database.

## Base URL
`http://localhost:8082/api/events`

## Endpoints

### 1. Create Event
**POST** `/api/events`

**Authentication:** Required (Bearer token - simulated)

**Request Body:**
```json
{
  "title": "Tech Conference 2026",
  "description": "Annual technology conference",
  "startTime": "2026-06-15T09:00:00Z",
  "endTime": "2026-06-15T17:00:00Z",
  "venue": "Convention Center",
  "businessId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response (201 Created):**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Tech Conference 2026",
  "description": "Annual technology conference",
  "startTime": "2026-06-15T09:00:00Z",
  "endTime": "2026-06-15T17:00:00Z",
  "venue": "Convention Center",
  "businessId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Dummy Values:**
- eventId: `550e8400-e29b-41d4-a716-446655440000`
- Authorization is simulated (not enforced)
- Venue conflict validation is simulated (always passes)

---

### 2. Search Events
**GET** `/api/events`

**Authentication:** Not required

**Query Parameters:**
- `city` (optional): Filter by city
- `date` (optional): Filter by date (YYYY-MM-DD)
- `businessId` (optional): Filter by business ID
- `page` (optional, default: 1): Page number
- `limit` (optional, default: 20): Items per page

**Example:** `/api/events?city=Amsterdam&date=2026-06-15&page=1&limit=20`

**Response (200 OK):**
```json
{
  "events": [
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Tech Conference 2026",
      "description": "Annual technology conference",
      "startTime": "2026-06-15T09:00:00Z",
      "endTime": "2026-06-15T17:00:00Z",
      "venue": "Convention Center",
      "businessId": "123e4567-e89b-12d3-a456-426614174000"
    },
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440001",
      "title": "Music Festival",
      "description": "Summer music festival featuring local artists",
      "startTime": "2026-07-20T14:00:00Z",
      "endTime": "2026-07-20T23:00:00Z",
      "venue": "City Park",
      "businessId": "123e4567-e89b-12d3-a456-426614174001"
    },
    {
      "eventId": "550e8400-e29b-41d4-a716-446655440002",
      "title": "Food Fair",
      "description": "International food and culture fair",
      "startTime": "2026-08-10T11:00:00Z",
      "endTime": "2026-08-10T20:00:00Z",
      "venue": "Downtown Square",
      "businessId": "123e4567-e89b-12d3-a456-426614174002"
    }
  ],
  "total": 3,
  "page": 1
}
```

**Dummy Values:**
- Returns 3 sample events
- Filters are accepted but not applied
- Pagination parameters are accepted but return fixed results

---

### 3. Get Event Details
**GET** `/api/events/{eventId}`

**Authentication:** Not required

**Path Parameters:**
- `eventId`: Event ID (e.g., `550e8400-e29b-41d4-a716-446655440000`)

**Response (200 OK):**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Tech Conference 2026",
  "description": "Annual technology conference featuring the latest innovations in software development, AI, and cloud computing",
  "startTime": "2026-06-15T09:00:00Z",
  "endTime": "2026-06-15T17:00:00Z",
  "venue": "Convention Center",
  "business": {
    "businessId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Tech Corp"
  }
}
```

**Dummy Values:**
- Returns a sample Tech Conference event
- Business info: `{ businessId: "123e4567-e89b-12d3-a456-426614174000", name: "Tech Corp" }`

---

### 4. Update Event
**PUT** `/api/events/{eventId}`

**Authentication:** Required (Bearer token - simulated)

**Path Parameters:**
- `eventId`: Event ID (e.g., `550e8400-e29b-41d4-a716-446655440000`)

**Request Body:**
```json
{
  "title": "Updated Tech Conference 2026",
  "description": "Updated description",
  "startTime": "2026-06-15T10:00:00Z",
  "endTime": "2026-06-15T18:00:00Z",
  "venue": "Grand Convention Center"
}
```

**Response (200 OK):**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Updated Tech Conference 2026",
  "description": "Updated description",
  "startTime": "2026-06-15T10:00:00Z",
  "endTime": "2026-06-15T18:00:00Z",
  "venue": "Grand Convention Center",
  "businessId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Dummy Values:**
- Authorization is simulated (not enforced)
- Venue conflict validation is simulated (always passes)
- Returns updated event with provided values

---

### 5. Delete Event
**DELETE** `/api/events/{eventId}`

**Authentication:** Required (Bearer token - simulated)

**Path Parameters:**
- `eventId`: Event ID (e.g., `550e8400-e29b-41d4-a716-446655440000`)

**Response (200 OK):**
```json
{
  "message": "Event cancelled"
}
```

**Dummy Values:**
- Authorization is simulated (not enforced)
- Always returns success message

---

### 6. Get Venue Availability
**GET** `/api/events/venues/{venue}/availability`

**Authentication:** Not required

**Path Parameters:**
- `venue`: Venue name (e.g., `Convention Center`)

**Query Parameters:**
- `date` (optional): Date to check availability (YYYY-MM-DD, e.g., `2026-06-15`)

**Example:** `/api/events/venues/Convention%20Center/availability?date=2026-06-15`

**Response (200 OK):**
```json
{
  "venue": "Convention Center",
  "availableSlots": [
    {
      "start": "2026-06-15T09:00:00Z",
      "end": "2026-06-15T12:00:00Z"
    },
    {
      "start": "2026-06-15T13:00:00Z",
      "end": "2026-06-15T17:00:00Z"
    },
    {
      "start": "2026-06-15T18:00:00Z",
      "end": "2026-06-15T22:00:00Z"
    }
  ]
}
```

**Dummy Values:**
- Returns 3 sample available time slots
- Slots: 09:00-12:00, 13:00-17:00, 18:00-22:00
- Date parameter is accepted but not applied to results

---

## Swagger Documentation

Access the interactive API documentation at:
`http://localhost:8082/swagger-ui.html`

## Testing the API

You can test the endpoints using:
1. **Swagger UI** - Interactive documentation with "Try it out" feature
2. **Postman** - Import the OpenAPI specification
3. **cURL** - Command-line testing

### Example cURL Commands:

**Create Event:**
```bash
curl -X POST http://localhost:8082/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer dummy-token" \
  -d '{
    "title": "Tech Conference 2026",
    "description": "Annual technology conference",
    "startTime": "2026-06-15T09:00:00Z",
    "endTime": "2026-06-15T17:00:00Z",
    "venue": "Convention Center",
    "businessId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

**Get All Events:**
```bash
curl http://localhost:8082/api/events
```

**Get Event by ID:**
```bash
curl http://localhost:8082/api/events/550e8400-e29b-41d4-a716-446655440000
```

**Get Venue Availability:**
```bash
curl "http://localhost:8082/api/events/venues/Convention%20Center/availability?date=2026-06-15"
```

## Notes

- All endpoints currently use **dummy data** and do not connect to a database
- Authentication is **simulated** and not enforced
- Venue conflict validation is **simulated** and always passes
- Filters and pagination parameters are accepted but not applied to results
- This is intended for development and testing purposes only
