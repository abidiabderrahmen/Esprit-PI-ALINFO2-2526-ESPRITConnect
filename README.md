# ESPRIT Connect - Alumni Networking Platform

A professional full-stack alumni platform connecting ESPRIT students, graduates, and companies.  
Built with **Angular 17** (frontend) and **Spring Boot 3.2** (backend).

---

## Features

| Module | Description |
|--------|-------------|
| **Authentication** | JWT-based login/register with role selection (Student, Alumni, Company, Admin), password reset via email |
| **Alumni Directory** | Search and filter users by skills, location, role; mentor discovery |
| **Connection System** | Send/accept/decline connection requests, mutual connections list |
| **Opportunities** | Job postings, internships, freelance projects with application system |
| **Events** | Conference, workshop, webinar, career fair management with registration |
| **News Feed** | Community posts with likes, comments, content types (achievements, articles, questions) |
| **AI Copilot** | Career guidance and job suggestions powered by Groq (Llama 3.3 70B) |
| **Job Search** | Real-time job listings from Freehire API (1.4M+ postings), skill-matched results |
| **Admin Dashboard** | Platform statistics, user management, activity monitoring |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Angular 17, Angular Material, SCSS |
| Backend | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.12.6) |
| Database | MySQL 8+ |
| AI | Groq API (Llama 3.3 70B Versatile) |
| Jobs API | Freehire API |
| Build | Maven 3.8+, npm 9+ |

---

## Project Structure

```
esprit-connect/
├── backend-spring/              # Spring Boot REST API
│   ├── src/main/java/com/esprit/
│   │   ├── config/              # Security, CORS, JWT config
│   │   ├── controller/          # REST controllers
│   │   ├── model/               # JPA entities
│   │   ├── repository/          # Spring Data repositories
│   │   ├── service/             # Business logic
│   │   └── dto/                 # Data Transfer Objects
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                    # Angular 17 SPA
│   ├── src/app/
│   │   ├── core/                # Models, services, guards, interceptors
│   │   ├── layouts/             # Main layout with sidebar
│   │   └── pages/               # Landing, auth, feed, directory, jobs, etc.
│   ├── angular.json
│   └── package.json
├── .env.example                 # Environment variables template
├── .gitignore
└── README.md
```

---

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Node.js 18+** and **npm 9+**
- **MySQL 8+** (running on `localhost:3306`)
- **Groq API Key** ([console.groq.com](https://console.groq.com)) for AI features

---

## Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/abidiabderrahmen/Esprit-PI-ALINFO2-2526-ESPRITConnect.git
cd Esprit-PI-ALINFO2-2526-ESPRITConnect
```

### 2. Configure environment variables

```bash
cp .env.example .env
# Edit .env and fill in your Groq API key, JWT secret, and email credentials
```

Then update `backend-spring/src/main/resources/application.properties` with your values, or set them as system environment variables.

### 3. Backend Setup (Spring Boot)

```bash
cd backend-spring

# Build the project
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

The API will be available at **http://localhost:8000/api/**

### 4. Frontend Setup (Angular)

```bash
cd frontend

# Install dependencies
npm install

# Start development server
ng serve
```

The app will be available at **http://localhost:4200/**

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | Authenticate and get JWT token |
| `/api/auth/register` | POST | Register a new user |
| `/api/auth/refresh` | POST | Refresh JWT token |
| `/api/users/me` | GET/PUT | Current user profile |
| `/api/users/` | GET | List/search users |
| `/api/connections/` | GET/POST | Connection requests |
| `/api/opportunities/` | GET/POST | Job/internship listings |
| `/api/events/` | GET/POST | Events management |
| `/api/feed/` | GET/POST | News feed posts |
| `/api/ai/suggestions` | POST | AI career suggestions |
| `/api/jobs/search` | GET | Real-time job search (Freehire) |
| `/api/dashboard/stats` | GET | Admin statistics |

---

## User Roles

| Role | Access |
|------|--------|
| **Student** | Browse directory, apply to jobs, join events, post in feed |
| **Alumni** | All student features + post opportunities, mentor others |
| **Company** | Post opportunities, browse directory, manage events |
| **Admin** | Full platform management, dashboard statistics |

---

## Authors

- **Abderrahmen Abidi** - ESPRIT School of Engineering - 1ALINFO2

---

## License

This project was developed as part of the ESPRIT Engineering curriculum (2025-2026).
