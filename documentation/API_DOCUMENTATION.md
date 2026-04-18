# MindTrack Journal — API Documentation

> Base URL: `http://localhost:8080`  
> All endpoints return a `BaseResponse<T>` wrapper unless noted.  
> Auth: Bearer JWT in `Authorization` header.

---

## Global Response Envelope

```json
{
  "success": true,
  "message": "...",
  "data": { ... }
}
```

`data` is `null`-excluded. On error, `success: false` and `message` holds the error.

---

## Auth Roles

| Role | Access |
|------|--------|
| `USER` | Own journals, profile |
| `ADMIN` | User management |
| `SYS_ADMIN` | Plans, system config |

---

## 1. Authentication

### POST `/api/v1/auth/register`

Register new user. Public.

**Request Body**
```json
{
  "username": "string (required)",
  "email": "string (required)",
  "password": "string (required)",
  "firstName": "string (required)",
  "lastName": "string (optional)"
}
```

**Response `201`**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "uuid",
    "username": "string",
    "email": "string"
  }
}
```

---

### POST `/api/v1/auth/login`

Login and get tokens. Public.

**Request Body**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Response `200`**
```json
{
  "access_token": "string",
  "refresh_token": "string",
  "expires_in": "string",
  "defaultTenantSlug": "string"
}
```

---

### POST `/api/v1/auth/refresh-token`

Exchange refresh token for new access token. Public.

**Request Body**
```json
{
  "refreshToken": "string (required)"
}
```

**Response `200`**
```json
{
  "access_token": "string",
  "refresh_token": "string",
  "expires_in": "string",
  "defaultTenantSlug": "string"
}
```

---

### DELETE `/api/v1/auth/delete/{id}`

Delete a user by ID. Requires `ADMIN` or `SYS_ADMIN`.

**Path Variables**

| Param | Type | Description |
|-------|------|-------------|
| `id` | string | User ID |

**Response `200`**
```json
{
  "success": true,
  "message": "User Deleted Successfully",
  "data": null
}
```

---

### POST `/api/v1/admin/auth/register`

Register admin user. Requires `SYS_ADMIN`.

**Request Body** — same as `/api/v1/auth/register`

**Response `200`**
```json
{
  "success": true,
  "message": "Admin registered successfully",
  "data": null
}
```

---

## 2. User Profile

### GET `/api/v1/users/me`

Get current user profile. Requires JWT.

**Response `200`**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "username": "string",
    "fullName": "string",
    "email": "string",
    "defaultTenant": {
      "id": "uuid",
      "slug": "string",
      "name": "string"
    },
    "active": true,
    "avatarUrl": "string",
    "countryCode": "string",
    "phone": "string",
    "timezone": "string",
    "address": "string"
  }
}
```

---

### PATCH `/api/v1/users/me`

Update current user profile. Requires JWT. All fields optional.

**Request Body**
```json
{
  "fullName": "string",
  "username": "string",
  "email": "string",
  "avatarUrl": "string",
  "countryCode": "string",
  "phone": "string",
  "timezone": "string",
  "address": "string",
  "active": true,
  "defaultTenant": "string (tenant slug)"
}
```

**Response `200`** — same shape as `GET /users/me`

---

### PUT `/api/v1/users/me/update-avatar`

Update avatar. Requires JWT. `multipart/form-data`.

**Form Fields**

| Field | Type | Required |
|-------|------|----------|
| `avatarUrl` | string | Yes |

**Response `200`**
```json
{
  "success": true,
  "message": "Avatar updated successfully",
  "data": null
}
```

---

## 3. Subscription Plans

### GET `/api/v1/plans`

List all active plans. Public.

