# Ai Powered Campus Connect Platform

A modern, scalable, multi-tenant digital campus ecosystem connecting students, faculty, clubs, and administrators with AI-powered features, smart recommendations, document RAG assistant, and career matching.

---

## 🚀 Tech Stack

### Frontend
- **Framework**: React.js 18 + Vite 5
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS 3
- **Routing**: React Router DOM 6
- **HTTP Client**: Axios
- **Linting**: ESLint

### Backend
- **Language**: Java 21 (JDK 21/17 compatible)
- **Framework**: Spring Boot 3.2.5
- **Web**: Spring Web
- **Data Access**: Spring Data JPA & Hibernate
- **Database**: MySQL 8.x
- **Security**: Spring Security (Dependency prepared for Phase 4 Authentication)
- **Validation**: Jakarta Validation (`spring-boot-starter-validation`)
- **Utilities**: Project Lombok
- **Build Tool**: Maven

### Database
- **Database Engine**: MySQL 8.x

---

## 📁 Project Structure

```text
Ai Powered Campus Connect Platform/
│
├── backend/                  # Spring Boot backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/campusconnect/
│   │   │   │   ├── config/       # Security, CORS, Application Beans
│   │   │   │   ├── controller/   # REST Controllers & API Endpoints
│   │   │   │   ├── service/      # Business Service Interface & Implementations
│   │   │   │   ├── repository/   # Spring Data JPA Repositories
│   │   │   │   ├── entity/       # Database Entities / Models
│   │   │   │   ├── dto/          # Data Transfer Objects
│   │   │   │   ├── mapper/       # Entity <-> DTO Mappers
│   │   │   │   ├── security/     # Authentication & Authorization Filters
│   │   │   │   ├── exception/    # Global Exception Handling
│   │   │   │   └── util/         # Helper Classes & Utilities
│   │   │   │   └── CampusConnectApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   └── pom.xml               # Maven Build File
│
├── frontend/                 # React + TypeScript + Vite + Tailwind CSS frontend
│   ├── src/
│   │   ├── assets/           # Static Media & Images
│   │   ├── components/       # Reusable React UI Components
│   │   ├── pages/            # View Pages (Home, etc.)
│   │   ├── layouts/          # Global App Layouts (Header, Footer, Navigation)
│   │   ├── routes/           # React Router Route Definitions
│   │   ├── services/         # Axios API Services & HTTP Base Config
│   │   ├── hooks/            # Custom React Hooks
│   │   ├── context/          # React Context Providers
│   │   ├── types/            # TypeScript Type & Interface Definitions
│   │   ├── utils/            # Helper Functions & Utilities
│   │   ├── App.tsx           # Main Application Container
│   │   ├── main.tsx          # React Entry Point
│   │   └── index.css         # Tailwind & Base Styles
│   ├── package.json          # Node Dependencies & Scripts
│   ├── vite.config.ts        # Vite Build Configuration
│   ├── tailwind.config.js    # Tailwind CSS Configuration
│   └── .env.example          # Environment Variables Example
│
├── docs/                     # Documentation Architecture
│   ├── requirements/         # Project Specifications & Requirements
│   ├── design/               # Architecture & System Diagrams
│   ├── database/             # ER Diagrams & Schema Documentation
│   └── api/                  # API Specifications & Endpoint Contracts
│
├── README.md                 # Project Overview & Guide
└── .gitignore                # Root Git Ignore Configuration
```

---

## 🛠️ Local Development Setup

### Prerequisites
- JDK 21 or JDK 17 installed
- Node.js 18+ and npm installed
- MySQL Server 8.x running locally or via Docker
- Apache Maven 3.8+ (or Maven wrapper)

---

### Backend Setup (`backend/`)

1. **Navigate to the backend directory**:
   ```bash
   cd backend
   ```

2. **Configure Database Credentials**:
   By default, the backend checks for environment variables `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
   You can export them in your terminal session or update `src/main/resources/application.properties`:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/campus_connect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
   export DB_USERNAME="root"
   export DB_PASSWORD="your_password"
   ```

3. **Build & Run**:
   ```bash
   mvn clean spring-boot:run
   ```
   *The backend server starts at `http://localhost:8080`.*

4. **Verify Health Check Endpoint**:
   ```bash
   curl http://localhost:8080/api/health
   ```
   **Expected Response**:
   ```json
   {
     "status": "UP",
     "application": "Ai Powered Campus Connect Platform"
   }
   ```

---

### Frontend Setup (`frontend/`)

1. **Navigate to the frontend directory**:
   ```bash
   cd frontend
   ```

2. **Create Environment File**:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   *Contains `VITE_API_BASE_URL=http://localhost:8080/api`.*

3. **Install Dependencies**:
   ```bash
   npm install
   ```

4. **Start Development Server**:
   ```bash
   npm run dev
   ```
   *The frontend starts at `http://localhost:5173`.*

---

## 📌 Current Phase

### **Phase 3 - Initial Project Setup** (Completed)
- Scalable directory structure established for frontend, backend, and docs.
- Spring Boot 3 + Maven dependency configuration with Spring Web, Data JPA, Security, Validation, Lombok, MySQL Driver.
- React 18 + TypeScript + Vite + Tailwind CSS + React Router + Axios setup.
- Backend health check REST API (`GET /api/health`).
- Centralized CORS and Axios configurations.
- Base router and UI landing page with real-time backend status checking.

---

## 🔮 Roadmap / Future Modules

Upcoming phases will incrementally implement business features:

1. **Phase 4: Authentication & User Management**
   - JWT-based authentication
   - Registration, Login, and Role-Based Access Control (RBAC: Student, Faculty, Club Admin, Super Admin)
2. **Phase 5: Student Profiles & Directory**
   - Academic details, skill tags, interest badges, public portfolios
3. **Phase 6: Campus Communication & Feed**
   - Events, Club Activities, Announcements, Discussion Forums
4. **Phase 7: Academic Resource Hub**
   - Notes sharing, syllabus repositories, past papers
5. **Phase 8: AI & Smart Services**
   - AI Campus Chatbot Assistant
   - Document RAG (Retrieval-Augmented Generation) for college handbooks
   - Intelligent Peer Matching & AI Career Recommendations
6. **Phase 9: Internship & Placement Portal**
   - Opportunities hub, resume builder, recruiter portal
7. **Phase 10: Admin Dashboard & Analytics**
   - System metrics, user moderation, report generation
