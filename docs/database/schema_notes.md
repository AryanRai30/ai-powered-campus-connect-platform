# Database Architecture Notes

## Overview
Database schema creation is deferred to later development phases.

## Guidelines for Future Entities
- Use snake_case naming for tables and columns (e.g., `user_profiles`, `created_at`).
- All primary keys should be auto-incrementing `BIGINT` IDs (`id`) or UUIDs where appropriate.
- Include auditing fields: `created_at`, `updated_at`, `is_active`.
- Foreign key constraints must be explicitly declared in Spring Data JPA entities.