**Response `200`**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "tier": "FREE | PRO | ENTERPRISE",
      "displayName": "string",
      "description": "string",
      "maxPages": -1,
      "maxBlocksPerPage": -1,
      "maxTags": -1,
      "maxMembers": 1,
      "aiEnrichmentEnabled": false,
      "sentimentAnalysisEnabled": true,
      "sharingEnabled": false,
      "exportEnabled": false,
      "priceMonthly": 0.00,
      "priceYearly": 0.00,
      "currency": "USD",
      "active": true
    }
  ]
}
```

> `-1` means unlimited.

---

### GET `/api/v1/plans/{planId}`

Get single plan. Public.

**Path Variables**

| Param | Type |
|-------|------|
| `planId` | uuid |

**Response `200`** — single `PlanResponse` object in `data`

---

### POST `/api/v1/admin/plans`

Create plan. Requires `SYS_ADMIN`.

**Request Body**
```json
{
  "tier": "FREE | PRO | ENTERPRISE (required)",
  "displayName": "string (required)",
  "description": "string",
  "maxPages": -1,
  "maxBlocksPerPage": -1,
  "maxTags": -1,
  "maxMembers": 1,
  "aiEnrichmentEnabled": false,
  "sentimentAnalysisEnabled": true,
  "sharingEnabled": false,
  "exportEnabled": false,
  "priceMonthly": 0.00,
  "priceYearly": 0.00,
  "currency": "USD"
}
```

**Response `201`** — `PlanResponse` in `data`

---

### PUT `/api/v1/admin/plans/{planId}`

Update plan. Requires `SYS_ADMIN`. All fields optional.

**Path Variables** — `planId` (uuid)

**Request Body** — same fields as POST, all optional

**Response `200`** — updated `PlanResponse`

---

### GET `/api/v1/admin/plans`

List all plans (including inactive). Requires `ADMIN` or `SYS_ADMIN`.

**Response `200`** — `List<PlanResponse>`

---

### GET `/api/v1/admin/plans/{planId}`

Get plan by ID (admin view). Requires `ADMIN` or `SYS_ADMIN`.

**Response `200`** — `PlanResponse`

---

### PATCH `/api/v1/admin/plans/{planId}/deactivate`

Deactivate plan. Requires `SYS_ADMIN`.

**Response `200`**
```json
{ "success": true, "message": "Plan deactivated", "data": null }
```

---

### PATCH `/api/v1/admin/plans/{planId}/reactivate`

Reactivate plan. Requires `SYS_ADMIN`.

**Response `200`**
```json
{ "success": true, "message": "Plan reactivated", "data": null }
```

---

## 4. Journal Pages

### POST `/api/v1/journals/pages`

Create journal page. Requires JWT.

**Request Body**
```json
{
  "tenantId": "uuid (required)",
  "title": "string (required)",
  "description": "string",
  "coverImageUrl": "string",
  "entryDate": "YYYY-MM-DD (required)",
  "isPrivate": true,
  "tagIds": ["uuid", "uuid"],
  "blocks": [
    {
      "type": "TEXT | IMAGE | VIDEO | AUDIO | QUOTE | HEADING (required)",
      "parentBlockId": "uuid (optional)",
      "orderIndex": 0,
      "content": { "text": "string" },
      "metadata": {}
    }
  ]
}
```

**Response `201`**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "tenantId": "uuid",
    "userId": "uuid",
    "title": "string",
    "description": "string",
    "coverImageUrl": "string",
    "entryDate": "YYYY-MM-DD",
    "isPrivate": true,
    "tags": [
      { "id": "uuid", "name": "string", "color": "string" }
    ],
    "blocks": [
      {
        "id": "uuid",
        "parentBlockId": "uuid",
        "type": "TEXT",
        "orderIndex": 0,
        "content": {},
        "metadata": {},
        "childBlocks": [],
        "createdAt": "ISO datetime",
        "updatedAt": "ISO datetime"
      }
    ],
    "sentiment": {
      "sentimentLabel": "POSITIVE | NEGATIVE | NEUTRAL",
      "sentimentScore": 0.85,
      "sentimentScores": { "POSITIVE": 0.85, "NEGATIVE": 0.05, "NEUTRAL": 0.10 },
      "dominantEmotion": "joy",
      "emotionVector": { "joy": 0.7, "sadness": 0.1 },
      "analysedAt": "ISO datetime"
    },
    "aiEnrichment": {},
    "createdAt": "ISO datetime",
    "updatedAt": "ISO datetime"
  }
}
```

---

### GET `/api/v1/journals/pages/{pageId}`

Get single page with full detail. Requires JWT.

**Path Variables** — `pageId` (uuid)

**Response `200`** — same as `POST /journals/pages` response

---

### GET `/api/v1/journals/pages`

List pages (paginated). Requires JWT.

**Query Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `from` | date (YYYY-MM-DD) | none | Filter from date |
| `to` | date (YYYY-MM-DD) | none | Filter to date |
| `page` | integer | `0` | Page number (0-indexed) |
| `size` | integer | `20` | Page size |
| `sort` | string | `entryDate` | Sort field |

**Response `200`**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "tenantId": "uuid",
        "userId": "uuid",
        "title": "string",
        "description": "string",
        "coverImageUrl": "string",
        "entryDate": "YYYY-MM-DD",
        "isPrivate": true,
        "tags": [],
        "blockCount": 3,
        "sentimentLabel": "POSITIVE",
        "sentimentScore": 0.85,
        "dominantEmotion": "joy",
        "createdAt": "ISO datetime",
        "updatedAt": "ISO datetime"
      }
    ],
    "totalElements": 42,
    "totalPages": 3,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

---

### PUT `/api/v1/journals/pages/{pageId}`

Update page metadata. Requires JWT. All fields optional.

**Path Variables** — `pageId` (uuid)

