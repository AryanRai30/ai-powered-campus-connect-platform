# High-Level Architecture Overview

## System Architecture

```text
[ React.js + TS Frontend (Vite) ]
               │
               ▼ HTTP / REST (Axios)
[ Spring Boot Monolithic Backend ]
 ├── REST Controllers (`/api/*`)
 ├── Layered Services & Security
 ├── Spring Data JPA Repositories
 └── Database Abstraction
               │
               ▼ JDBC Driver
      [ MySQL 8.x Database ]
```

## Backend Layer Responsibilities
- **Controller**: Handle HTTP requests, payload validation, and HTTP responses.
- **Service**: Implement core business logic, transactions, and AI orchestrations.
- **Repository**: Spring Data JPA interfaces for database persistence.
- **Entity**: JPA database models.
- **DTO**: Data Transfer Objects for API request/response isolation.
- **Mapper**: Bidirectional conversion between Entity models and DTOs.
- **Security**: Security filters, JWT authentication token handling, and RBAC rules.
- **Exception**: Global `@RestControllerAdvice` exception handlers.
