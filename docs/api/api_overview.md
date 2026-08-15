# REST API Specification Overview

## Base URL
Local Development: `http://localhost:8080/api`

## Phase 3 Active Endpoints

### Health Check
- **Endpoint**: `GET /api/health`
- **Authentication**: None (Public)
- **Response Format**: `application/json`
- **Status Code**: `200 OK`
- **Sample Response**:
  ```json
  {
    "status": "UP",
    "application": "Ai Powered Campus Connect Platform"
  }
  ```

## Future API Modules
- `/api/v1/auth/*` - Authentication & JWT token management
- `/api/v1/users/*` - Student & Faculty user profile management
- `/api/v1/events/*` - Campus events and announcements
- `/api/v1/resources/*` - Academic document repository
- `/api/v1/ai/*` - AI chatbot, document RAG, and recommendations
