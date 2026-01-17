# MindTrack Journal

> **A secure, private, and intelligent journaling application where every feature is designed to protect user data and build trust.**

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

---

## Overview

MindTrack Journal is a modern journaling application built with a "Privacy First" philosophy. We believe your thoughts should remain yours - encrypted, protected, and private. This application combines a seamless, distraction-free writing experience with robust security measures and private, self-hosted AI analytics.

## Features

### Core User Experience

We designed the core journaling experience to be simple, secure, and distraction-free.

- **Private Journaling Requirements:** The interface provides a clean canvas for your thoughts. It includes a Notion-style rich text editor supporting bold, italics, code blocks, and lists, allowing you to express yourself without limitations. You have full control to view, edit, and permanent delete your entries.
- **Secure Account Management:** From registration to account deletion, every step is built with security in mind. We provide secure password resets via time-limited links and a comprehensive "right to be forgotten" feature that permanently erases all your data upon request.

### AI & Analytics

The application provides intelligent insights into your emotional trends without compromising your privacy.

- **Private Sentiment Analysis:** Your journal entries are automatically analyzed for sentiment (Positive, Negative, Neutral). Crucially, this is handled by a **self-hosted AI service**, ensuring your private text never leaves your infrastructure or is shared with third-party AI providers.
- **Personal Analytics Dashboard:** Visualize your emotional journey with sentiment trend charts and a mood calendar. These insights are available strictly to you, securely rendered on your private dashboard.

### Security & Privacy

This is the foundation of MindTrack Journal. We employ industry-standard security practices to ensure your data is unreadable to anyone but you.

- **Authentication:** We use BCrypt for password hashing and stateless, cryptographically signed JWTs stored in secure, HTTP-only cookies to prevent common web attacks like XSS.
- **Strict Data Ownership:** The backend enforces rigorous access controls, ensuring that users can only access their own data.
- **End-to-End Protection:**
  - **In Transit:** All communication is secured via mandatory HTTPS/TLS.
  - **At Rest:** The database uses encryption at rest.
  - **Application Level:** Content is encrypted by the application *before* it reaches the database. Even if the database were compromised, your journal entries would remain unreadable.
- **Network Security:** We implement strict CORS policies, security headers (CSP, HSTS), and rate limiting on sensitive endpoints to protect against brute-force attacks.

### Technical Architecture

MindTrack Journal is engineered for scalability and maintainability.

- **Monorepo Architecture:** The Frontend, Backend, and ML services are managed in a single repository for streamlined development.
- **Containerization:** Every service—Java Backend, Next.js Frontend, Python ML Service, and PostgreSQL—is fully dockerized.
- **CI/CD & Testing:** Automated pipelines handle testing, building, and deployment, supported by a comprehensive suite of unit, integration, and component tests across all services.

---

## Roadmap

- **Import from Notion:** We are building tools to seamlessly migrate your existing journals from Notion.
- **Shariah-Compliant Payments:** Upcoming support for Tabby, Tamara, and PayHalal to provide ethical payment options.

---

## Architecture

### System Architecture
*[System Architecture Image Here]*

### Database Schema
*[Database Architecture Image Here]*

---

## Tech Stack

- **Frontend:** Next.js, React (Modern, responsive UI with SSR)
- **Backend:** Java, Spring Boot (Robust REST API with business logic and encryption)
- **ML Service:** Python, FastAPI (High-performance local sentiment analysis)
- **Database:** PostgreSQL (Relational storage with encryption support)
- **DevOps:** Docker, GitHub Actions (Containerization and CI/CD)

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local logic dev)
- Node.js 18+ (for local frontend dev)
- Python 3.9+ (for local ML dev)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/syedsadiquh/mindtrack-journal.git
   cd mindtrack-journal
   ```

2. **Start the application (Docker):**
   ```bash
   docker-compose up --build
   ```

3. **Access the services:**
   - Frontend: `http://localhost:3000`
   - Backend API: `http://localhost:2000`
   - ML Service: `http://localhost:8000`