**Request Body**
```json
{
  "title": "string",
  "description": "string",
  "coverImageUrl": "string",
  "isPrivate": true
}
```

**Response `200`** — full `JournalPageDetailResponse`

---

### POST `/api/v1/journals/pages/{pageId}/tags/{tagId}`

Add tag to page. Requires JWT.

**Path Variables**

| Param | Type |
|-------|------|
| `pageId` | uuid |
| `tagId` | uuid |

**Response `200`**
```json
{ "success": true, "message": "Tag added to page", "data": null }
```

---

### DELETE `/api/v1/journals/pages/{pageId}/tags/{tagId}`

Remove tag from page. Requires JWT.

**Path Variables** — `pageId` (uuid), `tagId` (uuid)

**Response `200`**
```json
{ "success": true, "message": "Tag removed from page", "data": null }
```

---

### GET `/api/v1/journals/pages/analytics-feed`

Lightweight list of pages with sentiment data for charting. Requires JWT.

**Query Parameters**

| Param | Type | Description |
|-------|------|-------------|
| `from` | date (YYYY-MM-DD) | optional |
| `to` | date (YYYY-MM-DD) | optional |

**Response `200`**
```json
{
  "success": true,
  "data": [
    {
      "entryDate": "YYYY-MM-DD",
      "sentimentLabel": "POSITIVE",
      "sentimentScore": 0.85,
      "dominantEmotion": "joy"
    }
  ]
}
```

---

### POST `/api/v1/journals/pages/{pageId}/analyze`

Manually trigger sentiment analysis on a page. Requires JWT.

**Path Variables** — `pageId` (uuid)

**Response `200`**
```json
{
  "success": true,
  "data": {
    "sentimentLabel": "POSITIVE",
    "sentimentScore": 0.85,
    "sentimentScores": {
      "POSITIVE": 0.85,
      "NEGATIVE": 0.05,
      "NEUTRAL": 0.10
    },
    "dominantEmotion": "joy",
    "emotionVector": {
      "joy": 0.70,
      "sadness": 0.05,
      "anger": 0.03,
      "fear": 0.02
    },
    "analysedAt": "ISO datetime"
  }
}
```

---

## 5. Journal Blocks

### POST `/api/v1/journals/pages/{pageId}/blocks`

Add block to page. Requires JWT.

**Path Variables** — `pageId` (uuid)

**Request Body**
```json
{
  "type": "TEXT | IMAGE | VIDEO | AUDIO | QUOTE | HEADING (required)",
  "parentBlockId": "uuid (optional, for nested blocks)",
  "orderIndex": 0,
  "content": {
    "text": "Your journal content here..."
  },
  "metadata": {}
}
```

> `content` shape varies by block type:
> - `TEXT` / `QUOTE` / `HEADING` → `{ "text": "string" }`
> - `IMAGE` / `VIDEO` / `AUDIO` → `{ "url": "string", "caption": "string" }`

**Response `201`**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "parentBlockId": "uuid",
    "type": "TEXT",
    "orderIndex": 0,
    "content": {},
    "metadata": {},
    "childBlocks": [],
    "createdAt": "ISO datetime",
    "updatedAt": "ISO datetime"
  }
}
```

---

### PUT `/api/v1/journals/pages/{pageId}/blocks/{blockId}`

Update block. Requires JWT. All fields optional.

**Path Variables** — `pageId` (uuid), `blockId` (uuid)

**Request Body**
```json
{
  "type": "TEXT",
  "orderIndex": 1,
  "content": { "text": "Updated content" },
  "metadata": {}
}
```

**Response `200`** — updated `JournalBlockResponse`

---

### DELETE `/api/v1/journals/pages/{pageId}/blocks/{blockId}`

Soft-delete block. Requires JWT.

**Path Variables** — `pageId` (uuid), `blockId` (uuid)

**Response `200`**
```json
{ "success": true, "message": "Block deleted", "data": null }
```

---

### PUT `/api/v1/journals/pages/{pageId}/blocks/reorder`

Reorder blocks by providing ordered list of IDs. Requires JWT.

**Path Variables** — `pageId` (uuid)

**Request Body**
```json
{
  "blockIds": ["uuid-1", "uuid-2", "uuid-3"]
}
```

**Response `200`** — `List<JournalBlockResponse>` in new order

---

## 6. Tags

### POST `/api/v1/journals/tags`

Create tag. Requires JWT.

**Request Body**
```json
{
  "tenantId": "uuid (required)",
  "name": "string (required)",
  "color": "#hex or css color (optional)"
}
```

**Response `201`**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "string",
    "color": "string"
  }
}
```

---

### GET `/api/v1/journals/tags`

List tags for a tenant. Requires JWT.

**Query Parameters**

| Param | Type | Required |
|-------|------|----------|
| `tenantId` | uuid | Yes |

**Response `200`**
```json
{
  "success": true,
  "data": [
    { "id": "uuid", "name": "string", "color": "string" }
  ]
}
```

---

### PUT `/api/v1/journals/tags/{tagId}`

Update tag. Requires JWT.

**Path Variables** — `tagId` (uuid)

**Query Parameters** — `tenantId` (uuid, required)

**Request Body**
```json
{
  "name": "string",
  "color": "string"
}
```

**Response `200`** — updated `TagResponse`

---

### DELETE `/api/v1/journals/tags/{tagId}`

Delete tag. Requires JWT.

**Path Variables** — `tagId` (uuid)

**Query Parameters** — `tenantId` (uuid, required)

**Response `200`**
```json
{ "success": true, "message": "Tag deleted", "data": null }
```

---

## 7. Analytics

### POST `/api/v1/analytics/refresh`

Force recompute all analytics for current user. Requires JWT.

**Response `200`**
```json
{ "success": true, "message": "Analytics refreshed successfully", "data": null }
```

---

### GET `/api/v1/analytics/summary`

Overall stats for the user. Requires JWT.

**Response `200`**
```json
{
  "success": true,
  "data": {
    "totalEntries": 42,
    "avgSentimentScore": 0.65,
    "positiveCount": 28,
    "negativeCount": 5,
    "neutralCount": 9,
    "mostFrequentEmotion": "joy",
    "currentStreak": 7,
    "longestStreak": 21
  }
}
```

---

### GET `/api/v1/analytics/sentiment/trends`

Sentiment trend over time. Requires JWT.

**Query Parameters**

| Param | Type | Default | Values |
|-------|------|---------|--------|
| `period` | string | `DAILY` | `DAILY`, `WEEKLY`, `MONTHLY` |
| `from` | date (YYYY-MM-DD) | none | optional |
| `to` | date (YYYY-MM-DD) | none | optional |

**Response `200`**
```json
{
  "success": true,
  "data": [
    {
      "period": "2024-01-15",
      "entryCount": 3,
      "avgSentimentScore": 0.72,
      "sentimentLabel": "POSITIVE",
      "dominantEmotion": "joy"
    }
  ]
}
```

> `period` value format:
> - `DAILY` → `YYYY-MM-DD`
> - `WEEKLY` → ISO week e.g. `2024-W03`
> - `MONTHLY` → `YYYY-MM`

---

### GET `/api/v1/analytics/mood/calendar`

Monthly mood calendar data. Requires JWT.

**Query Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `year` | integer | current year | Pass `0` for current |
| `month` | integer | current month | 1–12, pass `0` for current |

**Response `200`**
```json
{
  "success": true,
  "data": [
    {
      "date": "2024-01-15",
      "entryCount": 1,
      "sentimentScore": 0.82,
      "sentimentLabel": "POSITIVE",
      "dominantEmotion": "joy"
    }
  ]
}
```

---

### GET `/api/v1/analytics/emotions/distribution`

Emotion frequency breakdown. Requires JWT.

**Query Parameters**

| Param | Type | Default |
|-------|------|---------|
| `from` | date (YYYY-MM-DD) | 30 days ago |
| `to` | date (YYYY-MM-DD) | today |

**Response `200`**
```json
{
  "success": true,
  "data": [
    { "emotion": "joy", "count": 18, "percentage": 42.85 },
    { "emotion": "sadness", "count": 5, "percentage": 11.90 },
    { "emotion": "anger", "count": 3, "percentage": 7.14 }
  ]
}
```

---

### GET `/api/v1/analytics/writing/streak`

Current and longest writing streak. Requires JWT.

**Response `200`**
```json
{
  "success": true,
  "data": {
    "currentStreak": 7,
    "longestStreak": 21,
    "lastEntryDate": "YYYY-MM-DD"
  }
}
```

---

## 8. Notifications

### POST `/api/v1/notification/test`

Send test email notification.

**Query Parameters**

| Param | Type | Required |
|-------|------|----------|
| `email` | string | Yes |

**Response** — `204 No Content`

---

## Enums Reference

### `BlockType`
`TEXT`, `IMAGE`, `VIDEO`, `AUDIO`, `QUOTE`, `HEADING`

### `PlanTier`
`FREE`, `PRO`, `ENTERPRISE`

### `SentimentLabel`
`POSITIVE`, `NEGATIVE`, `NEUTRAL`

### `AnalyticsPeriod`
`DAILY`, `WEEKLY`, `MONTHLY`

---

## Error Response

```json
{
  "success": false,
  "message": "Descriptive error message",
  "data": null
}
```

Common HTTP status codes: `400` bad request, `401` unauthorized, `403` forbidden, `404` not found, `409` conflict.
